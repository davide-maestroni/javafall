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

/**
 * Interface defining a routine invocation.<br/>
 * This interface is meant to be used by a runner to ensure that the routine execution will take
 * place in the specific handled thread or threads.
 * <p/>
 * Created by davide on 9/7/14.
 */
public interface Invocation {

    /**
     * Called to abort the routine execution.
     */
    public void abort();

    /**
     * Called to run the routine execution.
     */
    public void run();
}