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
package com.bmd.wtf.xtr.arr;

import com.bmd.wtf.Waterfall;
import com.bmd.wtf.bdr.DuplicateDamException;
import com.bmd.wtf.bdr.Stream;
import com.bmd.wtf.crr.Current;
import com.bmd.wtf.dam.Dam;
import com.bmd.wtf.src.Spring;

import java.util.ArrayList;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This class implements an array of parallel {@link com.bmd.wtf.bdr.Stream}s.
 * <p/>
 * Created by davide on 4/5/14.
 *
 * @param <SOURCE> The spring data type.
 * @param <IN>     The input data type of the upstream pool.
 * @param <OUT>    The transported data type, that is the output data type of the upstream pool.
 */
public class StreamArray<SOURCE, IN, OUT> {

    private static final WeakHashMap<Barrage<?, ?>, Void> sBarrages =
            new WeakHashMap<Barrage<?, ?>, Void>();

    private final boolean mIsSingleStream;

    private final ArrayList<Stream<SOURCE, IN, OUT>> mStreams;

    /**
     * Creates an array of streams where every stream is the source one.
     *
     * @param stream       The source stream instance.
     * @param streamNumber The number of parallel streams.
     */
    StreamArray(final Stream<SOURCE, IN, OUT> stream, final int streamNumber) {

        if (stream == null) {

            throw new IllegalArgumentException("the input stream cannot be null");
        }

        if (streamNumber <= 0) {

            throw new IllegalArgumentException("the number of streams cannot be negative");
        }

        final ArrayList<Stream<SOURCE, IN, OUT>> streams =
                new ArrayList<Stream<SOURCE, IN, OUT>>(streamNumber);

        for (int i = 0; i < streamNumber; ++i) {

            streams.add(stream);
        }

        mStreams = streams;
        mIsSingleStream = true;
    }

    /**
     * Creates and array of streams from the specified ones.
     *
     * @param streams The streams composing the array.
     */
    StreamArray(final Set<Stream<SOURCE, IN, OUT>> streams) {

        this(new ArrayList<Stream<SOURCE, IN, OUT>>(streams));
    }

    /**
     * Private constructor.
     *
     * @param streams The streams composing the array.
     */
    private StreamArray(final ArrayList<Stream<SOURCE, IN, OUT>> streams) {

        if (streams.isEmpty() || streams.contains(null)) {

            throw new IllegalArgumentException("invalid array of input streams");
        }

        mStreams = streams;
        mIsSingleStream = false;
    }

    /**
     * Balances the data flows running through this stream array.
     * <p/>
     * The balancer is used to indicate into which one of the streams of the array to propagate
     * the flow of data. If -1 is returned from the balancer, no further propagation will happen.
     * If the total stream count is returned instead, the data or object will be propagated to
     * all the streams. All the other out-of-range values will be ignored.
     *
     * @param balancer The array balancer.
     * @return A new stream whose flows are balanced by the specified balancer.
     */
    public StreamArray<SOURCE, OUT, OUT> thenBalancedBy(final ArrayBalancer<OUT> balancer) {

        if (balancer == null) {

            throw new IllegalArgumentException("the array balancer cannot be null");
        }

        final ArrayList<Stream<SOURCE, IN, OUT>> streams = mStreams;

        final int streamsCount = streams.size();

        final ArrayList<Spring<OUT>> springs = new ArrayList<Spring<OUT>>(streamsCount);

        final SpringBalancerDam<OUT> balancerDam =
                new SpringBalancerDam<OUT>(balancer, streamsCount, springs);

        final Stream<SOURCE, OUT, OUT> balancedStream;

        if (mIsSingleStream) {

            balancedStream = streams.get(0).thenFlowingThrough(balancerDam);

        } else {

            balancedStream = thenMergingThrough(balancerDam);
        }

        final ArrayList<Stream<SOURCE, OUT, OUT>> outStreams =
                new ArrayList<Stream<SOURCE, OUT, OUT>>(streamsCount);

        for (int i = 0; i < streamsCount; ++i) {

            final Stream<OUT, OUT, OUT> outStream =
                    Waterfall.fallingFrom(new DebrisBalancerDam<OUT>(balancer, i));

            springs.add(outStream.backToSource());

            outStreams.add(balancedStream.thenFeeding(outStream));
        }

        return new StreamArray<SOURCE, OUT, OUT>(outStreams);
    }

    /**
     * Makes this stream array run into the currents created by the specified factory.
     *
     * @param factory The current factory.
     * @return A new stream running into the created currents.
     */
    public StreamArray<SOURCE, IN, OUT> thenFlowingInto(final CurrentFactory factory) {

        final ArrayList<Stream<SOURCE, IN, OUT>> streams =
                new ArrayList<Stream<SOURCE, IN, OUT>>(mStreams.size());

        int n = 0;

        for (final Stream<SOURCE, IN, OUT> stream : mStreams) {

            streams.add(stream.thenFlowingInto(factory.createForStream(n++)));
        }

        return new StreamArray<SOURCE, IN, OUT>(streams);
    }

    /**
     * Makes this stream array flow through the dams created by the specified factory.
     *
     * @param factory The dam factory.
     * @param <NOUT>  The output data type.
     * @return A new stream array flowing from the dams.
     */
    public <NOUT> StreamArray<SOURCE, OUT, NOUT> thenFlowingThrough(
            final DamFactory<OUT, NOUT> factory) {

        final ArrayList<Stream<SOURCE, OUT, NOUT>> streams =
                new ArrayList<Stream<SOURCE, OUT, NOUT>>(mStreams.size());

        int n = 0;

        for (final Stream<SOURCE, IN, OUT> stream : mStreams) {

            streams.add(stream.thenFlowingThrough(factory.createForStream(n++)));
        }

        return new StreamArray<SOURCE, OUT, NOUT>(streams);
    }

    /**
     * Makes this stream array flow through the specified barrage.
     *
     * @param barrage The barrage instance.
     * @param <NOUT>  The output data type.
     * @return A new stream array flowing from the barrage.
     */
    public <NOUT> StreamArray<SOURCE, OUT, NOUT> thenFlowingThrough(
            final Barrage<OUT, NOUT> barrage) {

        if (barrage == null) {

            throw new IllegalArgumentException("the output barrage cannot be null");
        }

        if (sBarrages.containsKey(barrage)) {

            throw new DuplicateDamException(
                    "the waterfall already contains the barrage: " + barrage);
        }

        sBarrages.put(barrage, null);

        final ArrayList<Stream<SOURCE, OUT, NOUT>> streams =
                new ArrayList<Stream<SOURCE, OUT, NOUT>>(mStreams.size());

        int n = 0;

        final Object mutex = new Object();

        for (final Stream<SOURCE, IN, OUT> stream : mStreams) {

            streams.add(stream.thenFlowingThrough(new BarrageDam<OUT, NOUT>(mutex, n++, barrage)));
        }

        return new StreamArray<SOURCE, OUT, NOUT>(streams);
    }

    /**
     * Merges this array into one stream.
     *
     * @return The merged stream.
     */
    public Stream<SOURCE, OUT, OUT> thenMerging() {

        final ArrayList<Stream<SOURCE, IN, OUT>> streams = mStreams;

        return streams.get(0).thenMerging(streams);
    }

    /**
     * Merges this array into one stream flowing into the specified current.
     *
     * @param current The current instance.
     * @return The merged stream.
     */
    public Stream<SOURCE, OUT, OUT> thenMergingInto(final Current current) {

        final ArrayList<Stream<SOURCE, IN, OUT>> streams = new ArrayList<Stream<SOURCE, IN, OUT>>();

        for (final Stream<SOURCE, IN, OUT> stream : mStreams) {

            streams.add(stream.thenFlowingInto(current));
        }

        return streams.get(0).thenMerging(streams);
    }

    /**
     * Merges this array into one stream flowing from the specified dam.
     *
     * @param dam    The dam instance.
     * @param <NOUT> The output data type.
     * @return The merged stream.
     */
    public <NOUT> Stream<SOURCE, OUT, NOUT> thenMergingThrough(final Dam<OUT, NOUT> dam) {

        return thenMerging().thenFlowingThrough(dam);
    }
}