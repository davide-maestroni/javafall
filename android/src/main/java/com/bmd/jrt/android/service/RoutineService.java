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
package com.bmd.jrt.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.bmd.jrt.android.invocation.AndroidInvocation;
import com.bmd.jrt.builder.DefaultConfigurationBuilder;
import com.bmd.jrt.builder.RoutineChannelBuilder.DataOrder;
import com.bmd.jrt.builder.RoutineConfiguration;
import com.bmd.jrt.builder.RoutineConfigurationBuilder;
import com.bmd.jrt.channel.OutputConsumer;
import com.bmd.jrt.channel.ParameterChannel;
import com.bmd.jrt.common.RoutineException;
import com.bmd.jrt.common.RoutineInterruptedException;
import com.bmd.jrt.invocation.Invocation;
import com.bmd.jrt.log.Log;
import com.bmd.jrt.log.Log.LogLevel;
import com.bmd.jrt.log.Logger;
import com.bmd.jrt.routine.AbstractRoutine;
import com.bmd.jrt.runner.Runner;
import com.bmd.jrt.runner.Runners;
import com.bmd.jrt.time.TimeDuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.bmd.jrt.common.Reflection.findConstructor;

/**
 * Basic implementation of a service running routine invocations.
 * <p/>
 * Created by davide on 1/9/15.
 */
public class RoutineService extends Service {

    public static final int MSG_ABORT = -1;

    public static final int MSG_COMPLETE = 2;

    public static final int MSG_DATA = 1;

    public static final int MSG_INIT = 0;

    private static final String KEY_ABORT_EXCEPTION = "abort_exception";

    private static final String KEY_AVAILABLE_TIMEOUT = "avail_time";

    private static final String KEY_AVAILABLE_TIMEUNIT = "avail_unit";

    private static final String KEY_DATA_VALUE = "data_value";

    private static final String KEY_INPUT_ORDER = "input_order";

    private static final String KEY_INVOCATION_CLASS = "invocation_class";

    private static final String KEY_INVOCATION_ID = "invocation_id";

    private static final String KEY_LOG_CLASS = "log_class";

    private static final String KEY_LOG_LEVEL = "log_level";

    private static final String KEY_MAX_RETAINED = "max_retained";

    private static final String KEY_MAX_RUNNING = "max_running";

    private static final String KEY_OUTPUT_ORDER = "output_order";

    private static final String KEY_PARALLEL_INVOCATION = "parallel_invocation";

    private static final String KEY_RUNNER_CLASS = "runner_class";

    private final Messenger mInMessenger = new Messenger(new IncomingHandler());

    private final HashMap<String, RoutineInvocation> mInvocationMap =
            new HashMap<String, RoutineInvocation>();

    private final Logger mLogger;

    private final Object mMutex = new Object();

    private final HashMap<RoutineInfo, RoutineState> mRoutineMap =
            new HashMap<RoutineInfo, RoutineState>();

    /**
     * Constructor.
     */
    @SuppressWarnings("UnusedDeclaration")
    public RoutineService() {

        this(Logger.getDefaultLog(), Logger.getDefaultLogLevel());
    }

    /**
     * Constructor.
     *
     * @param log      the log instance.
     * @param logLevel the log level.
     * @throws NullPointerException if the specified log level is null.
     */
    public RoutineService(@Nullable final Log log, @Nonnull final LogLevel logLevel) {

        mLogger = Logger.create(log, logLevel, this);
    }

    /**
     * Extracts the abort exception from the specified message.
     *
     * @param message the message.
     * @return the exception or null.
     * @throws NullPointerException if the specified message is null.
     */
    @Nullable
    public static Throwable getAbortError(@Nonnull final Message message) {

        final Bundle data = message.peekData();

        if (data == null) {

            return null;
        }

        data.setClassLoader(RoutineService.class.getClassLoader());
        return (Throwable) data.getSerializable(KEY_ABORT_EXCEPTION);
    }

    /**
     * Extracts the value object from the specified message.
     *
     * @param message the message.
     * @return the value or null.
     * @throws NullPointerException if the specified message is null.
     */
    @Nullable
    public static Object getValue(@Nonnull final Message message) {

        final Bundle data = message.peekData();

        if (data == null) {

            return null;
        }

        data.setClassLoader(RoutineService.class.getClassLoader());
        final ParcelableValue parcelable = data.getParcelable(KEY_DATA_VALUE);
        return parcelable.getValue();
    }

    /**
     * Puts the specified asynchronous invocation info into the passed bundle.
     *
     * @param bundle          the bundle to fill.
     * @param invocationId    the invocation ID.
     * @param invocationClass the invocation class.
     * @param configuration   the routine configuration.
     * @param runnerClass     the runner class.
     * @param logClass        the log class.
     * @throws NullPointerException if any of the specified nonnull parameters is null.
     */
    public static void putAsyncInvocation(@Nonnull final Bundle bundle,
            @Nonnull final String invocationId,
            @Nonnull final Class<? extends AndroidInvocation<?, ?>> invocationClass,
            @Nonnull final RoutineConfiguration configuration,
            @Nullable final Class<? extends Runner> runnerClass,
            @Nullable final Class<? extends Log> logClass) {

        putInvocation(bundle, false, invocationId, invocationClass, configuration, runnerClass,
                      logClass);
    }

    /**
     * Puts the specified abort exception into the passed bundle.
     *
     * @param bundle the bundle to fill.
     * @param error  the exception instance.
     */
    public static void putError(@Nonnull final Bundle bundle, final Throwable error) {

        bundle.putSerializable(RoutineService.KEY_ABORT_EXCEPTION, error);
    }

    /**
     * Puts the specified invocation ID into the passed bundle.
     *
     * @param bundle       the bundle to fill.
     * @param invocationId the invocation ID.
     */
    public static void putInvocationId(@Nonnull final Bundle bundle,
            @Nonnull final String invocationId) {

        bundle.putString(KEY_INVOCATION_ID, invocationId);
    }

    /**
     * Puts the specified parallel invocation info into the passed bundle.
     *
     * @param bundle          the bundle to fill.
     * @param invocationId    the invocation ID.
     * @param invocationClass the invocation class.
     * @param configuration   the routine configuration.
     * @param runnerClass     the runner class.
     * @param logClass        the log class.
     * @throws NullPointerException if any of the specified nonnull parameters is null.
     */
    public static void putParallelInvocation(@Nonnull final Bundle bundle,
            @Nonnull final String invocationId,
            @Nonnull final Class<? extends AndroidInvocation<?, ?>> invocationClass,
            @Nonnull final RoutineConfiguration configuration,
            @Nullable final Class<? extends Runner> runnerClass,
            @Nullable final Class<? extends Log> logClass) {

        putInvocation(bundle, true, invocationId, invocationClass, configuration, runnerClass,
                      logClass);
    }

    /**
     * Puts the specified value object into the passed bundle.
     *
     * @param bundle the bundle to fill.
     * @param value  the value instance.
     */
    public static void putValue(@Nonnull final Bundle bundle, final Object value) {

        bundle.putParcelable(KEY_DATA_VALUE, new ParcelableValue(value));
    }

    @Nonnull
    private static DataOrder normalize(@Nullable final DataOrder order) {

        return (order != null) ? order : DataOrder.DEFAULT;
    }

    @Nonnull
    private static LogLevel normalize(@Nullable final LogLevel level) {

        return (level != null) ? level : LogLevel.DEFAULT;
    }

    /**
     * Puts the specified invocation info into the passed bundle.
     *
     * @param bundle          the bundle to fill.
     * @param isParallel      whether the invocation is parallel.
     * @param invocationId    the invocation ID.
     * @param invocationClass the invocation class.
     * @param configuration   the routine configuration.
     * @param runnerClass     the runner class.
     * @param logClass        the log class.
     * @throws NullPointerException if any of the specified nonnull parameters is null.
     */
    private static void putInvocation(@Nonnull final Bundle bundle, boolean isParallel,
            @Nonnull final String invocationId,
            @Nonnull final Class<? extends AndroidInvocation<?, ?>> invocationClass,
            @Nonnull final RoutineConfiguration configuration,
            @Nullable final Class<? extends Runner> runnerClass,
            @Nullable final Class<? extends Log> logClass) {

        bundle.putBoolean(KEY_PARALLEL_INVOCATION, isParallel);
        bundle.putString(KEY_INVOCATION_ID, invocationId);
        bundle.putSerializable(KEY_INVOCATION_CLASS, invocationClass);
        bundle.putInt(KEY_MAX_RETAINED,
                      configuration.getMaxRetained(RoutineConfigurationBuilder.DEFAULT));
        bundle.putInt(KEY_MAX_RUNNING,
                      configuration.getMaxRunning(RoutineConfigurationBuilder.DEFAULT));

        final TimeDuration availTimeout = configuration.getAvailTimeout(null);

        if (availTimeout != null) {

            bundle.putLong(KEY_AVAILABLE_TIMEOUT, availTimeout.time);
            bundle.putSerializable(KEY_AVAILABLE_TIMEUNIT, availTimeout.unit);
        }

        bundle.putSerializable(KEY_INPUT_ORDER, configuration.getInputOrder(DataOrder.DEFAULT));
        bundle.putSerializable(KEY_OUTPUT_ORDER, configuration.getOutputOrder(DataOrder.DEFAULT));
        bundle.putSerializable(KEY_RUNNER_CLASS, runnerClass);
        bundle.putSerializable(KEY_LOG_CLASS, logClass);
        bundle.putSerializable(KEY_LOG_LEVEL, configuration.getLogLevel(LogLevel.DEFAULT));
    }

    @Override
    public void onDestroy() {

        synchronized (mMutex) {

            for (final RoutineState routineState : mRoutineMap.values()) {

                routineState.flush();
            }
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {

        return mInMessenger.getBinder();
    }

    @Nonnull
    private RoutineInvocation getInvocation(@Nonnull final Message message) {

        final Bundle data = message.peekData();

        if (data == null) {

            throw new IllegalArgumentException("service message cannot have null data");
        }

        data.setClassLoader(getClassLoader());
        final String invocationId = data.getString(KEY_INVOCATION_ID);

        if (invocationId == null) {

            throw new IllegalArgumentException("service message is missing invocation ID");
        }

        synchronized (mMutex) {

            final RoutineInvocation invocation = mInvocationMap.get(invocationId);

            if (invocation == null) {

                throw new IllegalArgumentException(
                        "service message has invalid invocation ID: " + invocationId);
            }

            return invocation;
        }
    }

    @SuppressWarnings("unchecked")
    private void initRoutine(final Message message) {

        final Bundle data = message.peekData();

        if (data == null) {

            throw new IllegalArgumentException("service message cannot have null data");
        }

        data.setClassLoader(getClassLoader());
        final String invocationId = data.getString(KEY_INVOCATION_ID);

        if (invocationId == null) {

            throw new IllegalArgumentException("service message is missing invocation ID");
        }

        final Class<? extends AndroidInvocation<Object, Object>> invocationClass =
                (Class<? extends AndroidInvocation<Object, Object>>) data.getSerializable(
                        KEY_INVOCATION_CLASS);

        if (invocationClass == null) {

            throw new IllegalArgumentException("service message is missing invocation class");
        }

        synchronized (mMutex) {

            final HashMap<String, RoutineInvocation> invocationMap = mInvocationMap;

            if (invocationMap.containsKey(invocationId)) {

                throw new IllegalArgumentException(
                        "an invocation with the same ID is already running: " + invocationId);
            }

            final int maxRetained = data.getInt(KEY_MAX_RETAINED);
            final int maxRunning = data.getInt(KEY_MAX_RUNNING);
            final long timeout = data.getLong(KEY_AVAILABLE_TIMEOUT);
            final TimeUnit timeUnit = (TimeUnit) data.getSerializable(KEY_AVAILABLE_TIMEUNIT);
            final TimeDuration availTimeout =
                    (timeUnit != null) ? TimeDuration.fromUnit(timeout, timeUnit) : null;
            final DataOrder inputOrder =
                    normalize((DataOrder) data.getSerializable(KEY_INPUT_ORDER));
            final DataOrder outputOrder =
                    normalize((DataOrder) data.getSerializable(KEY_OUTPUT_ORDER));
            final Class<? extends Runner> runnerClass =
                    (Class<? extends Runner>) data.getSerializable(KEY_RUNNER_CLASS);
            final Class<? extends Log> logClass =
                    (Class<? extends Log>) data.getSerializable(KEY_LOG_CLASS);
            final LogLevel logLevel = normalize((LogLevel) data.getSerializable(KEY_LOG_LEVEL));

            final RoutineInfo routineInfo =
                    new RoutineInfo(invocationClass, inputOrder, outputOrder, runnerClass, logClass,
                                    logLevel);
            final HashMap<RoutineInfo, RoutineState> routineMap = mRoutineMap;
            RoutineState routineState = routineMap.get(routineInfo);

            if (routineState == null) {

                final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

                if (runnerClass != null) {

                    try {

                        builder.runBy(findConstructor(runnerClass).newInstance());

                    } catch (final RoutineInterruptedException e) {

                        mLogger.err(e, "error creating the runner instance");
                        throw e.interrupt();

                    } catch (final Throwable t) {

                        mLogger.err(t, "error creating the runner instance");
                        throw new IllegalArgumentException(t);
                    }
                }

                if (logClass != null) {

                    try {

                        builder.loggedWith(findConstructor(logClass).newInstance());

                    } catch (final RoutineInterruptedException e) {

                        mLogger.err(e, "error creating the log instance");
                        throw e.interrupt();

                    } catch (final Throwable t) {

                        mLogger.err(t, "error creating the log instance");
                        throw new IllegalArgumentException(t);
                    }
                }

                builder.maxRetained(maxRetained)
                       .maxRunning(maxRunning)
                       .availableTimeout(availTimeout)
                       .inputOrder(inputOrder)
                       .outputOrder(outputOrder)
                       .logLevel(logLevel);

                final AndroidRoutine androidRoutine =
                        new AndroidRoutine(this, builder.buildConfiguration(), invocationClass);
                routineState = new RoutineState(androidRoutine);
                routineMap.put(routineInfo, routineState);
            }

            final boolean isParallel = data.getBoolean(KEY_PARALLEL_INVOCATION);
            final ParameterChannel<Object, Object> channel =
                    (isParallel) ? routineState.invokeParallel() : routineState.invokeAsync();

            invocationMap.put(invocationId,
                              new RoutineInvocation(invocationId, channel, routineInfo,
                                                    routineState));
        }
    }

    /**
     * Synchronous Android routine implementation.
     */
    private static class AndroidRoutine extends AbstractRoutine<Object, Object> {

        private final Constructor<? extends AndroidInvocation<Object, Object>> mConstructor;

        private final Context mContext;

        private final Logger mLogger;

        /**
         * Constructor.
         *
         * @param context         the routine context.
         * @param configuration   the routine configuration.
         * @param invocationClass the invocation class.
         */
        private AndroidRoutine(@Nonnull final Context context,
                @Nonnull final RoutineConfiguration configuration,
                @Nonnull final Class<? extends AndroidInvocation<Object, Object>> invocationClass) {

            super(configuration, Runners.sequentialRunner());

            mContext = context;
            mConstructor = findConstructor(invocationClass);
            mLogger = Logger.create(configuration.getLog(null),
                                    configuration.getLogLevel(LogLevel.DEFAULT), this);
        }

        @Nonnull
        @Override
        protected Invocation<Object, Object> createInvocation(final boolean async) {

            final Logger logger = mLogger;

            try {

                final Constructor<? extends AndroidInvocation<Object, Object>> constructor =
                        mConstructor;
                logger.dbg("creating a new instance of class: %s", constructor.getDeclaringClass());
                final AndroidInvocation<Object, Object> invocation = constructor.newInstance();
                invocation.onContext(mContext);
                return invocation;

            } catch (final InvocationTargetException e) {

                logger.err(e, "error creating the invocation instance");
                throw new RoutineException(e.getCause());

            } catch (final RoutineInterruptedException e) {

                logger.err(e, "error creating the invocation instance");
                throw e.interrupt();

            } catch (final RoutineException e) {

                logger.err(e, "error creating the invocation instance");
                throw e;

            } catch (final Throwable t) {

                logger.err(t, "error creating the invocation instance");
                throw new RoutineException(t);
            }
        }
    }

    /**
     * Class storing the routine information.
     */
    private static class RoutineInfo {

        private final DataOrder mInputOrder;

        private final Class<? extends AndroidInvocation<?, ?>> mInvocationClass;

        private final Class<? extends Log> mLogClass;

        private final LogLevel mLogLevel;

        private final DataOrder mOutputOrder;

        private final Class<? extends Runner> mRunnerClass;

        /**
         * Constructor.
         *
         * @param invocationClass the invocation class.
         * @param inputOrder      the input data order.
         * @param outputOrder     the output data order.
         * @param runnerClass     the runner class.
         * @param logClass        the log class.
         * @param logLevel        the log level.
         */
        private RoutineInfo(@Nonnull final Class<? extends AndroidInvocation<?, ?>> invocationClass,
                @Nonnull final DataOrder inputOrder, @Nonnull final DataOrder outputOrder,
                @Nullable final Class<? extends Runner> runnerClass,
                @Nullable final Class<? extends Log> logClass, @Nonnull final LogLevel logLevel) {

            mInvocationClass = invocationClass;
            mInputOrder = inputOrder;
            mOutputOrder = outputOrder;
            mRunnerClass = runnerClass;
            mLogClass = logClass;
            mLogLevel = logLevel;
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if (!(o instanceof RoutineInfo)) {

                return false;
            }

            final RoutineInfo that = (RoutineInfo) o;

            return mInputOrder == that.mInputOrder && mInvocationClass.equals(that.mInvocationClass)
                    && !(mLogClass != null ? !mLogClass.equals(that.mLogClass)
                    : that.mLogClass != null) && mLogLevel == that.mLogLevel
                    && mOutputOrder == that.mOutputOrder && !(mRunnerClass != null
                    ? !mRunnerClass.equals(that.mRunnerClass) : that.mRunnerClass != null);
        }

        @Override
        public int hashCode() {

            int result = mInputOrder.hashCode();
            result = 31 * result + mInvocationClass.hashCode();
            result = 31 * result + (mLogClass != null ? mLogClass.hashCode() : 0);
            result = 31 * result + mLogLevel.hashCode();
            result = 31 * result + mOutputOrder.hashCode();
            result = 31 * result + (mRunnerClass != null ? mRunnerClass.hashCode() : 0);
            return result;
        }
    }

    /**
     * Class storing the last routine state.
     */
    private static class RoutineState {

        private final AndroidRoutine mRoutine;

        private int mInvocationCount;

        /**
         * Constructor.
         *
         * @param routine the routine instance.
         */
        private RoutineState(@Nonnull final AndroidRoutine routine) {

            mRoutine = routine;
        }

        /**
         * Makes the routine flush all the cached invocation instances.
         */
        public void flush() {

            mRoutine.flush();
        }

        /**
         * Increments count of the running routines and starts an asynchronous invocation.
         *
         * @return the invocation parameter channel.
         */
        @Nonnull
        public ParameterChannel<Object, Object> invokeAsync() {

            ++mInvocationCount;
            return mRoutine.invokeAsync();
        }

        /**
         * Increments count of the running routines and starts a parallel invocation.
         *
         * @return the invocation parameter channel.
         */
        @Nonnull
        public ParameterChannel<Object, Object> invokeParallel() {

            ++mInvocationCount;
            return mRoutine.invokeParallel();
        }

        /**
         * Decrements count of the running routines and returns the updated value.
         *
         * @return the running routines count.
         */
        public int releaseInvocation() {

            return --mInvocationCount;
        }
    }

    /**
     * Handler implementation managing incoming messages from the routine invocation.
     */
    private class IncomingHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {

            final Logger logger = mLogger;
            logger.dbg("incoming routine message: %s", msg);

            try {

                switch (msg.what) {

                    case MSG_DATA:
                        getInvocation(msg).pass(getValue(msg));
                        break;

                    case MSG_COMPLETE:
                        final RoutineInvocation invocation = getInvocation(msg);
                        invocation.result(new ServiceOutputConsumer(invocation, msg.replyTo));
                        break;

                    case MSG_ABORT:
                        getInvocation(msg).abort(getAbortError(msg));
                        break;

                    case MSG_INIT:
                        initRoutine(msg);
                        break;

                    default:
                        super.handleMessage(msg);
                }

            } catch (final Throwable t) {

                logger.err(t, "error while parsing routine message");

                final Bundle data = msg.peekData();

                if (data != null) {

                    data.setClassLoader(getClassLoader());
                    final String invocationId = data.getString(KEY_INVOCATION_ID);

                    if (invocationId != null) {

                        synchronized (mMutex) {

                            final RoutineInvocation invocation = mInvocationMap.get(invocationId);

                            if (invocation != null) {

                                invocation.release();
                            }
                        }
                    }
                }

                final Messenger outMessenger = msg.replyTo;

                if (outMessenger == null) {

                    logger.wrn("avoid aborting since reply messenger is null");
                    return;
                }

                final Bundle bundle = new Bundle();
                putError(bundle, t);
                final Message message = Message.obtain(null, RoutineService.MSG_ABORT);
                message.setData(bundle);

                try {

                    outMessenger.send(message);

                } catch (final RemoteException e) {

                    logger.err(e, "error while sending routine abort message");
                }
            }
        }
    }

    /**
     * Class storing the routine invocation information.
     */
    private class RoutineInvocation {

        private final ParameterChannel<Object, Object> mChannel;

        private final String mId;

        private final RoutineInfo mRoutineInfo;

        private final RoutineState mRoutineState;

        /**
         * Constructor.
         *
         * @param id      the invocation ID.
         * @param channel the invocation parameter channel.
         * @param info    the routine info.
         * @param state   the routine state.
         */
        private RoutineInvocation(@Nonnull final String id,
                @Nonnull final ParameterChannel<Object, Object> channel,
                @Nonnull final RoutineInfo info, @Nonnull final RoutineState state) {

            mId = id;
            mChannel = channel;
            mRoutineInfo = info;
            mRoutineState = state;
        }

        /**
         * Closes the channel and abort the transfer of data.
         *
         * @param reason the throwable object identifying the reason of the routine abortion.
         */
        public void abort(@Nullable final Throwable reason) {

            mChannel.abort(reason);
        }

        /**
         * Passes the specified input to the invocation parameter channel.
         *
         * @param input the input.
         * @throws IllegalStateException if the channel is already closed.
         * @throws RoutineException      if the execution has been aborted with an exception.
         */
        public void pass(@Nullable final Object input) {

            mChannel.pass(input);
        }

        /**
         * Releases this invocation, that is, removes it from the service cache.
         */
        public void release() {

            synchronized (mMutex) {

                mInvocationMap.remove(mId);

                if (mRoutineState.releaseInvocation() <= 0) {

                    mRoutineMap.remove(mRoutineInfo);
                }
            }
        }

        /**
         * Closes the input channel and binds the specified consumer to the output one.
         *
         * @throws NullPointerException  if the specified consumer is null.
         * @throws IllegalStateException if the channel is already closed or already bound to a
         *                               consumer.
         * @throws RoutineException      if the execution has been aborted with an exception.
         */
        public void result(@Nonnull final OutputConsumer<Object> consumer) {

            mChannel.result().bind(consumer);
        }
    }

    /**
     * Output consumer sending messages to the routine.
     */
    private class ServiceOutputConsumer implements OutputConsumer<Object> {

        private final RoutineInvocation mInvocation;

        private final Messenger mOutMessenger;

        /**
         * Constructor.
         *
         * @param invocation the routine invocation.
         * @param messenger  the output messenger.
         * @throws NullPointerException if the specified messenger is null.
         */
        @SuppressWarnings("ConstantConditions")
        private ServiceOutputConsumer(@Nonnull final RoutineInvocation invocation,
                @Nonnull final Messenger messenger) {

            if (messenger == null) {

                throw new NullPointerException("the output messenger must not be null");
            }

            mInvocation = invocation;
            mOutMessenger = messenger;
        }

        @Override
        public void onComplete() {

            mInvocation.release();

            final Bundle data = new Bundle();
            final Message message = Message.obtain(null, RoutineService.MSG_COMPLETE);
            message.setData(data);

            try {

                mOutMessenger.send(message);

            } catch (final RemoteException e) {

                throw new RoutineException(e);
            }
        }

        @Override
        public void onError(@Nullable final Throwable error) {

            mInvocation.release();

            final Bundle data = new Bundle();
            putError(data, error);
            final Message message = Message.obtain(null, RoutineService.MSG_ABORT);
            message.setData(data);

            try {

                mOutMessenger.send(message);

            } catch (final RemoteException e) {

                throw new RoutineException(e);
            }
        }

        @Override
        public void onOutput(final Object o) {

            final Bundle data = new Bundle();
            putValue(data, o);
            final Message message = Message.obtain(null, RoutineService.MSG_DATA);
            message.setData(data);

            try {

                mOutMessenger.send(message);

            } catch (final RemoteException e) {

                throw new RoutineException(e);
            }
        }
    }
}