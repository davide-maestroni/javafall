/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmd.jrt.routine;

import com.bmd.jrt.channel.OutputChannel;
import com.bmd.jrt.channel.OutputConsumer;
import com.bmd.jrt.channel.RoutineChannel;
import com.bmd.jrt.execution.Execution;
import com.bmd.jrt.routine.DefaultInvocation.InputIterator;
import com.bmd.jrt.routine.DefaultResultChannel.AbortHandler;
import com.bmd.jrt.runner.Invocation;
import com.bmd.jrt.runner.Runner;
import com.bmd.jrt.time.TimeDuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.bmd.jrt.time.TimeDuration.ZERO;
import static com.bmd.jrt.time.TimeDuration.fromUnit;

/**
 * Default implementation of a routine input channel.
 * <p/>
 * Created by davide on 9/24/14.
 *
 * @param <INPUT>  the input type.
 * @param <OUTPUT> the output type.
 */
class DefaultRoutineChannel<INPUT, OUTPUT> implements RoutineChannel<INPUT, OUTPUT> {

    private final SimpleQueue<INPUT> mInputQueue = new SimpleQueue<INPUT>();

    private final DefaultInvocation<INPUT, OUTPUT> mInvocation;

    private final Object mMutex = new Object();

    private final DefaultResultChannel<OUTPUT> mResultChanel;

    private final Runner mRunner;

    private Throwable mAbortException;

    private ArrayList<OutputChannel<?>> mBoundChannels = new ArrayList<OutputChannel<?>>();

    private TimeDuration mInputDelay = ZERO;

    private int mPendingInputCount;

    private ChannelState mState = ChannelState.INPUT;

    /**
     * Constructor.
     *
     * @param provider the execution provider.
     * @param runner   the runner instance.
     * @throws java.lang.IllegalArgumentException if one of the parameters is null.
     */
    public DefaultRoutineChannel(final ExecutionProvider<INPUT, OUTPUT> provider,
            final Runner runner) {

        mRunner = runner;
        mResultChanel = new DefaultResultChannel<OUTPUT>(new AbortHandler() {

            @Override
            public void onAbort(final Throwable throwable) {

                synchronized (mMutex) {

                    if (mState == ChannelState.EXCEPTION) {

                        return;
                    }

                    mInputQueue.clear();

                    mAbortException = throwable;
                    mState = ChannelState.EXCEPTION;
                }

                mRunner.runAbort(mInvocation);
            }
        }, runner);
        mInvocation = new DefaultInvocation<INPUT, OUTPUT>(provider, new DefaultInputIterator(),
                                                           mResultChanel);
    }

    @Override
    public boolean abort() {

        return abort(null);
    }

    @Override
    public boolean abort(final Throwable throwable) {

        synchronized (mMutex) {

            if (!isOpen()) {

                return false;
            }

            mInputQueue.clear();

            mAbortException = throwable;
            mState = ChannelState.EXCEPTION;
        }

        mRunner.runAbort(mInvocation);

        return false;
    }

    @Override
    public boolean isOpen() {

        synchronized (mMutex) {

            return (mState == ChannelState.INPUT);
        }
    }

    @Override
    public RoutineChannel<INPUT, OUTPUT> after(final TimeDuration delay) {

        synchronized (mMutex) {

            verifyInput();

            if (delay == null) {

                throw new IllegalArgumentException();
            }

            mInputDelay = delay;
        }

        return this;
    }

    @Override
    public RoutineChannel<INPUT, OUTPUT> after(final long delay, final TimeUnit timeUnit) {

        return after(fromUnit(delay, timeUnit));
    }

    @Override
    public RoutineChannel<INPUT, OUTPUT> pass(final OutputChannel<INPUT> channel) {

        final TimeDuration delay;

        synchronized (mMutex) {

            verifyInput();

            if (channel == null) {

                return this;
            }

            mBoundChannels.add(channel);

            delay = mInputDelay;

            ++mPendingInputCount;
        }

        channel.bind(new DefaultOutputConsumer(delay));

        return this;
    }

    @Override
    public RoutineChannel<INPUT, OUTPUT> pass(final Iterable<? extends INPUT> inputs) {

        final TimeDuration delay;

        synchronized (mMutex) {

            verifyInput();

            if (inputs == null) {

                return this;
            }

            delay = mInputDelay;

            if (delay.isZero()) {

                final SimpleQueue<INPUT> inputQueue = mInputQueue;

                for (final INPUT input : inputs) {

                    inputQueue.add(input);
                }
            }

            ++mPendingInputCount;
        }

        if (delay.isZero()) {

            mRunner.run(mInvocation, 0, TimeUnit.MILLISECONDS);

        } else {

            mRunner.run(new DelayedListInputInvocation(inputs), delay.time, delay.unit);
        }

        return this;
    }

    @Override
    public RoutineChannel<INPUT, OUTPUT> pass(final INPUT input) {

        final TimeDuration delay;

        synchronized (mMutex) {

            verifyInput();

            delay = mInputDelay;

            if (delay.isZero()) {

                mInputQueue.add(input);
            }

            ++mPendingInputCount;
        }

        if (delay.isZero()) {

            mRunner.run(mInvocation, 0, TimeUnit.MILLISECONDS);

        } else {

            mRunner.run(new DelayedInputInvocation(input), delay.time, delay.unit);
        }

        return this;
    }

    @Override
    public RoutineChannel<INPUT, OUTPUT> pass(final INPUT... inputs) {

        synchronized (mMutex) {

            verifyInput();

            if (inputs == null) {

                return this;
            }
        }

        return pass(Arrays.asList(inputs));
    }

    @Override
    public OutputChannel<OUTPUT> close() {

        synchronized (mMutex) {

            verifyInput();

            mState = ChannelState.OUTPUT;

            ++mPendingInputCount;
        }

        mRunner.run(mInvocation, 0, TimeUnit.MILLISECONDS);

        return mResultChanel.getOutput();
    }

    private void verifyInput() {

        final Throwable throwable = mAbortException;

        if (throwable != null) {

            throw RoutineExceptionWrapper.wrap(throwable).raise();
        }

        if (!isOpen()) {

            throw new IllegalStateException();
        }
    }

    /**
     * Enumeration identifying the channel internal state.
     */
    private static enum ChannelState {

        INPUT,
        OUTPUT,
        EXCEPTION
    }

    /**
     * Interface defining an object managing the creation and the recycling of execution instances.
     * <p/>
     * Created by davide on 9/24/14.
     *
     * @param <INPUT>  the input type.
     * @param <OUTPUT> the output type.
     */
    public interface ExecutionProvider<INPUT, OUTPUT> {

        /**
         * Creates and returns a new execution instance.
         *
         * @return the execution instance.
         */
        public Execution<INPUT, OUTPUT> create();

        /**
         * Discards the specified execution.
         *
         * @param execution the execution instance.
         */
        public void discard(Execution<INPUT, OUTPUT> execution);

        /**
         * Recycles the specified execution.
         *
         * @param execution the execution instance.
         */
        public void recycle(Execution<INPUT, OUTPUT> execution);
    }

    /**
     * Default implementation of an input iterator.
     */
    private class DefaultInputIterator implements InputIterator<INPUT> {

        @Override
        public Throwable getAbortException() {

            synchronized (mMutex) {

                return mAbortException;
            }
        }

        @Override
        public boolean isAborting() {

            synchronized (mMutex) {

                return (mState == ChannelState.EXCEPTION);
            }
        }

        @Override
        public boolean isComplete() {

            synchronized (mMutex) {

                return (mState == ChannelState.OUTPUT) && (mPendingInputCount <= 0);
            }
        }

        @Override
        public void onAbortComplete() {

            final Throwable exception;
            final ArrayList<OutputChannel<?>> channels;

            synchronized (mMutex) {

                exception = mAbortException;

                channels = new ArrayList<OutputChannel<?>>(mBoundChannels);
                mBoundChannels.clear();
            }

            for (final OutputChannel<?> channel : channels) {

                channel.abort(exception);
            }
        }

        @Override
        public boolean onConsumeInput() {

            synchronized (mMutex) {

                if ((mState != ChannelState.INPUT) && (mState != ChannelState.OUTPUT)) {

                    return false;
                }

                --mPendingInputCount;
            }

            return true;
        }

        @Override
        public boolean hasNext() {

            synchronized (mMutex) {

                return !mInputQueue.isEmpty();
            }
        }

        @Override
        public INPUT next() {

            synchronized (mMutex) {

                return mInputQueue.removeFirst();
            }
        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();
        }
    }

    /**
     * Default implementation of an output consumer pushing the consume data into the input
     * channel queue.
     */
    private class DefaultOutputConsumer implements OutputConsumer<INPUT> {

        private final TimeDuration mDelay;

        /**
         * Constructor.
         *
         * @param delay the output delay.
         */
        public DefaultOutputConsumer(final TimeDuration delay) {

            mDelay = delay;
        }

        @Override
        public void onAbort(final Throwable throwable) {

            synchronized (mMutex) {

                if (!isOpen() && (mState != ChannelState.OUTPUT)) {

                    return;
                }

                mInputQueue.clear();

                mAbortException = throwable;
                mState = ChannelState.EXCEPTION;
            }

            mRunner.runAbort(mInvocation);
        }

        @Override
        public void onClose() {

            mRunner.run(mInvocation, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        public void onOutput(final INPUT output) {

            final TimeDuration delay = mDelay;

            synchronized (mMutex) {

                final Throwable throwable = mAbortException;

                if (throwable != null) {

                    throw RoutineExceptionWrapper.wrap(throwable).raise();
                }

                if (!isOpen() && (mState != ChannelState.OUTPUT)) {

                    throw new IllegalStateException();
                }

                if (delay.isZero()) {

                    mInputQueue.add(output);
                }

                ++mPendingInputCount;
            }

            if (delay.isZero()) {

                mRunner.run(mInvocation, 0, TimeUnit.MILLISECONDS);

            } else {

                mRunner.run(new DelayedInputInvocation(output), delay.time, delay.unit);
            }
        }
    }

    /**
     * Implementation of an invocation handling a delayed input.
     */
    private class DelayedInputInvocation implements Invocation {

        private final INPUT mInput;

        /**
         * Constructor.
         *
         * @param input the input.
         */
        public DelayedInputInvocation(final INPUT input) {

            mInput = input;
        }

        @Override
        public void abort() {

            throw new UnsupportedOperationException();
        }

        @Override
        public void run() {

            synchronized (mMutex) {

                if ((mState != ChannelState.INPUT) && (mState != ChannelState.OUTPUT)) {

                    return;
                }

                mInputQueue.add(mInput);
            }

            mInvocation.run();
        }
    }

    /**
     * Implementation of an invocation handling a delayed input of a list of data.
     */
    private class DelayedListInputInvocation implements Invocation {

        private final ArrayList<INPUT> mInputs;

        /**
         * Constructor.
         *
         * @param inputs the iterable returning the input data.
         */
        public DelayedListInputInvocation(final Iterable<? extends INPUT> inputs) {

            final ArrayList<INPUT> inputList = new ArrayList<INPUT>();

            for (final INPUT input : inputs) {

                inputList.add(input);
            }

            mInputs = inputList;
        }

        @Override
        public void abort() {

            throw new UnsupportedOperationException();
        }

        @Override
        public void run() {

            synchronized (mMutex) {

                if ((mState != ChannelState.INPUT) && (mState != ChannelState.OUTPUT)) {

                    return;
                }

                mInputQueue.addAll(mInputs);
            }

            mInvocation.run();
        }
    }
}