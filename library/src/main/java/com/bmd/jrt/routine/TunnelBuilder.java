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

import com.bmd.jrt.builder.RoutineChannelBuilder.DataOrder;
import com.bmd.jrt.builder.RoutineConfiguration;
import com.bmd.jrt.builder.RoutineConfigurationBuilder;
import com.bmd.jrt.channel.Tunnel;
import com.bmd.jrt.log.Log;
import com.bmd.jrt.log.Log.LogLevel;
import com.bmd.jrt.runner.Runner;
import com.bmd.jrt.time.TimeDuration;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class implementing a builder of tunnel objects.
 * <p/>
 * Created by davide on 10/25/14.
 */
public class TunnelBuilder {

    private final RoutineConfigurationBuilder mBuilder;

    /**
     * Avoid direct instantiation.
     */
    TunnelBuilder() {

        mBuilder = new RoutineConfigurationBuilder();
    }

    /**
     * Applies the specified configuration to this builder.<br/>
     * Note that the configuration options not supported by this builder methods will be ignored.
     *
     * @param configuration the configuration.
     * @return this builder.
     * @throws java.lang.NullPointerException if the specified configuration is null.
     */
    @Nonnull
    public TunnelBuilder apply(@Nonnull final RoutineConfiguration configuration) {

        mBuilder.apply(configuration);
        return this;
    }

    /**
     * Sets the timeout for the tunnel to have room for additional data.<br/>
     * Note that the output buffer timeout set through the <code>apply()</code> method will be used
     * to fill this value.
     *
     * @param timeout  the timeout.
     * @param timeUnit the timeout time unit.
     * @return this builder.
     * @throws java.lang.IllegalArgumentException if the specified timeout is negative.
     * @throws java.lang.NullPointerException     if the specified time unit is null.
     */
    @Nonnull
    public TunnelBuilder bufferTimeout(final long timeout, @Nonnull final TimeUnit timeUnit) {

        mBuilder.outputTimeout(timeout, timeUnit);
        return this;
    }

    /**
     * Sets the timeout for the tunnel to have room for additional data. A null value means
     * that it is up to the framework to chose a default.<br/>
     * Note that the output buffer timeout set through the <code>apply()</code> method will be used
     * to fill this value.
     *
     * @param timeout the timeout.
     * @return this builder.
     */
    @Nonnull
    public TunnelBuilder bufferTimeout(@Nonnull final TimeDuration timeout) {

        mBuilder.outputTimeout(timeout);
        return this;
    }

    /**
     * Builds and returns the tunnel instance.
     *
     * @return the newly created tunnel.
     */
    @Nonnull
    public <T> Tunnel<T> buildTunnel() {

        return new DefaultTunnel<T>(mBuilder.buildConfiguration());
    }

    /**
     * Sets the order in which data are collected from the tunnel.<br/>
     * Note that the output order set through the <code>apply()</code> method will be used to fill
     * this value.
     *
     * @param order the order type.
     * @return this builder.
     * @throws java.lang.NullPointerException if the specified order type is null.
     */
    @Nonnull
    public TunnelBuilder dataOrder(@Nonnull final DataOrder order) {

        mBuilder.outputOrder(order);
        return this;
    }

    /**
     * Sets the runner instance used to schedule delayed inputs. A null value means that it is up to
     * the framework to chose a default instance.<br/>
     * Note that the async runner set through the <code>apply()</code> method will be used to fill
     * this value.
     *
     * @param runner the runner instance.
     * @return this builder.
     */
    @Nonnull
    public TunnelBuilder delayRunner(@Nonnull final Runner runner) {

        mBuilder.runBy(runner);
        return this;
    }

    /**
     * Sets the log level.
     *
     * @param level the log level.
     * @return this builder.
     * @throws java.lang.NullPointerException if the log level is null.
     */
    @Nonnull
    public TunnelBuilder logLevel(@Nonnull final LogLevel level) {

        mBuilder.logLevel(level);
        return this;
    }

    /**
     * Sets the log instance. A null value means that it is up to the framework to chose a default
     * implementation.
     *
     * @param log the log instance.
     * @return this builder.
     */
    @Nonnull
    public TunnelBuilder loggedWith(@Nullable final Log log) {

        mBuilder.loggedWith(log);
        return this;
    }

    /**
     * Sets the maximum number of data that the tunnel can retain before they are consumed. A
     * {@link RoutineConfiguration#DEFAULT} value means that it is up to the framework to chose a
     * default size.<br/>
     * Note that the max output buffer size set through the <code>apply()</code> method will be used
     * to fill this value.
     *
     * @param maxBufferSize the maximum size.
     * @return this builder.
     * @throws java.lang.IllegalArgumentException if the number is less than 1.
     */
    @Nonnull
    public TunnelBuilder maxSize(final int maxBufferSize) {

        mBuilder.outputSize(maxBufferSize);
        return this;
    }
}
