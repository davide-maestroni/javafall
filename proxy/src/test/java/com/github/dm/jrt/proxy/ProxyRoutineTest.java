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

package com.github.dm.jrt.proxy;

import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.channel.AbortException;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.common.BackoffBuilder;
import com.github.dm.jrt.core.common.BackoffDecorator;
import com.github.dm.jrt.core.config.ChannelConfiguration.TimeoutActionType;
import com.github.dm.jrt.core.config.InvocationConfiguration;
import com.github.dm.jrt.core.config.InvocationConfiguration.AgingPriority;
import com.github.dm.jrt.core.invocation.InvocationException;
import com.github.dm.jrt.core.log.Log;
import com.github.dm.jrt.core.log.Log.Level;
import com.github.dm.jrt.core.log.NullLog;
import com.github.dm.jrt.core.routine.InvocationMode;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.runner.Execution;
import com.github.dm.jrt.core.runner.Runner;
import com.github.dm.jrt.core.runner.RunnerDecorator;
import com.github.dm.jrt.core.runner.Runners;
import com.github.dm.jrt.core.runner.SyncRunner;
import com.github.dm.jrt.core.util.ClassToken;
import com.github.dm.jrt.core.util.UnitDuration;
import com.github.dm.jrt.object.JRoutineObject;
import com.github.dm.jrt.object.annotation.Alias;
import com.github.dm.jrt.object.annotation.AsyncInput;
import com.github.dm.jrt.object.annotation.AsyncInput.InputMode;
import com.github.dm.jrt.object.annotation.AsyncMethod;
import com.github.dm.jrt.object.annotation.AsyncOutput;
import com.github.dm.jrt.object.annotation.AsyncOutput.OutputMode;
import com.github.dm.jrt.object.annotation.InputBackoff;
import com.github.dm.jrt.object.annotation.Invoke;
import com.github.dm.jrt.object.annotation.LogType;
import com.github.dm.jrt.object.annotation.OutputBackoff;
import com.github.dm.jrt.object.annotation.OutputTimeout;
import com.github.dm.jrt.object.annotation.OutputTimeoutAction;
import com.github.dm.jrt.object.annotation.Priority;
import com.github.dm.jrt.object.annotation.RunnerType;
import com.github.dm.jrt.proxy.annotation.Proxy;
import com.github.dm.jrt.proxy.builder.ProxyObjectBuilder;
import com.github.dm.jrt.proxy.builder.ProxyRoutineBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.core.config.InvocationConfiguration.builder;
import static com.github.dm.jrt.core.util.ClassToken.tokenOf;
import static com.github.dm.jrt.core.util.UnitDuration.seconds;
import static com.github.dm.jrt.object.InvocationTarget.classOfType;
import static com.github.dm.jrt.object.InvocationTarget.instance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Proxy builder unit tests.
 * <p>
 * Created by davide-maestroni on 03/06/2015.
 */
public class ProxyRoutineTest {

  @Test
  public void testAgingPriority() {

    final Pass pass = new Pass();
    final TestRunner runner = new TestRunner();
    final PriorityPass priorityPass = JRoutineProxy.with(instance(pass))
                                                   .applyInvocationConfiguration()
                                                   .withRunner(runner)
                                                   .configured()
                                                   .buildProxy(PriorityPass.class);
    final Channel<?, String> output1 = priorityPass.passNormal("test1").eventuallyContinue();

    for (int i = 0; i < AgingPriority.HIGH_PRIORITY - 1; i++) {

      priorityPass.passHigh("test2");
      runner.run(1);
      assertThat(output1.all()).isEmpty();
    }

    final Channel<?, String> output2 = priorityPass.passHigh("test2");
    runner.run(1);
    assertThat(output1.all()).containsExactly("test1");
    runner.run(Integer.MAX_VALUE);
    assertThat(output2.all()).containsExactly("test2");
  }

  @Test
  public void testAnnotationGenerics() {

    final Size size = new Size();
    final SizeItf proxy = JRoutineObject.with(instance(size)).buildProxy(SizeItf.class);
    assertThat(
        proxy.getSize(Arrays.asList("test1", "test2", "test3")).after(seconds(3)).next()).isEqualTo(
        3);
    assertThat(proxy.getSize()
                    .pass(Arrays.asList("test1", "test2", "test3"))
                    .close()
                    .after(seconds(3))
                    .next()).isEqualTo(3);
    assertThat(
        proxy.getSize(new String[]{"test1", "test2", "test3"}).after(seconds(3)).next()).isEqualTo(
        3);
  }

  @Test
  public void testClassStaticMethod() {

    final TestStatic testStatic = JRoutineProxy.with(classOfType(TestClass.class))
                                               .applyInvocationConfiguration()
                                               .withRunner(Runners.poolRunner())
                                               .withLogLevel(Level.DEBUG)
                                               .withLog(new NullLog())
                                               .configured()
                                               .buildProxy(TestStatic.class);

    try {

      assertThat(testStatic.getOne().all()).containsExactly(1);

      fail();

    } catch (final InvocationException ignored) {

    }

    assertThat(testStatic.getTwo().all()).containsExactly(2);
  }

  @Test
  public void testConstructor() {

    boolean failed = false;
    try {
      new JRoutineProxy();
      failed = true;

    } catch (final Throwable ignored) {

    }

    assertThat(failed).isFalse();
  }

  @Test
  public void testGenericProxyCache() {

    final TestList<String> testList = new TestList<String>();
    final ProxyRoutineBuilder builder = JRoutineProxy.with(instance(testList))
                                                     .applyInvocationConfiguration()
                                                     .withRunner(Runners.syncRunner())
                                                     .configured();

    final TestListItf<String> testListItf1 =
        builder.buildProxy(new ClassToken<TestListItf<String>>() {});
    testListItf1.add("test");

    assertThat(testListItf1.get(0)).isEqualTo("test");
    assertThat(builder.buildProxy(new ClassToken<TestListItf<Integer>>() {})).isSameAs(
        testListItf1);

    final TestListItf<Integer> testListItf2 =
        builder.buildProxy(new ClassToken<TestListItf<Integer>>() {});
    assertThat(testListItf2).isSameAs(testListItf1);
    assertThat(builder.buildProxy(new ClassToken<TestListItf<Integer>>() {})).isSameAs(
        testListItf2);

    testListItf2.add(3);
    assertThat(testListItf2.get(1)).isEqualTo(3);
    assertThat(testListItf2.getAsync(1).next()).isEqualTo(3);
  }

  @Test
  public void testInterface() {

    final TestClass test = new TestClass();
    final ClassToken<TestInterfaceProxy> token = tokenOf(TestInterfaceProxy.class);
    final TestInterfaceProxy testProxy = JRoutineProxy.with(instance(test)).buildProxy(token);

    assertThat(testProxy.getOne().next()).isEqualTo(1);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testNullPointerError() {

    final TestClass test = new TestClass();

    try {

      JRoutineProxy.with(instance(test)).buildProxy((Class<?>) null);

      fail();

    } catch (final NullPointerException ignored) {

    }

    try {

      JRoutineProxy.with(instance(test)).buildProxy((ClassToken<?>) null);

      fail();

    } catch (final NullPointerException ignored) {

    }
  }

  @Test
  public void testObjectStaticMethod() {

    final TestClass test = new TestClass();
    final TestStatic testStatic = JRoutineProxy.with(instance(test))
                                               .applyInvocationConfiguration()
                                               .withRunner(Runners.poolRunner())
                                               .withLogLevel(Level.DEBUG)
                                               .withLog(new NullLog())
                                               .configured()
                                               .buildProxy(TestStatic.class);

    assertThat(testStatic.getOne().all()).containsExactly(1);
    assertThat(testStatic.getTwo().all()).containsExactly(2);
  }

  @Test
  public void testProxy() {

    final NullLog log = new NullLog();
    final Runner runner = Runners.poolRunner();
    final TestClass test = new TestClass();
    final TestProxy testProxy = JRoutineProxy.with(instance(test))
                                             .applyInvocationConfiguration()
                                             .withRunner(runner)
                                             .withLogLevel(Level.DEBUG)
                                             .withLog(log)
                                             .configured()
                                             .buildProxy(tokenOf(TestProxy.class));

    assertThat(testProxy.getOne().next()).isEqualTo(1);
    assertThat(testProxy.getStringParallel1(JRoutineCore.io().of(1, 2, 3))).isIn("1", "2", "3");
    assertThat(testProxy.getStringParallel2(
        JRoutineCore.io().of(new HashSet<Integer>(Arrays.asList(1, 2, 3)))).all()).containsOnly("1",
        "2", "3");

    final ArrayList<String> list = new ArrayList<String>();
    assertThat(
        (Iterable<?>) testProxy.getList(JRoutineCore.io().<List<String>>of(list)).iterator().next())
        .isSameAs(list);

    assertThat(testProxy.getString(JRoutineCore.io().of(3))).isEqualTo("3");
  }

  @Test
  public void testProxyBuilder() {

    final NullLog log = new NullLog();
    final Runner runner = Runners.poolRunner();
    final TestClass test = new TestClass();
    final InvocationConfiguration configuration =
        builder().withRunner(runner).withLogLevel(Level.DEBUG).withLog(log).configured();
    final ProxyObjectBuilder<TestProxy> builder =
        com.github.dm.jrt.proxy.Proxy_Test.with(instance(test));
    final TestProxy testProxy = builder.applyInvocationConfiguration()
                                       .with(configuration)
                                       .configured()
                                       .applyObjectConfiguration()
                                       .withSharedFields()
                                       .configured()
                                       .buildProxy();

    assertThat(testProxy.getOne().next()).isEqualTo(1);
    assertThat(testProxy.getStringParallel1(JRoutineCore.io().of(1, 2, 3))).isIn("1", "2", "3");
    assertThat(testProxy.getStringParallel2(
        JRoutineCore.io().of(new HashSet<Integer>(Arrays.asList(1, 2, 3)))).all()).containsOnly("1",
        "2", "3");

    final ArrayList<String> list = new ArrayList<String>();
    assertThat(
        (Iterable<?>) testProxy.getList(JRoutineCore.io().<List<String>>of(list)).iterator().next())
        .isSameAs(list);

    assertThat(testProxy.getString(JRoutineCore.io().of(3))).isEqualTo("3");

    assertThat(JRoutineProxy.with(instance(test))
                            .applyInvocationConfiguration()
                            .with(configuration)
                            .configured()
                            .applyObjectConfiguration()
                            .withSharedFields()
                            .configured()
                            .buildProxy(tokenOf(TestProxy.class))).isSameAs(testProxy);
  }

  @Test
  public void testProxyCache() {

    final NullLog log = new NullLog();
    final Runner runner = Runners.poolRunner();
    final TestClass test = new TestClass();
    final InvocationConfiguration configuration =
        builder().withRunner(runner).withLogLevel(Level.DEBUG).withLog(log).configured();
    final TestProxy testProxy = JRoutineProxy.with(instance(test))
                                             .applyInvocationConfiguration()
                                             .with(configuration)
                                             .configured()
                                             .buildProxy(tokenOf(TestProxy.class));

    assertThat(JRoutineProxy.with(instance(test))
                            .applyInvocationConfiguration()
                            .with(configuration)
                            .configured()
                            .buildProxy(tokenOf(TestProxy.class))).isSameAs(testProxy);
  }

  @Test
  public void testProxyError() {

    final TestClass test = new TestClass();

    try {

      JRoutineProxy.with(instance(test)).buildProxy(TestClass.class);

      fail();

    } catch (final IllegalArgumentException ignored) {

    }

    try {

      JRoutineProxy.with(instance(test)).buildProxy(tokenOf(TestClass.class));

      fail();

    } catch (final IllegalArgumentException ignored) {

    }
  }

  @Test
  public void testSharedFields() {

    final TestClass2 test = new TestClass2();
    final ProxyRoutineBuilder builder = JRoutineProxy.with(instance(test))
                                                     .applyInvocationConfiguration()
                                                     .withOutputTimeout(seconds(2))
                                                     .configured();

    long startTime = System.currentTimeMillis();

    Channel<?, Integer> getOne = builder.applyObjectConfiguration()
                                        .withSharedFields("1")
                                        .configured()
                                        .buildProxy(TestClassAsync.class)
                                        .getOne();
    Channel<?, Integer> getTwo = builder.applyObjectConfiguration()
                                        .withSharedFields("2")
                                        .configured()
                                        .buildProxy(TestClassAsync.class)
                                        .getTwo();

    assertThat(getOne.getComplete()).isTrue();
    assertThat(getTwo.getComplete()).isTrue();
    assertThat(System.currentTimeMillis() - startTime).isLessThan(1000);

    startTime = System.currentTimeMillis();

    getOne = builder.buildProxy(TestClassAsync.class).getOne();
    getTwo = builder.buildProxy(TestClassAsync.class).getTwo();

    assertThat(getOne.getComplete()).isTrue();
    assertThat(getTwo.getComplete()).isTrue();
    assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(1000);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTemplates() {

    final Impl impl = new Impl();
    final Itf itf = JRoutineProxy.with(instance(impl))
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

  @Test
  public void testTimeoutActionAnnotation() throws NoSuchMethodException {

    final TestTimeout testTimeout = new TestTimeout();
    assertThat(JRoutineProxy.with(instance(testTimeout))
                            .applyInvocationConfiguration()
                            .withOutputTimeout(seconds(1))
                            .configured()
                            .buildProxy(TestTimeoutItf.class)
                            .getInt()).isEqualTo(31);

    try {

      JRoutineProxy.with(instance(testTimeout))
                   .applyInvocationConfiguration()
                   .withOutputTimeoutAction(TimeoutActionType.FAIL)
                   .configured()
                   .buildProxy(TestTimeoutItf.class)
                   .getInt();

      fail();

    } catch (final AbortException ignored) {

    }
  }

  @Proxy(Impl.class)
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

  @Proxy(Pass.class)
  public interface PriorityPass {

    @AsyncOutput
    @Alias("pass")
    @Priority(AgingPriority.HIGH_PRIORITY)
    Channel<?, String> passHigh(String s);

    @AsyncOutput
    @Alias("pass")
    @Priority(AgingPriority.NORMAL_PRIORITY)
    Channel<?, String> passNormal(String s);
  }

  @Proxy(Size.class)
  public interface SizeItf {

    @AsyncMethod(List.class)
    Channel<List<String>, Integer> getSize();

    @AsyncOutput
    Channel<?, Integer> getSize(List<String> l);

    @LogType(NullLog.class)
    @InputBackoff(MyBackoff.class)
    @OutputBackoff(MyBackoff.class)
    @RunnerType(MyRunner.class)
    @AsyncOutput
    Channel<?, Integer> getSize(String[] a);
  }

  @Proxy(TestClass2.class)
  public interface TestClassAsync {

    @AsyncOutput
    Channel<?, Integer> getOne();

    @AsyncOutput
    Channel<?, Integer> getTwo();
  }

  @SuppressWarnings("unused")
  public interface TestClassInterface {

    int getOne();
  }

  @Proxy(TestClassInterface.class)
  public interface TestInterfaceProxy {

    @OutputTimeout(300)
    @AsyncOutput
    Channel<?, Integer> getOne();
  }

  @Proxy(TestList.class)
  public interface TestListItf<TYPE> {

    void add(Object t);

    TYPE get(int i);

    @Alias("get")
    @AsyncOutput
    Channel<?, TYPE> getAsync(int i);
  }

  @Proxy(value = TestClass.class, className = "Test",
      classPackage = "com.github.dm.jrt.proxy")
  public interface TestProxy {

    @OutputTimeout(300)
    @Invoke(InvocationMode.PARALLEL)
    @AsyncOutput
    Iterable<Iterable> getList(@AsyncInput(List.class) Channel<?, List<String>> i);

    @OutputTimeout(300)
    @AsyncOutput
    Channel<?, Integer> getOne();

    @OutputTimeout(300)
    String getString(@AsyncInput(int.class) Channel<?, Integer> i);

    @Alias("getString")
    @OutputTimeout(300)
    @Invoke(InvocationMode.PARALLEL)
    String getStringParallel1(@AsyncInput(int.class) Channel<?, Integer> i);

    @Alias("getString")
    @OutputTimeout(300)
    @Invoke(InvocationMode.PARALLEL)
    @AsyncOutput
    Channel<?, String> getStringParallel2(@AsyncInput(int.class) Channel<?, Integer> i);
  }

  @Proxy(TestClass.class)
  public interface TestStatic {

    @OutputTimeout(300)
    @AsyncOutput
    Channel<?, Integer> getOne();

    @OutputTimeout(300)
    @AsyncOutput
    Channel<?, Integer> getTwo();
  }

  @Proxy(TestTimeout.class)
  public interface TestTimeoutItf {

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

  public static class MyBackoff extends BackoffDecorator {

    public MyBackoff() {
      super(BackoffBuilder.afterCount(3).linearDelay(1, TimeUnit.SECONDS));
    }
  }

  public static class MyRunner extends RunnerDecorator {

    public MyRunner() {
      super(Runners.syncRunner());
    }
  }

  @SuppressWarnings("unused")
  public static class Pass {

    public String pass(final String s) {

      return s;
    }
  }

  @SuppressWarnings("unused")
  public static class Size {

    public int getSize(final List<String> l) {

      return l.size();
    }

    public int getSize(final String s) {

      return s.length();
    }

    public int getSize(final String[] a) {

      return a.length;
    }
  }

  @SuppressWarnings("unused")
  public static class TestClass implements TestClassInterface {

    public static int getTwo() {

      return 2;
    }

    public List<String> getList(final List<String> list) {

      return list;
    }

    public int getOne() {

      return 1;
    }

    public String getString(final int i) {

      return Integer.toString(i);
    }
  }

  @SuppressWarnings("unused")
  public static class TestClass2 {

    public int getOne() throws InterruptedException {

      UnitDuration.millis(500).sleepAtLeast();

      return 1;
    }

    public int getTwo() throws InterruptedException {

      UnitDuration.millis(500).sleepAtLeast();

      return 2;
    }
  }

  @SuppressWarnings("unused")
  public static class TestList<TYPE> {

    private final ArrayList<TYPE> mList = new ArrayList<TYPE>();

    public void add(TYPE t) {

      mList.add(t);
    }

    public TYPE get(int i) {

      return mList.get(i);
    }
  }

  @SuppressWarnings("unused")
  public static class TestTimeout {

    public int getInt() throws InterruptedException {

      Thread.sleep(100);
      return 31;
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

  private static class TestRunner extends SyncRunner {

    private final ArrayList<Execution> mExecutions = new ArrayList<Execution>();

    @Override
    public boolean isExecutionThread() {
      return false;
    }

    @Override
    public void run(@NotNull final Execution execution, final long delay,
        @NotNull final TimeUnit timeUnit) {
      mExecutions.add(execution);
    }

    private void run(int count) {
      final ArrayList<Execution> executions = mExecutions;
      while (!executions.isEmpty() && (count-- > 0)) {
        final Execution execution = executions.remove(0);
        execution.run();
      }
    }
  }
}
