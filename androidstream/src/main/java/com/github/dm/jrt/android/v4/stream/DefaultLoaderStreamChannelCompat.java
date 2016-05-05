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

package com.github.dm.jrt.android.v4.stream;

import android.support.annotation.NonNull;

import com.github.dm.jrt.android.channel.ParcelableSelectable;
import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.CacheStrategyType;
import com.github.dm.jrt.android.core.config.LoaderConfiguration.Configurable;
import com.github.dm.jrt.android.core.invocation.ContextInvocationFactory;
import com.github.dm.jrt.android.v4.channel.SparseChannelsCompat;
import com.github.dm.jrt.android.v4.core.JRoutineLoaderCompat;
import com.github.dm.jrt.android.v4.core.JRoutineLoaderCompat.LoaderBuilderCompat;
import com.github.dm.jrt.android.v4.core.LoaderContextCompat;
import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.channel.OutputConsumer;
import com.github.dm.jrt.core.channel.ResultChannel;
import com.github.dm.jrt.core.config.ChannelConfiguration;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.config.InvocationConfiguration.OrderType;
import com.github.dm.jrt.core.error.RoutineException;
import com.github.dm.jrt.core.invocation.InvocationFactory;
import com.github.dm.jrt.core.routine.InvocationMode;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.runner.Runner;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.core.util.DeepEqualObject;
import com.github.dm.jrt.core.util.Reflection;
import com.github.dm.jrt.core.util.UnitDuration;
import com.github.dm.jrt.function.BiConsumer;
import com.github.dm.jrt.function.BiFunction;
import com.github.dm.jrt.function.Consumer;
import com.github.dm.jrt.function.Function;
import com.github.dm.jrt.function.Predicate;
import com.github.dm.jrt.function.Supplier;
import com.github.dm.jrt.function.Wrapper;
import com.github.dm.jrt.stream.AbstractStreamChannel;
import com.github.dm.jrt.stream.StreamChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.android.core.RoutineContextInvocation.factoryFrom;
import static com.github.dm.jrt.core.util.Reflection.asArgs;
import static com.github.dm.jrt.function.Functions.wrap;

/**
 * Default implementation of a loader stream output channel.
 * <p>
 * Created by davide-maestroni on 01/15/2016.
 *
 * @param <OUT> the output data type.
 */
class DefaultLoaderStreamChannelCompat<IN, OUT> extends AbstractStreamChannel<IN, OUT>
        implements LoaderStreamChannelCompat<OUT>, Configurable<LoaderStreamChannelCompat<OUT>> {

    private final InvocationConfiguration.Configurable<LoaderStreamChannelCompat<OUT>>
            mInvocationConfigurable =
            new InvocationConfiguration.Configurable<LoaderStreamChannelCompat<OUT>>() {

                @NotNull
                public LoaderStreamChannelCompat<OUT> apply(
                        @NotNull final InvocationConfiguration configuration) {

                    DefaultLoaderStreamChannelCompat.super.invocationConfiguration()
                                                          .with(null)
                                                          .with(configuration)
                                                          .apply();
                    return DefaultLoaderStreamChannelCompat.this;
                }
            };

    private final InvocationConfiguration.Configurable<LoaderStreamChannelCompat<OUT>>
            mStreamInvocationConfigurable =
            new InvocationConfiguration.Configurable<LoaderStreamChannelCompat<OUT>>() {

                @NotNull
                public LoaderStreamChannelCompat<OUT> apply(
                        @NotNull final InvocationConfiguration configuration) {

                    DefaultLoaderStreamChannelCompat.super.streamInvocationConfiguration()
                                                          .with(null)
                                                          .with(configuration)
                                                          .apply();
                    return DefaultLoaderStreamChannelCompat.this;
                }
            };

    private LoaderConfiguration mConfiguration = LoaderConfiguration.defaultConfiguration();

    private LoaderBuilderCompat mContextBuilder;

    private LoaderConfiguration mStreamConfiguration;

    private final Configurable<LoaderStreamChannelCompat<OUT>> mStreamConfigurable =
            new Configurable<LoaderStreamChannelCompat<OUT>>() {

                @NotNull
                public LoaderStreamChannelCompat<OUT> apply(
                        @NotNull final LoaderConfiguration configuration) {

                    mStreamConfiguration = configuration;
                    return DefaultLoaderStreamChannelCompat.this;
                }
            };

    /**
     * Constructor.
     *
     * @param builder       the context builder.
     * @param sourceChannel the source output channel.
     * @param invoke        the invoke function.
     */
    DefaultLoaderStreamChannelCompat(@Nullable final LoaderBuilderCompat builder,
            @NotNull final OutputChannel<IN> sourceChannel,
            @NotNull final Function<OutputChannel<IN>, OutputChannel<OUT>> invoke) {

        this(builder, InvocationConfiguration.defaultConfiguration(),
                LoaderConfiguration.defaultConfiguration(), InvocationMode.ASYNC, sourceChannel,
                invoke);
    }

    /**
     * Constructor.
     *
     * @param builder                 the context builder.
     * @param invocationConfiguration the initial invocation configuration.
     * @param loaderConfiguration     the initial loader configuration.
     * @param invocationMode          the invocation mode.
     * @param sourceChannel           the source output channel.
     * @param invoke                  the invoke function.
     */
    private DefaultLoaderStreamChannelCompat(@Nullable final LoaderBuilderCompat builder,
            @NotNull final InvocationConfiguration invocationConfiguration,
            @NotNull final LoaderConfiguration loaderConfiguration,
            @NotNull final InvocationMode invocationMode,
            @NotNull final OutputChannel<IN> sourceChannel,
            @NotNull final Function<OutputChannel<IN>, OutputChannel<OUT>> invoke) {

        super(invocationConfiguration, invocationMode, sourceChannel, invoke);
        mContextBuilder = builder;
        mStreamConfiguration =
                ConstantConditions.notNull("loader configuration", loaderConfiguration);
    }

    private static void checkStatic(@NotNull final Wrapper wrapper,
            @NotNull final Object function) {

        if (!wrapper.hasStaticScope()) {
            throw new IllegalArgumentException(
                    "the function instance does not have a static scope: " + function.getClass()
                                                                                     .getName());
        }
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> afterMax(@NotNull final UnitDuration timeout) {

        return (LoaderStreamChannelCompat<OUT>) super.afterMax(timeout);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> afterMax(final long timeout,
            @NotNull final TimeUnit timeUnit) {

        return (LoaderStreamChannelCompat<OUT>) super.afterMax(timeout, timeUnit);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> allInto(@NotNull final Collection<? super OUT> results) {

        return (LoaderStreamChannelCompat<OUT>) super.allInto(results);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> bind(
            @NotNull final OutputConsumer<? super OUT> consumer) {

        return (LoaderStreamChannelCompat<OUT>) super.bind(consumer);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> eventuallyAbort() {

        return (LoaderStreamChannelCompat<OUT>) super.eventuallyAbort();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> eventuallyAbort(@Nullable final Throwable reason) {

        return (LoaderStreamChannelCompat<OUT>) super.eventuallyAbort(reason);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> eventuallyExit() {

        return (LoaderStreamChannelCompat<OUT>) super.eventuallyExit();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> eventuallyThrow() {

        return (LoaderStreamChannelCompat<OUT>) super.eventuallyThrow();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> immediately() {

        return (LoaderStreamChannelCompat<OUT>) super.immediately();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> skipNext(final int count) {

        return (LoaderStreamChannelCompat<OUT>) super.skipNext(count);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> apply(
            @NotNull final Function<? super StreamChannel<OUT>, ? extends OutputChannel<AFTER>>
                    function) {

        return (LoaderStreamChannelCompat<AFTER>) super.apply(function);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> async() {

        return (LoaderStreamChannelCompat<OUT>) super.async();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> backPressureOn(@Nullable final Runner runner,
            final int maxInputs, final long maxDelay, @NotNull final TimeUnit timeUnit) {

        return (LoaderStreamChannelCompat<OUT>) super.backPressureOn(runner, maxInputs, maxDelay,
                timeUnit);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> backPressureOn(@Nullable final Runner runner,
            final int maxInputs, @Nullable final UnitDuration maxDelay) {

        return (LoaderStreamChannelCompat<OUT>) super.backPressureOn(runner, maxInputs, maxDelay);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> collect(
            @NotNull final BiConsumer<? super OUT, ? super OUT> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<OUT>) super.collect(consumer);
    }

    @NotNull
    @Override
    public <AFTER extends Collection<? super OUT>> LoaderStreamChannelCompat<AFTER> collect(
            @NotNull final Supplier<? extends AFTER> supplier) {

        checkStatic(wrap(supplier), supplier);
        return (LoaderStreamChannelCompat<AFTER>) super.collect(supplier);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> collect(
            @NotNull final Supplier<? extends AFTER> supplier,
            @NotNull final BiConsumer<? super AFTER, ? super OUT> consumer) {

        checkStatic(wrap(supplier), supplier);
        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<AFTER>) super.collect(supplier, consumer);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> concat(@Nullable final OUT output) {

        return (LoaderStreamChannelCompat<OUT>) super.concat(output);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> concat(@Nullable final OUT... outputs) {

        return (LoaderStreamChannelCompat<OUT>) super.concat(outputs);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> concat(@Nullable final Iterable<? extends OUT> outputs) {

        return (LoaderStreamChannelCompat<OUT>) super.concat(outputs);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> concat(
            @NotNull final OutputChannel<? extends OUT> channel) {

        return (LoaderStreamChannelCompat<OUT>) super.concat(channel);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> filter(@NotNull final Predicate<? super OUT> predicate) {

        checkStatic(wrap(predicate), predicate);
        return (LoaderStreamChannelCompat<OUT>) super.filter(predicate);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> flatMap(
            @NotNull final Function<? super OUT, ? extends OutputChannel<? extends AFTER>>
                    function) {

        checkStatic(wrap(function), function);
        return (LoaderStreamChannelCompat<AFTER>) super.flatMap(function);
    }

    @NotNull
    @Override
    public InvocationConfiguration.Builder<? extends LoaderStreamChannelCompat<OUT>>
    invocationConfiguration() {

        final InvocationConfiguration config = getConfiguration();
        return new InvocationConfiguration.Builder<LoaderStreamChannelCompat<OUT>>(
                mInvocationConfigurable, config);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> limit(final int count) {

        return (LoaderStreamChannelCompat<OUT>) super.limit(count);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> map(
            @NotNull final BiConsumer<? super OUT, ? super ResultChannel<AFTER>> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<AFTER>) super.map(consumer);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> map(
            @NotNull final Function<? super OUT, ? extends AFTER> function) {

        checkStatic(wrap(function), function);
        return (LoaderStreamChannelCompat<AFTER>) super.map(function);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> map(
            @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {

        if (!Reflection.hasStaticScope(factory)) {
            throw new IllegalArgumentException(
                    "the factory instance does not have a static scope: " + factory.getClass()
                                                                                   .getName());
        }

        return (LoaderStreamChannelCompat<AFTER>) super.map(factory);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> map(
            @NotNull final Routine<? super OUT, ? extends AFTER> routine) {

        return (LoaderStreamChannelCompat<AFTER>) super.map(routine);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> mapAll(
            @NotNull final BiConsumer<? super List<OUT>, ? super ResultChannel<AFTER>> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<AFTER>) super.mapAll(consumer);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> mapAll(
            @NotNull final Function<? super List<OUT>, ? extends AFTER> function) {

        checkStatic(wrap(function), function);
        return (LoaderStreamChannelCompat<AFTER>) super.mapAll(function);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> onError(
            @NotNull final Consumer<? super RoutineException> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<OUT>) super.onError(consumer);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<Void> onOutput(@NotNull final Consumer<? super OUT> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<Void>) super.onOutput(consumer);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> orElse(@Nullable final OUT output) {

        return (LoaderStreamChannelCompat<OUT>) super.orElse(output);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> orElse(@Nullable final OUT... outputs) {

        return (LoaderStreamChannelCompat<OUT>) super.orElse(outputs);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> orElse(@Nullable final Iterable<? extends OUT> outputs) {

        return (LoaderStreamChannelCompat<OUT>) super.orElse(outputs);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> orElseGet(final long count,
            @NotNull final Consumer<? super ResultChannel<OUT>> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<OUT>) super.orElseGet(count, consumer);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> orElseGet(
            @NotNull final Consumer<? super ResultChannel<OUT>> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<OUT>) super.orElseGet(consumer);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> orElseGet(final long count,
            @NotNull final Supplier<? extends OUT> supplier) {

        checkStatic(wrap(supplier), supplier);
        return (LoaderStreamChannelCompat<OUT>) super.orElseGet(count, supplier);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> orElseGet(
            @NotNull final Supplier<? extends OUT> supplier) {

        checkStatic(wrap(supplier), supplier);
        return (LoaderStreamChannelCompat<OUT>) super.orElseGet(supplier);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> ordered(@Nullable final OrderType orderType) {

        return (LoaderStreamChannelCompat<OUT>) super.ordered(orderType);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> parallel(final int maxInvocations) {

        return (LoaderStreamChannelCompat<OUT>) super.parallel(maxInvocations);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> parallel() {

        return (LoaderStreamChannelCompat<OUT>) super.parallel();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> peek(@NotNull final Consumer<? super OUT> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<OUT>) super.peek(consumer);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> reduce(
            @NotNull final BiFunction<? super OUT, ? super OUT, ? extends OUT> function) {

        checkStatic(wrap(function), function);
        return (LoaderStreamChannelCompat<OUT>) super.reduce(function);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> reduce(
            @NotNull final Supplier<? extends AFTER> supplier,
            @NotNull final BiFunction<? super AFTER, ? super OUT, ? extends AFTER> function) {

        checkStatic(wrap(supplier), supplier);
        checkStatic(wrap(function), function);
        return (LoaderStreamChannelCompat<AFTER>) super.reduce(supplier, function);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> repeat() {

        return (LoaderStreamChannelCompat<OUT>) super.repeat();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> runOn(@Nullable final Runner runner) {

        return (LoaderStreamChannelCompat<OUT>) super.runOn(runner);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> runOnShared() {

        return (LoaderStreamChannelCompat<OUT>) super.runOnShared();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> serial() {

        return (LoaderStreamChannelCompat<OUT>) super.serial();
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> skip(final int count) {

        return (LoaderStreamChannelCompat<OUT>) super.skip(count);
    }

    @NotNull
    @Override
    public InvocationConfiguration.Builder<? extends LoaderStreamChannelCompat<OUT>>
    streamInvocationConfiguration() {

        final InvocationConfiguration config = getStreamConfiguration();
        return new InvocationConfiguration.Builder<LoaderStreamChannelCompat<OUT>>(
                mStreamInvocationConfigurable, config);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> sync() {

        return (LoaderStreamChannelCompat<OUT>) super.sync();
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> then(@Nullable final AFTER output) {

        return (LoaderStreamChannelCompat<AFTER>) super.then(output);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> then(@Nullable final AFTER... outputs) {

        return (LoaderStreamChannelCompat<AFTER>) super.then(outputs);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> then(
            @Nullable final Iterable<? extends AFTER> outputs) {

        return (LoaderStreamChannelCompat<AFTER>) super.then(outputs);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> thenGet(final long count,
            @NotNull final Consumer<? super ResultChannel<AFTER>> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<AFTER>) super.thenGet(count, consumer);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> thenGet(
            @NotNull final Consumer<? super ResultChannel<AFTER>> consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<AFTER>) super.thenGet(consumer);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> thenGet(final long count,
            @NotNull final Supplier<? extends AFTER> supplier) {

        checkStatic(wrap(supplier), supplier);
        return (LoaderStreamChannelCompat<AFTER>) super.thenGet(count, supplier);
    }

    @NotNull
    @Override
    public <AFTER> LoaderStreamChannelCompat<AFTER> thenGet(
            @NotNull final Supplier<? extends AFTER> supplier) {

        checkStatic(wrap(supplier), supplier);
        return (LoaderStreamChannelCompat<AFTER>) super.thenGet(supplier);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<? extends ParcelableSelectable<OUT>> toSelectable(
            final int index) {

        return newChannel(getStreamConfiguration(), getInvocationMode(), getSourceChannel(),
                getInvoke().andThen(new SelectableInvoke<OUT>(buildChannelConfiguration(), index)));
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> tryCatch(
            @NotNull final BiConsumer<? super RoutineException, ? super InputChannel<OUT>>
                    consumer) {

        checkStatic(wrap(consumer), consumer);
        return (LoaderStreamChannelCompat<OUT>) super.tryCatch(consumer);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> tryCatch(
            @NotNull final Function<? super RoutineException, ? extends OUT> function) {

        checkStatic(wrap(function), function);
        return (LoaderStreamChannelCompat<OUT>) super.tryCatch(function);
    }

    @NotNull
    @Override
    public LoaderStreamChannelCompat<OUT> tryFinally(@NotNull final Runnable runnable) {

        if (!Reflection.hasStaticScope(runnable)) {
            throw new IllegalArgumentException(
                    "the runnable instance does not have a static scope: " + runnable.getClass()
                                                                                     .getName());
        }

        return (LoaderStreamChannelCompat<OUT>) super.tryFinally(runnable);
    }

    @NotNull
    @Override
    protected <BEFORE, AFTER> LoaderStreamChannelCompat<AFTER> newChannel(
            @NotNull final InvocationConfiguration streamConfiguration,
            @NotNull final InvocationMode invocationMode,
            @NotNull final OutputChannel<BEFORE> sourceChannel,
            @NotNull final Function<OutputChannel<BEFORE>, OutputChannel<AFTER>> invoke) {

        return newChannel(streamConfiguration, mStreamConfiguration, invocationMode, sourceChannel,
                invoke);
    }

    @NotNull
    @Override
    protected <AFTER> Routine<? super OUT, ? extends AFTER> newRoutine(
            @NotNull final InvocationConfiguration configuration,
            @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {

        return newRoutine(configuration, buildLoaderConfiguration(), factory);
    }

    @NotNull
    public LoaderStreamChannelCompat<OUT> cache(@Nullable final CacheStrategyType strategyType) {

        return loaderConfiguration().withCacheStrategy(strategyType).apply();
    }

    @NotNull
    public LoaderStreamChannelCompat<OUT> factoryId(final int factoryId) {

        return loaderConfiguration().withFactoryId(factoryId).apply();
    }

    @NotNull
    public LoaderConfiguration.Builder<? extends LoaderStreamChannelCompat<OUT>>
    loaderConfiguration() {

        final LoaderConfiguration config = mConfiguration;
        return new LoaderConfiguration.Builder<LoaderStreamChannelCompat<OUT>>(this, config);
    }

    @NotNull
    public LoaderStreamChannelCompat<OUT> loaderId(final int loaderId) {

        return loaderConfiguration().withLoaderId(loaderId).apply();
    }

    @NotNull
    public <AFTER> LoaderStreamChannelCompat<AFTER> map(
            @NotNull final ContextInvocationFactory<? super OUT, ? extends AFTER> factory) {

        final LoaderBuilderCompat contextBuilder = mContextBuilder;
        if (contextBuilder == null) {
            throw new IllegalStateException("the loader context is null");
        }

        return map(contextBuilder.on(factory)
                                 .invocationConfiguration()
                                 .with(buildConfiguration())
                                 .apply()
                                 .loaderConfiguration()
                                 .with(buildLoaderConfiguration())
                                 .apply()
                                 .buildRoutine());
    }

    @NotNull
    public LoaderStreamChannelCompat<OUT> staleAfter(@Nullable final UnitDuration staleTime) {

        return loaderConfiguration().withResultStaleTime(staleTime).apply();
    }

    @NotNull
    public LoaderStreamChannelCompat<OUT> staleAfter(final long time,
            @NotNull final TimeUnit timeUnit) {

        return loaderConfiguration().withResultStaleTime(time, timeUnit).apply();
    }

    @NotNull
    public LoaderConfiguration.Builder<? extends LoaderStreamChannelCompat<OUT>>
    streamLoaderConfiguration() {

        final LoaderConfiguration config = mStreamConfiguration;
        return new LoaderConfiguration.Builder<LoaderStreamChannelCompat<OUT>>(mStreamConfigurable,
                config);
    }

    @NotNull
    public LoaderStreamChannelCompat<OUT> with(@Nullable final LoaderContextCompat context) {

        mContextBuilder = (context != null) ? JRoutineLoaderCompat.with(context) : null;
        return this;
    }

    @NonNull
    private LoaderConfiguration buildLoaderConfiguration() {

        return mStreamConfiguration.builderFrom().with(mConfiguration).apply();
    }

    @NotNull
    private <BEFORE, AFTER> LoaderStreamChannelCompat<AFTER> newChannel(
            @NotNull final InvocationConfiguration invocationConfiguration,
            @NotNull final LoaderConfiguration loaderConfiguration,
            @NotNull final InvocationMode invocationMode,
            @NotNull final OutputChannel<BEFORE> sourceChannel,
            @NotNull final Function<OutputChannel<BEFORE>, OutputChannel<AFTER>> invoke) {

        return new DefaultLoaderStreamChannelCompat<BEFORE, AFTER>(mContextBuilder,
                invocationConfiguration, loaderConfiguration, invocationMode, sourceChannel,
                invoke);
    }

    @NotNull
    private <AFTER> Routine<? super OUT, ? extends AFTER> newRoutine(
            @NotNull final InvocationConfiguration invocationConfiguration,
            @NotNull final LoaderConfiguration loaderConfiguration,
            @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {

        final LoaderBuilderCompat contextBuilder = mContextBuilder;
        if (contextBuilder == null) {
            return JRoutineCore.on(factory)
                               .invocationConfiguration()
                               .with(invocationConfiguration)
                               .apply()
                               .buildRoutine();
        }

        final ContextInvocationFactory<? super OUT, ? extends AFTER> invocationFactory =
                factoryFrom(JRoutineCore.on(factory).buildRoutine(), factory.hashCode(),
                        InvocationMode.SYNC);
        return contextBuilder.on(invocationFactory)
                             .invocationConfiguration()
                             .with(invocationConfiguration)
                             .apply()
                             .loaderConfiguration()
                             .with(loaderConfiguration)
                             .apply()
                             .buildRoutine();
    }

    // TODO: 05/05/16 javadoc
    private static class SelectableInvoke<OUT> extends DeepEqualObject
            implements Function<OutputChannel<OUT>, OutputChannel<ParcelableSelectable<OUT>>> {

        private final ChannelConfiguration mConfiguration;

        private final int mIndex;

        private SelectableInvoke(@NotNull final ChannelConfiguration configuration,
                final int index) {

            super(asArgs(configuration, index));
            mConfiguration = configuration;
            mIndex = index;
        }

        @SuppressWarnings("unchecked")
        public OutputChannel<ParcelableSelectable<OUT>> apply(final OutputChannel<OUT> channel) {

            final OutputChannel<? extends ParcelableSelectable<OUT>> outputChannel =
                    SparseChannelsCompat.toSelectable(channel, mIndex)
                                        .channelConfiguration()
                                        .with(mConfiguration)
                                        .apply()
                                        .buildChannels();
            return (OutputChannel<ParcelableSelectable<OUT>>) outputChannel;
        }
    }

    @NotNull
    public LoaderStreamChannelCompat<OUT> apply(@NotNull final LoaderConfiguration configuration) {

        mConfiguration = ConstantConditions.notNull("loader configuration", configuration);
        return this;
    }
}
