/*
 * Copyright 2017 Davide Maestroni
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

package com.github.dm.jrt.function;

import com.github.dm.jrt.core.common.RoutineException;
import com.github.dm.jrt.function.builder.FunctionalChannelConsumer;
import com.github.dm.jrt.function.util.Action;
import com.github.dm.jrt.function.util.ActionDecorator;
import com.github.dm.jrt.function.util.Consumer;
import com.github.dm.jrt.function.util.ConsumerDecorator;

import org.jetbrains.annotations.NotNull;

import static com.github.dm.jrt.function.util.ActionDecorator.wrapAction;
import static com.github.dm.jrt.function.util.ConsumerDecorator.wrapConsumer;

/**
 * Utility class used to build channel consumers based on functions.
 * <p>
 * Created by davide-maestroni on 09/21/2015.
 *
 * @param <OUT> the output data type.
 */
class DefaultFunctionalChannelConsumer<OUT> implements FunctionalChannelConsumer<OUT> {

  private final ActionDecorator mOnComplete;

  private final ConsumerDecorator<RoutineException> mOnError;

  private final ConsumerDecorator<OUT> mOnOutput;

  /**
   * Constructor.
   *
   * @param onOutput   the output consumer.
   * @param onError    the error consumer.
   * @param onComplete the complete action.
   */
  @SuppressWarnings("unchecked")
  private DefaultFunctionalChannelConsumer(@NotNull final ConsumerDecorator<? super OUT> onOutput,
      @NotNull final ConsumerDecorator<? super RoutineException> onError,
      @NotNull final ActionDecorator onComplete) {
    mOnOutput = (ConsumerDecorator<OUT>) onOutput;
    mOnError = (ConsumerDecorator<RoutineException>) onError;
    mOnComplete = onComplete;
  }

  /**
   * Returns a channel consumer builder employing the specified action to handle the invocation
   * completion.
   *
   * @param onComplete the action instance.
   * @return the builder instance.
   */
  @NotNull
  static FunctionalChannelConsumer<Object> onComplete(@NotNull final Action onComplete) {
    return new DefaultFunctionalChannelConsumer<Object>(ConsumerDecorator.sink(),
        ConsumerDecorator.<RoutineException>sink(), wrapAction(onComplete));
  }

  /**
   * Returns a channel consumer builder employing the specified consumer function to handle the
   * invocation errors.
   *
   * @param onError the consumer function.
   * @return the builder instance.
   */
  @NotNull
  static FunctionalChannelConsumer<Object> onError(
      @NotNull final Consumer<? super RoutineException> onError) {
    return new DefaultFunctionalChannelConsumer<Object>(ConsumerDecorator.sink(),
        wrapConsumer(onError), ActionDecorator.noOp());
  }

  /**
   * Returns a channel consumer builder employing the specified consumer function to handle the
   * invocation outputs.
   *
   * @param onOutput the consumer function.
   * @param <OUT>    the output data type.
   * @return the builder instance.
   */
  @NotNull
  static <OUT> FunctionalChannelConsumer<OUT> onOutput(
      @NotNull final Consumer<? super OUT> onOutput) {
    return new DefaultFunctionalChannelConsumer<OUT>(wrapConsumer(onOutput),
        ConsumerDecorator.<RoutineException>sink(), ActionDecorator.noOp());
  }

  /**
   * Returns a channel consumer builder employing the specified consumer function to handle the
   * invocation outputs.
   *
   * @param onOutput the consumer function.
   * @param onError  the consumer function.
   * @param <OUT>    the output data type.
   * @return the builder instance.
   */
  @NotNull
  static <OUT> FunctionalChannelConsumer<OUT> onOutput(
      @NotNull final Consumer<? super OUT> onOutput,
      @NotNull final Consumer<? super RoutineException> onError) {
    return new DefaultFunctionalChannelConsumer<OUT>(wrapConsumer(onOutput), wrapConsumer(onError),
        ActionDecorator.noOp());
  }

  /**
   * Returns a channel consumer builder employing the specified functions to handle the invocation
   * outputs, errors adn completion.
   *
   * @param onOutput   the consumer function.
   * @param onError    the consumer function.
   * @param onComplete the action instance.
   * @param <OUT>      the output data type.
   * @return the builder instance.
   */
  @NotNull
  static <OUT> FunctionalChannelConsumer<OUT> onOutput(
      @NotNull final Consumer<? super OUT> onOutput,
      @NotNull final Consumer<? super RoutineException> onError, @NotNull final Action onComplete) {
    return new DefaultFunctionalChannelConsumer<OUT>(wrapConsumer(onOutput), wrapConsumer(onError),
        wrapAction(onComplete));
  }

  @NotNull
  public FunctionalChannelConsumer<OUT> andOnComplete(@NotNull final Action onComplete) {
    return new DefaultFunctionalChannelConsumer<OUT>(mOnOutput, mOnError,
        mOnComplete.andThen(onComplete));
  }

  @NotNull
  public FunctionalChannelConsumer<OUT> andOnError(
      @NotNull final Consumer<? super RoutineException> onError) {
    return new DefaultFunctionalChannelConsumer<OUT>(mOnOutput, mOnError.andThen(onError),
        mOnComplete);
  }

  @NotNull
  public FunctionalChannelConsumer<OUT> andOnOutput(@NotNull final Consumer<? super OUT> onOutput) {
    return new DefaultFunctionalChannelConsumer<OUT>(mOnOutput.andThen(onOutput), mOnError,
        mOnComplete);
  }

  public void onComplete() throws Exception {
    mOnComplete.perform();
  }

  public void onError(@NotNull final RoutineException error) throws Exception {
    mOnError.accept(error);
  }

  public void onOutput(final OUT output) throws Exception {
    mOnOutput.accept(output);
  }
}