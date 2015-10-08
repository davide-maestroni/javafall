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
package com.github.dm.jrt.builder;

import com.github.dm.jrt.channel.IOChannel;

import org.jetbrains.annotations.NotNull;

/**
 * Interface defining a builder of I/O channel objects.
 * <p/>
 * Created by davide-maestroni on 03/07/2015.
 */
public interface IOChannelBuilder extends ConfigurableChannelBuilder<IOChannelBuilder> {

    /**
     * Builds and returns the I/O channel instance.
     *
     * @param <DATA> the data type.
     * @return the newly created channel.
     */
    @NotNull
    <DATA> IOChannel<DATA, DATA> buildChannel();
}