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

package com.github.dm.jrt.android.object;

import com.github.dm.jrt.android.core.ServiceContext;
import com.github.dm.jrt.android.object.builder.ServiceObjectRoutineBuilder;
import com.github.dm.jrt.core.util.ConstantConditions;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class supporting the creation of routine builders specific to the Android platform.
 * <br>
 * Routine invocations created through the returned builder will be executed inside a Service
 * specified by the Service context. Be aware, though, that the invocation results will be
 * dispatched into the configured Looper, so that, waiting for the outputs on the very same Looper
 * thread, immediately after its invocation, will result in a deadlock.
 * <br>
 * By default output results are dispatched in the main Looper.
 * <br>
 * Note that the configuration of the maximum number of concurrent invocations will not be shared
 * among synchronous and asynchronous invocations, but the invocations created inside the Service
 * and the synchronous will respect the same limit separately.
 * <p>
 * It is up to the caller to properly declare the Service in the manifest file. Note also that it is
 * possible to manage the Service lifecycle starting it through the
 * {@link android.content.Context#startService(android.content.Intent)} method. Normally the Service
 * will stay active only during a routine invocation. In fact, it is responsibility of the caller
 * to ensure that the started invocations have completed or have been aborted when the relative
 * Context (for example the Activity) is destroyed, so to avoid the leak of IPC connections.
 * <br>
 * The Service can be also made run in a different process, however, in such case, the data passed
 * through the routine input and output channels, as well as the factory arguments, must comply with
 * the {@link android.os.Parcel#writeValue(Object)} method. Be aware though, that issues may arise
 * when employing {@link java.io.Serializable} objects on some OS versions, so, it is advisable to
 * use {@link android.os.Parcelable} objects instead.
 * <p>
 * The class provides an additional way to build a routine, based on the asynchronous invocation of
 * a method of an existing class or object via reflection.
 * <br>
 * It is possible to annotate selected methods to be asynchronously invoked, or to simply select
 * a method through its signature. It is also possible to build a proxy object whose methods will
 * in turn asynchronously invoke the target object ones.
 * <p>
 * Note however that, since the method might be invoked in a different process, it is not possible
 * to pass along the actual instance, but just the information needed to get or instantiate it
 * inside the target Service.
 * <p>
 * Created by davide-maestroni on 01/08/2015.
 *
 * @see com.github.dm.jrt.object.JRoutineObject JRoutineObject
 */
public class JRoutineServiceObject {

  /**
   * Avoid explicit instantiation.
   */
  protected JRoutineServiceObject() {
    ConstantConditions.avoid();
  }

  /**
   * Returns a Context based builder of Service routine builders.
   *
   * @param context the Service context.
   * @return the Context based builder.
   */
  @NotNull
  public static ServiceObjectBuilder on(@NotNull final ServiceContext context) {
    return new ServiceObjectBuilder(context);
  }

  /**
   * Context based builder of Service routine builders.
   */
  @SuppressWarnings("WeakerAccess")
  public static class ServiceObjectBuilder {

    private final ServiceContext mContext;

    /**
     * Constructor.
     *
     * @param context the Service context.
     */
    private ServiceObjectBuilder(@NotNull final ServiceContext context) {
      mContext = ConstantConditions.notNull("Service context", context);
    }

    /**
     * Returns a builder of routines running in a Service based on the builder context, wrapping
     * the specified target object.
     * <br>
     * In order to customize the object creation, the caller must employ an implementation of a
     * {@link com.github.dm.jrt.android.object.builder.FactoryContext FactoryContext} as the
     * invocation Service.
     * <p>
     * Note that the built routine results will be dispatched into the configured Looper, thus,
     * waiting for the outputs on the very same Looper thread, immediately after its invocation,
     * will result in a deadlock. By default output results are dispatched in the main Looper.
     *
     * @param target the invocation target.
     * @return the routine builder instance.
     */
    @NotNull
    public ServiceObjectRoutineBuilder with(@NotNull final ContextInvocationTarget<?> target) {
      return new DefaultServiceObjectRoutineBuilder(mContext, target);
    }
  }
}
