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

import com.github.dm.jrt.builder.ConfigurableBuilder;
import com.github.dm.jrt.builder.InvocationConfiguration.Builder;
import com.github.dm.jrt.channel.Channel.OutputChannel;
import com.github.dm.jrt.channel.OutputConsumer;
import com.github.dm.jrt.channel.ResultChannel;
import com.github.dm.jrt.channel.RoutineException;
import com.github.dm.jrt.function.BiConsumer;
import com.github.dm.jrt.function.BiFunction;
import com.github.dm.jrt.function.Consumer;
import com.github.dm.jrt.function.Function;
import com.github.dm.jrt.function.Predicate;
import com.github.dm.jrt.function.Supplier;
import com.github.dm.jrt.invocation.InvocationFactory;
import com.github.dm.jrt.routine.Routine;
import com.github.dm.jrt.runner.Runner;
import com.github.dm.jrt.util.TimeDuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Interface defining a stream output channel, that is, a channel concatenating map and reduce
 * functions.
 * <br/>
 * Each function in the channel is backed by a sub-routine instance, that can have its own specific
 * configuration and invocation mode.
 * <p/>
 * Note that, if at least one reduce function is part of the concatenation, the results will be
 * propagated only when the invocation completes.
 * <p/>
 * Created by davide-maestroni on 12/23/2015.
 *
 * @param <OUT> the output data type.
 */
public interface StreamOutputChannel<OUT>
        extends OutputChannel<OUT>, ConfigurableBuilder<StreamOutputChannel<OUT>> {

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> afterMax(@NotNull TimeDuration timeout);

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> afterMax(long timeout, @NotNull TimeUnit timeUnit);

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> allInto(@NotNull Collection<? super OUT> results);

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> eventuallyAbort();

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> eventuallyAbort(@Nullable Throwable reason);

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> eventuallyExit();

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> eventuallyThrow();

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> immediately();

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> passTo(@NotNull OutputConsumer<? super OUT> consumer);

    /**
     * {@inheritDoc}
     */
    @NotNull
    StreamOutputChannel<OUT> skip(int count);

    /**
     * Concatenates a stream channel based on the specified collecting consumer to this one.<br/>
     * The outputs will be collected by applying the function, only when the outputs complete.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the bi-consumer instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncCollect(
            @NotNull BiConsumer<? super List<? extends OUT>, ? super ResultChannel<AFTER>>
                    consumer);

    /**
     * Concatenates a stream channel based on the specified collecting function to this one.<br/>
     * The outputs will be collected by applying the function, only when the outputs complete.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncCollect(
            @NotNull Function<? super List<? extends OUT>, ? extends AFTER> function);

    /**
     * Concatenates a stream channel based on the specified predicate to this one.<br/>
     * The output will be filtered according to the result returned by the predicate.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param predicate the predicate instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<OUT> asyncFilter(@NotNull Predicate<? super OUT> predicate);

    /**
     * Concatenates a stream channel based on the specified consumer to this one.<br/>
     * The channel outputs will not be further propagated.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the consumer instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<Void> asyncForEach(@NotNull Consumer<? super OUT> consumer);

    /**
     * Concatenates a stream channel based on the specified consumer to this one.<br/>
     * The consumer will be called only when the invocation completes.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the consumer instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncGenerate(
            @NotNull Consumer<? super ResultChannel<AFTER>> consumer);

    /**
     * Concatenates a stream channel based on the specified supplier to this one.<br/>
     * The supplier will be called {@code count} number of times only when the outputs complete.
     * The count number must be positive.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param count    the number of generated outputs.
     * @param supplier the supplier instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel
     * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative..
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncGenerate(long count,
            @NotNull Supplier<? extends AFTER> supplier);

    /**
     * Concatenates a stream channel based on the specified supplier to this one.<br/>
     * The supplier will be called only when the outputs complete.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param supplier the supplier instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncGenerate(@NotNull Supplier<? extends AFTER> supplier);

    /**
     * Lifts this stream outputs by applying the specified function.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @param <AFTER>  the lifting output type.
     * @return the lifted stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncLift(
            @NotNull Function<? super OUT, ? extends OutputChannel<? extends AFTER>> function);

    /**
     * Concatenates a stream channel based on the specified consumer to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the bi-consumer instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncMap(
            @NotNull BiConsumer<? super OUT, ? super ResultChannel<AFTER>> consumer);

    /**
     * Concatenates a stream channel based on the specified function to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncMap(
            @NotNull Function<? super OUT, ? extends AFTER> function);

    /**
     * Concatenates a stream channel based on the specified factory to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param factory the invocation factory.
     * @param <AFTER> the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncMap(
            @NotNull InvocationFactory<? super OUT, ? extends AFTER> factory);

    /**
     * Concatenates a stream channel based on the specified instance to this one.
     * <p/>
     * Note that the created routine will ignore the current configuration, even if it will be
     * employed in further concatenations, and will be invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param routine the routine instance.
     * @param <AFTER> the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> asyncMap(
            @NotNull Routine<? super OUT, ? extends AFTER> routine);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The generated data will start from the specified first one up to and including the specified
     * last one, by computing each next element through the specified function.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start     the first element of the range.
     * @param end       the last element of the range.
     * @param increment the function incrementing the current element.
     * @param <AFTER>   the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER extends Comparable<AFTER>> StreamOutputChannel<AFTER> asyncRange(
            @NotNull final AFTER start, @NotNull final AFTER end,
            @NotNull final Function<AFTER, AFTER> increment);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The channel will generate a range of numbers up to and including the {@code end} element, by
     * applying a default increment of {@code +1} or {@code -1} depending on the comparison between
     * the first and the last element. That is, if the first element is less than the last, the
     * increment will be {@code +1}. On the contrary, if the former is greater than the latter, the
     * increment will be {@code -1}.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start the first element of the range.
     * @param end   the last element of the range.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<Number> asyncRange(@NotNull final Number start, @NotNull final Number end);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The channel will generate a range of numbers by applying the specified increment up to and
     * including the {@code end} element.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start     the first element of the range.
     * @param end       the last element of the range.
     * @param increment the increment to apply to the current element.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<Number> asyncRange(@NotNull final Number start, @NotNull final Number end,
            @NotNull final Number increment);

    /**
     * Concatenates a stream channel based on the specified accumulating function to this one.
     * <br/>
     * The output will be computed as follows:
     * <pre>
     *     <code>
     *
     *         acc = function.apply(acc, input);
     *     </code>
     * </pre>
     * The accumulated value will be passed as result only when the outputs complete.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in an asynchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the bi-function instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<OUT> asyncReduce(
            @NotNull BiFunction<? super OUT, ? super OUT, ? extends OUT> function);

    // TODO: 30/12/15 unordered, backPressure, runOn
    @NotNull
    StreamOutputChannel<OUT> backPressureOn(@Nullable Runner runner, int maxOutputs, long maxTime,
            TimeUnit timeUnit);

    // TODO: 30/12/15 unordered, backPressure, runOn
    @NotNull
    StreamOutputChannel<OUT> backPressureOn(@Nullable Runner runner, int maxOutputs,
            TimeDuration maxTime);

    // TODO: 30/12/15 unordered, backPressure, runOn
    @NotNull
    StreamOutputChannel<OUT> maxParallelInvocations(int maxInvocations);

    // TODO: 30/12/15 unordered, backPressure, runOn
    @NotNull
    StreamOutputChannel<OUT> ordered();

    /**
     * Concatenates a stream channel based on the specified predicate to this one.<br/>
     * The output will be filtered according to the result returned by the predicate.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param predicate the predicate instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<OUT> parallelFilter(@NotNull Predicate<? super OUT> predicate);

    /**
     * Concatenates a stream channel based on the specified consumer to this one.<br/>
     * The consumer will be called {@code count} number of times only when the outputs complete.
     * The count number must be positive.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param count    the number of generated outputs.
     * @param consumer the consumer instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel
     * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> parallelGenerate(long count,
            @NotNull Consumer<? super ResultChannel<AFTER>> consumer);

    /**
     * Concatenates a stream channel based on the specified supplier to this one.<br/>
     * The supplier will be called {@code count} number of times only when the outputs complete.
     * The count number must be positive.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param count    the number of generated outputs.
     * @param supplier the supplier instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel
     * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> parallelGenerate(long count,
            @NotNull Supplier<? extends AFTER> supplier);

    /**
     * Lifts this stream outputs by applying the specified function.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @param <AFTER>  the lifting output type.
     * @return the lifted stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> parallelLift(
            @NotNull Function<? super OUT, ? extends OutputChannel<? extends AFTER>> function);

    /**
     * Concatenates a stream channel based on the specified consumer to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the bi-consumer instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> parallelMap(
            @NotNull BiConsumer<? super OUT, ? super ResultChannel<AFTER>> consumer);

    /**
     * Concatenates a stream channel based on the specified function to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> parallelMap(
            @NotNull Function<? super OUT, ? extends AFTER> function);

    /**
     * Concatenates a stream channel based on the specified factory to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param factory the invocation factory.
     * @param <AFTER> the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> parallelMap(
            @NotNull InvocationFactory<? super OUT, ? extends AFTER> factory);

    /**
     * Concatenates a stream channel based on the specified instance to this one.
     * <p/>
     * Note that the created routine will ignore the current configuration, even if it will be
     * employed in further concatenations, and will be invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param routine the routine instance.
     * @param <AFTER> the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> parallelMap(
            @NotNull Routine<? super OUT, ? extends AFTER> routine);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The generated data will start from the specified first one up to and including the specified
     * last one, by computing each next element through the specified function.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start     the first element of the range.
     * @param end       the last element of the range.
     * @param increment the function incrementing the current element.
     * @param <AFTER>   the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER extends Comparable<AFTER>> StreamOutputChannel<AFTER> parallelRange(
            @NotNull final AFTER start, @NotNull final AFTER end,
            @NotNull final Function<AFTER, AFTER> increment);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The channel will generate a range of numbers up to and including the {@code end} element, by
     * applying a default increment of {@code +1} or {@code -1} depending on the comparison between
     * the first and the last element. That is, if the first element is less than the last, the
     * increment will be {@code +1}. On the contrary, if the former is greater than the latter, the
     * increment will be {@code -1}.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start the first element of the range.
     * @param end   the last element of the range.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<Number> parallelRange(@NotNull final Number start,
            @NotNull final Number end);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The channel will generate a range of numbers by applying the specified increment up to and
     * including the {@code end} element.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a parallel mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start     the first element of the range.
     * @param end       the last element of the range.
     * @param increment the increment to apply to the current element.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<Number> parallelRange(@NotNull final Number start,
            @NotNull final Number end, @NotNull final Number increment);

    // TODO: 30/12/15 unordered, backPressure, runOn
    @NotNull
    StreamOutputChannel<OUT> runOn(@Nullable Runner runner);

    // TODO: 30/12/15 unordered, backPressure, runOn
    @NotNull
    StreamOutputChannel<OUT> runOnShared();

    /**
     * Concatenates a stream channel based on the specified collecting consumer to this one.<br/>
     * The outputs will be collected by applying the function, only when the outputs complete.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the bi-consumer instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncCollect(
            @NotNull BiConsumer<? super List<? extends OUT>, ? super ResultChannel<AFTER>>
                    consumer);

    /**
     * Concatenates a stream channel based on the specified collecting function to this one.<br/>
     * The outputs will be collected by applying the function, only when the outputs complete.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncCollect(
            @NotNull Function<? super List<? extends OUT>, ? extends AFTER> function);

    /**
     * Concatenates a stream channel based on the specified predicate to this one.<br/>
     * The output will be filtered according to the result returned by the predicate.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param predicate the predicate instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<OUT> syncFilter(@NotNull Predicate<? super OUT> predicate);

    /**
     * Concatenates a stream channel based on the specified consumer to this one.<br/>
     * The channel outputs will not be further propagated.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the consumer instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<Void> syncForEach(@NotNull Consumer<? super OUT> consumer);

    /**
     * Concatenates a stream channel based on the specified consumer to this one.<br/>
     * The consumer will be called only when the invocation completes.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the consumer instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncGenerate(
            @NotNull Consumer<? super ResultChannel<AFTER>> consumer);

    /**
     * Concatenates a stream channel based on the specified supplier to this one.<br/>
     * The supplier will be called {@code count} number of times only when the outputs complete.
     * The count number must be positive.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param count    the number of generated outputs.
     * @param supplier the supplier instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel
     * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncGenerate(long count,
            @NotNull Supplier<? extends AFTER> supplier);

    /**
     * Concatenates a stream channel based on the specified supplier to this one.<br/>
     * The supplier will be called only when the outputs complete.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param supplier the supplier instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncGenerate(@NotNull Supplier<? extends AFTER> supplier);

    /**
     * Lifts this stream outputs by applying the specified function.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @param <AFTER>  the lifting output type.
     * @return the lifted stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncLift(
            @NotNull Function<? super OUT, ? extends OutputChannel<? extends AFTER>> function);

    /**
     * Concatenates a stream channel based on the specified consumer to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param consumer the bi-consumer instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncMap(
            @NotNull BiConsumer<? super OUT, ? super ResultChannel<AFTER>> consumer);

    /**
     * Concatenates a stream channel based on the specified function to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @param <AFTER>  the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncMap(
            @NotNull Function<? super OUT, ? extends AFTER> function);

    /**
     * Concatenates a stream channel based on the specified factory to this one.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param factory the invocation factory.
     * @param <AFTER> the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncMap(
            @NotNull InvocationFactory<? super OUT, ? extends AFTER> factory);

    /**
     * Concatenates a stream channel based on the specified instance to this one.
     * <p/>
     * Note that the created routine will ignore the current configuration, even if it will be
     * employed in further concatenations, and will be invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param routine the routine instance.
     * @param <AFTER> the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER> StreamOutputChannel<AFTER> syncMap(
            @NotNull Routine<? super OUT, ? extends AFTER> routine);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The generated data will start from the specified first one up to and including the specified
     * last one, by computing each next element through the specified function.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start     the first element of the range.
     * @param end       the last element of the range.
     * @param increment the function incrementing the current element.
     * @param <AFTER>   the concatenation output type.
     * @return the concatenated stream channel.
     */
    @NotNull
    <AFTER extends Comparable<AFTER>> StreamOutputChannel<AFTER> syncRange(
            @NotNull final AFTER start, @NotNull final AFTER end,
            @NotNull final Function<AFTER, AFTER> increment);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The channel will generate a range of numbers up to and including the {@code end} element, by
     * applying a default increment of {@code +1} or {@code -1} depending on the comparison between
     * the first and the last element. That is, if the first element is less than the last, the
     * increment will be {@code +1}. On the contrary, if the former is greater than the latter, the
     * increment will be {@code -1}.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start the first element of the range.
     * @param end   the last element of the range.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<Number> syncRange(@NotNull final Number start, @NotNull final Number end);

    /**
     * Concatenates a stream channel generating the specified range of data.<br/>
     * The channel will generate a range of numbers by applying the specified increment up to and
     * including the {@code end} element.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param start     the first element of the range.
     * @param end       the last element of the range.
     * @param increment the increment to apply to the current element.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<Number> syncRange(@NotNull final Number start, @NotNull final Number end,
            @NotNull final Number increment);

    /**
     * Concatenates a stream channel based on the specified accumulating function to this one.
     * <br/>
     * The output will be computed as follows:
     * <pre>
     *     <code>
     *
     *         acc = function.apply(acc, input);
     *     </code>
     * </pre>
     * The accumulated value will be passed as result only when the outputs complete.
     * <p/>
     * Note that the created routine will be initialized with the current configuration and will be
     * invoked in a synchronous mode.<br/>
     * Note also that the output channel will be bound as a result of the call.
     *
     * @param function the bi-function instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<OUT> syncReduce(
            @NotNull BiFunction<? super OUT, ? super OUT, ? extends OUT> function);

    /**
     * Concatenates a consumer handling the invocation exceptions.<br/>
     * The errors will not be automatically further propagated.
     * <p/>
     * Note that the output channel will be bound as a result of the call.
     *
     * @param consumer the bi-consumer instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<OUT> tryCatch(
            @NotNull BiConsumer<? super RoutineException, ? super InputChannel<OUT>> consumer);

    /**
     * Concatenates a consumer handling a invocation exceptions.<br/>
     * The errors will not be automatically further propagated.
     * <p/>
     * Note that the output channel will be bound as a result of the call.
     *
     * @param consumer the consumer instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<OUT> tryCatch(@NotNull Consumer<? super RoutineException> consumer);

    /**
     * Concatenates a function handling a invocation exceptions.<br/>
     * The errors will not be automatically further propagated.
     * <p/>
     * Note that the output channel will be bound as a result of the call.
     *
     * @param function the function instance.
     * @return the concatenated stream channel.
     */
    @NotNull
    StreamOutputChannel<OUT> tryCatch(
            @NotNull Function<? super RoutineException, ? extends OUT> function);

    /**
     * Gets the invocation configuration builder related only to the next concatenated routine
     * instance. Any further addition to the chain will retain only the base configuration.
     * <p/>
     * Note that the configuration builder will be initialized with the current configuration for
     * the next routine.
     *
     * @return the invocation configuration builder.
     */
    @NotNull
    Builder<? extends StreamOutputChannel<OUT>> withNextInvocations();
}
