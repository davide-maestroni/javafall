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

package com.github.dm.jrt.android.v11.object;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.test.ActivityInstrumentationTestCase2;

import com.github.dm.jrt.android.core.config.LoaderConfiguration;
import com.github.dm.jrt.android.core.invocation.CallContextInvocation;
import com.github.dm.jrt.android.core.invocation.ContextInvocationFactory;
import com.github.dm.jrt.android.object.builder.FactoryContextWrapper;
import com.github.dm.jrt.android.object.builder.LoaderObjectRoutineBuilder;
import com.github.dm.jrt.android.v11.core.JRoutineLoader;
import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.channel.AbortException;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.config.ChannelConfiguration.OrderType;
import com.github.dm.jrt.core.config.ChannelConfiguration.TimeoutActionType;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.invocation.InvocationException;
import com.github.dm.jrt.core.log.Log;
import com.github.dm.jrt.core.log.Log.Level;
import com.github.dm.jrt.core.log.NullLog;
import com.github.dm.jrt.core.routine.InvocationMode;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.runner.Runners;
import com.github.dm.jrt.core.util.ClassToken;
import com.github.dm.jrt.core.util.Reflection;
import com.github.dm.jrt.core.util.UnitDuration;
import com.github.dm.jrt.object.annotation.Alias;
import com.github.dm.jrt.object.annotation.AsyncInput;
import com.github.dm.jrt.object.annotation.AsyncInput.InputMode;
import com.github.dm.jrt.object.annotation.AsyncMethod;
import com.github.dm.jrt.object.annotation.AsyncOutput;
import com.github.dm.jrt.object.annotation.AsyncOutput.OutputMode;
import com.github.dm.jrt.object.annotation.Invoke;
import com.github.dm.jrt.object.annotation.OutputTimeout;
import com.github.dm.jrt.object.annotation.OutputTimeoutAction;
import com.github.dm.jrt.object.annotation.SharedFields;
import com.github.dm.jrt.object.config.ObjectConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.android.core.invocation.ContextInvocationFactory.factoryOf;
import static com.github.dm.jrt.android.object.ContextInvocationTarget.classOfType;
import static com.github.dm.jrt.android.object.ContextInvocationTarget.instanceOf;
import static com.github.dm.jrt.android.v11.core.LoaderContext.loaderFrom;
import static com.github.dm.jrt.core.common.BackoffBuilder.afterCount;
import static com.github.dm.jrt.core.config.InvocationConfiguration.builder;
import static com.github.dm.jrt.core.util.UnitDuration.infinity;
import static com.github.dm.jrt.core.util.UnitDuration.seconds;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Loader object routine Activity unit tests.
 * <p>
 * Created by davide-maestroni on 04/07/2015.
 */
@TargetApi(VERSION_CODES.FROYO)
public class LoaderObjectRoutineActivityTest
    extends ActivityInstrumentationTestCase2<TestActivity> {

  public LoaderObjectRoutineActivityTest() {

    super(TestActivity.class);
  }

  @SuppressWarnings("ConstantConditions")
  public void testActivityNullPointerErrors() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoader.on(null).with(factoryOf(TestInvocation.class));

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineLoader.on(loaderFrom(getActivity())).with((ContextInvocationFactory<?, ?>) null);

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineLoaderObject.on(null).with(classOfType(TestInvocation.class));

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineLoaderObject.on(null).with(instanceOf(TestInvocation.class, Reflection.NO_ARGS));

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity())).with(classOfType((Class<?>) null));

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity())).with(instanceOf(null, Reflection.NO_ARGS));

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineLoaderObject.on(null);

      fail();

    } catch (final NullPointerException ignored) {

    }
  }

  public void testAliasMethod() throws NoSuchMethodException {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final UnitDuration timeout = seconds(10);
    final Routine<Object, Object> routine = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                                                .with(instanceOf(TestClass.class))
                                                                .applyInvocationConfiguration()
                                                                .withRunner(Runners.poolRunner())
                                                                .withMaxInstances(1)
                                                                .withCoreInstances(1)
                                                                .withOutputTimeoutAction(
                                                                    TimeoutActionType.CONTINUE)
                                                                .withLogLevel(Level.DEBUG)
                                                                .withLog(new NullLog())
                                                                .configured()
                                                                .method(TestClass.GET);

    assertThat(routine.close().after(timeout).all()).containsExactly(-77L);
  }

  public void testArgs() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    assertThat(JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                   .with(instanceOf(TestArgs.class, 17))
                                   .method("getId")
                                   .close()
                                   .after(seconds(10))
                                   .next()).isEqualTo(17);
  }

  public void testAsyncInputProxyRoutine() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final UnitDuration timeout = seconds(10);
    final SumItf sumAsync = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                                .with(instanceOf(Sum.class))
                                                .applyInvocationConfiguration()
                                                .withOutputTimeout(timeout)
                                                .configured()
                                                .buildProxy(SumItf.class);
    final Channel<Integer, Integer> channel3 = JRoutineCore.io().buildChannel();
    channel3.pass(7).close();
    assertThat(sumAsync.compute(3, channel3)).isEqualTo(10);

    final Channel<Integer, Integer> channel4 = JRoutineCore.io().buildChannel();
    channel4.pass(1, 2, 3, 4).close();
    assertThat(sumAsync.compute(channel4)).isEqualTo(10);

    final Channel<int[], int[]> channel5 = JRoutineCore.io().buildChannel();
    channel5.pass(new int[]{1, 2, 3, 4}).close();
    assertThat(sumAsync.compute1(channel5)).isEqualTo(10);

    final Channel<Integer, Integer> channel6 = JRoutineCore.io().buildChannel();
    channel6.pass(1, 2, 3, 4).close();
    assertThat(sumAsync.computeList(channel6)).isEqualTo(10);

    final Channel<Integer, Integer> channel7 = JRoutineCore.io().buildChannel();
    channel7.pass(1, 2, 3, 4).close();
    assertThat(sumAsync.computeList1(channel7)).isEqualTo(10);
  }

  public void testAsyncOutputProxyRoutine() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final UnitDuration timeout = seconds(10);
    final CountItf countAsync = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                                    .with(instanceOf(Count.class))
                                                    .applyInvocationConfiguration()
                                                    .withOutputTimeout(timeout)
                                                    .configured()
                                                    .buildProxy(CountItf.class);
    assertThat(countAsync.count(3).all()).containsExactly(0, 1, 2);
    assertThat(countAsync.count1(3).all()).containsExactly(new int[]{0, 1, 2});
    assertThat(countAsync.count2(2).all()).containsExactly(0, 1);
    assertThat(countAsync.countList(3).all()).containsExactly(0, 1, 2);
    assertThat(countAsync.countList1(3).all()).containsExactly(0, 1, 2);
  }

  @SuppressWarnings("ConstantConditions")
  public void testConfigurationErrors() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      new DefaultLoaderObjectRoutineBuilder(loaderFrom(getActivity()),
          instanceOf(TestClass.class)).apply((InvocationConfiguration) null);

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      new DefaultLoaderObjectRoutineBuilder(loaderFrom(getActivity()),
          instanceOf(TestClass.class)).apply((ObjectConfiguration) null);

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      new DefaultLoaderObjectRoutineBuilder(loaderFrom(getActivity()),
          instanceOf(TestClass.class)).apply((LoaderConfiguration) null);

      fail();

    } catch (final NullPointerException ignored) {

    }
  }

  public void testConfigurationWarnings() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final CountLog countLog = new CountLog();
    final InvocationConfiguration configuration = builder().withRunner(Runners.poolRunner())
                                                           .withInputOrder(OrderType.UNSORTED)
                                                           .withInputBackoff(
                                                               afterCount(3).constantDelay(
                                                                   seconds(10)))
                                                           .withInputMaxSize(33)
                                                           .withOutputOrder(OrderType.UNSORTED)
                                                           .withOutputBackoff(
                                                               afterCount(3).constantDelay(
                                                                   seconds(10)))
                                                           .withOutputMaxSize(33)
                                                           .withLogLevel(Level.DEBUG)
                                                           .withLog(countLog)
                                                           .configured();
    JRoutineLoaderObject.on(loaderFrom(getActivity()))
                        .with(instanceOf(TestClass.class))
                        .applyInvocationConfiguration()
                        .with(configuration)
                        .configured()
                        .applyObjectConfiguration()
                        .withSharedFields("test")
                        .configured()
                        .method(TestClass.GET);
    assertThat(countLog.getWrnCount()).isEqualTo(1);

    JRoutineLoaderObject.on(loaderFrom(getActivity()))
                        .with(instanceOf(Square.class))
                        .applyInvocationConfiguration()
                        .with(configuration)
                        .configured()
                        .applyObjectConfiguration()
                        .withSharedFields("test")
                        .configured()
                        .buildProxy(SquareItf.class)
                        .compute(3);
    assertThat(countLog.getWrnCount()).isEqualTo(2);
  }

  public void testConstructor() {

    boolean failed = false;
    try {
      new JRoutineLoaderObject();
      failed = true;

    } catch (final Throwable ignored) {

    }

    assertThat(failed).isFalse();
  }

  public void testContextWrapper() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final TestActivity activity = getActivity();
    final StringContext contextWrapper = new StringContext(activity);
    assertThat(JRoutineLoaderObject.on(loaderFrom(activity, contextWrapper))
                                   .with(instanceOf(String.class))
                                   .method("toString")
                                   .close()
                                   .after(seconds(10))
                                   .next()).isEqualTo("test1");
  }

  public void testDuplicateAnnotationError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(DuplicateAnnotation.class))
                          .method(DuplicateAnnotation.GET);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  public void testException() throws NoSuchMethodException {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final UnitDuration timeout = seconds(10);
    final Routine<Object, Object> routine3 = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                                                 .with(instanceOf(TestClass.class))
                                                                 .method(TestClass.THROW);

    try {

      routine3.call(new IllegalArgumentException("test")).after(timeout).all();

      fail();

    } catch (final InvocationException e) {

      assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
      assertThat(e.getCause().getMessage()).isEqualTo("test");
    }
  }

  public void testInvalidProxyError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .buildProxy(TestClass.class);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .buildProxy(ClassToken.tokenOf(TestClass.class));

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  public void testInvalidProxyInputAnnotationError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Sum.class))
                          .buildProxy(SumError.class)
                          .compute(1, new int[0]);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Sum.class))
                          .buildProxy(SumError.class)
                          .compute(new String[0]);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Sum.class))
                          .buildProxy(SumError.class)
                          .compute(new int[0]);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Sum.class))
                          .buildProxy(SumError.class)
                          .compute(Collections.<Integer>emptyList());

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    final Channel<Integer, Integer> channel = JRoutineCore.io().buildChannel();

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Sum.class))
                          .buildProxy(SumError.class)
                          .compute(channel);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Sum.class))
                          .buildProxy(SumError.class)
                          .compute(1, channel);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Sum.class))
                          .buildProxy(SumError.class)
                          .compute("test", channel);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  public void testInvalidProxyMethodError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .applyInvocationConfiguration()
                          .withOutputTimeout(infinity())
                          .configured()
                          .buildProxy(TestItf.class)
                          .throwException(null);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .applyInvocationConfiguration()
                          .withOutputTimeout(infinity())
                          .configured()
                          .buildProxy(TestItf.class)
                          .throwException1(null);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .applyInvocationConfiguration()
                          .withOutputTimeout(infinity())
                          .configured()
                          .buildProxy(TestItf.class)
                          .throwException2(null);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  public void testInvalidProxyOutputAnnotationError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Count.class))
                          .buildProxy(CountError.class)
                          .count(3);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Count.class))
                          .buildProxy(CountError.class)
                          .count1(3);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Count.class))
                          .buildProxy(CountError.class)
                          .countList(3);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(Count.class))
                          .buildProxy(CountError.class)
                          .countList1(3);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  public void testMethod() throws NoSuchMethodException {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final UnitDuration timeout = seconds(10);
    final Routine<Object, Object> routine2 = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                                                 .with(instanceOf(TestClass.class))
                                                                 .applyInvocationConfiguration()
                                                                 .withRunner(Runners.poolRunner())
                                                                 .withMaxInstances(1)
                                                                 .configured()
                                                                 .applyObjectConfiguration()
                                                                 .withSharedFields("test")
                                                                 .configured()
                                                                 .method(TestClass.class.getMethod(
                                                                     "getLong"));

    assertThat(routine2.close().after(timeout).all()).containsExactly(-77L);

  }

  public void testMethodBySignature() throws NoSuchMethodException {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final UnitDuration timeout = seconds(10);
    final Routine<Object, Object> routine1 = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                                                 .with(instanceOf(TestClass.class))
                                                                 .applyInvocationConfiguration()
                                                                 .withRunner(Runners.poolRunner())
                                                                 .configured()
                                                                 .method("getLong");

    assertThat(routine1.close().after(timeout).all()).containsExactly(-77L);
  }

  public void testMissingAliasMethodError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .method("test");

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  public void testMissingMethodError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .method("test");

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  @SuppressWarnings("ConstantConditions")
  public void testNullPointerError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity())).with(null);

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity())).with(instanceOf(null));

      fail();

    } catch (final NullPointerException ignored) {

    }
  }

  @SuppressWarnings("ConstantConditions")
  public void testNullProxyError() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .buildProxy((Class<?>) null);

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestClass.class))
                          .buildProxy((ClassToken<?>) null);

      fail();

    } catch (final NullPointerException ignored) {

    }
  }

  @SuppressWarnings("unchecked")
  public void testProxyAnnotations() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final Itf itf = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                        .with(instanceOf(Impl.class))
                                        .applyInvocationConfiguration()
                                        .withOutputTimeout(seconds(10))
                                        .configured()
                                        .buildProxy(Itf.class);

    assertThat(itf.add0('c')).isEqualTo((int) 'c');
    final Channel<Character, Character> channel1 = JRoutineCore.io().buildChannel();
    channel1.pass('a').close();
    assertThat(itf.add1(channel1)).isEqualTo((int) 'a');
    final Channel<Character, Character> channel2 = JRoutineCore.io().buildChannel();
    channel2.pass('d', 'e', 'f').close();
    assertThat(itf.add2(channel2)).isIn((int) 'd', (int) 'e', (int) 'f');
    assertThat(itf.add3('c').all()).containsExactly((int) 'c');
    final Channel<Character, Character> channel3 = JRoutineCore.io().buildChannel();
    channel3.pass('a').close();
    assertThat(itf.add4(channel3).all()).containsExactly((int) 'a');
    final Channel<Character, Character> channel4 = JRoutineCore.io().buildChannel();
    channel4.pass('d', 'e', 'f').close();
    assertThat(itf.add5(channel4).all()).containsOnly((int) 'd', (int) 'e', (int) 'f');
    assertThat(itf.add6().pass('d').close().all()).containsOnly((int) 'd');
    assertThat(itf.add7().pass('d', 'e', 'f').close().all()).containsOnly((int) 'd', (int) 'e',
        (int) 'f');
    assertThat(itf.add10().call('d').all()).containsOnly((int) 'd');
    assertThat(itf.add11().callParallel('d', 'e', 'f').all()).containsOnly((int) 'd', (int) 'e',
        (int) 'f');
    assertThat(itf.addA00(new char[]{'c', 'z'})).isEqualTo(new int[]{'c', 'z'});
    final Channel<char[], char[]> channel5 = JRoutineCore.io().buildChannel();
    channel5.pass(new char[]{'a', 'z'}).close();
    assertThat(itf.addA01(channel5)).isEqualTo(new int[]{'a', 'z'});
    final Channel<Character, Character> channel6 = JRoutineCore.io().buildChannel();
    channel6.pass('d', 'e', 'f').close();
    assertThat(itf.addA02(channel6)).isEqualTo(new int[]{'d', 'e', 'f'});
    final Channel<char[], char[]> channel7 = JRoutineCore.io().buildChannel();
    channel7.pass(new char[]{'d', 'z'}, new char[]{'e', 'z'}, new char[]{'f', 'z'}).close();
    assertThat(itf.addA03(channel7)).isIn(new int[]{'d', 'z'}, new int[]{'e', 'z'},
        new int[]{'f', 'z'});
    assertThat(itf.addA04(new char[]{'c', 'z'}).all()).containsExactly(new int[]{'c', 'z'});
    final Channel<char[], char[]> channel8 = JRoutineCore.io().buildChannel();
    channel8.pass(new char[]{'a', 'z'}).close();
    assertThat(itf.addA05(channel8).all()).containsExactly(new int[]{'a', 'z'});
    final Channel<Character, Character> channel9 = JRoutineCore.io().buildChannel();
    channel9.pass('d', 'e', 'f').close();
    assertThat(itf.addA06(channel9).all()).containsExactly(new int[]{'d', 'e', 'f'});
    final Channel<char[], char[]> channel10 = JRoutineCore.io().buildChannel();
    channel10.pass(new char[]{'d', 'z'}, new char[]{'e', 'z'}, new char[]{'f', 'z'}).close();
    assertThat(itf.addA07(channel10).all()).containsOnly(new int[]{'d', 'z'}, new int[]{'e', 'z'},
        new int[]{'f', 'z'});
    assertThat(itf.addA08(new char[]{'c', 'z'}).all()).containsExactly((int) 'c', (int) 'z');
    final Channel<char[], char[]> channel11 = JRoutineCore.io().buildChannel();
    channel11.pass(new char[]{'a', 'z'}).close();
    assertThat(itf.addA09(channel11).all()).containsExactly((int) 'a', (int) 'z');
    final Channel<Character, Character> channel12 = JRoutineCore.io().buildChannel();
    channel12.pass('d', 'e', 'f').close();
    assertThat(itf.addA10(channel12).all()).containsExactly((int) 'd', (int) 'e', (int) 'f');
    final Channel<char[], char[]> channel13 = JRoutineCore.io().buildChannel();
    channel13.pass(new char[]{'d', 'z'}, new char[]{'e', 'z'}, new char[]{'f', 'z'}).close();
    assertThat(itf.addA11(channel13).all()).containsOnly((int) 'd', (int) 'e', (int) 'f',
        (int) 'z');
    assertThat(itf.addA12().pass(new char[]{'c', 'z'}).close().all()).containsOnly(
        new int[]{'c', 'z'});
    assertThat(itf.addA13()
                  .pass(new char[]{'d', 'z'}, new char[]{'e', 'z'}, new char[]{'f', 'z'})
                  .close()
                  .all()).containsOnly(new int[]{'d', 'z'}, new int[]{'e', 'z'},
        new int[]{'f', 'z'});
    assertThat(itf.addA14().call(new char[]{'c', 'z'}).all()).containsOnly(new int[]{'c', 'z'});
    assertThat(itf.addA15()
                  .callParallel(new char[]{'d', 'z'}, new char[]{'e', 'z'}, new char[]{'f', 'z'})
                  .all()).containsOnly(new int[]{'d', 'z'}, new int[]{'e', 'z'},
        new int[]{'f', 'z'});
    assertThat(itf.addA16().pass(new char[]{'c', 'z'}).close().all()).containsExactly((int) 'c',
        (int) 'z');
    assertThat(itf.addA17()
                  .pass(new char[]{'d', 'z'}, new char[]{'e', 'z'}, new char[]{'f', 'z'})
                  .close()
                  .all()).containsOnly((int) 'd', (int) 'z', (int) 'e', (int) 'z', (int) 'f',
        (int) 'z');
    assertThat(itf.addA18().call(new char[]{'c', 'z'}).all()).containsExactly((int) 'c', (int) 'z');
    assertThat(itf.addA19()
                  .callParallel(new char[]{'d', 'z'}, new char[]{'e', 'z'}, new char[]{'f', 'z'})
                  .all()).containsOnly((int) 'd', (int) 'z', (int) 'e', (int) 'z', (int) 'f',
        (int) 'z');
    assertThat(itf.addL00(Arrays.asList('c', 'z'))).isEqualTo(Arrays.asList((int) 'c', (int) 'z'));
    final Channel<List<Character>, List<Character>> channel20 = JRoutineCore.io().buildChannel();
    channel20.pass(Arrays.asList('a', 'z')).close();
    assertThat(itf.addL01(channel20)).isEqualTo(Arrays.asList((int) 'a', (int) 'z'));
    final Channel<Character, Character> channel21 = JRoutineCore.io().buildChannel();
    channel21.pass('d', 'e', 'f').close();
    assertThat(itf.addL02(channel21)).isEqualTo(Arrays.asList((int) 'd', (int) 'e', (int) 'f'));
    final Channel<List<Character>, List<Character>> channel22 = JRoutineCore.io().buildChannel();
    channel22.pass(Arrays.asList('d', 'z'), Arrays.asList('e', 'z'), Arrays.asList('f', 'z'))
             .close();
    assertThat(itf.addL03(channel22)).isIn(Arrays.asList((int) 'd', (int) 'z'),
        Arrays.asList((int) 'e', (int) 'z'), Arrays.asList((int) 'f', (int) 'z'));
    assertThat(itf.addL04(Arrays.asList('c', 'z')).all()).containsExactly(
        Arrays.asList((int) 'c', (int) 'z'));
    final Channel<List<Character>, List<Character>> channel23 = JRoutineCore.io().buildChannel();
    channel23.pass(Arrays.asList('a', 'z')).close();
    assertThat(itf.addL05(channel23).all()).containsExactly(Arrays.asList((int) 'a', (int) 'z'));
    final Channel<Character, Character> channel24 = JRoutineCore.io().buildChannel();
    channel24.pass('d', 'e', 'f').close();
    assertThat(itf.addL06(channel24).all()).containsExactly(
        Arrays.asList((int) 'd', (int) 'e', (int) 'f'));
    final Channel<List<Character>, List<Character>> channel25 = JRoutineCore.io().buildChannel();
    channel25.pass(Arrays.asList('d', 'z'), Arrays.asList('e', 'z'), Arrays.asList('f', 'z'))
             .close();
    assertThat(itf.addL07(channel25).all()).containsOnly(Arrays.asList((int) 'd', (int) 'z'),
        Arrays.asList((int) 'e', (int) 'z'), Arrays.asList((int) 'f', (int) 'z'));
    assertThat(itf.addL08(Arrays.asList('c', 'z')).all()).containsExactly((int) 'c', (int) 'z');
    final Channel<List<Character>, List<Character>> channel26 = JRoutineCore.io().buildChannel();
    channel26.pass(Arrays.asList('a', 'z')).close();
    assertThat(itf.addL09(channel26).all()).containsExactly((int) 'a', (int) 'z');
    final Channel<Character, Character> channel27 = JRoutineCore.io().buildChannel();
    channel27.pass('d', 'e', 'f').close();
    assertThat(itf.addL10(channel27).all()).containsExactly((int) 'd', (int) 'e', (int) 'f');
    final Channel<List<Character>, List<Character>> channel28 = JRoutineCore.io().buildChannel();
    channel28.pass(Arrays.asList('d', 'z'), Arrays.asList('e', 'z'), Arrays.asList('f', 'z'))
             .close();
    assertThat(itf.addL11(channel28).all()).containsOnly((int) 'd', (int) 'e', (int) 'f',
        (int) 'z');
    assertThat(itf.addL12().pass(Arrays.asList('c', 'z')).close().all()).containsOnly(
        Arrays.asList((int) 'c', (int) 'z'));
    assertThat(itf.addL13()
                  .pass(Arrays.asList('d', 'z'), Arrays.asList('e', 'z'), Arrays.asList('f', 'z'))
                  .close()
                  .all()).containsOnly(Arrays.asList((int) 'd', (int) 'z'),
        Arrays.asList((int) 'e', (int) 'z'), Arrays.asList((int) 'f', (int) 'z'));
    assertThat(itf.addL14().call(Arrays.asList('c', 'z')).all()).containsOnly(
        Arrays.asList((int) 'c', (int) 'z'));
    assertThat(itf.addL15()
                  .callParallel(Arrays.asList('d', 'z'), Arrays.asList('e', 'z'),
                      Arrays.asList('f', 'z'))
                  .all()).containsOnly(Arrays.asList((int) 'd', (int) 'z'),
        Arrays.asList((int) 'e', (int) 'z'), Arrays.asList((int) 'f', (int) 'z'));
    assertThat(itf.addL16().pass(Arrays.asList('c', 'z')).close().all()).containsExactly((int) 'c',
        (int) 'z');
    assertThat(itf.addL17()
                  .pass(Arrays.asList('d', 'z'), Arrays.asList('e', 'z'), Arrays.asList('f', 'z'))
                  .close()
                  .all()).containsOnly((int) 'd', (int) 'z', (int) 'e', (int) 'z', (int) 'f',
        (int) 'z');
    assertThat(itf.addL18().call(Arrays.asList('c', 'z')).all()).containsExactly((int) 'c',
        (int) 'z');
    assertThat(itf.addL19()
                  .callParallel(Arrays.asList('d', 'z'), Arrays.asList('e', 'z'),
                      Arrays.asList('f', 'z'))
                  .all()).containsOnly((int) 'd', (int) 'z', (int) 'e', (int) 'z', (int) 'f',
        (int) 'z');
    assertThat(itf.get0()).isEqualTo(31);
    assertThat(itf.get1().all()).containsExactly(31);
    assertThat(itf.get2().close().all()).containsExactly(31);
    assertThat(itf.get4().close().all()).containsExactly(31);
    assertThat(itf.getA0()).isEqualTo(new int[]{1, 2, 3});
    assertThat(itf.getA1().all()).containsExactly(1, 2, 3);
    assertThat(itf.getA2().close().all()).containsExactly(new int[]{1, 2, 3});
    assertThat(itf.getA3().close().all()).containsExactly(new int[]{1, 2, 3});
    assertThat(itf.getA4().close().all()).containsExactly(1, 2, 3);
    assertThat(itf.getA5().close().all()).containsExactly(1, 2, 3);
    assertThat(itf.getL0()).isEqualTo(Arrays.asList(1, 2, 3));
    assertThat(itf.getL1().all()).containsExactly(1, 2, 3);
    assertThat(itf.getL2().close().all()).containsExactly(Arrays.asList(1, 2, 3));
    assertThat(itf.getL3().close().all()).containsExactly(Arrays.asList(1, 2, 3));
    assertThat(itf.getL4().close().all()).containsExactly(1, 2, 3);
    assertThat(itf.getL5().close().all()).containsExactly(1, 2, 3);
    itf.set0(-17);
    final Channel<Integer, Integer> channel35 = JRoutineCore.io().buildChannel();
    channel35.pass(-17).close();
    itf.set1(channel35);
    final Channel<Integer, Integer> channel36 = JRoutineCore.io().buildChannel();
    channel36.pass(-17).close();
    itf.set2(channel36);
    itf.set3().pass(-17).close().getComplete();
    itf.set5().call(-17).getComplete();
    itf.setA0(new int[]{1, 2, 3});
    final Channel<int[], int[]> channel37 = JRoutineCore.io().buildChannel();
    channel37.pass(new int[]{1, 2, 3}).close();
    itf.setA1(channel37);
    final Channel<Integer, Integer> channel38 = JRoutineCore.io().buildChannel();
    channel38.pass(1, 2, 3).close();
    itf.setA2(channel38);
    final Channel<int[], int[]> channel39 = JRoutineCore.io().buildChannel();
    channel39.pass(new int[]{1, 2, 3}).close();
    itf.setA3(channel39);
    itf.setA4().pass(new int[]{1, 2, 3}).close().getComplete();
    itf.setA6().call(new int[]{1, 2, 3}).getComplete();
    itf.setL0(Arrays.asList(1, 2, 3));
    final Channel<List<Integer>, List<Integer>> channel40 = JRoutineCore.io().buildChannel();
    channel40.pass(Arrays.asList(1, 2, 3)).close();
    itf.setL1(channel40);
    final Channel<Integer, Integer> channel41 = JRoutineCore.io().buildChannel();
    channel41.pass(1, 2, 3).close();
    itf.setL2(channel41);
    final Channel<List<Integer>, List<Integer>> channel42 = JRoutineCore.io().buildChannel();
    channel42.pass(Arrays.asList(1, 2, 3)).close();
    itf.setL3(channel42);
    itf.setL4().pass(Arrays.asList(1, 2, 3)).close().getComplete();
    itf.setL6().call(Arrays.asList(1, 2, 3)).getComplete();
  }

  public void testProxyRoutine() {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final UnitDuration timeout = seconds(10);
    final SquareItf squareAsync = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                                      .with(instanceOf(Square.class))
                                                      .buildProxy(SquareItf.class);

    assertThat(squareAsync.compute(3)).isEqualTo(9);

    final Channel<Integer, Integer> channel1 = JRoutineCore.io().buildChannel();
    channel1.pass(4).close();
    assertThat(squareAsync.computeAsync(channel1)).isEqualTo(16);

    final Channel<Integer, Integer> channel2 = JRoutineCore.io().buildChannel();
    channel2.pass(1, 2, 3).close();
    assertThat(squareAsync.computeParallel(channel2).after(timeout).all()).containsOnly(1, 4, 9);
  }

  public void testSharedFields() throws NoSuchMethodException {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    final LoaderObjectRoutineBuilder builder = JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                                                   .with(
                                                                       instanceOf(TestClass2.class))
                                                                   .applyInvocationConfiguration()
                                                                   .withOutputTimeout(seconds(10))
                                                                   .configured();

    long startTime = System.currentTimeMillis();

    Channel<?, Object> getOne = builder.applyObjectConfiguration()
                                       .withSharedFields("1")
                                       .configured()
                                       .method("getOne")
                                       .close();
    Channel<?, Object> getTwo = builder.applyObjectConfiguration()
                                       .withSharedFields("2")
                                       .configured()
                                       .method("getTwo")
                                       .close();

    assertThat(getOne.getComplete()).isTrue();
    assertThat(getTwo.getComplete()).isTrue();
    assertThat(System.currentTimeMillis() - startTime).isLessThan(4000);

    startTime = System.currentTimeMillis();

    getOne = builder.method("getOne").close();
    getTwo = builder.method("getTwo").close();

    assertThat(getOne.getComplete()).isTrue();
    assertThat(getTwo.getComplete()).isTrue();
    assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(4000);
  }

  public void testTimeoutActionAnnotation() throws NoSuchMethodException {

    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

      return;
    }

    assertThat(JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                   .with(instanceOf(TestTimeout.class))
                                   .applyInvocationConfiguration()
                                   .withOutputTimeout(seconds(10))
                                   .configured()
                                   .applyLoaderConfiguration()
                                   .withLoaderId(0)
                                   .configured()
                                   .method("test")
                                   .close()
                                   .next()).isEqualTo(31);

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestTimeout.class))
                          .applyInvocationConfiguration()
                          .withOutputTimeoutAction(TimeoutActionType.FAIL)
                          .configured()
                          .applyLoaderConfiguration()
                          .withLoaderId(1)
                          .configured()
                          .method("test")
                          .close()
                          .next();

      fail();

    } catch (final NoSuchElementException ignored) {

    }

    assertThat(JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                   .with(instanceOf(TestTimeout.class))
                                   .applyInvocationConfiguration()
                                   .withOutputTimeout(seconds(10))
                                   .configured()
                                   .applyLoaderConfiguration()
                                   .withLoaderId(2)
                                   .configured()
                                   .method("getInt")
                                   .close()
                                   .next()).isEqualTo(31);

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestTimeout.class))
                          .applyInvocationConfiguration()
                          .withOutputTimeoutAction(TimeoutActionType.FAIL)
                          .configured()
                          .applyLoaderConfiguration()
                          .withLoaderId(3)
                          .configured()
                          .method("getInt")
                          .close()
                          .next();

      fail();

    } catch (final NoSuchElementException ignored) {

    }

    assertThat(JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                   .with(instanceOf(TestTimeout.class))
                                   .applyInvocationConfiguration()
                                   .withOutputTimeout(seconds(10))
                                   .configured()
                                   .applyLoaderConfiguration()
                                   .withLoaderId(4)
                                   .configured()
                                   .method(TestTimeout.class.getMethod("getInt"))
                                   .close()
                                   .next()).isEqualTo(31);

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestTimeout.class))
                          .applyInvocationConfiguration()
                          .withOutputTimeoutAction(TimeoutActionType.FAIL)
                          .configured()
                          .applyLoaderConfiguration()
                          .withLoaderId(5)
                          .configured()
                          .method(TestTimeout.class.getMethod("getInt"))
                          .close()
                          .next();

      fail();

    } catch (final NoSuchElementException ignored) {

    }

    assertThat(JRoutineLoaderObject.on(loaderFrom(getActivity()))
                                   .with(instanceOf(TestTimeout.class))
                                   .applyInvocationConfiguration()
                                   .withOutputTimeout(seconds(10))
                                   .configured()
                                   .applyLoaderConfiguration()
                                   .withLoaderId(6)
                                   .configured()
                                   .buildProxy(TestTimeoutItf.class)
                                   .getInt()).isEqualTo(31);

    try {

      JRoutineLoaderObject.on(loaderFrom(getActivity()))
                          .with(instanceOf(TestTimeout.class))
                          .applyInvocationConfiguration()
                          .withOutputTimeoutAction(TimeoutActionType.FAIL)
                          .configured()
                          .applyLoaderConfiguration()
                          .withLoaderId(7)
                          .configured()
                          .buildProxy(TestTimeoutItf.class)
                          .getInt();

      fail();

    } catch (final AbortException ignored) {

    }
  }

  public interface Itf {

    @Alias("a")
    int add0(char c);

    @Alias("a")
    int add1(@AsyncInput(value = char.class, mode = InputMode.VALUE) Channel<?, Character> c);

    @Alias("a")
    @AsyncMethod(char.class)
    Routine<Character, Integer> add10();

    @Alias("a")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(char.class)
    Routine<Character, Integer> add11();

    @Alias("a")
    @Invoke(InvocationMode.PARALLEL)
    int add2(@AsyncInput(value = char.class, mode = InputMode.VALUE) Channel<?, Character> c);

    @Alias("a")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, Integer> add3(char c);

    @Alias("a")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, Integer> add4(
        @AsyncInput(value = char.class, mode = InputMode.VALUE) Channel<?, Character> c);

    @Alias("a")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, Integer> add5(
        @AsyncInput(value = char.class, mode = InputMode.VALUE) Channel<?, Character> c);

    @Alias("a")
    @AsyncMethod(char.class)
    Channel<Character, Integer> add6();

    @Alias("a")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(char.class)
    Channel<Character, Integer> add7();

    @Alias("aa")
    int[] addA00(char[] c);

    @Alias("aa")
    int[] addA01(@AsyncInput(value = char[].class,
        mode = InputMode.VALUE) Channel<?, char[]> c);

    @Alias("aa")
    int[] addA02(@AsyncInput(value = char[].class,
        mode = InputMode.COLLECTION) Channel<?, Character> c);

    @Alias("aa")
    @Invoke(InvocationMode.PARALLEL)
    int[] addA03(@AsyncInput(value = char[].class,
        mode = InputMode.VALUE) Channel<?, char[]> c);

    @Alias("aa")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, int[]> addA04(char[] c);

    @Alias("aa")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, int[]> addA05(
        @AsyncInput(value = char[].class, mode = InputMode.VALUE) Channel<?, char[]> c);

    @Alias("aa")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, int[]> addA06(@AsyncInput(value = char[].class,
        mode = InputMode.COLLECTION) Channel<?, Character> c);

    @Alias("aa")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, int[]> addA07(@AsyncInput(value = char[].class,
        mode = InputMode.VALUE) Channel<?, char[]> c);

    @Alias("aa")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> addA08(char[] c);

    @Alias("aa")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> addA09(
        @AsyncInput(value = char[].class, mode = InputMode.VALUE) Channel<?, char[]> c);

    @Alias("aa")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> addA10(@AsyncInput(value = char[].class,
        mode = InputMode.COLLECTION) Channel<?, Character> c);

    @Alias("aa")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> addA11(@AsyncInput(value = char[].class,
        mode = InputMode.VALUE) Channel<?, char[]> c);

    @Alias("aa")
    @AsyncMethod(char[].class)
    Channel<char[], int[]> addA12();

    @Alias("aa")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(char[].class)
    Channel<char[], int[]> addA13();

    @Alias("aa")
    @AsyncMethod(char[].class)
    Routine<char[], int[]> addA14();

    @Alias("aa")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(char[].class)
    Routine<char[], int[]> addA15();

    @Alias("aa")
    @AsyncMethod(value = char[].class, mode = OutputMode.ELEMENT)
    Channel<char[], Integer> addA16();

    @Alias("aa")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(value = char[].class, mode = OutputMode.ELEMENT)
    Channel<char[], Integer> addA17();

    @Alias("aa")
    @AsyncMethod(value = char[].class, mode = OutputMode.ELEMENT)
    Routine<char[], Integer> addA18();

    @Alias("aa")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(value = char[].class, mode = OutputMode.ELEMENT)
    Routine<char[], Integer> addA19();

    @Alias("al")
    List<Integer> addL00(List<Character> c);

    @Alias("al")
    List<Integer> addL01(@AsyncInput(value = List.class,
        mode = InputMode.VALUE) Channel<?, List<Character>> c);

    @Alias("al")
    List<Integer> addL02(@AsyncInput(value = List.class,
        mode = InputMode.COLLECTION) Channel<?, Character> c);

    @Alias("al")
    @Invoke(InvocationMode.PARALLEL)
    List<Integer> addL03(@AsyncInput(value = List.class,
        mode = InputMode.VALUE) Channel<?, List<Character>> c);

    @Alias("al")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, List<Integer>> addL04(List<Character> c);

    @Alias("al")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, List<Integer>> addL05(@AsyncInput(value = List.class,
        mode = InputMode.VALUE) Channel<?, List<Character>> c);

    @Alias("al")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, List<Integer>> addL06(@AsyncInput(value = List.class,
        mode = InputMode.COLLECTION) Channel<?, Character> c);

    @Alias("al")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, List<Integer>> addL07(@AsyncInput(value = List.class,
        mode = InputMode.VALUE) Channel<?, List<Character>> c);

    @Alias("al")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> addL08(List<Character> c);

    @Alias("al")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> addL09(@AsyncInput(value = List.class,
        mode = InputMode.VALUE) Channel<?, List<Character>> c);

    @Alias("al")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> addL10(@AsyncInput(value = List.class,
        mode = InputMode.COLLECTION) Channel<?, Character> c);

    @Alias("al")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> addL11(@AsyncInput(value = List.class,
        mode = InputMode.VALUE) Channel<?, List<Character>> c);

    @Alias("al")
    @AsyncMethod(List.class)
    Channel<List<Character>, List<Integer>> addL12();

    @Alias("al")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(List.class)
    Channel<List<Character>, List<Integer>> addL13();

    @Alias("al")
    @AsyncMethod(List.class)
    Routine<List<Character>, List<Integer>> addL14();

    @Alias("al")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(List.class)
    Routine<List<Character>, List<Integer>> addL15();

    @Alias("al")
    @AsyncMethod(value = List.class, mode = OutputMode.ELEMENT)
    Channel<List<Character>, Integer> addL16();

    @Alias("al")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(value = List.class, mode = OutputMode.ELEMENT)
    Channel<List<Character>, Integer> addL17();

    @Alias("al")
    @AsyncMethod(value = List.class, mode = OutputMode.ELEMENT)
    Routine<List<Character>, Integer> addL18();

    @Alias("al")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncMethod(value = List.class, mode = OutputMode.ELEMENT)
    Routine<List<Character>, Integer> addL19();

    @Alias("g")
    int get0();

    @Alias("s")
    void set0(int i);

    @Alias("g")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, Integer> get1();

    @Alias("s")
    void set1(@AsyncInput(value = int.class, mode = InputMode.VALUE) Channel<?, Integer> i);

    @Alias("g")
    @AsyncMethod({})
    Channel<Void, Integer> get2();

    @Alias("s")
    @Invoke(InvocationMode.PARALLEL)
    void set2(@AsyncInput(value = int.class, mode = InputMode.VALUE) Channel<?, Integer> i);

    @Alias("g")
    @AsyncMethod({})
    Routine<Void, Integer> get4();

    @Alias("ga")
    int[] getA0();

    @Alias("sa")
    void setA0(int[] i);

    @Alias("ga")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> getA1();

    @Alias("sa")
    void setA1(@AsyncInput(value = int[].class, mode = InputMode.VALUE) Channel<?, int[]> i);

    @Alias("ga")
    @AsyncMethod({})
    Channel<Void, int[]> getA2();

    @Alias("sa")
    void setA2(@AsyncInput(value = int[].class,
        mode = InputMode.COLLECTION) Channel<?, Integer> i);

    @Alias("ga")
    @AsyncMethod({})
    Routine<Void, int[]> getA3();

    @Alias("sa")
    @Invoke(InvocationMode.PARALLEL)
    void setA3(@AsyncInput(value = int[].class, mode = InputMode.VALUE) Channel<?, int[]> i);

    @Alias("ga")
    @AsyncMethod(value = {}, mode = OutputMode.ELEMENT)
    Channel<Void, Integer> getA4();

    @Alias("ga")
    @AsyncMethod(value = {}, mode = OutputMode.ELEMENT)
    Routine<Void, Integer> getA5();

    @Alias("gl")
    List<Integer> getL0();

    @Alias("sl")
    void setL0(List<Integer> i);

    @Alias("gl")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> getL1();

    @Alias("sl")
    void setL1(@AsyncInput(value = List.class,
        mode = InputMode.VALUE) Channel<?, List<Integer>> i);

    @Alias("gl")
    @AsyncMethod({})
    Channel<Void, List<Integer>> getL2();

    @Alias("sl")
    void setL2(@AsyncInput(value = List.class, mode = InputMode.COLLECTION) Channel<?, Integer> i);

    @Alias("gl")
    @AsyncMethod({})
    Routine<Void, List<Integer>> getL3();

    @Alias("sl")
    @Invoke(InvocationMode.PARALLEL)
    void setL3(@AsyncInput(value = List.class,
        mode = InputMode.VALUE) Channel<?, List<Integer>> i);

    @Alias("gl")
    @AsyncMethod(value = {}, mode = OutputMode.ELEMENT)
    Channel<Void, Integer> getL4();

    @Alias("gl")
    @AsyncMethod(value = {}, mode = OutputMode.ELEMENT)
    Routine<Void, Integer> getL5();

    @Alias("s")
    @AsyncMethod(int.class)
    Channel<Integer, Void> set3();

    @Alias("s")
    @AsyncMethod(int.class)
    Routine<Integer, Void> set5();

    @Alias("sa")
    @AsyncMethod(int[].class)
    Channel<int[], Void> setA4();

    @Alias("sa")
    @AsyncMethod(int[].class)
    Routine<int[], Void> setA6();

    @Alias("sl")
    @AsyncMethod(List.class)
    Channel<List<Integer>, Void> setL4();

    @Alias("sl")
    @AsyncMethod(List.class)
    Routine<List<Integer>, Void> setL6();
  }

  private interface CountError {

    @AsyncOutput
    String[] count(int length);

    @Alias("count")
    @AsyncOutput(OutputMode.ELEMENT)
    String[] count1(int length);

    @AsyncOutput(OutputMode.VALUE)
    List<Integer> countList(int length);

    @Alias("countList")
    @AsyncOutput(OutputMode.ELEMENT)
    List<Integer> countList1(int length);
  }

  private interface CountItf {

    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> count(int length);

    @Alias("count")
    @AsyncOutput(OutputMode.VALUE)
    Channel<?, int[]> count1(int length);

    @Alias("count")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> count2(int length);

    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> countList(int length);

    @Alias("countList")
    @AsyncOutput(OutputMode.ELEMENT)
    Channel<?, Integer> countList1(int length);
  }

  private interface SquareItf {

    @OutputTimeout(value = 10, unit = TimeUnit.SECONDS)
    int compute(int i);

    @Alias("compute")
    @OutputTimeout(10000)
    int computeAsync(@AsyncInput(int.class) Channel<?, Integer> i);

    @SharedFields({})
    @Alias("compute")
    @Invoke(InvocationMode.PARALLEL)
    @AsyncOutput
    Channel<?, Integer> computeParallel(
        @AsyncInput(value = int.class, mode = InputMode.VALUE) Channel<?, Integer> i);
  }

  private interface SumError {

    int compute(int a, @AsyncInput(int.class) int[] b);

    int compute(@AsyncInput(int.class) String[] ints);

    int compute(@AsyncInput(value = int.class, mode = InputMode.VALUE) int[] ints);

    int compute(@AsyncInput(value = int.class, mode = InputMode.COLLECTION) Iterable<Integer> ints);

    int compute(@AsyncInput(value = int.class,
        mode = InputMode.COLLECTION) Channel<?, Integer> ints);

    int compute(int a, @AsyncInput(value = int[].class,
        mode = InputMode.COLLECTION) Channel<?, Integer> b);

    @Invoke(InvocationMode.PARALLEL)
    int compute(String text,
        @AsyncInput(value = int.class, mode = InputMode.VALUE) Channel<?, Integer> ints);
  }

  private interface SumItf {

    int compute(int a, @AsyncInput(int.class) Channel<?, Integer> b);

    int compute(@AsyncInput(value = int[].class,
        mode = InputMode.COLLECTION) Channel<?, Integer> ints);

    @Alias("compute")
    int compute1(@AsyncInput(int[].class) Channel<?, int[]> ints);

    @Alias("compute")
    int computeList(@AsyncInput(value = List.class,
        mode = InputMode.COLLECTION) Channel<?, Integer> ints);

    @Alias("compute")
    int computeList1(@AsyncInput(value = List.class,
        mode = InputMode.COLLECTION) Channel<?, Integer> ints);
  }

  @SuppressWarnings("unused")
  private interface TestItf {

    void throwException(@AsyncInput(int.class) RuntimeException ex);

    @Alias(TestClass.THROW)
    @AsyncOutput
    void throwException1(RuntimeException ex);

    @Alias(TestClass.THROW)
    int throwException2(RuntimeException ex);
  }

  private interface TestTimeoutItf {

    @OutputTimeoutAction(TimeoutActionType.ABORT)
    int getInt();
  }

  @SuppressWarnings("unused")
  public static class Impl {

    @Alias("a")
    public int add(char c) {

      return c;
    }

    @Alias("aa")
    public int[] addArray(char[] c) {

      final int[] array = new int[c.length];

      for (int i = 0; i < c.length; i++) {

        array[i] = c[i];
      }

      return array;
    }

    @Alias("al")
    public List<Integer> addList(List<Character> c) {

      final ArrayList<Integer> list = new ArrayList<Integer>(c.size());

      for (final Character character : c) {

        list.add((int) character);
      }

      return list;
    }

    @Alias("g")
    public int get() {

      return 31;
    }

    @Alias("ga")
    public int[] getArray() {

      return new int[]{1, 2, 3};
    }

    @Alias("sa")
    public void setArray(int[] i) {

      assertThat(i).containsExactly(1, 2, 3);
    }

    @Alias("gl")
    public List<Integer> getList() {

      return Arrays.asList(1, 2, 3);
    }

    @Alias("sl")
    public void setList(List<Integer> l) {

      assertThat(l).containsExactly(1, 2, 3);
    }

    @Alias("s")
    public void set(int i) {

      assertThat(i).isEqualTo(-17);
    }
  }

  public static class TestInvocation extends CallContextInvocation<Object, Object> {

    @Override
    protected void onCall(@NotNull final List<?> inputs,
        @NotNull final Channel<Object, ?> result) throws Exception {

    }
  }

  @SuppressWarnings("unused")
  private static class Count {

    public int[] count(final int length) {

      final int[] array = new int[length];

      for (int i = 0; i < length; i++) {

        array[i] = i;
      }

      return array;
    }

    public List<Integer> countList(final int length) {

      final ArrayList<Integer> list = new ArrayList<Integer>(length);

      for (int i = 0; i < length; i++) {

        list.add(i);
      }

      return list;
    }
  }

  @SuppressWarnings("unused")
  private static class CountLog implements Log {

    private int mDgbCount;

    private int mErrCount;

    private int mWrnCount;

    public void dbg(@NotNull final List<Object> contexts, @Nullable final String message,
        @Nullable final Throwable throwable) {

      ++mDgbCount;
    }

    public void err(@NotNull final List<Object> contexts, @Nullable final String message,
        @Nullable final Throwable throwable) {

      ++mErrCount;
    }

    public void wrn(@NotNull final List<Object> contexts, @Nullable final String message,
        @Nullable final Throwable throwable) {

      ++mWrnCount;
    }

    public int getDgbCount() {

      return mDgbCount;
    }

    public int getErrCount() {

      return mErrCount;
    }

    public int getWrnCount() {

      return mWrnCount;
    }
  }

  @SuppressWarnings("unused")
  private static class DuplicateAnnotation {

    public static final String GET = "get";

    @Alias(GET)
    public int getOne() {

      return 1;
    }

    @Alias(GET)
    public int getTwo() {

      return 2;
    }
  }

  @SuppressWarnings("unused")
  private static class Inc {

    public int inc(final int i) {

      return i + 1;
    }
  }

  @SuppressWarnings("unused")
  private static class Square {

    public int compute(final int i) {

      return i * i;
    }
  }

  private static class StringContext extends FactoryContextWrapper {

    /**
     * Constructor.
     *
     * @param base the base Context.
     */
    public StringContext(@NotNull final Context base) {

      super(base);
    }

    @Nullable
    public <TYPE> TYPE geInstance(@NotNull final Class<? extends TYPE> type,
        @NotNull final Object... args) {

      return type.cast("test1");
    }
  }

  @SuppressWarnings("unused")
  private static class Sum {

    public int compute(final int a, final int b) {

      return a + b;
    }

    public int compute(final int... ints) {

      int s = 0;

      for (final int i : ints) {

        s += i;
      }

      return s;
    }

    public int compute(final List<Integer> ints) {

      int s = 0;

      for (final int i : ints) {

        s += i;
      }

      return s;
    }
  }

  @SuppressWarnings("unused")
  private static class TestArgs {

    private final int mId;

    public TestArgs(final int id) {

      mId = id;
    }

    public int getId() {

      return mId;
    }
  }

  @SuppressWarnings("unused")
  private static class TestClass {

    public static final String GET = "get";

    public static final String THROW = "throw";

    @Alias(GET)
    public long getLong() {

      return -77;

    }

    @Alias(THROW)
    public void throwException(final RuntimeException ex) {

      throw ex;
    }
  }

  @SuppressWarnings("unused")
  private static class TestClass2 {

    public int getOne() throws InterruptedException {

      UnitDuration.millis(2000).sleepAtLeast();

      return 1;
    }

    public int getTwo() throws InterruptedException {

      UnitDuration.millis(2000).sleepAtLeast();

      return 2;
    }
  }

  @SuppressWarnings("unused")
  private static class TestTimeout {

    @Alias("test")
    @OutputTimeoutAction(TimeoutActionType.CONTINUE)
    public int getInt() throws InterruptedException {

      Thread.sleep(100);
      return 31;
    }
  }
}
