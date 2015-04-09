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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface defining a factory providing wrapped object instances, whose methods are to be called
 * inside asynchronous invocations.
 * <p/>
 * Created by Davide on 4/6/2015.
 */
public interface FactoryContext {

    /**
     * Returns an instance of the object described by the specified parameters.<br/>
     * If a null instance is returned, the constructor matching the specified arguments will be
     * called by default.
     *
     * @param type   the type of the returned instance.
     * @param args   the constructor arguments.
     * @param <TYPE> the wrapped object type.
     * @return the object instance.
     */
    @Nullable
    <TYPE> TYPE geInstance(@Nonnull final Class<? extends TYPE> type,
            @Nonnull final Object... args);
}
