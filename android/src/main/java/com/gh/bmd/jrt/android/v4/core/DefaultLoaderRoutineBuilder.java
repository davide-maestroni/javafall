/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh.bmd.jrt.android.v4.core;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.gh.bmd.jrt.android.builder.LoaderConfiguration;
import com.gh.bmd.jrt.android.builder.LoaderRoutineBuilder;
import com.gh.bmd.jrt.android.invocation.ContextInvocationFactory;
import com.gh.bmd.jrt.android.routine.LoaderRoutine;
import com.gh.bmd.jrt.android.runner.Runners;
import com.gh.bmd.jrt.builder.RoutineConfiguration;
import com.gh.bmd.jrt.builder.TemplateRoutineBuilder;
import com.gh.bmd.jrt.log.Logger;
import com.gh.bmd.jrt.runner.Runner;
import com.gh.bmd.jrt.time.TimeDuration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default implementation of an loader routine builder.
 * <p/>
 * Created by davide-maestroni on 12/9/14.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
class DefaultLoaderRoutineBuilder<INPUT, OUTPUT> extends TemplateRoutineBuilder<INPUT, OUTPUT>
        implements LoaderRoutineBuilder<INPUT, OUTPUT>,
        LoaderConfiguration.Configurable<LoaderRoutineBuilder<INPUT, OUTPUT>> {

    private final WeakReference<Object> mContext;

    private final ContextInvocationFactory<INPUT, OUTPUT> mFactory;

    private final RoutineConfiguration.Configurable<LoaderRoutineBuilder<INPUT, OUTPUT>>
            mRoutineConfigurable =
            new RoutineConfiguration.Configurable<LoaderRoutineBuilder<INPUT, OUTPUT>>() {

                @Nonnull
                public LoaderRoutineBuilder<INPUT, OUTPUT> setConfiguration(
                        @Nonnull final RoutineConfiguration configuration) {

                    return DefaultLoaderRoutineBuilder.this.setConfiguration(configuration);
                }
            };

    private LoaderConfiguration mLoaderConfiguration = LoaderConfiguration.DEFAULT_CONFIGURATION;

    /**
     * Constructor.
     *
     * @param activity the context activity.
     * @param factory  the invocation factory.
     * @throws java.lang.IllegalArgumentException if the class of the specified factory is not
     *                                            static.
     */
    DefaultLoaderRoutineBuilder(@Nonnull final FragmentActivity activity,
            @Nonnull final ContextInvocationFactory<INPUT, OUTPUT> factory) {

        this((Object) activity, factory);
    }

    /**
     * Constructor.
     *
     * @param fragment the context fragment.
     * @param factory  the invocation factory.
     * @throws java.lang.IllegalArgumentException if the class of the specified factory is not
     *                                            static.
     */
    DefaultLoaderRoutineBuilder(@Nonnull final Fragment fragment,
            @Nonnull final ContextInvocationFactory<INPUT, OUTPUT> factory) {

        this((Object) fragment, factory);
    }

    /**
     * Constructor.
     *
     * @param context the context instance.
     * @param factory the invocation factory.
     * @throws java.lang.IllegalArgumentException if the class of the specified factory is not
     *                                            static.
     */
    @SuppressWarnings("ConstantConditions")
    private DefaultLoaderRoutineBuilder(@Nonnull final Object context,
            @Nonnull final ContextInvocationFactory<INPUT, OUTPUT> factory) {

        if (context == null) {

            throw new NullPointerException("the routine context must not be null");
        }

        final Class<? extends ContextInvocationFactory> factoryClass = factory.getClass();

        if ((factoryClass.getEnclosingClass() != null) && !Modifier.isStatic(
                factoryClass.getModifiers())) {

            throw new IllegalArgumentException(
                    "the factory class must be static: " + factoryClass.getName());
        }

        mContext = new WeakReference<Object>(context);
        mFactory = factory;
    }

    @Nonnull
    public LoaderRoutine<INPUT, OUTPUT> buildRoutine() {

        final RoutineConfiguration configuration = getConfiguration();
        warn(configuration);
        final RoutineConfiguration.Builder<RoutineConfiguration> builder =
                configuration.builderFrom()
                             .withAsyncRunner(Runners.mainRunner())
                             .withInputMaxSize(Integer.MAX_VALUE)
                             .withInputTimeout(TimeDuration.INFINITY)
                             .withOutputMaxSize(Integer.MAX_VALUE)
                             .withOutputTimeout(TimeDuration.INFINITY);
        return new DefaultLoaderRoutine<INPUT, OUTPUT>(mContext, mFactory, builder.set(),
                                                       mLoaderConfiguration);
    }

    @Override
    public void purge() {

        buildRoutine().purge();
    }

    public void purge(@Nullable final INPUT input) {

        buildRoutine().purge(input);
    }

    public void purge(@Nullable final INPUT... inputs) {

        buildRoutine().purge(inputs);
    }

    public void purge(@Nullable final Iterable<? extends INPUT> inputs) {

        buildRoutine().purge(inputs);
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public LoaderRoutineBuilder<INPUT, OUTPUT> setConfiguration(
            @Nonnull final LoaderConfiguration configuration) {

        if (configuration == null) {

            throw new NullPointerException("the configuration must not be null");
        }

        mLoaderConfiguration = configuration;
        return this;
    }

    @Nonnull
    public LoaderRoutineBuilder<INPUT, OUTPUT> setConfiguration(
            @Nonnull final RoutineConfiguration configuration) {

        super.setConfiguration(configuration);
        return this;
    }

    @Nonnull
    @Override
    public RoutineConfiguration.Builder<? extends
            LoaderRoutineBuilder<INPUT, OUTPUT>> withRoutine() {

        return new RoutineConfiguration.Builder<LoaderRoutineBuilder<INPUT, OUTPUT>>(
                mRoutineConfigurable, getConfiguration());
    }

    @Nonnull
    public LoaderConfiguration.Builder<? extends LoaderRoutineBuilder<INPUT, OUTPUT>> withLoader() {

        final LoaderConfiguration config = mLoaderConfiguration;
        return new LoaderConfiguration.Builder<LoaderRoutineBuilder<INPUT, OUTPUT>>(this, config);
    }

    /**
     * Logs any warning related to ignored options in the specified configuration.
     *
     * @param configuration the routine configuration.
     */
    private void warn(@Nonnull final RoutineConfiguration configuration) {

        Logger logger = null;
        final Runner asyncRunner = configuration.getAsyncRunnerOr(null);

        if (asyncRunner != null) {

            logger = configuration.newLogger(this);
            logger.wrn("the specified runner will be ignored: %s", asyncRunner);
        }

        final int inputSize = configuration.getInputMaxSizeOr(RoutineConfiguration.DEFAULT);

        if (inputSize != RoutineConfiguration.DEFAULT) {

            if (logger == null) {

                logger = configuration.newLogger(this);
            }

            logger.wrn("the specified maximum input size will be ignored: %d", inputSize);
        }

        final TimeDuration inputTimeout = configuration.getInputTimeoutOr(null);

        if (inputTimeout != null) {

            if (logger == null) {

                logger = configuration.newLogger(this);
            }

            logger.wrn("the specified input timeout will be ignored: %s", inputTimeout);
        }

        final int outputSize = configuration.getOutputMaxSizeOr(RoutineConfiguration.DEFAULT);

        if (outputSize != RoutineConfiguration.DEFAULT) {

            if (logger == null) {

                logger = configuration.newLogger(this);
            }

            logger.wrn("the specified maximum output size will be ignored: %d", outputSize);
        }

        final TimeDuration outputTimeout = configuration.getOutputTimeoutOr(null);

        if (outputTimeout != null) {

            if (logger == null) {

                logger = configuration.newLogger(this);
            }

            logger.wrn("the specified output timeout will be ignored: %s", outputTimeout);
        }
    }
}