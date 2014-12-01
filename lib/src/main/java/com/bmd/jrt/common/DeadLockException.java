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
package com.bmd.jrt.common;

/**
 * Exception indicating a possible dead lock.
 * <p/>
 * Created by davide on 11/25/14.
 */
public class DeadLockException extends RoutineException {

    /**
     * Constructor.
     */
    public DeadLockException() {

        super(null);
    }

    @Override
    public boolean needsUnwrap() {

        return false;
    }
}
