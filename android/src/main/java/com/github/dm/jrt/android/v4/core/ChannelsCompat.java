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

package com.github.dm.jrt.android.v4.core;

import android.support.v4.util.SparseArrayCompat;

import com.github.dm.jrt.android.core.Channels;
import com.github.dm.jrt.builder.ChannelConfiguration;
import com.github.dm.jrt.channel.Channel.InputChannel;
import com.github.dm.jrt.channel.Channel.OutputChannel;
import com.github.dm.jrt.channel.IOChannel;
import com.github.dm.jrt.channel.OutputConsumer;
import com.github.dm.jrt.common.RoutineException;
import com.github.dm.jrt.util.WeakIdentityHashMap;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Utility class for handling routine channels.
 * <p/>
 * Created by davide-maestroni on 08/03/2015.
 */
public class ChannelsCompat extends Channels {

    private static final WeakIdentityHashMap<InputChannel<?>, HashMap<SelectInfo,
            SparseArrayCompat<IOChannel<?>>>>
            sInputChannels =
            new WeakIdentityHashMap<InputChannel<?>, HashMap<SelectInfo,
                    SparseArrayCompat<IOChannel<?>>>>();

    /**
     * Avoid direct instantiation.
     */
    protected ChannelsCompat() {

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
            @NotNull final SparseArrayCompat<? extends InputChannel<? extends IN>> channels) {

        final int size = channels.size();
        if (size == 0) {
            throw new IllegalArgumentException("the map of channels must not be empty");
        }

        if (channels.indexOfValue(null) >= 0) {
            throw new NullPointerException("the map of channels must not contain null objects");
        }

        final SparseArrayCompat<? extends InputChannel<? extends IN>> channelMap = channels.clone();
        return new AbstractBuilder<IOChannel<Selectable<? extends IN>>>() {

            @NotNull
            @Override
            protected IOChannel<Selectable<? extends IN>> build(
                    @NotNull final ChannelConfiguration configuration) {

                final SparseArrayCompat<IOChannel<?>> ioChannelMap =
                        new SparseArrayCompat<IOChannel<?>>(size);
                for (int i = 0; i < size; ++i) {
                    final IOChannel<?> ioChannel = JRoutineCompat.io().buildChannel();
                    ioChannel.passTo(((InputChannel<Object>) channelMap.valueAt(i)));
                    ioChannelMap.put(channelMap.keyAt(i), ioChannel);
                }

                final IOChannel<Selectable<? extends IN>> ioChannel = JRoutineCompat.io()
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
            @NotNull final SparseArrayCompat<? extends OutputChannel<? extends OUT>> channels) {

        final int size = channels.size();
        if (size == 0) {
            throw new IllegalArgumentException("the map of channels must not be empty");
        }

        if (channels.indexOfValue(null) >= 0) {
            throw new NullPointerException("the map of channels must not contain null objects");
        }

        final SparseArrayCompat<? extends OutputChannel<? extends OUT>> channelMap =
                channels.clone();
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
     * Returns a builder of maps of input channels accepting the data identified by the specified
     * indexes.<br/>
     * Note that the builder will return the same map for the same inputs and equal configuration,
     * and that the returned channels <b>must be explicitly closed</b> in order to ensure the
     * completion of the invocation lifecycle.
     *
     * @param channel the selectable channel.
     * @param indexes the array of indexes.
     * @param <DATA>  the channel data type.
     * @param <IN>    the input data type.
     * @return the map of indexes and I/O channels builder.
     * @see com.github.dm.jrt.core.Channels#select(InputChannel, int...)
     */
    @NotNull
    public static <DATA, IN extends DATA> Builder<? extends SparseArrayCompat<IOChannel<IN>>>
    selectParcelable(
            @NotNull final InputChannel<? super ParcelableSelectable<DATA>> channel,
            @NotNull final int... indexes) {

        final HashSet<Integer> indexSet = new HashSet<Integer>();
        for (final int index : indexes) {
            indexSet.add(index);
        }

        return new InputMapBuilder<DATA, IN>(channel, indexSet);
    }

    /**
     * Returns a builder of maps of input channels accepting the data identified by the specified
     * indexes.<br/>
     * Note that the builder will return the same map for the same inputs and equal configuration,
     * and that the returned channels <b>must be explicitly closed</b> in order to ensure the
     * completion of the invocation lifecycle.
     *
     * @param channel the selectable channel.
     * @param indexes the iterable returning the channel indexes.
     * @param <DATA>  the channel data type.
     * @param <IN>    the input data type.
     * @return the map of indexes and I/O channels builder.
     * @see com.github.dm.jrt.core.Channels#select(InputChannel, Iterable)
     */
    @NotNull
    public static <DATA, IN extends DATA> Builder<? extends SparseArrayCompat<IOChannel<IN>>>
    selectParcelable(
            @NotNull final InputChannel<? super ParcelableSelectable<DATA>> channel,
            @NotNull final Iterable<Integer> indexes) {

        final HashSet<Integer> indexSet = new HashSet<Integer>();
        for (final Integer index : indexes) {
            if (index == null) {
                throw new NullPointerException(
                        "the iterable of indexes must not return a null object");
            }

            indexSet.add(index);
        }

        return new InputMapBuilder<DATA, IN>(channel, indexSet);
    }

    /**
     * Returns a builder of maps of input channels accepting the data identified by the specified
     * indexes.<br/>
     * Note that the builder will return the same map for the same inputs and equal configuration,
     * and that the returned channels <b>must be explicitly closed</b> in order to ensure the
     * completion of the invocation lifecycle.
     *
     * @param startIndex the selectable start index.
     * @param rangeSize  the size of the range of indexes (must be positive).
     * @param channel    the selectable channel.
     * @param <DATA>     the channel data type.
     * @param <IN>       the input data type.
     * @return the map of indexes and I/O channels  builder.
     * @throws java.lang.IllegalArgumentException if the specified range size is negative or 0.
     * @see com.github.dm.jrt.core.Channels#select(int, int, InputChannel)
     */
    @NotNull
    public static <DATA, IN extends DATA> Builder<? extends SparseArrayCompat<IOChannel<IN>>>
    selectParcelable(
            final int startIndex, final int rangeSize,
            @NotNull final InputChannel<? super ParcelableSelectable<DATA>> channel) {

        if (rangeSize <= 0) {
            throw new IllegalArgumentException("invalid range size: " + rangeSize);
        }

        final HashSet<Integer> indexSet = new HashSet<Integer>();
        final int endIndex = startIndex + rangeSize;
        if (endIndex <= 0) {
            throw new IllegalArgumentException("range overflow: " + startIndex + "..." + endIndex);
        }

        for (int i = startIndex; i < endIndex; i++) {
            indexSet.add(i);
        }

        return new InputMapBuilder<DATA, IN>(channel, indexSet);
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
    public static <OUT> SparseArrayCompat<OutputChannel<OUT>> selectParcelable(final int startIndex,
            final int rangeSize,
            @NotNull final OutputChannel<? extends ParcelableSelectable<? extends OUT>> channel) {

        if (rangeSize <= 0) {
            throw new IllegalArgumentException("invalid range size: " + rangeSize);
        }

        final SparseArrayCompat<IOChannel<OUT>> inputMap =
                new SparseArrayCompat<IOChannel<OUT>>(rangeSize);
        final SparseArrayCompat<OutputChannel<OUT>> outputMap =
                new SparseArrayCompat<OutputChannel<OUT>>(rangeSize);
        for (int index = startIndex; index < rangeSize; index++) {
            final Integer integer = index;
            final IOChannel<OUT> ioChannel = JRoutineCompat.io().buildChannel();
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
    public static <OUT> SparseArrayCompat<OutputChannel<OUT>> selectParcelable(
            @NotNull final OutputChannel<? extends ParcelableSelectable<? extends OUT>> channel,
            @NotNull final int... indexes) {

        final int size = indexes.length;
        final SparseArrayCompat<IOChannel<OUT>> inputMap =
                new SparseArrayCompat<IOChannel<OUT>>(size);
        final SparseArrayCompat<OutputChannel<OUT>> outputMap =
                new SparseArrayCompat<OutputChannel<OUT>>(size);
        for (final Integer index : indexes) {
            final IOChannel<OUT> ioChannel = JRoutineCompat.io().buildChannel();
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
    public static <OUT> SparseArrayCompat<OutputChannel<OUT>> selectParcelable(
            @NotNull final OutputChannel<? extends ParcelableSelectable<? extends OUT>> channel,
            @NotNull final Iterable<Integer> indexes) {

        final SparseArrayCompat<IOChannel<OUT>> inputMap = new SparseArrayCompat<IOChannel<OUT>>();
        final SparseArrayCompat<OutputChannel<OUT>> outputMap =
                new SparseArrayCompat<OutputChannel<OUT>>();
        for (final Integer index : indexes) {
            final IOChannel<OUT> ioChannel = JRoutineCompat.io().buildChannel();
            inputMap.put(index, ioChannel);
            outputMap.put(index, ioChannel);
        }

        channel.passTo(new SortingMapOutputConsumer<OUT>(inputMap));
        return outputMap;
    }

    // TODO: 20/02/16 javadoc
    private static class InputMapBuilder<DATA, IN extends DATA>
            extends AbstractBuilder<SparseArrayCompat<IOChannel<IN>>> {

        private final InputChannel<? super ParcelableSelectable<DATA>> mChannel;

        private final HashSet<Integer> mIndexes;

        private InputMapBuilder(
                @NotNull final InputChannel<? super ParcelableSelectable<DATA>> channel,
                @NotNull final HashSet<Integer> indexes) {

            mChannel = channel;
            mIndexes = indexes;
        }

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        protected SparseArrayCompat<IOChannel<IN>> build(
                @NotNull final ChannelConfiguration configuration) {

            final HashSet<Integer> indexes = mIndexes;
            final InputChannel<? super ParcelableSelectable<DATA>> channel = mChannel;
            synchronized (sInputChannels) {
                final WeakIdentityHashMap<InputChannel<?>, HashMap<SelectInfo,
                        SparseArrayCompat<IOChannel<?>>>>
                        inputChannels = sInputChannels;
                HashMap<SelectInfo, SparseArrayCompat<IOChannel<?>>> channelMaps =
                        inputChannels.get(channel);
                if (channelMaps == null) {
                    channelMaps = new HashMap<SelectInfo, SparseArrayCompat<IOChannel<?>>>();
                    inputChannels.put(channel, channelMaps);
                }

                final SelectInfo selectInfo = new SelectInfo(configuration, indexes);
                final SparseArrayCompat<IOChannel<IN>> channelMap =
                        new SparseArrayCompat<IOChannel<IN>>(indexes.size());
                SparseArrayCompat<IOChannel<?>> channels = channelMaps.get(selectInfo);
                if (channels != null) {
                    final int size = channels.size();
                    for (int i = 0; i < size; i++) {
                        channelMap.append(channels.keyAt(i), (IOChannel<IN>) channels.valueAt(i));
                    }

                } else {
                    channels = new SparseArrayCompat<IOChannel<?>>(indexes.size());
                    for (final Integer index : indexes) {
                        final IOChannel<IN> ioChannel =
                                Channels.<DATA, IN>selectParcelable(channel, index)
                                        .withChannels()
                                        .with(configuration)
                                        .configured()
                                        .build();
                        channelMap.put(index, ioChannel);
                        channels.put(index, ioChannel);
                    }

                    channelMaps.put(selectInfo, channels);
                }

                return channelMap;
            }
        }
    }

    // TODO: 2/19/16 javadoc
    private static class SelectInfo {

        private final ChannelConfiguration mConfiguration;

        private final HashSet<Integer> mIndexes;

        private SelectInfo(@NotNull final ChannelConfiguration configuration,
                @NotNull final HashSet<Integer> indexes) {

            mConfiguration = configuration;
            mIndexes = indexes;
        }

        @Override
        public boolean equals(final Object o) {

            // AUTO-GENERATED CODE
            if (this == o) {
                return true;
            }

            if (!(o instanceof SelectInfo)) {
                return false;
            }

            final SelectInfo that = (SelectInfo) o;
            return mConfiguration.equals(that.mConfiguration) && mIndexes.equals(that.mIndexes);
        }

        @Override
        public int hashCode() {

            // AUTO-GENERATED CODE
            int result = mConfiguration.hashCode();
            result = 31 * result + mIndexes.hashCode();
            return result;
        }
    }

    /**
     * Output consumer sorting the output data among a map of channels.
     *
     * @param <OUT> the output data type.
     */
    private static class SortingMapOutputConsumer<OUT>
            implements OutputConsumer<ParcelableSelectable<? extends OUT>> {

        private final SparseArrayCompat<IOChannel<OUT>> mChannels;

        /**
         * Constructor.
         *
         * @param channels the map of indexes and I/O channels.
         */
        private SortingMapOutputConsumer(
                @NotNull final SparseArrayCompat<IOChannel<OUT>> channels) {

            mChannels = channels;
        }

        public void onComplete() {

            final SparseArrayCompat<IOChannel<OUT>> channels = mChannels;
            final int size = channels.size();
            for (int i = 0; i < size; ++i) {
                channels.valueAt(i).close();
            }
        }

        public void onError(@NotNull final RoutineException error) {

            final SparseArrayCompat<IOChannel<OUT>> channels = mChannels;
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
