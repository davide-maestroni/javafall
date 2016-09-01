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

package com.github.dm.jrt.android.v4.stream;

import com.github.dm.jrt.core.util.ConstantConditions;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class acting as a factory of stream routine builders.
 * <p>
 * A stream routine builder allows to easily build a concatenation of invocations as a single
 * routine.
 * <br>
 * For instance, a routine computing the root mean square of a number of integers can be defined as:
 * <pre>
 *     <code>
 *
 *         final Routine&lt;Integer, Double&gt; rms =
 *                 JRoutineLoaderStreamCompat.&lt;Integer&gt;withStream()
 *                                           .immediate()
 *                                           .map(i -&gt; i * i)
 *                                           .map(averageFloat())
 *                                           .map(Math::sqrt)
 *                                           .on(loaderFrom(activity))
 *                                           .buildRoutine();
 *     </code>
 * </pre>
 * <p>
 * Created by davide-maestroni on 07/04/2016.
 */
public class JRoutineLoaderStreamCompat {

    /**
     * Avoid explicit instantiation.
     */
    protected JRoutineLoaderStreamCompat() {
        ConstantConditions.avoid();
    }

    /**
     * Returns a stream routine builder.
     *
     * @param <IN> the input data type.
     * @return the routine builder instance.
     */
    @NotNull
    public static <IN> LoaderStreamBuilderCompat<IN, IN> withStream() {
        return new DefaultLoaderStreamBuilderCompat<IN, IN>();
    }
}
