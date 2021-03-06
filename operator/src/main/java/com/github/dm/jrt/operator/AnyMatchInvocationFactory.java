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

package com.github.dm.jrt.operator;

import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.invocation.Invocation;
import com.github.dm.jrt.core.invocation.InvocationFactory;
import com.github.dm.jrt.core.invocation.TemplateInvocation;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.function.PredicateDecorator;

import org.jetbrains.annotations.NotNull;

import static com.github.dm.jrt.core.util.Reflection.asArgs;

/**
 * Factory of invocations verifying that any of the inputs satisfies a specific conditions.
 * <p>
 * Created by davide-maestroni on 05/02/2016.
 *
 * @param <IN> the input data type.
 */
class AnyMatchInvocationFactory<IN> extends InvocationFactory<IN, Boolean> {

  private final PredicateDecorator<? super IN> mFilterPredicate;

  /**
   * Constructor.
   *
   * @param filterPredicate the predicate defining the condition.
   */
  AnyMatchInvocationFactory(@NotNull final PredicateDecorator<? super IN> filterPredicate) {
    super(asArgs(ConstantConditions.notNull("predicate instance", filterPredicate)));
    mFilterPredicate = filterPredicate;
  }

  @NotNull
  @Override
  public Invocation<IN, Boolean> newInvocation() {
    return new AnyMatchInvocation<IN>(mFilterPredicate);
  }

  /**
   * Invocation verifying that any of the inputs satisfies a specific conditions.
   *
   * @param <IN> the input data type.
   */
  private static class AnyMatchInvocation<IN> extends TemplateInvocation<IN, Boolean> {

    private final PredicateDecorator<? super IN> mFilterPredicate;

    private boolean mIsMatch;

    /**
     * Constructor.
     *
     * @param filterPredicate the predicate defining the condition.
     */
    private AnyMatchInvocation(@NotNull final PredicateDecorator<? super IN> filterPredicate) {
      mFilterPredicate = filterPredicate;
    }

    @Override
    public void onComplete(@NotNull final Channel<Boolean, ?> result) {
      if (!mIsMatch) {
        result.pass(false);
      }
    }

    @Override
    public void onInput(final IN input, @NotNull final Channel<Boolean, ?> result) throws
        Exception {
      if (!mIsMatch && mFilterPredicate.test(input)) {
        mIsMatch = true;
        result.pass(true);
      }
    }

    @Override
    public void onRestart() {
      mIsMatch = false;
    }
  }
}
