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

package com.github.dm.jrt.core.builder;

import com.github.dm.jrt.core.config.ChannelConfiguration.Builder;

import org.jetbrains.annotations.NotNull;

/**
 * Interface defining a configurable builder of channels.
 * <p>
 * Created by davide-maestroni on 08/18/2015.
 *
 * @param <TYPE> the builder type.
 */
public interface ChannelConfigurableBuilder<TYPE> {

    /**
     * Gets the channel configuration builder related to the channel builder instance.
     * <p>
     * Note that the configuration builder will be initialized with the current configuration.
     *
     * @return the channel configuration builder.
     */
    @NotNull
    Builder<? extends TYPE> getChannelConfiguration();
}
