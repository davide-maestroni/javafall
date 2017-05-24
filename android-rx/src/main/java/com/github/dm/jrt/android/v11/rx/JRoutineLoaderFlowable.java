/*
 * Copyright 2017 Davide Maestroni
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

package com.github.dm.jrt.android.v11.rx;

import com.github.dm.jrt.android.rx.LoaderFlowable;
import com.github.dm.jrt.android.v11.core.LoaderSource;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class integrating the JRoutine Android classes with RxJava ones.
 * <p>
 * The example below shows how it's possible to make the computation happen in a dedicated Loader:
 * <pre><code>
 * JRoutineLoaderFlowable.flowableOn(loaderOf(activity))
 *                       .configuration()
 *                       .withInvocationId(INVOCATION_ID)
 *                       .configuration()
 *                       .observeOnLoader(myFlowable)
 *                       .subscribe(getConsumer());
 * </code></pre>
 * Note that the Loader ID, by default, will only depend on the inputs, so, in order to avoid
 * clashing, it is advisable to explicitly set one through the configuration.
 * <p>
 * See
 * {@link com.github.dm.jrt.android.v4.rx.JRoutineLoaderFlowableCompat JRoutineLoaderObservableCompat}
 * for support of API levels lower than {@link android.os.Build.VERSION_CODES#HONEYCOMB 11}.
 * <p>
 * Created by davide-maestroni on 02/09/2017.
 */
@SuppressWarnings("WeakerAccess")
public class JRoutineLoaderFlowable {

  /**
   * Returns a Loader Flowable instance based on the specified source.
   *
   * @param loaderSource the Loader source.
   * @return the Loader Flowable.
   */
  @NotNull
  public static LoaderFlowable flowableOn(@NotNull final LoaderSource loaderSource) {
    return new DefaultLoaderFlowable(loaderSource);
  }
}
