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
package com.gh.bmd.jrt.invocation;

import com.gh.bmd.jrt.channel.ResultChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Empty abstract implementation of a routine invocation.
 * <p/>
 * This class is useful to avoid the need of implementing all the methods defined in the interface.
 * <p/>
 * Created by davide-maestroni on 9/11/14.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
public abstract class TemplateInvocation<INPUT, OUTPUT> implements Invocation<INPUT, OUTPUT> {

    public void onAbort(@Nullable final Throwable reason) {

    }

    public void onDestroy() {

    }

    public void onInitialize() {

    }

    public void onInput(final INPUT input, @Nonnull final ResultChannel<OUTPUT> result) {

    }

    public void onResult(@Nonnull final ResultChannel<OUTPUT> result) {

    }

    public void onTerminate() {

    }
}
