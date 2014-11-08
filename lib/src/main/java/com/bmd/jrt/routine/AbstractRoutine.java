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
import com.bmd.jrt.channel.ParameterChannel;
import com.bmd.jrt.common.RoutineInterruptedException;
import com.bmd.jrt.invocation.Invocation;
import com.bmd.jrt.log.Log;
import com.bmd.jrt.log.Log.LogLevel;
import com.bmd.jrt.log.Logger;
import com.bmd.jrt.routine.DefaultParameterChannel.InvocationManager;
import com.bmd.jrt.runner.Runner;
import com.bmd.jrt.time.TimeDuration;
import com.bmd.jrt.time.TimeDuration.Check;

import java.util.LinkedList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Basic abstract implementation of a routine.
 * <p/>
 * This class provides implementations for all the routine functionalities. The inheriting class
 * just need to creates invocation objects when required.
 * <p/>
 * Created by davide on 9/7/14.
 *
 * @param <INPUT>  the input type.
 * @param <OUTPUT> the output type.
 */
public abstract class AbstractRoutine<INPUT, OUTPUT> extends BasicRoutine<INPUT, OUTPUT> {

    private final Runner mAsyncRunner;

    private final TimeDuration mAvailTimeout;

    private final LinkedList<Invocation<INPUT, OUTPUT>> mInvocations =
            new LinkedList<Invocation<INPUT, OUTPUT>>();

    private final Logger mLogger;

    private final int mMaxRetained;

    private final int mMaxRunning;

    private final Object mMutex = new Object();

    private final boolean mOrderedInput;

    private final boolean mOrderedOutput;

    private final Runner mSyncRunner;

    private int mRunningCount;

    /**
     * Constructor.
     *
     * @param syncRunner    the runner used for synchronous invocation.
     * @param asyncRunner   the runner used for asynchronous invocation.
     * @param maxRunning    the maximum number of parallel running invocations. Must be positive.
     * @param maxRetained   the maximum number of retained invocation instances. Must be 0 or a
     *                      positive number.
     * @param availTimeout  the maximum timeout while waiting for an invocation instance to be
     *                      available.
     * @param orderedInput  whether the input data are forced to be delivered in insertion order.
     * @param orderedOutput whether the output data are forced to be delivered in insertion order.
     * @param log           the log instance.
     * @param logLevel      the log level.
     * @throws NullPointerException     if one of the parameters is null.
     * @throws IllegalArgumentException if at least one of the parameter is invalid.
     */
    @SuppressWarnings("ConstantConditions")
    protected AbstractRoutine(@Nonnull final Runner syncRunner, @Nonnull final Runner asyncRunner,
            final int maxRunning, final int maxRetained, @Nonnull final TimeDuration availTimeout,
            final boolean orderedInput, final boolean orderedOutput, @Nonnull final Log log,
            @Nonnull final LogLevel logLevel) {

        if (syncRunner == null) {

            throw new NullPointerException("the synchronous runner instance must not be null");
        }

        if (asyncRunner == null) {

            throw new NullPointerException("the asynchronous runner instance must not be null");
        }

        if (maxRunning < 1) {

            throw new IllegalArgumentException(
                    "the maximum number of parallel running invocations must be a positive number");
        }

        if (maxRetained < 0) {

            throw new IllegalArgumentException(
                    "the maximum number of retained invocation instances must be 0 or positive");
        }

        if (availTimeout == null) {

            throw new NullPointerException(
                    "the timeout for available invocation instances must not be null");
        }

        mSyncRunner = syncRunner;
        mAsyncRunner = asyncRunner;
        mMaxRunning = maxRunning;
        mMaxRetained = maxRetained;
        mAvailTimeout = availTimeout;
        mOrderedInput = orderedInput;
        mOrderedOutput = orderedOutput;
        mLogger = Logger.create(log, logLevel, this);
    }

    /**
     * Constructor.
     *
     * @param syncRunner    the runner used for synchronous invocation.
     * @param asyncRunner   the runner used for asynchronous invocation.
     * @param maxRunning    the maximum number of parallel running invocations. Must be positive.
     * @param maxRetained   the maximum number of retained invocation instances. Must be 0 or a
     *                      positive number.
     * @param availTimeout  the maximum timeout while waiting for an invocation instance to be
     *                      available.
     * @param orderedInput  whether the input data are forced to be delivered in insertion order.
     * @param orderedOutput whether the output data are forced to be delivered in insertion order.
     * @param logger        the logger instance.
     */
    private AbstractRoutine(@Nonnull final Runner syncRunner, @Nonnull final Runner asyncRunner,
            final int maxRunning, final int maxRetained, @Nonnull final TimeDuration availTimeout,
            final boolean orderedInput, final boolean orderedOutput, @Nonnull final Logger logger) {

        mSyncRunner = syncRunner;
        mAsyncRunner = asyncRunner;
        mMaxRunning = maxRunning;
        mMaxRetained = maxRetained;
        mAvailTimeout = availTimeout;
        mOrderedInput = orderedInput;
        mOrderedOutput = orderedOutput;
        mLogger = logger;
    }

    @Override
    @Nonnull
    public ParameterChannel<INPUT, OUTPUT> invoke() {

        return invoke(false);
    }

    @Override
    @Nonnull
    public ParameterChannel<INPUT, OUTPUT> invokeAsync() {

        return invoke(true);
    }

    @Override
    @Nonnull
    public ParameterChannel<INPUT, OUTPUT> invokeParallel() {

        mLogger.dbg("invoking routine: parallel");

        final AbstractRoutine<INPUT, OUTPUT> parallelRoutine =
                new AbstractRoutine<INPUT, OUTPUT>(mSyncRunner, mAsyncRunner, mMaxRunning,
                                                   mMaxRetained, mAvailTimeout, mOrderedInput,
                                                   mOrderedOutput, mLogger) {

                    @Override
                    @Nonnull
                    protected Invocation<INPUT, OUTPUT> createInvocation(final boolean async) {

                        return new ParallelInvocation<INPUT, OUTPUT>(AbstractRoutine.this);
                    }
                };

        return parallelRoutine.invokeAsync();
    }

    @Override
    @Nonnull
    public OutputChannel<OUTPUT> runParallel(@Nullable final Iterable<? extends INPUT> inputs) {

        return invokeParallel().pass(inputs).results();
    }

    @Override
    @Nonnull
    public OutputChannel<OUTPUT> runParallel(@Nullable final OutputChannel<INPUT> inputs) {

        return invokeParallel().pass(inputs).results();
    }

    /**
     * Creates a new invocation instance.
     *
     * @param async whether the invocation is asynchronous.
     * @return the invocation instance.
     */
    @Nonnull
    protected abstract Invocation<INPUT, OUTPUT> createInvocation(boolean async);

    @Nonnull
    private ParameterChannel<INPUT, OUTPUT> invoke(final boolean async) {

        final Logger logger = mLogger;

        logger.dbg("invoking routine: %ssync", (async) ? "a" : "");

        return new DefaultParameterChannel<INPUT, OUTPUT>(new DefaultInvocationManager(async),
                                                          (async) ? mAsyncRunner : mSyncRunner,
                                                          mOrderedInput, mOrderedOutput, logger);
    }

    /**
     * Default implementation of an invocation manager supporting recycling of invocation instances.
     */
    private class DefaultInvocationManager implements InvocationManager<INPUT, OUTPUT> {

        private final boolean mAsync;

        /**
         * Constructor.
         *
         * @param async whether the invocation is asynchronous.
         */
        private DefaultInvocationManager(final boolean async) {

            mAsync = async;
        }

        @Override
        @Nonnull
        public Invocation<INPUT, OUTPUT> create() {

            synchronized (mMutex) {

                boolean isTimeout = false;

                try {

                    final int maxRunning = mMaxRunning;

                    isTimeout = !mAvailTimeout.waitTrue(mMutex, new Check() {

                        @Override
                        public boolean isTrue() {

                            return mRunningCount < maxRunning;
                        }
                    });

                } catch (final InterruptedException e) {

                    mLogger.err(e, "waiting for available instance interrupted [#%d]", mMaxRunning);

                    RoutineInterruptedException.interrupt(e);
                }

                if (isTimeout) {

                    mLogger.wrn("routine instance not available after timeout [#%d]: %s",
                                mMaxRunning, mAvailTimeout);

                    throw new RoutineNotAvailableException();
                }

                ++mRunningCount;

                final LinkedList<Invocation<INPUT, OUTPUT>> invocations = mInvocations;

                if (!invocations.isEmpty()) {

                    final Invocation<INPUT, OUTPUT> invocation = invocations.removeFirst();

                    mLogger.dbg("reusing invocation instance [%d/%d]: %s", invocations.size() + 1,
                                mMaxRetained, invocation);

                    return invocation;
                }

                mLogger.dbg("creating invocation instance [1/%d]", mMaxRetained);

                return createInvocation(mAsync);
            }
        }

        @Override
        @SuppressFBWarnings(value = "NO_NOTIFY_NOT_NOTIFYALL",
                            justification = "only one invocation is released")
        public void discard(@Nonnull final Invocation<INPUT, OUTPUT> invocation) {

            synchronized (mMutex) {

                mLogger.wrn("discarding invocation instance after error: %s", invocation);

                --mRunningCount;
                mMutex.notify();
            }
        }

        @Override
        @SuppressFBWarnings(value = "NO_NOTIFY_NOT_NOTIFYALL",
                            justification = "only one invocation is released")
        public void recycle(@Nonnull final Invocation<INPUT, OUTPUT> invocation) {

            synchronized (mMutex) {

                final LinkedList<Invocation<INPUT, OUTPUT>> invocations = mInvocations;

                if (invocations.size() < mMaxRetained) {

                    mLogger.dbg("recycling invocation instance [%d/%d]: %s", invocations.size() + 1,
                                mMaxRetained, invocation);

                    invocations.add(invocation);

                } else {

                    mLogger.wrn("discarding invocation instance [%d/%d]: %s", mMaxRetained,
                                mMaxRetained, invocation);
                }

                --mRunningCount;
                mMutex.notify();
            }
        }
    }
}
