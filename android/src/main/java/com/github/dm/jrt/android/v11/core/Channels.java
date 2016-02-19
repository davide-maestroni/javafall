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

import android.util.SparseArray;

import com.github.dm.jrt.android.v4.core.JRoutineCompat;
import com.github.dm.jrt.builder.ChannelConfiguration;
import com.github.dm.jrt.channel.Channel.InputChannel;
import com.github.dm.jrt.channel.Channel.OutputChannel;
import com.github.dm.jrt.channel.IOChannel;
import com.github.dm.jrt.channel.OutputConsumer;
import com.github.dm.jrt.common.RoutineException;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Utility class for handling routine channels.
 * <p/>
 * Created by davide-maestroni on 08/03/2015.
 */
public class Channels extends com.github.dm.jrt.android.core.Channels {

    /**
     * Avoid direct instantiation.
     */
    protected Channels() {

    }

    /**
     * Returns a builder of input channels combining the specified channels into a selectable one.
     * The selectable indexes will be the keys of the specified map.<br/>
     * Note that the builder will successfully create only one input channel instance, and that the
     * returned channel <b>must be explicitly closed</b> in order to ensure the completion of the
     * invocation lifecycle.
     *
     * @param channels the map of indexes and input channels.
     * @param <IN>     the input data type.
     * @return the selectable I/O channel builder.
     * @throws java.lang.IllegalArgumentException if the specified map is empty.
     * @see com.github.dm.jrt.core.Channels#combine(Map)
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <IN> Builder<? extends IOChannel<Selectable<? extends IN>>> combine(
            @NotNull final SparseArray<? extends InputChannel<? extends IN>> channels) {

        final int size = channels.size();
        if (size == 0) {
            throw new IllegalArgumentException("the map of channels must not be empty");
        }

        if (channels.indexOfValue(null) >= 0) {
            throw new NullPointerException("the map of channels must not contain null objects");
        }

        final SparseArray<? extends InputChannel<? extends IN>> channelMap = channels.clone();
        return new AbstractBuilder<IOChannel<Selectable<? extends IN>>>() {

            @NotNull
            @Override
            protected IOChannel<Selectable<? extends IN>> build(
                    @NotNull final ChannelConfiguration configuration) {

                final SparseArray<IOChannel<?>> ioChannelMap = new SparseArray<IOChannel<?>>(size);
                for (int i = 0; i < size; ++i) {
                    final IOChannel<?> ioChannel = JRoutine.io().buildChannel();
                    ioChannel.passTo(((InputChannel<Object>) channelMap.valueAt(i)));
                    ioChannelMap.put(channelMap.keyAt(i), ioChannel);
                }

                final IOChannel<Selectable<? extends IN>> ioChannel = JRoutine.io()
                                                                              .withChannels()
                                                                              .with(configuration)
                                                                              .configured()
                                                                              .buildChannel();
                ioChannel.passTo(new SortingMapOutputConsumer(ioChannelMap));
                return ioChannel;
            }
        };
    }

    /**
     * Returns a builder of output channels merging the specified channels into a selectable one.
     * The selectable indexes will be the keys of the specified sparse array.<br/>
     * Note that the builder will successfully create only one output channel instance, and that the
     * passed channels will be bound as a result of the creation.
     *
     * @param channels the map of indexes and output channels.
     * @param <OUT>    the output data type.
     * @return the selectable output channel builder.
     * @throws java.lang.IllegalArgumentException if the specified map is empty.
     * @see com.github.dm.jrt.core.Channels#merge(Map)
     */
    @NotNull
    public static <OUT> Builder<? extends OutputChannel<? extends ParcelableSelectable<OUT>>> merge(
            @NotNull final SparseArray<? extends OutputChannel<? extends OUT>> channels) {

        final int size = channels.size();
        if (size == 0) {
            throw new IllegalArgumentException("the map of channels must not be empty");
        }

        if (channels.indexOfValue(null) >= 0) {
            throw new NullPointerException("the map of channels must not contain null objects");
        }

        final SparseArray<? extends OutputChannel<? extends OUT>> channelMap = channels.clone();
        return new AbstractBuilder<OutputChannel<ParcelableSelectable<OUT>>>() {

            @NotNull
            @Override
            protected OutputChannel<ParcelableSelectable<OUT>> build(
                    @NotNull final ChannelConfiguration configuration) {

                final IOChannel<ParcelableSelectable<OUT>> ioChannel = JRoutineCompat.io()
                                                                                     .withChannels()
                                                                                     .with(configuration)
                                                                                     .configured()
                                                                                     .buildChannel();
                for (int i = 0; i < size; ++i) {
                    ioChannel.pass(toSelectable(channelMap.valueAt(i), channelMap.keyAt(i)));
                }

                return ioChannel.close();
            }
        };
    }

    /**
     * Returns a map of input channels accepting the input data identified by the specified indexes.
     * <br/>
     * Note that the returned channels <b>must be explicitly closed</b> in order to ensure the
     * completion of the invocation lifecycle.
     *
     * @param channel the selectable channel.
     * @param indexes the array of indexes.
     * @param <DATA>  the channel data type.
     * @param <IN>    the input data type.
     * @return the map of indexes and I/O channels.
     * @see com.github.dm.jrt.core.Channels#select(InputChannel, int...)
     */
    @NotNull
    public static <DATA, IN extends DATA> SparseArray<IOChannel<IN>> selectParcelable(
            @NotNull final InputChannel<? super ParcelableSelectable<DATA>> channel,
            @NotNull final int... indexes) {

        final int size = indexes.length;
        final SparseArray<IOChannel<IN>> channelMap = new SparseArray<IOChannel<IN>>(size);
        for (final int index : indexes) {
            channelMap.append(index, Channels.<DATA, IN>selectParcelable(channel, index));
        }

        return channelMap;
    }

    /**
     * Returns a map of input channels accepting the input data identified by the specified indexes.
     * <br/>
     * Note that the returned channels <b>must be explicitly closed</b> in order to ensure the
     * completion of the invocation lifecycle.
     *
     * @param channel the selectable channel.
     * @param indexes the iterable returning the channel indexes.
     * @param <DATA>  the channel data type.
     * @param <IN>    the input data type.
     * @return the map of indexes and I/O channels.
     * @see com.github.dm.jrt.core.Channels#select(InputChannel, Iterable)
     */
    @NotNull
    public static <DATA, IN extends DATA> SparseArray<IOChannel<IN>> selectParcelable(
            @NotNull final InputChannel<? super ParcelableSelectable<DATA>> channel,
            @NotNull final Iterable<Integer> indexes) {

        final SparseArray<IOChannel<IN>> channelMap = new SparseArray<IOChannel<IN>>();
        for (final Integer index : indexes) {
            channelMap.append(index, Channels.<DATA, IN>selectParcelable(channel, index));
        }

        return channelMap;
    }

    /**
     * Returns a map of input channels accepting the input data identified by the specified indexes.
     * <br/>
     * Note that the returned channels <b>must be explicitly closed</b> in order to ensure the
     * completion of the invocation lifecycle.
     *
     * @param startIndex the selectable start index.
     * @param rangeSize  the size of the range of indexes (must be positive).
     * @param channel    the selectable channel.
     * @param <DATA>     the channel data type.
     * @param <IN>       the input data type.
     * @return the map of indexes and I/O channels.
     * @throws java.lang.IllegalArgumentException if the specified range size is negative or 0.
     * @see com.github.dm.jrt.core.Channels#select(int, int, InputChannel)
     */
    @NotNull
    public static <DATA, IN extends DATA> SparseArray<IOChannel<IN>> selectParcelable(
            final int startIndex, final int rangeSize,
            @NotNull final InputChannel<? super ParcelableSelectable<DATA>> channel) {

        if (rangeSize <= 0) {
            throw new IllegalArgumentException("invalid range size: " + rangeSize);
        }

        final SparseArray<IOChannel<IN>> channelMap = new SparseArray<IOChannel<IN>>(rangeSize);
        for (int index = startIndex; index < rangeSize; index++) {
            channelMap.append(index, Channels.<DATA, IN>selectParcelable(channel, index));
        }

        return channelMap;
    }

    /**
     * Returns a map of output channels returning the output data filtered by the specified indexes.
     * <br/>
     * Note that the passed channel will be bound as a result of the call.
     *
     * @param startIndex the selectable start index.
     * @param rangeSize  the size of the range of indexes (must be positive).
     * @param channel    the selectable channel.
     * @param <OUT>      the output data type.
     * @return the map of indexes and output channels.
     * @throws java.lang.IllegalArgumentException if the specified range size is negative or 0.
     * @see com.github.dm.jrt.core.Channels#select(int, int, OutputChannel)
     */
    @NotNull
    public static <OUT> SparseArray<OutputChannel<OUT>> selectParcelable(final int startIndex,
            final int rangeSize,
            @NotNull final OutputChannel<? extends ParcelableSelectable<? extends OUT>> channel) {

        if (rangeSize <= 0) {
            throw new IllegalArgumentException("invalid range size: " + rangeSize);
        }

        final SparseArray<IOChannel<OUT>> inputMap = new SparseArray<IOChannel<OUT>>(rangeSize);
        final SparseArray<OutputChannel<OUT>> outputMap =
                new SparseArray<OutputChannel<OUT>>(rangeSize);
        for (int index = startIndex; index < rangeSize; index++) {
            final Integer integer = index;
            final IOChannel<OUT> ioChannel = JRoutine.io().buildChannel();
            inputMap.put(integer, ioChannel);
            outputMap.put(integer, ioChannel);
        }

        channel.passTo(new SortingMapOutputConsumer<OUT>(inputMap));
        return outputMap;
    }

    /**
     * Returns a map of output channels returning the outputs filtered by the specified indexes.
     * <br/>
     * Note that the passed channel will be bound as a result of the call.
     *
     * @param channel the selectable output channel.
     * @param indexes the list of indexes.
     * @param <OUT>   the output data type.
     * @return the map of indexes and output channels.
     * @see com.github.dm.jrt.core.Channels#select(OutputChannel, int...)
     */
    @NotNull
    public static <OUT> SparseArray<OutputChannel<OUT>> selectParcelable(
            @NotNull final OutputChannel<? extends ParcelableSelectable<? extends OUT>> channel,
            @NotNull final int... indexes) {

        final int size = indexes.length;
        final SparseArray<IOChannel<OUT>> inputMap = new SparseArray<IOChannel<OUT>>(size);
        final SparseArray<OutputChannel<OUT>> outputMap = new SparseArray<OutputChannel<OUT>>(size);
        for (final Integer index : indexes) {
            final IOChannel<OUT> ioChannel = JRoutine.io().buildChannel();
            inputMap.put(index, ioChannel);
            outputMap.put(index, ioChannel);
        }

        channel.passTo(new SortingMapOutputConsumer<OUT>(inputMap));
        return outputMap;
    }

    /**
     * Returns a map of output channels returning the output data filtered by the specified indexes.
     * <br/>
     * Note that the passed channel will be bound as a result of the call.
     *
     * @param channel the selectable output channel.
     * @param indexes the iterable returning the channel indexes.
     * @param <OUT>   the output data type.
     * @return the map of indexes and output channels.
     * @see com.github.dm.jrt.core.Channels#select(OutputChannel, Iterable)
     */
    @NotNull
    public static <OUT> SparseArray<OutputChannel<OUT>> selectParcelable(
            @NotNull final OutputChannel<? extends ParcelableSelectable<? extends OUT>> channel,
            @NotNull final Iterable<Integer> indexes) {

        final SparseArray<IOChannel<OUT>> inputMap = new SparseArray<IOChannel<OUT>>();
        final SparseArray<OutputChannel<OUT>> outputMap = new SparseArray<OutputChannel<OUT>>();
        for (final Integer index : indexes) {
            final IOChannel<OUT> ioChannel = JRoutine.io().buildChannel();
            inputMap.put(index, ioChannel);
            outputMap.put(index, ioChannel);
        }

        channel.passTo(new SortingMapOutputConsumer<OUT>(inputMap));
        return outputMap;
    }

    /**
     * Output consumer sorting the output data among a map of channels.
     *
     * @param <OUT> the output data type.
     */
    private static class SortingMapOutputConsumer<OUT>
            implements OutputConsumer<ParcelableSelectable<? extends OUT>> {

        private final SparseArray<IOChannel<OUT>> mChannels;

        /**
         * Constructor.
         *
         * @param channels the map of indexes and I/O channels.
         */
        private SortingMapOutputConsumer(@NotNull final SparseArray<IOChannel<OUT>> channels) {

            mChannels = channels;
        }

        public void onComplete() {

            final SparseArray<IOChannel<OUT>> channels = mChannels;
            final int size = channels.size();
            for (int i = 0; i < size; ++i) {
                channels.valueAt(i).close();
            }
        }

        public void onError(@NotNull final RoutineException error) {

            final SparseArray<IOChannel<OUT>> channels = mChannels;
            final int size = channels.size();
            for (int i = 0; i < size; ++i) {
                channels.valueAt(i).abort(error);
            }
        }

        public void onOutput(final ParcelableSelectable<? extends OUT> selectable) {

            final IOChannel<OUT> channel = mChannels.get(selectable.index);
            if (channel != null) {
                channel.pass(selectable.data);
            }
        }
    }
}
