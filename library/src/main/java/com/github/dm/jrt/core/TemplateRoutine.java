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
package com.github.dm.jrt.core;

import com.github.dm.jrt.builder.ChannelConfiguration;
import com.github.dm.jrt.builder.InvocationConfiguration;
import com.github.dm.jrt.channel.OutputChannel;
import com.github.dm.jrt.channel.StreamingChannel;
import com.github.dm.jrt.routine.Routine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.dm.jrt.builder.ChannelConfiguration.DEFAULT;
import static com.github.dm.jrt.builder.ChannelConfiguration.builder;

/**
 * Empty abstract implementation of a routine.
 * <p/>
 * This class is useful to avoid the need of implementing some of the methods defined in the
 * interface.
 * <p/>
 * Created by davide-maestroni on 10/17/2014.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
public abstract class TemplateRoutine<IN, OUT> implements Routine<IN, OUT> {

    @NotNull
    public OutputChannel<OUT> asyncCall() {

        return asyncInvoke().result();
    }

    @NotNull
    public OutputChannel<OUT> asyncCall(@Nullable final IN input) {

        return asyncInvoke().pass(input).result();
    }

    @NotNull
    public OutputChannel<OUT> asyncCall(@Nullable final IN... inputs) {

        return asyncInvoke().pass(inputs).result();
    }

    @NotNull
    public OutputChannel<OUT> asyncCall(@Nullable final Iterable<? extends IN> inputs) {

        return asyncInvoke().pass(inputs).result();
    }

    @NotNull
    public OutputChannel<OUT> asyncCall(@Nullable final OutputChannel<? extends IN> inputs) {

        return asyncInvoke().pass(inputs).result();
    }

    @NotNull
    public StreamingChannel<IN, OUT> asyncStream() {

        final DefaultTransportChannel<IN> transportChannel =
                new DefaultTransportChannel<IN>(buildChannelConfiguration());
        return new DefaultStreamingChannel<IN, OUT>(transportChannel, asyncCall(transportChannel));
    }

    @NotNull
    public OutputChannel<OUT> parallelCall() {

        return parallelInvoke().result();
    }

    @NotNull
    public OutputChannel<OUT> parallelCall(@Nullable final IN input) {

        return parallelInvoke().pass(input).result();
    }

    @NotNull
    public OutputChannel<OUT> parallelCall(@Nullable final IN... inputs) {

        return parallelInvoke().pass(inputs).result();
    }

    @NotNull
    public OutputChannel<OUT> parallelCall(@Nullable final Iterable<? extends IN> inputs) {

        return parallelInvoke().pass(inputs).result();
    }

    @NotNull
    public OutputChannel<OUT> parallelCall(@Nullable final OutputChannel<? extends IN> inputs) {

        return parallelInvoke().pass(inputs).result();
    }

    @NotNull
    public StreamingChannel<IN, OUT> parallelStream() {

        final DefaultTransportChannel<IN> transportChannel =
                new DefaultTransportChannel<IN>(buildChannelConfiguration());
        return new DefaultStreamingChannel<IN, OUT>(transportChannel,
                                                    parallelCall(transportChannel));
    }

    public void purge() {

    }

    @NotNull
    public OutputChannel<OUT> syncCall() {

        return syncInvoke().result();
    }

    @NotNull
    public OutputChannel<OUT> syncCall(@Nullable final IN input) {

        return syncInvoke().pass(input).result();
    }

    @NotNull
    public OutputChannel<OUT> syncCall(@Nullable final IN... inputs) {

        return syncInvoke().pass(inputs).result();
    }

    @NotNull
    public OutputChannel<OUT> syncCall(@Nullable final Iterable<? extends IN> inputs) {

        return syncInvoke().pass(inputs).result();
    }

    @NotNull
    public OutputChannel<OUT> syncCall(@Nullable final OutputChannel<? extends IN> inputs) {

        return syncInvoke().pass(inputs).result();
    }

    @NotNull
    public StreamingChannel<IN, OUT> syncStream() {

        final DefaultTransportChannel<IN> transportChannel =
                new DefaultTransportChannel<IN>(buildChannelConfiguration());
        return new DefaultStreamingChannel<IN, OUT>(transportChannel, syncCall(transportChannel));
    }

    /**
     * Returns the invocation configuration.
     *
     * @return the configuration.
     */
    @NotNull
    protected abstract InvocationConfiguration getConfiguration();

    @NotNull
    private ChannelConfiguration buildChannelConfiguration() {

        final InvocationConfiguration configuration = getConfiguration();
        return builder().withAsyncRunner(configuration.getRunnerOr(null))
                        .withChannelMaxSize(configuration.getInputMaxSizeOr(DEFAULT))
                        .withChannelOrder(configuration.getInputOrderTypeOr(null))
                        .withChannelTimeout(configuration.getInputTimeoutOr(null))
                        .withPassTimeout(configuration.getExecutionTimeoutOr(null))
                        .withPassTimeoutAction(configuration.getExecutionTimeoutActionOr(null))
                        .withLog(configuration.getLogOr(null))
                        .withLogLevel(configuration.getLogLevelOr(null))
                        .set();
    }
}
