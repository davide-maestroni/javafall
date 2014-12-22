/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmd.jrt.android.v4.routine;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.SparseArray;

import com.bmd.jrt.android.builder.AndroidRoutineBuilder;
import com.bmd.jrt.android.builder.AndroidRoutineBuilder.ClashResolution;
import com.bmd.jrt.android.builder.AndroidRoutineBuilder.ResultCache;
import com.bmd.jrt.android.builder.InputClashException;
import com.bmd.jrt.android.builder.RoutineClashException;
import com.bmd.jrt.channel.IOChannel;
import com.bmd.jrt.channel.IOChannel.IOChannelInput;
import com.bmd.jrt.channel.OutputChannel;
import com.bmd.jrt.channel.ResultChannel;
import com.bmd.jrt.common.CacheHashMap;
import com.bmd.jrt.common.RoutineException;
import com.bmd.jrt.common.RoutineInterruptedException;
import com.bmd.jrt.invocation.Invocation;
import com.bmd.jrt.invocation.SimpleInvocation;
import com.bmd.jrt.log.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Invocation implementation employing loaders to perform background operations.
 * <p/>
 * Created by davide on 12/11/14.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
class LoaderInvocation<INPUT, OUTPUT> extends SimpleInvocation<INPUT, OUTPUT> {

    private static final CacheHashMap<Object, SparseArray<RoutineLoaderCallbacks<?>>> sCallbackMap =
            new CacheHashMap<Object, SparseArray<RoutineLoaderCallbacks<?>>>();

    private final ResultCache mCacheType;

    private final ClashResolution mClashResolution;

    private final Constructor<? extends Invocation<INPUT, OUTPUT>> mConstructor;

    private final WeakReference<Object> mContext;

    private final int mLoaderId;

    private final Logger mLogger;

    /**
     * Constructor.
     *
     * @param context     the context reference.
     * @param loaderId    the loader ID.
     * @param resolution  the clash resolution type.
     * @param cacheType   the result cache type.
     * @param constructor the invocation constructor.
     * @param logger      the logger instance.
     * @throws NullPointerException if any of the specified parameters is null.
     */
    @SuppressWarnings("ConstantConditions")
    LoaderInvocation(@Nonnull final WeakReference<Object> context, final int loaderId,
            @Nonnull final ClashResolution resolution, @Nonnull final ResultCache cacheType,
            @Nonnull final Constructor<? extends Invocation<INPUT, OUTPUT>> constructor,
            @Nonnull final Logger logger) {

        if (context == null) {

            throw new NullPointerException("the context reference must not be null");
        }

        if (resolution == null) {

            throw new NullPointerException("the clash resolution type must not be null");
        }

        if (cacheType == null) {

            throw new NullPointerException("the result cache type must not be null");
        }

        if (constructor == null) {

            throw new NullPointerException("the invocation constructor must not be null");
        }

        mContext = context;
        mLoaderId = loaderId;
        mClashResolution = resolution;
        mCacheType = cacheType;
        mConstructor = constructor;
        mLogger = logger.subContextLogger(this);
    }

    /**
     * Enables routine invocation for the specified fragment.<br/>
     * This method must be called in the fragment <code>onCreate()</code> method.
     *
     * @param fragment the fragment instance.
     * @throws NullPointerException if the specified fragment is null.
     */
    @SuppressWarnings("ConstantConditions")
    static void initContext(@Nonnull final Fragment fragment) {

        if (fragment == null) {

            throw new NullPointerException("the fragment instance must not be null");
        }

        synchronized (sCallbackMap) {

            if (!sCallbackMap.containsKey(fragment)) {

                sCallbackMap.put(fragment, new SparseArray<RoutineLoaderCallbacks<?>>());
            }

            fragment.getLoaderManager();
        }
    }

    /**
     * Enables routine invocation for the specified activity.<br/>
     * This method must be called in the activity <code>onCreate()</code> method.
     *
     * @param activity the activity instance.
     * @throws NullPointerException if the specified activity is null.
     */
    @SuppressWarnings("ConstantConditions")
    static void initContext(@Nonnull final FragmentActivity activity) {

        if (activity == null) {

            throw new NullPointerException("the activity instance must not be null");
        }

        synchronized (sCallbackMap) {

            if (!sCallbackMap.containsKey(activity)) {

                sCallbackMap.put(activity, new SparseArray<RoutineLoaderCallbacks<?>>());
            }

            activity.getSupportLoaderManager();
        }
    }

    /**
     * Checks if the specified fragment is enabled for routine invocation.
     *
     * @param fragment the fragment instance.
     * @return whether the fragment is enabled.
     * @throws NullPointerException if the specified fragment is null.
     */
    @SuppressWarnings("ConstantConditions")
    static boolean isEnabled(@Nonnull final Fragment fragment) {

        if (fragment == null) {

            throw new NullPointerException("the fragment instance must not be null");
        }

        synchronized (sCallbackMap) {

            return sCallbackMap.containsKey(fragment);
        }
    }

    /**
     * Checks if the specified activity is enabled for routine invocation.
     *
     * @param activity the activity instance.
     * @return whether the activity is enabled.
     * @throws NullPointerException if the specified activity is null.
     */
    @SuppressWarnings("ConstantConditions")
    static boolean isEnabled(@Nonnull final FragmentActivity activity) {

        if (activity == null) {

            throw new NullPointerException("the activity instance must not be null");
        }

        synchronized (sCallbackMap) {

            return sCallbackMap.containsKey(activity);
        }
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE",
                        justification = "class comparison with == is done")
    @SuppressWarnings("unchecked")
    public void onCall(@Nonnull final List<? extends INPUT> inputs,
            @Nonnull final ResultChannel<OUTPUT> result) {

        final Logger logger = mLogger;
        final Object context = mContext.get();

        if (context == null) {

            logger.dbg("avoiding running invocation since context is null");
            return;
        }

        final Context loaderContext;
        final LoaderManager loaderManager;

        if (context instanceof FragmentActivity) {

            final FragmentActivity activity = (FragmentActivity) context;
            loaderContext = activity.getApplicationContext();
            loaderManager = activity.getSupportLoaderManager();
            logger.dbg("running invocation linked to activity: %s", activity);

        } else if (context instanceof Fragment) {

            final Fragment fragment = (Fragment) context;
            loaderContext = fragment.getActivity().getApplicationContext();
            loaderManager = fragment.getLoaderManager();
            logger.dbg("running invocation linked to fragment: %s", fragment);

        } else {

            throw new IllegalArgumentException(
                    "invalid context type: " + context.getClass().getCanonicalName());
        }

        final Constructor<? extends Invocation<INPUT, OUTPUT>> constructor = mConstructor;
        int loaderId = mLoaderId;

        if (loaderId == AndroidRoutineBuilder.GENERATED) {

            loaderId = 31 * constructor.getDeclaringClass().hashCode() + inputs.hashCode();
            logger.dbg("generating invocation ID: %d", loaderId);
        }

        boolean needRestart = true;
        final Loader<InvocationResult<OUTPUT>> loader = loaderManager.getLoader(loaderId);

        if (loader != null) {

            if (loader.getClass() != RoutineLoader.class) {

                logger.err("invocation ID clashing with loader [%d]: %s", loaderId, loader);
                throw new IllegalStateException("invalid loader with ID=" + loaderId + ": " + loader
                        .getClass()
                        .getCanonicalName());
            }

            final RoutineLoader<INPUT, OUTPUT> routineLoader =
                    (RoutineLoader<INPUT, OUTPUT>) loader;

            if (!routineLoader.isSameInvocationType(mConstructor.getDeclaringClass())) {

                logger.wrn("clashing invocation ID [%d]", loaderId);
                throw new RoutineClashException(loaderId);
            }

            final ClashResolution resolution = mClashResolution;

            if (resolution != ClashResolution.RESET) {

                if ((resolution == ClashResolution.KEEP) || routineLoader.areSameInputs(inputs)) {

                    logger.dbg("keeping existing invocation [%d]", loaderId);
                    needRestart = false;

                } else if (resolution == ClashResolution.ABORT) {

                    logger.dbg("aborting invocation invocation [%d]", loaderId);
                    throw new InputClashException(loaderId);
                }
            }
        }

        final CacheHashMap<Object, SparseArray<RoutineLoaderCallbacks<?>>> callbackMap =
                sCallbackMap;
        final SparseArray<RoutineLoaderCallbacks<?>> callbackArray = callbackMap.get(context);

        RoutineLoaderCallbacks<OUTPUT> callbacks =
                (RoutineLoaderCallbacks<OUTPUT>) callbackArray.get(loaderId);

        if ((callbacks == null) || needRestart) {

            final Invocation<INPUT, OUTPUT> invocation;

            try {

                logger.dbg("creating a new instance of class [%d]: %s", loaderId,
                           constructor.getDeclaringClass());
                invocation = constructor.newInstance();

            } catch (final InvocationTargetException e) {

                logger.err(e, "error creating the invocation instance [%d]", loaderId);
                throw new RoutineException(e.getCause());

            } catch (final RoutineInterruptedException e) {

                logger.err(e, "error creating the invocation instance");
                throw e.interrupt();

            } catch (final RoutineException e) {

                logger.err(e, "error creating the invocation instance [%d]", loaderId);
                throw e;

            } catch (final Throwable t) {

                logger.err(t, "error creating the invocation instance [%d]", loaderId);
                throw new RoutineException(t);
            }

            if (callbacks != null) {

                logger.dbg("resetting existing callbacks [%d]", loaderId);
                callbacks.reset();
            }

            final RoutineLoader<INPUT, OUTPUT> routineLoader =
                    new RoutineLoader<INPUT, OUTPUT>(loaderContext, invocation, inputs, logger);
            callbacks = new RoutineLoaderCallbacks<OUTPUT>(loaderManager, routineLoader, logger);
            callbackArray.put(loaderId, callbacks);
            needRestart = true;
        }

        logger.dbg("setting result cache type [%d]: %s", loaderId, mCacheType);
        callbacks.setCacheType(mCacheType);

        final OutputChannel<OUTPUT> outputChannel = callbacks.newChannel();

        if (needRestart) {

            logger.dbg("restarting loader [%d]", loaderId);
            loaderManager.restartLoader(loaderId, Bundle.EMPTY, callbacks);

        } else {

            logger.dbg("initializing loader [%d]", loaderId);
            loaderManager.initLoader(loaderId, Bundle.EMPTY, callbacks);
        }

        result.pass(outputChannel);
    }

    /**
     * Loader callbacks implementation.<br/>
     * The callbacks object will make sure that the loader results are passed to the output channels
     * returned.
     *
     * @param <OUTPUT> the output data type.
     */
    private static class RoutineLoaderCallbacks<OUTPUT>
            implements LoaderCallbacks<InvocationResult<OUTPUT>> {

        private final ArrayList<IOChannel<OUTPUT>> mChannels = new ArrayList<IOChannel<OUTPUT>>();

        private final RoutineLoader<?, OUTPUT> mLoader;

        private final LoaderManager mLoaderManager;

        private final Logger mLogger;

        private ResultCache mCacheType;

        private int mResultCount;

        /**
         * Constructor.
         *
         * @param loaderManager the loader manager.
         * @param loader        the loader instance.
         * @param logger        the logger instance.
         */
        private RoutineLoaderCallbacks(@Nonnull final LoaderManager loaderManager,
                @Nonnull final RoutineLoader<?, OUTPUT> loader, @Nonnull final Logger logger) {

            mLoaderManager = loaderManager;
            mLoader = loader;
            mLogger = logger.subContextLogger(this);
        }

        /**
         * Creates and returns a new output channel.<br/>
         * The channel will be used to deliver the loader results.
         *
         * @return the new output channel.
         */
        @Nonnull
        public OutputChannel<OUTPUT> newChannel() {

            final Logger logger = mLogger;
            logger.dbg("creating new result channel");

            final RoutineLoader<?, OUTPUT> internalLoader = mLoader;
            final ArrayList<IOChannel<OUTPUT>> channels = mChannels;
            final IOChannel<OUTPUT> channel = JRoutine.io()
                                                      .loggedWith(logger.getLog())
                                                      .logLevel(logger.getLogLevel())
                                                      .buildChannel();
            channels.add(channel);
            internalLoader.setInvocationCount(
                    Math.max(channels.size(), internalLoader.getInvocationCount()));
            return channel.output();
        }

        @Override
        public Loader<InvocationResult<OUTPUT>> onCreateLoader(final int id, final Bundle args) {

            mLogger.dbg("creating Android loader: %d", id);
            return mLoader;
        }

        @Override
        public void onLoadFinished(final Loader<InvocationResult<OUTPUT>> loader,
                final InvocationResult<OUTPUT> result) {

            final Logger logger = mLogger;
            final ArrayList<IOChannel<OUTPUT>> channels = mChannels;

            logger.dbg("dispatching invocation result: " + result);

            for (final IOChannel<OUTPUT> channel : channels) {

                final IOChannelInput<OUTPUT> input = channel.input();
                result.passTo(input);
                input.close();
            }

            mResultCount += channels.size();
            channels.clear();

            final RoutineLoader<?, OUTPUT> internalLoader = mLoader;

            if (mResultCount >= internalLoader.getInvocationCount()) {

                mResultCount = 0;

                final ResultCache cacheType = mCacheType;

                if ((cacheType == ResultCache.CLEAR) || (result.isError() ? (cacheType
                        == ResultCache.RETAIN_RESULT) : (cacheType == ResultCache.RETAIN_ERROR))) {

                    final int id = internalLoader.getId();
                    logger.dbg("destroying Android loader: %d", id);
                    mLoaderManager.destroyLoader(id);
                }
            }
        }

        @Override
        public void onLoaderReset(final Loader<InvocationResult<OUTPUT>> loader) {

            mLogger.dbg("resetting Android loader: %d", mLoader.getId());
            reset();
        }

        private void reset() {

            mLogger.dbg("aborting result channels");
            final ArrayList<IOChannel<OUTPUT>> channels = mChannels;

            for (final IOChannel<OUTPUT> channel : channels) {

                channel.input().abort();
            }

            channels.clear();
        }

        private void setCacheType(@Nonnull final ResultCache cacheType) {

            mLogger.dbg("setting cache type: %s", cacheType);
            mCacheType = cacheType;
        }
    }
}
