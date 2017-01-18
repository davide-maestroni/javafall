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

package com.github.dm.jrt;

import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.util.ClassToken;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.proxy.JRoutineProxy;
import com.github.dm.jrt.proxy.annotation.Proxy;
import com.github.dm.jrt.proxy.builder.ProxyRoutineBuilder;
import com.github.dm.jrt.reflect.InvocationTarget;
import com.github.dm.jrt.reflect.JRoutineReflection;
import com.github.dm.jrt.reflect.builder.ReflectionRoutineBuilder;
import com.github.dm.jrt.reflect.config.ReflectionConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Default implementation of a reflection/proxy routine builder.
 * <p>
 * Created by davide-maestroni on 03/03/2016.
 */
class DefaultReflectionProxyRoutineBuilder implements ReflectionProxyRoutineBuilder {

  private final InvocationTarget<?> mTarget;

  private InvocationConfiguration mInvocationConfiguration =
      InvocationConfiguration.defaultConfiguration();

  private ProxyStrategyType mProxyStrategyType;

  private ReflectionConfiguration mReflectionConfiguration =
      ReflectionConfiguration.defaultConfiguration();

  /**
   * Constructor.
   *
   * @param target the invocation target.
   * @throws java.lang.IllegalArgumentException if the class of specified target represents an
   *                                            interface.
   */
  DefaultReflectionProxyRoutineBuilder(@NotNull final InvocationTarget<?> target) {
    final Class<?> targetClass = target.getTargetClass();
    if (targetClass.isInterface()) {
      throw new IllegalArgumentException(
          "the target class must not be an interface: " + targetClass.getName());
    }

    mTarget = target;
  }

  @NotNull
  public ReflectionProxyRoutineBuilder apply(@NotNull final InvocationConfiguration configuration) {
    mInvocationConfiguration =
        ConstantConditions.notNull("invocation configuration", configuration);
    return this;
  }

  @NotNull
  public ReflectionProxyRoutineBuilder apply(@NotNull final ReflectionConfiguration configuration) {
    mReflectionConfiguration =
        ConstantConditions.notNull("reflection configuration", configuration);
    return this;
  }

  @NotNull
  public InvocationConfiguration.Builder<? extends ReflectionProxyRoutineBuilder>
  applyInvocationConfiguration() {
    final InvocationConfiguration config = mInvocationConfiguration;
    return new InvocationConfiguration.Builder<ReflectionProxyRoutineBuilder>(
        new InvocationConfiguration.Configurable<ReflectionProxyRoutineBuilder>() {

          @NotNull
          public ReflectionProxyRoutineBuilder apply(
              @NotNull final InvocationConfiguration configuration) {
            return DefaultReflectionProxyRoutineBuilder.this.apply(configuration);
          }
        }, config);
  }

  @NotNull
  public ReflectionConfiguration.Builder<? extends ReflectionProxyRoutineBuilder>
  applyReflectionConfiguration() {
    final ReflectionConfiguration config = mReflectionConfiguration;
    return new ReflectionConfiguration.Builder<ReflectionProxyRoutineBuilder>(
        new ReflectionConfiguration.Configurable<ReflectionProxyRoutineBuilder>() {

          @NotNull
          public ReflectionProxyRoutineBuilder apply(
              @NotNull final ReflectionConfiguration configuration) {
            return DefaultReflectionProxyRoutineBuilder.this.apply(configuration);
          }
        }, config);
  }

  @NotNull
  public ReflectionProxyRoutineBuilder withStrategy(
      @Nullable final ProxyStrategyType strategyType) {
    mProxyStrategyType = strategyType;
    return this;
  }

  @NotNull
  public <TYPE> TYPE buildProxy(@NotNull final Class<TYPE> itf) {
    final ProxyStrategyType proxyStrategyType = mProxyStrategyType;
    if (proxyStrategyType == null) {
      final Proxy proxyAnnotation = itf.getAnnotation(Proxy.class);
      if ((proxyAnnotation != null) && mTarget.isAssignableTo(proxyAnnotation.value())) {
        return newProxyBuilder().buildProxy(itf);
      }

      return newReflectionBuilder().buildProxy(itf);

    } else if (proxyStrategyType == ProxyStrategyType.CODE_GENERATION) {
      return newProxyBuilder().buildProxy(itf);
    }

    return newReflectionBuilder().buildProxy(itf);
  }

  @NotNull
  public <TYPE> TYPE buildProxy(@NotNull final ClassToken<TYPE> itf) {
    return buildProxy(itf.getRawClass());
  }

  @NotNull
  public <IN, OUT> Routine<IN, OUT> method(@NotNull final String name) {
    return newReflectionBuilder().method(name);
  }

  @NotNull
  public <IN, OUT> Routine<IN, OUT> method(@NotNull final String name,
      @NotNull final Class<?>... parameterTypes) {
    return newReflectionBuilder().method(name, parameterTypes);
  }

  @NotNull
  public <IN, OUT> Routine<IN, OUT> method(@NotNull final Method method) {
    return newReflectionBuilder().method(method);
  }

  @NotNull
  private ProxyRoutineBuilder newProxyBuilder() {
    return JRoutineProxy.with(mTarget)
                        .apply(mInvocationConfiguration)
                        .apply(mReflectionConfiguration);
  }

  @NotNull
  private ReflectionRoutineBuilder newReflectionBuilder() {
    return JRoutineReflection.with(mTarget)
                             .apply(mInvocationConfiguration)
                             .apply(mReflectionConfiguration);
  }
}
