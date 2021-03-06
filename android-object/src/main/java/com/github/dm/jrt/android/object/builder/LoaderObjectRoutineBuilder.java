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

package com.github.dm.jrt.android.object.builder;

import com.github.dm.jrt.android.core.config.LoaderConfigurable;
import com.github.dm.jrt.android.core.routine.LoaderRoutine;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.util.ClassToken;
import com.github.dm.jrt.object.builder.ObjectRoutineBuilder;
import com.github.dm.jrt.object.config.ObjectConfiguration;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Interface defining a builder of routines wrapping an object methods.
 * <p>
 * The single methods can be accessed via reflection or the whole instance can be proxied through
 * an interface.
 * <p>
 * Created by davide-maestroni on 04/06/2015.
 */
public interface LoaderObjectRoutineBuilder
    extends ObjectRoutineBuilder, LoaderConfigurable<LoaderObjectRoutineBuilder> {

  /**
   * {@inheritDoc}
   * <p>
   * The configured asynchronous runner will be ignored.
   */
  @NotNull
  @Override
  LoaderObjectRoutineBuilder apply(@NotNull InvocationConfiguration configuration);

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  LoaderObjectRoutineBuilder apply(@NotNull ObjectConfiguration configuration);

  /**
   * {@inheritDoc}
   * <p>
   * The configured asynchronous runner will be ignored.
   */
  @NotNull
  @Override
  InvocationConfiguration.Builder<? extends LoaderObjectRoutineBuilder>
  applyInvocationConfiguration();

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  ObjectConfiguration.Builder<? extends LoaderObjectRoutineBuilder> applyObjectConfiguration();

  /**
   * Returns a proxy object enabling asynchronous call of the target instance methods.
   * <p>
   * The routines used for calling the methods will honor the attributes specified in any optional
   * <i>{@code com.github.dm.jrt.object.annotation.*}</i> as well as
   * <i>{@code com.github.dm.jrt.android.object.annotation.*}</i> annotations.
   * <br>
   * Note that such annotations will override any configuration set through the builder.
   *
   * @param itf    the token of the interface implemented by the return object.
   * @param <TYPE> the interface type.
   * @return the proxy object.
   * @throws java.lang.IllegalArgumentException if the specified class does not represent an
   *                                            interface.
   * @see <a href='{@docRoot}/com/github/dm/jrt/android/object/annotation/package-summary.html'>
   * Android Annotations</a>
   * @see com.github.dm.jrt.object.annotation Annotations
   */
  @NotNull
  @Override
  <TYPE> TYPE buildProxy(@NotNull Class<TYPE> itf);

  /**
   * Returns a proxy object enabling asynchronous call of the target instance methods.
   * <p>
   * The routines used for calling the methods will honor the attributes specified in any optional
   * <i>{@code com.github.dm.jrt.object.annotation.*}</i> as well as
   * <i>{@code com.github.dm.jrt.android.object.annotation.*}</i> annotations.
   * <br>
   * Note that such annotations will override any configuration set through the builder.
   *
   * @param itf    the token of the interface implemented by the return object.
   * @param <TYPE> the interface type.
   * @return the proxy object.
   * @throws java.lang.IllegalArgumentException if the specified class token does not represent an
   *                                            interface.
   * @see <a href='{@docRoot}/com/github/dm/jrt/android/object/annotation/package-summary.html'>
   * Android Annotations</a>
   * @see com.github.dm.jrt.object.annotation Annotations
   */
  @NotNull
  @Override
  <TYPE> TYPE buildProxy(@NotNull ClassToken<TYPE> itf);

  /**
   * Returns a routine used to call the method whose identifying name is specified in an
   * {@link com.github.dm.jrt.object.annotation.Alias Alias} annotation.
   * <p>
   * If no method with the specified alias is found, this method will behave like
   * {@link #method(String, Class[])} with no parameter.
   * <br>
   * Optional <i>{@code com.github.dm.jrt.object.annotation.*}</i> as well as
   * <i>{@code com.github.dm.jrt.android.object.annotation.*}</i> method annotations will be
   * honored.
   * <br>
   * Note that such annotations will override any configuration set through the builder.
   *
   * @param name  the name specified in the annotation.
   * @param <IN>  the input data type.
   * @param <OUT> the output data type.
   * @return the routine.
   * @throws java.lang.IllegalArgumentException if the specified method is not found.
   * @see <a href='{@docRoot}/com/github/dm/jrt/android/object/annotation/package-summary.html'>
   * Android Annotations</a>
   * @see com.github.dm.jrt.object.annotation Annotations
   */
  @NotNull
  @Override
  <IN, OUT> LoaderRoutine<IN, OUT> method(@NotNull String name);

  /**
   * Returns a routine used to call the specified method.
   * <p>
   * The method is searched via reflection ignoring a name specified in an
   * {@link com.github.dm.jrt.object.annotation.Alias Alias} annotation. Though, optional
   * <i>{@code com.github.dm.jrt.object.annotation.*}</i> as well as
   * <i>{@code com.github.dm.jrt.android.object.annotation.*}</i> method annotations will be
   * honored.
   * <br>
   * Note that such annotations will override any configuration set through the builder.
   *
   * @param name           the method name.
   * @param parameterTypes the method parameter types.
   * @param <IN>           the input data type.
   * @param <OUT>          the output data type.
   * @return the routine.
   * @throws java.lang.IllegalArgumentException if no matching method is found.
   * @see <a href='{@docRoot}/com/github/dm/jrt/android/object/annotation/package-summary.html'>
   * Android Annotations</a>
   * @see com.github.dm.jrt.object.annotation Annotations
   */
  @NotNull
  @Override
  <IN, OUT> LoaderRoutine<IN, OUT> method(@NotNull String name,
      @NotNull Class<?>... parameterTypes);

  /**
   * Returns a routine used to call the specified method.
   * <p>
   * The method is invoked ignoring a name specified in an
   * {@link com.github.dm.jrt.object.annotation.Alias Alias} annotation. Though, optional
   * <i>{@code com.github.dm.jrt.object.annotation.*}</i> as well as
   * <i>{@code com.github.dm.jrt.android.object.annotation.*}</i> method annotations will be
   * honored.
   * <br>
   * Note that such annotations will override any configuration set through the builder.
   *
   * @param method the method instance.
   * @param <IN>   the input data type.
   * @param <OUT>  the output data type.
   * @return the routine.
   * @see <a href='{@docRoot}/com/github/dm/jrt/android/object/annotation/package-summary.html'>
   * Android Annotations</a>
   * @see com.github.dm.jrt.object.annotation Annotations
   */
  @NotNull
  @Override
  <IN, OUT> LoaderRoutine<IN, OUT> method(@NotNull Method method);
}
