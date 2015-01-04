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
 * Interface defining a basic communication channel with the routine.
 * <p/>
 * Channel instances are used to transfer data to and from the code executed inside the routine
 * invocation.
 * <p/>
 * Created by davide on 9/9/14.
 */
public interface Channel {

    /**
     * Closes the channel and abort the transfer of data, thus aborting the routine execution.
     * <p/>
     * Note that, in case the channel was already closed, the call to this method has no effect.
     *
     * @return whether the channel status changed as a result of the call.
     */
    public boolean abort();

    /**
     * Closes the channel and abort the transfer of data, thus aborting the routine execution and
     * causing the specified throwable to be passed as the abortion reason.
     * <p/>
     * Note that, in case the channel was already closed, the call to this method has no effect.
     *
     * @param reason the throwable object identifying the reason of the routine abortion.
     * @return whether the channel status changed as a result of the call.
     */
    public boolean abort(@Nullable Throwable reason);

    /**
     * Checks if the channel is open, that is, data can be written or read.
     *
     * @return whether the channel is open.
     */
    public boolean isOpen();
}