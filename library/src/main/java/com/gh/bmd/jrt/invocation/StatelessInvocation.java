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
package com.gh.bmd.jrt.invocation;

import com.gh.bmd.jrt.channel.ResultChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract implementation of an invocation that does not retain an internal variable state.
 * <p/>
 * Created by davide on 2/14/15.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
public abstract class StatelessInvocation<INPUT, OUTPUT> implements Invocation<INPUT, OUTPUT> {

    @Override
    public final void onAbort(@Nullable final Throwable reason) {

    }

    @Override
    public final void onDestroy() {

    }

    @Override
    public final void onInit() {

    }

    @Override
    public final void onResult(@Nonnull final ResultChannel<OUTPUT> result) {

    }

    @Override
    public final void onReturn() {

    }
}