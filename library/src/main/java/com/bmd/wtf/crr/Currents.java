/**
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
package com.bmd.wtf.crr;

/**
 * Utility class for handling current objects.
 * <p/>
 * Created by davide on 6/8/14.
 */
public class Currents {

    private static volatile PassThroughCurrent sPassThrough;

    /**
     * Avoid direct instantiation.
     */
    protected Currents() {

    }

    /**
     * Returns the default {@link PassThroughCurrent} instance.
     *
     * @return the instance.
     */
    public static Current passThrough() {

        if (sPassThrough == null) {

            sPassThrough = new PassThroughCurrent();
        }

        return sPassThrough;
    }

    /**
     * Returns a new {@link ThreadPoolCurrent} instance.
     *
     * @param poolSize the maximum size of the thread pool.
     * @return the new instance.
     * @throws java.lang.IllegalArgumentException if the pool size is negative.
     */
    public static Current pool(final int poolSize) {

        return new ThreadPoolCurrent(poolSize);
    }
}