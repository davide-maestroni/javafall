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

package com.github.dm.jrt.android.retrofit;

import com.github.dm.jrt.android.channel.JRoutineAndroidChannels;
import com.github.dm.jrt.android.channel.ParcelableFlowData;
import com.github.dm.jrt.android.channel.io.ParcelableByteChannel;
import com.github.dm.jrt.channel.config.ByteChunkStreamConfiguration.CloseActionType;
import com.github.dm.jrt.channel.io.ByteChannel.ByteChunkOutputStream;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.invocation.MappingInvocation;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;

import static com.github.dm.jrt.android.retrofit.ServiceCallInvocation.BYTES_ID;
import static com.github.dm.jrt.android.retrofit.ServiceCallInvocation.MEDIA_TYPE_ID;
import static com.github.dm.jrt.android.retrofit.ServiceCallInvocation.REQUEST_DATA_ID;

/**
 * Mapping invocation used to split the Retrofit call into request data and body, so to be more
 * easily parceled.
 * <p>
 * Created by davide-maestroni on 05/19/2016.
 */
class CallMappingInvocation extends MappingInvocation<Call<?>, ParcelableFlowData<Object>> {

  /**
   * Constructor.
   */
  CallMappingInvocation() {
    super(null);
  }

  @Override
  public void onInput(final Call<?> input,
      @NotNull final Channel<ParcelableFlowData<Object>, ?> result) throws IOException {
    final Request request = input.request();
    result.pass(new ParcelableFlowData<Object>(REQUEST_DATA_ID, RequestData.of(request)));
    final RequestBody body = request.body();
    if (body != null) {
      final MediaType mediaType = body.contentType();
      result.pass(new ParcelableFlowData<Object>(MEDIA_TYPE_ID,
          (mediaType != null) ? mediaType.toString() : null));
      final Channel<Object, ?> channel =
          JRoutineAndroidChannels.channelHandler().inputOfParcelableFlow(result, BYTES_ID);
      final ByteChunkOutputStream outputStream = ParcelableByteChannel.outputStream()
                                                                      .withStream()
                                                                      .withOnClose(
                                                                          CloseActionType
                                                                              .CLOSE_CHANNEL)
                                                                      .configuration()
                                                                      .of(channel);
      final BufferedSink buffer = Okio.buffer(Okio.sink(outputStream));
      body.writeTo(buffer);
      buffer.close();
    }
  }
}
