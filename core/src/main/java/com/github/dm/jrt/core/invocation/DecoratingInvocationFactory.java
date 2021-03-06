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

package com.github.dm.jrt.core.invocation;

import com.github.dm.jrt.core.util.ConstantConditions;

import org.jetbrains.annotations.NotNull;

import static com.github.dm.jrt.core.util.Reflection.asArgs;

/**
 * Class decorating the invocations produced by an invocation factory.
 * <p>
 * Created by davide-maestroni on 08/19/2015.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
@SuppressWarnings("WeakerAccess")
public abstract class DecoratingInvocationFactory<IN, OUT> extends InvocationFactory<IN, OUT> {

  private final InvocationFactory<IN, OUT> mFactory;

  /**
   * Constructor.
   *
   * @param wrapped the wrapped factory instance.
   */
  public DecoratingInvocationFactory(@NotNull final InvocationFactory<IN, OUT> wrapped) {
    super(asArgs(ConstantConditions.notNull("wrapped factory", wrapped)));
    mFactory = wrapped;
  }

  @NotNull
  @Override
  public final Invocation<IN, OUT> newInvocation() throws Exception {
    return decorate(mFactory.newInvocation());
  }

  /**
   * Decorates the specified invocation.
   *
   * @param invocation the invocation instance to decorate.
   * @return the decorated invocation.
   * @throws java.lang.Exception if an unexpected error occurs.
   */
  @NotNull
  protected abstract Invocation<IN, OUT> decorate(@NotNull Invocation<IN, OUT> invocation) throws
      Exception;
}
