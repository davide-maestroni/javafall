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

package com.github.dm.jrt.android.v4.stream;

import android.content.Context;

import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.core.invocation.ContextInvocation;
import com.github.dm.jrt.android.core.invocation.ContextInvocationFactory;
import com.github.dm.jrt.android.stream.builder.AbstractLoaderStreamLifter;
import com.github.dm.jrt.android.stream.builder.LoaderStreamLifter;
import com.github.dm.jrt.android.v4.core.JRoutineLoaderCompat;
import com.github.dm.jrt.android.v4.core.LoaderSourceCompat;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.common.RoutineException;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.function.util.Supplier;

import org.jetbrains.annotations.NotNull;

import static com.github.dm.jrt.core.util.Reflection.asArgs;
import static com.github.dm.jrt.function.util.SupplierDecorator.wrapSupplier;

/**
 * Utility class providing transformation functions based on Loader instances.
 * <p>
 * Created by davide-maestroni on 01/30/2017.
 */
public class JRoutineLoaderStreamCompat {

  /**
   * Avoid explicit instantiation.
   */
  private JRoutineLoaderStreamCompat() {
    ConstantConditions.avoid();
  }

  /**
   * Returns a builder of functions making the stream routine run on the specified Loader.
   * <p>
   * The example below shows how it's possible to make the computation happen in a dedicated Loader:
   * <pre><code>
   * JRoutineStream.withStreamOf(routine)
   *               .lift(JRoutineLoaderStreamCompat.streamLifterOn(loaderOf(activity))
   *                                               .withLoader()
   *                                               .withInvocationId(INVOCATION_ID)
   *                                               .configuration()
   *                                               .runOnLoader())
   *               .invoke()
   *               .consume(getConsumer())
   *               .close();
   * </code></pre>
   * Note that the Loader ID, by default, will only depend on the inputs, so that, in order to avoid
   * clashing, it is advisable to explicitly set the invocation ID like shown in the example.
   *
   * @param loaderSource the Loader source.
   * @return the lifting function builder.
   */
  @NotNull
  public static LoaderStreamLifter streamLifterOn(@NotNull final LoaderSourceCompat loaderSource) {
    ConstantConditions.notNull("Loader context", loaderSource);
    return new AbstractLoaderStreamLifter() {

      @NotNull
      @Override
      protected <IN, OUT> Supplier<? extends Channel<IN, OUT>> lift(
          @NotNull final InvocationConfiguration invocationConfiguration,
          @NotNull final LoaderConfiguration loaderConfiguration,
          @NotNull final Supplier<? extends Channel<IN, OUT>> supplier) {
        return new Supplier<Channel<IN, OUT>>() {

          @Override
          public Channel<IN, OUT> get() {
            return JRoutineLoaderCompat.routineOn(loaderSource)
                                       .withConfiguration(invocationConfiguration)
                                       .withConfiguration(loaderConfiguration)
                                       .of(new LoaderContextInvocationFactory<IN, OUT>(supplier))
                                       .invoke();
          }
        };
      }
    };
  }

  /**
   * Context invocation backing a binding function.
   *
   * @param <IN>  the input data type.
   * @param <OUT> the output data type.
   */
  private static class LoaderContextInvocation<IN, OUT> implements ContextInvocation<IN, OUT> {

    private final Supplier<? extends Channel<IN, OUT>> mChannelSupplier;

    private Channel<IN, OUT> mChannel;

    /**
     * Constructor.
     *
     * @param channelSupplier the channel supplier.
     */
    private LoaderContextInvocation(
        @NotNull final Supplier<? extends Channel<IN, OUT>> channelSupplier) {
      mChannelSupplier = channelSupplier;
    }

    @Override
    public void onAbort(@NotNull final RoutineException reason) {
      mChannel.abort(reason);
    }

    @Override
    public void onComplete(@NotNull final Channel<OUT, ?> result) {
      bind(result);
      mChannel.close();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onInput(final IN input, @NotNull final Channel<OUT, ?> result) {
      bind(result);
      mChannel.pass(input);
    }

    @Override
    public boolean onRecycle() {
      mChannel = null;
      return true;
    }

    @Override
    public void onStart() throws Exception {
      mChannel = mChannelSupplier.get();
    }

    @Override
    public void onContext(@NotNull final Context context) {
    }

    private void bind(@NotNull final Channel<OUT, ?> result) {
      final Channel<IN, OUT> channel = mChannel;
      if (!channel.isBound()) {
        result.pass(channel);
      }
    }
  }

  /**
   * Factory of context invocation backing a binding function.
   *
   * @param <IN>  the input data type.
   * @param <OUT> the output data type.
   */
  private static class LoaderContextInvocationFactory<IN, OUT>
      extends ContextInvocationFactory<IN, OUT> {

    private final Supplier<? extends Channel<IN, OUT>> mChannelSupplier;

    /**
     * Constructor.
     *
     * @param channelSupplier the channel supplier.
     */
    private LoaderContextInvocationFactory(
        @NotNull final Supplier<? extends Channel<IN, OUT>> channelSupplier) {
      super(asArgs(wrapSupplier(channelSupplier)));
      mChannelSupplier = channelSupplier;
    }

    @NotNull
    @Override
    public ContextInvocation<IN, OUT> newInvocation() {
      return new LoaderContextInvocation<IN, OUT>(mChannelSupplier);
    }
  }
}
