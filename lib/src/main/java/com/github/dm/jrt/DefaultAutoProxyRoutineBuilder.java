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
import com.github.dm.jrt.object.InvocationTarget;
import com.github.dm.jrt.object.JRoutineObject;
import com.github.dm.jrt.object.builder.ObjectRoutineBuilder;
import com.github.dm.jrt.object.config.ProxyConfiguration;
import com.github.dm.jrt.proxy.JRoutineProxy;
import com.github.dm.jrt.proxy.annotation.Proxy;
import com.github.dm.jrt.proxy.builder.ProxyRoutineBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Default implementation of a target routine builder.
 * <p>
 * Created by davide-maestroni on 03/03/2016.
 */
class DefaultAutoProxyRoutineBuilder implements AutoProxyRoutineBuilder {

    private final InvocationTarget<?> mTarget;

    private BuilderType mBuilderType;

    private InvocationConfiguration mInvocationConfiguration =
            InvocationConfiguration.defaultConfiguration();

    private final InvocationConfiguration.Configurable<DefaultAutoProxyRoutineBuilder>
            mInvocationConfigurable =
            new InvocationConfiguration.Configurable<DefaultAutoProxyRoutineBuilder>() {

                @NotNull
                public DefaultAutoProxyRoutineBuilder applyConfiguration(
                        @NotNull final InvocationConfiguration configuration) {

                    mInvocationConfiguration = configuration;
                    return DefaultAutoProxyRoutineBuilder.this;
                }
            };

    private ProxyConfiguration mProxyConfiguration = ProxyConfiguration.defaultConfiguration();

    private final ProxyConfiguration.Configurable<DefaultAutoProxyRoutineBuilder>
            mProxyConfigurable =
            new ProxyConfiguration.Configurable<DefaultAutoProxyRoutineBuilder>() {

                @NotNull
                public DefaultAutoProxyRoutineBuilder applyConfiguration(
                        @NotNull final ProxyConfiguration configuration) {

                    mProxyConfiguration = configuration;
                    return DefaultAutoProxyRoutineBuilder.this;
                }
            };

    /**
     * Constructor.
     *
     * @param target the invocation target.
     * @throws java.lang.IllegalArgumentException if the class of specified target represents an
     *                                            interface.
     */
    DefaultAutoProxyRoutineBuilder(@NotNull final InvocationTarget<?> target) {

        final Class<?> targetClass = target.getTargetClass();
        if (targetClass.isInterface()) {
            throw new IllegalArgumentException(
                    "the target class must not be an interface: " + targetClass.getName());
        }

        mTarget = target;
    }

    @NotNull
    public <TYPE> TYPE buildProxy(@NotNull final Class<TYPE> itf) {

        final BuilderType builderType = mBuilderType;
        if (builderType == null) {
            final Proxy proxyAnnotation = itf.getAnnotation(Proxy.class);
            if ((proxyAnnotation != null) && mTarget.isAssignableTo(proxyAnnotation.value())) {
                return newProxyBuilder().buildProxy(itf);
            }

            return newObjectBuilder().buildProxy(itf);

        } else if (builderType == BuilderType.PROXY) {
            return newProxyBuilder().buildProxy(itf);
        }

        return newObjectBuilder().buildProxy(itf);
    }

    @NotNull
    public <TYPE> TYPE buildProxy(@NotNull final ClassToken<TYPE> itf) {

        return buildProxy(itf.getRawClass());
    }

    @NotNull
    public <IN, OUT> Routine<IN, OUT> method(@NotNull final String name) {

        return newObjectBuilder().method(name);
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
    public InvocationConfiguration.Builder<? extends AutoProxyRoutineBuilder>
    invocationConfiguration() {

        final InvocationConfiguration config = mInvocationConfiguration;
        return new InvocationConfiguration.Builder<DefaultAutoProxyRoutineBuilder>(
                mInvocationConfigurable, config);
    }

    @NotNull
    public ProxyConfiguration.Builder<? extends AutoProxyRoutineBuilder> proxyConfiguration() {

        final ProxyConfiguration config = mProxyConfiguration;
        return new ProxyConfiguration.Builder<DefaultAutoProxyRoutineBuilder>(mProxyConfigurable,
                config);
    }

    @NotNull
    public AutoProxyRoutineBuilder withType(@Nullable final BuilderType builderType) {

        mBuilderType = builderType;
        return this;
    }

    @NotNull
    private ObjectRoutineBuilder newObjectBuilder() {

        return JRoutineObject.on(mTarget)
                             .invocationConfiguration()
                             .with(mInvocationConfiguration)
                             .apply()
                             .proxyConfiguration()
                             .with(mProxyConfiguration)
                             .apply();
    }

    @NotNull
    private ProxyRoutineBuilder newProxyBuilder() {

        return JRoutineProxy.on(mTarget)
                            .invocationConfiguration()
                            .with(mInvocationConfiguration)
                            .apply()
                            .proxyConfiguration()
                            .with(mProxyConfiguration)
                            .apply();
    }
}
