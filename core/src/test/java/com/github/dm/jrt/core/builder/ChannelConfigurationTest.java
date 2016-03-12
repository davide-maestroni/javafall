/*
 * Copyright 2016 Davide Maestroni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dm.jrt.core.builder;

import com.github.dm.jrt.core.config.ChannelConfiguration;
import com.github.dm.jrt.core.config.ChannelConfiguration.Builder;
import com.github.dm.jrt.core.config.InvocationConfiguration.OrderType;
import com.github.dm.jrt.core.config.InvocationConfiguration.TimeoutActionType;
import com.github.dm.jrt.core.log.Log.Level;
import com.github.dm.jrt.core.log.Logs;
import com.github.dm.jrt.core.log.NullLog;
import com.github.dm.jrt.core.runner.Runners;
import com.github.dm.jrt.core.util.TimeDuration;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.core.config.ChannelConfiguration.builder;
import static com.github.dm.jrt.core.config.ChannelConfiguration.builderFrom;
import static com.github.dm.jrt.core.config.ChannelConfiguration.builderFromInputChannel;
import static com.github.dm.jrt.core.config.ChannelConfiguration.builderFromInvocation;
import static com.github.dm.jrt.core.config.ChannelConfiguration.builderFromOutputChannel;
import static com.github.dm.jrt.core.util.TimeDuration.millis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Channel configuration unit tests.
 * <p/>
 * Created by davide-maestroni on 07/03/2015.
 */
public class ChannelConfigurationTest {

    @Test
    public void testBuildFrom() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(builderFrom(configuration).getConfigured().hashCode()).isEqualTo(
                configuration.hashCode());
        assertThat(builderFrom(configuration).getConfigured()).isEqualTo(configuration);
        assertThat(builderFrom(null).getConfigured().hashCode()).isEqualTo(
                com.github.dm.jrt.core.config.ChannelConfiguration.DEFAULT_CONFIGURATION.hashCode());
        assertThat(builderFrom(null).getConfigured()).isEqualTo(
                com.github.dm.jrt.core.config.ChannelConfiguration.DEFAULT_CONFIGURATION);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testBuildNullPointerError() {

        try {

            new Builder<Object>(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new Builder<Object>(null, com.github.dm.jrt.core.config.ChannelConfiguration.DEFAULT_CONFIGURATION);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    @Test
    public void testBuilderFromEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .withLogLevel(Level.SILENT)
                                                            .withChannelMaxDelay(
                                                                    TimeDuration.seconds(1))
                                                            .withReadTimeout(
                                                                    TimeDuration.seconds(10))
                                                            .withReadTimeoutAction(
                                                                    TimeoutActionType.ABORT)
                                                            .getConfigured();
        assertThat(builder().with(configuration).getConfigured()).isEqualTo(configuration);
        assertThat(configuration.builderFrom().getConfigured()).isEqualTo(configuration);
        assertThat(configuration.builderFrom().with(null).getConfigured()).isEqualTo(
                com.github.dm.jrt.core.config.ChannelConfiguration.DEFAULT_CONFIGURATION);
    }

    @Test
    public void testChannelLimitEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelLimit(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(builder().withChannelLimit(10).getConfigured());
        assertThat(configuration.builderFrom().withChannelLimit(1).getConfigured()).isNotEqualTo(
                builder().withChannelLimit(1).getConfigured());
    }

    @Test
    public void testChannelLimitError() {

        try {

            builder().withChannelLimit(-1);

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testChannelMaxDelayEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(
                builder().withChannelMaxDelay(TimeDuration.ZERO).getConfigured());
        assertThat(configuration).isNotEqualTo(
                builder().withChannelMaxDelay(1, TimeUnit.MILLISECONDS).getConfigured());
        assertThat(configuration.builderFrom()
                                .withChannelMaxDelay(millis(1))
                                .getConfigured()).isNotEqualTo(
                builder().withChannelMaxDelay(1, TimeUnit.MILLISECONDS).getConfigured());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testChannelMaxDelayError() {

        try {

            builder().withChannelMaxDelay(1, null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            builder().withChannelMaxDelay(-1, TimeUnit.MILLISECONDS);

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testChannelOrderEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(
                builder().withChannelOrder(OrderType.BY_DELAY).getConfigured());
        assertThat(configuration.builderFrom()
                                .withChannelOrder(OrderType.BY_CALL)
                                .getConfigured()).isNotEqualTo(
                builder().withChannelOrder(OrderType.BY_CALL).getConfigured());
    }

    @Test
    public void testChannelSizeEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(builder().withChannelMaxSize(10).getConfigured());
        assertThat(configuration.builderFrom().withChannelMaxSize(1).getConfigured()).isNotEqualTo(
                builder().withChannelMaxSize(1).getConfigured());
    }

    @Test
    public void testChannelSizeError() {

        try {

            builder().withChannelMaxSize(0);

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testFromInputChannelConfiguration() {

        final com.github.dm.jrt.core.config.InvocationConfiguration.Builder<com.github.dm.jrt.core.config.InvocationConfiguration> builder =
                com.github.dm.jrt.core.config.InvocationConfiguration.builder();
        final com.github.dm.jrt.core.config.InvocationConfiguration invocationConfiguration =
                builder.withRunner(Runners.syncRunner())
                       .withReadTimeout(millis(100))
                       .withReadTimeoutAction(TimeoutActionType.ABORT)
                       .withLog(Logs.nullLog())
                       .withLogLevel(Level.SILENT)
                       .withInputOrder(OrderType.BY_CALL)
                       .withInputLimit(10)
                       .withInputMaxDelay(millis(33))
                       .withInputMaxSize(100)
                       .getConfigured();
        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withChannelLimit(10)
                                                            .withChannelMaxDelay(millis(33))
                                                            .withChannelMaxSize(100)
                                                            .withRunner(Runners.syncRunner())
                                                            .withReadTimeout(millis(100))
                                                            .withReadTimeoutAction(
                                                                    TimeoutActionType.ABORT)
                                                            .withLog(Logs.nullLog())
                                                            .withLogLevel(Level.SILENT)
                                                            .getConfigured();
        assertThat(builderFromInputChannel(invocationConfiguration).getConfigured()).isEqualTo(
                configuration);
    }

    @Test
    public void testFromInvocationConfiguration() {

        final com.github.dm.jrt.core.config.InvocationConfiguration.Builder<com.github.dm.jrt.core.config.InvocationConfiguration> builder =
                com.github.dm.jrt.core.config.InvocationConfiguration.builder();
        final com.github.dm.jrt.core.config.InvocationConfiguration invocationConfiguration =
                builder.withRunner(Runners.syncRunner())
                       .withReadTimeout(millis(100))
                       .withReadTimeoutAction(TimeoutActionType.ABORT)
                       .withLog(Logs.nullLog())
                       .withLogLevel(Level.SILENT)
                       .withInputOrder(OrderType.BY_CALL)
                       .withInputLimit(10)
                       .withInputMaxDelay(millis(33))
                       .withInputMaxSize(100)
                       .getConfigured();
        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withRunner(Runners.syncRunner())
                                                            .withReadTimeout(millis(100))
                                                            .withReadTimeoutAction(
                                                                    TimeoutActionType.ABORT)
                                                            .withLog(Logs.nullLog())
                                                            .withLogLevel(Level.SILENT)
                                                            .getConfigured();
        assertThat(builderFromInvocation(invocationConfiguration).getConfigured()).isEqualTo(
                configuration);
    }

    @Test
    public void testFromOutputChannelConfiguration() {

        final com.github.dm.jrt.core.config.InvocationConfiguration.Builder<com.github.dm.jrt.core.config.InvocationConfiguration> builder =
                com.github.dm.jrt.core.config.InvocationConfiguration.builder();
        final com.github.dm.jrt.core.config.InvocationConfiguration invocationConfiguration =
                builder.withRunner(Runners.syncRunner())
                       .withReadTimeout(millis(100))
                       .withReadTimeoutAction(TimeoutActionType.ABORT)
                       .withLog(Logs.nullLog())
                       .withLogLevel(Level.SILENT)
                       .withOutputOrder(OrderType.BY_CALL)
                       .withOutputLimit(10)
                       .withOutputMaxDelay(millis(33))
                       .withOutputMaxSize(100)
                       .getConfigured();
        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withChannelLimit(10)
                                                            .withChannelMaxDelay(millis(33))
                                                            .withChannelMaxSize(100)
                                                            .withRunner(Runners.syncRunner())
                                                            .withReadTimeout(millis(100))
                                                            .withReadTimeoutAction(
                                                                    TimeoutActionType.ABORT)
                                                            .withLog(Logs.nullLog())
                                                            .withLogLevel(Level.SILENT)
                                                            .getConfigured();
        assertThat(builderFromOutputChannel(invocationConfiguration).getConfigured()).isEqualTo(
                configuration);
    }

    @Test
    public void testLogEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(builder().withLog(Logs.nullLog()).getConfigured());
        assertThat(
                configuration.builderFrom().withLog(Logs.systemLog()).getConfigured()).isNotEqualTo(
                builder().withLog(Logs.systemLog()).getConfigured());
    }

    @Test
    public void testLogLevelEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(builder().withLogLevel(Level.DEBUG).getConfigured());
        assertThat(configuration.builderFrom()
                                .withLogLevel(Level.WARNING)
                                .getConfigured()).isNotEqualTo(
                builder().withLogLevel(Level.WARNING).getConfigured());
    }

    @Test
    public void testReadTimeoutActionEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(
                builder().withReadTimeoutAction(TimeoutActionType.ABORT).getConfigured());
        assertThat(configuration).isNotEqualTo(
                builder().withReadTimeoutAction(TimeoutActionType.EXIT).getConfigured());
        assertThat(configuration.builderFrom()
                                .withReadTimeoutAction(TimeoutActionType.THROW)
                                .getConfigured()).isNotEqualTo(
                builder().withReadTimeoutAction(TimeoutActionType.THROW).getConfigured());
    }

    @Test
    public void testReadTimeoutEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(
                builder().withReadTimeout(TimeDuration.ZERO).getConfigured());
        assertThat(configuration).isNotEqualTo(
                builder().withReadTimeout(1, TimeUnit.MILLISECONDS).getConfigured());
        assertThat(configuration.builderFrom()
                                .withReadTimeout(millis(1))
                                .getConfigured()).isNotEqualTo(
                builder().withReadTimeout(1, TimeUnit.MILLISECONDS).getConfigured());
    }

    @Test
    public void testRunnerEquals() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withRunner(Runners.syncRunner())
                                                            .withLog(new NullLog())
                                                            .withChannelMaxSize(100)
                                                            .getConfigured();
        assertThat(configuration).isNotEqualTo(
                builder().withRunner(Runners.sharedRunner()).getConfigured());
        assertThat(configuration.builderFrom()
                                .withRunner(Runners.syncRunner())
                                .getConfigured()).isNotEqualTo(
                builder().withRunner(Runners.syncRunner()).getConfigured());
    }

    @Test
    public void testToInputChannelConfiguration() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withChannelLimit(10)
                                                            .withChannelMaxDelay(millis(33))
                                                            .withChannelMaxSize(100)
                                                            .withRunner(Runners.syncRunner())
                                                            .withReadTimeout(millis(100))
                                                            .withReadTimeoutAction(
                                                                    TimeoutActionType.ABORT)
                                                            .withLog(Logs.nullLog())
                                                            .withLogLevel(Level.SILENT)
                                                            .getConfigured();
        final com.github.dm.jrt.core.config.InvocationConfiguration.Builder<com.github.dm.jrt.core.config.InvocationConfiguration> builder =
                com.github.dm.jrt.core.config.InvocationConfiguration.builder();
        final com.github.dm.jrt.core.config.InvocationConfiguration invocationConfiguration =
                builder.withRunner(Runners.syncRunner())
                       .withReadTimeout(millis(100))
                       .withReadTimeoutAction(TimeoutActionType.ABORT)
                       .withLog(Logs.nullLog())
                       .withLogLevel(Level.SILENT)
                       .withInputOrder(OrderType.BY_CALL)
                       .withInputLimit(10)
                       .withInputMaxDelay(millis(33))
                       .withInputMaxSize(100)
                       .getConfigured();
        assertThat(configuration.toInputChannelConfiguration()).isEqualTo(invocationConfiguration);
    }

    @Test
    public void testToInvocationConfiguration() {

        final com.github.dm.jrt.core.config.ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withChannelLimit(10)
                                                            .withChannelMaxDelay(millis(33))
                                                            .withChannelMaxSize(100)
                                                            .withRunner(Runners.syncRunner())
                                                            .withReadTimeout(millis(100))
                                                            .withReadTimeoutAction(
                                                                    TimeoutActionType.ABORT)
                                                            .withLog(Logs.nullLog())
                                                            .withLogLevel(Level.SILENT)
                                                            .getConfigured();
        final com.github.dm.jrt.core.config.InvocationConfiguration.Builder<com.github.dm.jrt.core.config.InvocationConfiguration> builder =
                com.github.dm.jrt.core.config.InvocationConfiguration.builder();
        final com.github.dm.jrt.core.config.InvocationConfiguration invocationConfiguration =
                builder.withRunner(Runners.syncRunner())
                       .withReadTimeout(millis(100))
                       .withReadTimeoutAction(TimeoutActionType.ABORT)
                       .withLog(Logs.nullLog())
                       .withLogLevel(Level.SILENT)
                       .getConfigured();
        assertThat(configuration.toInvocationConfiguration()).isEqualTo(invocationConfiguration);
    }

    @Test
    public void testToOutputChannelConfiguration() {

        final ChannelConfiguration configuration = builder().withChannelOrder(OrderType.BY_CALL)
                                                            .withChannelLimit(10)
                                                            .withChannelMaxDelay(millis(33))
                                                            .withChannelMaxSize(100)
                                                            .withRunner(Runners.syncRunner())
                                                            .withReadTimeout(millis(100))
                                                            .withReadTimeoutAction(
                                                                    TimeoutActionType.ABORT)
                                                            .withLog(Logs.nullLog())
                                                            .withLogLevel(Level.SILENT)
                                                            .getConfigured();
        final com.github.dm.jrt.core.config.InvocationConfiguration.Builder<com.github.dm.jrt.core.config.InvocationConfiguration> builder =
                com.github.dm.jrt.core.config.InvocationConfiguration.builder();
        final com.github.dm.jrt.core.config.InvocationConfiguration invocationConfiguration =
                builder.withRunner(Runners.syncRunner())
                       .withReadTimeout(millis(100))
                       .withReadTimeoutAction(TimeoutActionType.ABORT)
                       .withLog(Logs.nullLog())
                       .withLogLevel(Level.SILENT)
                       .withOutputOrder(OrderType.BY_CALL)
                       .withOutputLimit(10)
                       .withOutputMaxDelay(millis(33))
                       .withOutputMaxSize(100)
                       .getConfigured();
        assertThat(configuration.toOutputChannelConfiguration()).isEqualTo(invocationConfiguration);
    }
}
