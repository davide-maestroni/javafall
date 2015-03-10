/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh.bmd.jrt.builder;

import com.gh.bmd.jrt.common.DeadlockException;

import javax.annotation.Nullable;

/**
 * Exception indicating that no room in the output channel buffer became available before the
 * specific timeout elapsed.
 * <p/>
 * Created by davide on 11/25/14.
 */
public class OutputDeadlockException extends DeadlockException {

    /**
     * Constructor.
     *
     * @param message the error message.
     */
    public OutputDeadlockException(@Nullable final String message) {

        super(message);
    }
}