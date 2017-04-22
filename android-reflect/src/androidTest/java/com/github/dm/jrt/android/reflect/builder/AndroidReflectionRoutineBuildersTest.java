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

package com.github.dm.jrt.android.reflect.builder;

import android.test.AndroidTestCase;

import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.CacheStrategyType;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.ClashResolutionType;
import com.github.dm.jrt.android.core.config.ServiceConfiguration;
import com.github.dm.jrt.android.reflect.annotation.CacheStrategy;
import com.github.dm.jrt.android.reflect.annotation.ClashResolution;
import com.github.dm.jrt.android.reflect.annotation.InvocationId;
import com.github.dm.jrt.android.reflect.annotation.LoaderId;
import com.github.dm.jrt.android.reflect.annotation.MatchResolution;
import com.github.dm.jrt.android.reflect.annotation.ResultStaleTime;
import com.github.dm.jrt.android.reflect.annotation.ServiceExecutor;
import com.github.dm.jrt.android.reflect.annotation.ServiceLog;
import com.github.dm.jrt.core.executor.ScheduledExecutorDecorator;
import com.github.dm.jrt.core.executor.ScheduledExecutors;
import com.github.dm.jrt.core.log.NullLog;
import com.github.dm.jrt.core.util.DurationMeasure;

import static com.github.dm.jrt.android.reflect.builder.AndroidReflectionRoutineBuilders
    .withAnnotations;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Android builder unit tests.
 * <p>
 * Created by davide-maestroni on 03/08/2016.
 */
public class AndroidReflectionRoutineBuildersTest extends AndroidTestCase {

  public void testBuilderConfigurationThroughAnnotations() throws NoSuchMethodException {

    assertThat(withAnnotations(LoaderConfiguration.defaultConfiguration(),
        AnnotationItf.class.getMethod("toString"))).isEqualTo( //
        LoaderConfiguration.builder()
                           .withCacheStrategy(CacheStrategyType.CACHE_IF_ERROR)
                           .withClashResolution(ClashResolutionType.ABORT_BOTH)
                           .withInvocationId(13)
                           .withMatchResolution(ClashResolutionType.ABORT_THIS)
                           .withLoaderId(-77)
                           .withResultStaleTime(DurationMeasure.millis(333))
                           .apply());
    assertThat(
        withAnnotations(ServiceConfiguration.builder().withExecutorArgs(1).withLogArgs(1).apply(),
            AnnotationItf.class.getMethod("toString"))).isEqualTo( //
        ServiceConfiguration.builder()
                            .withLogClass(NullLog.class)
                            .withExecutorClass(MyExecutor.class)
                            .apply());
  }

  public void testConstructor() {

    boolean failed = false;
    try {
      new AndroidReflectionRoutineBuilders();
      failed = true;

    } catch (final Throwable ignored) {

    }

    assertThat(failed).isFalse();
  }

  public interface AnnotationItf {

    @CacheStrategy(CacheStrategyType.CACHE_IF_ERROR)
    @ClashResolution(ClashResolutionType.ABORT_BOTH)
    @InvocationId(13)
    @LoaderId(-77)
    @MatchResolution(ClashResolutionType.ABORT_THIS)
    @ResultStaleTime(333)
    @ServiceExecutor(MyExecutor.class)
    @ServiceLog(NullLog.class)
    String toString();
  }

  public static class MyExecutor extends ScheduledExecutorDecorator {

    public MyExecutor() {
      super(ScheduledExecutors.defaultExecutor());
    }
  }
}
