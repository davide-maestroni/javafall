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

package com.github.dm.jrt.core.common;

/**
 * Interface defining a backoff policy returning a delay in milliseconds.
 * <br>
 * The implementation should be stateless and rely only on the passed count to compute the delay.
 * In fact, the implementing class is likely to be called from different threads and with unrelated
 * count numbers.
 * <p>
 * Created by davide-maestroni on 05/09/2016.
 */
public interface Backoff {

  /**
   * Constant indicating that no delay should be applied.
   */
  int NO_DELAY = -1;

  /**
   * Gets the delay for the specified count.
   *
   * @param count the count (it must be positive).
   * @return the delay in milliseconds.
   */
  long getDelay(int count);
}
