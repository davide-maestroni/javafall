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

package com.github.dm.jrt.stream;

import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.config.ChannelConfiguration;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.executor.ScheduledExecutor;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.function.util.Function;
import com.github.dm.jrt.function.util.Supplier;
import com.github.dm.jrt.stream.transform.LiftingFunction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.github.dm.jrt.function.util.SupplierDecorator.wrapSupplier;

/**
 * Lifting function splitting the outputs produced by the stream, so that each subset will be
 * processed by a different routine invocation.
 * <p>
 * Created by davide-maestroni on 05/02/2017.
 *
 * @param <IN>    the input data type.
 * @param <OUT>   the output data type.
 * @param <AFTER> the new output type.
 */
class LiftSplitIn<IN, OUT, AFTER> implements LiftingFunction<IN, OUT, IN, AFTER> {

  private final ChannelConfiguration mConfiguration;

  private final int mCount;

  private final ScheduledExecutor mExecutor;

  private final Routine<? super OUT, ? extends AFTER> mRoutine;

  /**
   * Constructor.
   *
   * @param executor      the executor instance.
   * @param configuration the invocation configuration.
   * @param count         the invocation count.
   * @param routine       the routine instance.
   */
  LiftSplitIn(@NotNull final ScheduledExecutor executor,
      @NotNull final InvocationConfiguration configuration, final int count,
      @NotNull final Routine<? super OUT, ? extends AFTER> routine) {
    mExecutor = ConstantConditions.notNull("executor instance", executor);
    mCount = ConstantConditions.positive("invocation count", count);
    mRoutine = ConstantConditions.notNull("routine instance", routine);
    mConfiguration = configuration.outputConfigurationBuilder().configuration();
  }

  public Supplier<? extends Channel<IN, AFTER>> apply(
      final Supplier<? extends Channel<IN, OUT>> supplier) {
    return wrapSupplier(supplier).andThen(new Function<Channel<IN, OUT>, Channel<IN, AFTER>>() {

      public Channel<IN, AFTER> apply(final Channel<IN, OUT> channel) {
        final int count = mCount;
        @SuppressWarnings("unchecked") final Routine<OUT, AFTER> routine =
            (Routine<OUT, AFTER>) mRoutine;
        final Channel<AFTER, AFTER> outputChannel =
            JRoutineCore.channelOn(mExecutor).withConfiguration(mConfiguration).ofType();
        final ArrayList<Channel<OUT, AFTER>> channels = new ArrayList<Channel<OUT, AFTER>>(count);
        for (int i = 0; i < count; ++i) {
          final Channel<OUT, AFTER> inputChannel = (routine).invoke();
          channels.add(inputChannel);
          outputChannel.pass(inputChannel);
        }

        channel.consume(new SplitInChannelConsumer<OUT>(channels));
        return JRoutineCore.flatten(channel, outputChannel.close());
      }
    });
  }
}
