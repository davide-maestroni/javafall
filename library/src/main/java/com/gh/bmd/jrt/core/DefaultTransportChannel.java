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
package com.gh.bmd.jrt.core;

import com.gh.bmd.jrt.builder.RoutineConfiguration;
import com.gh.bmd.jrt.builder.RoutineConfiguration.OrderType;
import com.gh.bmd.jrt.channel.OutputChannel;
import com.gh.bmd.jrt.channel.OutputConsumer;
import com.gh.bmd.jrt.channel.TransportChannel;
import com.gh.bmd.jrt.core.DefaultResultChannel.AbortHandler;
import com.gh.bmd.jrt.log.Logger;
import com.gh.bmd.jrt.runner.Runner;
import com.gh.bmd.jrt.runner.Runners;
import com.gh.bmd.jrt.time.TimeDuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default implementation of a transport channel.
 * <p/>
 * Created by davide-maestroni on 10/24/14.
 *
 * @param <DATA> the data type.
 */
class DefaultTransportChannel<DATA> implements TransportChannel<DATA> {

    private final DefaultTransportInput<DATA> mInputChannel;

    private final DefaultTransportOutput<DATA> mOutputChannel;

    /**
     * Constructor.
     *
     * @param configuration the routine configuration.
     */
    DefaultTransportChannel(@Nonnull final RoutineConfiguration configuration) {

        final Logger logger = configuration.newLogger(this);
        final ChannelAbortHandler abortHandler = new ChannelAbortHandler();
        final DefaultResultChannel<DATA> inputChannel =
                new DefaultResultChannel<DATA>(configuration, abortHandler,
                                               configuration.getAsyncRunnerOr(
                                                       Runners.sharedRunner()), logger);
        abortHandler.setChannel(inputChannel);
        mInputChannel = new DefaultTransportInput<DATA>(inputChannel);
        mOutputChannel = new DefaultTransportOutput<DATA>(inputChannel.getOutput());
        logger.dbg("building transport channel with configuration: %s", configuration);
        warn(logger, configuration);
    }

    /**
     * Logs any warning related to ignored options in the specified configuration.
     *
     * @param logger        the logger instance.
     * @param configuration the routine configuration.
     */
    private static void warn(@Nonnull final Logger logger,
            @Nonnull final RoutineConfiguration configuration) {

        final Object[] args = configuration.getFactoryArgsOr(null);

        if (args != null) {

            logger.wrn("the specified factory arguments will be ignored: %s",
                       Arrays.toString(args));
        }

        final Runner syncRunner = configuration.getSyncRunnerOr(null);

        if (syncRunner != null) {

            logger.wrn("the specified synchronous runner will be ignored: %s", syncRunner);
        }

        final int maxInvocations = configuration.getMaxInvocationsOr(RoutineConfiguration.DEFAULT);

        if (maxInvocations != RoutineConfiguration.DEFAULT) {

            logger.wrn("the specified maximum running invocations will be ignored: %d",
                       maxInvocations);
        }

        final int coreInvocations =
                configuration.getCoreInvocationsOr(RoutineConfiguration.DEFAULT);

        if (coreInvocations != RoutineConfiguration.DEFAULT) {

            logger.wrn("the specified core invocations will be ignored: %d", coreInvocations);
        }

        final TimeDuration availableTimeout = configuration.getAvailInvocationTimeoutOr(null);

        if (availableTimeout != null) {

            logger.wrn("the specified available invocation timeout will be ignored: %s",
                       availableTimeout);
        }

        final OrderType inputOrderType = configuration.getInputOrderTypeOr(null);

        if (inputOrderType != null) {

            logger.wrn("the specified input order type will be ignored: %s", inputOrderType);
        }

        final int inputSize = configuration.getInputMaxSizeOr(RoutineConfiguration.DEFAULT);

        if (inputSize != RoutineConfiguration.DEFAULT) {

            logger.wrn("the specified maximum input size will be ignored: %d", inputSize);
        }

        final TimeDuration inputTimeout = configuration.getInputTimeoutOr(null);

        if (inputTimeout != null) {

            logger.wrn("the specified input timeout will be ignored: %s", inputTimeout);
        }
    }

    @Nonnull
    public TransportInput<DATA> input() {

        return mInputChannel;
    }

    @Nonnull
    public TransportOutput<DATA> output() {

        return mOutputChannel;
    }

    /**
     * Abort handler used to close the input channel on abort.
     */
    private static class ChannelAbortHandler implements AbortHandler {

        private DefaultResultChannel<?> mChannel;

        public void onAbort(@Nullable final Throwable reason, final long delay,
                @Nonnull final TimeUnit timeUnit) {

            mChannel.close(reason);
        }

        private void setChannel(@Nonnull final DefaultResultChannel<?> channel) {

            mChannel = channel;
        }
    }

    /**
     * Default implementation of a transport channel input.
     *
     * @param <INPUT> the input data type.
     */
    private static class DefaultTransportInput<INPUT> implements TransportInput<INPUT> {

        private final DefaultResultChannel<INPUT> mChannel;

        /**
         * Constructor.
         *
         * @param wrapped the wrapped result channel.
         */
        private DefaultTransportInput(@Nonnull final DefaultResultChannel<INPUT> wrapped) {

            mChannel = wrapped;
        }

        public boolean abort() {

            return mChannel.abort();
        }

        @Nonnull
        public TransportInput<INPUT> after(@Nonnull final TimeDuration delay) {

            mChannel.after(delay);
            return this;
        }

        @Nonnull
        public TransportInput<INPUT> after(final long delay, @Nonnull final TimeUnit timeUnit) {

            mChannel.after(delay, timeUnit);
            return this;
        }

        @Nonnull
        public TransportInput<INPUT> now() {

            mChannel.now();
            return this;
        }

        @Nonnull
        public TransportInput<INPUT> pass(@Nullable final OutputChannel<? extends INPUT> channel) {

            mChannel.pass(channel);
            return this;
        }

        @Nonnull
        public TransportInput<INPUT> pass(@Nullable final Iterable<? extends INPUT> inputs) {

            mChannel.pass(inputs);
            return this;
        }

        @Nonnull
        public TransportInput<INPUT> pass(@Nullable final INPUT input) {

            mChannel.pass(input);
            return this;
        }

        @Nonnull
        public TransportInput<INPUT> pass(@Nullable final INPUT... inputs) {

            mChannel.pass(inputs);
            return this;
        }

        public void close() {

            mChannel.close();
        }

        public boolean abort(@Nullable final Throwable reason) {

            return mChannel.abort(reason);
        }

        public boolean isOpen() {

            return mChannel.isOpen();
        }
    }

    /**
     * Default implementation of a transport channel output.
     *
     * @param <OUTPUT> the output data type.
     */
    private static class DefaultTransportOutput<OUTPUT> implements TransportOutput<OUTPUT> {

        private final OutputChannel<OUTPUT> mChannel;

        /**
         * Constructor.
         *
         * @param wrapped the wrapped output channel.
         */
        private DefaultTransportOutput(@Nonnull final OutputChannel<OUTPUT> wrapped) {

            mChannel = wrapped;
        }

        @Nonnull
        public TransportOutput<OUTPUT> afterMax(@Nonnull final TimeDuration timeout) {

            mChannel.afterMax(timeout);
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> afterMax(final long timeout,
                @Nonnull final TimeUnit timeUnit) {

            mChannel.afterMax(timeout, timeUnit);
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> bind(
                @Nonnull final OutputConsumer<? super OUTPUT> consumer) {

            mChannel.bind(consumer);
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> eventually() {

            mChannel.eventually();
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> eventuallyAbort() {

            mChannel.eventuallyAbort();
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> eventuallyDeadlock() {

            mChannel.eventuallyDeadlock();
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> eventuallyExit() {

            mChannel.eventuallyExit();
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> immediately() {

            mChannel.immediately();
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> readAllInto(
                @Nonnull final Collection<? super OUTPUT> result) {

            mChannel.readAllInto(result);
            return this;
        }

        @Nonnull
        public TransportOutput<OUTPUT> unbind(
                @Nullable final OutputConsumer<? super OUTPUT> consumer) {

            mChannel.unbind(consumer);
            return this;
        }

        public boolean checkComplete() {

            return mChannel.checkComplete();
        }

        public boolean isBound() {

            return mChannel.isBound();
        }

        @Nonnull
        public List<OUTPUT> readAll() {

            return mChannel.readAll();
        }

        public OUTPUT readNext() {

            return mChannel.readNext();
        }

        public Iterator<OUTPUT> iterator() {

            return mChannel.iterator();
        }

        public boolean abort() {

            return mChannel.abort();
        }

        public boolean abort(@Nullable final Throwable reason) {

            return mChannel.abort(reason);
        }

        public boolean isOpen() {

            return mChannel.isOpen();
        }
    }
}