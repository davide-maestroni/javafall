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
package com.gh.bmd.jrt.channel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exception indicating that an invocation has been explicitly aborted.
 * <p/>
 * Created by davide-maestroni on 1/27/15.
 */
public class AbortException extends RoutineException {

    /**
     * Constructor.
     *
     * @param cause the wrapped exception.
     */
    public AbortException(@Nullable final Throwable cause) {

        super(cause);
    }

    /**
     * Wraps the specified throwable only if it is not an instance of
     * {@link com.gh.bmd.jrt.channel.RoutineException RoutineException}.
     *
     * @param cause the throwable to wrap.
     * @return the throwable or an abort exception wrapping it.
     */
    @Nonnull
    public static RoutineException wrapIfNeeded(@Nullable final Throwable cause) {

        return (cause instanceof RoutineException) ? (RoutineException) cause
                : new AbortException(cause);
    }
}