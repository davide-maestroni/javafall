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
package com.gh.bmd.jrt.android.proxy.builder;

import com.gh.bmd.jrt.android.builder.ServiceConfigurableBuilder;
import com.gh.bmd.jrt.android.builder.ServiceConfiguration;
import com.gh.bmd.jrt.builder.ProxyConfiguration;
import com.gh.bmd.jrt.builder.RoutineConfiguration;
import com.gh.bmd.jrt.common.ClassToken;
import com.gh.bmd.jrt.proxy.builder.ProxyRoutineBuilder;

import javax.annotation.Nonnull;

/**
 * Interface defining a builder of routines wrapping an object instance, whose methods are executed
 * in a dedicated service.
 * <p/>
 * Note that only instance methods can be asynchronously invoked through the routines created by
 * this builder.
 * <p/>
 * Created by davide-maestroni on 13/05/15.
 */
public interface ServiceProxyRoutineBuilder
        extends ProxyRoutineBuilder, ServiceConfigurableBuilder<ServiceProxyRoutineBuilder> {

    /**
     * Returns a proxy object enabling asynchronous calling of the target instance methods.
     * <p/>
     * The routines used for calling the methods will honor the attributes specified in any
     * optional {@link com.gh.bmd.jrt.annotation.Alias}, {@link com.gh.bmd.jrt.annotation.Timeout},
     * {@link com.gh.bmd.jrt.annotation.TimeoutAction} and {@link com.gh.bmd.jrt.annotation.Param},
     * as well as {@link com.gh.bmd.jrt.android.annotation.LoaderId},
     * {@link com.gh.bmd.jrt.android.annotation.ClashResolution} and
     * {@link com.gh.bmd.jrt.android.annotation.CacheStrategy} annotations.<br/>
     * Note that such annotations will override any configuration set through the builder.
     * <p/>
     * The proxy object is created through code generation based on the interfaces annotated with
     * {@link com.gh.bmd.jrt.android.proxy.annotation.ServiceProxy}. The generated class name and
     * package will be chosen according to the specific annotation attributes.<br/>
     * It is actually possible to avoid the use of reflection for the proxy object instantiation by
     * explicitly calling the "&lt;generated_class_name&gt;.onService()" method.<br/>
     * Note, however, that, since the class is generated, a generic IDE may highlight an error even
     * if the compilation is successful.
     *
     * @param itf    the interface implemented by the return object.
     * @param <TYPE> the interface type.
     * @return the proxy object.
     * @throws java.lang.IllegalArgumentException if the specified class does not represent an
     *                                            interface.
     */
    @Nonnull
    <TYPE> TYPE buildProxy(@Nonnull Class<TYPE> itf);

    /**
     * Returns a proxy object enabling asynchronous calling of the target instance methods.
     * <p/>
     * The routines used for calling the methods will honor the attributes specified in any
     * optional {@link com.gh.bmd.jrt.annotation.Alias}, {@link com.gh.bmd.jrt.annotation.Timeout},
     * {@link com.gh.bmd.jrt.annotation.TimeoutAction} and {@link com.gh.bmd.jrt.annotation.Param},
     * as well as {@link com.gh.bmd.jrt.android.annotation.LoaderId},
     * {@link com.gh.bmd.jrt.android.annotation.ClashResolution} and
     * {@link com.gh.bmd.jrt.android.annotation.CacheStrategy} annotations.<br/>
     * Note that such annotations will override any configuration set through the builder.
     * <p/>
     * The proxy object is created through code generation based on the interfaces annotated with
     * {@link com.gh.bmd.jrt.android.proxy.annotation.ServiceProxy}. The generated class name and
     * package will be chosen according to the specific annotation attributes.<br/>
     * It is actually possible to avoid the use of reflection for the proxy object instantiation by
     * explicitly calling the "&lt;generated_class_name&gt;.onService()" method.<br/>
     * Note, however, that, since the class is generated, a generic IDE may highlight an error even
     * if the compilation is successful.
     *
     * @param itf    the token of the interface implemented by the return object.
     * @param <TYPE> the interface type.
     * @return the proxy object.
     * @throws java.lang.IllegalArgumentException if the specified class does not represent an
     *                                            interface.
     */
    @Nonnull
    <TYPE> TYPE buildProxy(@Nonnull ClassToken<TYPE> itf);

    /**
     * Note that all the options related to the output and input channels will be ignored.
     *
     * @return the routine configuration builder.
     */
    @Nonnull
    RoutineConfiguration.Builder<? extends ServiceProxyRoutineBuilder> withRoutine();

    /**
     * {@inheritDoc}
     */
    @Nonnull
    ProxyConfiguration.Builder<? extends ServiceProxyRoutineBuilder> withProxy();

    /**
     * {@inheritDoc}
     */
    @Nonnull
    ServiceConfiguration.Builder<? extends ServiceProxyRoutineBuilder> withService();
}