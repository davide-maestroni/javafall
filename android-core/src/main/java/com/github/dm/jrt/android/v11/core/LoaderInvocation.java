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
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.SparseArray;

import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.CacheStrategyType;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.ClashResolutionType;
import com.github.dm.jrt.android.core.invocation.ContextInvocation;
import com.github.dm.jrt.android.core.invocation.ContextInvocationFactory;
import com.github.dm.jrt.android.core.invocation.InvocationClashException;
import com.github.dm.jrt.android.core.invocation.StaleResultException;
import com.github.dm.jrt.android.core.invocation.TypeClashException;
import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.common.RoutineException;
import com.github.dm.jrt.core.config.ChannelConfiguration.OrderType;
import com.github.dm.jrt.core.executor.ScheduledExecutor;
import com.github.dm.jrt.core.executor.ScheduledExecutors;
import com.github.dm.jrt.core.invocation.CallInvocation;
import com.github.dm.jrt.core.log.Logger;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.core.util.WeakIdentityHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.android.core.executor.AndroidExecutors.mainExecutor;
import static com.github.dm.jrt.core.executor.ScheduledExecutors.syncExecutor;
import static com.github.dm.jrt.core.util.DurationMeasure.indefiniteTime;

/**
 * Invocation implementation employing Loaders to perform background operations.
 * <p>
 * Created by davide-maestroni on 12/11/2014.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
@TargetApi(VERSION_CODES.HONEYCOMB)
class LoaderInvocation<IN, OUT> extends CallInvocation<IN, OUT> {

  private static final WeakIdentityHashMap<Object,
      SparseArray<WeakReference<RoutineLoaderCallbacks<?>>>>
      sCallbacks =
      new WeakIdentityHashMap<Object, SparseArray<WeakReference<RoutineLoaderCallbacks<?>>>>();

  private static final ScheduledExecutor sMainExecutor =
      ScheduledExecutors.zeroDelayExecutor(mainExecutor());

  private final CacheStrategyType mCacheStrategyType;

  private final ClashResolutionType mClashResolutionType;

  private final ContextInvocationFactory<IN, OUT> mFactory;

  private final int mLoaderId;

  private final LoaderSource mLoaderSource;

  private final Logger mLogger;

  private final ClashResolutionType mMatchResolutionType;

  private final OrderType mOrderType;

  private final long mResultStaleTimeMillis;

  /**
   * Constructor.
   *
   * @param loaderSource  the Loader source.
   * @param factory       the invocation factory.
   * @param configuration the Loader configuration.
   * @param order         the input data order.
   * @param logger        the logger instance.
   */
  LoaderInvocation(@NotNull final LoaderSource loaderSource,
      @NotNull final ContextInvocationFactory<IN, OUT> factory,
      @NotNull final LoaderConfiguration configuration, @Nullable final OrderType order,
      @NotNull final Logger logger) {
    mLoaderSource = ConstantConditions.notNull("Loader source", loaderSource);
    mFactory = ConstantConditions.notNull("Context invocation factory", factory);
    mLoaderId = configuration.getLoaderIdOrElse(LoaderConfiguration.AUTO);
    mClashResolutionType =
        configuration.getClashResolutionTypeOrElse(ClashResolutionType.ABORT_OTHER);
    mMatchResolutionType = configuration.getMatchResolutionTypeOrElse(ClashResolutionType.JOIN);
    mCacheStrategyType = configuration.getCacheStrategyTypeOrElse(CacheStrategyType.CLEAR);
    mResultStaleTimeMillis = configuration.getResultStaleTimeOrElse(indefiniteTime()).toMillis();
    mOrderType = order;
    mLogger = logger.subContextLogger(this);
  }

  /**
   * Destroys the Loader with the specified ID.
   *
   * @param loaderSource the Loader source.
   * @param loaderId     the Loader ID.
   */
  static void clearLoader(@NotNull final LoaderSource loaderSource, final int loaderId) {
    sMainExecutor.execute(new ClearCommand(loaderSource, loaderId));
  }

  /**
   * Destroys the Loader with the specified ID and the specified inputs.
   *
   * @param loaderSource the Loader source.
   * @param loaderId     the Loader ID.
   * @param inputs       the invocation inputs.
   */
  static void clearLoader(@NotNull final LoaderSource loaderSource, final int loaderId,
      @NotNull final List<?> inputs) {
    sMainExecutor.execute(new ClearInputsCommand(loaderSource, loaderId, inputs), 0,
        TimeUnit.MILLISECONDS);
  }

  /**
   * Destroys all the Loaders with the specified invocation factory.
   *
   * @param loaderSource the Loader source.
   * @param loaderId     the Loader ID.
   * @param factory      the invocation factory.
   */
  static void clearLoaders(@NotNull final LoaderSource loaderSource, final int loaderId,
      @NotNull final ContextInvocationFactory<?, ?> factory) {
    sMainExecutor.execute(new ClearFactoryCommand(loaderSource, factory, loaderId), 0,
        TimeUnit.MILLISECONDS);
  }

  /**
   * Destroys all the Loaders with the specified invocation factory and inputs.
   *
   * @param loaderSource the Loader source.
   * @param loaderId     the Loader ID.
   * @param factory      the invocation factory.
   * @param inputs       the invocation inputs.
   */
  static void clearLoaders(@NotNull final LoaderSource loaderSource, final int loaderId,
      @NotNull final ContextInvocationFactory<?, ?> factory, @NotNull final List<?> inputs) {
    sMainExecutor.execute(new ClearFactoryInputsCommand(loaderSource, factory, loaderId, inputs), 0,
        TimeUnit.MILLISECONDS);
  }

  private static void clearLoaderInternal(@NotNull final LoaderSource loaderSource,
      final int loaderId) {
    final Object component = loaderSource.getComponent();
    final WeakIdentityHashMap<Object, SparseArray<WeakReference<RoutineLoaderCallbacks<?>>>>
        callbackMap = sCallbacks;
    final SparseArray<WeakReference<RoutineLoaderCallbacks<?>>> callbackArray =
        callbackMap.prune().get(component);
    if (callbackArray == null) {
      return;
    }

    final LoaderManager loaderManager = loaderSource.getLoaderManager();
    if (loaderManager == null) {
      return;
    }

    int i = 0;
    while (i < callbackArray.size()) {
      final RoutineLoaderCallbacks<?> callbacks = callbackArray.valueAt(i).get();
      if (callbacks == null) {
        callbackArray.removeAt(i);
        continue;
      }

      final InvocationLoader<?, ?> loader = callbacks.mLoader;
      if ((loaderId == callbackArray.keyAt(i)) && (loader.getInvocationCount() == 0)) {
        loaderManager.destroyLoader(loaderId);
        callbackArray.removeAt(i);
        continue;
      }

      ++i;
    }

    if (callbackArray.size() == 0) {
      callbackMap.remove(component);
    }
  }

  private static void clearLoaderInternal(@NotNull final LoaderSource loaderSource,
      final int loaderId, @NotNull final List<?> inputs) {
    final Object component = loaderSource.getComponent();
    final WeakIdentityHashMap<Object, SparseArray<WeakReference<RoutineLoaderCallbacks<?>>>>
        callbackMap = sCallbacks;
    final SparseArray<WeakReference<RoutineLoaderCallbacks<?>>> callbackArray =
        callbackMap.prune().get(component);
    if (callbackArray == null) {
      return;
    }

    final LoaderManager loaderManager = loaderSource.getLoaderManager();
    if (loaderManager == null) {
      return;
    }

    int i = 0;
    while (i < callbackArray.size()) {
      final RoutineLoaderCallbacks<?> callbacks = callbackArray.valueAt(i).get();
      if (callbacks == null) {
        callbackArray.removeAt(i);
        continue;
      }

      @SuppressWarnings("unchecked") final InvocationLoader<Object, Object> loader =
          (InvocationLoader<Object, Object>) callbacks.mLoader;
      if ((loader.getInvocationCount() == 0) && (loaderId == callbackArray.keyAt(i))
          && loader.areSameInputs(inputs)) {
        loaderManager.destroyLoader(loaderId);
        callbackArray.removeAt(i);
        continue;
      }

      ++i;
    }

    if (callbackArray.size() == 0) {
      callbackMap.remove(component);
    }
  }

  private static void clearLoadersInternal(@NotNull final LoaderSource loaderSource,
      final int loaderId, @NotNull final ContextInvocationFactory<?, ?> factory) {
    final Object component = loaderSource.getComponent();
    final WeakIdentityHashMap<Object, SparseArray<WeakReference<RoutineLoaderCallbacks<?>>>>
        callbackMap = sCallbacks;
    final SparseArray<WeakReference<RoutineLoaderCallbacks<?>>> callbackArray =
        callbackMap.prune().get(component);
    if (callbackArray == null) {
      return;
    }

    final LoaderManager loaderManager = loaderSource.getLoaderManager();
    if (loaderManager == null) {
      return;
    }

    int i = 0;
    while (i < callbackArray.size()) {
      final RoutineLoaderCallbacks<?> callbacks = callbackArray.valueAt(i).get();
      if (callbacks == null) {
        callbackArray.removeAt(i);
        continue;
      }

      final InvocationLoader<?, ?> loader = callbacks.mLoader;
      if (loader.getInvocationFactory().equals(factory) && (loader.getInvocationCount() == 0)) {
        final int id = callbackArray.keyAt(i);
        if ((loaderId == LoaderConfiguration.AUTO) || (loaderId == id)) {
          loaderManager.destroyLoader(id);
          callbackArray.removeAt(i);
          continue;
        }
      }

      ++i;
    }

    if (callbackArray.size() == 0) {
      callbackMap.remove(component);
    }
  }

  private static void clearLoadersInternal(@NotNull final LoaderSource loaderSource,
      final int loaderId, @NotNull final ContextInvocationFactory<?, ?> factory,
      @NotNull final List<?> inputs) {
    final Object component = loaderSource.getComponent();
    final WeakIdentityHashMap<Object, SparseArray<WeakReference<RoutineLoaderCallbacks<?>>>>
        callbackMap = sCallbacks;
    final SparseArray<WeakReference<RoutineLoaderCallbacks<?>>> callbackArray =
        callbackMap.prune().get(component);
    if (callbackArray == null) {
      return;
    }

    final LoaderManager loaderManager = loaderSource.getLoaderManager();
    if (loaderManager == null) {
      return;
    }

    int i = 0;
    while (i < callbackArray.size()) {
      final RoutineLoaderCallbacks<?> callbacks = callbackArray.valueAt(i).get();
      if (callbacks == null) {
        callbackArray.removeAt(i);
        continue;
      }

      @SuppressWarnings("unchecked") final InvocationLoader<Object, Object> loader =
          (InvocationLoader<Object, Object>) callbacks.mLoader;
      if (loader.getInvocationFactory().equals(factory) && (loader.getInvocationCount() == 0)) {
        final int id = callbackArray.keyAt(i);
        if (((loaderId == LoaderConfiguration.AUTO) || (loaderId == id)) && loader.areSameInputs(
            inputs)) {
          loaderManager.destroyLoader(id);
          callbackArray.removeAt(i);
          continue;
        }
      }

      ++i;
    }

    if (callbackArray.size() == 0) {
      callbackMap.remove(component);
    }
  }

  @Override
  public void onAbort(@NotNull final RoutineException reason) throws Exception {
    super.onAbort(reason);
    final Logger logger = mLogger;
    final Context context = mLoaderSource.getLoaderContext();
    if (context == null) {
      logger.dbg("avoiding aborting invocation since Context is null");
      return;
    }

    final ContextInvocation<IN, OUT> invocation = createInvocation(mLoaderId);
    invocation.onContext(context.getApplicationContext());
    JRoutineCore.routineOn(syncExecutor())
                .withInvocation()
                .withCoreInvocations(0)
                .withLog(logger.getLog())
                .withLogLevel(logger.getLogLevel())
                .configuration()
                .ofSingleton(invocation)
                .invoke()
                .abort(reason);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void onCall(@NotNull final List<? extends IN> inputs,
      @NotNull final Channel<OUT, ?> result) throws Exception {
    final LoaderSource loaderSource = mLoaderSource;
    final Object component = loaderSource.getComponent();
    final Context context = loaderSource.getLoaderContext();
    final LoaderManager loaderManager = loaderSource.getLoaderManager();
    if ((component == null) || (context == null) || (loaderManager == null)) {
      throw new IllegalArgumentException("the routine Context has been destroyed");
    }

    final Logger logger = mLogger;
    int loaderId = mLoaderId;
    if (loaderId == LoaderConfiguration.AUTO) {
      loaderId = 31 * mFactory.hashCode() + inputs.hashCode();
      logger.dbg("generating Loader ID: %d", loaderId);
    }

    final Loader<InvocationResult<OUT>> loader = loaderManager.getLoader(loaderId);
    final ClashType clashType = getClashType(loader, loaderId, inputs);
    final WeakIdentityHashMap<Object, SparseArray<WeakReference<RoutineLoaderCallbacks<?>>>>
        callbackMap = sCallbacks;
    SparseArray<WeakReference<RoutineLoaderCallbacks<?>>> callbackArray =
        callbackMap.get(component);
    if (callbackArray == null) {
      callbackArray = new SparseArray<WeakReference<RoutineLoaderCallbacks<?>>>();
      callbackMap.put(component, callbackArray);
    }

    final WeakReference<RoutineLoaderCallbacks<?>> callbackReference = callbackArray.get(loaderId);
    RoutineLoaderCallbacks<OUT> callbacks =
        (callbackReference != null) ? (RoutineLoaderCallbacks<OUT>) callbackReference.get() : null;
    if (clashType == ClashType.ABORT_BOTH) {
      final InvocationClashException clashException = new InvocationClashException(loaderId);
      if (callbacks != null) {
        logger.dbg("resetting existing callbacks [%d]", loaderId);
        callbacks.reset(clashException);
      }

      throw clashException;
    }

    final boolean isRoutineLoader = InvocationLoader.class.isInstance(loader);
    final boolean isStaleResult =
        isRoutineLoader && ((InvocationLoader<?, OUT>) loader).isStaleResult(
            mResultStaleTimeMillis);
    final boolean isRestart = (clashType == ClashType.ABORT_OTHER) || isStaleResult;
    if ((callbacks == null) || (loader == null) || isRestart) {
      final InvocationLoader<IN, OUT> invocationLoader;
      if ((clashType == ClashType.NONE) && isRoutineLoader && !isStaleResult) {
        invocationLoader = (InvocationLoader<IN, OUT>) loader;

      } else {
        invocationLoader = null;
      }

      final RoutineLoaderCallbacks<OUT> newCallbacks =
          createCallbacks(context, loaderManager, invocationLoader, inputs, loaderId);
      if (callbacks != null) {
        logger.dbg("resetting existing callbacks [%d]", loaderId);
        callbacks.reset(
            ((clashType == ClashType.ABORT_OTHER) || !isStaleResult) ? new InvocationClashException(
                loaderId) : new StaleResultException(loaderId));
      }

      callbackArray.put(loaderId, new WeakReference<RoutineLoaderCallbacks<?>>(newCallbacks));
      callbacks = newCallbacks;
    }

    final CacheStrategyType strategyType = mCacheStrategyType;
    logger.dbg("setting result cache type [%d]: %s", loaderId, strategyType);
    callbacks.setCacheStrategy(strategyType);
    result.pass(callbacks.newChannel());
    if (isRestart) {
      logger.dbg("restarting Loader [%d]", loaderId);
      loaderManager.restartLoader(loaderId, Bundle.EMPTY, callbacks);

    } else {
      logger.dbg("initializing Loader [%d]", loaderId);
      loaderManager.initLoader(loaderId, Bundle.EMPTY, callbacks);
    }
  }

  @NotNull
  private RoutineLoaderCallbacks<OUT> createCallbacks(@NotNull final Context context,
      @NotNull final LoaderManager loaderManager, @Nullable final InvocationLoader<IN, OUT> loader,
      @NotNull final List<? extends IN> inputs, final int loaderId) throws Exception {
    final Logger logger = mLogger;
    final InvocationLoader<IN, OUT> callbacksLoader = (loader != null) ? loader
        : new InvocationLoader<IN, OUT>(context, inputs, createInvocation(loaderId), mFactory,
            mOrderType, logger);
    return new RoutineLoaderCallbacks<OUT>(loaderManager, callbacksLoader, logger);
  }

  @NotNull
  private ContextInvocation<IN, OUT> createInvocation(final int loaderId) throws Exception {
    final Logger logger = mLogger;
    try {
      logger.dbg("creating a new invocation instance [%d]", loaderId);
      return mFactory.newInvocation();

    } catch (final Exception e) {
      logger.err(e, "error creating the invocation instance [%d]", loaderId);
      throw e;
    }
  }

  @NotNull
  private ClashType getClashType(@Nullable final Loader<InvocationResult<OUT>> loader,
      final int loaderId, @NotNull final List<? extends IN> inputs) {
    if (loader == null) {
      return ClashType.NONE;
    }

    final Logger logger = mLogger;
    if (loader.getClass() != InvocationLoader.class) {
      logger.err("clashing Loader ID [%d]: %s", loaderId, loader.getClass().getName());
      throw new TypeClashException(loaderId);
    }

    final ContextInvocationFactory<IN, OUT> factory = mFactory;
    @SuppressWarnings("unchecked") final InvocationLoader<IN, OUT> invocationLoader =
        (InvocationLoader<IN, OUT>) loader;
    final ClashResolutionType resolution = (factory instanceof MissingLoaderInvocationFactory) || ((
        invocationLoader.getInvocationFactory().equals(factory) && invocationLoader.areSameInputs(
            inputs))) ? mMatchResolutionType : mClashResolutionType;
    if (resolution == ClashResolutionType.JOIN) {
      logger.dbg("joining existing invocation [%d]", loaderId);
      return ClashType.NONE;

    } else if (resolution == ClashResolutionType.ABORT_BOTH) {
      logger.dbg("aborting existing invocation [%d]", loaderId);
      return ClashType.ABORT_BOTH;

    } else if (resolution == ClashResolutionType.ABORT_OTHER) {
      logger.dbg("aborting existing invocation [%d]", loaderId);
      return ClashType.ABORT_OTHER;

    } else if (resolution == ClashResolutionType.ABORT_THIS) {
      logger.dbg("aborting invocation [%d]", loaderId);
      throw new InvocationClashException(loaderId);
    }

    return ClashType.ABORT_BOTH;
  }

  /**
   * Clash type enumeration.
   */
  private enum ClashType {

    NONE,        // no clash detected
    ABORT_OTHER, // need to abort the running Loader
    ABORT_BOTH   // need to abort both the invocation and the running Loader
  }

  /**
   * Runnable implementation purging the Loader with a specific ID.
   */
  private static class ClearCommand implements Runnable {

    private final int mLoaderId;

    private final LoaderSource mLoaderSource;

    /**
     * Constructor.
     *
     * @param loaderSource the Loader source.
     * @param loaderId     the Loader ID.
     */
    private ClearCommand(@NotNull final LoaderSource loaderSource, final int loaderId) {
      mLoaderSource = loaderSource;
      mLoaderId = loaderId;
    }

    @Override
    public void run() {
      clearLoaderInternal(mLoaderSource, mLoaderId);
    }
  }

  /**
   * Runnable implementation purging all Loaders with a specific invocation factory.
   */
  private static class ClearFactoryCommand implements Runnable {

    private final ContextInvocationFactory<?, ?> mFactory;

    private final int mLoaderId;

    private final LoaderSource mLoaderSource;

    /**
     * Constructor.
     *
     * @param loaderSource the Loader source.
     * @param factory      the invocation factory.
     * @param loaderId     the Loader ID.
     */
    private ClearFactoryCommand(@NotNull final LoaderSource loaderSource,
        @NotNull final ContextInvocationFactory<?, ?> factory, final int loaderId) {
      mLoaderSource = loaderSource;
      mFactory = factory;
      mLoaderId = loaderId;
    }

    @Override
    public void run() {
      clearLoadersInternal(mLoaderSource, mLoaderId, mFactory);
    }
  }

  /**
   * Runnable implementation purging the Loader with a specific invocation factory and inputs.
   */
  private static class ClearFactoryInputsCommand implements Runnable {

    private final ContextInvocationFactory<?, ?> mFactory;

    private final List<?> mInputs;

    private final int mLoaderId;

    private final LoaderSource mLoaderSource;

    /**
     * Constructor.
     *
     * @param loaderSource the Loader source.
     * @param factory      the invocation factory.
     * @param loaderId     the Loader ID.
     * @param inputs       the list of inputs.
     */
    private ClearFactoryInputsCommand(@NotNull final LoaderSource loaderSource,
        @NotNull final ContextInvocationFactory<?, ?> factory, final int loaderId,
        @NotNull final List<?> inputs) {
      mLoaderSource = loaderSource;
      mFactory = factory;
      mLoaderId = loaderId;
      mInputs = new ArrayList<Object>(inputs);
    }

    @Override
    public void run() {
      clearLoadersInternal(mLoaderSource, mLoaderId, mFactory, mInputs);
    }
  }

  /**
   * Runnable implementation purging the Loader with a specific ID and inputs.
   */
  private static class ClearInputsCommand implements Runnable {

    private final List<?> mInputs;

    private final int mLoaderId;

    private final LoaderSource mLoaderSource;

    /**
     * Constructor.
     *
     * @param loaderSource the Loader source.
     * @param loaderId     the Loader ID.
     * @param inputs       the list of inputs.
     */
    private ClearInputsCommand(@NotNull final LoaderSource loaderSource, final int loaderId,
        @NotNull final List<?> inputs) {
      mLoaderSource = loaderSource;
      mLoaderId = loaderId;
      mInputs = new ArrayList<Object>(inputs);
    }

    @Override
    public void run() {
      clearLoaderInternal(mLoaderSource, mLoaderId, mInputs);
    }
  }

  /**
   * Loader callbacks implementation.
   * <br>
   * The callbacks object will make sure that the Loader results are passed to the returned output
   * channels.
   *
   * @param <OUT> the output data type.
   */
  private static class RoutineLoaderCallbacks<OUT>
      implements LoaderCallbacks<InvocationResult<OUT>> {

    private final ArrayList<Channel<OUT, ?>> mAbortedChannels = new ArrayList<Channel<OUT, ?>>();

    private final ArrayList<Channel<OUT, ?>> mChannels = new ArrayList<Channel<OUT, ?>>();

    private final InvocationLoader<?, OUT> mLoader;

    private final LoaderManager mLoaderManager;

    private final Logger mLogger;

    private final ArrayList<Channel<OUT, ?>> mNewChannels = new ArrayList<Channel<OUT, ?>>();

    private CacheStrategyType mCacheStrategyType;

    private int mResultCount;

    /**
     * Constructor.
     *
     * @param loaderManager the Loader manager.
     * @param loader        the Loader instance.
     * @param logger        the logger instance.
     */
    private RoutineLoaderCallbacks(@NotNull final LoaderManager loaderManager,
        @NotNull final InvocationLoader<?, OUT> loader, @NotNull final Logger logger) {
      mLoaderManager = loaderManager;
      mLoader = loader;
      mLogger = logger.subContextLogger(this);
    }

    @Override
    public Loader<InvocationResult<OUT>> onCreateLoader(final int id, final Bundle args) {
      mLogger.dbg("creating Android Loader: %d", id);
      return mLoader;
    }

    @Override
    public void onLoadFinished(final Loader<InvocationResult<OUT>> loader,
        final InvocationResult<OUT> data) {
      final Logger logger = mLogger;
      final InvocationLoader<?, OUT> internalLoader = mLoader;
      final ArrayList<Channel<OUT, ?>> channels = mChannels;
      final ArrayList<Channel<OUT, ?>> newChannels = mNewChannels;
      final ArrayList<Channel<OUT, ?>> abortedChannels = mAbortedChannels;
      logger.dbg("dispatching invocation result: %s", data);
      if (data.passTo(newChannels, channels, abortedChannels)) {
        final ArrayList<Channel<OUT, ?>> channelsToClose = new ArrayList<Channel<OUT, ?>>(channels);
        channelsToClose.addAll(newChannels);
        mResultCount += channels.size() + newChannels.size();
        channels.clear();
        newChannels.clear();
        abortedChannels.clear();
        if (mResultCount >= internalLoader.getInvocationCount()) {
          mResultCount = 0;
          internalLoader.setInvocationCount(0);
          final CacheStrategyType strategyType = mCacheStrategyType;
          if ((strategyType == CacheStrategyType.CLEAR) || (data.isError() ? (strategyType
              == CacheStrategyType.CACHE_IF_SUCCESS)
              : (strategyType == CacheStrategyType.CACHE_IF_ERROR))) {
            final int id = internalLoader.getId();
            logger.dbg("destroying Android Loader: %d", id);
            mLoaderManager.destroyLoader(id);
          }
        }

        if (data.isError()) {
          final RoutineException exception = data.getAbortException();
          for (final Channel<OUT, ?> channel : channelsToClose) {
            channel.abort(exception);
          }

        } else {
          for (final Channel<OUT, ?> channel : channelsToClose) {
            channel.close();
          }
        }

      } else {
        mResultCount += abortedChannels.size();
        channels.addAll(newChannels);
        channels.removeAll(abortedChannels);
        newChannels.clear();
        if (channels.isEmpty() && (mResultCount >= internalLoader.getInvocationCount())) {
          data.abort();
        }
      }
    }

    @Override
    public void onLoaderReset(final Loader<InvocationResult<OUT>> loader) {
      mLogger.dbg("resetting Android Loader: %d", mLoader.getId());
      reset(new InvocationClashException(mLoader.getId()));
    }

    @NotNull
    private Channel<?, OUT> newChannel() {
      final Logger logger = mLogger;
      logger.dbg("creating new result channel");
      final InvocationLoader<?, OUT> internalLoader = mLoader;
      final ArrayList<Channel<OUT, ?>> channels = mNewChannels;
      final Channel<OUT, OUT> channel = JRoutineCore.channel()
                                                    .withChannel()
                                                    .withLog(logger.getLog())
                                                    .withLogLevel(logger.getLogLevel())
                                                    .configuration()
                                                    .ofType();
      channels.add(channel);
      internalLoader.setInvocationCount(
          Math.max(channels.size() + mAbortedChannels.size(), internalLoader.getInvocationCount()));
      return channel;
    }

    private void reset(@Nullable final Throwable reason) {
      mLogger.dbg("aborting result channels");
      mResultCount = 0;
      final ArrayList<Channel<OUT, ?>> channels = mChannels;
      final ArrayList<Channel<OUT, ?>> newChannels = mNewChannels;
      for (final Channel<OUT, ?> channel : channels) {
        channel.abort(reason);
      }

      channels.clear();
      for (final Channel<OUT, ?> newChannel : newChannels) {
        newChannel.abort(reason);
      }

      newChannels.clear();
      mAbortedChannels.clear();
    }

    private void setCacheStrategy(@NotNull final CacheStrategyType strategyType) {
      mLogger.dbg("setting cache type: %s", strategyType);
      mCacheStrategyType = strategyType;
    }
  }
}
