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
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.function.SupplierDecorator;

import org.jetbrains.annotations.NotNull;

import static com.github.dm.jrt.core.util.Reflection.asArgs;

/**
 * Generating invocation used to call a supplier a specific number of times.
 * <p>
 * Created by davide-maestroni on 04/19/2016.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
class ThenSupplierInvocation<IN, OUT> extends GenerateInvocation<IN, OUT> {

  private final long mCount;

  private final SupplierDecorator<? extends OUT> mOutputSupplier;

  /**
   * Constructor.
   *
   * @param count          the loop count.
   * @param outputSupplier the supplier instance.
   */
  ThenSupplierInvocation(final long count,
      @NotNull final SupplierDecorator<? extends OUT> outputSupplier) {
    super(asArgs(ConstantConditions.positive("count number", count),
        ConstantConditions.notNull("supplier instance", outputSupplier)));
    mCount = count;
    mOutputSupplier = outputSupplier;
  }

  public void onComplete(@NotNull final Channel<OUT, ?> result) throws Exception {
    final long count = mCount;
    final SupplierDecorator<? extends OUT> supplier = mOutputSupplier;
    for (long i = 0; i < count; ++i) {
      result.pass(supplier.get());
    }
  }

  public void onInput(final IN input, @NotNull final Channel<OUT, ?> result) {
  }
}
