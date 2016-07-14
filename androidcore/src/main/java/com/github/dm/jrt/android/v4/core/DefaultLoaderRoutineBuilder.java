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

package com.github.dm.jrt.android.v4.core;

import com.github.dm.jrt.android.core.builder.LoaderRoutineBuilder;
import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.core.invocation.ContextInvocationFactory;
import com.github.dm.jrt.android.core.routine.LoaderRoutine;
import com.github.dm.jrt.core.builder.TemplateRoutineBuilder;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.runner.Runner;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.core.util.Reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.dm.jrt.android.core.runner.AndroidRunners.mainRunner;
import static com.github.dm.jrt.core.runner.Runners.zeroDelayRunner;

/**
 * Default implementation of a loader routine builder.
 * <p>
 * Created by davide-maestroni on 12/09/2014.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
class DefaultLoaderRoutineBuilder<IN, OUT> extends TemplateRoutineBuilder<IN, OUT>
        implements LoaderRoutineBuilder<IN, OUT>,
        LoaderConfiguration.Configurable<LoaderRoutineBuilder<IN, OUT>> {

    private final LoaderContextCompat mContext;

    private final ContextInvocationFactory<IN, OUT> mFactory;

    private final InvocationConfiguration.Configurable<LoaderRoutineBuilder<IN, OUT>>
            mRoutineConfigurable =
            new InvocationConfiguration.Configurable<LoaderRoutineBuilder<IN, OUT>>() {

                @NotNull
                public LoaderRoutineBuilder<IN, OUT> apply(
                        @NotNull final InvocationConfiguration configuration) {
                    return DefaultLoaderRoutineBuilder.this.apply(configuration);
                }
            };

    private LoaderConfiguration mLoaderConfiguration = LoaderConfiguration.defaultConfiguration();

    /**
     * Constructor.
     *
     * @param context the routine context.
     * @param factory the invocation factory.
     * @throws java.lang.IllegalArgumentException if the class of the specified factory has not a
     *                                            static scope.
     */
    DefaultLoaderRoutineBuilder(@NotNull final LoaderContextCompat context,
            @NotNull final ContextInvocationFactory<IN, OUT> factory) {
        mContext = ConstantConditions.notNull("loader context", context);
        final Class<? extends ContextInvocationFactory> factoryClass = factory.getClass();
        if (!Reflection.hasStaticScope(factoryClass)) {
            throw new IllegalArgumentException(
                    "the factory class must have a static scope: " + factoryClass.getName());
        }

        mFactory = factory;
    }

    @NotNull
    @Override
    public LoaderRoutineBuilder<IN, OUT> apply(
            @NotNull final InvocationConfiguration configuration) {
        super.apply(configuration);
        return this;
    }

    @NotNull
    @Override
    public InvocationConfiguration.Builder<? extends
            LoaderRoutineBuilder<IN, OUT>> invocationConfiguration() {
        return new InvocationConfiguration.Builder<LoaderRoutineBuilder<IN, OUT>>(
                mRoutineConfigurable, getConfiguration());
    }

    @NotNull
    @Override
    public LoaderRoutineBuilder<IN, OUT> apply(@NotNull final LoaderConfiguration configuration) {
        mLoaderConfiguration = ConstantConditions.notNull("loader configuration", configuration);
        return this;
    }

    @NotNull
    @Override
    public LoaderRoutine<IN, OUT> buildRoutine() {
        final InvocationConfiguration configuration = getConfiguration();
        final Runner asyncRunner = configuration.getRunnerOrElse(null);
        if (asyncRunner != null) {
            configuration.newLogger(this)
                         .wrn("the specified async runner will be ignored: %s", asyncRunner);
        }

        final InvocationConfiguration.Builder<InvocationConfiguration> builder =
                configuration.builderFrom().withRunner(zeroDelayRunner(mainRunner()));
        return new DefaultLoaderRoutine<IN, OUT>(mContext, mFactory, builder.configured(),
                mLoaderConfiguration);
    }

    @Override
    public void clear(@Nullable final IN input) {
        buildRoutine().clear(input);
    }

    public void clear(@Nullable final IN... inputs) {
        buildRoutine().clear(inputs);
    }

    @Override
    public void clear(@Nullable final Iterable<? extends IN> inputs) {
        buildRoutine().clear(inputs);
    }

    @Override
    public void clear() {
        buildRoutine().clear();
    }

    @NotNull
    @Override
    public LoaderConfiguration.Builder<? extends LoaderRoutineBuilder<IN, OUT>>
    loaderConfiguration() {
        final LoaderConfiguration config = mLoaderConfiguration;
        return new LoaderConfiguration.Builder<LoaderRoutineBuilder<IN, OUT>>(this, config);
    }
}
