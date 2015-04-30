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
package com.gh.bmd.jrt.processor.builder;

import com.gh.bmd.jrt.builder.ConfigurableBuilder;
import com.gh.bmd.jrt.builder.ProxyConfigurableBuilder;
import com.gh.bmd.jrt.builder.RoutineConfiguration.Builder;

import javax.annotation.Nonnull;

/**
 * Interface defining a builder of async wrapper objects.
 * <p/>
 * Created by davide on 3/7/15.
 *
 * @param <TYPE> the interface type.
 */
public interface WrapperBuilder<TYPE> extends ConfigurableBuilder<WrapperBuilder<TYPE>>,
        ProxyConfigurableBuilder<WrapperBuilder<TYPE>> {

    /**
     * Returns a wrapper object enabling asynchronous calling of the target instance methods.
     * <p/>
     * The routines used for calling the methods will honor the attributes specified in any
     * optional {@link com.gh.bmd.jrt.annotation.Bind}, {@link com.gh.bmd.jrt.annotation.Timeout},
     * {@link com.gh.bmd.jrt.annotation.TimeoutAction} and {@link com.gh.bmd.jrt.annotation.Pass}
     * annotations.<br/>
     * Note that such annotations will override any configuration set through the builder.
     * <p/>
     * The wrapping object is created through code generation based on the interfaces annotated
     * with {@link com.gh.bmd.jrt.processor.annotation.Wrap}.<br/>
     * Note that, you'll need to enable annotation pre-processing by adding the processor artifact
     * to the specific project dependencies.
     *
     * @return the wrapping object.
     */
    @Nonnull
    TYPE buildWrapper();

    /**
     * Note that all the options related to the output and input channels will be ignored.
     *
     * @return the routine configuration builder.
     */
    @Nonnull
    Builder<? extends WrapperBuilder<TYPE>> withRoutineConfiguration();
}
