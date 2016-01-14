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
package com.github.dm.jrt.stream;

import com.github.dm.jrt.builder.InvocationConfiguration;
import com.github.dm.jrt.builder.InvocationConfiguration.Builder;
import com.github.dm.jrt.builder.InvocationConfiguration.Configurable;
import com.github.dm.jrt.builder.InvocationConfiguration.OrderType;
import com.github.dm.jrt.channel.IOChannel;
import com.github.dm.jrt.channel.InvocationChannel;
import com.github.dm.jrt.channel.OutputConsumer;
import com.github.dm.jrt.channel.ResultChannel;
import com.github.dm.jrt.channel.RoutineException;
import com.github.dm.jrt.core.JRoutine;
import com.github.dm.jrt.function.BiConsumer;
import com.github.dm.jrt.function.BiFunction;
import com.github.dm.jrt.function.Consumer;
import com.github.dm.jrt.function.ConsumerWrapper;
import com.github.dm.jrt.function.Function;
import com.github.dm.jrt.function.FunctionWrapper;
import com.github.dm.jrt.function.Functions;
import com.github.dm.jrt.function.Predicate;
import com.github.dm.jrt.function.Supplier;
import com.github.dm.jrt.function.SupplierWrapper;
import com.github.dm.jrt.invocation.FilterInvocation;
import com.github.dm.jrt.invocation.Invocation;
import com.github.dm.jrt.invocation.InvocationFactory;
import com.github.dm.jrt.routine.Routine;
import com.github.dm.jrt.runner.Runner;
import com.github.dm.jrt.util.TimeDuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.function.Functions.consumerFactory;
import static com.github.dm.jrt.function.Functions.consumerFilter;
import static com.github.dm.jrt.function.Functions.functionFactory;
import static com.github.dm.jrt.function.Functions.functionFilter;
import static com.github.dm.jrt.function.Functions.predicateFilter;
import static com.github.dm.jrt.function.Functions.wrapConsumer;
import static com.github.dm.jrt.function.Functions.wrapFunction;
import static com.github.dm.jrt.function.Functions.wrapSupplier;
import static com.github.dm.jrt.util.TimeDuration.fromUnit;

/**
 * Abstract implementation of a stream output channel.
 * <p/>
 * This class provides a default implementation of all the stream channel features. The inheriting
 * class just needs to create routine and channel instances when required.
 * <p/>
 * Created by davide-maestroni on 01/12/2016.
 *
 * @param <OUT> the output data type.
 */
public abstract class AbstractStreamOutputChannel<OUT>
        implements StreamOutputChannel<OUT>, Configurable<StreamOutputChannel<OUT>> {

    private final OutputChannel<OUT> mChannel;

    private InvocationConfiguration mConfiguration = InvocationConfiguration.DEFAULT_CONFIGURATION;

    private InvocationConfiguration mStreamConfiguration =
            InvocationConfiguration.DEFAULT_CONFIGURATION;

    private final Configurable<StreamOutputChannel<OUT>> mStreamConfigurable =
            new Configurable<StreamOutputChannel<OUT>>() {

                @NotNull
                public StreamOutputChannel<OUT> setConfiguration(
                        @NotNull final InvocationConfiguration configuration) {

                    mStreamConfiguration = configuration;
                    return AbstractStreamOutputChannel.this;
                }
            };

    /**
     * Constructor.
     *
     * @param configuration the initial invocation configuration.
     * @param channel       the wrapped output channel.
     */
    @SuppressWarnings("ConstantConditions")
    protected AbstractStreamOutputChannel(@NotNull final InvocationConfiguration configuration,
            @NotNull final OutputChannel<OUT> channel) {

        if (configuration == null) {

            throw new NullPointerException("the configuration must not be null");
        }

        if (channel == null) {

            throw new NullPointerException("the output channel instance must not be null");
        }

        mStreamConfiguration = configuration;
        mChannel = channel;
    }

    @NotNull
    private static <IN> RangeInvocation<IN, ? extends Number> range(@NotNull final Number start,
            @NotNull final Number end) {

        if ((start instanceof Double) || (end instanceof Double)) {

            final double startValue = start.doubleValue();
            final double endValue = end.doubleValue();
            return range(start, end, (startValue <= endValue) ? 1 : -1);

        } else if ((start instanceof Float) || (end instanceof Float)) {

            final float startValue = start.floatValue();
            final float endValue = end.floatValue();
            return range(start, end, (startValue <= endValue) ? 1 : -1);

        } else if ((start instanceof Long) || (end instanceof Long)) {

            final long startValue = start.longValue();
            final long endValue = end.longValue();
            return range(start, end, (startValue <= endValue) ? 1 : -1);

        } else if ((start instanceof Integer) || (end instanceof Integer)) {

            final int startValue = start.intValue();
            final int endValue = end.intValue();
            return range(start, end, (startValue <= endValue) ? 1 : -1);

        } else if ((start instanceof Short) || (end instanceof Short)) {

            final short startValue = start.shortValue();
            final short endValue = end.shortValue();
            return range(start, end, (short) ((startValue <= endValue) ? 1 : -1));

        } else if ((start instanceof Byte) || (end instanceof Byte)) {

            final byte startValue = start.byteValue();
            final byte endValue = end.byteValue();
            return range(start, end, (byte) ((startValue <= endValue) ? 1 : -1));
        }

        throw new IllegalArgumentException(
                "unsupported Number class: [" + start.getClass().getCanonicalName() + ", "
                        + end.getClass().getCanonicalName() + "]");
    }

    @NotNull
    private static <IN> RangeInvocation<IN, ? extends Number> range(@NotNull final Number start,
            @NotNull final Number end, @NotNull final Number increment) {

        if ((start instanceof Double) || (end instanceof Double) || (increment instanceof Double)) {

            final double startValue = start.doubleValue();
            final double endValue = end.doubleValue();
            final double incValue = increment.doubleValue();
            final Function<Double, Double> function = new Function<Double, Double>() {

                public Double apply(final Double aDouble) {

                    return aDouble + incValue;
                }
            };

            return new RangeInvocation<IN, Double>(startValue, endValue, wrapFunction(function));

        } else if ((start instanceof Float) || (end instanceof Float)
                || (increment instanceof Float)) {

            final float startValue = start.floatValue();
            final float endValue = end.floatValue();
            final float incValue = increment.floatValue();
            final Function<Float, Float> function = new Function<Float, Float>() {

                public Float apply(final Float aFloat) {

                    return aFloat + incValue;
                }
            };

            return new RangeInvocation<IN, Float>(startValue, endValue, wrapFunction(function));

        } else if ((start instanceof Long) || (end instanceof Long)
                || (increment instanceof Long)) {

            final long startValue = start.longValue();
            final long endValue = end.longValue();
            final long incValue = increment.longValue();
            final Function<Long, Long> function = new Function<Long, Long>() {

                public Long apply(final Long aLong) {

                    return aLong + incValue;
                }
            };

            return new RangeInvocation<IN, Long>(startValue, endValue, wrapFunction(function));

        } else if ((start instanceof Integer) || (end instanceof Integer)
                || (increment instanceof Integer)) {

            final int startValue = start.intValue();
            final int endValue = end.intValue();
            final int incValue = increment.intValue();
            final Function<Integer, Integer> function = new Function<Integer, Integer>() {

                public Integer apply(final Integer anInteger) {

                    return anInteger + incValue;
                }
            };

            return new RangeInvocation<IN, Integer>(startValue, endValue, wrapFunction(function));

        } else if ((start instanceof Short) || (end instanceof Short)
                || (increment instanceof Short)) {

            final short startValue = start.shortValue();
            final short endValue = end.shortValue();
            final short incValue = increment.shortValue();
            final Function<Short, Short> function = new Function<Short, Short>() {

                public Short apply(final Short aShort) {

                    return (short) (aShort + incValue);
                }
            };

            return new RangeInvocation<IN, Short>(startValue, endValue, wrapFunction(function));

        } else if ((start instanceof Byte) || (end instanceof Byte)
                || (increment instanceof Byte)) {

            final byte startValue = start.byteValue();
            final byte endValue = end.byteValue();
            final byte incValue = increment.byteValue();
            final Function<Byte, Byte> function = new Function<Byte, Byte>() {

                public Byte apply(final Byte aByte) {

                    return (byte) (aByte + incValue);
                }
            };

            return new RangeInvocation<IN, Byte>(startValue, endValue, wrapFunction(function));
        }

        throw new IllegalArgumentException(
                "unsupported Number class: [" + start.getClass().getCanonicalName() + ", "
                        + end.getClass().getCanonicalName() + ", " + increment.getClass()
                                                                              .getCanonicalName()
                        + "]");
    }

    public boolean abort() {

        return mChannel.abort();
    }

    public boolean abort(@Nullable final Throwable reason) {

        return mChannel.abort(reason);
    }

    public boolean isEmpty() {

        return mChannel.isEmpty();
    }

    public boolean isOpen() {

        return mChannel.isOpen();
    }

    @NotNull
    public StreamOutputChannel<OUT> afterMax(@NotNull final TimeDuration timeout) {

        mChannel.afterMax(timeout);
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> afterMax(final long timeout, @NotNull final TimeUnit timeUnit) {

        mChannel.afterMax(timeout, timeUnit);
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> allInto(@NotNull final Collection<? super OUT> results) {

        mChannel.allInto(results);
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> eventuallyAbort() {

        mChannel.eventuallyAbort();
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> eventuallyAbort(@Nullable final Throwable reason) {

        mChannel.eventuallyAbort(reason);
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> eventuallyExit() {

        mChannel.eventuallyExit();
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> eventuallyThrow() {

        mChannel.eventuallyThrow();
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> immediately() {

        mChannel.immediately();
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> passTo(@NotNull final OutputConsumer<? super OUT> consumer) {

        mChannel.passTo(consumer);
        return this;
    }

    @NotNull
    public StreamOutputChannel<OUT> skip(final int count) {

        mChannel.skip(count);
        return this;
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncCollect(
            @NotNull final BiConsumer<? super List<? extends OUT>, ? super ResultChannel<AFTER>>
                    consumer) {

        return asyncMap(consumerFactory(consumer));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncCollect(
            @NotNull final Function<? super List<? extends OUT>, ? extends AFTER> function) {

        return asyncMap(functionFactory(function));
    }

    @NotNull
    public StreamOutputChannel<OUT> asyncFilter(@NotNull final Predicate<? super OUT> predicate) {

        return asyncMap(predicateFilter(predicate));
    }

    @NotNull
    public StreamOutputChannel<Void> asyncForEach(@NotNull final Consumer<? super OUT> consumer) {

        return asyncMap(new ConsumerInvocation<OUT>(wrapConsumer(consumer)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncGenerate(
            @NotNull final Consumer<? super ResultChannel<AFTER>> consumer) {

        return asyncMap(new GenerateConsumerInvocation<OUT, AFTER>(wrapConsumer(consumer)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncGenerate(final long count,
            @NotNull final Supplier<? extends AFTER> supplier) {

        return asyncMap(new GenerateSupplierInvocation<OUT, AFTER>(count, wrapSupplier(supplier)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncGenerate(
            @NotNull final Supplier<? extends AFTER> supplier) {

        return asyncGenerate(1, supplier);
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncLift(
            @NotNull final Function<? super OUT, ? extends OutputChannel<? extends AFTER>>
                    function) {

        return asyncMap(new LiftInvocation<OUT, AFTER>(wrapFunction(function)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncMap(
            @NotNull final BiConsumer<? super OUT, ? super ResultChannel<AFTER>> consumer) {

        return asyncMap(consumerFilter(consumer));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncMap(
            @NotNull final Function<? super OUT, ? extends AFTER> function) {

        return asyncMap(functionFilter(function));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncMap(
            @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {

        return asyncMap(buildRoutine(factory));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> asyncMap(
            @NotNull final Routine<? super OUT, ? extends AFTER> routine) {

        return concatRoutine(routine.asyncInvoke());
    }

    @NotNull
    public <AFTER extends Comparable<AFTER>> StreamOutputChannel<AFTER> asyncRange(
            @NotNull final AFTER start, @NotNull final AFTER end,
            @NotNull final Function<AFTER, AFTER> increment) {

        return asyncMap(new RangeInvocation<OUT, AFTER>(start, end, wrapFunction(increment)));
    }

    @NotNull
    public StreamOutputChannel<Number> asyncRange(@NotNull final Number start,
            @NotNull final Number end) {

        return this.<Number>asyncMap(range(start, end));
    }

    @NotNull
    public StreamOutputChannel<Number> asyncRange(@NotNull final Number start,
            @NotNull final Number end, @NotNull final Number increment) {

        return this.<Number>asyncMap(range(start, end, increment));
    }

    @NotNull
    public StreamOutputChannel<OUT> asyncReduce(
            @NotNull final BiFunction<? super OUT, ? super OUT, ? extends OUT> function) {

        return asyncMap(AccumulateInvocation.functionFactory(function));
    }

    @NotNull
    public StreamOutputChannel<OUT> backPressureOn(@Nullable final Runner runner,
            final int maxInputs, final long maxDelay, @NotNull final TimeUnit timeUnit) {

        return backPressureOn(runner, maxInputs, fromUnit(maxDelay, timeUnit));
    }

    @NotNull
    public StreamOutputChannel<OUT> backPressureOn(@Nullable final Runner runner,
            final int maxInputs, @Nullable final TimeDuration maxDelay) {

        return withInvocations().withRunner(runner)
                                .withInputLimit(maxInputs)
                                .withInputMaxDelay(maxDelay)
                                .set();
    }

    @NotNull
    public StreamOutputChannel<OUT> maxParallelInvocations(final int maxInvocations) {

        return withInvocations().withMaxInstances(maxInvocations).set();
    }

    @NotNull
    public StreamOutputChannel<OUT> ordered(@Nullable final OrderType orderType) {

        return withStreamInvocations().withOutputOrder(orderType).set();
    }

    @NotNull
    public StreamOutputChannel<OUT> parallelFilter(
            @NotNull final Predicate<? super OUT> predicate) {

        return parallelMap(predicateFilter(predicate));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> parallelGenerate(final long count,
            @NotNull final Consumer<? super ResultChannel<AFTER>> consumer) {

        if (count <= 0) {

            throw new IllegalArgumentException("the count number must be positive: " + count);
        }

        return asyncRange(1, count).parallelMap(
                new GenerateConsumerInvocation<Number, AFTER>(wrapConsumer(consumer)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> parallelGenerate(final long count,
            @NotNull final Supplier<? extends AFTER> supplier) {

        if (count <= 0) {

            throw new IllegalArgumentException("the count number must be positive: " + count);
        }

        return asyncRange(1, count).parallelMap(
                new GenerateSupplierInvocation<Number, AFTER>(1, wrapSupplier(supplier)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> parallelLift(
            @NotNull final Function<? super OUT, ? extends OutputChannel<? extends AFTER>>
                    function) {

        return parallelMap(new LiftInvocation<OUT, AFTER>(wrapFunction(function)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> parallelMap(
            @NotNull final BiConsumer<? super OUT, ? super ResultChannel<AFTER>> consumer) {

        return parallelMap(consumerFilter(consumer));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> parallelMap(
            @NotNull final Function<? super OUT, ? extends AFTER> function) {

        return parallelMap(functionFilter(function));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> parallelMap(
            @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {

        return parallelMap(buildRoutine(factory));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> parallelMap(
            @NotNull final Routine<? super OUT, ? extends AFTER> routine) {

        return concatRoutine(routine.parallelInvoke());
    }

    @NotNull
    public <AFTER extends Comparable<AFTER>> StreamOutputChannel<AFTER> parallelRange(
            @NotNull final AFTER start, @NotNull final AFTER end,
            @NotNull final Function<AFTER, AFTER> increment) {

        return asyncRange(start, end, increment).parallelMap(Functions.<AFTER>identity());
    }

    @NotNull
    public StreamOutputChannel<Number> parallelRange(@NotNull final Number start,
            @NotNull final Number end) {

        return asyncRange(start, end).parallelMap(Functions.<Number>identity());
    }

    @NotNull
    public StreamOutputChannel<Number> parallelRange(@NotNull final Number start,
            @NotNull final Number end, @NotNull final Number increment) {

        return asyncRange(start, end, increment).parallelMap(Functions.<Number>identity());
    }

    @NotNull
    public StreamOutputChannel<OUT> runOn(@Nullable final Runner runner) {

        return withStreamInvocations().withRunner(runner).set().asyncMap(Functions.<OUT>identity());
    }

    @NotNull
    public StreamOutputChannel<OUT> runOnShared() {

        return runOn(null);
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncCollect(
            @NotNull final BiConsumer<? super List<? extends OUT>, ? super ResultChannel<AFTER>>
                    consumer) {

        return syncMap(consumerFactory(consumer));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncCollect(
            @NotNull final Function<? super List<? extends OUT>, ? extends AFTER> function) {

        return syncMap(functionFactory(function));
    }

    @NotNull
    public StreamOutputChannel<OUT> syncFilter(@NotNull final Predicate<? super OUT> predicate) {

        return syncMap(predicateFilter(predicate));
    }

    @NotNull
    public StreamOutputChannel<Void> syncForEach(@NotNull final Consumer<? super OUT> consumer) {

        return syncMap(new ConsumerInvocation<OUT>(wrapConsumer(consumer)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncGenerate(
            @NotNull final Consumer<? super ResultChannel<AFTER>> consumer) {

        return syncMap(new GenerateConsumerInvocation<OUT, AFTER>(wrapConsumer(consumer)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncGenerate(final long count,
            @NotNull final Supplier<? extends AFTER> supplier) {

        return syncMap(new GenerateSupplierInvocation<OUT, AFTER>(count, wrapSupplier(supplier)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncGenerate(
            @NotNull final Supplier<? extends AFTER> supplier) {

        return syncGenerate(1, supplier);
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncLift(
            @NotNull final Function<? super OUT, ? extends OutputChannel<? extends AFTER>>
                    function) {

        return syncMap(new LiftInvocation<OUT, AFTER>(wrapFunction(function)));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncMap(
            @NotNull final BiConsumer<? super OUT, ? super ResultChannel<AFTER>> consumer) {

        return syncMap(consumerFilter(consumer));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncMap(
            @NotNull final Function<? super OUT, ? extends AFTER> function) {

        return syncMap(functionFilter(function));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncMap(
            @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {

        return syncMap(buildRoutine(factory));
    }

    @NotNull
    public <AFTER> StreamOutputChannel<AFTER> syncMap(
            @NotNull final Routine<? super OUT, ? extends AFTER> routine) {

        return concatRoutine(routine.syncInvoke());
    }

    @NotNull
    public <AFTER extends Comparable<AFTER>> StreamOutputChannel<AFTER> syncRange(
            @NotNull final AFTER start, @NotNull final AFTER end,
            @NotNull final Function<AFTER, AFTER> increment) {

        return syncMap(new RangeInvocation<OUT, AFTER>(start, end, wrapFunction(increment)));
    }

    @NotNull
    public StreamOutputChannel<Number> syncRange(@NotNull final Number start,
            @NotNull final Number end) {

        return this.<Number>syncMap(range(start, end));
    }

    @NotNull
    public StreamOutputChannel<Number> syncRange(@NotNull final Number start,
            @NotNull final Number end, @NotNull final Number increment) {

        return this.<Number>syncMap(range(start, end, increment));
    }

    @NotNull
    public StreamOutputChannel<OUT> syncReduce(
            @NotNull final BiFunction<? super OUT, ? super OUT, ? extends OUT> function) {

        return syncMap(AccumulateInvocation.functionFactory(function));
    }

    @NotNull
    @SuppressWarnings("ConstantConditions")
    public StreamOutputChannel<OUT> tryCatch(
            @NotNull final BiConsumer<? super RoutineException, ? super InputChannel<OUT>>
                    consumer) {

        if (consumer == null) {

            throw new NullPointerException("the consumer instance must not be null");
        }

        final IOChannel<OUT> ioChannel = JRoutine.io().buildChannel();
        mChannel.passTo(new TryCatchOutputConsumer<OUT>(consumer, ioChannel));
        return newChannel(mStreamConfiguration, ioChannel);
    }

    @NotNull
    @SuppressWarnings("ConstantConditions")
    public StreamOutputChannel<OUT> tryCatch(
            @NotNull final Consumer<? super RoutineException> consumer) {

        if (consumer == null) {

            throw new NullPointerException("the consumer instance must not be null");
        }

        return tryCatch(new TryCatchBiConsumerConsumer<OUT>(consumer));
    }

    @NotNull
    @SuppressWarnings("ConstantConditions")
    public StreamOutputChannel<OUT> tryCatch(
            @NotNull final Function<? super RoutineException, ? extends OUT> function) {

        if (function == null) {

            throw new NullPointerException("the function instance must not be null");
        }

        return tryCatch(new TryCatchBiConsumerFunction<OUT>(function));
    }

    @NotNull
    public Builder<? extends StreamOutputChannel<OUT>> withInvocations() {

        return new Builder<StreamOutputChannel<OUT>>(this, mConfiguration);
    }

    @NotNull
    public Builder<? extends StreamOutputChannel<OUT>> withStreamInvocations() {

        return new Builder<StreamOutputChannel<OUT>>(mStreamConfigurable, mStreamConfiguration);
    }

    @NotNull
    public List<OUT> all() {

        return mChannel.all();
    }

    public boolean checkComplete() {

        return mChannel.checkComplete();
    }

    public boolean hasNext() {

        return mChannel.hasNext();
    }

    public OUT next() {

        return mChannel.next();
    }

    public boolean isBound() {

        return mChannel.isBound();
    }

    @NotNull
    public List<OUT> next(final int count) {

        return mChannel.next(count);
    }

    @NotNull
    public <CHANNEL extends InputChannel<? super OUT>> CHANNEL passTo(
            @NotNull final CHANNEL channel) {

        return mChannel.passTo(channel);
    }

    public Iterator<OUT> iterator() {

        return mChannel.iterator();
    }

    public void remove() {

        mChannel.remove();
    }

    /**
     * Returns the configuration which will be used by the next routine concatenated to the stream.
     *
     * @return the configuration.
     */
    @NotNull
    protected InvocationConfiguration getConfiguration() {

        return mConfiguration;
    }

    /**
     * Returns the configuration used by all the routines concatenated to the stream.
     *
     * @return the configuration.
     */
    @NotNull
    protected InvocationConfiguration getStreamConfiguration() {

        return mStreamConfiguration;
    }

    /**
     * Creates a new channel instance.
     *
     * @param configuration the stream configuration.
     * @param channel       the wrapped output channel.
     * @param <AFTER>       the concatenation output type.
     * @return the newly created channel instance.
     */
    @NotNull
    protected abstract <AFTER> StreamOutputChannel<AFTER> newChannel(
            @NotNull InvocationConfiguration configuration, @NotNull OutputChannel<AFTER> channel);

    /**
     * Creates a new routine instance based on the specified factory.
     *
     * @param configuration the routine configuration.
     * @param factory       the invocation factory.
     * @param <AFTER>       the concatenation output type.
     * @return the newly created routine instance.
     */
    @NotNull
    protected abstract <AFTER> Routine<? super OUT, ? extends AFTER> newRoutine(
            @NotNull InvocationConfiguration configuration,
            @NotNull InvocationFactory<? super OUT, ? extends AFTER> factory);

    @NotNull
    private <AFTER> Routine<? super OUT, ? extends AFTER> buildRoutine(
            @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {

        return newRoutine(mStreamConfiguration.builderFrom().with(mConfiguration).set(), factory);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <AFTER> StreamOutputChannel<AFTER> concatRoutine(
            @NotNull final InvocationChannel<? super OUT, ? extends AFTER> channel) {

        return newChannel(mStreamConfiguration,
                          (OutputChannel<AFTER>) mChannel.passTo(channel).result());
    }

    /**
     * Invocation implementation wrapping a consumer accepting output data.
     *
     * @param <OUT> the output data type.
     */
    private static class ConsumerInvocation<OUT> extends FilterInvocation<OUT, Void> {

        private final ConsumerWrapper<? super OUT> mConsumer;

        /**
         * Constructor.
         *
         * @param consumer the consumer instance.
         */
        private ConsumerInvocation(@NotNull final ConsumerWrapper<? super OUT> consumer) {

            mConsumer = consumer;
        }

        @Override
        public int hashCode() {

            return mConsumer.safeHashCode();
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if (!(o instanceof ConsumerInvocation)) {

                return false;
            }

            final ConsumerInvocation<?> that = (ConsumerInvocation<?>) o;
            return mConsumer.safeEquals(that.mConsumer);
        }

        public void onInput(final OUT input, @NotNull final ResultChannel<Void> result) {

            mConsumer.accept(input);
        }
    }

    /**
     * Invocation implementation wrapping a consumer instance.
     *
     * @param <IN>  the input data type.
     * @param <OUT> the output data type.
     */
    private static class GenerateConsumerInvocation<IN, OUT> extends InvocationFactory<IN, OUT>
            implements Invocation<IN, OUT> {

        private final ConsumerWrapper<? super ResultChannel<OUT>> mConsumer;

        /**
         * Constructor.
         *
         * @param consumer the consumer instance.
         */
        private GenerateConsumerInvocation(
                @NotNull final ConsumerWrapper<? super ResultChannel<OUT>> consumer) {

            mConsumer = consumer;
        }

        @NotNull
        @Override
        public Invocation<IN, OUT> newInvocation() {

            return this;
        }

        public void onAbort(@Nullable final RoutineException reason) {

        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if (!(o instanceof GenerateConsumerInvocation)) {

                return false;
            }

            final GenerateConsumerInvocation<?, ?> that = (GenerateConsumerInvocation<?, ?>) o;
            return mConsumer.safeEquals(that.mConsumer);
        }

        @Override
        public int hashCode() {

            return mConsumer.safeHashCode();
        }


        public void onDestroy() {

        }

        public void onInitialize() {

        }

        public void onInput(final IN input, @NotNull final ResultChannel<OUT> result) {

        }

        public void onResult(@NotNull final ResultChannel<OUT> result) {

            mConsumer.accept(result);
        }

        public void onTerminate() {

        }
    }

    /**
     * Invocation implementation wrapping a supplier instance.
     *
     * @param <IN>  the input data type.
     * @param <OUT> the output data type.
     */
    private static class GenerateSupplierInvocation<IN, OUT> extends InvocationFactory<IN, OUT>
            implements Invocation<IN, OUT> {

        private final long mCount;

        private final SupplierWrapper<? extends OUT> mSupplier;

        /**
         * Constructor.
         *
         * @param count    the number of generated outputs.
         * @param supplier the supplier instance.
         * @throws java.lang.IllegalArgumentException if the specified count number is 0 or
         *                                            negative.
         */
        private GenerateSupplierInvocation(final long count,
                @NotNull final SupplierWrapper<? extends OUT> supplier) {

            if (count <= 0) {

                throw new IllegalArgumentException("the count number must be positive: " + count);
            }

            mCount = count;
            mSupplier = supplier;
        }

        @NotNull
        @Override
        public Invocation<IN, OUT> newInvocation() {

            return this;
        }

        public void onAbort(@Nullable final RoutineException reason) {

        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if (!(o instanceof GenerateSupplierInvocation)) {

                return false;
            }

            final GenerateSupplierInvocation<?, ?> that = (GenerateSupplierInvocation<?, ?>) o;
            return mCount == that.mCount && mSupplier.safeEquals(that.mSupplier);
        }

        @Override
        public int hashCode() {

            int result = (int) (mCount ^ (mCount >>> 32));
            result = 31 * result + mSupplier.safeHashCode();
            return result;
        }

        public void onDestroy() {

        }

        public void onInitialize() {

        }

        public void onInput(final IN input, @NotNull final ResultChannel<OUT> result) {

        }

        public void onResult(@NotNull final ResultChannel<OUT> result) {

            for (int i = 0; i < mCount; ++i) {

                result.pass(mSupplier.get());
            }
        }

        public void onTerminate() {

        }
    }

    /**
     * Filter invocation implementation wrapping a lifting function.
     *
     * @param <IN>  the input data type.
     * @param <OUT> the output data type.
     */
    private static class LiftInvocation<IN, OUT> extends FilterInvocation<IN, OUT> {

        private final FunctionWrapper<? super IN, ? extends OutputChannel<? extends OUT>> mFunction;

        /**
         * Constructor.
         *
         * @param function the lifting function.
         */
        private LiftInvocation(
                @NotNull final FunctionWrapper<? super IN, ? extends OutputChannel<? extends
                        OUT>> function) {

            mFunction = function;
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if (!(o instanceof LiftInvocation)) {

                return false;
            }

            final LiftInvocation<?, ?> that = (LiftInvocation<?, ?>) o;
            return mFunction.safeEquals(that.mFunction);
        }

        @Override
        public int hashCode() {

            return mFunction.safeHashCode();
        }

        public void onInput(final IN input, @NotNull final ResultChannel<OUT> result) {

            final OutputChannel<? extends OUT> channel = mFunction.apply(input);

            if (channel != null) {

                channel.passTo(result);
            }
        }
    }

    /**
     * Invocation implementation generating a range of data.
     *
     * @param <IN>  the input data type.
     * @param <OUT> the output data type.
     */
    private static class RangeInvocation<IN, OUT extends Comparable<OUT>>
            extends InvocationFactory<IN, OUT> implements Invocation<IN, OUT> {

        private final OUT mEnd;

        private final FunctionWrapper<OUT, OUT> mIncrement;

        private final OUT mStart;

        /**
         * Constructor.
         *
         * @param start     the first element of the range.
         * @param end       the last element of the range.
         * @param increment the function incrementing the current element.
         */
        @SuppressWarnings("ConstantConditions")
        private RangeInvocation(@NotNull final OUT start, @NotNull final OUT end,
                @NotNull final FunctionWrapper<OUT, OUT> increment) {

            if (start == null) {

                throw new NullPointerException("the start element must not be null");
            }

            if (end == null) {

                throw new NullPointerException("the end element must not be null");
            }

            mStart = start;
            mEnd = end;
            mIncrement = increment;
        }

        @NotNull
        @Override
        public Invocation<IN, OUT> newInvocation() {

            return this;
        }

        @Override
        @SuppressWarnings({"SimplifiableIfStatement", "EqualsBetweenInconvertibleTypes"})
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if (!(o instanceof RangeInvocation)) {

                return false;
            }

            final RangeInvocation<?, ?> that = (RangeInvocation<?, ?>) o;

            if (!mEnd.equals(that.mEnd)) {

                return false;
            }

            if (!mIncrement.safeEquals(that.mIncrement)) {

                return false;
            }

            return mStart.equals(that.mStart);
        }

        @Override
        public int hashCode() {

            int result = mEnd.hashCode();
            result = 31 * result + mIncrement.safeHashCode();
            result = 31 * result + mStart.hashCode();
            return result;
        }

        public void onAbort(@Nullable final RoutineException reason) {

        }

        public void onDestroy() {

        }

        public void onInitialize() {

        }

        public void onInput(final IN input, @NotNull final ResultChannel<OUT> result) {

        }

        public void onResult(@NotNull final ResultChannel<OUT> result) {

            final OUT start = mStart;
            final OUT end = mEnd;
            final Function<OUT, OUT> increment = mIncrement;
            OUT current = start;

            if (start.compareTo(end) <= 0) {

                while (current.compareTo(end) <= 0) {

                    result.pass(current);
                    current = increment.apply(current);
                }

            } else {

                while (current.compareTo(end) >= 0) {

                    result.pass(current);
                    current = increment.apply(current);
                }
            }
        }

        public void onTerminate() {

        }
    }

    /**
     * Bi-consumer implementation wrapping a try/catch consumer.
     *
     * @param <OUT> the output data type.
     */
    private static class TryCatchBiConsumerConsumer<OUT>
            implements BiConsumer<RoutineException, InputChannel<OUT>> {

        private final Consumer<? super RoutineException> mConsumer;

        /**
         * Constructor.
         *
         * @param consumer the consumer instance.
         */
        private TryCatchBiConsumerConsumer(
                @NotNull final Consumer<? super RoutineException> consumer) {

            mConsumer = consumer;
        }

        public void accept(final RoutineException error, final InputChannel<OUT> channel) {

            mConsumer.accept(error);
        }
    }

    /**
     * Bi-consumer implementation wrapping a try/catch function.
     *
     * @param <OUT> the output data type.
     */
    private static class TryCatchBiConsumerFunction<OUT>
            implements BiConsumer<RoutineException, InputChannel<OUT>> {

        private final Function<? super RoutineException, ? extends OUT> mFunction;

        /**
         * Constructor.
         *
         * @param function the function instance.
         */
        private TryCatchBiConsumerFunction(
                @NotNull final Function<? super RoutineException, ? extends OUT> function) {

            mFunction = function;
        }

        public void accept(final RoutineException error, final InputChannel<OUT> channel) {

            channel.pass(mFunction.apply(error));
        }
    }

    /**
     * Try/catch output consumer implementation.
     *
     * @param <OUT> the output data type.
     */
    private static class TryCatchOutputConsumer<OUT> implements OutputConsumer<OUT> {

        private final BiConsumer<? super RoutineException, ? super InputChannel<OUT>> mConsumer;

        private final IOChannel<OUT> mOutputChannel;

        /**
         * Constructor.
         *
         * @param consumer      the consumer instance.
         * @param outputChannel the output channel.
         */
        private TryCatchOutputConsumer(
                @NotNull final BiConsumer<? super RoutineException, ? super InputChannel<OUT>>
                        consumer,
                @NotNull final IOChannel<OUT> outputChannel) {

            mConsumer = consumer;
            mOutputChannel = outputChannel;
        }

        public void onComplete() {

            mOutputChannel.close();
        }

        public void onError(@NotNull final RoutineException error) {

            final IOChannel<OUT> channel = mOutputChannel;

            try {

                mConsumer.accept(error, channel);
                channel.close();

            } catch (final Throwable t) {

                channel.abort(t);
            }
        }

        public void onOutput(final OUT output) {

            mOutputChannel.pass(output);
        }
    }

    @NotNull
    @SuppressWarnings("ConstantConditions")
    public StreamOutputChannel<OUT> setConfiguration(
            @NotNull final InvocationConfiguration configuration) {

        if (configuration == null) {

            throw new NullPointerException("the invocation configuration must not be null");
        }

        mConfiguration = configuration;
        return this;
    }
}