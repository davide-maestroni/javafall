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
package com.gh.bmd.jrt.android.v11.core;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.test.ActivityInstrumentationTestCase2;

import com.gh.bmd.jrt.android.invocation.TemplateContextInvocation;
import com.gh.bmd.jrt.builder.RoutineConfiguration.OrderType;
import com.gh.bmd.jrt.channel.OutputChannel;
import com.gh.bmd.jrt.channel.ResultChannel;
import com.gh.bmd.jrt.common.ClassToken;
import com.gh.bmd.jrt.routine.Routine;
import com.gh.bmd.jrt.time.TimeDuration;

import java.util.concurrent.Semaphore;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Invocation context routine rotation unit tests.
 * <p/>
 * Created by davide-maestroni on 1/28/15.
 */
@TargetApi(VERSION_CODES.HONEYCOMB)
public class LoaderRoutineBuilderRotationTest
        extends ActivityInstrumentationTestCase2<RotationTestActivity> {

    public LoaderRoutineBuilderRotationTest() {

        super(RotationTestActivity.class);
    }

    public void testActivityRotationChannel() throws InterruptedException {

        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

            return;
        }

        final TimeDuration timeout = TimeDuration.seconds(10);
        JRoutine.onActivity(getActivity(), ClassToken.tokenOf(ToUpperCase.class))
                .withRoutine()
                .withOutputOrder(OrderType.PASS_ORDER)
                .set()
                .withLoader()
                .withId(0)
                .set()
                .callAsync("test1", "test2");

        final Semaphore semaphore = new Semaphore(0);

        getActivity().runOnUiThread(new Runnable() {

            public void run() {

                getActivity().recreate();
                semaphore.release();
            }
        });

        semaphore.acquire();
        getInstrumentation().waitForIdleSync();

        final OutputChannel<String> channel = JRoutine.onActivity(getActivity(), 0).buildChannel();

        assertThat(channel.afterMax(timeout).readAll()).containsExactly("TEST1", "TEST2");
    }

    public void testActivityRotationInputs() throws InterruptedException {

        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

            return;
        }

        final TimeDuration timeout = TimeDuration.seconds(10);
        final Routine<String, String> routine1 =
                JRoutine.onActivity(getActivity(), ClassToken.tokenOf(ToUpperCase.class))
                        .buildRoutine();
        routine1.callAsync("test1");
        routine1.callAsync("test2");

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
                JRoutine.onActivity(getActivity(), ClassToken.tokenOf(ToUpperCase.class))
                        .buildRoutine();
        final OutputChannel<String> result1 = routine2.callAsync("test1").afterMax(timeout);
        final OutputChannel<String> result2 = routine2.callAsync("test2").afterMax(timeout);

        assertThat(result1.readNext()).isEqualTo("TEST1");
        assertThat(result2.readNext()).isEqualTo("TEST2");
    }

    public void testActivityRotationSame() throws InterruptedException {

        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {

            return;
        }

        final TimeDuration timeout = TimeDuration.seconds(10);
        final Data data1 = new Data();
        final Routine<Data, Data> routine1 =
                JRoutine.onActivity(getActivity(), ClassToken.tokenOf(Delay.class)).buildRoutine();
        routine1.callAsync(data1);
        routine1.callAsync(data1);

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
                JRoutine.onActivity(getActivity(), ClassToken.tokenOf(Delay.class)).buildRoutine();
        final OutputChannel<Data> result1 = routine2.callAsync(data1).afterMax(timeout);
        final OutputChannel<Data> result2 = routine2.callAsync(data1).afterMax(timeout);

        assertThat(result1.readNext()).isSameAs(data1);
        assertThat(result2.readNext()).isSameAs(data1);
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