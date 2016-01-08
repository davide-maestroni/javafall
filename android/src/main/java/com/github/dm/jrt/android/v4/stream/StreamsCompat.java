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
package com.github.dm.jrt.android.v4.stream;

import android.support.v4.util.SparseArrayCompat;

import com.github.dm.jrt.android.builder.LoaderRoutineBuilder;
import com.github.dm.jrt.android.invocation.FunctionContextInvocationFactory;
import com.github.dm.jrt.android.v4.core.ChannelsCompat;
import com.github.dm.jrt.android.v4.core.JRoutineCompat;
import com.github.dm.jrt.android.v4.core.JRoutineCompat.ContextBuilder;
import com.github.dm.jrt.android.v4.core.LoaderContextCompat;
import com.github.dm.jrt.channel.Channel.OutputChannel;
import com.github.dm.jrt.core.DelegatingInvocation.DelegationType;
import com.github.dm.jrt.function.Function;
import com.github.dm.jrt.invocation.InvocationFactory;
import com.github.dm.jrt.stream.StreamOutputChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.dm.jrt.android.core.DelegatingContextInvocation.factoryFrom;
import static com.github.dm.jrt.function.Functions.wrapFunction;

/**
 * Utility class acting as a factory of stream output channels.
 * <p/>
 * Created by davide-maestroni on 01/04/2016.
 */
public class StreamsCompat extends ChannelsCompat {

    /**
     * Avoid direct instantiation.
     */
    protected StreamsCompat() {

    }

    /**
     * Returns a stream output channel blending the outputs coming from the specified ones.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channels the list of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> blend(
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.blend(channels));
    }

    /**
     * Returns a stream output channel blending the outputs coming from the specified ones.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channels the array of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> blend(
            @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>blend(channels));
    }

    /**
     * Returns a stream output channel concatenating the outputs coming from the specified ones, so
     * that, all the outputs of the first channel will come before all the outputs of the second
     * one, and so on.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channels the list of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> concat(
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.concat(channels));
    }

    /**
     * Returns a stream output channel concatenating the outputs coming from the specified ones, so
     * that, all the outputs of the first channel will come before all the outputs of the second
     * one, and so on.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channels the array of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> concat(
            @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>concat(channels));
    }

    /**
     * Returns an invocation factory, whose invocation instances employ the stream output channels,
     * provided by the specified function, to process input data.<br/>
     * The function should return a new instance each time it is called, starting from the passed
     * one.
     *
     * @param function the function providing the stream output channels.
     * @param <IN>     the input data type.
     * @param <OUT>    the output data type.
     * @return the invocation factory.
     */
    @NotNull
    public static <IN, OUT> FunctionContextInvocationFactory<IN, OUT> factory(
            @NotNull final Function<? super StreamOutputChannel<? extends IN>, ? extends
                    StreamOutputChannel<? extends OUT>> function) {

        return factoryFrom(com.github.dm.jrt.stream.Streams.on(function),
                           wrapFunction(function).hashCode(), DelegationType.SYNC);
    }

    /**
     * Returns a factory of invocations grouping the input data in collections of the specified
     * size.
     *
     * @param size   the group size.
     * @param <DATA> the data type.
     * @return the invocation factory.
     */
    @NotNull
    public static <DATA> InvocationFactory<DATA, List<DATA>> groupBy(final int size) {

        return com.github.dm.jrt.stream.Streams.groupBy(size);
    }

    /**
     * Returns a stream output channel joining the data coming from the specified list of channels.
     * <br/>
     * An output will be generated only when at least one result is available for each channel.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channels the list of channels.
     * @param <OUT>    the output data type.
     * @return the output channel.
     * @throws IllegalArgumentException if the specified list is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<List<? extends OUT>> join(
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.join(channels));
    }

    /**
     * Returns a stream output channel joining the data coming from the specified list of channels.
     * <br/>
     * An output will be generated only when at least one result is available for each channel.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channels the array of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     * @throws IllegalArgumentException if the specified array is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<List<? extends OUT>> join(
            @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>join(channels));
    }

    /**
     * Returns a stream output channel joining the data coming from the specified list of channels.
     * <br/>
     * An output will be generated only when at least one result is available for each channel.
     * Moreover, when all the output channels complete, the remaining outputs will be returned by
     * filling the gaps with the specified placeholder instance, so that the generated list of data
     * will always have the same size of the channel list.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param placeholder the placeholder instance.
     * @param channels    the list of channels.
     * @param <OUT>       the output data type.
     * @return the stream channel.
     * @throws IllegalArgumentException if the specified list is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<List<? extends OUT>> joinAndFlush(
            @Nullable final OUT placeholder,
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.joinAndFlush(placeholder, channels));
    }

    /**
     * Returns a stream output channel joining the data coming from the specified list of channels.
     * <br/>
     * An output will be generated only when at least one result is available for each channel.
     * Moreover, when all the output channels complete, the remaining outputs will be returned by
     * filling the gaps with the specified placeholder instance, so that the generated list of data
     * will always have the same size of the channel list.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param placeholder the placeholder instance.
     * @param channels    the array of channels.
     * @param <OUT>       the output data type.
     * @return the stream channel.
     * @throws IllegalArgumentException if the specified array is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<List<? extends OUT>> joinAndFlush(
            @Nullable final Object placeholder, @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>joinAndFlush(placeholder, channels));
    }

    /**
     * Returns an factory of invocations passing at max the specified number of input data and
     * discarding the following ones.
     *
     * @param count  the maximum number of data to pass.
     * @param <DATA> the data type.
     * @return the invocation factory.
     */
    @NotNull
    public static <DATA> InvocationFactory<DATA, DATA> limit(final int count) {

        return com.github.dm.jrt.stream.Streams.limit(count);
    }

    /**
     * Merges the specified channels into a selectable one.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param startIndex the selectable start index.
     * @param channels   the list of channels.
     * @param <OUT>      the output data type.
     * @return the selectable stream channel.
     * @throws IllegalArgumentException if the specified list is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<? extends ParcelableSelectable<OUT>> merge(
            final int startIndex,
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.merge(startIndex, channels));
    }

    /**
     * Merges the specified channels into a selectable one.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param startIndex the selectable start index.
     * @param channels   the array of channels.
     * @param <OUT>      the output data type.
     * @return the selectable stream channel.
     * @throws IllegalArgumentException if the specified array is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<? extends ParcelableSelectable<OUT>> merge(
            final int startIndex, @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>merge(startIndex, channels));
    }

    /**
     * Merges the specified channels into a selectable one. The selectable indexes will be the same
     * as the list ones.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channels the channels to merge.
     * @param <OUT>    the output data type.
     * @return the selectable stream channel.
     * @throws IllegalArgumentException if the specified list is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<? extends ParcelableSelectable<OUT>> merge(
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.merge(channels));
    }

    /**
     * Merges the specified channels into a selectable one. The selectable indexes will be the same
     * as the array ones.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channels the channels to merge.
     * @param <OUT>    the output data type.
     * @return the selectable stream channel.
     * @throws IllegalArgumentException if the specified array is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<? extends ParcelableSelectable<OUT>> merge(
            @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>merge(channels));
    }

    /**
     * Merges the specified channels into a selectable one.<br/>
     * Note that the returned channel will employ a synchronous runner to transfer data, and that
     * the passed ones will be bound as a result of the call.
     *
     * @param channelMap the map of indexes and output channels.
     * @param <OUT>      the output data type.
     * @return the selectable output channel.
     * @throws IllegalArgumentException if the specified map is empty.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<? extends ParcelableSelectable<OUT>> merge(
            @NotNull final SparseArrayCompat<? extends OutputChannel<? extends OUT>> channelMap) {

        return streamOf(ChannelsCompat.merge(channelMap));
    }

    /**
     * Returns an factory of invocations skipping the specified number of input data.
     *
     * @param count  the number of data to skip.
     * @param <DATA> the data type.
     * @return the invocation factory.
     */
    @NotNull
    public static <DATA> InvocationFactory<DATA, DATA> skip(final int count) {

        return com.github.dm.jrt.stream.Streams.skip(count);
    }

    /**
     * Builds and returns a new stream output channel.
     *
     * @param <OUT> the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> streamOf() {

        return com.github.dm.jrt.stream.Streams.streamOf();
    }

    /**
     * Builds and returns a new stream output channel generating the specified outputs.
     *
     * @param outputs the iterable returning the output data.
     * @param <OUT>   the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> streamOf(@Nullable final Iterable<OUT> outputs) {

        return com.github.dm.jrt.stream.Streams.streamOf(outputs);
    }

    /**
     * Builds and returns a new stream output channel generating the specified output.
     *
     * @param output the output.
     * @param <OUT>  the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> streamOf(@Nullable final OUT output) {

        return com.github.dm.jrt.stream.Streams.streamOf(output);
    }

    /**
     * Builds and returns a new stream output channel generating the specified outputs.
     *
     * @param outputs the output data.
     * @param <OUT>   the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> streamOf(@Nullable final OUT... outputs) {

        return com.github.dm.jrt.stream.Streams.streamOf(outputs);
    }

    /**
     * Builds and returns a new stream output channel generating the specified outputs.
     * <p/>
     * Note that the output channel will be bound as a result of the call.
     *
     * @param output the output channel returning the output data.
     * @param <OUT>  the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamOutputChannel<OUT> streamOf(
            @NotNull final OutputChannel<OUT> output) {

        return com.github.dm.jrt.stream.Streams.streamOf(output);
    }

    /**
     * Returns a context based builder of loader routine builders.
     *
     * @param context the loader context.
     * @return the context builder.
     */
    @NotNull
    public static StreamContextBuilder with(@NotNull final LoaderContextCompat context) {

        return new StreamContextBuilder(JRoutineCompat.with(context));
    }

    /**
     * Context based builder of loader routine builders.
     */
    public static class StreamContextBuilder {

        private final ContextBuilder mContextBuilder;

        /**
         * Constructor.
         *
         * @param builder the context builder.
         */
        private StreamContextBuilder(@NotNull final ContextBuilder builder) {

            mContextBuilder = builder;
        }

        /**
         * Returns a loader routine builder, whose invocation instances employ the stream output
         * channels, provided by the specified function, to process input data.<br/>
         * The function should return a new instance each time it is called, starting from the
         * passed one.
         *
         * @param function the function providing the stream output channels.
         * @param <IN>     the input data type.
         * @param <OUT>    the output data type.
         * @return the loader routine builder.
         */
        @NotNull
        public <IN, OUT> LoaderRoutineBuilder<IN, OUT> on(
                @NotNull final Function<? super StreamOutputChannel<? extends IN>, ? extends
                        StreamOutputChannel<? extends OUT>> function) {

            return mContextBuilder.on(factory(function));
        }
    }
}