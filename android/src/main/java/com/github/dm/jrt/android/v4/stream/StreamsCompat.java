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

import android.support.v4.util.SparseArrayCompat;

import com.github.dm.jrt.android.builder.LoaderRoutineBuilder;
import com.github.dm.jrt.android.invocation.FunctionContextInvocationFactory;
import com.github.dm.jrt.android.v4.core.ChannelsCompat;
import com.github.dm.jrt.android.v4.core.JRoutineCompat;
import com.github.dm.jrt.android.v4.core.JRoutineCompat.ContextBuilderCompat;
import com.github.dm.jrt.android.v4.core.LoaderContextCompat;
import com.github.dm.jrt.channel.Channel.OutputChannel;
import com.github.dm.jrt.channel.IOChannel;
import com.github.dm.jrt.core.DelegatingInvocation.DelegationType;
import com.github.dm.jrt.function.Function;
import com.github.dm.jrt.invocation.InvocationFactory;
import com.github.dm.jrt.stream.StreamChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.dm.jrt.android.core.DelegatingContextInvocation.factoryFrom;
import static com.github.dm.jrt.function.Functions.wrapFunction;

/**
 * Utility class acting as a factory of stream output channels.
 * <p/>
 * Created by davide-maestroni on 01/04/2016.
 */
public class StreamsCompat extends ChannelsCompat {

    private static final WeakHashMap<LoaderContextCompat, StreamContextBuilder> sBuilders =
            new WeakHashMap<LoaderContextCompat, StreamContextBuilder>();

    /**
     * Avoid direct instantiation.
     */
    protected StreamsCompat() {

    }

    /**
     * Returns a stream output channel blending the outputs coming from the specified ones.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param channels the list of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<OUT> blend(
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.blend(channels));
    }

    /**
     * Returns a stream output channel blending the outputs coming from the specified ones.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param channels the array of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<OUT> blend(
            @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>blend(channels));
    }

    /**
     * Returns a stream output channel concatenating the outputs coming from the specified ones, so
     * that, all the outputs of the first channel will come before all the outputs of the second
     * one, and so on.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param channels the list of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<OUT> concat(
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.concat(channels));
    }

    /**
     * Returns a stream output channel concatenating the outputs coming from the specified ones, so
     * that, all the outputs of the first channel will come before all the outputs of the second
     * one, and so on.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param channels the array of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<OUT> concat(
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
            @NotNull final Function<? super StreamChannel<? extends IN>, ? extends
                    StreamChannel<? extends OUT>> function) {

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
     * Note that the channels will be bound as a result of the call.
     *
     * @param channels the list of channels.
     * @param <OUT>    the output data type.
     * @return the output channel.
     * @throws java.lang.IllegalArgumentException if the specified list is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<List<? extends OUT>> join(
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.join(channels));
    }

    /**
     * Returns a stream output channel joining the data coming from the specified list of channels.
     * <br/>
     * An output will be generated only when at least one result is available for each channel.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param channels the array of channels.
     * @param <OUT>    the output data type.
     * @return the stream channel.
     * @throws java.lang.IllegalArgumentException if the specified array is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<List<? extends OUT>> join(
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
     * Note that the channels will be bound as a result of the call.
     *
     * @param placeholder the placeholder instance.
     * @param channels    the list of channels.
     * @param <OUT>       the output data type.
     * @return the stream channel.
     * @throws java.lang.IllegalArgumentException if the specified list is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<List<? extends OUT>> joinAndFlush(
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
     * Note that the channels will be bound as a result of the call.
     *
     * @param placeholder the placeholder instance.
     * @param channels    the array of channels.
     * @param <OUT>       the output data type.
     * @return the stream channel.
     * @throws java.lang.IllegalArgumentException if the specified array is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<List<? extends OUT>> joinAndFlush(
            @Nullable final Object placeholder, @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>joinAndFlush(placeholder, channels));
    }

    /**
     * Builds and returns a new lazy stream output channel.<br/>
     * The stream will start producing results only when it is bound to another channel or an output
     * consumer, or when any of the read methods is called.
     *
     * @param <OUT> the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamChannel<OUT> lazyStreamOf() {

        return lazyStreamOf(JRoutineCompat.io().<OUT>buildChannel().close());
    }

    /**
     * Builds and returns a new lazy stream output channel generating the specified outputs.<br/>
     * The stream will start producing results only when it is bound to another channel or an output
     * consumer, or when any of the read methods is called.
     *
     * @param outputs the iterable returning the output data.
     * @param <OUT>   the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamChannel<OUT> lazyStreamOf(@Nullable final Iterable<OUT> outputs) {

        return lazyStreamOf(JRoutineCompat.io().of(outputs));
    }

    /**
     * Builds and returns a new lazy stream output channel generating the specified output.<br/>
     * The stream will start producing results only when it is bound to another channel or an output
     * consumer, or when any of the read methods is called.
     *
     * @param output the output.
     * @param <OUT>  the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamChannel<OUT> lazyStreamOf(@Nullable final OUT output) {

        return lazyStreamOf(JRoutineCompat.io().of(output));
    }

    /**
     * Builds and returns a new lazy stream output channel generating the specified outputs.<br/>
     * The stream will start producing results only when it is bound to another channel or an output
     * consumer, or when any of the read methods is called.
     *
     * @param outputs the output data.
     * @param <OUT>   the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> StreamChannel<OUT> lazyStreamOf(@Nullable final OUT... outputs) {

        return lazyStreamOf(JRoutineCompat.io().of(outputs));
    }

    /**
     * Builds and returns a new lazy stream output channel generating the specified outputs.<br/>
     * The stream will start producing results only when it is bound to another channel or an output
     * consumer, or when any of the read methods is called.
     * <p/>
     * Note that the output channel will be bound as a result of the call.
     *
     * @param output the output channel returning the output data.
     * @param <OUT>  the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    @SuppressWarnings("ConstantConditions")
    public static <OUT> StreamChannel<OUT> lazyStreamOf(@NotNull final OutputChannel<OUT> output) {

        if (output == null) {

            throw new NullPointerException("the output channel instance must not be null");
        }

        final IOChannel<OUT> ioChannel = JRoutineCompat.io().buildChannel();
        return new DefaultLoaderStreamChannelCompat<OUT>(null, ioChannel,
                                                         new BindingRunnable<OUT>(ioChannel,
                                                                                  output));
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
     * Note that the channels will be bound as a result of the call.
     *
     * @param startIndex the selectable start index.
     * @param channels   the list of channels.
     * @param <OUT>      the output data type.
     * @return the selectable stream channel.
     * @throws java.lang.IllegalArgumentException if the specified list is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<? extends ParcelableSelectable<OUT>> merge(
            final int startIndex,
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.merge(startIndex, channels));
    }

    /**
     * Merges the specified channels into a selectable one.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param startIndex the selectable start index.
     * @param channels   the array of channels.
     * @param <OUT>      the output data type.
     * @return the selectable stream channel.
     * @throws java.lang.IllegalArgumentException if the specified array is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<? extends ParcelableSelectable<OUT>> merge(
            final int startIndex, @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>merge(startIndex, channels));
    }

    /**
     * Merges the specified channels into a selectable one. The selectable indexes will be the same
     * as the list ones.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param channels the channels to merge.
     * @param <OUT>    the output data type.
     * @return the selectable stream channel.
     * @throws java.lang.IllegalArgumentException if the specified list is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<? extends ParcelableSelectable<OUT>> merge(
            @NotNull final List<? extends OutputChannel<? extends OUT>> channels) {

        return streamOf(ChannelsCompat.merge(channels));
    }

    /**
     * Merges the specified channels into a selectable one. The selectable indexes will be the same
     * as the array ones.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param channels the channels to merge.
     * @param <OUT>    the output data type.
     * @return the selectable stream channel.
     * @throws java.lang.IllegalArgumentException if the specified array is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<? extends ParcelableSelectable<OUT>> merge(
            @NotNull final OutputChannel<?>... channels) {

        return streamOf(ChannelsCompat.<OUT>merge(channels));
    }

    /**
     * Merges the specified channels into a selectable one.<br/>
     * Note that the channels will be bound as a result of the call.
     *
     * @param channelMap the map of indexes and output channels.
     * @param <OUT>      the output data type.
     * @return the selectable output channel.
     * @throws java.lang.IllegalArgumentException if the specified map is empty.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<? extends ParcelableSelectable<OUT>> merge(
            @NotNull final SparseArrayCompat<? extends OutputChannel<? extends OUT>> channelMap) {

        return streamOf(ChannelsCompat.merge(channelMap));
    }

    /**
     * Returns a new channel repeating the output data to any newly bound channel or consumer, thus
     * effectively supporting binding of several output consumers.<br/>
     * Note that the passed channels will be bound as a result of the call.
     *
     * @param channel the output channel.
     * @param <OUT>   the output data type.
     * @return the repeating channel.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<OUT> repeat(
            @NotNull final OutputChannel<OUT> channel) {

        return streamOf(ChannelsCompat.repeat(channel));
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
    public static <OUT> LoaderStreamChannelCompat<OUT> streamOf() {

        return streamOf(JRoutineCompat.io().<OUT>buildChannel().close());
    }

    /**
     * Builds and returns a new stream output channel generating the specified outputs.
     *
     * @param outputs the iterable returning the output data.
     * @param <OUT>   the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<OUT> streamOf(
            @Nullable final Iterable<OUT> outputs) {

        return streamOf(JRoutineCompat.io().of(outputs));
    }

    /**
     * Builds and returns a new stream output channel generating the specified output.
     *
     * @param output the output.
     * @param <OUT>  the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<OUT> streamOf(@Nullable final OUT output) {

        return streamOf(JRoutineCompat.io().of(output));
    }

    /**
     * Builds and returns a new stream output channel generating the specified outputs.
     *
     * @param outputs the output data.
     * @param <OUT>   the output data type.
     * @return the newly created channel instance.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<OUT> streamOf(@Nullable final OUT... outputs) {

        return streamOf(JRoutineCompat.io().of(outputs));
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
    public static <OUT> LoaderStreamChannelCompat<OUT> streamOf(
            @NotNull final OutputChannel<OUT> output) {

        return new DefaultLoaderStreamChannelCompat<OUT>(null, output);
    }

    /**
     * Returns a new channel making the specified one selectable.<br/>
     * Each output will be passed along unchanged.<br/>
     * Note that the passed channel will be bound as a result of the call.
     *
     * @param channel the channel to make selectable.
     * @param index   the channel index.
     * @param <OUT>   the output data type.
     * @return the selectable output channel.
     */
    @NotNull
    public static <OUT> LoaderStreamChannelCompat<? extends ParcelableSelectable<OUT>> toSelectable(
            @NotNull final OutputChannel<? extends OUT> channel, final int index) {

        return streamOf(ChannelsCompat.toSelectable(channel, index));
    }

    /**
     * Returns a context based builder of loader routine builders.
     *
     * @param context the loader context.
     * @return the context builder.
     */
    @NotNull
    public static StreamContextBuilder with(@NotNull final LoaderContextCompat context) {

        synchronized (sBuilders) {
            final WeakHashMap<LoaderContextCompat, StreamContextBuilder> builders = sBuilders;
            StreamContextBuilder contextBuilder = builders.get(context);
            if (contextBuilder == null) {
                contextBuilder = new StreamContextBuilder(JRoutineCompat.with(context));
                builders.put(context, contextBuilder);
            }

            return contextBuilder;
        }
    }

    /**
     * Context based builder of loader routine builders.
     */
    public static class StreamContextBuilder {

        private final ContextBuilderCompat mContextBuilder;

        /**
         * Constructor.
         *
         * @param builder the context builder.
         */
        private StreamContextBuilder(@NotNull final ContextBuilderCompat builder) {

            mContextBuilder = builder;
        }

        /**
         * Builds and returns a new lazy stream output channel.<br/>
         * The stream will start producing results only when it is bound to another channel or an
         * output consumer, or when any of the read methods is called.
         *
         * @param <OUT> the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        public <OUT> StreamChannel<OUT> lazyStreamOf() {

            return lazyStreamOf(JRoutineCompat.io().<OUT>buildChannel().close());
        }

        /**
         * Builds and returns a new lazy stream output channel generating the specified outputs.
         * <br/>
         * The stream will start producing results only when it is bound to another channel or an
         * output consumer, or when any of the read methods is called.
         *
         * @param outputs the iterable returning the output data.
         * @param <OUT>   the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        public <OUT> StreamChannel<OUT> lazyStreamOf(@Nullable final Iterable<OUT> outputs) {

            return lazyStreamOf(JRoutineCompat.io().of(outputs));
        }

        /**
         * Builds and returns a new lazy stream output channel generating the specified output.<br/>
         * The stream will start producing results only when it is bound to another channel or an
         * output consumer, or when any of the read methods is called.
         *
         * @param output the output.
         * @param <OUT>  the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        public <OUT> StreamChannel<OUT> lazyStreamOf(@Nullable final OUT output) {

            return lazyStreamOf(JRoutineCompat.io().of(output));
        }

        /**
         * Builds and returns a new lazy stream output channel generating the specified outputs.
         * <br/>
         * The stream will start producing results only when it is bound to another channel or an
         * output consumer, or when any of the read methods is called.
         *
         * @param outputs the output data.
         * @param <OUT>   the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        public <OUT> StreamChannel<OUT> lazyStreamOf(@Nullable final OUT... outputs) {

            return lazyStreamOf(JRoutineCompat.io().of(outputs));
        }

        /**
         * Builds and returns a new lazy stream output channel generating the specified outputs.
         * <br/>
         * The stream will start producing results only when it is bound to another channel or an
         * output consumer, or when any of the read methods is called.
         * <p/>
         * Note that the output channel will be bound as a result of the call.
         *
         * @param output the output channel returning the output data.
         * @param <OUT>  the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        @SuppressWarnings("ConstantConditions")
        public <OUT> StreamChannel<OUT> lazyStreamOf(@NotNull final OutputChannel<OUT> output) {

            if (output == null) {

                throw new NullPointerException("the output channel instance must not be null");
            }

            final IOChannel<OUT> ioChannel = JRoutineCompat.io().buildChannel();
            return new DefaultLoaderStreamChannelCompat<OUT>(mContextBuilder, ioChannel,
                                                             new BindingRunnable<OUT>(ioChannel,
                                                                                      output));
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
                @NotNull final Function<? super StreamChannel<? extends IN>, ? extends
                        StreamChannel<? extends OUT>> function) {

            return mContextBuilder.on(factory(function));
        }

        /**
         * Builds and returns a new stream output channel.
         *
         * @param <OUT> the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        public <OUT> LoaderStreamChannelCompat<OUT> streamOf() {

            return streamOf(JRoutineCompat.io().<OUT>buildChannel().close());
        }

        /**
         * Builds and returns a new stream output channel generating the specified outputs.
         *
         * @param outputs the iterable returning the output data.
         * @param <OUT>   the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        public <OUT> LoaderStreamChannelCompat<OUT> streamOf(
                @Nullable final Iterable<OUT> outputs) {

            return streamOf(JRoutineCompat.io().of(outputs));
        }

        /**
         * Builds and returns a new stream output channel generating the specified output.
         *
         * @param output the output.
         * @param <OUT>  the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        public <OUT> LoaderStreamChannelCompat<OUT> streamOf(@Nullable final OUT output) {

            return streamOf(JRoutineCompat.io().of(output));
        }

        /**
         * Builds and returns a new stream output channel generating the specified outputs.
         *
         * @param outputs the output data.
         * @param <OUT>   the output data type.
         * @return the newly created channel instance.
         */
        @NotNull
        public <OUT> LoaderStreamChannelCompat<OUT> streamOf(@Nullable final OUT... outputs) {

            return streamOf(JRoutineCompat.io().of(outputs));
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
        public <OUT> LoaderStreamChannelCompat<OUT> streamOf(
                @NotNull final OutputChannel<OUT> output) {

            return new DefaultLoaderStreamChannelCompat<OUT>(mContextBuilder, output);
        }
    }

    /**
     * Runnable binding two channels together.
     *
     * @param <OUT> the output data type.
     */
    private static class BindingRunnable<OUT> implements Runnable {

        private final IOChannel<OUT> mChannel;

        private final AtomicBoolean mIsBound = new AtomicBoolean();

        private final OutputChannel<OUT> mOutput;

        /**
         * Constructor.
         *
         * @param channel the I/O channel.
         * @param output  the output channel.
         */
        private BindingRunnable(@NotNull final IOChannel<OUT> channel,
                @NotNull final OutputChannel<OUT> output) {

            mChannel = channel;
            mOutput = output;
        }

        public void run() {

            if (!mIsBound.getAndSet(true)) {
                mOutput.passTo(mChannel).close();
            }
        }
    }
}
