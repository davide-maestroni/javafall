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

package com.github.dm.jrt.android.v11.core;

import android.annotation.TargetApi;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Build.VERSION_CODES;

import com.github.dm.jrt.android.core.invocation.ContextInvocation;
import com.github.dm.jrt.android.core.invocation.ContextInvocationFactory;
import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.config.ChannelConfiguration.OrderType;
import com.github.dm.jrt.core.invocation.InvocationException;
import com.github.dm.jrt.core.log.Logger;
import com.github.dm.jrt.core.util.ConstantConditions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.dm.jrt.core.executor.ScheduledExecutors.syncExecutor;

/**
 * Loader implementation performing the routine invocation.
 * <p>
 * Created by davide-maestroni on 12/08/2014.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
@TargetApi(VERSION_CODES.HONEYCOMB)
class InvocationLoader<IN, OUT> extends AsyncTaskLoader<InvocationResult<OUT>> {

  private final ContextInvocationFactory<IN, OUT> mFactory;

  private final List<? extends IN> mInputs;

  private final ContextInvocation<IN, OUT> mInvocation;

  private final Logger mLogger;

  private final OrderType mOrderType;

  private int mInvocationCount;

  private InvocationResult<OUT> mResult;

  /**
   * Constructor.
   *
   * @param context    used to retrieve the application Context.
   * @param inputs     the input data.
   * @param invocation the invocation instance.
   * @param factory    the invocation factory.
   * @param order      the data order.
   * @param logger     the logger instance.
   */
  InvocationLoader(@NotNull final Context context, @NotNull final List<? extends IN> inputs,
      @NotNull final ContextInvocation<IN, OUT> invocation,
      @NotNull final ContextInvocationFactory<IN, OUT> factory, @Nullable final OrderType order,
      @NotNull final Logger logger) {
    super(context);
    mInputs = ConstantConditions.notNull("list of input data", inputs);
    mInvocation = ConstantConditions.notNull("invocation instance", invocation);
    mFactory = ConstantConditions.notNull("Context invocation factory", factory);
    mOrderType = order;
    mLogger = logger.subContextLogger(this);
  }

  @Override
  public void deliverResult(final InvocationResult<OUT> data) {
    mLogger.dbg("delivering result: %s", data);
    mResult = data;
    super.deliverResult(data);
  }

  @Override
  protected void onStartLoading() {
    super.onStartLoading();
    final Logger logger = mLogger;
    logger.dbg("start background invocation");
    final InvocationResult<OUT> result = mResult;
    if (takeContentChanged() || (result == null)) {
      forceLoad();

    } else {
      logger.dbg("re-delivering result: %s", result);
      super.deliverResult(result);
    }
  }

  @Override
  protected void onReset() {
    mLogger.dbg("resetting result");
    mResult = null;
    super.onReset();
  }

  @Override
  public InvocationResult<OUT> loadInBackground() {
    final Logger logger = mLogger;
    final InvocationChannelConsumer<OUT> consumer =
        new InvocationChannelConsumer<OUT>(this, logger);
    final ContextInvocation<IN, OUT> invocation = mInvocation;
    try {
      invocation.onContext(getContext());
      JRoutineCore.routineOn(syncExecutor())
                  .withInvocation()
                  .withCoreInvocations(0)
                  .withOutputOrder(mOrderType)
                  .withLog(logger.getLog())
                  .withLogLevel(logger.getLogLevel())
                  .configuration()
                  .ofSingleton(invocation)
                  .invoke()
                  .consume(consumer)
                  .pass(mInputs)
                  .close();

    } catch (final Exception e) {
      logger.err("failed to initialize invocation Context", e);
      consumer.onError(InvocationException.wrapIfNeeded(e));
    }

    return consumer.createResult();
  }

  /**
   * Checks if the Loader inputs are equal to the specified ones.
   *
   * @param inputs the input data.
   * @return whether the inputs are equal.
   */
  boolean areSameInputs(@Nullable final List<? extends IN> inputs) {
    return mInputs.equals(inputs);
  }

  /**
   * Gets this Loader invocation count.
   *
   * @return the invocation count.
   */
  int getInvocationCount() {
    return mInvocationCount;
  }

  /**
   * Sets the invocation count.
   *
   * @param count the invocation count.
   */
  void setInvocationCount(final int count) {
    mInvocationCount = count;
  }

  /**
   * Returns the invocation factory.
   *
   * @return the factory.
   */
  @NotNull
  ContextInvocationFactory<IN, OUT> getInvocationFactory() {
    return mFactory;
  }

  /**
   * Checks if the last result has been delivered more than the specified milliseconds in the past.
   *
   * @param staleTimeMillis the stale time in milliseconds.
   * @return whether the result is stale.
   */
  boolean isStaleResult(final long staleTimeMillis) {
    final InvocationResult<OUT> result = mResult;
    return (result != null) && ((System.currentTimeMillis() - result.getResultTimestamp())
        > staleTimeMillis);
  }
}
