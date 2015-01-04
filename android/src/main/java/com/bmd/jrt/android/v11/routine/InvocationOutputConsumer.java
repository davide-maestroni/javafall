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
package com.bmd.jrt.android.v11.routine;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;

import com.bmd.jrt.channel.IOChannel.IOChannelInput;
import com.bmd.jrt.channel.TemplateOutputConsumer;
import com.bmd.jrt.common.RoutineException;
import com.bmd.jrt.common.RoutineInterruptedException;
import com.bmd.jrt.log.Logger;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class storing the invocation loader results.
 * <p/>
 * Created by davide on 12/12/14.
 *
 * @param <OUTPUT> the output data type.
 */
@TargetApi(VERSION_CODES.HONEYCOMB)
class InvocationOutputConsumer<OUTPUT> extends TemplateOutputConsumer<OUTPUT> {

    private final ArrayList<OUTPUT> mCachedResults = new ArrayList<OUTPUT>();

    private final ArrayList<OUTPUT> mLastResults = new ArrayList<OUTPUT>();

    private final RoutineLoader<?, OUTPUT> mLoader;

    private final Logger mLogger;

    private final Object mMutex = new Object();

    private RoutineException mAbortException;

    private boolean mIsComplete;

    /**
     * Constructor.
     *
     * @param loader the loader instance.
     * @param logger the logger instance.
     * @throws NullPointerException if any of the parameter is null.
     */
    @SuppressWarnings("ConstantConditions")
    InvocationOutputConsumer(@Nonnull final RoutineLoader<?, OUTPUT> loader,
            @Nonnull final Logger logger) {

        if (loader == null) {

            throw new NullPointerException("the loader cannot be null");
        }

        mLoader = loader;
        mLogger = logger.subContextLogger(this);
    }

    @Override
    public void onComplete() {

        final boolean deliverResult;

        synchronized (mMutex) {

            mIsComplete = true;

            if (mAbortException != null) {

                mLogger.dbg("aborting channel");
                throw mAbortException;
            }

            deliverResult = mLastResults.isEmpty();
        }

        if (deliverResult) {

            mLogger.dbg("delivering final result");
            mLoader.deliverResult(createResult());
        }
    }

    @Override
    public void onError(@Nullable final Throwable error) {

        final boolean deliverResult;
        final RoutineException abortException;

        synchronized (mMutex) {

            mIsComplete = true;
            abortException = new RoutineException(error);
            mAbortException = abortException;
            deliverResult = mLastResults.isEmpty();
        }

        if (deliverResult) {

            mLogger.dbg(abortException, "delivering error");
            mLoader.deliverResult(createResult());
        }
    }

    @Override
    public void onOutput(final OUTPUT output) {

        final boolean deliverResult;

        synchronized (mMutex) {

            if (mAbortException != null) {

                mLogger.dbg("aborting channel");
                throw mAbortException;
            }

            deliverResult = mLastResults.isEmpty();
            mLastResults.add(output);
        }

        if (deliverResult) {

            mLogger.dbg("delivering result: %s", output);
            mLoader.deliverResult(createResult());
        }
    }

    /**
     * Creates and returns a new invocation result object.
     *
     * @return the result object.
     */
    InvocationResult<OUTPUT> createResult() {

        return new Result();
    }

    /**
     * Implementation of an invocation result.
     */
    private class Result implements InvocationResult<OUTPUT> {

        @Nullable
        @Override
        public Throwable getAbortException() {

            synchronized (mMutex) {

                final RoutineException exception = mAbortException;
                return (exception != null) ? exception.getCause() : null;
            }
        }

        @Override
        public boolean isError() {

            synchronized (mMutex) {

                return (mAbortException != null);
            }
        }

        @Override
        public boolean passTo(@Nonnull final Collection<IOChannelInput<OUTPUT>> newChannels,
                @Nonnull final Collection<IOChannelInput<OUTPUT>> oldChannels) {

            synchronized (mMutex) {

                final Logger logger = mLogger;
                final ArrayList<OUTPUT> lastResults = mLastResults;
                final ArrayList<OUTPUT> cachedResults = mCachedResults;

                if (mAbortException != null) {

                    logger.dbg("avoiding passing results since invocation is aborted");

                    lastResults.clear();
                    cachedResults.clear();
                    return true;

                } else {

                    try {

                        logger.dbg("passing result: %s + %s", cachedResults, lastResults);

                        for (final IOChannelInput<OUTPUT> newChannel : newChannels) {

                            newChannel.pass(cachedResults).pass(lastResults);
                        }

                        for (final IOChannelInput<OUTPUT> channel : oldChannels) {

                            channel.pass(lastResults);
                        }

                        cachedResults.addAll(lastResults);
                        lastResults.clear();

                    } catch (final RoutineInterruptedException e) {

                        throw e.interrupt();

                    } catch (final RoutineException e) {

                        mIsComplete = true;
                        mAbortException = e;
                    }
                }

                logger.dbg("invocation is complete: %s", mIsComplete);
                return mIsComplete;
            }
        }
    }
}