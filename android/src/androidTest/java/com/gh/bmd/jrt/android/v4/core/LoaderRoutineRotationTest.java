/*
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
package com.gh.bmd.jrt.android.v4.core;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.test.ActivityInstrumentationTestCase2;

import com.gh.bmd.jrt.android.invocation.TemplateContextInvocation;
import com.gh.bmd.jrt.builder.InvocationConfiguration.OrderType;
import com.gh.bmd.jrt.channel.OutputChannel;
import com.gh.bmd.jrt.channel.ResultChannel;
import com.gh.bmd.jrt.routine.Routine;
import com.gh.bmd.jrt.util.TimeDuration;

import java.util.concurrent.Semaphore;

import javax.annotation.Nonnull;

import static com.gh.bmd.jrt.android.invocation.ContextInvocations.factoryOf;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Loader routine rotation unit tests.
 * <p/>
 * Created by davide-maestroni on 1/28/15.
 */
@TargetApi(VERSION_CODES.FROYO)
public class LoaderRoutineRotationTest
        extends ActivityInstrumentationTestCase2<RotationTestActivity> {

    public LoaderRoutineRotationTest() {

        super(RotationTestActivity.class);
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    public void testActivityRotationChannel() throws InterruptedException {

        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

            return;
        }

        final TimeDuration timeout = TimeDuration.seconds(10);
        JRoutine.onActivity(getActivity(), factoryOf(ToUpperCase.class))
                .invocations()
                .withOutputOrder(OrderType.BY_CALL)
                .set()
                .loaders()
                .withId(0)
                .set()
                .asyncCall("test1", "test2");

        final Semaphore semaphore = new Semaphore(0);

        getActivity().runOnUiThread(new Runnable() {

            public void run() {

                getActivity().recreate();
                semaphore.release();
            }
        });

        semaphore.acquire();
        getInstrumentation().waitForIdleSync();

        final OutputChannel<String> channel =
                JRoutine.onActivity(getActivity()).loaders().withId(0).set().buildChannel();

        assertThat(channel.afterMax(timeout).all()).containsExactly("TEST1", "TEST2");
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    public void testActivityRotationInputs() throws InterruptedException {

        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

            return;
        }

        final TimeDuration timeout = TimeDuration.seconds(10);
        final Routine<String, String> routine1 =
                JRoutine.onActivity(getActivity(), factoryOf(ToUpperCase.class)).buildRoutine();
        routine1.asyncCall("test1");
        routine1.asyncCall("test2");

        final Semaphore semaphore = new Semaphore(0);

        getActivity().runOnUiThread(new Runnable() {

            public void run() {

                getActivity().recreate();
                semaphore.release();
            }
        });

        semaphore.acquire();
        getInstrumentation().waitForIdleSync();

        final Routine<String, String> routine2 =
                JRoutine.onActivity(getActivity(), factoryOf(ToUpperCase.class)).buildRoutine();
        final OutputChannel<String> result1 = routine2.asyncCall("test1").afterMax(timeout);
        final OutputChannel<String> result2 = routine2.asyncCall("test2").afterMax(timeout);

        assertThat(result1.next()).isEqualTo("TEST1");
        assertThat(result2.next()).isEqualTo("TEST2");
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    public void testActivityRotationSame() throws InterruptedException {

        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

            return;
        }

        final TimeDuration timeout = TimeDuration.seconds(10);
        final Data data1 = new Data();
        final Routine<Data, Data> routine1 =
                JRoutine.onActivity(getActivity(), factoryOf(Delay.class)).buildRoutine();
        routine1.asyncCall(data1);
        routine1.asyncCall(data1);

        final Semaphore semaphore = new Semaphore(0);

        getActivity().runOnUiThread(new Runnable() {

            public void run() {

                getActivity().recreate();
                semaphore.release();
            }
        });

        semaphore.acquire();
        getInstrumentation().waitForIdleSync();

        final Routine<Data, Data> routine2 =
                JRoutine.onActivity(getActivity(), factoryOf(Delay.class)).buildRoutine();
        final OutputChannel<Data> result1 = routine2.asyncCall(data1).afterMax(timeout);
        final OutputChannel<Data> result2 = routine2.asyncCall(data1).afterMax(timeout);

        assertThat(result1.next()).isSameAs(data1);
        assertThat(result2.next()).isSameAs(data1);
    }

    private static class Data {

    }

    private static class Delay extends TemplateContextInvocation<Data, Data> {

        @Override
        public void onInput(final Data d, @Nonnull final ResultChannel<Data> result) {

            result.after(TimeDuration.millis(500)).pass(d);
        }
    }

    private static class ToUpperCase extends TemplateContextInvocation<String, String> {

        @Override
        public void onInput(final String s, @Nonnull final ResultChannel<String> result) {

            result.after(TimeDuration.millis(500)).pass(s.toUpperCase());
        }
    }
}
