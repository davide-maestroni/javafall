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
package com.bmd.android.jrt.runner;

import android.os.HandlerThread;
import android.os.Looper;
import android.test.AndroidTestCase;

import com.bmd.jrt.runner.Invocation;
import com.bmd.jrt.runner.Runner;
import com.bmd.jrt.runner.RunnerDecorator;
import com.bmd.jrt.time.Time;
import com.bmd.jrt.time.TimeDuration;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static com.bmd.jrt.time.Time.current;
import static com.bmd.jrt.time.TimeDuration.ZERO;
import static com.bmd.jrt.time.TimeDuration.micros;
import static com.bmd.jrt.time.TimeDuration.millis;
import static com.bmd.jrt.time.TimeDuration.nanos;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Android runners unit tests.
 * <p/>
 * Created by davide on 10/10/14.
 */
public class AndroidRunnerTest extends AndroidTestCase {

    @SuppressWarnings("ConstantConditions")
    public void testError() {

        try {

            new LooperRunner(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    public void testLooperRunner() throws InterruptedException {

        testRunner(new LooperRunner(Looper.myLooper()));
        testRunner(AndroidRunners.main());
        testRunner(AndroidRunners.my());
        testRunner(AndroidRunners.thread(new HandlerThread("test")));
        testRunner(new RunnerDecorator(AndroidRunners.main()));
    }

    public void testMainRunner() throws InterruptedException {

        testRunner(new MainRunner());
    }

    public void testTaskRunner() throws InterruptedException {

        testRunner(new AsyncTaskRunner(null));
        testRunner(AndroidRunners.task());
        testRunner(AndroidRunners.task(Executors.newCachedThreadPool()));
        testRunner(new RunnerDecorator(AndroidRunners.task(Executors.newSingleThreadExecutor())));
    }

    private void testRunner(final Runner runner) throws InterruptedException {

        final Random random = new Random(System.currentTimeMillis());
        final ArrayList<TestAbortInvocation> invocations = new ArrayList<TestAbortInvocation>();

        for (int i = 0; i < 13; i++) {

            if (random.nextBoolean()) {

                final TimeDuration delay;
                final int unit = random.nextInt(4);

                switch (unit) {

                    case 0:

                        delay = millis((long) Math.floor(random.nextFloat() * 500));

                        break;

                    case 1:

                        delay = micros((long) Math.floor(
                                random.nextFloat() * millis(500).toMicros()));

                        break;

                    case 2:

                        delay = nanos((long) Math.floor(
                                random.nextFloat() * millis(500).toNanos()));

                        break;

                    default:

                        delay = ZERO;

                        break;
                }

                final TestRunInvocation invocation = new TestRunInvocation(delay);
                invocations.add(invocation);

                runner.run(invocation, delay.time, delay.unit);

            } else {

                final TestAbortInvocation invocation = new TestAbortInvocation();
                invocations.add(invocation);

                runner.runAbort(invocation);
            }
        }

        for (final TestAbortInvocation invocation : invocations) {

            invocation.await();
            assertThat(invocation.isPassed()).isTrue();
        }

        invocations.clear();

        final ArrayList<TimeDuration> delays = new ArrayList<TimeDuration>();

        for (int i = 0; i < 13; i++) {

            final TimeDuration delay;
            final int unit = random.nextInt(4);

            switch (unit) {

                case 0:

                    delay = millis((long) Math.floor(random.nextFloat() * 500));

                    break;

                case 1:

                    delay = micros((long) Math.floor(random.nextFloat() * millis(500).toMicros()));

                    break;

                case 2:

                    delay = nanos((long) Math.floor(random.nextFloat() * millis(500).toNanos()));

                    break;

                default:

                    delay = ZERO;

                    break;
            }

            delays.add(delay);

            final TestRunInvocation invocation = new TestRunInvocation(delay);
            invocations.add(invocation);
        }

        final TestRecursiveInvocation recursiveInvocation =
                new TestRecursiveInvocation(runner, invocations, delays, ZERO);

        runner.run(recursiveInvocation, ZERO.time, ZERO.unit);

        for (final TestAbortInvocation invocation : invocations) {

            invocation.await();
            assertThat(invocation.isPassed()).isTrue();
        }
    }

    private static class TestAbortInvocation implements Invocation {

        protected final Semaphore mSemaphore = new Semaphore(0);

        private boolean mIsAbort;

        @Override
        public void abort() {

            mIsAbort = true;

            mSemaphore.release();
        }

        @Override
        public void run() {

            mSemaphore.release();
        }

        public void await() throws InterruptedException {

            mSemaphore.acquire();
        }

        public boolean isPassed() {

            return mIsAbort;
        }
    }

    private static class TestRecursiveInvocation extends TestRunInvocation {

        private final ArrayList<TimeDuration> mDelays;

        private final ArrayList<TestAbortInvocation> mInvocations;

        private final Runner mRunner;

        public TestRecursiveInvocation(final Runner runner,
                final ArrayList<TestAbortInvocation> invocations,
                final ArrayList<TimeDuration> delays, final TimeDuration delay) {

            super(delay);

            mRunner = runner;
            mInvocations = invocations;
            mDelays = delays;
        }

        @Override
        public void run() {

            final ArrayList<TestAbortInvocation> invocations = mInvocations;
            final ArrayList<TimeDuration> delays = mDelays;
            final Runner runner = mRunner;
            final int size = invocations.size();

            for (int i = 0; i < size; i++) {

                final TimeDuration delay = delays.get(i);
                runner.run(invocations.get(i), delay.time, delay.unit);
            }

            super.run();
        }
    }

    private static class TestRunInvocation extends TestAbortInvocation {

        private final TimeDuration mDelay;

        private final Time mStartTime;

        private boolean mIsPassed;

        public TestRunInvocation(final TimeDuration delay) {

            mStartTime = current();
            mDelay = delay;
        }

        @Override
        public void run() {

            // the JVM might not have nanosecond precision...
            mIsPassed = (current().toMillis() - mStartTime.toMillis() + 1 >= mDelay.toMillis());

            mSemaphore.release();
        }

        @Override
        public boolean isPassed() {

            return mIsPassed;
        }
    }
}