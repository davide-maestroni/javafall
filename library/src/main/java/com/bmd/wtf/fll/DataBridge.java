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
package com.bmd.wtf.fll;

import com.bmd.wtf.flw.Bridge;
import com.bmd.wtf.flw.DelayInterruptedException;
import com.bmd.wtf.gts.Gate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default implementation of a bridge.
 * <p/>
 * Created by davide on 6/13/14.
 *
 * @param <TYPE> the bridge type.
 */
class DataBridge<TYPE> implements Bridge<TYPE> {

    private final Classification<TYPE> mClassification;

    private final Condition mCondition;

    private final Gate<?, ?> mGate;

    private final ReentrantLock mLock;

    private volatile ConditionEvaluator<? super TYPE> mEvaluator;

    private volatile RuntimeException mTimeoutException;

    private volatile long mTimeoutMs;

    /**
     * Constructor.
     *
     * @param gate           the bridge gate.
     * @param classification the bridge classification.
     * @throws IllegalArgumentException if the gate or the classification are null.
     */
    public DataBridge(final BridgeGate<?, ?> gate, final Classification<TYPE> classification) {

        if (gate == null) {

            throw new IllegalArgumentException("the bridge gate cannot be null");
        }

        if (classification == null) {

            throw new IllegalArgumentException("the bridge classification cannot be null");
        }

        mClassification = classification;
        mGate = gate.gate;
        mLock = gate.lock;
        mCondition = gate.condition;
    }

    @Override
    public Bridge<TYPE> afterMax(final long maxDelay, final TimeUnit timeUnit) {

        mTimeoutMs = timeUnit.toMillis(Math.max(0, maxDelay));

        return this;
    }

    @Override
    public Bridge<TYPE> eventually() {

        mTimeoutMs = -1;

        return this;
    }

    @Override
    public Bridge<TYPE> eventuallyThrow(final RuntimeException exception) {

        mTimeoutException = exception;

        return this;
    }

    @Override
    public Bridge<TYPE> immediately() {

        mTimeoutMs = 0;

        return this;
    }

    @Override
    public <RESULT> RESULT perform(final Action<RESULT, ? super TYPE> action,
            final Object... args) {

        final ReentrantLock lock = mLock;
        lock.lock();

        try {

            final TYPE gate = mClassification.cast(mGate);

            //noinspection unchecked
            waitForCondition(gate);

            return action.doOn(gate, args);

        } finally {

            mCondition.signalAll();

            lock.unlock();
        }
    }

    @Override
    public Bridge<TYPE> when(final ConditionEvaluator<? super TYPE> evaluator) {

        mEvaluator = evaluator;

        return this;
    }

    private boolean meetsCondition(final ConditionEvaluator<? super TYPE> evaluator,
            final TYPE gate) {

        return (evaluator == null) || evaluator.isSatisfied(gate);
    }

    private void waitForCondition(final TYPE gate) {

        long currentTimeout = mTimeoutMs;

        final RuntimeException timeoutException = mTimeoutException;

        final ConditionEvaluator<? super TYPE> evaluator = mEvaluator;

        if ((currentTimeout == 0) || meetsCondition(evaluator, gate)) {

            return;
        }

        boolean isTimeout = false;

        try {

            final Condition condition = mCondition;

            final long startTime = System.currentTimeMillis();
            final long endTime = startTime + currentTimeout;

            do {

                if (currentTimeout >= 0) {

                    condition.await(currentTimeout, TimeUnit.MILLISECONDS);

                    currentTimeout = endTime - System.currentTimeMillis();

                    if (!meetsCondition(evaluator, gate) && (currentTimeout <= 0)) {

                        isTimeout = true;

                        break;
                    }

                } else {

                    condition.await();
                }

            } while (!meetsCondition(evaluator, gate));

        } catch (final InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new DelayInterruptedException(e);
        }

        if (isTimeout && (timeoutException != null)) {

            throw timeoutException;
        }
    }
}