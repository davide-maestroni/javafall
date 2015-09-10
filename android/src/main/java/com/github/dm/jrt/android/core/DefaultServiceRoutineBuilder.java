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
package com.github.dm.jrt.android.core;

import com.github.dm.jrt.android.builder.ServiceConfiguration;
import com.github.dm.jrt.android.builder.ServiceRoutineBuilder;
import com.github.dm.jrt.builder.InvocationConfiguration;
import com.github.dm.jrt.builder.TemplateRoutineBuilder;
import com.github.dm.jrt.routine.Routine;

import javax.annotation.Nonnull;

/**
 * Class implementing a builder of routine objects executed in a dedicated service.
 * <p/>
 * Created by davide-maestroni on 01/08/2015.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
class DefaultServiceRoutineBuilder<IN, OUT> extends TemplateRoutineBuilder<IN, OUT>
        implements ServiceRoutineBuilder<IN, OUT>,
        ServiceConfiguration.Configurable<ServiceRoutineBuilder<IN, OUT>> {

    private final InvocationConfiguration.Configurable<ServiceRoutineBuilder<IN, OUT>>
            mConfigurable =
            new InvocationConfiguration.Configurable<ServiceRoutineBuilder<IN, OUT>>() {

                @Nonnull
                public ServiceRoutineBuilder<IN, OUT> setConfiguration(
                        @Nonnull final InvocationConfiguration configuration) {

                    return DefaultServiceRoutineBuilder.this.setConfiguration(configuration);
                }
            };

    private final ServiceContext mContext;

    private final TargetInvocationFactory<IN, OUT> mTargetFactory;

    private ServiceConfiguration mServiceConfiguration = ServiceConfiguration.DEFAULT_CONFIGURATION;

    /**
     * Constructor.
     *
     * @param context the routine context.
     * @param target  the invocation factory target.
     */
    @SuppressWarnings("ConstantConditions")
    DefaultServiceRoutineBuilder(@Nonnull final ServiceContext context,
            @Nonnull final TargetInvocationFactory<IN, OUT> target) {

        if (context == null) {

            throw new NullPointerException("the context must not be null");
        }

        if (target == null) {

            throw new NullPointerException("the invocation target not be null");
        }

        mContext = context;
        mTargetFactory = target;
    }

    @Nonnull
    public Routine<IN, OUT> buildRoutine() {

        return new ServiceRoutine<IN, OUT>(mContext, mTargetFactory, getConfiguration(),
                                           mServiceConfiguration);
    }

    @Nonnull
    @Override
    public InvocationConfiguration.Builder<? extends ServiceRoutineBuilder<IN, OUT>> invocations() {

        final InvocationConfiguration config = getConfiguration();
        return new InvocationConfiguration.Builder<ServiceRoutineBuilder<IN, OUT>>(mConfigurable,
                                                                                   config);
    }

    @Nonnull
    @Override
    public ServiceRoutineBuilder<IN, OUT> setConfiguration(
            @Nonnull final InvocationConfiguration configuration) {

        super.setConfiguration(configuration);
        return this;
    }

    @Nonnull
    public ServiceConfiguration.Builder<? extends ServiceRoutineBuilder<IN, OUT>> service() {

        final ServiceConfiguration config = mServiceConfiguration;
        return new ServiceConfiguration.Builder<ServiceRoutineBuilder<IN, OUT>>(this, config);
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public ServiceRoutineBuilder<IN, OUT> setConfiguration(
            @Nonnull final ServiceConfiguration configuration) {

        if (configuration == null) {

            throw new NullPointerException("the service configuration must not be null");
        }

        mServiceConfiguration = configuration;
        return this;
    }
}
