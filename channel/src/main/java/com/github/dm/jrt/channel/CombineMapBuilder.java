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

package com.github.dm.jrt.channel;

import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.config.ChannelConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Builder implementation returning a channel combining data from a map of channels.
 * <p>
 * Created by davide-maestroni on 02/26/2016.
 *
 * @param <IN> the input data type.
 */
class CombineMapBuilder<IN> extends AbstractBuilder<Channel<Selectable<? extends IN>, ?>> {

    private final HashMap<Integer, Channel<? extends IN, ?>> mChannelMap;

    /**
     * Constructor.
     *
     * @param channels the map of channels to combine.
     * @throws java.lang.IllegalArgumentException if the specified map is empty.
     * @throws java.lang.NullPointerException     if the specified map is null or contains a null
     *                                            object.
     */
    CombineMapBuilder(@NotNull final Map<Integer, ? extends Channel<? extends IN, ?>> channels) {
        if (channels.isEmpty()) {
            throw new IllegalArgumentException("the map of channels must not be empty");
        }

        final HashMap<Integer, Channel<? extends IN, ?>> channelMap =
                new HashMap<Integer, Channel<? extends IN, ?>>(channels);
        if (channelMap.containsValue(null)) {
            throw new NullPointerException("the map of channels must not contain null objects");
        }

        mChannelMap = channelMap;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    protected Channel<Selectable<? extends IN>, ?> build(
            @NotNull final ChannelConfiguration configuration) {
        final HashMap<Integer, Channel<? extends IN, ?>> channelMap = mChannelMap;
        final HashMap<Integer, Channel<IN, ?>> inputChannelMap =
                new HashMap<Integer, Channel<IN, ?>>(channelMap.size());
        for (final Entry<Integer, Channel<? extends IN, ?>> entry : channelMap.entrySet()) {
            final Channel<IN, IN> outputChannel = JRoutineCore.io()
                                                              .channelConfiguration()
                                                              .with(configuration)
                                                              .configured()
                                                              .buildChannel();
            outputChannel.bind((Channel<IN, ?>) entry.getValue());
            inputChannelMap.put(entry.getKey(), outputChannel);
        }

        final Channel<Selectable<? extends IN>, Selectable<? extends IN>> inputChannel =
                JRoutineCore.io()
                            .channelConfiguration()
                            .with(configuration)
                            .configured()
                            .buildChannel();
        return inputChannel.bind(new SortingMapChannelConsumer<IN>(inputChannelMap));
    }
}
