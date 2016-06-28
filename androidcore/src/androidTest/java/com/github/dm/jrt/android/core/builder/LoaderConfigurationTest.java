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

package com.github.dm.jrt.android.core.builder;

import android.test.AndroidTestCase;

import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.Builder;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.CacheStrategyType;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.ClashResolutionType;
import com.github.dm.jrt.core.util.UnitDuration;

import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.android.core.config.LoaderConfiguration.builder;
import static com.github.dm.jrt.android.core.config.LoaderConfiguration.builderFrom;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Loader invocation configuration unit tests.
 * <p>
 * Created by davide-maestroni on 04/22/2015.
 */
public class LoaderConfigurationTest extends AndroidTestCase {

    public void testBuildFrom() {

        final ClashResolutionType resolutionType = ClashResolutionType.ABORT_OTHER;
        final CacheStrategyType strategyType = CacheStrategyType.CACHE;
        final LoaderConfiguration configuration = builder().withLoaderId(-1)
                                                           .withFactoryId(71)
                                                           .withClashResolution(resolutionType)
                                                           .withMatchResolution(resolutionType)
                                                           .withCacheStrategy(strategyType)
                                                           .withResultStaleTime(1, TimeUnit.SECONDS)
                                                           .applied();
        assertThat(configuration.builderFrom().applied()).isEqualTo(configuration);
        assertThat(builderFrom(null).applied()).isEqualTo(
                LoaderConfiguration.defaultConfiguration());
    }

    @SuppressWarnings("ConstantConditions")
    public void testBuildNullPointerError() {

        try {

            new Builder<Object>(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new Builder<Object>(null, LoaderConfiguration.defaultConfiguration());

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    public void testBuilderFromEquals() {

        final ClashResolutionType resolutionType = ClashResolutionType.ABORT_OTHER;
        final CacheStrategyType strategyType = CacheStrategyType.CACHE;
        final LoaderConfiguration configuration = builder().withLoaderId(-1)
                                                           .withFactoryId(71)
                                                           .withClashResolution(resolutionType)
                                                           .withMatchResolution(resolutionType)
                                                           .withCacheStrategy(strategyType)
                                                           .withResultStaleTime(1, TimeUnit.SECONDS)
                                                           .applied();
        assertThat(builder().with(configuration).applied()).isEqualTo(configuration);
        assertThat(configuration.builderFrom().applied()).isEqualTo(configuration);
        assertThat(configuration.builderFrom().with(null).applied()).isEqualTo(
                LoaderConfiguration.defaultConfiguration());
    }

    public void testCacheStrategyEquals() {

        final ClashResolutionType resolutionType = ClashResolutionType.ABORT_OTHER;
        final CacheStrategyType strategyType = CacheStrategyType.CACHE;
        final LoaderConfiguration configuration = builder().withLoaderId(-1)
                                                           .withClashResolution(resolutionType)
                                                           .withMatchResolution(resolutionType)
                                                           .withCacheStrategy(strategyType)
                                                           .withResultStaleTime(1, TimeUnit.SECONDS)
                                                           .applied();
        assertThat(configuration).isNotEqualTo(
                builder().withCacheStrategy(CacheStrategyType.CLEAR).applied());
        assertThat(configuration.builderFrom()
                                .withCacheStrategy(CacheStrategyType.CACHE_IF_ERROR)
                                .applied()).isNotEqualTo(
                builder().withCacheStrategy(CacheStrategyType.CACHE_IF_ERROR).applied());
    }

    public void testClashResolutionEquals() {

        final ClashResolutionType resolutionType = ClashResolutionType.ABORT_OTHER;
        final CacheStrategyType strategyType = CacheStrategyType.CACHE;
        final LoaderConfiguration configuration = builder().withLoaderId(-1)
                                                           .withClashResolution(resolutionType)
                                                           .withMatchResolution(resolutionType)
                                                           .withCacheStrategy(strategyType)
                                                           .withResultStaleTime(1, TimeUnit.SECONDS)
                                                           .applied();
        assertThat(configuration).isNotEqualTo(
                builder().withClashResolution(ClashResolutionType.ABORT_THIS).applied());
        assertThat(
                configuration.builderFrom().withClashResolution(ClashResolutionType.JOIN).applied())
                .isNotEqualTo(builder().withClashResolution(ClashResolutionType.JOIN).applied());
    }

    public void testFactoryIdEquals() {

        final ClashResolutionType resolutionType = ClashResolutionType.ABORT_OTHER;
        final CacheStrategyType strategyType = CacheStrategyType.CACHE;
        final LoaderConfiguration configuration = builder().withLoaderId(-1)
                                                           .withFactoryId(71)
                                                           .withClashResolution(resolutionType)
                                                           .withMatchResolution(resolutionType)
                                                           .withCacheStrategy(strategyType)
                                                           .withResultStaleTime(1, TimeUnit.SECONDS)
                                                           .applied();
        assertThat(configuration).isNotEqualTo(builder().withFactoryId(3).applied());
        assertThat(configuration.builderFrom().withFactoryId(27).applied()).isNotEqualTo(
                builder().withFactoryId(27).applied());
    }

    public void testIdEquals() {

        final ClashResolutionType resolutionType = ClashResolutionType.ABORT_OTHER;
        final CacheStrategyType strategyType = CacheStrategyType.CACHE;
        final LoaderConfiguration configuration = builder().withLoaderId(-1)
                                                           .withClashResolution(resolutionType)
                                                           .withMatchResolution(resolutionType)
                                                           .withCacheStrategy(strategyType)
                                                           .withResultStaleTime(1, TimeUnit.SECONDS)
                                                           .applied();
        assertThat(configuration).isNotEqualTo(builder().withLoaderId(3).applied());
        assertThat(configuration.builderFrom().withLoaderId(27).applied()).isNotEqualTo(
                builder().withLoaderId(27).applied());
    }

    public void testMatchResolutionEquals() {

        final ClashResolutionType resolutionType = ClashResolutionType.ABORT_OTHER;
        final CacheStrategyType strategyType = CacheStrategyType.CACHE;
        final LoaderConfiguration configuration = builder().withLoaderId(-1)
                                                           .withClashResolution(resolutionType)
                                                           .withMatchResolution(resolutionType)
                                                           .withCacheStrategy(strategyType)
                                                           .withResultStaleTime(1, TimeUnit.SECONDS)
                                                           .applied();
        assertThat(configuration).isNotEqualTo(
                builder().withMatchResolution(ClashResolutionType.ABORT_THIS).applied());
        assertThat(
                configuration.builderFrom().withMatchResolution(ClashResolutionType.JOIN).applied())
                .isNotEqualTo(builder().withMatchResolution(ClashResolutionType.JOIN).applied());
    }

    public void testStaleTimeEquals() {

        final ClashResolutionType resolutionType = ClashResolutionType.ABORT_OTHER;
        final CacheStrategyType strategyType = CacheStrategyType.CACHE;
        final LoaderConfiguration configuration = builder().withLoaderId(-1)
                                                           .withClashResolution(resolutionType)
                                                           .withMatchResolution(resolutionType)
                                                           .withCacheStrategy(strategyType)
                                                           .withResultStaleTime(1, TimeUnit.SECONDS)
                                                           .applied();
        assertThat(configuration).isNotEqualTo(
                builder().withResultStaleTime(UnitDuration.days(3)).applied());
        assertThat(configuration.builderFrom().withResultStaleTime(UnitDuration.hours(7)).applied())
                .isNotEqualTo(builder().withResultStaleTime(UnitDuration.hours(7)).applied());
    }

    @SuppressWarnings("ConstantConditions")
    public void testStaleTimeErrors() {

        try {

            builder().withResultStaleTime(1, null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            builder().withResultStaleTime(-1, TimeUnit.MILLISECONDS);

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }
}
