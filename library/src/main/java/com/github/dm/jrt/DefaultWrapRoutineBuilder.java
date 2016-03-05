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

import com.github.dm.jrt.core.builder.InvocationConfiguration;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.util.ClassToken;
import com.github.dm.jrt.object.InvocationTarget;
import com.github.dm.jrt.object.JRoutineObject;
import com.github.dm.jrt.object.builder.ObjectRoutineBuilder;
import com.github.dm.jrt.object.builder.ProxyConfiguration;
import com.github.dm.jrt.proxy.JRoutineProxy;
import com.github.dm.jrt.proxy.annotation.Proxy;
import com.github.dm.jrt.proxy.builder.ProxyRoutineBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Created by davide-maestroni on 03/03/2016.
 */
class DefaultWrapRoutineBuilder implements WrapRoutineBuilder {

    private final InvocationTarget<?> mTarget;

    private ProxyBuilderType mBuilderType;

    private InvocationConfiguration mInvocationConfiguration =
            InvocationConfiguration.DEFAULT_CONFIGURATION;

    private final InvocationConfiguration.Configurable<DefaultWrapRoutineBuilder>
            mInvocationConfigurable =
            new InvocationConfiguration.Configurable<DefaultWrapRoutineBuilder>() {

                @NotNull
                public DefaultWrapRoutineBuilder setConfiguration(
                        @NotNull final InvocationConfiguration configuration) {

                    mInvocationConfiguration = configuration;
                    return DefaultWrapRoutineBuilder.this;
                }
            };

    private ProxyConfiguration mProxyConfiguration = ProxyConfiguration.DEFAULT_CONFIGURATION;

    private final ProxyConfiguration.Configurable<DefaultWrapRoutineBuilder> mProxyConfigurable =
            new ProxyConfiguration.Configurable<DefaultWrapRoutineBuilder>() {

                @NotNull
                public DefaultWrapRoutineBuilder setConfiguration(
                        @NotNull final ProxyConfiguration configuration) {

                    mProxyConfiguration = configuration;
                    return DefaultWrapRoutineBuilder.this;
                }
            };

    DefaultWrapRoutineBuilder(@NotNull final InvocationTarget<?> target) {

        final Class<?> targetClass = target.getTargetClass();
        if (targetClass.isInterface()) {
            throw new IllegalArgumentException(
                    "the target class must not be an interface: " + targetClass.getName());
        }

        mTarget = target;
    }

    @NotNull
    public <IN, OUT> Routine<IN, OUT> alias(@NotNull final String name) {

        return newObjectBuilder().alias(name);
    }

    @NotNull
    public <TYPE> TYPE buildProxy(@NotNull final Class<TYPE> itf) {

        final ProxyBuilderType builderType = mBuilderType;
        if (builderType == null) {
            final Proxy proxyAnnotation = itf.getAnnotation(Proxy.class);
            if ((proxyAnnotation != null) && mTarget.isAssignableTo(proxyAnnotation.value())) {
                return newProxyBuilder().buildProxy(itf);
            }

            return newObjectBuilder().buildProxy(itf);

        } else if (builderType == ProxyBuilderType.PROCESSOR) {
            return newProxyBuilder().buildProxy(itf);
        }

        return newObjectBuilder().buildProxy(itf);
    }

    @NotNull
    public <TYPE> TYPE buildProxy(@NotNull final ClassToken<TYPE> itf) {

        return buildProxy(itf.getRawClass());
    }

    @NotNull
    public <IN, OUT> Routine<IN, OUT> method(@NotNull final String name,
            @NotNull final Class<?>... parameterTypes) {

        return newObjectBuilder().method(name, parameterTypes);
    }

    @NotNull
    public <IN, OUT> Routine<IN, OUT> method(@NotNull final Method method) {

        return newObjectBuilder().method(method);
    }

    @NotNull
    public WrapRoutineBuilder withBuilder(@Nullable final ProxyBuilderType builderType) {

        mBuilderType = builderType;
        return this;
    }

    @NotNull
    public InvocationConfiguration.Builder<? extends WrapRoutineBuilder> withInvocations() {

        return new InvocationConfiguration.Builder<DefaultWrapRoutineBuilder>(
                mInvocationConfigurable, mInvocationConfiguration);
    }

    @NotNull
    public ProxyConfiguration.Builder<? extends WrapRoutineBuilder> withProxies() {

        return new ProxyConfiguration.Builder<DefaultWrapRoutineBuilder>(mProxyConfigurable,
                                                                         mProxyConfiguration);
    }

    @NotNull
    private ObjectRoutineBuilder newObjectBuilder() {

        return JRoutineObject.on(mTarget)
                             .withInvocations()
                             .with(mInvocationConfiguration)
                             .getConfigured()
                             .withProxies()
                             .with(mProxyConfiguration)
                             .getConfigured();
    }

    @NotNull
    private ProxyRoutineBuilder newProxyBuilder() {

        return JRoutineProxy.on(mTarget)
                            .withInvocations()
                            .with(mInvocationConfiguration)
                            .getConfigured()
                            .withProxies()
                            .with(mProxyConfiguration)
                            .getConfigured();
    }
}
