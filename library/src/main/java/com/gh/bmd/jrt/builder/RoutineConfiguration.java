/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh.bmd.jrt.builder;

import com.gh.bmd.jrt.log.Log;
import com.gh.bmd.jrt.log.Log.LogLevel;
import com.gh.bmd.jrt.runner.Runner;
import com.gh.bmd.jrt.time.TimeDuration;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.gh.bmd.jrt.time.TimeDuration.fromUnit;

/**
 * Class storing the routine configuration.
 * <p/>
 * Each instance is immutable, thus, in order to modify a configuration parameter, a new builder
 * must be created starting from the specific configuration instance.
 * <p/>
 * Created by davide on 11/15/14.
 */
public class RoutineConfiguration {

    /**
     * Constant indicating the default value of an integer attribute.
     */
    public static final int DEFAULT = Integer.MIN_VALUE;

    /**
     * Empty configuration constant.<br/>The configuration has all the values set to their default.
     */
    public static final RoutineConfiguration EMPTY_CONFIGURATION = builder().buildConfiguration();

    private final TimeDuration mAvailTimeout;

    private final int mCoreInvocations;

    private final int mInputMaxSize;

    private final OrderType mInputOrder;

    private final TimeDuration mInputTimeout;

    private final Log mLog;

    private final LogLevel mLogLevel;

    private final int mMaxInvocations;

    private final int mOutputMaxSize;

    private final OrderType mOutputOrder;

    private final TimeDuration mOutputTimeout;

    private final TimeDuration mReadTimeout;

    private final Runner mRunner;

    private final RunnerType mRunnerType;

    private final TimeoutAction mTimeoutAction;

    /**
     * Constructor.
     *
     * @param runner          the runner used for asynchronous invocations.
     * @param runnerType      the type of runner used for synchronous invocations.
     * @param maxInvocations  the maximum number of parallel running invocations. Must be positive.
     * @param coreInvocations the maximum number of retained invocation instances. Must be 0 or a
     *                        positive number.
     * @param availTimeout    the maximum timeout while waiting for an invocation instance to be
     *                        available.
     * @param readTimeout     the action to be taken if the timeout elapses before a readable result
     *                        is available.
     * @param actionType      the timeout for an invocation instance to produce a result.
     * @param inputOrder      the order in which input data are collected from the input channel.
     * @param inputMaxSize    the maximum number of buffered input data. Must be positive.
     * @param inputTimeout    the maximum timeout while waiting for an input to be passed to the
     *                        input channel.
     * @param outputOrder     the order in which output data are collected from the result channel.
     * @param outputMaxSize   the maximum number of buffered output data. Must be positive.
     * @param outputTimeout   the maximum timeout while waiting for an output to be passed to the
     *                        result channel.
     * @param log             the log instance.
     * @param logLevel        the log level.
     */
    private RoutineConfiguration(@Nullable final Runner runner,
            @Nullable final RunnerType runnerType, final int maxInvocations,
            final int coreInvocations, @Nullable final TimeDuration availTimeout,
            @Nullable final TimeDuration readTimeout, @Nullable final TimeoutAction actionType,
            @Nullable final OrderType inputOrder, final int inputMaxSize,
            @Nullable final TimeDuration inputTimeout, @Nullable final OrderType outputOrder,
            final int outputMaxSize, @Nullable final TimeDuration outputTimeout,
            @Nullable final Log log, @Nullable final LogLevel logLevel) {

        mRunner = runner;
        mRunnerType = runnerType;
        mMaxInvocations = maxInvocations;
        mCoreInvocations = coreInvocations;
        mAvailTimeout = availTimeout;
        mReadTimeout = readTimeout;
        mTimeoutAction = actionType;
        mInputOrder = inputOrder;
        mInputMaxSize = inputMaxSize;
        mInputTimeout = inputTimeout;
        mOutputOrder = outputOrder;
        mOutputMaxSize = outputMaxSize;
        mOutputTimeout = outputTimeout;
        mLog = log;
        mLogLevel = logLevel;
    }

    /**
     * Returns a routine configuration builder.
     *
     * @return the builder.
     */
    @Nonnull
    public static Builder builder() {

        return new Builder();
    }

    /**
     * Returns a routine configuration builder initialized with the specified configuration.
     *
     * @param initialConfiguration the initial configuration.
     * @return the builder.
     */
    @Nonnull
    public static Builder builderFrom(@Nonnull final RoutineConfiguration initialConfiguration) {

        return new Builder(initialConfiguration);
    }

    /**
     * Returns the specified configuration or the empty one if the former is null.
     *
     * @param configuration the routine configuration.
     * @return the configuration.
     */
    @Nonnull
    public static RoutineConfiguration notNull(@Nullable final RoutineConfiguration configuration) {

        return (configuration != null) ? configuration : EMPTY_CONFIGURATION;
    }

    /**
     * Short for <b><code>builder().onReadTimeout(action).buildConfiguration()</code></b>.
     *
     * @param action the action type.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration onReadTimeout(@Nullable final TimeoutAction action) {

        return builder().onReadTimeout(action).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withAvailableTimeout(timeout, timeUnit).buildConfiguration()
     * </code></b>.
     *
     * @param timeout  the timeout.
     * @param timeUnit the timeout time unit.
     * @return the routine configuration.
     * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
     * @throws java.lang.NullPointerException     if the specified time unit is null.
     */
    @Nonnull
    public static RoutineConfiguration withAvailableTimeout(final long timeout,
            @Nonnull final TimeUnit timeUnit) {

        return withAvailableTimeout(fromUnit(timeout, timeUnit));
    }

    /**
     * Short for <b><code>builder().withAvailableTimeout(timeout).buildConfiguration()</code></b>.
     *
     * @param timeout the timeout.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withAvailableTimeout(@Nullable final TimeDuration timeout) {

        return builder().withAvailableTimeout(timeout).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withCoreInvocations(coreInvocations).buildConfiguration()
     * </code></b>.
     *
     * @param coreInvocations the max number of instances.
     * @return the routine configuration.
     * @throws java.lang.IllegalArgumentException if the number is negative.
     */
    @Nonnull
    public static RoutineConfiguration withCoreInvocations(final int coreInvocations) {

        return builder().withCoreInvocations(coreInvocations).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withInputOrder(order).buildConfiguration()</code></b>.
     *
     * @param order the order type.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withInputOrder(@Nullable final OrderType order) {

        return builder().withInputOrder(order).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withInputSize(inputMaxSize).buildConfiguration()</code></b>.
     *
     * @param inputMaxSize the maximum size.
     * @return the routine configuration.
     * @throws java.lang.IllegalArgumentException if the number is less than 1.
     */
    @Nonnull
    public static RoutineConfiguration withInputSize(final int inputMaxSize) {

        return builder().withInputSize(inputMaxSize).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withInputTimeout(timeout, timeUnit).buildConfiguration()
     * </code></b>.
     *
     * @param timeout  the timeout.
     * @param timeUnit the timeout time unit.
     * @return this builder.
     * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
     * @throws java.lang.NullPointerException     if the specified time unit is null.
     */
    @Nonnull
    public static RoutineConfiguration withInputTimeout(final long timeout,
            @Nonnull final TimeUnit timeUnit) {

        return withInputTimeout(fromUnit(timeout, timeUnit));
    }

    /**
     * Short for <b><code>builder().withInputTimeout(timeout).buildConfiguration()</code></b>.
     *
     * @param timeout the timeout.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withInputTimeout(@Nullable final TimeDuration timeout) {

        return builder().withInputTimeout(timeout).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withLog(log).buildConfiguration()</code></b>.
     *
     * @param log the log instance.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withLog(@Nullable final Log log) {

        return builder().withLog(log).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withLogLevel(level).buildConfiguration()</code></b>.
     *
     * @param level the log level.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withLogLevel(@Nullable final LogLevel level) {

        return builder().withLogLevel(level).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withMaxInvocations(maxInvocations).buildConfiguration()
     * </code></b>.
     *
     * @param maxInvocations the max number of instances.
     * @return the routine configuration.
     * @throws java.lang.IllegalArgumentException if the number is less than 1.
     */
    @Nonnull
    public static RoutineConfiguration withMaxInvocations(final int maxInvocations) {

        return builder().withMaxInvocations(maxInvocations).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withOutputOrder(order).buildConfiguration()</code></b>.
     *
     * @param order the order type.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withOutputOrder(@Nullable final OrderType order) {

        return builder().withOutputOrder(order).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withOutputSize(outputMaxSize).buildConfiguration()</code></b>.
     *
     * @param outputMaxSize the maximum size.
     * @return the routine configuration.
     * @throws java.lang.IllegalArgumentException if the number is less than 1.
     */
    @Nonnull
    public static RoutineConfiguration withOutputSize(final int outputMaxSize) {

        return builder().withOutputSize(outputMaxSize).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withOutputTimeout(timeout, timeUnit).buildConfiguration()
     * </code></b>.
     *
     * @param timeout  the timeout.
     * @param timeUnit the timeout time unit.
     * @return the routine configuration.
     * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
     * @throws java.lang.NullPointerException     if the specified time unit is null.
     */
    @Nonnull
    public static RoutineConfiguration withOutputTimeout(final long timeout,
            @Nonnull final TimeUnit timeUnit) {

        return withOutputTimeout(fromUnit(timeout, timeUnit));
    }

    /**
     * Short for <b><code>builder().withOutputTimeout(timeout).buildConfiguration()</code></b>.
     *
     * @param timeout the timeout.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withOutputTimeout(@Nullable final TimeDuration timeout) {

        return builder().withOutputTimeout(timeout).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withReadTimeout(timeout, timeUnit).buildConfiguration()
     * </code></b>.
     *
     * @param timeout  the timeout.
     * @param timeUnit the timeout time unit.
     * @return the routine configuration.
     * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
     * @throws java.lang.NullPointerException     if the specified time unit is null.
     */
    @Nonnull
    public static RoutineConfiguration withReadTimeout(final long timeout,
            @Nonnull final TimeUnit timeUnit) {

        return withReadTimeout(fromUnit(timeout, timeUnit));
    }

    /**
     * Short for <b><code>builder().withReadTimeout(timeout).buildConfiguration()</code></b>.
     *
     * @param timeout the timeout.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withReadTimeout(@Nullable final TimeDuration timeout) {

        return builder().withReadTimeout(timeout).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withRunner(runner).buildConfiguration()</code></b>.
     *
     * @param runner the runner instance.
     * @return the routine configuration.
     */
    @Nonnull
    public static RoutineConfiguration withRunner(@Nullable final Runner runner) {

        return builder().withRunner(runner).buildConfiguration();
    }

    /**
     * Short for <b><code>builder().withSyncRunner(type).buildConfiguration()</code></b>.
     *
     * @param type the runner type.
     * @return this builder.
     */
    @Nonnull
    public static RoutineConfiguration withSyncRunner(@Nullable final RunnerType type) {

        return builder().withSyncRunner(type).buildConfiguration();
    }

    /**
     * Returns a routine configuration builder initialized with this configuration.
     *
     * @return the builder.
     */
    @Nonnull
    public Builder builderFrom() {

        return new Builder(this);
    }

    /**
     * Returns the maximum timeout while waiting for an invocation instance to be available (null
     * by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the timeout.
     */
    public TimeDuration getAvailTimeoutOr(@Nullable final TimeDuration valueIfNotSet) {

        final TimeDuration availTimeout = mAvailTimeout;
        return (availTimeout != null) ? availTimeout : valueIfNotSet;
    }

    /**
     * Returns the maximum number of retained invocation instances (DEFAULT by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the maximum number.
     */
    public int getCoreInvocationsOr(final int valueIfNotSet) {

        final int coreInvocations = mCoreInvocations;
        return (coreInvocations != DEFAULT) ? coreInvocations : valueIfNotSet;
    }

    /**
     * Returns the input data order (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the order type.
     */
    public OrderType getInputOrderOr(@Nullable final OrderType valueIfNotSet) {

        final OrderType orderedInput = mInputOrder;
        return (orderedInput != null) ? orderedInput : valueIfNotSet;
    }

    /**
     * Returns the maximum number of buffered input data (DEFAULT by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the maximum size.
     */
    public int getInputSizeOr(final int valueIfNotSet) {

        final int inputMaxSize = mInputMaxSize;
        return (inputMaxSize != DEFAULT) ? inputMaxSize : valueIfNotSet;
    }

    /**
     * Returns the maximum timeout while waiting for an input to be passed to the input channel
     * (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the timeout.
     */
    public TimeDuration getInputTimeoutOr(@Nullable final TimeDuration valueIfNotSet) {

        final TimeDuration inputTimeout = mInputTimeout;
        return (inputTimeout != null) ? inputTimeout : valueIfNotSet;
    }

    /**
     * Returns the log level (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the log level.
     */
    public LogLevel getLogLevelOr(@Nullable final LogLevel valueIfNotSet) {

        final LogLevel logLevel = mLogLevel;
        return (logLevel != null) ? logLevel : valueIfNotSet;
    }

    /**
     * Returns the log instance (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the log instance.
     */
    public Log getLogOr(@Nullable final Log valueIfNotSet) {

        final Log log = mLog;
        return (log != null) ? log : valueIfNotSet;
    }

    /**
     * Returns the maximum number of parallel running invocations (DEFAULT by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the maximum number.
     */
    public int getMaxInvocationsOr(final int valueIfNotSet) {

        final int maxInvocations = mMaxInvocations;
        return (maxInvocations != DEFAULT) ? maxInvocations : valueIfNotSet;
    }

    /**
     * Returns the output data order (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the order type.
     */
    public OrderType getOutputOrderOr(@Nullable final OrderType valueIfNotSet) {

        final OrderType orderedOutput = mOutputOrder;
        return (orderedOutput != null) ? orderedOutput : valueIfNotSet;
    }

    /**
     * Returns the maximum number of buffered output data (DEFAULT by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the maximum size.
     */
    public int getOutputSizeOr(final int valueIfNotSet) {

        final int outputMaxSize = mOutputMaxSize;
        return (outputMaxSize != DEFAULT) ? outputMaxSize : valueIfNotSet;
    }

    /**
     * Returns the maximum timeout while waiting for an output to be passed to the result channel
     * (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the timeout.
     */
    public TimeDuration getOutputTimeoutOr(@Nullable final TimeDuration valueIfNotSet) {

        final TimeDuration outputTimeout = mOutputTimeout;
        return (outputTimeout != null) ? outputTimeout : valueIfNotSet;
    }

    /**
     * Returns the action to be taken if the timeout elapses before a readable result is available
     * (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the action type.
     */
    public TimeoutAction getReadTimeoutActionOr(@Nullable final TimeoutAction valueIfNotSet) {

        final TimeoutAction timeoutAction = mTimeoutAction;
        return (timeoutAction != null) ? timeoutAction : valueIfNotSet;
    }

    /**
     * Returns the timeout for an invocation instance to produce a readable result (null by
     * default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the timeout.
     */
    public TimeDuration getReadTimeoutOr(@Nullable final TimeDuration valueIfNotSet) {

        final TimeDuration readTimeout = mReadTimeout;
        return (readTimeout != null) ? readTimeout : valueIfNotSet;
    }

    /**
     * Returns the runner used for asynchronous invocations (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the runner instance.
     */
    public Runner getRunnerOr(@Nullable final Runner valueIfNotSet) {

        final Runner runner = mRunner;
        return (runner != null) ? runner : valueIfNotSet;
    }

    /**
     * Returns the type of the runner used for synchronous invocations (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the runner type.
     */
    public RunnerType getSyncRunnerOr(@Nullable final RunnerType valueIfNotSet) {

        final RunnerType runnerType = mRunnerType;
        return (runnerType != null) ? runnerType : valueIfNotSet;
    }

    @Override
    public int hashCode() {

        // auto-generated code
        int result = mAvailTimeout != null ? mAvailTimeout.hashCode() : 0;
        result = 31 * result + mCoreInvocations;
        result = 31 * result + mInputMaxSize;
        result = 31 * result + (mInputOrder != null ? mInputOrder.hashCode() : 0);
        result = 31 * result + (mInputTimeout != null ? mInputTimeout.hashCode() : 0);
        result = 31 * result + (mLog != null ? mLog.hashCode() : 0);
        result = 31 * result + (mLogLevel != null ? mLogLevel.hashCode() : 0);
        result = 31 * result + mMaxInvocations;
        result = 31 * result + mOutputMaxSize;
        result = 31 * result + (mOutputOrder != null ? mOutputOrder.hashCode() : 0);
        result = 31 * result + (mOutputTimeout != null ? mOutputTimeout.hashCode() : 0);
        result = 31 * result + (mReadTimeout != null ? mReadTimeout.hashCode() : 0);
        result = 31 * result + (mRunner != null ? mRunner.hashCode() : 0);
        result = 31 * result + (mRunnerType != null ? mRunnerType.hashCode() : 0);
        result = 31 * result + (mTimeoutAction != null ? mTimeoutAction.hashCode() : 0);
        return result;
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(final Object o) {

        // auto-generated code
        if (this == o) {

            return true;
        }

        if (!(o instanceof RoutineConfiguration)) {

            return false;
        }

        final RoutineConfiguration that = (RoutineConfiguration) o;

        if (mCoreInvocations != that.mCoreInvocations) {

            return false;
        }

        if (mInputMaxSize != that.mInputMaxSize) {

            return false;
        }

        if (mMaxInvocations != that.mMaxInvocations) {

            return false;
        }

        if (mOutputMaxSize != that.mOutputMaxSize) {

            return false;
        }

        if (mAvailTimeout != null ? !mAvailTimeout.equals(that.mAvailTimeout)
                : that.mAvailTimeout != null) {

            return false;
        }

        if (mInputOrder != that.mInputOrder) {

            return false;
        }

        if (mInputTimeout != null ? !mInputTimeout.equals(that.mInputTimeout)
                : that.mInputTimeout != null) {

            return false;
        }

        if (mLog != null ? !mLog.equals(that.mLog) : that.mLog != null) {

            return false;
        }

        if (mLogLevel != that.mLogLevel) {

            return false;
        }

        if (mOutputOrder != that.mOutputOrder) {

            return false;
        }

        if (mOutputTimeout != null ? !mOutputTimeout.equals(that.mOutputTimeout)
                : that.mOutputTimeout != null) {

            return false;
        }

        if (mReadTimeout != null ? !mReadTimeout.equals(that.mReadTimeout)
                : that.mReadTimeout != null) {

            return false;
        }

        if (mRunner != null ? !mRunner.equals(that.mRunner) : that.mRunner != null) {

            return false;
        }

        if (mRunnerType != that.mRunnerType) {

            return false;
        }

        return mTimeoutAction == that.mTimeoutAction;
    }

    @Override
    public String toString() {

        return "RoutineConfiguration{" +
                "mAvailTimeout=" + mAvailTimeout +
                ", mCoreInvocations=" + mCoreInvocations +
                ", mInputMaxSize=" + mInputMaxSize +
                ", mInputOrder=" + mInputOrder +
                ", mInputTimeout=" + mInputTimeout +
                ", mLog=" + mLog +
                ", mLogLevel=" + mLogLevel +
                ", mMaxInvocations=" + mMaxInvocations +
                ", mOutputMaxSize=" + mOutputMaxSize +
                ", mOutputOrder=" + mOutputOrder +
                ", mOutputTimeout=" + mOutputTimeout +
                ", mReadTimeout=" + mReadTimeout +
                ", mRunner=" + mRunner +
                ", mRunnerType=" + mRunnerType +
                ", mTimeoutAction=" + mTimeoutAction +
                '}';
    }

    /**
     * Enumeration defining how data are ordered inside a channel.
     */
    public enum OrderType {

        /**
         * Passing order.<br/>
         * Data are returned in the same order as they are passed to the channel, independently from
         * the specific delay.
         */
        PASSING,
        /**
         * Delivery order.<br/>
         * Data are returned in the same order as they are delivered, taking also into consideration
         * the specific delay. Note that the delivery time might be different based on the specific
         * runner implementation, so there is no guarantee about the data order when, for example,
         * two objects are passed one immediately after the other with the same delay.
         */
        DELIVERY,
    }

    /**
     * Synchronous runner type enumeration.
     */
    public enum RunnerType {

        /**
         * Sequential runner.<br/>
         * The sequential one simply runs the executions as soon as they are invoked.<br/>
         * The executions will run inside the calling thread.
         */
        SEQUENTIAL,
        /**
         * Queued runner.<br/>
         * The queued runner maintains an internal buffer of executions that are consumed only when
         * the last one completes, thus avoiding overflowing the call stack because of nested calls
         * to other routines.<br/>
         * The executions will run inside the calling thread.
         */
        QUEUED
    }

    /**
     * Enumeration indicating the action to take on output channel timeout.
     */
    public enum TimeoutAction {

        /**
         * Deadlock.<br/>
         * If no result is available after the specified timeout, the called method will throw a
         * {@link com.gh.bmd.jrt.channel.ReadDeadlockException}.
         */
        DEADLOCK,
        /**
         * Break execution.<br/>
         * If no result is available after the specified timeout, the called method will stop its
         * execution and exit immediately.
         */
        EXIT,
        /**
         * Abort invocation.<br/>
         * If no result is available after the specified timeout, the invocation will be aborted and
         * the method will immediately exit.
         */
        ABORT
    }

    /**
     * Builder of routine configurations.
     */
    public static class Builder {

        private TimeDuration mAvailTimeout;

        private int mCoreInvocations;

        private int mInputMaxSize;

        private OrderType mInputOrder;

        private TimeDuration mInputTimeout;

        private Log mLog;

        private LogLevel mLogLevel;

        private int mMaxInvocations;

        private int mOutputMaxSize;

        private OrderType mOutputOrder;

        private TimeDuration mOutputTimeout;

        private TimeDuration mReadTimeout;

        private Runner mRunner;

        private RunnerType mRunnerType;

        private TimeoutAction mTimeoutAction;

        /**
         * Constructor.
         */
        private Builder() {

            mMaxInvocations = DEFAULT;
            mCoreInvocations = DEFAULT;
            mInputMaxSize = DEFAULT;
            mOutputMaxSize = DEFAULT;
        }

        /**
         * Constructor.
         *
         * @param initialConfiguration the initial configuration.
         * @throws java.lang.NullPointerException if the specified configuration instance is null.
         */
        private Builder(@Nonnull final RoutineConfiguration initialConfiguration) {

            mRunner = initialConfiguration.getRunnerOr(null);
            mRunnerType = initialConfiguration.getSyncRunnerOr(null);
            mMaxInvocations = initialConfiguration.getMaxInvocationsOr(DEFAULT);
            mCoreInvocations = initialConfiguration.getCoreInvocationsOr(DEFAULT);
            mAvailTimeout = initialConfiguration.getAvailTimeoutOr(null);
            mReadTimeout = initialConfiguration.getReadTimeoutOr(null);
            mTimeoutAction = initialConfiguration.getReadTimeoutActionOr(null);
            mInputOrder = initialConfiguration.getInputOrderOr(null);
            mInputMaxSize = initialConfiguration.getInputSizeOr(DEFAULT);
            mInputTimeout = initialConfiguration.getInputTimeoutOr(null);
            mOutputOrder = initialConfiguration.getOutputOrderOr(null);
            mOutputMaxSize = initialConfiguration.getOutputSizeOr(DEFAULT);
            mOutputTimeout = initialConfiguration.getOutputTimeoutOr(null);
            mLog = initialConfiguration.getLogOr(null);
            mLogLevel = initialConfiguration.getLogLevelOr(null);
        }

        /**
         * Builds and return the configuration instance.
         *
         * @return the routine configuration instance.
         */
        @Nonnull
        public RoutineConfiguration buildConfiguration() {

            return new RoutineConfiguration(mRunner, mRunnerType, mMaxInvocations, mCoreInvocations,
                                            mAvailTimeout, mReadTimeout, mTimeoutAction,
                                            mInputOrder, mInputMaxSize, mInputTimeout, mOutputOrder,
                                            mOutputMaxSize, mOutputTimeout, mLog, mLogLevel);
        }

        /**
         * Sets the action to be taken if the timeout elapses before a result can be read from the
         * output channel.
         *
         * @param action the action type.
         * @return this builder.
         */
        @Nonnull
        public Builder onReadTimeout(@Nullable final TimeoutAction action) {

            mTimeoutAction = action;
            return this;
        }

        /**
         * Sets the timeout for an invocation instance to become available.
         *
         * @param timeout  the timeout.
         * @param timeUnit the timeout time unit.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
         * @throws java.lang.NullPointerException     if the specified time unit is null.
         */
        @Nonnull
        public Builder withAvailableTimeout(final long timeout, @Nonnull final TimeUnit timeUnit) {

            return withAvailableTimeout(fromUnit(timeout, timeUnit));
        }

        /**
         * Sets the timeout for an invocation instance to become available. A null value means that
         * it is up to the framework to choose a default duration.
         *
         * @param timeout the timeout.
         * @return this builder.
         */
        @Nonnull
        public Builder withAvailableTimeout(@Nullable final TimeDuration timeout) {

            mAvailTimeout = timeout;
            return this;
        }

        /**
         * Applies the specified configuration to this builder.
         *
         * @param configuration the routine configuration.
         * @return this builder.
         * @throws java.lang.NullPointerException if the specified configuration is null.
         */
        @Nonnull
        public Builder withConfiguration(@Nonnull final RoutineConfiguration configuration) {

            applyInvocationConfiguration(configuration);
            applyChannelConfiguration(configuration);
            applyLogConfiguration(configuration);

            return this;
        }

        /**
         * Sets the number of invocation instances which represents the core pool of reusable
         * invocations. A {@link RoutineConfiguration#DEFAULT} value means that it is up to the
         * framework to choose a default number.
         *
         * @param coreInvocations the max number of instances.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the number is negative.
         */
        @Nonnull
        public Builder withCoreInvocations(final int coreInvocations) {

            if ((coreInvocations != DEFAULT) && (coreInvocations < 0)) {

                throw new IllegalArgumentException(
                        "the maximum number of retained instances cannot be negative");
            }

            mCoreInvocations = coreInvocations;
            return this;
        }

        /**
         * Sets the order in which input data are collected from the input channel. A null value
         * means that it is up to the framework to choose a default order type.
         *
         * @param order the order type.
         * @return this builder.
         */
        @Nonnull
        public Builder withInputOrder(@Nullable final OrderType order) {

            mInputOrder = order;
            return this;
        }

        /**
         * Sets the maximum number of data that the input channel can retain before they are
         * consumed. A {@link RoutineConfiguration#DEFAULT} value means that it is up to the
         * framework to choose a default size.
         *
         * @param inputMaxSize the maximum size.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the number is less than 1.
         */
        @Nonnull
        public Builder withInputSize(final int inputMaxSize) {

            if ((inputMaxSize != DEFAULT) && (inputMaxSize <= 0)) {

                throw new IllegalArgumentException("the input buffer size cannot be 0 or negative");
            }

            mInputMaxSize = inputMaxSize;
            return this;
        }

        /**
         * Sets the timeout for an input channel to have room for additional data.
         *
         * @param timeout  the timeout.
         * @param timeUnit the timeout time unit.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
         * @throws java.lang.NullPointerException     if the specified time unit is null.
         */
        @Nonnull
        public Builder withInputTimeout(final long timeout, @Nonnull final TimeUnit timeUnit) {

            return withInputTimeout(fromUnit(timeout, timeUnit));
        }

        /**
         * Sets the timeout for an input channel to have room for additional data. A null value
         * means that it is up to the framework to choose a default.
         *
         * @param timeout the timeout.
         * @return this builder.
         */
        @Nonnull
        public Builder withInputTimeout(@Nullable final TimeDuration timeout) {

            mInputTimeout = timeout;
            return this;
        }

        /**
         * Sets the log instance. A null value means that it is up to the framework to choose a
         * default implementation.
         *
         * @param log the log instance.
         * @return this builder.
         */
        @Nonnull
        public Builder withLog(@Nullable final Log log) {

            mLog = log;
            return this;
        }

        /**
         * Sets the log level. A null value means that it is up to the framework to choose a default
         * level.
         *
         * @param level the log level.
         * @return this builder.
         */
        @Nonnull
        public Builder withLogLevel(@Nullable final LogLevel level) {

            mLogLevel = level;
            return this;
        }

        /**
         * Sets the max number of concurrently running invocation instances. A
         * {@link RoutineConfiguration#DEFAULT} value means that it is up to the framework to choose
         * a default number.
         *
         * @param maxInvocations the max number of instances.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the number is less than 1.
         */
        @Nonnull
        public Builder withMaxInvocations(final int maxInvocations) {

            if ((maxInvocations != DEFAULT) && (maxInvocations < 1)) {

                throw new IllegalArgumentException(
                        "the maximum number of concurrently running instances cannot be less than"
                                + " 1");
            }

            mMaxInvocations = maxInvocations;
            return this;
        }

        /**
         * Sets the order in which output data are collected from the result channel. A null value
         * means that it is up to the framework to choose a default order type.
         *
         * @param order the order type.
         * @return this builder.
         */
        @Nonnull
        public Builder withOutputOrder(@Nullable final OrderType order) {

            mOutputOrder = order;
            return this;
        }

        /**
         * Sets the maximum number of data that the result channel can retain before they are
         * consumed. A {@link RoutineConfiguration#DEFAULT} value means that it is up to the
         * framework to choose a default size.
         *
         * @param outputMaxSize the maximum size.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the number is less than 1.
         */
        @Nonnull
        public Builder withOutputSize(final int outputMaxSize) {

            if ((outputMaxSize != DEFAULT) && (outputMaxSize <= 0)) {

                throw new IllegalArgumentException(
                        "the output buffer size cannot be 0 or negative");
            }

            mOutputMaxSize = outputMaxSize;
            return this;
        }

        /**
         * Sets the timeout for a result channel to have room for additional data.
         *
         * @param timeout  the timeout.
         * @param timeUnit the timeout time unit.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
         * @throws java.lang.NullPointerException     if the specified time unit is null.
         */
        @Nonnull
        public Builder withOutputTimeout(final long timeout, @Nonnull final TimeUnit timeUnit) {

            return withOutputTimeout(fromUnit(timeout, timeUnit));
        }

        /**
         * Sets the timeout for a result channel to have room for additional data. A null value
         * means that it is up to the framework to choose a default.
         *
         * @param timeout the timeout.
         * @return this builder.
         */
        @Nonnull
        public Builder withOutputTimeout(@Nullable final TimeDuration timeout) {

            mOutputTimeout = timeout;
            return this;
        }

        /**
         * Sets the timeout for an invocation instance to produce a readable result.
         *
         * @param timeout  the timeout.
         * @param timeUnit the timeout time unit.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
         * @throws java.lang.NullPointerException     if the specified time unit is null.
         */
        @Nonnull
        public Builder withReadTimeout(final long timeout, @Nonnull final TimeUnit timeUnit) {

            return withReadTimeout(fromUnit(timeout, timeUnit));
        }

        /**
         * Sets the timeout for an invocation instance to produce a readable result. A null value
         * means that it is up to the framework to choose a default duration.
         *
         * @param timeout the timeout.
         * @return this builder.
         */
        @Nonnull
        public Builder withReadTimeout(@Nullable final TimeDuration timeout) {

            mReadTimeout = timeout;
            return this;
        }

        /**
         * Sets the asynchronous runner instance. A null value means that it is up to the framework
         * to choose a default instance.
         *
         * @param runner the runner instance.
         * @return this builder.
         */
        @Nonnull
        public Builder withRunner(@Nullable final Runner runner) {

            mRunner = runner;
            return this;
        }

        /**
         * Sets the type of the synchronous runner to be used by the routine. A null value means
         * that it is up to the framework to choose a default order type.
         *
         * @param type the runner type.
         * @return this builder.
         */
        @Nonnull
        public Builder withSyncRunner(@Nullable final RunnerType type) {

            mRunnerType = type;
            return this;
        }

        private void applyChannelConfiguration(@Nonnull final RoutineConfiguration configuration) {

            final OrderType inputOrder = configuration.getInputOrderOr(null);

            if (inputOrder != null) {

                withInputOrder(inputOrder);
            }

            final int inputSize = configuration.getInputSizeOr(DEFAULT);

            if (inputSize != DEFAULT) {

                withInputSize(inputSize);
            }

            final TimeDuration inputTimeout = configuration.getInputTimeoutOr(null);

            if (inputTimeout != null) {

                withInputTimeout(inputTimeout);
            }

            final OrderType outputOrder = configuration.getOutputOrderOr(null);

            if (outputOrder != null) {

                withOutputOrder(outputOrder);
            }

            final int outputSize = configuration.getOutputSizeOr(DEFAULT);

            if (outputSize != DEFAULT) {

                withOutputSize(outputSize);
            }

            final TimeDuration outputTimeout = configuration.getOutputTimeoutOr(null);

            if (outputTimeout != null) {

                withOutputTimeout(outputTimeout);
            }
        }

        private void applyInvocationConfiguration(
                @Nonnull final RoutineConfiguration configuration) {

            final Runner runner = configuration.getRunnerOr(null);

            if (runner != null) {

                withRunner(runner);
            }

            final RunnerType syncRunner = configuration.getSyncRunnerOr(null);

            if (syncRunner != null) {

                withSyncRunner(syncRunner);
            }

            final int maxInvocations = configuration.getMaxInvocationsOr(DEFAULT);

            if (maxInvocations != DEFAULT) {

                withMaxInvocations(maxInvocations);
            }

            final int coreInvocations = configuration.getCoreInvocationsOr(DEFAULT);

            if (coreInvocations != DEFAULT) {

                withCoreInvocations(coreInvocations);
            }

            final TimeDuration availTimeout = configuration.getAvailTimeoutOr(null);

            if (availTimeout != null) {

                withAvailableTimeout(availTimeout);
            }

            final TimeDuration readTimeout = configuration.getReadTimeoutOr(null);

            if (readTimeout != null) {

                withReadTimeout(readTimeout);
            }

            final TimeoutAction timeoutAction = configuration.getReadTimeoutActionOr(null);

            if (timeoutAction != null) {

                onReadTimeout(timeoutAction);
            }
        }

        private void applyLogConfiguration(@Nonnull final RoutineConfiguration configuration) {

            final Log log = configuration.getLogOr(null);

            if (log != null) {

                withLog(log);
            }

            final LogLevel logLevel = configuration.getLogLevelOr(null);

            if (logLevel != null) {

                withLogLevel(logLevel);
            }
        }
    }
}
