/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh.bmd.jrt.channel;

import com.gh.bmd.jrt.time.TimeDuration;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface defining a standalone channel.
 * <p/>
 * An standalone channel is useful to make other asynchronous tasks communicate with a routine.<br/>
 * The channel output can be passed to a routine input channel in order to feed it with data coming
 * asynchronously from another source. Note however, that in both cases the
 * <b><code>close()</code></b> method must be called to correctly terminate the invocation
 * lifecycle.
 * <p/>
 * Created by davide on 10/25/14.
 *
 * @param <DATA> the data type.
 */
public interface StandaloneChannel<DATA> {

    /**
     * Returns the input end of this channel.
     *
     * @return the input channel.
     */
    @Nonnull
    StandaloneInput<DATA> input();

    /**
     * Returns the output end of this channel.
     *
     * @return the output channel.
     */
    @Nonnull
    StandaloneOutput<DATA> output();

    /**
     * Interface defining a standalone channel input.
     *
     * @param <INPUT> the input data type.
     */
    interface StandaloneInput<INPUT> extends InputChannel<INPUT> {

        @Nonnull
        StandaloneInput<INPUT> after(@Nonnull TimeDuration delay);

        @Nonnull
        StandaloneInput<INPUT> after(long delay, @Nonnull TimeUnit timeUnit);

        @Nonnull
        StandaloneInput<INPUT> now();

        @Nonnull
        StandaloneInput<INPUT> pass(@Nullable OutputChannel<? extends INPUT> channel);

        @Nonnull
        StandaloneInput<INPUT> pass(@Nullable Iterable<? extends INPUT> inputs);

        @Nonnull
        StandaloneInput<INPUT> pass(@Nullable INPUT input);

        @Nonnull
        StandaloneInput<INPUT> pass(@Nullable INPUT... inputs);

        /**
         * Closes the channel input.<br/>
         * If the channel is already close, this method has no effect.
         * <p/>
         * Note that this method must be always called when done with the channel.
         */
        void close();
    }

    /**
     * Interface defining a standalone channel output.
     *
     * @param <OUTPUT> the output data type.
     */
    interface StandaloneOutput<OUTPUT> extends OutputChannel<OUTPUT> {

        @Nonnull
        StandaloneOutput<OUTPUT> afterMax(@Nonnull TimeDuration timeout);

        @Nonnull
        StandaloneOutput<OUTPUT> afterMax(long timeout, @Nonnull TimeUnit timeUnit);

        @Nonnull
        StandaloneOutput<OUTPUT> bind(@Nonnull OutputConsumer<? super OUTPUT> consumer);

        @Nonnull
        StandaloneOutput<OUTPUT> eventually();

        @Nonnull
        StandaloneOutput<OUTPUT> eventuallyAbort();

        @Nonnull
        StandaloneOutput<OUTPUT> eventuallyDeadlock();

        @Nonnull
        StandaloneOutput<OUTPUT> eventuallyExit();

        @Nonnull
        StandaloneOutput<OUTPUT> immediately();

        @Nonnull
        StandaloneOutput<OUTPUT> readAllInto(@Nonnull Collection<? super OUTPUT> results);

        @Nonnull
        StandaloneOutput<OUTPUT> unbind(@Nullable OutputConsumer<? super OUTPUT> consumer);
    }
}
