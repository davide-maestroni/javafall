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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.dm.jrt.core.util.Reflection.asArgs;

/**
 * Invocation implementation generating outputs.
 * <p>
 * Created by davide-maestroni on 04/19/2016.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
class ThenOutputInvocation<IN, OUT> extends GenerateInvocation<IN, OUT> {

  private final List<? extends OUT> mOutputs;

  /**
   * Constructor.
   *
   * @param outputs the outputs.
   */
  ThenOutputInvocation(@Nullable final List<? extends OUT> outputs) {
    super(asArgs(outputs));
    mOutputs = outputs;
  }

  public void onComplete(@NotNull final Channel<OUT, ?> result) {
    result.pass(mOutputs);
  }

  public void onInput(final IN input, @NotNull final Channel<OUT, ?> result) {
  }
}
