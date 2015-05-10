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
package com.gh.bmd.jrt.builder;

import com.gh.bmd.jrt.builder.ProxyConfiguration.Builder;

import javax.annotation.Nonnull;

/**
 * Interface defining a configurable builder of proxy routines.
 * <p/>
 * Created by davide on 01/05/15.
 *
 * @param <TYPE> the builder type.
 */
public interface ProxyConfigurableBuilder<TYPE> {

    /**
     * Gets the proxy configuration builder related to this builder instance.
     * The configuration options not supported by the builder implementation might be ignored.
     * <p/>
     * Note that the builder will be initialized with the current configuration.
     *
     * @return the proxy configuration builder.
     */
    @Nonnull
    Builder<? extends TYPE> withProxy();
}
