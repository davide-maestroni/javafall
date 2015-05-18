/*
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
package com.gh.bmd.jrt.android.v4.core;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.gh.bmd.jrt.android.builder.LoaderConfiguration;
import com.gh.bmd.jrt.android.invocation.ContextInvocation;
import com.gh.bmd.jrt.android.invocation.ContextInvocationFactory;
import com.gh.bmd.jrt.android.routine.LoaderRoutine;
import com.gh.bmd.jrt.android.runner.Runners;
import com.gh.bmd.jrt.builder.RoutineConfiguration;
import com.gh.bmd.jrt.builder.RoutineConfiguration.OrderType;
import com.gh.bmd.jrt.common.InvocationException;
import com.gh.bmd.jrt.common.InvocationInterruptedException;
import com.gh.bmd.jrt.common.Reflection;
import com.gh.bmd.jrt.common.RoutineException;
import com.gh.bmd.jrt.core.AbstractRoutine;
import com.gh.bmd.jrt.invocation.Invocation;
import com.gh.bmd.jrt.log.Logger;
import com.gh.bmd.jrt.runner.Execution;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Routine implementation delegating to Android loaders the asynchronous processing.
 * <p/>
 * Created by davide-maestroni on 1/10/15.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
class DefaultLoaderRoutine<INPUT, OUTPUT> extends AbstractRoutine<INPUT, OUTPUT>
        implements LoaderRoutine<INPUT, OUTPUT> {

    private final Object[] mArgs;

    private final LoaderConfiguration mConfiguration;

    private final WeakReference<Object> mContext;

    private final ContextInvocationFactory<INPUT, OUTPUT> mFactory;

    private final int mLoaderId;

    private final OrderType mOrderType;

    /**
     * Constructor.
     *
     * @param context              the context reference.
     * @param factory              the invocation factory.
     * @param routineConfiguration the routine configuration.
     * @param loaderConfiguration  the loader configuration.
     * @throws java.lang.IllegalArgumentException if at least one of the parameter is invalid.
     */
    @SuppressWarnings("ConstantConditions")
    DefaultLoaderRoutine(@Nonnull final WeakReference<Object> context,
            @Nonnull final ContextInvocationFactory<INPUT, OUTPUT> factory,
            @Nonnull final RoutineConfiguration routineConfiguration,
            @Nonnull final LoaderConfiguration loaderConfiguration) {

        super(routineConfiguration);

        if (context == null) {

            throw new NullPointerException("the context must not be null");
        }

        if (factory == null) {

            throw new NullPointerException("the context invocation factory must not be null");
        }

        if (loaderConfiguration == null) {

            throw new NullPointerException("the loader configuration must not be null");
        }

        mContext = context;
        mFactory = factory;
        mConfiguration = loaderConfiguration;
        mLoaderId = loaderConfiguration.getLoaderIdOr(LoaderConfiguration.AUTO);
        mArgs = routineConfiguration.getFactoryArgsOr(Reflection.NO_ARGS);
        mOrderType = routineConfiguration.getOutputOrderTypeOr(null);
        getLogger().dbg("building context routine with invocation type %s and configuration: %s",
                        factory.getInvocationType(), loaderConfiguration);
    }

    @Override
    public void purge() {

        super.purge();
        final WeakReference<Object> context = mContext;

        if (context.get() != null) {

            Runners.mainRunner()
                   .run(new PurgeExecution(context, mFactory.getInvocationType(), mArgs, mLoaderId),
                        0, TimeUnit.MILLISECONDS);
        }
    }

    @Nonnull
    @Override
    protected Invocation<INPUT, OUTPUT> convertInvocation(final boolean async,
            @Nonnull final Invocation<INPUT, OUTPUT> invocation) {

        try {

            invocation.onDestroy();

        } catch (final InvocationInterruptedException e) {

            throw e;

        } catch (final Throwable ignored) {

            getLogger().wrn(ignored, "ignoring exception while destroying invocation instance");
        }

        return newInvocation(async);
    }

    @Nonnull
    @Override
    protected Invocation<INPUT, OUTPUT> newInvocation(final boolean async) {

        final Logger logger = getLogger();

        if (async) {

            return new LoaderInvocation<INPUT, OUTPUT>(mContext, mFactory, mArgs, mConfiguration,
                                                       mOrderType, logger);
        }

        final Object context = mContext.get();

        if (context == null) {

            throw new IllegalStateException("the routine context has been destroyed");
        }

        final Context appContext;

        if (context instanceof FragmentActivity) {

            final FragmentActivity activity = (FragmentActivity) context;
            appContext = activity.getApplicationContext();

        } else if (context instanceof Fragment) {

            final Fragment fragment = (Fragment) context;
            appContext = fragment.getActivity().getApplicationContext();

        } else {

            throw new IllegalArgumentException(
                    "invalid context type: " + context.getClass().getName());
        }

        try {

            final ContextInvocationFactory<INPUT, OUTPUT> factory = mFactory;
            logger.dbg("creating a new invocation instance of type: %s",
                       factory.getInvocationType());
            final ContextInvocation<INPUT, OUTPUT> invocation = factory.newInvocation(mArgs);
            invocation.onContext(appContext);
            return invocation;

        } catch (final RoutineException e) {

            logger.err(e, "error creating the invocation instance");
            throw e;

        } catch (final Throwable t) {

            logger.err(t, "error creating the invocation instance");
            throw new InvocationException(t);
        }
    }

    public void purge(@Nullable final INPUT input) {

        final WeakReference<Object> context = mContext;

        if (context.get() != null) {

            final List<INPUT> inputList = Collections.singletonList(input);
            final PurgeInputsExecution<INPUT> execution =
                    new PurgeInputsExecution<INPUT>(context, mFactory.getInvocationType(), mArgs,
                                                    mLoaderId, inputList);
            Runners.mainRunner().run(execution, 0, TimeUnit.MILLISECONDS);
        }
    }

    public void purge(@Nullable final INPUT... inputs) {

        final WeakReference<Object> context = mContext;

        if (context.get() != null) {

            final List<INPUT> inputList =
                    (inputs == null) ? Collections.<INPUT>emptyList() : Arrays.asList(inputs);
            final PurgeInputsExecution<INPUT> execution =
                    new PurgeInputsExecution<INPUT>(context, mFactory.getInvocationType(), mArgs,
                                                    mLoaderId, inputList);
            Runners.mainRunner().run(execution, 0, TimeUnit.MILLISECONDS);
        }
    }

    public void purge(@Nullable final Iterable<? extends INPUT> inputs) {

        final WeakReference<Object> context = mContext;

        if (context.get() != null) {

            final List<INPUT> inputList;

            if (inputs == null) {

                inputList = Collections.emptyList();

            } else {

                inputList = new ArrayList<INPUT>();

                for (final INPUT input : inputs) {

                    inputList.add(input);
                }
            }

            final PurgeInputsExecution<INPUT> execution =
                    new PurgeInputsExecution<INPUT>(context, mFactory.getInvocationType(), mArgs,
                                                    mLoaderId, inputList);
            Runners.mainRunner().run(execution, 0, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Execution implementation purging all loaders with a specific invocation class.
     */
    private static class PurgeExecution implements Execution {

        private final WeakReference<Object> mContext;

        private final Object[] mInvocationArgs;

        private final String mInvocationType;

        private final int mLoaderId;

        /**
         * Constructor.
         *
         * @param context        the context reference.
         * @param invocationType the invocation type.
         * @param invocationArgs the invocation factory arguments.
         * @param loaderId       the loader ID.
         */
        private PurgeExecution(@Nonnull final WeakReference<Object> context,
                @Nonnull final String invocationType, @Nonnull final Object[] invocationArgs,
                final int loaderId) {

            mContext = context;
            mInvocationType = invocationType;
            mInvocationArgs = invocationArgs;
            mLoaderId = loaderId;
        }

        public void run() {

            final Object context = mContext.get();

            if (context != null) {

                LoaderInvocation.purgeLoaders(context, mLoaderId, mInvocationType, mInvocationArgs);
            }
        }
    }

    /**
     * Execution implementation purging the loader with a specific invocation class and inputs.
     *
     * @param <INPUT> the input data type.
     */
    private static class PurgeInputsExecution<INPUT> implements Execution {

        private final WeakReference<Object> mContext;

        private final List<INPUT> mInputs;

        private final Object[] mInvocationArgs;

        private final String mInvocationType;

        private final int mLoaderId;

        /**
         * Constructor.
         *
         * @param context        the context reference.
         * @param invocationType the invocation type.
         * @param invocationArgs the invocation factory arguments.
         * @param loaderId       the loader ID.
         * @param inputs         the list of inputs.
         */
        private PurgeInputsExecution(@Nonnull final WeakReference<Object> context,
                @Nonnull final String invocationType, @Nonnull final Object[] invocationArgs,
                final int loaderId, @Nonnull final List<INPUT> inputs) {

            mContext = context;
            mInvocationType = invocationType;
            mInvocationArgs = invocationArgs;
            mLoaderId = loaderId;
            mInputs = inputs;
        }

        public void run() {

            final Object context = mContext.get();

            if (context != null) {

                LoaderInvocation.purgeLoader(context, mLoaderId, mInvocationType, mInvocationArgs,
                                             mInputs);
            }
        }
    }
}