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

package com.github.dm.jrt.core;

import com.github.dm.jrt.core.builder.AbstractRoutineBuilder;
import com.github.dm.jrt.core.invocation.InvocationFactory;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.util.ConstantConditions;

import org.jetbrains.annotations.NotNull;

/**
 * Class implementing a builder of routine objects based on an invocation factory.
 * <p>
 * Created by davide-maestroni on 09/21/2014.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
class DefaultRoutineBuilder<IN, OUT> extends AbstractRoutineBuilder<IN, OUT> {

  private final InvocationFactory<IN, OUT> mFactory;

  /**
   * Constructor.
   *
   * @param factory the invocation factory.
   */
  DefaultRoutineBuilder(@NotNull final InvocationFactory<IN, OUT> factory) {
    mFactory = ConstantConditions.notNull("invocation factory", factory);
  }

  @NotNull
  public Routine<IN, OUT> buildRoutine() {
    return new DefaultRoutine<IN, OUT>(getConfiguration(), mFactory);
  }
}
