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
package com.gh.bmd.jrt.core;

import com.gh.bmd.jrt.builder.TemplateRoutineBuilder;
import com.gh.bmd.jrt.invocation.InvocationFactory;
import com.gh.bmd.jrt.routine.Routine;
import com.gh.bmd.jrt.util.Reflection;

import javax.annotation.Nonnull;

/**
 * Class implementing a builder of routine objects based on an invocation factory.
 * <p/>
 * Created by davide-maestroni on 9/21/14.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
class DefaultRoutineBuilder<INPUT, OUTPUT> extends TemplateRoutineBuilder<INPUT, OUTPUT> {

    private final InvocationFactory<INPUT, OUTPUT> mFactory;

    /**
     * Constructor.
     *
     * @param factory the invocation factory.
     */
    @SuppressWarnings("ConstantConditions")
    DefaultRoutineBuilder(@Nonnull final InvocationFactory<INPUT, OUTPUT> factory) {

        final Class<? extends InvocationFactory> factoryClass = factory.getClass();

        if (!Reflection.isStaticClass(factoryClass)) {

            throw new IllegalArgumentException(
                    "the factory class must be static: " + factoryClass.getName());
        }

        mFactory = factory;
    }

    @Nonnull
    public Routine<INPUT, OUTPUT> buildRoutine() {

        return new DefaultRoutine<INPUT, OUTPUT>(getConfiguration(), mFactory);
    }
}
