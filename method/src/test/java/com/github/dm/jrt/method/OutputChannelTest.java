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

package com.github.dm.jrt.method;

import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.channel.AbortException;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.channel.OutputTimeoutException;
import com.github.dm.jrt.core.config.ChannelConfiguration.OrderType;
import com.github.dm.jrt.core.config.ChannelConfiguration.TimeoutActionType;
import com.github.dm.jrt.core.error.DeadlockException;
import com.github.dm.jrt.core.error.TimeoutException;
import com.github.dm.jrt.core.invocation.IdentityInvocation;
import com.github.dm.jrt.core.log.Log;
import com.github.dm.jrt.core.log.Log.Level;
import com.github.dm.jrt.core.log.NullLog;
import com.github.dm.jrt.core.runner.Runners;
import com.github.dm.jrt.core.util.UnitDuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.core.util.BackoffBuilder.noDelay;
import static com.github.dm.jrt.core.util.UnitDuration.millis;
import static com.github.dm.jrt.core.util.UnitDuration.seconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Channel decorator unit tests.
 * <p>
 * Created by davide-maestroni on 08/14/2016.
 */
public class OutputChannelTest {

    @Test
    @SuppressWarnings({"ConstantConditions", "ThrowableResultOfMethodCallIgnored"})
    public void testAbort() {
        final UnitDuration timeout = seconds(1);
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.abort(new IllegalStateException());
        try {
            channel.after(timeout).throwError();

        } catch (final AbortException ex) {
            assertThat(ex.getCause()).isExactlyInstanceOf(IllegalStateException.class);
        }

        assertThat(channel.getError().getCause()).isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testAbortDelay() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test");
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        final ArrayList<String> results = new ArrayList<String>();
        channel.after(10, TimeUnit.MILLISECONDS).allInto(results);
        assertThat(results).isEmpty();
        assertThat(channel.immediately().eventuallyContinue().getComplete()).isFalse();
        assertThat(channel.immediately().abort()).isTrue();
        try {
            channel.next();
            fail();

        } catch (final AbortException ignored) {
        }

        assertThat(channel.isOpen()).isFalse();
    }

    @Test
    public void testAllIntoTimeout() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyAbort().eventuallyFail();
        try {
            channel.allInto(new ArrayList<String>());
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testAllIntoTimeout2() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail().after(millis(10));
        try {
            channel.allInto(new ArrayList<String>());
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testAllTimeout() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail();
        try {
            channel.all();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testAllTimeout2() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail().after(millis(10));
        try {
            channel.all();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testAsynchronousInput() {
        final UnitDuration timeout = seconds(1);
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);

                } catch (final InterruptedException ignored) {

                } finally {
                    channel.pass("test").close();
                }
            }
        }.start();
        final Channel<String, String> outputChannel =
                JRoutineCore.with(IdentityInvocation.<String>factoryOf()).call(channel);
        assertThat(outputChannel.after(timeout).next()).isEqualTo("test");
        assertThat(outputChannel.getComplete()).isTrue();
    }

    @Test
    public void testAsynchronousInput2() {
        final UnitDuration timeout = seconds(1);
        final OutputChannel<String> channel1 = //
                RoutineMethod.toOutput(JRoutineCore.io()
                                                   .applyChannelConfiguration()
                                                   .withOrder(OrderType.SORTED)
                                                   .configured().<String>buildChannel());
        new Thread() {

            @Override
            public void run() {
                channel1.after(1, TimeUnit.MILLISECONDS)
                        .after(millis(200))
                        .pass("test1", "test2")
                        .pass(Collections.singleton("test3"))
                        .close();
            }
        }.start();
        final Channel<String, String> outputChannel1 =
                JRoutineCore.with(IdentityInvocation.<String>factoryOf()).call(channel1);
        assertThat(outputChannel1.after(timeout).all()).containsExactly("test1", "test2", "test3");
    }

    @Test
    public void testEmpty() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        assertThat(channel.isEmpty()).isTrue();
        assertThat(channel.pass("test").isEmpty()).isFalse();
        channel.after(seconds(1)).next();
        assertThat(channel.isEmpty()).isTrue();
        assertThat(channel.after(millis(100)).pass("test").isEmpty()).isFalse();
        assertThat(channel.close().after(seconds(10)).getComplete()).isTrue();
        assertThat(channel.isEmpty()).isFalse();
    }

    @Test
    public void testEmptyAbort() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        assertThat(channel.isEmpty()).isTrue();
        assertThat(channel.pass("test").isEmpty()).isFalse();
        assertThat(channel.abort()).isTrue();
        assertThat(channel.isEmpty()).isFalse();
    }

    @Test
    public void testHasNextIteratorTimeout() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail();
        try {
            channel.iterator().hasNext();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testHasNextIteratorTimeout2() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail().after(millis(10));
        try {
            channel.iterator().hasNext();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testMaxSize() {
        try {
            RoutineMethod.toOutput(JRoutineCore.io()
                                               .applyChannelConfiguration()
                                               .withMaxSize(1)
                                               .configured()
                                               .buildChannel()).pass("test1", "test2");
            fail();

        } catch (final DeadlockException ignored) {
        }
    }

    @Test
    public void testNextIteratorTimeout() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail();
        try {
            channel.iterator().next();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testNextIteratorTimeout2() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail().after(millis(10));
        try {
            channel.iterator().next();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testNextList() {
        assertThat(RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                                .pass("test1", "test2", "test3", "test4")
                                .close()
                                .after(seconds(1))
                                .next(2)).containsExactly("test1", "test2");
        assertThat(RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                                .pass("test1")
                                .close()
                                .eventuallyContinue()
                                .after(seconds(1))
                                .next(2)).containsExactly("test1");
        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .pass("test1")
                         .eventuallyAbort()
                         .after(seconds(1))
                         .next(2);
            fail();

        } catch (final AbortException ignored) {
        }

        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .pass("test1")
                         .eventuallyAbort(new IllegalStateException())
                         .after(seconds(1))
                         .next(2);
            fail();

        } catch (final AbortException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(IllegalStateException.class);
        }

        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .pass("test1")
                         .eventuallyFail()
                         .after(seconds(1))
                         .next(2);
            fail();

        } catch (final TimeoutException ignored) {
        }
    }

    @Test
    public void testNextOr() {
        assertThat(RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                                .pass("test1")
                                .after(seconds(1))
                                .nextOrElse(2)).isEqualTo("test1");
        assertThat(RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                                .eventuallyContinue()
                                .after(seconds(1))
                                .nextOrElse(2)).isEqualTo(2);
        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .eventuallyAbort()
                         .after(millis(100))
                         .nextOrElse("test2");
            fail();

        } catch (final AbortException ignored) {
        }

        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .eventuallyAbort(new IllegalStateException())
                         .after(millis(100))
                         .nextOrElse("test2");
            fail();

        } catch (final AbortException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(IllegalStateException.class);
        }

        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .eventuallyFail()
                         .after(millis(100))
                         .nextOrElse("test2");
            fail();

        } catch (final TimeoutException ignored) {
        }
    }

    @Test
    public void testNextTimeout() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail();
        try {
            channel.next();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testNextTimeout2() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        channel.after(seconds(3)).pass("test").close();
        assertThat(channel.immediately().eventuallyContinue().all()).isEmpty();
        channel.eventuallyFail().after(millis(10));
        try {
            channel.next();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }

        assertThat(channel.getComplete()).isFalse();
    }

    @Test
    public void testOrderType() {
        final UnitDuration timeout = seconds(1);
        final OutputChannel<Object> channel = //
                RoutineMethod.toOutput(JRoutineCore.io()
                                                   .applyChannelConfiguration()
                                                   .withOrder(OrderType.SORTED)
                                                   .withRunner(Runners.sharedRunner())
                                                   .withMaxSize(1)
                                                   .withBackoff(noDelay())
                                                   .withLogLevel(Level.DEBUG)
                                                   .withLog(new NullLog())
                                                   .configured()
                                                   .buildChannel());
        channel.pass(-77L);
        assertThat(channel.after(timeout).next()).isEqualTo(-77L);
        final OutputChannel<Object> channel1 = RoutineMethod.outputChannel();
        channel1.after(millis(200)).pass(23).immediately().pass(-77L).close();
        assertThat(channel1.after(timeout).all()).containsOnly(23, -77L);
        final OutputChannel<Object> channel2 = RoutineMethod.outputChannel();
        channel2.unsorted().sorted();
        channel2.after(millis(200)).pass(23).immediately().pass(-77L).close();
        assertThat(channel2.after(timeout).all()).containsExactly(23, -77L);
    }

    @Test
    public void testPartialOut() {
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        new Thread() {

            @Override
            public void run() {
                channel.pass("test");
            }
        }.start();
        final long startTime = System.currentTimeMillis();
        final Channel<String, String> outputChannel =
                JRoutineCore.with(IdentityInvocation.<String>factoryOf())
                            .call(channel)
                            .eventuallyContinue();
        assertThat(outputChannel.after(millis(500)).all()).containsExactly("test");
        assertThat(System.currentTimeMillis() - startTime).isLessThan(2000);
        assertThat(outputChannel.immediately().getComplete()).isFalse();
        channel.close();
        assertThat(channel.isOpen()).isFalse();
        assertThat(outputChannel.after(millis(500)).getComplete()).isTrue();
    }

    @Test
    public void testPassTimeout() {
        final OutputChannel<Object> channel1 = //
                RoutineMethod.toOutput(JRoutineCore.io()
                                                   .applyChannelConfiguration()
                                                   .withOutputTimeout(millis(10))
                                                   .withOutputTimeoutAction(
                                                           TimeoutActionType.CONTINUE)
                                                   .configured()
                                                   .buildChannel());
        assertThat(channel1.all()).isEmpty();
    }

    @Test
    public void testPassTimeout2() {
        final OutputChannel<Object> channel2 = //
                RoutineMethod.toOutput(JRoutineCore.io()
                                                   .applyChannelConfiguration()
                                                   .withOutputTimeout(millis(10))
                                                   .withOutputTimeoutAction(TimeoutActionType.ABORT)
                                                   .configured()
                                                   .buildChannel());
        try {
            channel2.all();
            fail();

        } catch (final AbortException ignored) {
        }
    }

    @Test
    public void testPassTimeout3() {
        final OutputChannel<Object> channel3 = //
                RoutineMethod.toOutput(JRoutineCore.io()
                                                   .applyChannelConfiguration()
                                                   .withOutputTimeout(millis(10))
                                                   .withOutputTimeoutAction(TimeoutActionType.FAIL)
                                                   .configured()
                                                   .buildChannel());
        try {
            channel3.all();
            fail();

        } catch (final OutputTimeoutException ignored) {
        }
    }

    @Test
    public void testPendingInputs() throws InterruptedException {
        final OutputChannel<Object> channel = RoutineMethod.outputChannel();
        assertThat(channel.isOpen()).isTrue();
        channel.pass("test");
        assertThat(channel.isOpen()).isTrue();
        channel.after(millis(500)).pass("test");
        assertThat(channel.isOpen()).isTrue();
        final OutputChannel<Object> outputChannel = RoutineMethod.outputChannel();
        channel.pass(outputChannel);
        assertThat(channel.isOpen()).isTrue();
        channel.immediately().close();
        assertThat(channel.isOpen()).isFalse();
        outputChannel.close();
        assertThat(channel.isOpen()).isFalse();
    }

    @Test
    public void testPendingInputsAbort() throws InterruptedException {
        final OutputChannel<Object> channel = RoutineMethod.outputChannel();
        assertThat(channel.isOpen()).isTrue();
        channel.pass("test");
        assertThat(channel.isOpen()).isTrue();
        channel.after(millis(500)).pass("test");
        assertThat(channel.isOpen()).isTrue();
        final OutputChannel<Object> outputChannel = RoutineMethod.outputChannel();
        channel.pass(outputChannel);
        assertThat(channel.isOpen()).isTrue();
        channel.immediately().abort();
        assertThat(channel.isOpen()).isFalse();
    }

    @Test
    public void testReadFirst() throws InterruptedException {
        final UnitDuration timeout = seconds(1);
        final OutputChannel<String> channel = RoutineMethod.outputChannel();
        new WeakThread(channel).start();
        final Channel<String, String> outputChannel =
                JRoutineCore.with(IdentityInvocation.<String>factoryOf()).call(channel);
        assertThat(outputChannel.after(timeout).next()).isEqualTo("test");
    }

    @Test
    public void testSize() {
        final OutputChannel<Object> channel =
                RoutineMethod.toOutput(JRoutineCore.with(IdentityInvocation.factoryOf()).call());
        assertThat(channel.inputCount()).isEqualTo(0);
        assertThat(channel.outputCount()).isEqualTo(0);
        channel.after(millis(500)).pass("test");
        assertThat(channel.inputCount()).isEqualTo(1);
        assertThat(channel.outputCount()).isEqualTo(0);
        channel.immediately().close();
        assertThat(channel.after(seconds(1)).getComplete()).isTrue();
        assertThat(channel.inputCount()).isEqualTo(0);
        assertThat(channel.outputCount()).isEqualTo(1);
        assertThat(channel.size()).isEqualTo(1);
        assertThat(channel.skipNext(1).outputCount()).isEqualTo(0);

        final OutputChannel<Object> channel1 = RoutineMethod.outputChannel();
        assertThat(channel1.inputCount()).isEqualTo(0);
        assertThat(channel1.outputCount()).isEqualTo(0);
        channel1.after(millis(500)).pass("test");
        assertThat(channel1.inputCount()).isEqualTo(1);
        assertThat(channel1.outputCount()).isEqualTo(1);
        channel1.immediately().close();
        assertThat(channel1.after(seconds(1)).getComplete()).isTrue();
        assertThat(channel1.inputCount()).isEqualTo(1);
        assertThat(channel1.outputCount()).isEqualTo(1);
        assertThat(channel1.size()).isEqualTo(1);
        assertThat(channel1.skipNext(1).outputCount()).isEqualTo(0);
    }

    @Test
    public void testSkip() {
        assertThat(RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                                .pass("test1", "test2", "test3", "test4")
                                .close()
                                .after(seconds(1))
                                .skipNext(2)
                                .all()).containsExactly("test3", "test4");
        assertThat(RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                                .pass("test1")
                                .close()
                                .eventuallyContinue()
                                .after(seconds(1))
                                .skipNext(2)
                                .all()).isEmpty();
        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .pass("test1")
                         .close()
                         .eventuallyAbort()
                         .after(seconds(1))
                         .skipNext(2);
            fail();

        } catch (final AbortException ignored) {
        }

        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .pass("test1")
                         .close()
                         .eventuallyAbort(new IllegalStateException())
                         .after(seconds(1))
                         .skipNext(2);
            fail();

        } catch (final AbortException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(IllegalStateException.class);
        }

        try {
            RoutineMethod.toOutput(JRoutineCore.io().buildChannel())
                         .pass("test1")
                         .close()
                         .eventuallyFail()
                         .after(seconds(1))
                         .skipNext(2);
            fail();

        } catch (final TimeoutException ignored) {
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

    private static class WeakThread extends Thread {

        private final WeakReference<Channel<String, String>> mChannelRef;

        public WeakThread(final Channel<String, String> channel) {
            mChannelRef = new WeakReference<Channel<String, String>>(channel);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(500);

            } catch (final InterruptedException ignored) {

            } finally {
                final Channel<String, String> channel = mChannelRef.get();
                if (channel != null) {
                    channel.pass("test");
                }
            }
        }
    }
}
