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

import android.os.Handler;
import android.os.Looper;

import com.bmd.jrt.runner.Invocation;
import com.bmd.jrt.runner.Runner;
import com.bmd.jrt.runner.Runners;

import java.util.concurrent.TimeUnit;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Implementation of a runner employing the Android {@link android.os.Looper} queue to execute
 * the routine invocations.
 * <p/>
 * Created by davide on 9/28/14.
 */
class LooperRunner implements Runner {

    private final Handler mHandler;

    private final Runner mQueuedRunner;

    private final Thread mThread;

    /**
     * Constructor.
     *
     * @param looper the looper to employ.
     * @throws NullPointerException if the specified looper is null.
     */
    LooperRunner(@NonNull final Looper looper) {

        mThread = looper.getThread();
        mHandler = new Handler(looper);
        mQueuedRunner = Runners.queued();
    }

    @Override
    public void run(@NonNull final Invocation invocation, final long delay,
            @NonNull final TimeUnit timeUnit) {

        if (Thread.currentThread().equals(mThread)) {

            mQueuedRunner.run(invocation, delay, timeUnit);

        } else {

            final InvocationRunnable runnable = new InvocationRunnable(invocation);

            if (delay > 0) {

                mHandler.postDelayed(runnable, timeUnit.toMillis(delay));

            } else {

                mHandler.post(runnable);
            }
        }
    }

    @Override
    public void runAbort(@NonNull final Invocation invocation) {

        if (Thread.currentThread().equals(mThread)) {

            mQueuedRunner.runAbort(invocation);

        } else {

            mHandler.post(new AbortRunnable(invocation));
        }
    }

    /**
     * Runnable used to abort an invocation.
     */
    private static class AbortRunnable implements Runnable {

        private final Invocation mInvocation;

        /**
         * Constructor.
         *
         * @param invocation the invocation instance.
         */
        private AbortRunnable(@NonNull final Invocation invocation) {

            mInvocation = invocation;
        }

        @Override
        public void run() {

            mInvocation.abort();
        }
    }

    /**
     * Runnable used to run an invocation.
     */
    private static class InvocationRunnable implements Runnable {

        private final Invocation mInvocation;

        /**
         * Constructor.
         *
         * @param invocation the invocation instance.
         */
        private InvocationRunnable(@NonNull final Invocation invocation) {

            mInvocation = invocation;
        }

        @Override
        public void run() {

            mInvocation.run();
        }
    }
}