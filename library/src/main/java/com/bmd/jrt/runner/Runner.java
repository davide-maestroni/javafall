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

import java.util.concurrent.TimeUnit;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The runner interface defines an object responsible for executing routine invocations inside
 * specifically managed threads.
 * <p/>
 * The implementation can both be synchronous or asynchronous, it can allocate specific threads or
 * share a pool of them between different instances.<br/>
 * The only requirements is that the specified invocation is called each time a run method is
 * invoked.<br/>
 * Note also that the runner methods can be called from different thread, so, it is up to the
 * implementing class to ensure synchronization when needed.
 * <p/>
 * Created by davide on 9/7/14.
 */
public interface Runner {

    /**
     * Runs the specified invocation (that is, it calls the invocation <code>run()</code> method
     * inside the runner thread).
     *
     * @param invocation the invocation.
     * @param delay      the execution delay.
     * @param timeUnit   the delay time unit.
     */
    public void run(@NonNull Invocation invocation, long delay, @NonNull TimeUnit timeUnit);

    /**
     * Runs the specified abort invocation. (that is, calls the invocation <code>abort()</code>
     * method inside the runner thread).
     *
     * @param invocation the invocation.
     */
    public void runAbort(@NonNull Invocation invocation);
}