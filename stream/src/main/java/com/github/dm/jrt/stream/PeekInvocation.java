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

package com.github.dm.jrt.stream;

import com.github.dm.jrt.core.channel.ResultChannel;
import com.github.dm.jrt.core.invocation.MappingInvocation;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.function.ConsumerWrapper;

import org.jetbrains.annotations.NotNull;

import static com.github.dm.jrt.core.util.Reflection.asArgs;

/**
 * Mapping invocation peeking output data as they are passed along.
 * <p>
 * Created by davide-maestroni on 04/21/2016.
 *
 * @param <DATA> the data type.
 */
class PeekInvocation<DATA> extends MappingInvocation<DATA, DATA> {

    private final ConsumerWrapper<? super DATA> mPeekConsumer;

    /**
     * Constructor.
     *
     * @param peekConsumer the consumer instance.
     */
    PeekInvocation(@NotNull final ConsumerWrapper<? super DATA> peekConsumer) {
        super(asArgs(ConstantConditions.notNull("consumer instance", peekConsumer)));
        mPeekConsumer = peekConsumer;
    }

    public void onInput(final DATA input, @NotNull final ResultChannel<DATA> result) throws
            Exception {
        mPeekConsumer.accept(input);
        result.pass(input);
    }
}
