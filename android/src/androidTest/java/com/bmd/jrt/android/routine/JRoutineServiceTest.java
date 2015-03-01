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
package com.bmd.jrt.android.routine;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.test.ActivityInstrumentationTestCase2;

import com.bmd.jrt.android.invocation.AndroidPassingInvocation;
import com.bmd.jrt.android.invocation.AndroidSingleCallInvocation;
import com.bmd.jrt.android.invocation.AndroidTemplateInvocation;
import com.bmd.jrt.android.log.AndroidLog;
import com.bmd.jrt.android.runner.MainRunner;
import com.bmd.jrt.builder.RoutineConfiguration;
import com.bmd.jrt.builder.RoutineConfiguration.Builder;
import com.bmd.jrt.builder.RoutineConfiguration.RunnerType;
import com.bmd.jrt.builder.RoutineConfiguration.TimeoutAction;
import com.bmd.jrt.channel.OutputChannel;
import com.bmd.jrt.channel.ReadDeadlockException;
import com.bmd.jrt.channel.ResultChannel;
import com.bmd.jrt.common.AbortException;
import com.bmd.jrt.common.ClassToken;
import com.bmd.jrt.common.InvocationException;
import com.bmd.jrt.common.InvocationInterruptedException;
import com.bmd.jrt.log.Log.LogLevel;
import com.bmd.jrt.routine.Routine;
import com.bmd.jrt.time.TimeDuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import static com.bmd.jrt.builder.RoutineConfiguration.OrderBy;
import static com.bmd.jrt.builder.RoutineConfiguration.builder;
import static com.bmd.jrt.time.TimeDuration.millis;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * JRoutine activity unit tests.
 * <p/>
 * Created by davide on 12/1/15.
 */
@TargetApi(VERSION_CODES.FROYO)
public class JRoutineServiceTest extends ActivityInstrumentationTestCase2<TestActivity> {

    public JRoutineServiceTest() {

        super(TestActivity.class);
    }

    public void testAbort() {

        final TimeDuration timeout = TimeDuration.seconds(10);
        final Data data = new Data();
        final Routine<Data, Data> routine1 =
                JRoutine.onService(getActivity(), ClassToken.tokenOf(Delay.class))
                        .dispatchingOn(Looper.getMainLooper())
                        .withRunnerClass(MainRunner.class)
                        .buildRoutine();

        final OutputChannel<Data> channel = routine1.callAsync(data);
        assertThat(channel.abort(new IllegalArgumentException("test"))).isTrue();

        try {

            channel.afterMax(timeout).readNext();

            fail();

        } catch (final InvocationException e) {

            assertThat(e.getCause().getMessage()).isEqualTo("test");
        }

        final Routine<Data, Data> routine2 =
                JRoutine.onService(getActivity(), ClassToken.tokenOf(Abort.class))
                        .dispatchingOn(Looper.getMainLooper())
                        .buildRoutine();

        try {

            routine2.callAsync(data).afterMax(timeout).readNext();

            fail();

        } catch (final InvocationException e) {

            assertThat(e.getCause().getMessage()).isEqualTo("test");
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void testBuilderError() {

        final ClassToken<AndroidPassingInvocation<String>> classToken =
                new ClassToken<AndroidPassingInvocation<String>>() {};

        try {

            JRoutine.onService(null, classToken);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.onService(getActivity(), null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    public void testInvocations() throws InterruptedException {

        final TimeDuration timeout = TimeDuration.seconds(10);
        final Routine<String, String> routine1 =
                JRoutine.onService(getActivity(), ClassToken.tokenOf(StringPassingInvocation.class))
                        .dispatchingOn(Looper.getMainLooper())
                        .withConfiguration(builder().withSyncRunner(RunnerType.QUEUED)
                                                    .withInputOrder(OrderBy.DELIVERY)
                                                    .withLogLevel(LogLevel.DEBUG)
                                                    .buildConfiguration())
                        .withLogClass(AndroidLog.class)
                        .buildRoutine();
        assertThat(routine1.callSync("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsOnly("1", "2", "3", "4", "5");
        assertThat(routine1.callAsync("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsOnly("1", "2", "3", "4", "5");
        assertThat(routine1.callParallel("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsOnly("1", "2", "3", "4", "5");

        final ClassToken<StringSingleCallInvocation> token =
                ClassToken.tokenOf(StringSingleCallInvocation.class);
        final RoutineConfiguration configuration2 = builder().withSyncRunner(RunnerType.SEQUENTIAL)
                                                             .withOutputOrder(OrderBy.DELIVERY)
                                                             .withLogLevel(LogLevel.DEBUG)
                                                             .buildConfiguration();
        final Routine<String, String> routine2 = JRoutine.onService(getActivity(), token)
                                                         .dispatchingOn(Looper.getMainLooper())
                                                         .withConfiguration(configuration2)
                                                         .withLogClass(AndroidLog.class)
                                                         .buildRoutine();
        assertThat(routine2.callSync("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsExactly("1", "2", "3", "4", "5");
        assertThat(routine2.callAsync("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsExactly("1", "2", "3", "4", "5");
        assertThat(routine2.callParallel("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsOnly("1", "2", "3", "4", "5");

        final Builder builder = RoutineConfiguration.builder().withInputOrder(OrderBy.PASSING)


                                                    .withOutputOrder(OrderBy.PASSING);
        final Routine<String, String> routine3 = JRoutine.onService(getActivity(), token)
                                                         .dispatchingOn(Looper.getMainLooper())
                                                         .withConfiguration(
                                                                 builder.buildConfiguration())
                                                         .buildRoutine();
        assertThat(routine3.callSync("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsExactly("1", "2", "3", "4", "5");
        assertThat(routine3.callAsync("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsExactly("1", "2", "3", "4", "5");
        assertThat(routine3.callParallel("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsExactly("1", "2", "3", "4", "5");

        final RoutineConfiguration configuration4 = builder().withCoreInvocations(0)
                                                             .withMaxInvocations(2)
                                                             .withAvailableTimeout(1,
                                                                                   TimeUnit.SECONDS)
                                                             .withAvailableTimeout(
                                                                     TimeDuration.millis(200))
                                                             .buildConfiguration();
        final Routine<String, String> routine4 = JRoutine.onService(getActivity(), token)
                                                         .dispatchingOn(Looper.getMainLooper())
                                                         .withConfiguration(configuration4)
                                                         .buildRoutine();
        assertThat(routine4.callSync("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsOnly("1", "2", "3", "4", "5");
        assertThat(routine4.callAsync("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsOnly("1", "2", "3", "4", "5");
        assertThat(routine4.callParallel("1", "2", "3", "4", "5")
                           .afterMax(timeout)
                           .readAll()).containsOnly("1", "2", "3", "4", "5");
    }

    public void testParcelable() {

        final TimeDuration timeout = TimeDuration.seconds(10);
        final MyParcelable p = new MyParcelable(33, -17);
        final Routine<MyParcelable, MyParcelable> routine =
                JRoutine.onService(getActivity(), ClassToken.tokenOf(MyParcelableInvocation.class))
                        .dispatchingOn(Looper.getMainLooper())
                        .buildRoutine();
        assertThat(routine.callAsync(p).afterMax(timeout).readNext()).isEqualTo(p);
    }

    public void testReadTimeout() {

        final ClassToken<AndroidPassingInvocation<String>> classToken =
                new ClassToken<AndroidPassingInvocation<String>>() {};
        final RoutineConfiguration configuration1 = builder().withReadTimeout(millis(10))
                                                             .onReadTimeout(TimeoutAction.EXIT)
                                                             .buildConfiguration();
        final Routine<String, String> routine1 = JRoutine.onService(getActivity(), classToken)
                                                         .withConfiguration(configuration1)
                                                         .buildRoutine();

        assertThat(routine1.callAsync("test1").readAll()).isEmpty();

        final RoutineConfiguration configuration2 = builder().withReadTimeout(millis(10))
                                                             .onReadTimeout(TimeoutAction.ABORT)
                                                             .buildConfiguration();
        final Routine<String, String> routine2 = JRoutine.onService(getActivity(), classToken)
                                                         .withConfiguration(configuration2)
                                                         .buildRoutine();

        try {

            routine2.callAsync("test2").readAll();

            fail();

        } catch (final AbortException ignored) {

        }

        final RoutineConfiguration configuration3 = builder().withReadTimeout(millis(10))
                                                             .onReadTimeout(TimeoutAction.DEADLOCK)
                                                             .buildConfiguration();
        final Routine<String, String> routine3 = JRoutine.onService(getActivity(), classToken)
                                                         .withConfiguration(configuration3)
                                                         .buildRoutine();

        try {

            routine3.callAsync("test3").readAll();

            fail();

        } catch (final ReadDeadlockException ignored) {

        }
    }

    public void testService() {

        final TimeDuration timeout = TimeDuration.seconds(10);
        final Routine<String, String> routine =
                JRoutine.onService(getActivity(), ClassToken.tokenOf(StringPassingInvocation.class))
                        .dispatchingOn(Looper.getMainLooper())
                        .withServiceClass(TestService.class)
                        .buildRoutine();
        assertThat(
                routine.callSync("1", "2", "3", "4", "5").afterMax(timeout).readAll()).containsOnly(
                "1", "2", "3", "4", "5");
        assertThat(routine.callAsync("1", "2", "3", "4", "5")
                          .afterMax(timeout)
                          .readAll()).containsOnly("1", "2", "3", "4", "5");
        assertThat(routine.callParallel("1", "2", "3", "4", "5")
                          .afterMax(timeout)
                          .readAll()).containsOnly("1", "2", "3", "4", "5");
    }

    private static class Abort extends AndroidTemplateInvocation<Data, Data> {

        @Override
        public void onInput(final Data d, @Nonnull final ResultChannel<Data> result) {

            try {

                Thread.sleep(500);

            } catch (final InterruptedException e) {

                throw InvocationInterruptedException.interrupt(e);
            }

            result.abort(new IllegalStateException("test"));
        }
    }

    private static class Data implements Parcelable {

        public static final Creator<Data> CREATOR = new Creator<Data>() {


            @Override
            public Data createFromParcel(final Parcel source) {

                return new Data();
            }

            @Override
            public Data[] newArray(final int size) {

                return new Data[size];
            }
        };

        @Override
        public int describeContents() {

            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {

        }
    }

    private static class Delay extends AndroidTemplateInvocation<Data, Data> {

        @Override
        public void onInput(final Data d, @Nonnull final ResultChannel<Data> result) {

            result.after(TimeDuration.millis(500)).pass(d);
        }
    }

    private static class MyParcelable implements Parcelable {

        public static final Creator<MyParcelable> CREATOR = new Creator<MyParcelable>() {

            @Override
            public MyParcelable createFromParcel(final Parcel source) {

                final int x = source.readInt();
                final int y = source.readInt();
                return new MyParcelable(x, y);
            }

            @Override
            public MyParcelable[] newArray(final int size) {

                return new MyParcelable[0];
            }
        };

        private final int mX;

        private final int mY;

        private MyParcelable(final int x, final int y) {

            mX = x;
            mY = y;
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if (!(o instanceof MyParcelable)) {

                return false;
            }

            final MyParcelable that = (MyParcelable) o;

            return mX == that.mX && mY == that.mY;
        }

        @Override
        public int hashCode() {

            int result = mX;
            result = 31 * result + mY;
            return result;
        }

        @Override
        public int describeContents() {

            return 0;
        }


        @Override
        public void writeToParcel(final Parcel dest, final int flags) {

            dest.writeInt(mX);
            dest.writeInt(mY);
        }
    }

    private static class MyParcelableInvocation extends AndroidPassingInvocation<MyParcelable> {

    }

    private static class StringPassingInvocation extends AndroidPassingInvocation<String> {

    }

    private static class StringSingleCallInvocation
            extends AndroidSingleCallInvocation<String, String> {

        @Override
        public void onCall(@Nonnull final List<? extends String> strings,
                @Nonnull final ResultChannel<String> result) {

            result.pass(strings);
        }
    }
}
