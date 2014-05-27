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
package com.bmd.wtf.bdr;

import com.bmd.wtf.src.Floodgate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Instances of this class are used internally by {@link com.bmd.wtf.bdr.DataPool}s to collect
 * data and objects, and then discharge them through the thread pump.
 * <p/>
 * Before invoking any method the caller must first open the gate by calling {@link #open()} and
 * then close it through the method {@link #close()}. Note also that the two calls must happen in
 * the same thread.
 * <p/>
 * Created by davide on 3/2/14.
 *
 * @param <IN>  The input data type.
 * @param <OUT> The output data type.
 */
class DataFloodgate<IN, OUT> implements Floodgate<IN, OUT> {

    private static final ThreadLocal<DataPump> sPump = new ThreadLocal<DataPump>() {

        @Override
        protected DataPump initialValue() {

            return new DataPump();
        }
    };

    private final ReentrantLock mLock = new ReentrantLock();

    private final DataPool<IN, OUT> mPool;

    private volatile DataPump mPump;

    public DataFloodgate(final DataPool<IN, OUT> pool) {

        mPool = pool;
    }

    @Override
    public Floodgate<IN, OUT> discharge(final OUT drop) {

        failIfClosed();

        mPump.discharge(mPool, drop);

        return this;
    }

    @Override
    public Floodgate<IN, OUT> discharge(final OUT... drops) {

        failIfClosed();

        if ((drops == null) || (drops.length == 0)) {

            return this;
        }

        if (drops.length == 1) {

            mPump.discharge(mPool, drops[0]);

        } else {

            mPump.discharge(mPool, drops);
        }

        return this;
    }

    @Override
    public Floodgate<IN, OUT> discharge(final Iterable<? extends OUT> drops) {

        failIfClosed();

        if (drops == null) {

            return this;
        }

        mPump.discharge(mPool, drops);

        return this;
    }

    @Override
    public Floodgate<IN, OUT> dischargeAfter(final long delay, final TimeUnit timeUnit,
            final OUT drop) {

        failIfClosed();

        mPump.dischargeAfter(mPool, delay, timeUnit, drop);

        return this;
    }

    @Override
    public Floodgate<IN, OUT> dischargeAfter(final long delay, final TimeUnit timeUnit,
            final OUT... drops) {

        failIfClosed();

        if ((drops == null) || (drops.length == 0)) {

            return this;
        }

        if (drops.length == 1) {

            mPump.dischargeAfter(mPool, delay, timeUnit, drops[0]);

        } else {

            mPump.dischargeAfter(mPool, delay, timeUnit, drops);
        }

        return this;
    }

    @Override
    public Floodgate<IN, OUT> dischargeAfter(final long delay, final TimeUnit timeUnit,
            final Iterable<? extends OUT> drops) {

        failIfClosed();

        if (drops == null) {

            return this;
        }

        mPump.dischargeAfter(mPool, delay, timeUnit, drops);

        return this;
    }

    @Override
    public Floodgate<IN, OUT> drop(final Object debris) {

        failIfClosed();

        mPump.drop(mPool, debris);

        return this;
    }

    @Override
    public Floodgate<IN, OUT> dropAfter(final long delay, final TimeUnit timeUnit,
            final Object debris) {

        failIfClosed();

        mPump.dropAfter(mPool, delay, timeUnit, debris);

        return this;
    }

    @Override
    public void drain() {

        final DataPool<IN, OUT> pool = mPool;

        pool.inputStreams.clear();
        pool.outputStreams.clear();
    }

    @Override
    public Floodgate<IN, OUT> rechargeAfter(final long delay, final TimeUnit timeUnit,
            final IN drop) {

        failIfClosed();

        mPump.rechargeAfter(mPool, delay, timeUnit, drop);

        return this;
    }

    @Override
    public Floodgate<IN, OUT> rechargeAfter(final long delay, final TimeUnit timeUnit,
            final IN... drops) {

        failIfClosed();

        if ((drops == null) || (drops.length == 0)) {

            return this;
        }

        if (drops.length == 1) {

            mPump.rechargeAfter(mPool, delay, timeUnit, drops[0]);

        } else {

            mPump.rechargeAfter(mPool, delay, timeUnit, drops);
        }

        return this;
    }

    @Override
    public Floodgate<IN, OUT> rechargeAfter(final long delay, final TimeUnit timeUnit,
            final Iterable<? extends IN> drops) {

        failIfClosed();

        if (drops == null) {

            return this;
        }

        mPump.rechargeAfter(mPool, delay, timeUnit, drops);

        return this;
    }

    @Override
    public Floodgate<IN, OUT> redropAfter(final long delay, final TimeUnit timeUnit,
            final Object debris) {

        failIfClosed();

        mPump.redropAfter(mPool, delay, timeUnit, debris);

        return this;
    }

    @Override
    public void exhaust() {

        final DataPool<IN, OUT> pool = mPool;

        for (final Stream<?, IN, OUT> stream : pool.outputStreams) {

            stream.drain(true);
        }

        for (final Stream<?, ?, IN> stream : pool.inputStreams) {

            stream.drain(false);
        }
    }

    @Override
    public void flush() {

        failIfClosed();

        mPump.flush(mPool);
    }

    void close() {

        if (!mLock.isHeldByCurrentThread()) {

            throw new UnauthorizedDischargeException(
                    "an open floodgate cannot be closed in a different thread");
        }

        final DataPump pump = mPump;

        mPump = null;

        mLock.unlock();

        pump.run();
    }

    void open() {

        mLock.lock();

        mPump = sPump.get();
    }

    private void failIfClosed() {

        mLock.lock();

        try {

            if (mPump == null) {

                throw new UnauthorizedDischargeException(
                        "cannot discharge into a closed floodgate");
            }

        } finally {

            mLock.unlock();
        }
    }
}