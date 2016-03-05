/*
 * Copyright 2016 Davide Maestroni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dm.jrt;

import com.github.dm.jrt.core.builder.InvocationConfiguration;
import com.github.dm.jrt.object.builder.ObjectRoutineBuilder;
import com.github.dm.jrt.object.builder.ProxyConfiguration;

import org.jetbrains.annotations.NotNull;

/**
 * Created by davide-maestroni on 03/03/2016.
 */
public interface WrapRoutineBuilder extends ObjectRoutineBuilder {

    /**
     * {@inheritDoc}
     */
    @NotNull
    InvocationConfiguration.Builder<? extends WrapRoutineBuilder> withInvocations();

    /**
     * {@inheritDoc}
     */
    @NotNull
    ProxyConfiguration.Builder<? extends WrapRoutineBuilder> withProxies();
}
