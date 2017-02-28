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
import com.github.dm.jrt.core.builder.AbstractChannelBuilder;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.util.ConstantConditions;

import org.jetbrains.annotations.NotNull;

/**
 * Builder implementation returning a channel making the output data a flow.
 * <p>
 * Created by davide-maestroni on 02/26/2016.
 *
 * @param <OUT> the output data type.
 */
class OutputFlowBuilder<OUT> extends AbstractChannelBuilder<Flow<OUT>, Flow<OUT>> {

  private final Channel<?, ? extends OUT> mChannel;

  private final int mId;

  /**
   * Constructor.
   *
   * @param channel the channel.
   * @param id      the flow ID.
   */
  OutputFlowBuilder(@NotNull final Channel<?, ? extends OUT> channel, final int id) {
    mChannel = ConstantConditions.notNull("channel instance", channel);
    mId = id;
  }

  @NotNull
  public Channel<Flow<OUT>, Flow<OUT>> buildChannel() {
    final Channel<Flow<OUT>, Flow<OUT>> outputChannel =
        JRoutineCore.<Flow<OUT>>ofData().apply(getConfiguration()).buildChannel();
    mChannel.consume(new FlowChannelConsumer<OUT, OUT>(outputChannel, mId));
    return outputChannel;
  }
}
