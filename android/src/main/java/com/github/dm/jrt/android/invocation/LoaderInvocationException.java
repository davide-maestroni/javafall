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
package com.github.dm.jrt.android.invocation;

import com.github.dm.jrt.channel.RoutineException;

/**
 * Base exception indicating that an unrecoverable error occurred during a loader invocation
 * execution.
 * <p/>
 * Created by davide-maestroni on 06/03/2015.
 */
public class LoaderInvocationException extends RoutineException {

    private final int mId;

    /**
     * Constructor.
     *
     * @param id the loader ID.
     */
    public LoaderInvocationException(final int id) {

        mId = id;
    }

    /**
     * Returns the loader ID.
     *
     * @return the loader ID.
     */
    public int getId() {

        return mId;
    }
}