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
package com.bmd.jrt.routine;

import com.bmd.jrt.annotation.Async;
import com.bmd.jrt.annotation.AsyncParameters;
import com.bmd.jrt.annotation.AsyncResult;
import com.bmd.jrt.channel.BasicOutputConsumer;
import com.bmd.jrt.channel.OutputChannel;
import com.bmd.jrt.channel.OutputConsumer;
import com.bmd.jrt.channel.ParameterChannel;
import com.bmd.jrt.channel.ResultChannel;
import com.bmd.jrt.common.ClassToken;
import com.bmd.jrt.common.RoutineException;
import com.bmd.jrt.execution.BasicExecution;
import com.bmd.jrt.execution.Execution;
import com.bmd.jrt.execution.ExecutionBody;
import com.bmd.jrt.log.Log.LogLevel;
import com.bmd.jrt.log.Logger;
import com.bmd.jrt.log.NullLog;
import com.bmd.jrt.routine.DefaultInvocation.InputIterator;
import com.bmd.jrt.routine.DefaultParameterChannel.ExecutionProvider;
import com.bmd.jrt.routine.DefaultResultChannel.AbortHandler;
import com.bmd.jrt.runner.Runners;
import com.bmd.jrt.time.TimeDuration;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.bmd.jrt.common.ClassToken.tokenOf;
import static com.bmd.jrt.routine.JavaRoutine.on;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Routine unit tests.
 * <p/>
 * Created by davide on 9/9/14.
 */
public class RoutineTest extends TestCase {

    public void testAbort() {

        final Routine<String, String> routine =
                on(tokenOf(DelayedExecution.class)).withArgs(TimeDuration.millis(100))
                                                   .buildRoutine();

        final ParameterChannel<String, String> inputChannel = routine.invokeAsync().pass("test1");
        final OutputChannel<String> outputChannel = inputChannel.results();

        assertThat(inputChannel.isOpen()).isFalse();
        assertThat(inputChannel.abort(new IllegalArgumentException("test1"))).isFalse();
        assertThat(inputChannel.isOpen()).isFalse();
        assertThat(outputChannel.readFirst()).isEqualTo("test1");

        final ParameterChannel<String, String> inputChannel1 = routine.invokeAsync().pass("test1");
        final OutputChannel<String> outputChannel1 = inputChannel1.results();

        assertThat(inputChannel1.isOpen()).isFalse();
        assertThat(inputChannel1.abort()).isFalse();
        assertThat(inputChannel1.isOpen()).isFalse();
        assertThat(outputChannel1.isOpen()).isTrue();
        assertThat(outputChannel1.readFirst()).isEqualTo("test1");
        assertThat(outputChannel1.isOpen()).isFalse();

        final OutputChannel<String> channel = routine.runAsync("test2");
        assertThat(channel.isOpen()).isTrue();
        assertThat(channel.abort(new IllegalArgumentException("test2"))).isTrue();
        assertThat(channel.abort()).isFalse();
        assertThat(channel.isOpen()).isTrue();

        try {

            channel.readFirst();

            fail();

        } catch (final RoutineException ex) {

            assertThat(ex.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
            assertThat(ex.getCause().getMessage()).isEqualTo("test2");
        }

        assertThat(channel.isOpen()).isFalse();


        final OutputChannel<String> channel1 = routine.runAsync("test2");
        assertThat(channel1.isOpen()).isTrue();
        assertThat(channel1.abort()).isTrue();
        assertThat(channel1.abort(new IllegalArgumentException("test2"))).isFalse();
        assertThat(channel1.isOpen()).isTrue();

        try {

            channel1.readFirst();

            fail();

        } catch (final RoutineException ex) {

            assertThat(ex.getCause()).isNull();
        }

        assertThat(channel1.isOpen()).isFalse();


        final Execution<String, String> abortExecution = new BasicExecution<String, String>() {

            @Override
            public void onInput(final String s, @Nonnull final ResultChannel<String> results) {

                assertThat(results.isOpen()).isTrue();
                assertThat(results.abort(new IllegalArgumentException(s))).isTrue();
                assertThat(results.abort()).isFalse();
                assertThat(results.isOpen()).isFalse();
            }
        };

        final Routine<String, String> routine1 =
                on(ClassToken.tokenOf(abortExecution)).withArgs(this).buildRoutine();

        try {

            routine1.invokeAsync()
                    .after(TimeDuration.millis(10))
                    .pass("test_abort")
                    .results()
                    .readFirst();

            fail();

        } catch (final RoutineException ex) {

            assertThat(ex.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
            assertThat(ex.getCause().getMessage()).isEqualTo("test_abort");
        }

        final Execution<String, String> abortExecution2 = new BasicExecution<String, String>() {

            @Override
            public void onInput(final String s, @Nonnull final ResultChannel<String> results) {

                assertThat(results.abort()).isTrue();
                assertThat(results.abort(new IllegalArgumentException(s))).isFalse();
            }
        };

        final Routine<String, String> routine2 =
                on(ClassToken.tokenOf(abortExecution2)).withArgs(this).buildRoutine();

        try {

            routine2.invokeAsync()
                    .after(TimeDuration.millis(10))
                    .pass("test_abort")
                    .results()
                    .readFirst();

            fail();

        } catch (final RoutineException ex) {

            assertThat(ex.getCause()).isNull();
        }
    }

    public void testAbortInput() throws InterruptedException {

        final Semaphore semaphore = new Semaphore(0);
        final AtomicReference<Throwable> abortReason = new AtomicReference<Throwable>();

        final BasicExecution<String, String> abortExecution = new BasicExecution<String, String>() {

            @Override
            public void onAbort(@Nullable final Throwable reason) {

                abortReason.set(reason);
                semaphore.release();
            }
        };

        final Routine<String, String> routine = JavaRoutine.on(tokenOf(abortExecution))
                                                           .withArgs(this, abortReason, semaphore)
                                                           .buildRoutine();

        final ParameterChannel<String, String> channel = routine.invokeAsync();
        final IllegalArgumentException exception = new IllegalArgumentException();
        channel.after(TimeDuration.millis(100)).abort(exception);

        semaphore.tryAcquire(1, TimeUnit.SECONDS);

        assertThat(abortReason.get()).isEqualTo(exception);

        final ParameterChannel<String, String> channel1 = routine.invokeAsync();
        final IllegalAccessError exception1 = new IllegalAccessError();
        channel1.now().abort(exception1);

        semaphore.tryAcquire(1, TimeUnit.SECONDS);

        assertThat(abortReason.get()).isEqualTo(exception1);
    }

    public void testCalls() {

        final Routine<String, String> routine =
                on(tokenOf(PassThroughExecution.class)).buildRoutine();

        assertThat(routine.call()).isEmpty();
        assertThat(routine.call(Arrays.asList("test1", "test2"))).containsExactly("test1", "test2");
        assertThat(routine.call(routine.run("test1", "test2"))).containsExactly("test1", "test2");
        assertThat(routine.call("test1")).containsExactly("test1");
        assertThat(routine.call("test1", "test2")).containsExactly("test1", "test2");
        assertThat(routine.callAsync()).isEmpty();
        assertThat(routine.callAsync(Arrays.asList("test1", "test2"))).containsExactly("test1",
                                                                                       "test2");
        assertThat(routine.callAsync(routine.run("test1", "test2"))).containsExactly("test1",
                                                                                     "test2");
        assertThat(routine.callAsync("test1")).containsExactly("test1");
        assertThat(routine.callAsync("test1", "test2")).containsExactly("test1", "test2");
        assertThat(routine.callParallel()).isEmpty();
        assertThat(routine.callParallel(Arrays.asList("test1", "test2"))).containsOnly("test1",
                                                                                       "test2");
        assertThat(routine.callParallel(routine.run("test1", "test2"))).containsOnly("test1",
                                                                                     "test2");
        assertThat(routine.callParallel("test1")).containsOnly("test1");
        assertThat(routine.callParallel("test1", "test2")).containsOnly("test1", "test2");

        assertThat(routine.run().readAll()).isEmpty();
        assertThat(routine.run(Arrays.asList("test1", "test2")).readAll()).containsExactly("test1",
                                                                                           "test2");
        assertThat(routine.run(routine.run("test1", "test2")).readAll()).containsExactly("test1",
                                                                                         "test2");
        assertThat(routine.run("test1").readAll()).containsExactly("test1");
        assertThat(routine.run("test1", "test2").readAll()).containsExactly("test1", "test2");
        assertThat(routine.runAsync().readAll()).isEmpty();
        assertThat(routine.runAsync(Arrays.asList("test1", "test2")).readAll()).containsExactly(
                "test1", "test2");
        assertThat(routine.runAsync(routine.run("test1", "test2")).readAll()).containsExactly(
                "test1", "test2");
        assertThat(routine.runAsync("test1").readAll()).containsExactly("test1");
        assertThat(routine.runAsync("test1", "test2").readAll()).containsExactly("test1", "test2");
        assertThat(routine.runParallel().readAll()).isEmpty();
        assertThat(routine.runParallel(Arrays.asList("test1", "test2")).readAll()).containsOnly(
                "test1", "test2");
        assertThat(routine.runParallel(routine.run("test1", "test2")).readAll()).containsOnly(
                "test1", "test2");
        assertThat(routine.runParallel("test1").readAll()).containsOnly("test1");
        assertThat(routine.runParallel("test1", "test2").readAll()).containsOnly("test1", "test2");

        assertThat(routine.invoke().pass().results().readAll()).isEmpty();
        assertThat(routine.invoke()
                          .pass(Arrays.asList("test1", "test2"))
                          .results()
                          .readAll()).containsExactly("test1", "test2");
        assertThat(routine.invoke()
                          .pass(routine.run("test1", "test2"))
                          .results()
                          .readAll()).containsExactly("test1", "test2");
        assertThat(routine.invoke().pass("test1").results().readAll()).containsExactly("test1");
        assertThat(routine.invoke().pass("test1", "test2").results().readAll()).containsExactly(
                "test1", "test2");
        assertThat(routine.invokeAsync().pass().results().readAll()).isEmpty();
        assertThat(routine.invokeAsync()
                          .pass(Arrays.asList("test1", "test2"))
                          .results()
                          .readAll()).containsExactly("test1", "test2");
        assertThat(routine.invokeAsync()
                          .pass(routine.run("test1", "test2"))
                          .results()
                          .readAll()).containsExactly("test1", "test2");
        assertThat(routine.invokeAsync().pass("test1").results().readAll()).containsExactly(
                "test1");
        assertThat(
                routine.invokeAsync().pass("test1", "test2").results().readAll()).containsExactly(
                "test1", "test2");
        assertThat(routine.invokeParallel().pass().results().readAll()).isEmpty();
        assertThat(routine.invokeParallel()
                          .pass(Arrays.asList("test1", "test2"))
                          .results()
                          .readAll()).containsOnly("test1", "test2");
        assertThat(routine.invokeParallel().pass(routine.run("test1", "test2")).results().readAll())
                .containsOnly("test1", "test2");
        assertThat(routine.invokeParallel().pass("test1").results().readAll()).containsOnly(
                "test1");
        assertThat(
                routine.invokeParallel().pass("test1", "test2").results().readAll()).containsOnly(
                "test1", "test2");
    }

    public void testChainedRoutine() {

        final ExecutionBody<Integer, Integer> execSum = new ExecutionBody<Integer, Integer>() {

            @Override
            public void onExec(@Nonnull final List<? extends Integer> integers,
                    @Nonnull final ResultChannel<Integer> results) {

                int sum = 0;

                for (final Integer integer : integers) {

                    sum += integer;
                }

                results.pass(sum);
            }
        };

        final Routine<Integer, Integer> sumRoutine =
                on(ClassToken.tokenOf(execSum)).withArgs(this).buildRoutine();

        final BasicExecution<Integer, Integer> invokeSquare =
                new BasicExecution<Integer, Integer>() {

                    @Override
                    public void onInput(final Integer integer,
                            @Nonnull final ResultChannel<Integer> results) {

                        final int input = integer;

                        results.pass(input * input);
                    }
                };

        final Routine<Integer, Integer> squareRoutine =
                on(ClassToken.tokenOf(invokeSquare)).withArgs(this).buildRoutine();

        assertThat(sumRoutine.call(squareRoutine.run(1, 2, 3, 4))).containsExactly(30);
        assertThat(sumRoutine.callAsync(squareRoutine.run(1, 2, 3, 4))).containsExactly(30);
        assertThat(sumRoutine.run(squareRoutine.run(1, 2, 3, 4)).readAll()).containsExactly(30);
        assertThat(sumRoutine.runAsync(squareRoutine.run(1, 2, 3, 4)).readAll()).containsExactly(
                30);

        assertThat(sumRoutine.call(squareRoutine.runAsync(1, 2, 3, 4))).containsExactly(30);
        assertThat(sumRoutine.callAsync(squareRoutine.runAsync(1, 2, 3, 4))).containsExactly(30);
        assertThat(sumRoutine.run(squareRoutine.runAsync(1, 2, 3, 4)).readAll()).containsExactly(
                30);//TODO fail
        assertThat(
                sumRoutine.runAsync(squareRoutine.runAsync(1, 2, 3, 4)).readAll()).containsExactly(
                30);

        assertThat(sumRoutine.call(squareRoutine.runParallel(1, 2, 3, 4))).containsExactly(30);
        assertThat(sumRoutine.callAsync(squareRoutine.runParallel(1, 2, 3, 4))).containsExactly(30);
        assertThat(sumRoutine.run(squareRoutine.runParallel(1, 2, 3, 4)).readAll()).containsExactly(
                30);
        assertThat(sumRoutine.runAsync(squareRoutine.runParallel(1, 2, 3, 4))
                             .readAll()).containsExactly(30);
    }

    public void testComposedRoutine() {

        final ExecutionBody<Integer, Integer> execSum = new ExecutionBody<Integer, Integer>() {

            @Override
            public void onExec(@Nonnull final List<? extends Integer> integers,
                    @Nonnull final ResultChannel<Integer> results) {

                int sum = 0;

                for (final Integer integer : integers) {

                    sum += integer;
                }

                results.pass(sum);
            }
        };

        final Routine<Integer, Integer> sumRoutine =
                on(ClassToken.tokenOf(execSum)).withArgs(this).buildRoutine();

        final BasicExecution<Integer, Integer> invokeSquare =
                new BasicExecution<Integer, Integer>() {

                    @Override
                    public void onInput(final Integer integer,
                            @Nonnull final ResultChannel<Integer> results) {

                        final int input = integer;

                        results.pass(input * input);
                    }
                };

        final Routine<Integer, Integer> squareRoutine =
                on(ClassToken.tokenOf(invokeSquare)).withArgs(this).buildRoutine();

        final BasicExecution<Integer, Integer> invokeSquareSum =
                new BasicExecution<Integer, Integer>() {

                    private ParameterChannel<Integer, Integer> mChannel;

                    @Override
                    public void onAbort(final Throwable reason) {

                        mChannel.abort(reason);
                    }

                    @Override
                    public void onInit() {

                        mChannel = sumRoutine.invokeAsync();
                    }

                    @Override
                    public void onInput(final Integer integer,
                            @Nonnull final ResultChannel<Integer> results) {

                        mChannel.pass(squareRoutine.runAsync(integer));
                    }

                    @Override
                    public void onResult(@Nonnull final ResultChannel<Integer> results) {

                        results.pass(mChannel.results());
                    }
                };

        final Routine<Integer, Integer> squareSumRoutine =
                on(ClassToken.tokenOf(invokeSquareSum)).withArgs(this, sumRoutine, squareRoutine)
                                                       .buildRoutine();

        assertThat(squareSumRoutine.call(1, 2, 3, 4)).containsExactly(30);
        assertThat(squareSumRoutine.callAsync(1, 2, 3, 4)).containsExactly(30);
        assertThat(squareSumRoutine.run(1, 2, 3, 4).readAll()).containsExactly(30);
        assertThat(squareSumRoutine.runAsync(1, 2, 3, 4).readAll()).containsExactly(30);
    }

    public void testDelay() {

        final Routine<String, String> routine = JavaRoutine.on(tokenOf(DelayedExecution.class))
                                                           .withArgs(TimeDuration.millis(10))
                                                           .buildRoutine();

        long startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel = routine.invokeAsync();
        channel.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel.after(TimeDuration.millis(10)).pass((String[]) null);
        channel.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsOnly("test1",
                                                                                           "test2",
                                                                                           "test3",
                                                                                           "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(110);

        final Routine<String, String> routine1 = JavaRoutine.on(tokenOf(DelayedExecution.class))
                                                            .orderedInput()
                                                            .orderedOutput()
                                                            .withArgs(TimeDuration.millis(10))
                                                            .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel1 = routine1.invokeAsync();
        channel1.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel1.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel1.after(TimeDuration.millis(10).microsTime()).pass(Arrays.asList("test3", "test4"));
        channel1.after(TimeDuration.millis(10)).pass((String[]) null);
        channel1.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(
                channel1.results().afterMax(TimeDuration.seconds(7000)).readAll()).containsExactly(
                "test1", "test2", "test3", "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(110);

        final Routine<String, String> routine2 = JavaRoutine.on(tokenOf(DelayedListExecution.class))
                                                            .withArgs(TimeDuration.millis(10), 2)
                                                            .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel2 = routine2.invokeAsync();
        channel2.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel2.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel2.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel2.after(TimeDuration.millis(10)).pass((String[]) null);
        channel2.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel2.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsOnly("test1",
                                                                                            "test2",
                                                                                            "test3",
                                                                                            "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(110);

        final Routine<String, String> routine3 = JavaRoutine.on(tokenOf(DelayedListExecution.class))
                                                            .orderedInput()
                                                            .orderedOutput()
                                                            .withArgs(TimeDuration.millis(10), 2)
                                                            .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel3 = routine3.invokeAsync();
        channel3.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel3.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel3.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel3.after(TimeDuration.millis(10)).pass((String[]) null);
        channel3.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel3.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsExactly(
                "test1", "test2", "test3", "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(110);

        final Routine<String, String> routine4 = JavaRoutine.on(tokenOf(DelayedListExecution.class))
                                                            .withArgs(TimeDuration.ZERO, 2)
                                                            .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel4 = routine4.invokeAsync();
        channel4.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel4.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel4.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel4.after(TimeDuration.millis(10)).pass((String[]) null);
        channel4.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel4.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsOnly("test1",
                                                                                            "test2",
                                                                                            "test3",
                                                                                            "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(100);

        final Routine<String, String> routine5 = JavaRoutine.on(tokenOf(DelayedListExecution.class))
                                                            .orderedInput()
                                                            .orderedOutput()
                                                            .withArgs(TimeDuration.ZERO, 2)
                                                            .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel5 = routine5.invokeAsync();
        channel5.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel5.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel5.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel5.after(TimeDuration.millis(10)).pass((String[]) null);
        channel5.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel5.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsExactly(
                "test1", "test2", "test3", "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(100);

        final Routine<String, String> routine6 =
                JavaRoutine.on(tokenOf(DelayedChannelExecution.class))
                           .withArgs(TimeDuration.millis(10))
                           .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel6 = routine6.invokeAsync();
        channel6.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel6.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel6.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel6.after(TimeDuration.millis(10)).pass((String[]) null);
        channel6.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel6.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsOnly("test1",
                                                                                            "test2",
                                                                                            "test3",
                                                                                            "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(110);

        final Routine<String, String> routine7 =
                JavaRoutine.on(tokenOf(DelayedChannelExecution.class))
                           .orderedInput()
                           .orderedOutput()
                           .withArgs(TimeDuration.millis(10))
                           .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel7 = routine7.invokeAsync();
        channel7.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel7.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel7.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel7.after(TimeDuration.millis(10)).pass((String[]) null);
        channel7.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel7.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsExactly(
                "test1", "test2", "test3", "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(110);

        final Routine<String, String> routine8 =
                JavaRoutine.on(tokenOf(DelayedChannelExecution.class))
                           .withArgs(TimeDuration.ZERO)
                           .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel8 = routine8.invokeAsync();
        channel8.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel8.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel8.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel8.after(TimeDuration.millis(10)).pass((String[]) null);
        channel8.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel8.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsOnly("test1",
                                                                                            "test2",
                                                                                            "test3",
                                                                                            "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(100);

        final Routine<String, String> routine9 =
                JavaRoutine.on(tokenOf(DelayedChannelExecution.class))
                           .orderedInput()
                           .orderedOutput()
                           .withArgs(TimeDuration.ZERO)
                           .buildRoutine();

        startTime = System.currentTimeMillis();

        final ParameterChannel<String, String> channel9 = routine9.invokeAsync();
        channel9.after(100, TimeUnit.MILLISECONDS).pass("test1");
        channel9.after(TimeDuration.millis(10).nanosTime()).pass("test2");
        channel9.after(TimeDuration.millis(10).microsTime()).pass("test3", "test4");
        channel9.after(TimeDuration.millis(10)).pass((String[]) null);
        channel9.now().pass((List<String>) null).pass((OutputChannel<String>) null);
        assertThat(channel9.results().afterMax(3, TimeUnit.SECONDS).readAll()).containsExactly(
                "test1", "test2", "test3", "test4");
        assertThat(System.currentTimeMillis() - startTime).isGreaterThanOrEqualTo(100);
    }

    @SuppressWarnings("ConstantConditions")
    public void testError() {

        try {

            new ParallelExecution<Object, Object>(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            on(tokenOf(ConstructorException.class)).logLevel(LogLevel.SILENT).buildRoutine().call();

            fail();

        } catch (final RoutineException ignored) {

        }

        try {

            new AbstractRoutine<Object, Object>(null, Runners.shared(), 1, 1, TimeDuration.ZERO,
                                                false, false, Logger.getDefaultLog(),
                                                Logger.getDefaultLogLevel()) {

                @Override
                @Nonnull
                protected Execution<Object, Object> createExecution(final boolean async) {

                    return new ConstructorException();
                }
            };

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new AbstractRoutine<Object, Object>(Runners.queued(), null, 1, 1, TimeDuration.ZERO,
                                                false, false, Logger.getDefaultLog(),
                                                Logger.getDefaultLogLevel()) {

                @Override
                @Nonnull
                protected Execution<Object, Object> createExecution(final boolean async) {

                    return new ConstructorException();
                }
            };

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new AbstractRoutine<Object, Object>(Runners.queued(), Runners.shared(), 1, 1, null,
                                                false, false, Logger.getDefaultLog(),
                                                Logger.getDefaultLogLevel()) {

                @Override
                @Nonnull
                protected Execution<Object, Object> createExecution(final boolean async) {

                    return new ConstructorException();
                }
            };

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new AbstractRoutine<Object, Object>(Runners.queued(), Runners.shared(), 1, 0,
                                                TimeDuration.ZERO, false, false, null, null) {

                @Override
                @Nonnull
                protected Execution<Object, Object> createExecution(final boolean async) {

                    return new ConstructorException();
                }
            };

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new AbstractRoutine<Object, Object>(Runners.queued(), Runners.shared(), 0, 1,
                                                TimeDuration.ZERO, false, false,
                                                Logger.getDefaultLog(),
                                                Logger.getDefaultLogLevel()) {

                @Override
                @Nonnull
                protected Execution<Object, Object> createExecution(final boolean async) {

                    return new ConstructorException();
                }
            };

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        try {

            new AbstractRoutine<Object, Object>(Runners.queued(), Runners.shared(), 1, -1,
                                                TimeDuration.ZERO, false, false,
                                                Logger.getDefaultLog(),
                                                Logger.getDefaultLogLevel()) {

                @Override
                @Nonnull
                protected Execution<Object, Object> createExecution(final boolean async) {

                    return new ConstructorException();
                }
            };

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        final Logger logger =
                Logger.create(Logger.getDefaultLog(), Logger.getDefaultLogLevel(), this);

        try {

            new DefaultInvocation<Object, Object>(null, new TestInputIterator(),
                                                  new DefaultResultChannel<Object>(
                                                          new TestAbortHandler(),
                                                          Runners.sequential(), false, logger),
                                                  logger);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultInvocation<Object, Object>(new TestExecutionProvider(), null,
                                                  new DefaultResultChannel<Object>(
                                                          new TestAbortHandler(),
                                                          Runners.sequential(), false, logger),
                                                  logger);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultInvocation<Object, Object>(new TestExecutionProvider(),
                                                  new TestInputIterator(), null, logger);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultInvocation<Object, Object>(new TestExecutionProvider(),
                                                  new TestInputIterator(),
                                                  new DefaultResultChannel<Object>(
                                                          new TestAbortHandler(),
                                                          Runners.sequential(), false, logger),
                                                  null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    public void testErrorConsumerOnResult() {

        final BasicOutputConsumer<String> exceptionConsumer = new BasicOutputConsumer<String>() {

            @Override
            public void onOutput(final String output) {

                throw new NullPointerException(output);
            }
        };

        testConsumer(exceptionConsumer);
    }

    public void testErrorConsumerOnReturn() {

        final BasicOutputConsumer<String> exceptionConsumer = new BasicOutputConsumer<String>() {

            @Override
            public void onClose() {

                throw new NullPointerException("test2");
            }
        };

        testConsumer(exceptionConsumer);
    }

    public void testErrorOnInit() {

        final BasicExecution<String, String> exceptionOnInit =
                new BasicExecution<String, String>() {

                    @Override
                    public void onInit() {

                        throw new NullPointerException("test1");
                    }
                };

        final Routine<String, String> exceptionRoutine =
                on(ClassToken.tokenOf(exceptionOnInit)).withArgs(this).buildRoutine();

        testException(exceptionRoutine, "test", "test1");

        final Routine<String, String> passThroughRoutine =
                on(tokenOf(PassThroughExecution.class)).buildRoutine();

        testChained(passThroughRoutine, exceptionRoutine, "test", "test1");
        testChained(exceptionRoutine, passThroughRoutine, "test", "test1");
    }

    public void testErrorOnInput() {

        final BasicExecution<String, String> exceptionOnInput =
                new BasicExecution<String, String>() {

                    @Override
                    public void onInput(final String s,
                            @Nonnull final ResultChannel<String> results) {

                        throw new NullPointerException(s);
                    }
                };

        final Routine<String, String> exceptionRoutine =
                on(ClassToken.tokenOf(exceptionOnInput)).withArgs(this).buildRoutine();

        testException(exceptionRoutine, "test2", "test2");

        final Routine<String, String> passThroughRoutine =
                on(tokenOf(PassThroughExecution.class)).buildRoutine();

        testChained(passThroughRoutine, exceptionRoutine, "test2", "test2");
        testChained(exceptionRoutine, passThroughRoutine, "test2", "test2");
    }

    public void testErrorOnResult() {

        final BasicExecution<String, String> exceptionOnResult =
                new BasicExecution<String, String>() {

                    @Override
                    public void onResult(@Nonnull final ResultChannel<String> results) {

                        throw new NullPointerException("test3");
                    }
                };

        final Routine<String, String> exceptionRoutine =
                on(ClassToken.tokenOf(exceptionOnResult)).withArgs(this).buildRoutine();

        testException(exceptionRoutine, "test", "test3");

        final Routine<String, String> passThroughRoutine =
                on(tokenOf(PassThroughExecution.class)).buildRoutine();

        testChained(passThroughRoutine, exceptionRoutine, "test", "test3");
        testChained(exceptionRoutine, passThroughRoutine, "test", "test3");
    }

    public void testErrorOnReturn() {

        final Execution<String, String> exceptionOnReturn = new BasicExecution<String, String>() {

            @Override
            public void onInput(final String s, @Nonnull final ResultChannel<String> results) {

                results.pass(s);
            }

            @Override
            public void onReturn() {

                throw new NullPointerException("test4");
            }
        };

        final Routine<String, String> exceptionRoutine =
                on(ClassToken.tokenOf(exceptionOnReturn)).withArgs(this).buildRoutine();

        testException(exceptionRoutine, "test", "test4");

        final Routine<String, String> passThroughRoutine =
                on(tokenOf(PassThroughExecution.class)).buildRoutine();

        testChained(passThroughRoutine, exceptionRoutine, "test", "test4");
        testChained(exceptionRoutine, passThroughRoutine, "test", "test4");
    }

    public void testMethod() throws NoSuchMethodException {

        assertThat(on(new TestClass()).classMethod(TestClass.class.getMethod("getOne"))
                                      .call()).containsExactly(1);
        assertThat(on(new TestClass()).classMethod("getOne").call()).containsExactly(1);
        assertThat(on(new TestClass()).method(TestClass.GET_METHOD).call()).containsExactly(1);
        assertThat(on(TestClass.class).method(TestClass.GET_METHOD).call(3)).containsExactly(3);
        assertThat(on(TestClass.class).method("get").callAsync(-3)).containsExactly(-3);
        assertThat(
                on(TestClass.class).classMethod("get", int.class).callParallel(17)).containsExactly(
                17);

        assertThat(on(new TestClass()).as(TestInterface.class).getInt(2)).isEqualTo(2);

        try {

            on(TestClass.class).method("get").callAsync();

            fail();

        } catch (final RoutineException ignored) {

        }

        try {

            on(TestClass.class).method("take");

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        assertThat(on(new TestClass()).as(TestInterfaceAsync.class).take(77)).isEqualTo(77);
        assertThat(on(new TestClass()).as(TestInterfaceAsync.class).getOne().readFirst()).isEqualTo(
                1);

        final TestInterfaceAsync testInterfaceAsync =
                on(new TestClass()).as(TestInterfaceAsync.class);
        assertThat(testInterfaceAsync.getInt(testInterfaceAsync.getOne())).isEqualTo(1);
    }

    @SuppressWarnings("ConstantConditions")
    public void testParameterChannelError() {

        try {

            new DefaultParameterChannel<Object, Object>(null, Runners.shared(), false, false,
                                                        Logger.create(new NullLog(),
                                                                      LogLevel.DEBUG));

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultParameterChannel<Object, Object>(new TestExecutionProvider(), null, false,
                                                        false, Logger.create(new NullLog(),
                                                                             LogLevel.DEBUG));

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultParameterChannel<Object, Object>(new TestExecutionProvider(),
                                                        Runners.shared(), false, false, null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            final DefaultParameterChannel<Object, Object> channel =
                    new DefaultParameterChannel<Object, Object>(new TestExecutionProvider(),
                                                                Runners.shared(), false, false,
                                                                Logger.create(new NullLog(),
                                                                              LogLevel.DEBUG));

            channel.results();
            channel.pass("test");

            fail();

        } catch (final IllegalStateException ignored) {

        }
    }

    @SuppressWarnings("ConstantConditions")
    public void testResultChannelError() {

        try {

            new DefaultResultChannel<Object>(null, Runners.shared(), false,
                                             Logger.create(new NullLog(), LogLevel.DEBUG));


        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultResultChannel<Object>(new TestAbortHandler(), null, false,
                                             Logger.create(new NullLog(), LogLevel.DEBUG));


        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultResultChannel<Object>(new TestAbortHandler(), Runners.shared(), false, null);


        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultResultChannel<Object>(new TestAbortHandler(), Runners.shared(), false,
                                             Logger.create(new NullLog(), LogLevel.DEBUG)).after(
                    null);


        } catch (final NullPointerException ignored) {

        }

        try {

            new DefaultResultChannel<Object>(new TestAbortHandler(), Runners.shared(), false,
                                             Logger.create(new NullLog(), LogLevel.DEBUG)).after(0,
                                                                                                 null);


        } catch (final NullPointerException ignored) {

        }

        final Routine<String, String> routine = JavaRoutine.on(tokenOf(DelayedExecution.class))
                                                           .logLevel(LogLevel.SILENT)
                                                           .withArgs(TimeDuration.ZERO)
                                                           .buildRoutine();
        final OutputChannel<String> channel = routine.run();

        try {

            channel.afterMax(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            channel.afterMax(0, null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            channel.bind(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            channel.readAllInto(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        final BasicOutputConsumer<String> consumer = new BasicOutputConsumer<String>() {};

        try {

            channel.bind(consumer).bind(consumer);

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            channel.iterator();

            fail();

        } catch (final IllegalStateException ignored) {

        }

        final Routine<String, String> routine1 = JavaRoutine.on(tokenOf(DelayedExecution.class))
                                                            .logLevel(LogLevel.SILENT)
                                                            .withArgs(TimeDuration.ZERO)
                                                            .buildRoutine();
        final Iterator<String> iterator =
                routine1.run("test").afterMax(TimeDuration.millis(10)).iterator();

        assertThat(iterator.next()).isEqualTo("test");
        iterator.remove();

        try {

            iterator.remove();

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            iterator.next();

            fail();

        } catch (final NoSuchElementException ignored) {

        }

        try {

            routine1.run().immediately().iterator().next();

            fail();

        } catch (final NoSuchElementException ignored) {

        }
    }

    public void testRoutine() {

        final BasicExecution<Integer, Integer> execSquare = new BasicExecution<Integer, Integer>() {

            @Override
            public void onInput(final Integer integer,
                    @Nonnull final ResultChannel<Integer> results) {

                final int input = integer;

                results.pass(input * input);
            }
        };

        final Routine<Integer, Integer> squareRoutine =
                on(ClassToken.tokenOf(execSquare)).withArgs(this).buildRoutine();

        assertThat(squareRoutine.call(1, 2, 3, 4)).containsExactly(1, 4, 9, 16);
        assertThat(squareRoutine.callAsync(1, 2, 3, 4)).containsExactly(1, 4, 9, 16);
        assertThat(squareRoutine.callParallel(1, 2, 3, 4)).containsOnly(1, 4, 9, 16);
        assertThat(squareRoutine.run(1, 2, 3, 4).readAll()).containsExactly(1, 4, 9, 16);
        assertThat(squareRoutine.runAsync(1, 2, 3, 4).readAll()).containsExactly(1, 4, 9, 16);
        assertThat(squareRoutine.runParallel(1, 2, 3, 4).readAll()).containsOnly(1, 4, 9, 16);
    }

    public void testRoutineFunction() {

        final ExecutionBody<Integer, Integer> execSum = new ExecutionBody<Integer, Integer>() {

            @Override
            public void onExec(@Nonnull final List<? extends Integer> integers,
                    @Nonnull final ResultChannel<Integer> results) {

                int sum = 0;

                for (final Integer integer : integers) {

                    sum += integer;
                }

                results.pass(sum);
            }
        };

        final Routine<Integer, Integer> sumRoutine =
                on(ClassToken.tokenOf(execSum)).withArgs(this).buildRoutine();

        assertThat(sumRoutine.call(1, 2, 3, 4)).containsExactly(10);
        assertThat(sumRoutine.callAsync(1, 2, 3, 4)).containsExactly(10);
        assertThat(sumRoutine.run(1, 2, 3, 4).readAll()).containsExactly(10);
        assertThat(sumRoutine.runAsync(1, 2, 3, 4).readAll()).containsExactly(10);
    }

    public void testTimeout() {

        final Routine<String, String> routine =
                on(tokenOf(DelayedExecution.class)).withArgs(TimeDuration.seconds(3))
                                                   .buildRoutine();

        final OutputChannel<String> channel = routine.runAsync("test");
        assertThat(channel.immediately().readAll()).isEmpty();

        try {

            channel.afterMax(TimeDuration.millis(10))
                   .eventuallyThrow(new IllegalStateException())
                   .readFirst();

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            channel.readAll();

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            channel.iterator().hasNext();

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            channel.iterator().next();

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            channel.waitComplete();

            fail();

        } catch (final IllegalStateException ignored) {

        }
    }

    private void testChained(final Routine<String, String> before,
            final Routine<String, String> after, final String input, final String expected) {

        try {

            before.call(after.run(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.callAsync(after.run(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.callParallel(after.run(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.run(after.run(input)).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.run(after.run(input))) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.runAsync(after.run(input)).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.runAsync(after.run(input))) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.runParallel(after.run(input)).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.runParallel(after.run(input))) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.invoke().pass(after.run(input)).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.invoke().pass(after.run(input)).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.invokeAsync().pass(after.run(input)).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.invokeAsync().pass(after.run(input)).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.invokeParallel().pass(after.run(input)).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.invokeParallel().pass(after.run(input)).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.call(after.runAsync(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.callAsync(after.runAsync(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.callParallel(after.runAsync(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.run(after.runAsync(input)).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.run(after.runAsync(input))) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.runAsync(after.runAsync(input)).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.runAsync(after.runAsync(input))) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.invoke().pass(after.runAsync(input)).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.invoke().pass(after.runAsync(input)).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.invokeAsync().pass(after.runAsync(input)).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.invokeAsync().pass(after.runAsync(input)).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.call(after.runParallel(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.callAsync(after.runParallel(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.callParallel(after.runParallel(input));

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.run(after.runParallel(input)).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.run(after.runParallel(input))) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.runAsync(after.runParallel(input)).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.runAsync(after.runParallel(input))) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.invoke().pass(after.runParallel(input)).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.invoke().pass(after.runParallel(input)).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            before.invokeAsync().pass(after.runParallel(input)).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : before.invokeAsync().pass(after.runParallel(input)).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }
    }

    private void testConsumer(final OutputConsumer<String> consumer) {

        final String input = "test";
        final Routine<String, String> routine =
                on(tokenOf(DelayedExecution.class)).withArgs(TimeDuration.ZERO).buildRoutine();

        assertThat(routine.run(input).bind(consumer).waitComplete()).isTrue();
        assertThat(routine.runAsync(input).bind(consumer).waitComplete()).isTrue();
        assertThat(routine.runParallel(input).bind(consumer).waitComplete()).isTrue();
        assertThat(routine.invoke().pass(input).results().bind(consumer).waitComplete()).isTrue();
        assertThat(
                routine.invokeAsync().pass(input).results().bind(consumer).waitComplete()).isTrue();
        assertThat(routine.invokeParallel()
                          .pass(input)
                          .results()
                          .bind(consumer)
                          .waitComplete()).isTrue();
    }

    private void testException(final Routine<String, String> routine, final String input,
            final String expected) {

        try {

            routine.call(input);

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            routine.callAsync(input);

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            routine.callParallel(input);

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            routine.run(input).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : routine.run(input)) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            routine.runAsync(input).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : routine.runAsync(input)) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            routine.runParallel(input).readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : routine.runParallel(input)) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            routine.invoke().pass(input).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : routine.invoke().pass(input).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            routine.invokeAsync().pass(input).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : routine.invokeAsync().pass(input).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            routine.invokeParallel().pass(input).results().readAll();

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }

        try {

            for (final String s : routine.invokeParallel().pass(input).results()) {

                assertThat(s).isNotEmpty();
            }

            fail();

        } catch (final RoutineException e) {

            assertThat(e.getCause().getMessage()).isEqualTo(expected);
        }
    }

    private interface TestInterface {

        public int getInt(int i);
    }

    private interface TestInterfaceAsync {

        @AsyncParameters({int.class})
        public int getInt(OutputChannel<Integer> i);

        @AsyncResult
        public OutputChannel<Integer> getOne();

        @Async(name = "getInt")
        public int take(int i);
    }

    private static class ConstructorException extends BasicExecution<Object, Object> {

        public ConstructorException() {

            throw new IllegalStateException();
        }
    }

    private static class DelayedChannelExecution extends BasicExecution<String, String> {

        private final TimeDuration mDelay;

        private final Routine<String, String> mRoutine;

        private boolean mFlag;

        public DelayedChannelExecution(final TimeDuration delay) {

            mDelay = delay;
            mRoutine =
                    on(tokenOf(DelayedExecution.class)).withArgs(TimeDuration.ZERO).buildRoutine();
        }

        @Override
        public void onInput(final String s, @Nonnull final ResultChannel<String> results) {

            if (mFlag) {

                results.after(mDelay).pass((OutputChannel<String>) null);

            } else {

                results.after(mDelay.time, mDelay.unit).pass((OutputChannel<String>) null);
            }

            results.pass(mRoutine.runAsync(s));

            mFlag = !mFlag;
        }
    }

    private static class DelayedExecution extends BasicExecution<String, String> {

        private final TimeDuration mDelay;

        private boolean mFlag;

        public DelayedExecution(final TimeDuration delay) {

            mDelay = delay;
        }

        @Override
        public void onInput(final String s, @Nonnull final ResultChannel<String> results) {

            if (mFlag) {

                results.after(mDelay);

            } else {

                results.after(mDelay.time, mDelay.unit);
            }

            results.pass(s);

            mFlag = !mFlag;
        }
    }

    private static class DelayedListExecution extends BasicExecution<String, String> {

        private final int mCount;

        private final TimeDuration mDelay;

        private final ArrayList<String> mList;

        private boolean mFlag;

        public DelayedListExecution(final TimeDuration delay, final int listCount) {

            mDelay = delay;
            mCount = listCount;
            mList = new ArrayList<String>(listCount);
        }

        @Override
        public void onInput(final String s, @Nonnull final ResultChannel<String> results) {

            final ArrayList<String> list = mList;
            list.add(s);

            if (list.size() >= mCount) {

                if (mFlag) {

                    results.after(mDelay).pass((String[]) null).pass(list);

                } else {

                    results.after(mDelay.time, mDelay.unit)
                           .pass((List<String>) null)
                           .pass(list.toArray(new String[list.size()]));
                }

                results.now();
                list.clear();

                mFlag = !mFlag;
            }
        }

        @Override
        public void onResult(@Nonnull final ResultChannel<String> results) {

            final ArrayList<String> list = mList;
            results.after(mDelay).pass(list);
            list.clear();
        }
    }

    private static class PassThroughExecution extends BasicExecution<String, String> {

        @Override
        public void onInput(final String s, @Nonnull final ResultChannel<String> results) {

            results.pass(s);
        }
    }

    private static class TestAbortHandler implements AbortHandler {

        @Override
        public void onAbort(@Nullable final Throwable throwable, final long delay,
                @Nonnull final TimeUnit timeUnit) {

        }
    }

    private static class TestClass implements TestInterface {

        public static final String GET_METHOD = "get";

        @Async(name = GET_METHOD)
        public static int get(final int i) {

            return i;
        }

        @Override
        public int getInt(final int i) {

            return i;
        }

        @Async(name = GET_METHOD)
        public int getOne() {

            return 1;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class TestExecutionProvider implements ExecutionProvider<Object, Object> {

        @Nonnull
        @Override
        public Execution<Object, Object> create() {

            return null;
        }

        @Override
        public void discard(@Nonnull final Execution<Object, Object> execution) {

        }

        @Override
        public void recycle(@Nonnull final Execution<Object, Object> execution) {

        }
    }

    private static class TestInputIterator implements InputIterator<Object> {

        @Nullable
        @Override
        public Throwable getAbortException() {

            return null;
        }

        @Override
        public boolean hasInput() {

            return false;
        }

        @Override
        public boolean isAborting() {

            return false;
        }

        @Override
        public boolean isComplete() {

            return false;
        }

        @Override
        public Object nextInput() {

            return null;
        }

        @Override
        public void onAbortComplete() {

        }

        @Override
        public void onConsumeInput() {

        }
    }
}