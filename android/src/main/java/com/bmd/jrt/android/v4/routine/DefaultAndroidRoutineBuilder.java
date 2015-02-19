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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.bmd.jrt.android.builder.AndroidRoutineBuilder;
import com.bmd.jrt.android.invocation.AndroidInvocation;
import com.bmd.jrt.android.runner.Runners;
import com.bmd.jrt.builder.RoutineBuilder.RunnerType;
import com.bmd.jrt.builder.RoutineBuilder.TimeoutAction;
import com.bmd.jrt.builder.RoutineChannelBuilder.OrderBy;
import com.bmd.jrt.builder.RoutineConfiguration;
import com.bmd.jrt.builder.RoutineConfigurationBuilder;
import com.bmd.jrt.common.ClassToken;
import com.bmd.jrt.log.Log;
import com.bmd.jrt.log.Log.LogLevel;
import com.bmd.jrt.routine.Routine;
import com.bmd.jrt.time.TimeDuration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.bmd.jrt.common.Reflection.findConstructor;

/**
 * Default implementation of an Android routine builder.
 * <p/>
 * Created by davide on 12/9/14.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
class DefaultAndroidRoutineBuilder<INPUT, OUTPUT> implements AndroidRoutineBuilder<INPUT, OUTPUT> {

    private final RoutineConfigurationBuilder mBuilder = new RoutineConfigurationBuilder();

    private final Constructor<? extends AndroidInvocation<INPUT, OUTPUT>> mConstructor;

    private final WeakReference<Object> mContext;

    private CacheStrategy mCacheStrategy;

    private ClashResolution mClashResolution;

    private int mLoaderId = AndroidRoutineBuilder.AUTO;

    /**
     * Constructor.
     *
     * @param activity   the context activity.
     * @param classToken the invocation class token.
     * @throws java.lang.NullPointerException if the activity or class token are null.
     */
    DefaultAndroidRoutineBuilder(@Nonnull final FragmentActivity activity,
            @Nonnull final ClassToken<? extends AndroidInvocation<INPUT, OUTPUT>> classToken) {

        this((Object) activity, classToken);
    }

    /**
     * Constructor.
     *
     * @param fragment   the context fragment.
     * @param classToken the invocation class token.
     * @throws java.lang.NullPointerException if the fragment or class token are null.
     */
    DefaultAndroidRoutineBuilder(@Nonnull final Fragment fragment,
            @Nonnull final ClassToken<? extends AndroidInvocation<INPUT, OUTPUT>> classToken) {

        this((Object) fragment, classToken);
    }

    /**
     * Constructor.
     *
     * @param context    the context instance.
     * @param classToken the invocation class token.
     * @throws java.lang.NullPointerException if the context or class token are null.
     */
    @SuppressWarnings("ConstantConditions")
    private DefaultAndroidRoutineBuilder(@Nonnull final Object context,
            @Nonnull final ClassToken<? extends AndroidInvocation<INPUT, OUTPUT>> classToken) {

        if (context == null) {

            throw new NullPointerException("the routine context must not be null");
        }

        mContext = new WeakReference<Object>(context);
        mConstructor = findConstructor(classToken.getRawClass());
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> apply(
            @Nonnull final RoutineConfiguration configuration) {

        mBuilder.apply(configuration);
        return this;
    }

    @Nonnull
    @Override
    public Routine<INPUT, OUTPUT> buildRoutine() {

        final RoutineConfigurationBuilder builder =
                new RoutineConfigurationBuilder(mBuilder.buildConfiguration());
        final RoutineConfiguration configuration = builder.withRunner(Runners.mainRunner())
                                                          .withInputSize(Integer.MAX_VALUE)
                                                          .withInputTimeout(TimeDuration.INFINITY)
                                                          .withOutputSize(Integer.MAX_VALUE)
                                                          .withOutputTimeout(TimeDuration.INFINITY)
                                                          .buildConfiguration();
        return new AndroidRoutine<INPUT, OUTPUT>(configuration, mContext, mLoaderId,
                                                 mClashResolution, mCacheStrategy, mConstructor);
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> onClash(
            @Nullable final ClashResolution resolution) {

        mClashResolution = resolution;
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> onComplete(
            @Nullable final CacheStrategy cacheStrategy) {

        mCacheStrategy = cacheStrategy;
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> onReadTimeout(
            @Nullable final TimeoutAction action) {

        mBuilder.onReadTimeout(action);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withAvailableTimeout(final long timeout,
            @Nonnull final TimeUnit timeUnit) {

        mBuilder.withAvailableTimeout(timeout, timeUnit);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withAvailableTimeout(
            @Nullable final TimeDuration timeout) {

        mBuilder.withAvailableTimeout(timeout);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withCoreInvocations(final int coreInvocations) {

        mBuilder.withCoreInvocations(coreInvocations);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withId(final int id) {

        mLoaderId = id;
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withInputOrder(@Nullable final OrderBy order) {

        mBuilder.withInputOrder(order);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withLog(@Nullable final Log log) {

        mBuilder.withLog(log);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withLogLevel(@Nullable final LogLevel level) {

        mBuilder.withLogLevel(level);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withMaxInvocations(final int maxInvocations) {

        mBuilder.withMaxInvocations(maxInvocations);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withOutputOrder(@Nullable final OrderBy order) {

        mBuilder.withOutputOrder(order);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withReadTimeout(final long timeout,
            @Nonnull final TimeUnit timeUnit) {

        mBuilder.withReadTimeout(timeout, timeUnit);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withReadTimeout(
            @Nullable final TimeDuration timeout) {

        mBuilder.withReadTimeout(timeout);
        return this;
    }

    @Nonnull
    @Override
    public AndroidRoutineBuilder<INPUT, OUTPUT> withSyncRunner(@Nullable final RunnerType type) {

        mBuilder.withSyncRunner(type);
        return this;
    }
}
