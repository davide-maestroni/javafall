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
package com.gh.bmd.jrt.android.proxy.core;

import com.gh.bmd.jrt.android.core.ServiceContext;
import com.gh.bmd.jrt.android.proxy.builder.ServiceProxyRoutineBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utility class used to create builders of objects wrapping target ones, so to enable asynchronous
 * calls of their methods in a dedicated service.
 * <p/>
 * The builders returned by this class are based on compile time code generation, enabled by
 * pre-processing of Java annotations.<br/>
 * The pre-processing is automatically triggered just by including the artifact of this class
 * module.
 * <p/>
 * Created by davide-maestroni on 13/05/15.
 *
 * @see com.gh.bmd.jrt.android.proxy.annotation.ServiceProxy ServiceProxy
 * @see com.gh.bmd.jrt.annotation.Alias Alias
 * @see com.gh.bmd.jrt.annotation.Input Input
 * @see com.gh.bmd.jrt.annotation.Inputs Inputs
 * @see com.gh.bmd.jrt.annotation.Output Output
 * @see com.gh.bmd.jrt.annotation.Priority Priority
 * @see com.gh.bmd.jrt.annotation.ShareGroup ShareGroup
 * @see com.gh.bmd.jrt.annotation.Timeout Timeout
 * @see com.gh.bmd.jrt.annotation.TimeoutAction TimeoutAction
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
        justification = "utility class extending the functions of another utility class")
public class JRoutineProxy extends com.gh.bmd.jrt.proxy.core.JRoutineProxy {

    /**
     * Avoid direct instantiation.
     */
    protected JRoutineProxy() {

    }

    /**
     * Returns a builder of routines, wrapping the specified object instance, running in a service
     * based on the specified context.<br/>
     * In order to customize the object creation, the caller must employ an implementation of a
     * {@link com.gh.bmd.jrt.android.builder.FactoryContext FactoryContext} as the invocation
     * service.
     * <p/>
     * Note that the built routine results will be dispatched into the configured looper, thus,
     * waiting for the outputs on the very same looper thread, immediately after its invocation,
     * will result in a deadlock.<br/>
     * By default, output results are dispatched in the main looper.
     *
     * @param context the service context.
     * @param target  the wrapped object class.
     * @return the routine builder instance.
     */
    @Nonnull
    public static ServiceProxyRoutineBuilder on(@Nonnull final ServiceContext context,
            @Nonnull final Class<?> target) {

        return on(context, target, (Object[]) null);
    }

    /**
     * Returns a builder of routines, wrapping the specified object instance, running in a service
     * based on the specified context.<br/>
     * In order to customize the object creation, the caller must employ an implementation of a
     * {@link com.gh.bmd.jrt.android.builder.FactoryContext FactoryContext} as the invocation
     * service.
     * <p/>
     * Note that the built routine results will be dispatched into the configured looper, thus,
     * waiting for the outputs on the very same looper thread, immediately after its invocation,
     * will result in a deadlock.<br/>
     * By default, output results are dispatched in the main looper.
     *
     * @param context     the service context.
     * @param target      the wrapped object class.
     * @param factoryArgs the object factory arguments.
     * @return the routine builder instance.
     */
    @Nonnull
    public static ServiceProxyRoutineBuilder on(@Nonnull final ServiceContext context,
            @Nonnull final Class<?> target, @Nullable final Object... factoryArgs) {

        return new DefaultServiceProxyRoutineBuilder(context, target, factoryArgs);
    }
}