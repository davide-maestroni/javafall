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
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.executor.ScheduledExecutor;
import com.github.dm.jrt.core.invocation.InterruptedInvocationException;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.core.util.SimpleQueue;
import com.github.dm.jrt.function.util.Supplier;
import com.github.dm.jrt.stream.ThrottleChannelConsumer.CompletionHandler;
import com.github.dm.jrt.stream.transform.LiftingFunction;

import org.jetbrains.annotations.NotNull;

/**
 * Lifting function making the stream throttle the invocation instances.
 * <p>
 * Created by davide-maestroni on 07/29/2016.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
class LiftThrottle<IN, OUT> implements LiftingFunction<IN, OUT, IN, OUT> {

  private final InvocationConfiguration mConfiguration;

  private final ScheduledExecutor mExecutor;

  private final int mMaxCount;

  private final Object mMutex = new Object();

  private final SimpleQueue<Runnable> mQueue = new SimpleQueue<Runnable>();

  private int mCount;

  /**
   * Constructor.
   *
   * @param executor      the executor instance.
   * @param configuration the channel configuration.
   * @param count         the maximum invocation count.
   */
  LiftThrottle(@NotNull final ScheduledExecutor executor,
      @NotNull final InvocationConfiguration configuration, final int count) {
    mExecutor = ConstantConditions.notNull("executor instance", executor);
    mConfiguration = ConstantConditions.notNull("invocation configuration", configuration);
    mMaxCount = ConstantConditions.positive("max count", count);
  }

  public Supplier<? extends Channel<IN, OUT>> apply(
      final Supplier<? extends Channel<IN, OUT>> supplier) {
    return new ThrottleSupplier(supplier);
  }

  /**
   * Channel supplier implementation.
   */
  private class ThrottleSupplier implements Supplier<Channel<IN, OUT>>, CompletionHandler {

    private final Supplier<? extends Channel<IN, OUT>> mChannelSupplier;

    /**
     * Constructor.
     *
     * @param channelSupplier the channel supplier.
     */
    private ThrottleSupplier(@NotNull final Supplier<? extends Channel<IN, OUT>> channelSupplier) {
      mChannelSupplier = channelSupplier;
    }

    public Channel<IN, OUT> get() throws Exception {
      final ScheduledExecutor executor = mExecutor;
      final InvocationConfiguration configuration = mConfiguration;
      final Channel<IN, IN> inputChannel = JRoutineCore.channelOn(executor)
                                                       .withConfiguration(
                                                           configuration.inputConfigurationBuilder()
                                                                        .configuration())
                                                       .ofType();
      final Channel<OUT, OUT> outputChannel = JRoutineCore.channelOn(executor)
                                                          .withConfiguration(
                                                              configuration
                                                                  .outputConfigurationBuilder()
                                                                           .configuration())
                                                          .ofType();
      final boolean isBind;
      synchronized (mMutex) {
        isBind = (++mCount <= mMaxCount);
        if (!isBind) {
          mQueue.add(new Runnable() {

            public void run() {
              try {
                mChannelSupplier.get()
                                .consume(new ThrottleChannelConsumer<OUT>(ThrottleSupplier.this,
                                    outputChannel))
                                .pass(inputChannel)
                                .close();

              } catch (final Throwable t) {
                outputChannel.abort(t);
                onComplete();
                InterruptedInvocationException.throwIfInterrupt(t);
              }
            }
          });
        }
      }

      if (isBind) {
        mChannelSupplier.get()
                        .consume(new ThrottleChannelConsumer<OUT>(this, outputChannel))
                        .pass(inputChannel)
                        .close();
      }

      return JRoutineCore.flatten(inputChannel, JRoutineCore.readOnly(outputChannel));
    }

    public void onComplete() {
      final Runnable runnable;
      synchronized (mMutex) {
        --mCount;
        final SimpleQueue<Runnable> queue = mQueue;
        if (queue.isEmpty()) {
          return;
        }

        runnable = queue.removeFirst();
      }

      runnable.run();
    }
  }
}
