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
package com.gh.bmd.jrt.routine;

import com.gh.bmd.jrt.builder.RoutineConfiguration;
import com.gh.bmd.jrt.common.InvocationException;
import com.gh.bmd.jrt.common.InvocationInterruptedException;
import com.gh.bmd.jrt.common.RoutineException;
import com.gh.bmd.jrt.invocation.Invocation;
import com.gh.bmd.jrt.invocation.InvocationFactory;
import com.gh.bmd.jrt.log.Logger;

import javax.annotation.Nonnull;

/**
 * Default implementation of a routine object instantiating invocation objects via reflection.
 * <p/>
 * Created by davide on 9/9/14.
 *
 * @param <INPUT>  the input data type.
 * @param <OUTPUT> the output data type.
 */
class DefaultRoutine<INPUT, OUTPUT> extends AbstractRoutine<INPUT, OUTPUT> {

    private final InvocationFactory<INPUT, OUTPUT> mFactory;

    /**
     * Constructor.
     *
     * @param configuration the routine configuration.
     * @param factory       the invocation factory.
     * @throws java.lang.IllegalArgumentException if at least one of the parameter is invalid.
     * @throws java.lang.NullPointerException     if at least one of the parameter is null.
     */
    @SuppressWarnings("ConstantConditions")
    DefaultRoutine(@Nonnull final RoutineConfiguration configuration,
            @Nonnull final InvocationFactory<INPUT, OUTPUT> factory) {

        super(configuration);

        if (factory == null) {

            throw new NullPointerException("the invocation factory must not be null");
        }

        mFactory = factory;
    }

    @Nonnull
    @Override
    protected Invocation<INPUT, OUTPUT> newInvocation(final boolean async) {

        final Logger logger = getLogger();

        try {

            final InvocationFactory<INPUT, OUTPUT> factory = mFactory;
            logger.dbg("creating a new invocation instance with factory: %s", factory);
            final Invocation<INPUT, OUTPUT> invocation = factory.newInvocation();
            logger.dbg("created a new instance of class: %s", invocation.getClass());
            return invocation;

        } catch (final InvocationInterruptedException e) {

            logger.err(e, "error creating the invocation instance");
            throw e.interrupt();

        } catch (final RoutineException e) {

            logger.err(e, "error creating the invocation instance");
            throw e;

        } catch (final Throwable t) {

            logger.err(t, "error creating the invocation instance");
            throw new InvocationException(t);
        }
    }
}