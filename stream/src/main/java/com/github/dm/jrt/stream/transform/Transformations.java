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

package com.github.dm.jrt.stream.transform;

import com.github.dm.jrt.core.JRoutineCore;
import com.github.dm.jrt.core.builder.RoutineBuilder;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.core.common.Backoff;
import com.github.dm.jrt.core.common.BackoffBuilder;
import com.github.dm.jrt.core.common.RoutineException;
import com.github.dm.jrt.core.invocation.InvocationFactory;
import com.github.dm.jrt.core.routine.Routine;
import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.core.util.DurationMeasure;
import com.github.dm.jrt.function.lambda.Action;
import com.github.dm.jrt.function.lambda.BiConsumer;
import com.github.dm.jrt.function.lambda.BiFunction;
import com.github.dm.jrt.function.lambda.Function;
import com.github.dm.jrt.stream.builder.StreamBuilder;
import com.github.dm.jrt.stream.builder.StreamConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.core.util.DurationMeasure.indefiniteTime;
import static com.github.dm.jrt.function.Functions.decorate;

/**
 * Utility class providing several transformation functions to be applied to a routine stream.
 * <p>
 * Created by davide-maestroni on 07/06/2016.
 */
@SuppressWarnings("WeakerAccess")
public class Transformations {

  /**
   * Avoid explicit instantiation.
   */
  protected Transformations() {
    ConstantConditions.avoid();
  }

  /**
   * Returns a function adding a delay at the end of the stream, so that any data, exception or
   * completion notification will be dispatched to the next concatenated routine after the
   * specified time.
   *
   * @param delay the delay.
   * @param <IN>  the input data type.
   * @param <OUT> the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> delay(
      @NotNull final DurationMeasure delay) {
    return delay(delay.value, delay.unit);
  }

  /**
   * Returns a function adding a delay at the end of the stream, so that any data, exception or
   * completion notification will be dispatched to the next concatenated routine after the
   * specified time.
   *
   * @param delay    the delay value.
   * @param timeUnit the delay time unit.
   * @param <IN>     the input data type.
   * @param <OUT>    the output data type.
   * @return the lifting function.
   * @throws java.lang.IllegalArgumentException if the specified delay is negative.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> delay(final long delay,
      @NotNull final TimeUnit timeUnit) {
    ConstantConditions.notNull("time unit", timeUnit);
    ConstantConditions.notNegative("delay value", delay);
    return new LiftFunction<IN, OUT, IN, OUT>() {

      public Function<Channel<?, IN>, Channel<?, OUT>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).andThen(
            new BindDelay<OUT>(streamConfiguration.toChannelConfiguration(), delay, timeUnit));
      }
    };
  }

  /**
   * Returns a function adding a delay at the beginning of the stream, so that any data, exception
   * or completion notification coming from the source will be dispatched to the stream after the
   * specified time.
   *
   * @param delay the delay.
   * @param <IN>  the input data type.
   * @param <OUT> the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> lag(@NotNull final DurationMeasure delay) {
    return lag(delay.value, delay.unit);
  }

  /**
   * Returns a function adding a delay at the beginning of the stream, so that any data, exception
   * or completion notification coming from the source will be dispatched to the stream after the
   * specified time.
   *
   * @param delay    the delay value.
   * @param timeUnit the delay time unit.
   * @param <IN>     the input data type.
   * @param <OUT>    the output data type.
   * @return the lifting function.
   * @throws java.lang.IllegalArgumentException if the specified delay is negative.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> lag(final long delay,
      @NotNull final TimeUnit timeUnit) {
    ConstantConditions.notNull("time unit", timeUnit);
    ConstantConditions.notNegative("delay value", delay);
    return new LiftFunction<IN, OUT, IN, OUT>() {

      public Function<Channel<?, IN>, Channel<?, OUT>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).compose(
            new BindDelay<IN>(streamConfiguration.toChannelConfiguration(), delay, timeUnit));
      }
    };
  }

  /**
   * Returns a function splitting the outputs produced by the stream, so that each subset will be
   * processed by a different routine invocation.
   * <br>
   * Each output will be assigned to a specific invocation based on the load of the available
   * invocations.
   *
   * @param invocationCount the number of processing invocations.
   * @param factory         the invocation factory.
   * @param <IN>            the input data type.
   * @param <OUT>           the output data type.
   * @param <AFTER>         the new output type.
   * @return the lifting function.
   * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT, AFTER> LiftFunction<IN, OUT, IN, AFTER> parallel(
      final int invocationCount,
      @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {
    ConstantConditions.notNull("invocation factory", factory);
    return new LiftFunction<IN, OUT, IN, AFTER>() {

      public Function<Channel<?, IN>, Channel<?, AFTER>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).andThen(
            new BindParallelCount<OUT, AFTER>(streamConfiguration.toChannelConfiguration(),
                invocationCount,
                JRoutineCore.with(factory).apply(streamConfiguration.toInvocationConfiguration()),
                streamConfiguration.getInvocationMode()));
      }
    };
  }

  /**
   * Returns a function splitting the outputs produced by the stream, so that each subset will be
   * processed by a different routine invocation.
   * <br>
   * Each output will be assigned to a specific invocation based on the load of the available
   * invocations.
   *
   * @param invocationCount the number of processing invocations.
   * @param routine         the processing routine instance.
   * @param <IN>            the input data type.
   * @param <OUT>           the output data type.
   * @param <AFTER>         the new output type.
   * @return the lifting function.
   * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT, AFTER> LiftFunction<IN, OUT, IN, AFTER> parallel(
      final int invocationCount, @NotNull final Routine<? super OUT, ? extends AFTER> routine) {
    ConstantConditions.notNull("routine instance", routine);
    return new LiftFunction<IN, OUT, IN, AFTER>() {

      public Function<Channel<?, IN>, Channel<?, AFTER>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).andThen(
            new BindParallelCount<OUT, AFTER>(streamConfiguration.toChannelConfiguration(),
                invocationCount, routine, streamConfiguration.getInvocationMode()));
      }
    };
  }

  /**
   * Returns a function splitting the outputs produced by the stream, so that each subset will be
   * processed by a different routine invocation.
   * <br>
   * Each output will be assigned to a specific invocation based on the load of the available
   * invocations.
   *
   * @param invocationCount the number of processing invocations.
   * @param builder         the builder of processing routine instances.
   * @param <IN>            the input data type.
   * @param <OUT>           the output data type.
   * @param <AFTER>         the new output type.
   * @return the lifting function.
   * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT, AFTER> LiftFunction<IN, OUT, IN, AFTER> parallel(
      final int invocationCount,
      @NotNull final RoutineBuilder<? super OUT, ? extends AFTER> builder) {
    return parallel(invocationCount, builder.buildRoutine());
  }

  /**
   * Returns a function splitting the outputs produced by the stream, so that each subset will be
   * processed by a different routine invocation.
   * <br>
   * Each output will be assigned to a specific set based on the key returned by the specified
   * function.
   *
   * @param keyFunction the function assigning a key to each output.
   * @param factory     the invocation factory.
   * @param <IN>        the input data type.
   * @param <OUT>       the output data type.
   * @param <AFTER>     the new output type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT, AFTER> LiftFunction<IN, OUT, IN, AFTER> parallelBy(
      @NotNull final Function<? super OUT, ?> keyFunction,
      @NotNull final InvocationFactory<? super OUT, ? extends AFTER> factory) {
    ConstantConditions.notNull("function instance", keyFunction);
    ConstantConditions.notNull("invocation factory", factory);
    return new LiftFunction<IN, OUT, IN, AFTER>() {

      public Function<Channel<?, IN>, Channel<?, AFTER>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).andThen(
            new BindParallelKey<OUT, AFTER>(streamConfiguration.toChannelConfiguration(),
                keyFunction,
                JRoutineCore.with(factory).apply(streamConfiguration.toInvocationConfiguration()),
                streamConfiguration.getInvocationMode()));
      }
    };
  }

  /**
   * Returns a function splitting the outputs produced by the stream, so that each subset will be
   * processed by a different routine invocation.
   * <br>
   * Each output will be assigned to a specific set based on the key returned by the specified
   * function.
   *
   * @param keyFunction the function assigning a key to each output.
   * @param routine     the processing routine instance
   * @param <IN>        the input data type.
   * @param <OUT>       the output data type.
   * @param <AFTER>     the new output type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT, AFTER> LiftFunction<IN, OUT, IN, AFTER> parallelBy(
      @NotNull final Function<? super OUT, ?> keyFunction,
      @NotNull final Routine<? super OUT, ? extends AFTER> routine) {
    ConstantConditions.notNull("function instance", keyFunction);
    ConstantConditions.notNull("routine instance", routine);
    return new LiftFunction<IN, OUT, IN, AFTER>() {

      public Function<Channel<?, IN>, Channel<?, AFTER>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).andThen(
            new BindParallelKey<OUT, AFTER>(streamConfiguration.toChannelConfiguration(),
                keyFunction, routine, streamConfiguration.getInvocationMode()));
      }
    };
  }

  /**
   * Returns a function splitting the outputs produced by the stream, so that each subset will be
   * processed by a different routine invocation.
   * <br>
   * Each output will be assigned to a specific set based on the key returned by the specified
   * function.
   *
   * @param keyFunction the function assigning a key to each output.
   * @param builder     the builder of processing routine instances.
   * @param <IN>        the input data type.
   * @param <OUT>       the output data type.
   * @param <AFTER>     the new output type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT, AFTER> LiftFunction<IN, OUT, IN, AFTER> parallelBy(
      @NotNull final Function<? super OUT, ?> keyFunction,
      @NotNull final RoutineBuilder<? super OUT, ? extends AFTER> builder) {
    return parallelBy(keyFunction, builder.buildRoutine());
  }

  /**
   * Returns a function making the stream retry the whole flow of data until the specified function
   * does not return a null value.
   * <br>
   * For each retry the function is called passing the retry count (starting from 1) and the error
   * which caused the failure. If the function returns a non-null value, it will represent the
   * number of milliseconds to wait before a further retry. While, in case the function returns
   * null, the flow of data will be aborted with the passed error as reason.
   * <p>
   * Note that no retry will be attempted in case of an explicit abortion, that is, if the error
   * is an instance of {@link com.github.dm.jrt.core.channel.AbortException}.
   *
   * @param backoffFunction the retry function.
   * @param <IN>            the input data type.
   * @param <OUT>           the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> retry(
      @NotNull final BiFunction<? super Integer, ? super RoutineException, ? extends Long>
          backoffFunction) {
    ConstantConditions.notNull("function instance", backoffFunction);
    return new LiftFunction<IN, OUT, IN, OUT>() {

      public Function<Channel<?, IN>, Channel<?, OUT>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return new BindRetry<IN, OUT>(streamConfiguration.toChannelConfiguration(), function,
            backoffFunction);
      }
    };
  }

  /**
   * Returns a function making the stream retry the whole flow of data at maximum for the
   * specified number of times.
   *
   * @param maxCount the maximum number of retries.
   * @param <IN>     the input data type.
   * @param <OUT>    the output data type.
   * @return the lifting function.
   * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> retry(final int maxCount) {
    return retry(maxCount, BackoffBuilder.noDelay());
  }

  /**
   * Returns a function making the stream retry the whole flow of data at maximum for the
   * specified number of times.
   * <br>
   * For each retry the specified backoff policy will be applied before re-starting the flow.
   *
   * @param maxCount the maximum number of retries.
   * @param backoff  the backoff policy.
   * @param <IN>     the input data type.
   * @param <OUT>    the output data type.
   * @return the lifting function.
   * @throws java.lang.IllegalArgumentException if the specified count number is 0 or negative.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> retry(final int maxCount,
      @NotNull final Backoff backoff) {
    return retry(new RetryBackoff(maxCount, backoff));
  }

  /**
   * Returns a function making the stream throttle the invocation instances so that only the
   * specified maximum number are concurrently running at any given time.
   * <br>
   * Note that the same function instance can be used with several streams, so that the total
   * number of invocations will not exceed the specified limit.
   *
   * @param maxInvocations the maximum number of invocations.
   * @param <IN>           the input data type.
   * @param <OUT>          the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> throttle(final int maxInvocations) {
    return new Throttle<IN, OUT>(maxInvocations);
  }

  /**
   * Returns a function making the stream throttle the invocation instances so that only the
   * specified maximum number are started in the passed time range.
   * <br>
   * Note that the same function instance can be used with several streams, so that the total
   * number of started invocations will not exceed the specified limit.
   *
   * @param maxInvocations the maximum number of invocations.
   * @param range          the time range.
   * @param <IN>           the input data type.
   * @param <OUT>          the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> throttle(final int maxInvocations,
      @NotNull final DurationMeasure range) {
    return throttle(maxInvocations, range.value, range.unit);
  }

  /**
   * Returns a function making the stream throttle the invocation instances so that only the
   * specified maximum number are started in the passed time range.
   * <br>
   * Note that the same function instance can be used with several streams, so that the total
   * number of started invocations will not exceed the specified limit.
   *
   * @param maxInvocations the maximum number.
   * @param range          the time range value.
   * @param timeUnit       the time range unit.
   * @param <IN>           the input data type.
   * @param <OUT>          the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> throttle(final int maxInvocations,
      final long range, @NotNull final TimeUnit timeUnit) {
    return new TimeThrottle<IN, OUT>(maxInvocations, range, timeUnit);
  }

  /**
   * Returns a function making the stream abort with a
   * {@link com.github.dm.jrt.stream.transform.ResultTimeoutException ResultTimeoutException} if
   * a new result is not produced before the specified timeout elapsed.
   *
   * @param outputTimeout the new output timeout.
   * @param <IN>          the input data type.
   * @param <OUT>         the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> timeoutAfter(
      @NotNull final DurationMeasure outputTimeout) {
    return timeoutAfter(outputTimeout.value, outputTimeout.unit);
  }

  /**
   * Returns a function making the stream abort with a
   * {@link com.github.dm.jrt.stream.transform.ResultTimeoutException ResultTimeoutException} if
   * a new result is not produced, or the invocation does not complete, before the specified
   * timeouts elapse.
   *
   * @param outputTimeout the new output timeout.
   * @param totalTimeout  the total timeout.
   * @param <IN>          the input data type.
   * @param <OUT>         the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> timeoutAfter(
      @NotNull final DurationMeasure outputTimeout, @NotNull final DurationMeasure totalTimeout) {
    return timeoutAfter(outputTimeout.value, outputTimeout.unit, totalTimeout.value,
        totalTimeout.unit);
  }

  /**
   * Returns a function making the stream abort with a
   * {@link com.github.dm.jrt.stream.transform.ResultTimeoutException ResultTimeoutException} if
   * a new result is not produced before the specified timeout elapses.
   *
   * @param outputTimeout  the new output timeout value.
   * @param outputTimeUnit the new output timeout unit.
   * @param <IN>           the input data type.
   * @param <OUT>          the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> timeoutAfter(final long outputTimeout,
      @NotNull final TimeUnit outputTimeUnit) {
    final DurationMeasure indefiniteTime = indefiniteTime();
    return timeoutAfter(outputTimeout, outputTimeUnit, indefiniteTime.value, indefiniteTime.unit);
  }

  /**
   * Returns a function making the stream abort with a
   * {@link com.github.dm.jrt.stream.transform.ResultTimeoutException ResultTimeoutException} if
   * a new result is not produced, or the invocation does not complete, before the specified
   * timeouts elapse.
   *
   * @param outputTimeout  the new output timeout value.
   * @param outputTimeUnit the new output timeout unit.
   * @param totalTimeout   the total timeout value.
   * @param totalTimeUnit  the total timeout unit.
   * @param <IN>           the input data type.
   * @param <OUT>          the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> timeoutAfter(final long outputTimeout,
      @NotNull final TimeUnit outputTimeUnit, final long totalTimeout,
      @NotNull final TimeUnit totalTimeUnit) {
    ConstantConditions.notNull("total time unit", totalTimeUnit);
    ConstantConditions.notNull("output time unit", outputTimeUnit);
    ConstantConditions.notNegative("total timeout value", totalTimeout);
    ConstantConditions.notNegative("output timeout value", outputTimeout);
    return new LiftFunction<IN, OUT, IN, OUT>() {

      public Function<Channel<?, IN>, Channel<?, OUT>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).andThen(
            new BindTimeout<OUT>(streamConfiguration.toChannelConfiguration(), outputTimeout,
                outputTimeUnit, totalTimeout, totalTimeUnit));
      }
    };
  }

  /**
   * Returns a function concatenating to the stream a consumer handling invocation exceptions.
   * <br>
   * The errors will not be automatically further propagated.
   *
   * @param catchFunction the function instance.
   * @param <IN>          the input data type.
   * @param <OUT>         the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> tryCatch(
      @NotNull final Function<? super RoutineException, ? extends OUT> catchFunction) {
    return tryCatchAccept(new TryCatchBiConsumerFunction<OUT>(catchFunction));
  }

  /**
   * Returns a function concatenating to the stream a consumer handling invocation exceptions.
   * <br>
   * The result channel of the backing routine will be passed to the consumer, so that multiple
   * or no results may be generated.
   * <br>
   * The errors will not be automatically further propagated.
   *
   * @param catchConsumer the bi-consumer instance.
   * @param <IN>          the input data type.
   * @param <OUT>         the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> tryCatchAccept(
      @NotNull final BiConsumer<? super RoutineException, ? super Channel<OUT, ?>> catchConsumer) {
    ConstantConditions.notNull("consumer instance", catchConsumer);
    return new LiftFunction<IN, OUT, IN, OUT>() {

      public Function<Channel<?, IN>, Channel<?, OUT>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).andThen(
            new BindTryCatch<OUT>(streamConfiguration.toChannelConfiguration(), catchConsumer));
      }
    };
  }

  /**
   * Returns a function concatenating to the stream an action always performed when outputs
   * complete, even if an error occurred.
   * <br>
   * Both outputs and errors will be automatically passed on.
   *
   * @param finallyAction the action instance.
   * @param <IN>          the input data type.
   * @param <OUT>         the output data type.
   * @return the lifting function.
   * @see StreamBuilder#lift(BiFunction)
   */
  @NotNull
  public static <IN, OUT> LiftFunction<IN, OUT, IN, OUT> tryFinally(
      @NotNull final Action finallyAction) {
    ConstantConditions.notNull("action instance", finallyAction);
    return new LiftFunction<IN, OUT, IN, OUT>() {

      public Function<Channel<?, IN>, Channel<?, OUT>> apply(
          final StreamConfiguration streamConfiguration,
          final Function<Channel<?, IN>, Channel<?, OUT>> function) {
        return decorate(function).andThen(
            new BindTryFinally<OUT>(streamConfiguration.toChannelConfiguration(), finallyAction));
      }
    };
  }
}
