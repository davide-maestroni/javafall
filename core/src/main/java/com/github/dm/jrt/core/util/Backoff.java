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

package com.github.dm.jrt.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Backoff utility class.
 * <br>
 * This class is useful to build a backoff policy, returning a delay in milliseconds to apply when
 * a counter exceeds a specified limit.
 * <br>
 * The implementation should be stateless and rely only to the passed excess count to compute the
 * delay.
 * <p>
 * Created by davide-maestroni on 05/09/2016.
 */
public abstract class Backoff {

    private static final ConstantBackoff sZeroBackoff = new ConstantBackoff(0);

    /**
     * Returns a constant backoff.
     * <br>
     * The backoff will always return the specified delay.
     *
     * @param value the delay value.
     * @param unit  the delay unit.
     * @return the backoff instance.
     * @throws java.lang.IllegalArgumentException if the delay is negative.
     */
    @NotNull
    public static Backoff constantDelay(final long value, @NotNull final TimeUnit unit) {

        return new ConstantBackoff(unit.toMillis(value));
    }

    /**
     * Returns a constant backoff.
     * <br>
     * The backoff will always return the specified delay.
     *
     * @param delay the delay.
     * @return the backoff instance.
     */
    @NotNull
    public static Backoff constantDelay(@NotNull final UnitDuration delay) {

        return new ConstantBackoff(delay.toMillis());
    }

    /**
     * Returns an exponentially increasing backoff.
     * <br>
     * The backoff will return a delay computed as: {@code delay * 2^(count - 1)}.
     *
     * @param value the delay value.
     * @param unit  the delay unit.
     * @return the backoff instance.
     * @throws java.lang.IllegalArgumentException if the delay is negative.
     */
    @NotNull
    public static Backoff exponentialDelay(final long value, @NotNull final TimeUnit unit) {

        return new ExponentialBackoff(unit.toMillis(value));
    }

    /**
     * Returns an exponentially increasing backoff.
     * <br>
     * The backoff will return a delay computed as: {@code delay * 2^(count - 1)}.
     *
     * @param delay the delay.
     * @return the backoff instance.
     */
    @NotNull
    public static Backoff exponentialDelay(@NotNull final UnitDuration delay) {

        return new ExponentialBackoff(delay.toMillis());
    }

    /**
     * Returns a de-correlated jitter backoff.
     * <br>
     * The backoff will return a delay computed by taking in consideration the previous jitter
     * delay.
     * <p>
     * Note that this particular implementation TODO
     *
     * @param value the delay value.
     * @param unit  the delay unit.
     * @return the backoff instance.
     * @throws java.lang.IllegalArgumentException if the delay is negative.
     */
    @NotNull
    public static Backoff jitterDelay(final long value, @NotNull final TimeUnit unit) {

        return new DecorrelatedJitterBackoff(unit.toMillis(value));
    }

    /**
     * Returns a de-correlated jitter backoff.
     * <br>
     * The backoff will return a delay computed by taking in consideration the previous jitter
     * delay.
     *
     * @param delay the delay.
     * @return the backoff instance.
     */
    @NotNull
    public static Backoff jitterDelay(@NotNull final UnitDuration delay) {

        return new DecorrelatedJitterBackoff(delay.toMillis());
    }

    /**
     * Returns an linearly increasing backoff.
     * <br>
     * The backoff will return a delay computed as: {@code delay * count}.
     *
     * @param value the delay value.
     * @param unit  the delay unit.
     * @return the backoff instance.
     * @throws java.lang.IllegalArgumentException if the delay is negative.
     */
    @NotNull
    public static Backoff linearDelay(final long value, @NotNull final TimeUnit unit) {

        return new LinearBackoff(unit.toMillis(value));
    }

    /**
     * Returns an linearly increasing backoff.
     * <br>
     * The backoff will return a delay computed as: {@code delay * count}.
     *
     * @param delay the delay.
     * @return the backoff instance.
     */
    @NotNull
    public static Backoff linearDelay(@NotNull final UnitDuration delay) {

        return new LinearBackoff(delay.toMillis());
    }

    /**
     * Returns the zero delay backoff instance.
     * <br>
     * The backoff will always return a delay of 0.
     *
     * @return the backoff instance.
     */
    @NotNull
    public static Backoff zeroDelay() {

        return sZeroBackoff;
    }

    /**
     * Caps this backoff policy to the specified maximum delay.
     *
     * @param value the delay value.
     * @param unit  the delay unit.
     * @return the capped backoff policy.
     * @throws java.lang.IllegalArgumentException if the delay is negative.
     */
    @NotNull
    public Backoff cappedTo(final long value, @NotNull final TimeUnit unit) {

        return new CappedBackoff(this, unit.toMillis(value));
    }

    /**
     * Caps this backoff policy to the specified maximum delay.
     *
     * @param delay the maximum delay.
     * @return the capped backoff policy.
     * @throws java.lang.IllegalArgumentException if the delay is negative.
     */
    @NotNull
    public Backoff cappedTo(@NotNull final UnitDuration delay) {

        return new CappedBackoff(this, delay.toMillis());
    }

    /**
     * Gets the delay for the specified count.
     *
     * @param count the count exceeding the limit (it is expected to be positive).
     * @return the delay in milliseconds.
     */
    public abstract long getDelay(int count);

    /**
     * Adds jitter to this backoff policy.
     *
     * @param percentage a floating number between 0 and 1 indicating the percentage of delay to
     *                   randomize.
     * @return the backoff policy with jitter.
     * @throws java.lang.IllegalArgumentException if the percentage is outside the [0, 1] range.
     */
    @NotNull
    public Backoff withJitter(final float percentage) {

        return new JitterBackoff(this, percentage);
    }

    /**
     * Constant backoff policy.
     */
    public static class ConstantBackoff extends Backoff {

        private final long mDelay;

        /**
         * Constructor.
         *
         * @param delayMillis the delay in milliseconds.
         * @throws java.lang.IllegalArgumentException if the delay is negative.
         */
        private ConstantBackoff(final long delayMillis) {

            mDelay = ConstantConditions.notNegative("backoff delay", delayMillis);
        }

        @Override
        public long getDelay(final int count) {

            return mDelay;
        }
    }

    /**
     * Capped delay backoff policy.
     */
    private static class CappedBackoff extends Backoff {

        private final Backoff mBackoff;

        private final long mDelay;

        /**
         * Constructor.
         *
         * @param wrapped     the wrapped backoff instance.
         * @param delayMillis the maximum delay in milliseconds.
         */
        private CappedBackoff(@NotNull final Backoff wrapped, final long delayMillis) {

            mBackoff = wrapped;
            mDelay = ConstantConditions.notNegative("backoff delay", delayMillis);
        }

        @Override
        public long getDelay(final int count) {

            return Math.min(mBackoff.getDelay(count), mDelay);
        }
    }

    /**
     * De-correlated jitter backoff.
     */
    private static class DecorrelatedJitterBackoff extends Backoff {

        private final long mDelay;

        private final Random mRandom = new Random();

        private long mLast;

        /**
         * Constructor.
         *
         * @param delayMillis the delay in milliseconds.
         */
        private DecorrelatedJitterBackoff(final long delayMillis) {

            mDelay = ConstantConditions.notNegative("backoff delay", delayMillis);
            mLast = delayMillis;
        }

        @Override
        public long getDelay(final int count) {

            final long delay = mDelay;
            mLast = delay + Math.round(((mLast * 3) - delay) * mRandom.nextDouble());
            return mLast;
        }
    }

    /**
     * Exponentially increasing backoff policy.
     */
    private static class ExponentialBackoff extends Backoff {

        private final long mDelay;

        /**
         * Constructor.
         *
         * @param delayMillis the delay in milliseconds.
         */
        private ExponentialBackoff(final long delayMillis) {

            mDelay = ConstantConditions.notNegative("backoff delay", delayMillis);
        }

        @Override
        public long getDelay(final int count) {

            return mDelay << (count - 1);
        }
    }

    /**
     * Backoff policy with jitter addition.
     */
    private static class JitterBackoff extends Backoff {

        private final Backoff mBackoff;

        private final float mPercent;

        private final Random mRandom = new Random();

        /**
         * Constructor.
         *
         * @param wrapped    the wrapped backoff instance.
         * @param percentage the percentage of delay to randomize.
         * @throws java.lang.IllegalArgumentException if the percentage is outside the [0, 1] range.
         */
        private JitterBackoff(@NotNull final Backoff wrapped, final float percentage) {

            if ((percentage < 0) || (percentage > 1)) {
                throw new IllegalArgumentException(
                        "the jitter percentage must be between 0 and 1, but is: " + percentage);
            }

            mBackoff = wrapped;
            mPercent = percentage;
        }

        @Override
        public long getDelay(final int count) {

            final float percent = mPercent;
            final long delay = mBackoff.getDelay(count);
            return Math.round((delay * percent * mRandom.nextDouble()) + (delay * (1 - percent)));
        }
    }

    /**
     * Linearly increasing backoff policy.
     */
    private static class LinearBackoff extends Backoff {

        private final long mDelay;

        /**
         * Constructor.
         *
         * @param delayMillis the delay in milliseconds.
         */
        private LinearBackoff(final long delayMillis) {

            mDelay = ConstantConditions.notNegative("backoff delay", delayMillis);
        }

        @Override
        public long getDelay(final int count) {

            return mDelay * count;
        }
    }
}
