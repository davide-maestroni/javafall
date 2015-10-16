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
package com.github.dm.jrt.functional;

/**
 * Interface representing a function that accepts one argument and produces a result.
 * <p/>
 * Created by davide-maestroni on 09/21/2015.
 *
 * @param <IN>  the input data type.
 * @param <OUT> the output data type.
 */
public interface Function<IN, OUT> {

    /**
     * Applies this function to the given argument.
     *
     * @param in the input argument.
     * @return the function result.
     */
    OUT apply(IN in);
}