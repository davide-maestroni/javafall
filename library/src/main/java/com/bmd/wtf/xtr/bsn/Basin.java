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
package com.bmd.wtf.xtr.bsn;

import com.bmd.wtf.bdr.Stream;
import com.bmd.wtf.dam.CollectorDam;
import com.bmd.wtf.src.Spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A Basin is used to feed a {@link com.bmd.wtf.bdr.Stream} through its source and then collect
 * the data flowing down through it.
 * <p/>
 * Created by davide on 3/7/14.
 *
 * @param <IN>  The input data type.
 * @param <OUT> The output data type.
 */
public class Basin<IN, OUT> {

    private final CollectorDam<OUT> mCollector;

    private final Stream<IN, OUT, OUT> mOutStream;

    private final HashSet<Spring<IN>> mSprings;

    /**
     * Constructor.
     *
     * @param springs   The springs from which data originate.
     * @param collector The collector dam instance.
     * @param outStream The output stream.
     */
    Basin(final Collection<Spring<IN>> springs, final CollectorDam<OUT> collector,
            final Stream<IN, OUT, OUT> outStream) {

        mSprings = new HashSet<Spring<IN>>(springs);
        mCollector = collector;
        mOutStream = outStream;
    }

    /**
     * Creates a new basin from the specified stream.
     *
     * @param stream The stream originating the basin.
     * @param <IN>   The input data type.
     * @param <OUT>  The output data type.
     * @return The new basin.
     */
    public static <IN, OUT> Basin<IN, OUT> collect(final Stream<IN, ?, OUT> stream) {

        final CollectorDam<OUT> dam = new CollectorDam<OUT>();

        return new Basin<IN, OUT>(Collections.singleton(stream.backToSource()), dam,
                                  stream.thenFallingThrough(dam));
    }

    /**
     * Creates a new basin from the specified streams.
     *
     * @param streams The streams originating the basin.
     * @param <IN>    The input data type.
     * @param <OUT>   The output data type.
     * @return The new basin.
     */
    public static <IN, OUT> Basin<IN, OUT> collect(final Stream<IN, ?, OUT>... streams) {

        if ((streams == null) || (streams.length == 0)) {

            throw new IllegalArgumentException(
                    "the array of streams to collect cannot be null or empty");
        }

        final ArrayList<Spring<IN>> springs = new ArrayList<Spring<IN>>(streams.length);

        for (final Stream<IN, ?, OUT> stream : streams) {

            springs.add(stream.backToSource());
        }

        final CollectorDam<OUT> dam = new CollectorDam<OUT>();

        return new Basin<IN, OUT>(springs, dam, streams[0].thenMergingThrough(dam, streams));
    }

    /**
     * Creates a new basin from the streams returned by the specified iterable.
     *
     * @param streams The iterable returning the streams originating the basin.
     * @param <IN>    The input data type.
     * @param <OUT>   The output data type.
     * @return The new basin.
     */
    public static <IN, OUT> Basin<IN, OUT> collect(
            final Iterable<? extends Stream<IN, ?, OUT>> streams) {

        if (streams == null) {

            throw new IllegalArgumentException(
                    "the collection of streams to collect cannot be null or empty");
        }

        final Stream<IN, ?, OUT> firstStream = streams.iterator().next();

        final ArrayList<Spring<IN>> springs = new ArrayList<Spring<IN>>();

        for (final Stream<IN, ?, OUT> stream : streams) {

            springs.add(stream.backToSource());
        }

        final CollectorDam<OUT> dam = new CollectorDam<OUT>();

        return new Basin<IN, OUT>(springs, dam, firstStream.thenMergingThrough(dam, streams));
    }

    /**
     * Feeds the specified basin by discharging the passed partial data drops into the originating
     * springs.
     *
     * @param basin The basin instance.
     * @param drops The data drops to discharge.
     * @return The passed basin.
     */
    public static <OUT, BASIN extends Basin<Byte, OUT>> BASIN partiallyFeed(final BASIN basin,
            final byte... drops) {

        if (drops != null) {

            for (final byte drop : drops) {

                basin.thenPartiallyFeedWith(drop);
            }
        }

        return basin;
    }

    /**
     * Feeds the specified basin by discharging the passed partial data drops into the originating
     * springs.
     *
     * @param basin The basin instance.
     * @param drops The data drops to discharge.
     * @return The passed basin.
     */
    public static <OUT, BASIN extends Basin<Character, OUT>> BASIN partiallyFeed(final BASIN basin,
            final char... drops) {

        if (drops != null) {

            for (final char drop : drops) {

                basin.thenPartiallyFeedWith(drop);
            }
        }

        return basin;
    }

    /**
     * Feeds the specified basin by discharging the passed partial data drops into the originating
     * springs.
     *
     * @param basin The basin instance.
     * @param drops The data drops to discharge.
     * @return The passed basin.
     */
    public static <OUT, BASIN extends Basin<Boolean, OUT>> BASIN partiallyFeed(final BASIN basin,
            final boolean... drops) {

        if (drops != null) {

            for (final boolean drop : drops) {

                basin.thenPartiallyFeedWith(drop);
            }
        }

        return basin;
    }

    /**
     * Feeds the specified basin by discharging the passed partial data drops into the originating
     * springs.
     *
     * @param basin The basin instance.
     * @param drops The data drops to discharge.
     * @return The passed basin.
     */
    public static <OUT, BASIN extends Basin<Short, OUT>> BASIN partiallyFeed(final BASIN basin,
            final short... drops) {

        if (drops != null) {

            for (final short drop : drops) {

                basin.thenPartiallyFeedWith(drop);
            }
        }

        return basin;
    }

    /**
     * Feeds the specified basin by discharging the passed partial data drops into the originating
     * springs.
     *
     * @param basin The basin instance.
     * @param drops The data drops to discharge.
     * @return The passed basin.
     */
    public static <OUT, BASIN extends Basin<Integer, OUT>> BASIN partiallyFeed(final BASIN basin,
            final int... drops) {

        if (drops != null) {

            for (final int drop : drops) {

                basin.thenPartiallyFeedWith(drop);
            }
        }

        return basin;
    }

    /**
     * Feeds the specified basin by discharging the passed partial data drops into the originating
     * springs.
     *
     * @param basin The basin instance.
     * @param drops The data drops to discharge.
     * @return The passed basin.
     */
    public static <OUT, BASIN extends Basin<Long, OUT>> BASIN partiallyFeed(final BASIN basin,
            final long... drops) {

        if (drops != null) {

            for (final long drop : drops) {

                basin.thenPartiallyFeedWith(drop);
            }
        }

        return basin;
    }

    /**
     * Feeds the specified basin by discharging the passed partial data drops into the originating
     * springs.
     *
     * @param basin The basin instance.
     * @param drops The data drops to discharge.
     * @return The passed basin.
     */
    public static <OUT, BASIN extends Basin<Float, OUT>> BASIN partiallyFeed(final BASIN basin,
            final float... drops) {

        if (drops != null) {

            for (final float drop : drops) {

                basin.thenPartiallyFeedWith(drop);
            }
        }

        return basin;
    }

    /**
     * Feeds the specified basin by discharging the passed partial data drops into the originating
     * springs.
     *
     * @param basin The basin instance.
     * @param drops The data drops to discharge.
     * @return The passed basin.
     */
    public static <OUT, BASIN extends Basin<Double, OUT>> BASIN partiallyFeed(final BASIN basin,
            final double... drops) {

        if (drops != null) {

            for (final double drop : drops) {

                basin.thenPartiallyFeedWith(drop);
            }
        }

        return basin;
    }

    /**
     * Collects the debris dropped through the basin.
     *
     * @return The dropped debris.
     */
    public List<Object> collectDebris() {

        return mCollector.collectDebris();
    }

    /**
     * Collects the debris dropped through the basin into the specified bucket.
     *
     * @param bucket The bucket to put the debris into.
     * @return This basin.
     */
    public Basin<IN, OUT> collectDebrisInto(final Collection<Object> bucket) {

        bucket.addAll(mCollector.collectDebris());

        return this;
    }

    /**
     * Collects the first debris dropped through the basin.
     *
     * @return The dropped debris.
     */
    public Object collectFirstDebris() {

        return mCollector.collectNextDebris();
    }

    /**
     * Collects the first data drop flown through the basin.
     *
     * @return The data drop.
     */
    public OUT collectFirstOutput() {

        return mCollector.collectNext();
    }

    /**
     * Collects the data flown through the basin.
     *
     * @return The data drops.
     */
    public List<OUT> collectOutput() {

        return mCollector.collect();
    }

    /**
     * Collects the data flown through the basin into the specified bucket.
     *
     * @param bucket The bucket to put the data into.
     * @return This basin.
     */
    public Basin<IN, OUT> collectOutputInto(final Collection<OUT> bucket) {

        bucket.addAll(mCollector.collect());

        return this;
    }

    /**
     * Drops the specified debris into the originating springs.
     *
     * @param debris The debris to drop.
     * @return This basin.
     */
    public Basin<IN, OUT> thenDrop(final Object debris) {

        for (final Spring<IN> spring : mSprings) {

            spring.drop(debris);
        }

        return this;
    }

    /**
     * Feeds this basin by discharging the specified data drops into the originating springs, then
     * flushes the spring flows.
     *
     * @param drops The data drops to discharge.
     * @return This basin.
     */
    public Basin<IN, OUT> thenFeedWith(final IN... drops) {

        return thenPartiallyFeedWith(drops).thenFlush();
    }

    /**
     * Feeds this basin by discharging the data drops returned by the specified iterable into the
     * originating springs, then flushes the spring flows.
     *
     * @param drops The iterable returning the data drops to discharge.
     * @return This basin.
     */
    public Basin<IN, OUT> thenFeedWith(final Iterable<? extends IN> drops) {

        return thenPartiallyFeedWith(drops).thenFlush();
    }

    /**
     * Feeds this basin by discharging the specified data drop into the originating springs, then
     * flushes the spring flows.
     *
     * @param drop The data drop to discharge.
     * @return This basin.
     */
    public Basin<IN, OUT> thenFeedWith(final IN drop) {

        return thenPartiallyFeedWith(drop).thenFlush();
    }

    /**
     * Flows on by returning the basin output stream.
     *
     * @return The output stream.
     */
    public Stream<IN, OUT, OUT> thenFlow() {

        return mOutStream;
    }

    /**
     * Flushes this basin by flushing the originating springs.
     *
     * @return This basin.
     */
    public Basin<IN, OUT> thenFlush() {

        for (final Spring<IN> spring : mSprings) {

            spring.flush();
        }

        return this;
    }

    /**
     * Feeds this basin by discharging the specified partial data drops into the originating
     * springs.
     *
     * @param drops The data drops to discharge.
     * @return This basin.
     */
    public Basin<IN, OUT> thenPartiallyFeedWith(final IN... drops) {

        // TODO: make test to verify data are not flushed

        for (final Spring<IN> spring : mSprings) {

            spring.discharge(drops);
        }

        return this;
    }

    /**
     * Feeds this basin by discharging the partial data drops returned by the specified iterable
     * into the originating springs.
     *
     * @param drops The iterable returning the data drops to discharge.
     * @return This basin.
     */
    public Basin<IN, OUT> thenPartiallyFeedWith(final Iterable<? extends IN> drops) {

        for (final Spring<IN> spring : mSprings) {

            spring.discharge(drops);
        }

        return this;
    }

    /**
     * Feeds this basin by discharging the specified partial data drop into the originating
     * springs.
     *
     * @param drop The data drop to discharge.
     * @return This basin.
     */
    public Basin<IN, OUT> thenPartiallyFeedWith(final IN drop) {

        for (final Spring<IN> spring : mSprings) {

            spring.discharge(drop);
        }

        return this;
    }
}