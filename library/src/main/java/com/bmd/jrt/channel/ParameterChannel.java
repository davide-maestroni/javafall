/**
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
package com.bmd.jrt.channel;

import com.bmd.jrt.time.TimeDuration;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface defining a parameter input channel, that is the channel used to pass input data to the
 * routine invocation.
 * <p/>
 * Created by davide on 9/15/14.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
public interface ParameterChannel<INPUT, OUTPUT> extends InputChannel<INPUT> {

    @Nonnull
    @Override
    public ParameterChannel<INPUT, OUTPUT> after(@Nonnull TimeDuration delay);

    @Nonnull
    @Override
    public ParameterChannel<INPUT, OUTPUT> after(long delay, @Nonnull TimeUnit timeUnit);

    @Nonnull
    @Override
    public ParameterChannel<INPUT, OUTPUT> now();

    @Nonnull
    @Override
    public ParameterChannel<INPUT, OUTPUT> pass(@Nullable OutputChannel<INPUT> channel);

    @Nonnull
    @Override
    public ParameterChannel<INPUT, OUTPUT> pass(@Nullable Iterable<? extends INPUT> inputs);

    @Nonnull
    @Override
    public ParameterChannel<INPUT, OUTPUT> pass(@Nullable INPUT input);

    @Nonnull
    @Override
    public ParameterChannel<INPUT, OUTPUT> pass(@Nullable INPUT... inputs);

    /**
     * Closes the input channel and returns the output one.
     *
     * @return the routine output channel.
     * @throws IllegalStateException                  if this channel is already closed.
     * @throws com.bmd.jrt.common.InvocationException if the execution has been aborted with an
     *                                                exception.
     */
    @Nonnull
    public OutputChannel<OUTPUT> result();
}
