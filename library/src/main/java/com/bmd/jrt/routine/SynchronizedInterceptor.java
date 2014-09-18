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

import com.bmd.jrt.channel.ResultInterceptor;

/**
 * Created by davide on 9/18/14.
 */
class SynchronizedInterceptor<RESULT> implements ResultInterceptor<RESULT> {

    private final ResultInterceptor<RESULT> mInterceptor;

    private final Object mMutex = new Object();

    public SynchronizedInterceptor(final ResultInterceptor<RESULT> wrapped) {

        mInterceptor = wrapped;
    }

    @Override
    public void onReset(final Throwable throwable) {

        synchronized (mMutex) {

            mInterceptor.onReset(throwable);
        }
    }

    @Override
    public void onResult(final RESULT result) {

        synchronized (mMutex) {

            mInterceptor.onResult(result);
        }
    }

    @Override
    public void onReturn() {

        synchronized (mMutex) {

            mInterceptor.onReturn();
        }
    }
}