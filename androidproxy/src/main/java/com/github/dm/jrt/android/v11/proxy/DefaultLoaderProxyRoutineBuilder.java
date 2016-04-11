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

package com.github.dm.jrt.android.v11.proxy;

import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.object.ContextInvocationTarget;
import com.github.dm.jrt.android.proxy.annotation.LoaderProxy;
import com.github.dm.jrt.android.proxy.builder.AbstractLoaderProxyObjectBuilder;
import com.github.dm.jrt.android.proxy.builder.LoaderProxyRoutineBuilder;
import com.github.dm.jrt.android.v11.core.LoaderContext;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.util.ClassToken;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.object.config.ProxyConfiguration;
import com.github.dm.jrt.proxy.annotation.Proxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

import static com.github.dm.jrt.core.util.Reflection.findConstructor;

/**
 * Default implementation of a context proxy builder.
 * <p>
 * Created by davide-maestroni on 05/06/2015.
 */
class DefaultLoaderProxyRoutineBuilder implements LoaderProxyRoutineBuilder,
        InvocationConfiguration.Configurable<LoaderProxyRoutineBuilder>,
        ProxyConfiguration.Configurable<LoaderProxyRoutineBuilder>,
        LoaderConfiguration.Configurable<LoaderProxyRoutineBuilder> {

    private final LoaderContext mContext;

    private final ContextInvocationTarget<?> mTarget;

    private InvocationConfiguration mInvocationConfiguration =
            InvocationConfiguration.defaultConfiguration();

    private LoaderConfiguration mLoaderConfiguration = LoaderConfiguration.defaultConfiguration();

    private ProxyConfiguration mProxyConfiguration = ProxyConfiguration.defaultConfiguration();

    /**
     * Constructor.
     *
     * @param context the routine context.
     * @param target  the invocation target.
     */
    DefaultLoaderProxyRoutineBuilder(@NotNull final LoaderContext context,
            @NotNull final ContextInvocationTarget<?> target) {

        mContext = ConstantConditions.notNull("loader context", context);
        mTarget = ConstantConditions.notNull("context invocation target", target);
    }

    @NotNull
    public <TYPE> TYPE buildProxy(@NotNull final Class<TYPE> itf) {

        if (!itf.isInterface()) {
            throw new IllegalArgumentException(
                    "the specified class is not an interface: " + itf.getName());
        }

        if (!itf.isAnnotationPresent(LoaderProxy.class)) {
            throw new IllegalArgumentException(
                    "the specified class is not annotated with " + LoaderProxy.class.getName()
                            + ": " + itf.getName());
        }

        final TargetLoaderProxyObjectBuilder<TYPE> builder =
                new TargetLoaderProxyObjectBuilder<TYPE>(mContext, mTarget, itf);
        return builder.getInvocationConfiguration()
                      .with(mInvocationConfiguration)
                      .setConfiguration()
                      .getProxyConfiguration()
                      .with(mProxyConfiguration)
                      .setConfiguration()
                      .getLoaderConfiguration()
                      .with(mLoaderConfiguration)
                      .setConfiguration()
                      .buildProxy();
    }

    @NotNull
    public <TYPE> TYPE buildProxy(@NotNull final ClassToken<TYPE> itf) {

        return buildProxy(itf.getRawClass());
    }

    @NotNull
    public InvocationConfiguration.Builder<? extends LoaderProxyRoutineBuilder>
    getInvocationConfiguration() {

        final InvocationConfiguration config = mInvocationConfiguration;
        return new InvocationConfiguration.Builder<LoaderProxyRoutineBuilder>(this, config);
    }

    @NotNull
    public ProxyConfiguration.Builder<? extends LoaderProxyRoutineBuilder> getProxyConfiguration() {

        final ProxyConfiguration config = mProxyConfiguration;
        return new ProxyConfiguration.Builder<LoaderProxyRoutineBuilder>(this, config);
    }

    @NotNull
    public LoaderConfiguration.Builder<? extends LoaderProxyRoutineBuilder>
    getLoaderConfiguration() {

        final LoaderConfiguration config = mLoaderConfiguration;
        return new LoaderConfiguration.Builder<LoaderProxyRoutineBuilder>(this, config);
    }

    @NotNull
    public LoaderProxyRoutineBuilder setConfiguration(
            @NotNull final LoaderConfiguration configuration) {

        mLoaderConfiguration = ConstantConditions.notNull("loader configuration", configuration);
        return this;
    }

    @NotNull
    public LoaderProxyRoutineBuilder setConfiguration(
            @NotNull final ProxyConfiguration configuration) {

        mProxyConfiguration = ConstantConditions.notNull("proxy configuration", configuration);
        return this;
    }

    @NotNull
    public LoaderProxyRoutineBuilder setConfiguration(
            @NotNull final InvocationConfiguration configuration) {

        mInvocationConfiguration =
                ConstantConditions.notNull("invocation configuration", configuration);
        return this;
    }

    /**
     * Proxy builder implementation.
     *
     * @param <TYPE> the interface type.
     */
    private static class TargetLoaderProxyObjectBuilder<TYPE>
            extends AbstractLoaderProxyObjectBuilder<TYPE> {

        private final LoaderContext mContext;

        private final Class<? super TYPE> mInterfaceClass;

        private final ContextInvocationTarget<?> mTarget;

        /**
         * Constructor.
         *
         * @param context        the routine context.
         * @param target         the invocation target.
         * @param interfaceClass the proxy interface class.
         */
        private TargetLoaderProxyObjectBuilder(@NotNull final LoaderContext context,
                @NotNull final ContextInvocationTarget<?> target,
                @NotNull final Class<? super TYPE> interfaceClass) {

            mContext = context;
            mTarget = target;
            mInterfaceClass = interfaceClass;
        }

        @NotNull
        @Override
        protected Class<? super TYPE> getInterfaceClass() {

            return mInterfaceClass;
        }

        @Nullable
        @Override
        protected Object getInvocationContext() {

            return mContext.getComponent();
        }

        @NotNull
        @Override
        protected Class<?> getTargetClass() {

            return mTarget.getTargetClass();
        }

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        protected TYPE newProxy(@NotNull final InvocationConfiguration invocationConfiguration,
                @NotNull final ProxyConfiguration proxyConfiguration,
                @NotNull final LoaderConfiguration loaderConfiguration) throws Exception {

            final LoaderContext context = mContext;
            final ContextInvocationTarget<?> target = mTarget;
            final Class<? super TYPE> interfaceClass = mInterfaceClass;
            final LoaderProxy annotation = interfaceClass.getAnnotation(LoaderProxy.class);
            String packageName = annotation.classPackage();
            if (packageName.equals(Proxy.DEFAULT)) {
                final Package classPackage = interfaceClass.getPackage();
                packageName = (classPackage != null) ? classPackage.getName() + "." : "";

            } else {
                packageName += ".";
            }

            String className = annotation.className();
            if (className.equals(Proxy.DEFAULT)) {
                className = interfaceClass.getSimpleName();
                Class<?> enclosingClass = interfaceClass.getEnclosingClass();
                while (enclosingClass != null) {
                    className = enclosingClass.getSimpleName() + "_" + className;
                    enclosingClass = enclosingClass.getEnclosingClass();
                }
            }

            final String fullClassName =
                    packageName + annotation.classPrefix() + className + annotation.classSuffix();
            final Constructor<?> constructor =
                    findConstructor(Class.forName(fullClassName), context, target,
                            invocationConfiguration, proxyConfiguration, loaderConfiguration);
            return (TYPE) constructor.newInstance(context, target, invocationConfiguration,
                    proxyConfiguration, loaderConfiguration);
        }
    }
}