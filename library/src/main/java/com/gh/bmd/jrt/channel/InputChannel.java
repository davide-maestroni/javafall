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
package com.gh.bmd.jrt.channel;

import com.gh.bmd.jrt.util.TimeDuration;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface defining an input channel.
 * <p/>
 * Note that the delivery order of the input data might not be guaranteed.
 * <p/>
 * Created by davide-maestroni on 9/4/14.
 *
 * @param <INPUT> the input data type.
 */
public interface InputChannel<INPUT> extends Channel {

    /**
     * Tells the channel to delay the transfer of data of the specified time duration.<br/>
     * Note that an abortion command will be delayed as well. Note, however, that a delayed abortion
     * will not prevent the invocation from completing, as input data do.
     *
     * @param delay the delay.
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     */
    @Nonnull
    InputChannel<INPUT> after(@Nonnull TimeDuration delay);

    /**
     * Tells the channel to delay the transfer of data of the specified time duration.<br/>
     * Note that an abortion command will be delayed as well. Note, however, that a delayed abortion
     * will not prevent the invocation from completing, as input data do.
     *
     * @param delay    the delay value.
     * @param timeUnit the delay time unit.
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalArgumentException      if the specified delay is negative.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     */
    @Nonnull
    InputChannel<INPUT> after(long delay, @Nonnull TimeUnit timeUnit);

    /**
     * Tells the channel to not delay the transfer of data.
     *
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     */
    @Nonnull
    InputChannel<INPUT> now();

    /**
     * Tells the channel to sort the passed input data based on the order of the calls to the pass
     * methods.
     * <p/>
     * By default no particular order is applied.
     *
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     * @see #orderByChance()
     * @see #orderByDelay()
     */
    @Nonnull
    InputChannel<INPUT> orderByCall();

    /**
     * Tells the channel to avoid sorting the passed input in any particular order.
     * <p/>
     * This is the default behavior.
     *
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     * @see #orderByCall()
     * @see #orderByDelay()
     */
    @Nonnull
    InputChannel<INPUT> orderByChance();

    /**
     * Tells the channel to sort the passed input data based on the specific delay.<br/>
     * Note that only the inputs passed with a 0 delay will be delivered in the same order as they
     * are passed to the channel, while the others will be delivered as soon as the dedicated runner
     * handles the specific execution.
     * <p/>
     * By default no particular order is applied.
     *
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     * @see #orderByCall()
     * @see #orderByChance()
     */
    @Nonnull
    InputChannel<INPUT> orderByDelay();

    /**
     * Passes the data returned by the specified channel to this one.
     * <p/>
     * Note that the output channel will be bound as a result of the call, thus effectively
     * preventing any other consumer from getting data from it.
     *
     * @param channel the output channel.
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     * @see com.gh.bmd.jrt.channel.OutputChannel#passTo(InputChannel)
     */
    @Nonnull
    InputChannel<INPUT> pass(@Nullable OutputChannel<? extends INPUT> channel);

    /**
     * Passes the data returned by the specified iterable to this channel.
     *
     * @param inputs the iterable returning the input data.
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     */
    @Nonnull
    InputChannel<INPUT> pass(@Nullable Iterable<? extends INPUT> inputs);

    /**
     * Passes the specified input to this channel.
     *
     * @param input the input.
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     */
    @Nonnull
    InputChannel<INPUT> pass(@Nullable INPUT input);

    /**
     * Passes the specified input data to this channel.
     *
     * @param inputs the input data.
     * @return this channel.
     * @throws com.gh.bmd.jrt.channel.RoutineException if the execution has been aborted.
     * @throws java.lang.IllegalStateException         if this channel is already closed.
     */
    @Nonnull
    InputChannel<INPUT> pass(@Nullable INPUT... inputs);
}
