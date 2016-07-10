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

package com.github.dm.jrt.android.core.invocation;

/**
 * Exception indicating a clash of routine invocations with same loader ID.
 * <p>
 * Created by davide-maestroni on 06/28/2016.
 */
public class LoaderClashException extends LoaderInvocationException {

    /**
     * Constructor.
     *
     * @param id the loader ID.
     */
    public LoaderClashException(final int id) {
        super(id);
    }
}
