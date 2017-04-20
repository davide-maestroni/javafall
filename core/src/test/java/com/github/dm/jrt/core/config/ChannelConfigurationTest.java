/*
 * Copyright 2017 Davide Maestroni
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

package com.github.dm.jrt.core.config;

import com.github.dm.jrt.core.config.ChannelConfiguration.Builder;
import com.github.dm.jrt.core.config.ChannelConfiguration.OrderType;
import com.github.dm.jrt.core.config.ChannelConfiguration.TimeoutActionType;
import com.github.dm.jrt.core.log.Log.Level;
import com.github.dm.jrt.core.log.Logs;
import com.github.dm.jrt.core.log.NullLog;
import com.github.dm.jrt.core.runner.Runners;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.core.common.BackoffBuilder.afterCount;
import static com.github.dm.jrt.core.common.BackoffBuilder.noDelay;
import static com.github.dm.jrt.core.config.ChannelConfiguration.builder;
import static com.github.dm.jrt.core.config.ChannelConfiguration.builderFrom;
import static com.github.dm.jrt.core.util.DurationMeasure.millis;
import static com.github.dm.jrt.core.util.DurationMeasure.noTime;
import static com.github.dm.jrt.core.util.DurationMeasure.seconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Channel configuration unit tests.
 * <p>
 * Created by davide-maestroni on 07/03/2015.
 */
public class ChannelConfigurationTest {

  @Test
  public void testBuildFrom() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(builderFrom(configuration).apply().hashCode()).isEqualTo(configuration.hashCode());
    assertThat(builderFrom(configuration).apply()).isEqualTo(configuration);
    assertThat(builderFrom(null).apply().hashCode()).isEqualTo(
        ChannelConfiguration.defaultConfiguration().hashCode());
    assertThat(builderFrom(null).apply()).isEqualTo(ChannelConfiguration.defaultConfiguration());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testBuildNullPointerError() {

    try {

      new Builder<Object>(null);

      fail();

    } catch (final NullPointerException ignored) {

    }
  }

  @Test
  public void testBuilderFromEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .withLogLevel(Level.SILENT)
                                                        .withBackoff(
                                                            afterCount(1).constantDelay(seconds(1)))
                                                        .withOutputTimeout(seconds(10))
                                                        .withOutputTimeoutAction(
                                                            TimeoutActionType.ABORT)
                                                        .apply();
    assertThat(builder().withPatch(configuration).apply()).isEqualTo(configuration);
    assertThat(configuration.builderFrom().apply()).isEqualTo(configuration);
    assertThat(configuration.builderFrom().withPatch(null).apply()).isEqualTo(configuration);
    assertThat(configuration.builderFrom().withDefaults().apply()).isEqualTo(
        ChannelConfiguration.defaultConfiguration());
  }

  @Test
  public void testChannelBackoffEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(configuration).isNotEqualTo(builder().withBackoff(noDelay()).apply());
    assertThat(configuration).isNotEqualTo(
        builder().withBackoff(afterCount(1).constantDelay(1, TimeUnit.MILLISECONDS)).apply());
    assertThat(configuration.builderFrom()
                            .withBackoff(afterCount(1).constantDelay(millis(1)))
                            .apply()).isNotEqualTo(
        builder().withBackoff(afterCount(1).constantDelay(1, TimeUnit.MILLISECONDS)).apply());
  }

  @Test
  public void testChannelOrderEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(configuration).isNotEqualTo(builder().withOrder(OrderType.UNSORTED).apply());
    assertThat(configuration.builderFrom().withOrder(OrderType.SORTED).apply()).isNotEqualTo(
        builder().withOrder(OrderType.SORTED).apply());
  }

  @Test
  public void testChannelSizeEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(configuration).isNotEqualTo(builder().withMaxSize(10).apply());
    assertThat(configuration.builderFrom().withMaxSize(1).apply()).isNotEqualTo(
        builder().withMaxSize(1).apply());
  }

  @Test
  public void testChannelSizeError() {

    try {

      builder().withMaxSize(0);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  @Test
  public void testLogEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(configuration).isNotEqualTo(builder().withLog(Logs.nullLog()).apply());
    assertThat(configuration.builderFrom().withLog(Logs.systemLog()).apply()).isNotEqualTo(
        builder().withLog(Logs.systemLog()).apply());
  }

  @Test
  public void testLogLevelEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(configuration).isNotEqualTo(builder().withLogLevel(Level.DEBUG).apply());
    assertThat(configuration.builderFrom().withLogLevel(Level.WARNING).apply()).isNotEqualTo(
        builder().withLogLevel(Level.WARNING).apply());
  }

  @Test
  public void testReadTimeoutActionEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(configuration).isNotEqualTo(
        builder().withOutputTimeoutAction(TimeoutActionType.ABORT).apply());
    assertThat(configuration).isNotEqualTo(
        builder().withOutputTimeoutAction(TimeoutActionType.CONTINUE).apply());
    assertThat(configuration.builderFrom()
                            .withOutputTimeoutAction(TimeoutActionType.FAIL)
                            .apply()).isNotEqualTo(
        builder().withOutputTimeoutAction(TimeoutActionType.FAIL).apply());
  }

  @Test
  public void testReadTimeoutEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(configuration).isNotEqualTo(builder().withOutputTimeout(noTime()).apply());
    assertThat(configuration).isNotEqualTo(
        builder().withOutputTimeout(1, TimeUnit.MILLISECONDS).apply());
    assertThat(configuration.builderFrom().withOutputTimeout(millis(1)).apply()).isNotEqualTo(
        builder().withOutputTimeout(1, TimeUnit.MILLISECONDS).apply());
  }

  @Test
  public void testRunnerEquals() {

    final ChannelConfiguration configuration = builder().withOrder(OrderType.SORTED)
                                                        .withRunner(Runners.syncRunner())
                                                        .withLog(new NullLog())
                                                        .withMaxSize(100)
                                                        .apply();
    assertThat(configuration).isNotEqualTo(builder().withRunner(Runners.sharedRunner()).apply());
    assertThat(configuration.builderFrom().withRunner(Runners.syncRunner()).apply()).isNotEqualTo(
        builder().withRunner(Runners.syncRunner()).apply());
  }
}