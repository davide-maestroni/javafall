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

package com.github.dm.jrt.channel;

import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.channel.ChannelConsumer;
import com.github.dm.jrt.core.channel.PipeChannel;
import com.github.dm.jrt.core.common.RoutineException;
import com.github.dm.jrt.core.config.ChannelConfiguration;
import com.github.dm.jrt.core.config.ChannelConfiguration.OrderType;
import com.github.dm.jrt.core.executor.ScheduledExecutor;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.core.util.DurationMeasure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Channel caching the output data and passing them to newly bound consumer, thus effectively
 * supporting binding of several channel consumers.
 * <p>
 * The {@link #isBound()} method will always return false and the bound methods will never fail.
 * <br>
 * Note, however, that the implementation will silently prevent the same consumer or channel
 * instance to be bound twice.
 * <p>
 * Created by davide-maestroni on 02/26/2016.
 *
 * @param <OUT> the output data type.
 */
class ReplayOutputChannel<OUT> implements Channel<OUT, OUT>, ChannelConsumer<OUT> {

  private final ArrayList<OUT> mCached = new ArrayList<OUT>();

  private final Channel<?, OUT> mChannel;

  private final IdentityHashMap<Channel<? super OUT, ?>, PipeChannel<OUT, OUT, ?>> mChannels =
      new IdentityHashMap<Channel<? super OUT, ?>, PipeChannel<OUT, OUT, ?>>();

  private final ChannelConfiguration mConfiguration;

  private final IdentityHashMap<ChannelConsumer<? super OUT>, Channel<OUT, OUT>> mConsumers =
      new IdentityHashMap<ChannelConsumer<? super OUT>, Channel<OUT, OUT>>();

  private final ScheduledExecutor mExecutor;

  private final Object mMutex = new Object();

  private RoutineException mAbortException;

  private boolean mIsComplete;

  private volatile Channel<OUT, OUT> mOutputChannel;

  /**
   * Constructor.
   *
   * @param executor      the executor instance.
   * @param configuration the channel configuration.
   * @param channel       the channel to replay.
   */
  ReplayOutputChannel(@NotNull final ScheduledExecutor executor,
      @NotNull final ChannelConfiguration configuration, @NotNull final Channel<?, OUT> channel) {
    mExecutor = ConstantConditions.notNull("executor instance", executor);
    mConfiguration = ConstantConditions.notNull("channel configuration", configuration);
    mOutputChannel = createOutputChannel();
    mChannel = channel;
    channel.consume((ChannelConsumer<? super OUT>) this);
  }

  @NotNull
  private static IllegalStateException illegalInput() {
    return new IllegalStateException("cannot pass data to a replay output channel");
  }

  public boolean abort() {
    return mChannel.abort();
  }

  public boolean abort(@Nullable final Throwable reason) {
    return mChannel.abort(reason);
  }

  @NotNull
  public Channel<OUT, OUT> after(final long timeout, @NotNull final TimeUnit timeUnit) {
    mOutputChannel.after(timeout, timeUnit);
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> after(@NotNull final DurationMeasure timeout) {
    mOutputChannel.after(timeout);
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> afterNoDelay() {
    mOutputChannel.afterNoDelay();
    return this;
  }

  @NotNull
  public List<OUT> all() {
    return mOutputChannel.all();
  }

  @NotNull
  public Channel<OUT, OUT> allInto(@NotNull final Collection<? super OUT> results) {
    mOutputChannel.allInto(results);
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> close() {
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> consume(@NotNull final ChannelConsumer<? super OUT> consumer) {
    final boolean isComplete;
    final RoutineException abortException;
    final Channel<OUT, OUT> outputChannel;
    final Channel<OUT, OUT> inputChannel;
    final Channel<OUT, OUT> newChannel;
    final ArrayList<OUT> cachedOutputs;
    synchronized (mMutex) {
      final IdentityHashMap<ChannelConsumer<? super OUT>, Channel<OUT, OUT>> consumers = mConsumers;
      if (consumers.containsKey(consumer)) {
        return this;
      }

      isComplete = mIsComplete;
      abortException = mAbortException;
      outputChannel = mOutputChannel;
      if ((abortException == null) && !isComplete) {
        consumers.put(consumer, outputChannel);
      }

      mOutputChannel = (newChannel = createOutputChannel());
      inputChannel = JRoutineCore.channel().ofType();
      newChannel.pass(inputChannel);
      cachedOutputs = new ArrayList<OUT>(mCached);
    }

    inputChannel.pass(cachedOutputs).close();
    if (abortException != null) {
      newChannel.abort(abortException);

    } else if (isComplete) {
      newChannel.close();
    }

    outputChannel.consume(consumer);
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> eventuallyAbort() {
    mOutputChannel.eventuallyAbort();
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> eventuallyAbort(@Nullable final Throwable reason) {
    mOutputChannel.eventuallyAbort(reason);
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> eventuallyContinue() {
    mOutputChannel.eventuallyContinue();
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> eventuallyFail() {
    mOutputChannel.eventuallyFail();
    return this;
  }

  @NotNull
  public Iterator<OUT> expiringIterator() {
    return mOutputChannel.expiringIterator();
  }

  public boolean getComplete() {
    return mOutputChannel.getComplete();
  }

  @Nullable
  public RoutineException getError() {
    return mOutputChannel.getError();
  }

  public boolean hasNext() {
    return mOutputChannel.hasNext();
  }

  public OUT next() {
    return mOutputChannel.next();
  }

  @NotNull
  public Channel<OUT, OUT> in(final long timeout, @NotNull final TimeUnit timeUnit) {
    mOutputChannel.in(timeout, timeUnit);
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> in(@NotNull final DurationMeasure timeout) {
    mOutputChannel.in(timeout);
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> inNoTime() {
    mOutputChannel.inNoTime();
    return this;
  }

  public int inputSize() {
    return mChannel.inputSize();
  }

  public boolean isBound() {
    return false;
  }

  public boolean isEmpty() {
    if (mChannel.isEmpty() && mOutputChannel.isEmpty()) {
      synchronized (mMutex) {
        return mCached.isEmpty();
      }
    }

    return false;
  }

  public boolean isOpen() {
    return false;
  }

  @NotNull
  public List<OUT> next(final int count) {
    return mOutputChannel.next(count);
  }

  public OUT nextOrElse(final OUT output) {
    return mOutputChannel.nextOrElse(output);
  }

  public int outputSize() {
    return mOutputChannel.outputSize();
  }

  @NotNull
  public Channel<OUT, OUT> pass(@Nullable final Channel<?, ? extends OUT> channel) {
    throw illegalInput();
  }

  @NotNull
  public Channel<OUT, OUT> pass(@Nullable final Iterable<? extends OUT> inputs) {
    throw illegalInput();
  }

  @NotNull
  public Channel<OUT, OUT> pass(@Nullable final OUT input) {
    throw illegalInput();
  }

  @NotNull
  public Channel<OUT, OUT> pass(@Nullable final OUT... inputs) {
    throw illegalInput();
  }

  @NotNull
  @SuppressWarnings("unchecked")
  public <AFTER> Channel<OUT, AFTER> pipe(@NotNull final Channel<? super OUT, AFTER> channel) {
    PipeChannel<OUT, OUT, ?> pipeChannel;
    synchronized (mMutex) {
      final IdentityHashMap<Channel<? super OUT, ?>, PipeChannel<OUT, OUT, ?>> channels = mChannels;
      pipeChannel = channels.get(channel);
      if (pipeChannel == null) {
        pipeChannel = new PipeChannel<OUT, OUT, AFTER>(this, channel);
        channels.put(channel, pipeChannel);
      }
    }

    return (Channel<OUT, AFTER>) pipeChannel;
  }

  public int size() {
    final int outputSize = mOutputChannel.size();
    final int size = mChannel.size() + outputSize;
    if (outputSize == 0) {
      synchronized (mMutex) {
        return size + mCached.size();
      }
    }

    return size;
  }

  @NotNull
  public Channel<OUT, OUT> skipNext(final int count) {
    mOutputChannel.skipNext(count);
    return this;
  }

  @NotNull
  public Channel<OUT, OUT> sorted() {
    return this;
  }

  public void throwError() {
    mOutputChannel.throwError();
  }

  @NotNull
  public Channel<OUT, OUT> unsorted() {
    return this;
  }

  public Iterator<OUT> iterator() {
    return mOutputChannel.iterator();
  }

  public void onComplete() {
    final ArrayList<Channel<OUT, OUT>> channels;
    synchronized (mMutex) {
      mIsComplete = true;
      channels = new ArrayList<Channel<OUT, OUT>>(mConsumers.values());
    }

    mOutputChannel.close();
    for (final Channel<OUT, OUT> channel : channels) {
      channel.close();
    }
  }

  public void onError(@NotNull final RoutineException error) {
    final ArrayList<Channel<OUT, OUT>> channels;
    synchronized (mMutex) {
      mAbortException = error;
      channels = new ArrayList<Channel<OUT, OUT>>(mConsumers.values());
    }

    mOutputChannel.abort(error);
    for (final Channel<OUT, OUT> channel : channels) {
      channel.abort(error);
    }
  }

  public void onOutput(final OUT output) {
    final ArrayList<Channel<OUT, OUT>> channels;
    synchronized (mMutex) {
      mCached.add(output);
      channels = new ArrayList<Channel<OUT, OUT>>(mConsumers.values());
    }

    mOutputChannel.pass(output);
    for (final Channel<OUT, OUT> channel : channels) {
      channel.pass(output);
    }
  }

  public void remove() {
    mOutputChannel.remove();
  }

  @NotNull
  private Channel<OUT, OUT> createOutputChannel() {
    return JRoutineCore.channelOn(mExecutor)
                       .withChannel()
                       .withPatch(mConfiguration)
                       .withOrder(OrderType.SORTED)
                       .configured()
                       .ofType();
  }
}
