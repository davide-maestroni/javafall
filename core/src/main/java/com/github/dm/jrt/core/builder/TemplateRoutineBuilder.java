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

package com.github.dm.jrt.core.builder;

import com.github.dm.jrt.core.channel.InvocationChannel;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.config.InvocationConfiguration.Builder;
import com.github.dm.jrt.core.config.InvocationConfiguration.Configurable;
import com.github.dm.jrt.core.routine.TemplateRoutine;
import com.github.dm.jrt.core.util.ConstantConditions;

import org.jetbrains.annotations.NotNull;

/**
 * Empty abstract implementation of a routine builder.
 * <p>
 * This class is useful to avoid the need of implementing some of the methods defined in the
 * interface.
 * <p>
 * Created by davide-maestroni on 03/16/2015.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
public abstract class TemplateRoutineBuilder<IN, OUT> extends TemplateRoutine<IN, OUT>
        implements RoutineBuilder<IN, OUT>, Configurable<RoutineBuilder<IN, OUT>> {

    private InvocationConfiguration mConfiguration = InvocationConfiguration.defaultConfiguration();

    @NotNull
    public InvocationChannel<IN, OUT> asyncInvoke() {

        return buildRoutine().asyncInvoke();
    }

    @NotNull
    public InvocationChannel<IN, OUT> parallelInvoke() {

        return buildRoutine().parallelInvoke();
    }

    @NotNull
    public InvocationChannel<IN, OUT> serialInvoke() {

        return buildRoutine().serialInvoke();
    }

    @NotNull
    public InvocationChannel<IN, OUT> syncInvoke() {

        return buildRoutine().syncInvoke();
    }

    @NotNull
    public Builder<? extends RoutineBuilder<IN, OUT>> getInvocationConfiguration() {

        return new Builder<RoutineBuilder<IN, OUT>>(this, mConfiguration);
    }

    @NotNull
    public RoutineBuilder<IN, OUT> setConfiguration(
            @NotNull final InvocationConfiguration configuration) {

        mConfiguration = ConstantConditions.notNull("invocation configuration", configuration);
        return this;
    }

    /**
     * Returns the builder invocation configuration.
     *
     * @return the invocation configuration.
     */
    @NotNull
    protected InvocationConfiguration getConfiguration() {

        return mConfiguration;
    }
}