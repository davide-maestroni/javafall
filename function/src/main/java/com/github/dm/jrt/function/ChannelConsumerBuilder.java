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

package com.github.dm.jrt.function;

import com.github.dm.jrt.core.channel.ChannelConsumer;
import com.github.dm.jrt.core.error.RoutineException;

import org.jetbrains.annotations.NotNull;

import static com.github.dm.jrt.function.ConsumerWrapper.wrap;

/**
 * Utility class used to build channel consumers based on consumer functions.
 * <p>
 * Created by davide-maestroni on 09/21/2015.
 *
 * @param <OUT> the output data type.
 */
public class ChannelConsumerBuilder<OUT> implements ChannelConsumer<OUT> {

    private final ActionWrapper mOnComplete;

    private final ConsumerWrapper<RoutineException> mOnError;

    private final ConsumerWrapper<OUT> mOnOutput;

    /**
     * Constructor.
     *
     * @param onComplete the complete action.
     * @param onError    the error consumer.
     * @param onOutput   the output consumer.
     */
    @SuppressWarnings("unchecked")
    ChannelConsumerBuilder(@NotNull final Action onComplete,
            @NotNull final Consumer<? super RoutineException> onError,
            @NotNull final Consumer<? super OUT> onOutput) {
        mOnOutput = (ConsumerWrapper<OUT>) wrap(onOutput);
        mOnError = (ConsumerWrapper<RoutineException>) wrap(onError);
        mOnComplete = ActionWrapper.wrap(onComplete);
    }

    public void onComplete() throws Exception {
        mOnComplete.perform();
    }

    public void onError(@NotNull final RoutineException error) throws Exception {
        mOnError.accept(error);
    }

    public void onOutput(final OUT output) throws Exception {
        mOnOutput.accept(output);
    }

    /**
     * Returns a new channel consumer builder employing also the specified consumer function to
     * handle the invocation completion.
     *
     * @param onComplete the action instance.
     * @return the builder instance.
     */
    @NotNull
    public ChannelConsumerBuilder<OUT> thenComplete(@NotNull final Action onComplete) {
        return new ChannelConsumerBuilder<OUT>(mOnComplete.andThen(onComplete), mOnError,
                mOnOutput);
    }

    /**
     * Returns a new channel consumer builder employing also the specified consumer function to
     * handle the invocation errors.
     *
     * @param onError the consumer function.
     * @return the builder instance.
     */
    @NotNull
    public ChannelConsumerBuilder<OUT> thenError(
            @NotNull final Consumer<? super RoutineException> onError) {
        return new ChannelConsumerBuilder<OUT>(mOnComplete, mOnError.andThen(onError), mOnOutput);
    }

    /**
     * Returns a new channel consumer builder employing also the specified consumer function to
     * handle the invocation outputs.
     *
     * @param onOutput the consumer function.
     * @return the builder instance.
     */
    @NotNull
    public ChannelConsumerBuilder<OUT> thenOutput(@NotNull final Consumer<? super OUT> onOutput) {
        return new ChannelConsumerBuilder<OUT>(mOnComplete, mOnError, mOnOutput.andThen(onOutput));
    }
}
