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

package com.github.dm.jrt.android.v4;

import com.github.dm.jrt.WrapperRoutineBuilder.ProxyStrategyType;
import com.github.dm.jrt.android.LoaderWrapperRoutineBuilder;
import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.proxy.annotation.LoaderProxyCompat;
import com.github.dm.jrt.android.proxy.builder.LoaderProxyRoutineBuilder;
import com.github.dm.jrt.android.reflect.ContextInvocationTarget;
import com.github.dm.jrt.android.reflect.builder.LoaderReflectionRoutineBuilder;
import com.github.dm.jrt.android.v4.core.LoaderSourceCompat;
import com.github.dm.jrt.android.v4.proxy.JRoutineLoaderProxyCompat;
import com.github.dm.jrt.android.v4.reflect.JRoutineLoaderReflectionCompat;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.config.InvocationConfiguration.Builder;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.util.ClassToken;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.reflect.config.WrapperConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Default implementation of a Loader reflection/proxy routine builder.
 * <p>
 * Created by davide-maestroni on 03/07/2016.
 */
class DefaultLoaderWrapperRoutineBuilderCompat implements LoaderWrapperRoutineBuilder {

  private final LoaderSourceCompat mLoaderSource;

  private InvocationConfiguration mInvocationConfiguration =
      InvocationConfiguration.defaultConfiguration();

  private LoaderConfiguration mLoaderConfiguration = LoaderConfiguration.defaultConfiguration();

  private ProxyStrategyType mProxyStrategyType;

  private WrapperConfiguration mWrapperConfiguration = WrapperConfiguration.defaultConfiguration();

  /**
   * Constructor.
   *
   * @param loaderSource the Loader source.
   */
  DefaultLoaderWrapperRoutineBuilderCompat(@NotNull final LoaderSourceCompat loaderSource) {
    mLoaderSource = ConstantConditions.notNull("Loader source", loaderSource);
  }

  @NotNull
  @Override
  public <IN, OUT> Routine<IN, OUT> methodOf(@NotNull final ContextInvocationTarget<?> target,
      @NotNull final String name) {
    return newReflectionBuilder().methodOf(target, name);
  }

  @NotNull
  @Override
  public <IN, OUT> Routine<IN, OUT> methodOf(@NotNull final ContextInvocationTarget<?> target,
      @NotNull final String name, @NotNull final Class<?>... parameterTypes) {
    return newReflectionBuilder().methodOf(target, name, parameterTypes);
  }

  @NotNull
  @Override
  public <IN, OUT> Routine<IN, OUT> methodOf(@NotNull final ContextInvocationTarget<?> target,
      @NotNull final Method method) {
    return newReflectionBuilder().methodOf(target, method);
  }

  @NotNull
  @Override
  public <TYPE> TYPE proxyOf(@NotNull final ContextInvocationTarget<?> target,
      @NotNull final ClassToken<TYPE> itf) {
    return proxyOf(target, itf.getRawClass());
  }

  @NotNull
  @Override
  public <TYPE> TYPE proxyOf(@NotNull final ContextInvocationTarget<?> target,
      @NotNull final Class<TYPE> itf) {
    final ProxyStrategyType proxyStrategyType = mProxyStrategyType;
    if (proxyStrategyType == null) {
      final LoaderProxyCompat proxyAnnotation = itf.getAnnotation(LoaderProxyCompat.class);
      if ((proxyAnnotation != null) && target.isAssignableTo(proxyAnnotation.value())) {
        return newProxyBuilder().proxyOf(target, itf);
      }

      return newReflectionBuilder().proxyOf(target, itf);

    } else if (proxyStrategyType == ProxyStrategyType.CODE_GENERATION) {
      return newProxyBuilder().proxyOf(target, itf);
    }

    return newReflectionBuilder().proxyOf(target, itf);
  }

  @NotNull
  @Override
  public LoaderWrapperRoutineBuilder withConfiguration(
      @NotNull final InvocationConfiguration configuration) {
    mInvocationConfiguration =
        ConstantConditions.notNull("invocation configuration", configuration);
    return this;
  }

  @NotNull
  @Override
  public LoaderWrapperRoutineBuilder withConfiguration(
      @NotNull final WrapperConfiguration configuration) {
    mWrapperConfiguration = ConstantConditions.notNull("wrapper configuration", configuration);
    return this;
  }

  @NotNull
  @Override
  public Builder<? extends LoaderWrapperRoutineBuilder> withInvocation() {
    return new InvocationConfiguration.Builder<LoaderWrapperRoutineBuilder>(
        new InvocationConfiguration.Configurable<LoaderWrapperRoutineBuilder>() {

          @NotNull
          @Override
          public LoaderWrapperRoutineBuilder withConfiguration(
              @NotNull final InvocationConfiguration configuration) {
            return DefaultLoaderWrapperRoutineBuilderCompat.this.withConfiguration(configuration);
          }
        }, mInvocationConfiguration);
  }

  @NotNull
  @Override
  public WrapperConfiguration.Builder<? extends LoaderWrapperRoutineBuilder> withWrapper() {
    return new WrapperConfiguration.Builder<LoaderWrapperRoutineBuilder>(
        new WrapperConfiguration.Configurable<LoaderWrapperRoutineBuilder>() {

          @NotNull
          @Override
          public LoaderWrapperRoutineBuilder withConfiguration(
              @NotNull final WrapperConfiguration configuration) {
            return DefaultLoaderWrapperRoutineBuilderCompat.this.withConfiguration(configuration);
          }
        }, mWrapperConfiguration);
  }

  @NotNull
  @Override
  public LoaderWrapperRoutineBuilder withConfiguration(
      @NotNull final LoaderConfiguration configuration) {
    mLoaderConfiguration = ConstantConditions.notNull("Loader configuration", configuration);
    return this;
  }

  @NotNull
  @Override
  public LoaderConfiguration.Builder<? extends LoaderWrapperRoutineBuilder> withLoader() {
    return new LoaderConfiguration.Builder<LoaderWrapperRoutineBuilder>(
        new LoaderConfiguration.Configurable<LoaderWrapperRoutineBuilder>() {

          @NotNull
          @Override
          public LoaderWrapperRoutineBuilder withConfiguration(
              @NotNull final LoaderConfiguration configuration) {
            return DefaultLoaderWrapperRoutineBuilderCompat.this.withConfiguration(configuration);
          }
        }, mLoaderConfiguration);
  }

  @NotNull
  @Override
  public LoaderWrapperRoutineBuilder withStrategy(@Nullable final ProxyStrategyType strategyType) {
    mProxyStrategyType = strategyType;
    return this;
  }

  @NotNull
  private LoaderProxyRoutineBuilder newProxyBuilder() {
    return JRoutineLoaderProxyCompat.wrapperOn(mLoaderSource)
                                    .withConfiguration(mInvocationConfiguration)
                                    .withConfiguration(mWrapperConfiguration)
                                    .withConfiguration(mLoaderConfiguration);
  }

  @NotNull
  private LoaderReflectionRoutineBuilder newReflectionBuilder() {
    return JRoutineLoaderReflectionCompat.wrapperOn(mLoaderSource)
                                         .withConfiguration(mInvocationConfiguration)
                                         .withConfiguration(mWrapperConfiguration)
                                         .withConfiguration(mLoaderConfiguration);
  }
}
