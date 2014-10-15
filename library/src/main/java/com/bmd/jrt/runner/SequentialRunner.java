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
package com.bmd.jrt.runner;

import com.bmd.jrt.common.RoutineInterruptedException;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import static com.bmd.jrt.time.TimeDuration.fromUnit;

/**
 * Class implementing a sequential synchronous runner.
 * <p/>
 * The runner simply executes the invocations as soon as they are run.<br/>
 * While it is less memory and CPU consuming than the queued implementation, it might greatly
 * increase the depth of the call stack, and blocks execution of the calling thread during delayed
 * invocations.
 * <p/>
 * Created by davide on 9/9/14.
 *
 * @see QueuedRunner
 */
class SequentialRunner implements Runner {

    @Override
    public void run(@Nonnull final Invocation invocation, final long delay,
            @Nonnull final TimeUnit timeUnit) {

        try {

            fromUnit(delay, timeUnit).sleepAtLeast();

            invocation.run();

        } catch (final InterruptedException e) {

            RoutineInterruptedException.interrupt(e);
        }
    }
}