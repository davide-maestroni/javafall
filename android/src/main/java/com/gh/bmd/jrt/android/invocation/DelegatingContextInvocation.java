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
package com.gh.bmd.jrt.android.invocation;

import android.content.Context;

import com.gh.bmd.jrt.invocation.DelegatingInvocation;
import com.gh.bmd.jrt.routine.Routine;

import javax.annotation.Nonnull;

/**
 * Invocation implementation delegating the execution to another routine.
 * <p/>
 * Created by davide-maestroni on 19/04/15.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
public class DelegatingContextInvocation<INPUT, OUTPUT> extends DelegatingInvocation<INPUT, OUTPUT>
        implements ContextInvocation<INPUT, OUTPUT> {

    /**
     * Constructor.
     *
     * @param routine        the routine used to execute this invocation.
     * @param delegationType the type of routine invocation.
     */
    public DelegatingContextInvocation(@Nonnull final Routine<INPUT, OUTPUT> routine,
            @Nonnull final DelegationType delegationType) {

        super(routine, delegationType);
    }

    /**
     * Returns a factory of delegating invocations.<br/>
     * Note that the specified identifier will be used to detect clashing of invocations (see
     * {@link com.gh.bmd.jrt.android.v11.core.JRoutine} and
     * {@link com.gh.bmd.jrt.android.v4.core.JRoutine}).
     *
     * @param routine        the routine used to execute this invocation.
     * @param routineId      the routine identifier.
     * @param delegationType the type of routine invocation.
     * @param <INPUT>        the input data type.
     * @param <OUTPUT>       the output data type.
     * @return the factory.
     */
    @Nonnull
    public static <INPUT, OUTPUT> ContextInvocationFactory<INPUT, OUTPUT> factoryFrom(
            @Nonnull final Routine<INPUT, OUTPUT> routine, final int routineId,
            @Nonnull final DelegationType delegationType) {

        return new DelegatingContextInvocationFactory<INPUT, OUTPUT>(routine, routineId,
                                                                     delegationType);
    }

    public void onContext(@Nonnull final Context context) {

    }

    /**
     * Factory creating delegating context invocation instances.
     *
     * @param <INPUT>  the input data type.
     * @param <OUTPUT> the output data type.
     */
    private static class DelegatingContextInvocationFactory<INPUT, OUTPUT>
            extends AbstractContextInvocationFactory<INPUT, OUTPUT> {

        private final DelegationType mDelegationType;

        private final Routine<INPUT, OUTPUT> mRoutine;

        /**
         * Constructor.
         *
         * @param routine        the delegated routine.
         * @param routineId      the routine identifier.
         * @param delegationType the type of routine invocation.
         */
        @SuppressWarnings("ConstantConditions")
        private DelegatingContextInvocationFactory(@Nonnull final Routine<INPUT, OUTPUT> routine,
                final int routineId, @Nonnull final DelegationType delegationType) {

            super(routineId, delegationType);

            if (routine == null) {

                throw new NullPointerException("the routine must not be null");
            }

            if (delegationType == null) {

                throw new NullPointerException("the invocation type must not be null");
            }

            mRoutine = routine;
            mDelegationType = delegationType;
        }

        @Nonnull
        public ContextInvocation<INPUT, OUTPUT> newInvocation() {

            return new DelegatingContextInvocation<INPUT, OUTPUT>(mRoutine, mDelegationType);
        }
    }
}
