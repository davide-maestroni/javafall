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
import com.bmd.jrt.execution.Execution;
import com.bmd.jrt.log.Log;
import com.bmd.jrt.log.Log.LogLevel;
import com.bmd.jrt.log.Logger;
import com.bmd.jrt.routine.DefaultParameterChannel.ExecutionProvider;
import com.bmd.jrt.runner.Runner;
import com.bmd.jrt.time.TimeDuration;
import com.bmd.jrt.time.TimeDuration.Check;

import java.util.LinkedList;
import java.util.List;

/**
 * Basic abstract implementation of a routine.
 * <p/>
 * This class provides implementations for all the routine functionalities. The inheriting class
 * just need to provide execution objects when required.
 * <p/>
 * Created by davide on 9/7/14.
 *
 * @param <INPUT>  the input type.
 * @param <OUTPUT> the output type.
 */
public abstract class AbstractRoutine<INPUT, OUTPUT> implements Routine<INPUT, OUTPUT> {

    private final Runner mAsyncRunner;

    private final TimeDuration mAvailTimeout;

    private final Logger mLogger;

    private final int mMaxRetained;

    private final int mMaxRunning;

    private final Object mMutex = new Object();

    private final boolean mOrderedInput;

    private final boolean mOrderedOutput;

    private final Runner mSyncRunner;

    private LinkedList<Execution<INPUT, OUTPUT>> mExecutions =
            new LinkedList<Execution<INPUT, OUTPUT>>();

    private int mRunningCount;

    /**
     * Constructor.
     *
     * @param syncRunner    the runner used for synchronous invocation.
     * @param asyncRunner   the runner used for asynchronous invocation.
     * @param maxRunning    the maximum number of parallel running executions. Must be positive.
     * @param maxRetained   the maximum number of retained execution instances. Must be 0 or a
     *                      positive number.
     * @param availTimeout  the maximum timeout while waiting for an execution instance to be
     *                      available.
     * @param orderedInput  whether the input data are forced to be delivered in insertion order.
     * @param orderedOutput whether the output data are forced to be delivered in insertion order.
     * @param log           the log instance.
     * @param logLevel      the log level.
     * @throws NullPointerException     if one of the parameters is null.
     * @throws IllegalArgumentException if at least one of the parameter is invalid.
     */
    protected AbstractRoutine(final Runner syncRunner, final Runner asyncRunner,
            final int maxRunning, final int maxRetained, final TimeDuration availTimeout,
            final boolean orderedInput, final boolean orderedOutput, final Log log,
            final LogLevel logLevel) {

        if (syncRunner == null) {

            throw new NullPointerException("the synchronous runner instance must not be null");
        }

        if (asyncRunner == null) {

            throw new NullPointerException("the asynchronous runner instance must not be null");
        }

        if (maxRunning < 1) {

            throw new IllegalArgumentException(
                    "the maximum number of parallel running execution must be a positive number");
        }

        if (maxRetained < 0) {

            throw new IllegalArgumentException(
                    "the maximum number of retained execution instances must be 0 or positive");
        }

        if (availTimeout == null) {

            throw new NullPointerException(
                    "the timeout for available execution instances must not be null");
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
     * @param maxRunning    the maximum number of parallel running executions. Must be positive.
     * @param maxRetained   the maximum number of retained execution instances. Must be 0 or a
     *                      positive number.
     * @param availTimeout  the maximum timeout while waiting for an execution instance to be
     *                      available.
     * @param orderedInput  whether the input data are forced to be delivered in insertion order.
     * @param orderedOutput whether the output data are forced to be delivered in insertion order.
     * @param logger        the logger instance.
     */
    private AbstractRoutine(final Runner syncRunner, final Runner asyncRunner, final int maxRunning,
            final int maxRetained, final TimeDuration availTimeout, final boolean orderedInput,
            final boolean orderedOutput, final Logger logger) {

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
    public List<OUTPUT> call() {

        return run().readAll();
    }

    @Override
    public List<OUTPUT> call(final INPUT input) {

        return run(input).readAll();
    }

    @Override
    public List<OUTPUT> call(final INPUT... inputs) {

        return run(inputs).readAll();
    }

    @Override
    public List<OUTPUT> call(final Iterable<? extends INPUT> inputs) {

        return run(inputs).readAll();
    }

    @Override
    public List<OUTPUT> call(final OutputChannel<? extends INPUT> inputs) {

        return run(inputs).readAll();
    }

    @Override
    public List<OUTPUT> callAsyn() {

        return runAsyn().readAll();
    }

    @Override
    public List<OUTPUT> callAsyn(final INPUT input) {

        return runAsyn(input).readAll();
    }

    @Override
    public List<OUTPUT> callAsyn(final INPUT... inputs) {

        return runAsyn(inputs).readAll();
    }

    @Override
    public List<OUTPUT> callAsyn(final Iterable<? extends INPUT> inputs) {

        return runAsyn(inputs).readAll();
    }

    @Override
    public List<OUTPUT> callAsyn(final OutputChannel<? extends INPUT> inputs) {

        return runAsyn(inputs).readAll();
    }

    @Override
    public List<OUTPUT> callParall() {

        return runParall().readAll();
    }

    @Override
    public List<OUTPUT> callParall(final INPUT input) {

        return runParall(input).readAll();
    }

    @Override
    public List<OUTPUT> callParall(final INPUT... inputs) {

        return runParall(inputs).readAll();
    }

    @Override
    public List<OUTPUT> callParall(final Iterable<? extends INPUT> inputs) {

        return runParall(inputs).readAll();
    }

    @Override
    public List<OUTPUT> callParall(final OutputChannel<? extends INPUT> inputs) {

        return runParall(inputs).readAll();
    }

    @Override
    public ParameterChannel<INPUT, OUTPUT> invoke() {

        return invoke(false);
    }

    @Override
    public ParameterChannel<INPUT, OUTPUT> invokeAsyn() {

        return invoke(true);
    }

    @Override
    public ParameterChannel<INPUT, OUTPUT> invokeParall() {

        mLogger.dbg("invoking routine: parallel");

        final AbstractRoutine<INPUT, OUTPUT> parallelRoutine =
                new AbstractRoutine<INPUT, OUTPUT>(mSyncRunner, mAsyncRunner, mMaxRunning,
                                                   mMaxRetained, mAvailTimeout, mOrderedInput,
                                                   mOrderedOutput, mLogger) {

                    @Override
                    protected Execution<INPUT, OUTPUT> createExecution(final boolean async) {

                        return new ParallelExecution<INPUT, OUTPUT>(AbstractRoutine.this);
                    }
                };

        return parallelRoutine.invokeAsyn();
    }

    @Override
    public OutputChannel<OUTPUT> run() {

        return invoke().results();
    }

    @Override
    public OutputChannel<OUTPUT> run(final INPUT input) {

        return invoke().pass(input).results();
    }

    @Override
    public OutputChannel<OUTPUT> run(final INPUT... inputs) {

        return invoke().pass(inputs).results();
    }

    @Override
    public OutputChannel<OUTPUT> run(final Iterable<? extends INPUT> inputs) {

        return invoke().pass(inputs).results();
    }

    @Override
    public OutputChannel<OUTPUT> run(final OutputChannel<? extends INPUT> inputs) {

        return invoke().pass(inputs).results();
    }

    @Override
    public OutputChannel<OUTPUT> runAsyn() {

        return invokeAsyn().results();
    }

    @Override
    public OutputChannel<OUTPUT> runAsyn(final INPUT input) {

        return invokeAsyn().pass(input).results();
    }

    @Override
    public OutputChannel<OUTPUT> runAsyn(final INPUT... inputs) {

        return invokeAsyn().pass(inputs).results();
    }

    @Override
    public OutputChannel<OUTPUT> runAsyn(final Iterable<? extends INPUT> inputs) {

        return invokeAsyn().pass(inputs).results();
    }

    @Override
    public OutputChannel<OUTPUT> runAsyn(final OutputChannel<? extends INPUT> inputs) {

        return invokeAsyn().pass(inputs).results();
    }

    @Override
    public OutputChannel<OUTPUT> runParall() {

        return invokeParall().results();
    }

    @Override
    public OutputChannel<OUTPUT> runParall(final INPUT input) {

        return invokeParall().pass(input).results();
    }

    @Override
    public OutputChannel<OUTPUT> runParall(final INPUT... inputs) {

        return invokeParall().pass(inputs).results();
    }

    @Override
    public OutputChannel<OUTPUT> runParall(final Iterable<? extends INPUT> inputs) {

        return invokeParall().pass(inputs).results();
    }

    @Override
    public OutputChannel<OUTPUT> runParall(final OutputChannel<? extends INPUT> inputs) {

        return invokeParall().pass(inputs).results();
    }

    /**
     * Creates a new execution instance.
     *
     * @param async whether the execution is asynchronous.
     * @return the execution instance.
     */
    protected abstract Execution<INPUT, OUTPUT> createExecution(final boolean async);

    private ParameterChannel<INPUT, OUTPUT> invoke(final boolean async) {

        final Logger logger = mLogger;

        logger.dbg("invoking routine: async = %s", async);

        return new DefaultParameterChannel<INPUT, OUTPUT>(new DefaultExecutionProvider(async),
                                                          (async) ? mAsyncRunner : mSyncRunner,
                                                          mOrderedInput, mOrderedOutput, logger);
    }

    /**
     * Default implementation of an execution provider supporting recycling of execution
     * instances.
     */
    private class DefaultExecutionProvider implements ExecutionProvider<INPUT, OUTPUT> {

        private final boolean mAsync;

        private DefaultExecutionProvider(final boolean async) {

            mAsync = async;
        }

        @Override
        public Execution<INPUT, OUTPUT> create() {

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

                    mLogger.err(e, "waiting for available instance interrupted [%d]", mMaxRunning);

                    RoutineInterruptedException.interrupt(e);
                }

                if (isTimeout) {

                    mLogger.wrn("routine instance not available after timeout [%d]: %s",
                                mMaxRunning, mAvailTimeout);

                    throw new RoutineNotAvailableException();
                }

                ++mRunningCount;

                final LinkedList<Execution<INPUT, OUTPUT>> executions = mExecutions;

                if (!executions.isEmpty()) {

                    final Execution<INPUT, OUTPUT> execution = executions.removeFirst();

                    mLogger.dbg("reusing execution instance [%d/%d]: %s", executions.size() + 1,
                                mMaxRetained, execution);

                    return execution;
                }

                mLogger.dbg("creating execution instance [1/%d]", mMaxRetained);

                return createExecution(mAsync);
            }
        }

        @Override
        public void discard(final Execution<INPUT, OUTPUT> execution) {

            synchronized (mMutex) {

                mLogger.wrn("discarding execution instance after error: %s", execution);

                --mRunningCount;
                mMutex.notify();
            }
        }

        @Override
        public void recycle(final Execution<INPUT, OUTPUT> execution) {

            synchronized (mMutex) {

                final LinkedList<Execution<INPUT, OUTPUT>> executions = mExecutions;

                if (executions.size() < mMaxRetained) {

                    mLogger.dbg("recycling execution instance [%d/%d]: %s", executions.size() + 1,
                                mMaxRetained, execution);

                    executions.add(execution);

                } else {

                    mLogger.wrn("discarding execution instance [%d/%d]: %s", mMaxRetained,
                                mMaxRetained, execution);
                }

                --mRunningCount;
                mMutex.notify();
            }
        }
    }
}