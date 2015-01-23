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

import javax.annotation.Nullable;

/**
 * Interface defining an output consumer that can be bound to an output channel.
 * <p/>
 * The typical lifecycle of a consumer object is the following:
 * <pre>
 *     <code>
 *
 *                           ------
 *                     |    |      |
 *                     V    V      |
 *               --------------    |
 *               | onOutput() |----
 *               --------------
 *                   |    |
 *                   |    |
 *             ------      ------
 *       |    |                  |    |
 *       V    V                  V    V
 *   --------------          --------------
 *   |onComplete()|          | onError()  |
 *   --------------          --------------
 *     </code>
 * </pre>
 * <p/>
 * Created by davide on 9/7/14.
 *
 * @param <OUTPUT> the output data type.
 */
public interface OutputConsumer<OUTPUT> {

    /**
     * Called when the channel closes after the routine completes its execution.
     */
    public void onComplete();

    /**
     * Called when the bounded channel transfer is aborted.
     *
     * @param error the reason of the abortion.
     */
    public void onError(@Nullable Throwable error);

    /**
     * Called when an output is passed to the channel.
     *
     * @param output the output.
     */
    public void onOutput(OUTPUT output);
}
