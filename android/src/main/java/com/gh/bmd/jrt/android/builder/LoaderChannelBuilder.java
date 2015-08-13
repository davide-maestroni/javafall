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
package com.gh.bmd.jrt.android.builder;

import com.gh.bmd.jrt.android.builder.LoaderConfiguration.Builder;
import com.gh.bmd.jrt.builder.ConfigurableBuilder;
import com.gh.bmd.jrt.channel.OutputChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface defining a builder of output channels bound to loader invocations.<br/>
 * In order to be successfully bound, the specific routine invocation must have a user defined ID
 * and still be running (or cached) at the time of the channel creation.
 * <p/>
 * Created by davide-maestroni on 1/14/15.
 *
 * @see com.gh.bmd.jrt.android.builder.LoaderRoutineBuilder LoaderRoutineBuilder
 */
public interface LoaderChannelBuilder extends ConfigurableBuilder<LoaderChannelBuilder>,
        LoaderConfigurableBuilder<LoaderChannelBuilder> {

    /**
     * Builds and returns an output channel bound to the routine invocation.
     *
     * @return the newly created output channel.
     * @throws java.lang.IllegalArgumentException if the configured loader ID is equal to AUTO.
     */
    @Nonnull
    <OUTPUT> OutputChannel<OUTPUT> buildChannel();

    /**
     * Note that the clash resolution types will be ignored.
     *
     * @return the loader configuration builder.
     */
    @Nonnull
    Builder<? extends LoaderChannelBuilder> loaders();

    /**
     * Makes the builder destroy the cached invocation instances with the specified inputs.
     *
     * @param inputs the inputs.
     */
    void purge(@Nullable Iterable<?> inputs);

    /**
     * Makes the builder destroy all the cached invocation instances.
     */
    void purge();

    /**
     * Makes the builder destroy the cached invocation instances with the specified input.
     *
     * @param input the input.
     */
    void purge(@Nullable Object input);

    /**
     * Makes the builder destroy the cached invocation instances with the specified inputs.
     *
     * @param inputs the inputs.
     */
    void purge(@Nullable Object... inputs);
}