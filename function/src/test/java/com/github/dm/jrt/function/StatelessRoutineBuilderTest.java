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

package com.github.dm.jrt.function;

import com.github.dm.jrt.core.channel.AbortException;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.common.RoutineException;
import com.github.dm.jrt.core.runner.Runners;
import com.github.dm.jrt.function.builder.StatelessRoutineBuilder;
import com.github.dm.jrt.function.util.BiConsumer;
import com.github.dm.jrt.function.util.Consumer;
import com.github.dm.jrt.function.util.Function;
import com.github.dm.jrt.function.util.Supplier;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.dm.jrt.core.util.DurationMeasure.seconds;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stateless routine builder unit tests.
 * <p>
 * Created by davide-maestroni on 03/03/2017.
 */
public class StatelessRoutineBuilderTest {

  @Test
  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public void testError() {
    final AtomicReference<RoutineException> reference = new AtomicReference<RoutineException>();
    final Channel<Void, Void> channel =
        JRoutineFunction.<Void, Void>stateless().onError(new Consumer<RoutineException>() {

          public void accept(final RoutineException e) throws Exception {
            reference.set(e);
          }
        }).invocationConfiguration().withRunner(Runners.immediateRunner()).apply().invoke();
    assertThat(reference.get()).isNull();
    channel.abort(new IOException());
    assertThat(reference.get()).isExactlyInstanceOf(AbortException.class);
    assertThat(reference.get().getCause()).isExactlyInstanceOf(IOException.class);
  }

  @Test
  public void testIncrement() {
    final StatelessRoutineBuilder<Integer, Integer> routine =
        JRoutineFunction.<Integer, Integer>stateless().onNext(
            new BiConsumer<Integer, Channel<Integer, ?>>() {

              public void accept(final Integer integer, final Channel<Integer, ?> result) {
                result.pass(integer + 1);
              }
            });
    assertThat(routine.invoke().pass(1, 2, 3, 4).close().in(seconds(1)).all()).containsExactly(2, 3,
        4, 5);
  }

  @Test
  public void testIncrementArray() {
    final StatelessRoutineBuilder<Integer, Integer> routine =
        JRoutineFunction.<Integer, Integer>stateless().onNextArray(
            new Function<Integer, Integer[]>() {

              public Integer[] apply(final Integer integer) {
                final Integer[] integers = new Integer[1];
                integers[0] = integer + 1;
                return integers;
              }
            });
    assertThat(routine.invoke().pass(1, 2, 3, 4).close().in(seconds(1)).all()).containsExactly(2, 3,
        4, 5);
  }

  @Test
  public void testIncrementIterable() {
    final StatelessRoutineBuilder<Integer, Integer> routine =
        JRoutineFunction.<Integer, Integer>stateless().onNextIterable(
            new Function<Integer, Iterable<? extends Integer>>() {

              public Iterable<? extends Integer> apply(final Integer integer) {
                return Collections.singleton(integer + 1);
              }
            });
    assertThat(routine.invoke().pass(1, 2, 3, 4).close().in(seconds(1)).all()).containsExactly(2, 3,
        4, 5);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testIncrementList() {
    final ArrayList<Integer> list = new ArrayList<Integer>();
    final StatelessRoutineBuilder<Integer, List<Integer>> routine =
        JRoutineFunction.<Integer, List<Integer>>stateless().onNextConsume(new Consumer<Integer>() {

          public void accept(final Integer integer) {
            list.add(integer + 1);
          }
        }).onCompleteOutput(new Supplier<List<Integer>>() {

          public List<Integer> get() {
            return list;
          }
        }).invocationConfiguration().withRunner(Runners.immediateRunner()).apply();
    assertThat(routine.invoke().pass(1, 2, 3, 4).close().in(seconds(1)).all()).containsOnly(
        Arrays.asList(2, 3, 4, 5));
  }

  @Test
  public void testIncrementOutput() {
    final StatelessRoutineBuilder<Integer, Integer> routine =
        JRoutineFunction.<Integer, Integer>stateless().onNextOutput(
            new Function<Integer, Integer>() {

              public Integer apply(final Integer integer) {
                return integer + 1;
              }
            });
    assertThat(routine.invoke().pass(1, 2, 3, 4).close().in(seconds(1)).all()).containsExactly(2, 3,
        4, 5);
  }

  @Test
  public void testProduceArray() {
    final StatelessRoutineBuilder<Integer, Integer> routine =
        JRoutineFunction.<Integer, Integer>stateless().onCompleteArray(new Supplier<Integer[]>() {

          public Integer[] get() {
            final Integer[] integers = new Integer[1];
            integers[0] = 17;
            return integers;
          }
        });
    assertThat(routine.invoke().pass(1, 2, 3, 4).close().in(seconds(1)).all()).containsExactly(17);
  }

  @Test
  public void testProduceIterable() {
    final StatelessRoutineBuilder<Integer, Integer> routine =
        JRoutineFunction.<Integer, Integer>stateless().onCompleteIterable(
            new Supplier<Iterable<? extends Integer>>() {

              public Iterable<? extends Integer> get() {
                return Collections.singleton(17);
              }
            });
    assertThat(routine.invoke().pass(1, 2, 3, 4).close().in(seconds(1)).all()).containsExactly(17);
  }

  @Test
  public void testProduceOutput() {
    final StatelessRoutineBuilder<Integer, Integer> routine =
        JRoutineFunction.<Integer, Integer>stateless().onCompleteOutput(new Supplier<Integer>() {

          public Integer get() {
            return 17;
          }
        });
    assertThat(routine.invoke().pass(1, 2, 3, 4).close().in(seconds(1)).all()).containsExactly(17);
  }
}