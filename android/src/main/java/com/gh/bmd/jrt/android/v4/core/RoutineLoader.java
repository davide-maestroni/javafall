/*
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
package com.gh.bmd.jrt.android.v4.core;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.gh.bmd.jrt.android.invocation.ContextInvocation;
import com.gh.bmd.jrt.builder.RoutineConfiguration.OrderType;
import com.gh.bmd.jrt.channel.OutputChannel;
import com.gh.bmd.jrt.channel.ResultChannel;
import com.gh.bmd.jrt.channel.TransportChannel;
import com.gh.bmd.jrt.channel.TransportChannel.TransportInput;
import com.gh.bmd.jrt.common.AbortException;
import com.gh.bmd.jrt.common.InvocationException;
import com.gh.bmd.jrt.common.InvocationInterruptedException;
import com.gh.bmd.jrt.log.Logger;
import com.gh.bmd.jrt.time.TimeDuration;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Loader implementation performing the routine invocation.
 * <p/>
 * Created by davide-maestroni on 12/8/14.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
class RoutineLoader<INPUT, OUTPUT> extends AsyncTaskLoader<InvocationResult<OUTPUT>> {

    private final Object[] mArgs;

    private final List<? extends INPUT> mInputs;

    private final ContextInvocation<INPUT, OUTPUT> mInvocation;

    private final String mInvocationType;

    private final Logger mLogger;

    private final OrderType mOrderType;

    private int mInvocationCount;

    private InvocationResult<OUTPUT> mResult;

    /**
     * Constructor.
     *
     * @param context        used to retrieve the application context.
     * @param invocation     the invocation instance.
     * @param invocationType the invocation type.
     * @param args           the invocation factory arguments.
     * @param inputs         the input data.
     * @param order          the data order.
     * @param logger         the logger instance.
     */
    @SuppressWarnings("ConstantConditions")
    RoutineLoader(@Nonnull final Context context,
            @Nonnull final ContextInvocation<INPUT, OUTPUT> invocation,
            @Nonnull final String invocationType, @Nonnull final Object[] args,
            @Nonnull final List<? extends INPUT> inputs, @Nullable final OrderType order,
            @Nonnull final Logger logger) {

        super(context);

        if (invocation == null) {

            throw new NullPointerException("the invocation instance must not be null");
        }

        if (invocationType == null) {

            throw new NullPointerException("the invocation type must not be null");
        }

        if (args == null) {

            throw new NullPointerException("the array of arguments must not be null");
        }

        if (inputs == null) {

            throw new NullPointerException("the list of input data must not be null");
        }

        mInvocation = invocation;
        mInvocationType = invocationType;
        mArgs = args;
        mInputs = inputs;
        mOrderType = order;
        mLogger = logger.subContextLogger(this);
    }

    /**
     * Checks if the loader inputs are equal to the specified ones.
     *
     * @param inputs the input data.
     * @return whether the inputs are equal.
     */
    public boolean areSameInputs(@Nullable final List<? extends INPUT> inputs) {

        return mInputs.equals(inputs);
    }

    @Override
    public void deliverResult(final InvocationResult<OUTPUT> data) {

        mLogger.dbg("delivering result: %s", data);
        mResult = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {

        super.onStartLoading();
        mLogger.dbg("start background invocation");

        if (mResult != null) {

            deliverResult(mResult);
        }

        if (takeContentChanged() || (mResult == null)) {

            forceLoad();
        }
    }

    @Override
    protected void onReset() {

        try {

            mInvocation.onDestroy();

        } catch (final InvocationInterruptedException e) {

            throw e;

        } catch (final Throwable ignored) {

            mLogger.wrn(ignored, "ignoring exception while destroying invocation instance");
        }

        mLogger.dbg("resetting result");
        mResult = null;
        super.onReset();
    }

    @Override
    public InvocationResult<OUTPUT> loadInBackground() {

        final Logger logger = mLogger;
        final ContextInvocation<INPUT, OUTPUT> invocation = mInvocation;
        final LoaderResultChannel<OUTPUT> channel =
                new LoaderResultChannel<OUTPUT>(mOrderType, logger);
        final InvocationOutputConsumer<OUTPUT> consumer =
                new InvocationOutputConsumer<OUTPUT>(this, logger);
        channel.output().passTo(consumer);
        Throwable abortException = null;
        logger.dbg("running invocation");

        try {

            invocation.onInitialize();

            for (final INPUT input : mInputs) {

                invocation.onInput(input, channel);
                abortException = channel.getAbortException();

                if (abortException != null) {

                    break;
                }
            }

            if (abortException == null) {

                invocation.onResult(channel);
                abortException = channel.getAbortException();
            }

        } catch (final InvocationException e) {

            abortException = e.getCause();

        } catch (final Throwable t) {

            abortException = t;
        }

        if (abortException != null) {

            logger.dbg(abortException, "aborting invocation");

            try {

                invocation.onAbort(abortException);
                invocation.onTerminate();

            } catch (final InvocationException e) {

                abortException = e.getCause();

            } catch (final Throwable t) {

                abortException = t;
            }

            logger.dbg(abortException, "aborted invocation");
            channel.abort(abortException);
            return consumer.createResult();

        } else {

            try {

                invocation.onTerminate();

            } catch (final InvocationInterruptedException e) {

                throw e;

            } catch (final Throwable ignored) {

            }
        }

        logger.dbg("reading invocation results");
        channel.close();
        return consumer.createResult();
    }

    /**
     * Gets the constructor arguments of this loader invocation.
     *
     * @return the array of arguments.
     */
    @Nonnull
    Object[] getInvocationArgs() {

        return mArgs;
    }

    /**
     * Gets this loader invocation count.
     *
     * @return the invocation count.
     */
    int getInvocationCount() {

        return mInvocationCount;
    }

    /**
     * Sets the invocation count.
     *
     * @param count the invocation count.
     */
    void setInvocationCount(final int count) {

        mInvocationCount = count;
    }

    /**
     * Returns the loader invocation type.
     *
     * @return the invocation type.
     */
    @Nonnull
    String getInvocationType() {

        return mInvocationType;
    }

    /**
     * Loader result channel.
     *
     * @param <OUTPUT> the output data type.
     */
    private static class LoaderResultChannel<OUTPUT> implements ResultChannel<OUTPUT> {

        private final AtomicReference<Throwable> mAbortException = new AtomicReference<Throwable>();

        private final TransportChannel<OUTPUT> mTransportChannel;

        private final TransportInput<OUTPUT> mTransportInput;

        /**
         * Constructor.
         *
         * @param order  the data order.
         * @param logger the logger instance.
         */
        private LoaderResultChannel(@Nullable final OrderType order, @Nonnull final Logger logger) {

            mTransportChannel = JRoutine.transport()
                                        .withRoutine()
                                        .withOutputOrder(order)
                                        .withOutputMaxSize(Integer.MAX_VALUE)
                                        .withOutputTimeout(TimeDuration.ZERO)
                                        .withLog(logger.getLog())
                                        .withLogLevel(logger.getLogLevel())
                                        .set()
                                        .buildChannel();
            mTransportInput = mTransportChannel.input();
        }

        public boolean abort() {

            final boolean isAbort = mTransportInput.abort();

            if (isAbort) {

                mAbortException.set(new AbortException(null));
            }

            return isAbort;
        }

        public boolean abort(@Nullable final Throwable reason) {

            final boolean isAbort = mTransportInput.abort(reason);

            if (isAbort) {

                mAbortException.set(new AbortException(reason));
            }

            return isAbort;
        }

        public boolean isOpen() {

            return mTransportInput.isOpen();
        }

        @Nonnull
        public ResultChannel<OUTPUT> after(@Nonnull final TimeDuration delay) {

            mTransportInput.after(delay);
            return this;
        }

        @Nonnull
        public ResultChannel<OUTPUT> after(final long delay, @Nonnull final TimeUnit timeUnit) {

            mTransportInput.after(delay, timeUnit);
            return this;
        }

        @Nonnull
        public ResultChannel<OUTPUT> now() {

            mTransportInput.now();
            return this;
        }

        @Nonnull
        public ResultChannel<OUTPUT> pass(@Nullable final OutputChannel<? extends OUTPUT> channel) {

            mTransportInput.pass(channel);
            return this;
        }

        @Nonnull
        public ResultChannel<OUTPUT> pass(@Nullable final Iterable<? extends OUTPUT> outputs) {

            mTransportInput.pass(outputs);
            return this;
        }

        @Nonnull
        public ResultChannel<OUTPUT> pass(@Nullable final OUTPUT output) {

            mTransportInput.pass(output);
            return this;
        }

        @Nonnull
        public ResultChannel<OUTPUT> pass(@Nullable final OUTPUT... outputs) {

            mTransportInput.pass(outputs);
            return this;
        }

        private void close() {

            mTransportInput.close();
        }

        @Nullable
        private Throwable getAbortException() {

            return mAbortException.get();
        }

        @Nonnull
        private OutputChannel<OUTPUT> output() {

            return mTransportChannel.output();
        }
    }
}
