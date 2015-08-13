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
package com.gh.bmd.jrt.android.builder;

import com.gh.bmd.jrt.builder.InvocationConfiguration;
import com.gh.bmd.jrt.builder.ObjectRoutineBuilder;
import com.gh.bmd.jrt.builder.ProxyConfiguration;
import com.gh.bmd.jrt.routine.Routine;
import com.gh.bmd.jrt.util.ClassToken;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

/**
 * Interface defining a builder of routines wrapping an object instance.
 * <p/>
 * The single methods can be accessed via reflection or the whole instance can be proxied through
 * an interface.
 * <p/>
 * Created by davide-maestroni on 4/6/2015.
 */
public interface LoaderObjectRoutineBuilder
        extends ObjectRoutineBuilder, LoaderConfigurableBuilder<LoaderObjectRoutineBuilder> {

    /**
     * Returns a routine used to call the method whose identifying name is specified in an
     * {@link com.gh.bmd.jrt.annotation.Alias Alias} annotation.<br/>
     * Optional {@link com.gh.bmd.jrt.annotation.Priority Priority},
     * {@link com.gh.bmd.jrt.annotation.ShareGroup ShareGroup},
     * {@link com.gh.bmd.jrt.annotation.Timeout Timeout},
     * {@link com.gh.bmd.jrt.annotation.TimeoutAction TimeoutAction}, as well as
     * {@link com.gh.bmd.jrt.android.annotation.CacheStrategy CacheStrategy},
     * {@link com.gh.bmd.jrt.android.annotation.ClashResolution ClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.InputClashResolution InputClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.LoaderId LoaderId} and
     * {@link com.gh.bmd.jrt.android.annotation.StaleTime StaleTime} method annotations will be
     * honored.<br/>
     * Note that such annotations will override any configuration set through the builder.
     *
     * @param name     the name specified in the annotation.
     * @param <INPUT>  the input data type.
     * @param <OUTPUT> the output data type.
     * @return the routine.
     * @throws java.lang.IllegalArgumentException if the specified method is not found.
     */
    @Nonnull
    <INPUT, OUTPUT> Routine<INPUT, OUTPUT> aliasMethod(@Nonnull String name);

    /**
     * Returns a routine used to call the specified method.
     * <p/>
     * The method is searched via reflection ignoring a name specified in an
     * {@link com.gh.bmd.jrt.annotation.Alias Alias} annotation. Though, optional
     * {@link com.gh.bmd.jrt.annotation.Priority Priority},
     * {@link com.gh.bmd.jrt.annotation.ShareGroup ShareGroup},
     * {@link com.gh.bmd.jrt.annotation.Timeout Timeout},
     * {@link com.gh.bmd.jrt.annotation.TimeoutAction TimeoutAction},
     * as well as {@link com.gh.bmd.jrt.android.annotation.CacheStrategy CacheStrategy},
     * {@link com.gh.bmd.jrt.android.annotation.ClashResolution ClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.InputClashResolution InputClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.LoaderId LoaderId} and
     * {@link com.gh.bmd.jrt.android.annotation.StaleTime StaleTime} method annotations will be
     * honored.<br/>
     * Note that such annotations will override any configuration set through the builder.
     *
     * @param name           the method name.
     * @param parameterTypes the method parameter types.
     * @return the routine.
     * @throws java.lang.IllegalArgumentException if no matching method is found.
     */
    @Nonnull
    <INPUT, OUTPUT> Routine<INPUT, OUTPUT> method(@Nonnull String name,
            @Nonnull Class<?>... parameterTypes);

    /**
     * Returns a routine used to call the specified method.
     * <p/>
     * The method is invoked ignoring a name specified in an
     * {@link com.gh.bmd.jrt.annotation.Alias Alias} annotation. Though, optional
     * {@link com.gh.bmd.jrt.annotation.Priority Priority},
     * {@link com.gh.bmd.jrt.annotation.ShareGroup ShareGroup},
     * {@link com.gh.bmd.jrt.annotation.Timeout Timeout},
     * {@link com.gh.bmd.jrt.annotation.TimeoutAction TimeoutAction},
     * as well as {@link com.gh.bmd.jrt.android.annotation.CacheStrategy CacheStrategy},
     * {@link com.gh.bmd.jrt.android.annotation.ClashResolution ClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.InputClashResolution InputClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.LoaderId LoaderId} and
     * {@link com.gh.bmd.jrt.android.annotation.StaleTime StaleTime} method annotations will be
     * honored.<br/>
     * Note that such annotations will override any configuration set through the builder.
     *
     * @param method   the method instance.
     * @param <INPUT>  the input data type.
     * @param <OUTPUT> the output data type.
     * @return the routine.
     */
    @Nonnull
    <INPUT, OUTPUT> Routine<INPUT, OUTPUT> method(@Nonnull Method method);

    /**
     * Returns a proxy object enabling asynchronous call of the target instance methods.
     * <p/>
     * The routines used for calling the methods will honor the attributes specified in any
     * optional {@link com.gh.bmd.jrt.annotation.Alias Alias},
     * {@link com.gh.bmd.jrt.annotation.Priority Priority},
     * {@link com.gh.bmd.jrt.annotation.ShareGroup ShareGroup},
     * {@link com.gh.bmd.jrt.annotation.Timeout Timeout},
     * {@link com.gh.bmd.jrt.annotation.TimeoutAction TimeoutAction}, as well as
     * {@link com.gh.bmd.jrt.android.annotation.CacheStrategy CacheStrategy},
     * {@link com.gh.bmd.jrt.android.annotation.ClashResolution ClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.InputClashResolution InputClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.LoaderId LoaderId} and
     * {@link com.gh.bmd.jrt.android.annotation.StaleTime StaleTime} annotations.<br/>
     * Note that such annotations will override any configuration set through the builder.
     *
     * @param itf    the token of the interface implemented by the return object.
     * @param <TYPE> the interface type.
     * @return the proxy object.
     * @throws java.lang.IllegalArgumentException if the specified class does not represent an
     *                                            interface.
     */
    @Nonnull
    <TYPE> TYPE buildProxy(@Nonnull Class<TYPE> itf);

    /**
     * Returns a proxy object enabling asynchronous call of the target instance methods.
     * <p/>
     * The routines used for calling the methods will honor the attributes specified in any
     * optional {@link com.gh.bmd.jrt.annotation.Alias Alias},
     * {@link com.gh.bmd.jrt.annotation.Priority Priority},
     * {@link com.gh.bmd.jrt.annotation.ShareGroup ShareGroup},
     * {@link com.gh.bmd.jrt.annotation.Timeout Timeout},
     * {@link com.gh.bmd.jrt.annotation.TimeoutAction TimeoutAction}, as well as
     * {@link com.gh.bmd.jrt.android.annotation.CacheStrategy CacheStrategy},
     * {@link com.gh.bmd.jrt.android.annotation.ClashResolution ClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.InputClashResolution InputClashResolution},
     * {@link com.gh.bmd.jrt.android.annotation.LoaderId LoaderId} and
     * {@link com.gh.bmd.jrt.android.annotation.StaleTime StaleTime} annotations.<br/>
     * Note that such annotations will override any configuration set through the builder.
     *
     * @param itf    the token of the interface implemented by the return object.
     * @param <TYPE> the interface type.
     * @return the proxy object.
     * @throws java.lang.IllegalArgumentException if the specified class token does not represent an
     *                                            interface.
     */
    @Nonnull
    <TYPE> TYPE buildProxy(@Nonnull ClassToken<TYPE> itf);

    /**
     * Note that the configured asynchronous runner will be ignored.
     *
     * @return the invocation configuration builder.
     */
    @Nonnull
    InvocationConfiguration.Builder<? extends LoaderObjectRoutineBuilder> invocations();

    /**
     * {@inheritDoc}
     */
    @Nonnull
    ProxyConfiguration.Builder<? extends LoaderObjectRoutineBuilder> proxies();
}