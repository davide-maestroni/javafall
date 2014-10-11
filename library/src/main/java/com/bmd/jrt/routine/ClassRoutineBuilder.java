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

import com.bmd.jrt.channel.ResultChannel;
import com.bmd.jrt.common.ClassToken;
import com.bmd.jrt.common.RoutineException;
import com.bmd.jrt.execution.ExecutionBody;
import com.bmd.jrt.log.Log;
import com.bmd.jrt.log.Log.LogLevel;
import com.bmd.jrt.log.Logger;
import com.bmd.jrt.runner.Runner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import static com.bmd.jrt.routine.ReflectionUtils.boxingClass;

/**
 * Class implementing a builder of a routine wrapping a class method.
 * <p/>
 * Note that only static methods can be asynchronously invoked through the routines created by
 * this builder.
 * <p/>
 * Created by davide on 9/21/14.
 *
 * @see Async
 */
public class ClassRoutineBuilder {

    private static final ClassToken<MethodExecutionBody> METHOD_EXECUTION_TOKEN =
            ClassToken.tokenOf(MethodExecutionBody.class);

    private static final WeakHashMap<Object, Object> sMutexMap = new WeakHashMap<Object, Object>();

    private static final WeakHashMap<Object, HashMap<RoutineInfo, Routine<Object, Object>>>
            sRoutineCache =
            new WeakHashMap<Object, HashMap<RoutineInfo, Routine<Object, Object>>>();

    private final boolean mIsClass;

    private final HashMap<String, Method> mMethodMap = new HashMap<String, Method>();

    private final Object mTarget;

    private final Class<?> mTargetClass;

    private Catch mCatchClause = new RethrowCatch();

    private Boolean mIsSequential;

    private Log mLog = null;

    private LogLevel mLogLevel = null;

    private Runner mRunner;

    /**
     * Constructor.
     *
     * @param target the target class or object.
     * @throws NullPointerException     if the specified target is null.
     * @throws IllegalArgumentException if a duplicate name in the annotations is detected.
     */
    ClassRoutineBuilder(@NonNull final Object target) {

        final Class<?> targetClass;

        if (target instanceof Class) {

            mTarget = null;
            mIsClass = true;

            targetClass = ((Class<?>) target);

        } else {

            mTarget = target;
            mIsClass = false;

            targetClass = target.getClass();
        }

        mTargetClass = targetClass;

        final HashMap<String, Method> methodMap = mMethodMap;
        fillMap(methodMap, targetClass.getMethods());

        final HashMap<String, Method> declaredMethodMap = new HashMap<String, Method>();

        fillMap(declaredMethodMap, targetClass.getDeclaredMethods());

        for (final Entry<String, Method> methodEntry : declaredMethodMap.entrySet()) {

            final String name = methodEntry.getKey();

            if (!methodMap.containsKey(name)) {

                methodMap.put(name, methodEntry.getValue());
            }
        }
    }

    /**
     * Returns a routine used for calling the specified method.
     * <p/>
     * The method is searched via reflection ignoring an optional name specified in a
     * {@link Async} annotation. Though, the other annotation attributes will be honored.
     *
     * @param name           the method name.
     * @param parameterTypes the method parameter types.
     * @return the routine.
     * @throws NullPointerException     if one of the parameter is null.
     * @throws IllegalArgumentException if no matching method is found.
     * @throws RoutineException         if an error occurred while instantiating the optional
     *                                  runner or the routine.
     */
    @NonNull
    public Routine<Object, Object> classMethod(@NonNull final String name,
            @NonNull final Class<?>... parameterTypes) {

        final Class<?> targetClass = mTargetClass;
        Method targetMethod = null;

        try {

            targetMethod = targetClass.getMethod(name, parameterTypes);

        } catch (final NoSuchMethodException ignored) {

        }

        if (targetMethod == null) {

            try {

                targetMethod = targetClass.getDeclaredMethod(name, parameterTypes);

            } catch (final NoSuchMethodException e) {

                throw new IllegalArgumentException(e);
            }
        }

        return classMethod(targetMethod);
    }

    /**
     * Returns a routine used for calling the specified method.
     * <p/>
     * The method is invoked ignoring an optional name specified in a {@link Async} annotation.
     * Though, the other annotation attributes will be honored.
     *
     * @param method the method instance.
     * @return the routine.
     * @throws NullPointerException if the specified method is null.
     * @throws RoutineException     if an error occurred while instantiating the optional runner
     *                              or the routine.
     */
    @NonNull
    public Routine<Object, Object> classMethod(@NonNull final Method method) {

        if (!method.isAccessible()) {

            AccessController.doPrivileged(new SetAccessibleAction(method));
        }

        Runner runner = mRunner;
        Boolean isSequential = mIsSequential;
        Log log = mLog;
        LogLevel logLevel = mLogLevel;

        final Async annotation = method.getAnnotation(Async.class);

        if (annotation != null) {

            if (runner == null) {

                final Class<? extends Runner> runnerClass = annotation.runner();

                if (runnerClass != DefaultRunner.class) {

                    try {

                        runner = runnerClass.newInstance();

                    } catch (final InstantiationException e) {

                        throw new RoutineException(e);

                    } catch (IllegalAccessException e) {

                        throw new RoutineException(e);
                    }
                }
            }

            if (isSequential == null) {

                isSequential = annotation.sequential();
            }

            if (log == null) {

                final Class<? extends Log> logClass = annotation.log();

                if (logClass != DefaultLog.class) {

                    try {

                        log = logClass.newInstance();

                    } catch (final InstantiationException e) {

                        throw new RoutineException(e);

                    } catch (IllegalAccessException e) {

                        throw new RoutineException(e);
                    }
                }
            }

            if (logLevel == null) {

                logLevel = annotation.logLevel();
            }
        }

        return getRoutine(method, runner, isSequential, false, log, logLevel);
    }

    /**
     * Sets the log level.
     *
     * @param level the log level.
     * @return this builder.
     * @throws NullPointerException if the log level is null.
     */
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public ClassRoutineBuilder logLevel(@NonNull final LogLevel level) {

        if (level == null) {

            throw new NullPointerException("the log level must not be null");
        }

        mLogLevel = level;

        return this;
    }

    /**
     * Sets the log instance.
     *
     * @param log the log instance.
     * @return this builder.
     * @throws NullPointerException if the log is null.
     */
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public ClassRoutineBuilder loggedWith(@NonNull final Log log) {

        if (log == null) {

            throw new NullPointerException("the log instance must not be null");
        }

        mLog = log;

        return this;
    }

    /**
     * Returns a routine used for calling the method whose identifying name is specified in a
     * {@link Async} annotation.
     *
     * @param name the name specified in the annotation.
     * @return the routine.
     * @throws IllegalArgumentException if the specified method is not found.
     * @throws RoutineException         if an error occurred while instantiating the optional
     *                                  runner or the routine.
     */
    @NonNull
    public Routine<Object, Object> method(@NonNull final String name) {

        final Method method = mMethodMap.get(name);

        if (method == null) {

            throw new IllegalArgumentException(
                    "no annotated method with name '" + name + "' has been found");
        }

        return classMethod(method);
    }

    /**
     * Tells the builder to create a routine using a queued runner for synchronous invocations.
     *
     * @return this builder.
     */
    @NonNull
    public ClassRoutineBuilder queued() {

        mIsSequential = false;

        return this;
    }

    /**
     * Tells the builder to create a routine using the specified runner instance for asynchronous
     * invocations.
     *
     * @param runner the runner instance.
     * @return this builder.
     * @throws NullPointerException if the specified runner is null.
     */
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public ClassRoutineBuilder runBy(@NonNull final Runner runner) {

        if (runner == null) {

            throw new NullPointerException("the runner instance must not be null");
        }

        mRunner = runner;

        return this;
    }

    /**
     * Tells the builder to create a routine using a sequential runner for synchronous invocations.
     *
     * @return this builder.
     */
    @NonNull
    public ClassRoutineBuilder sequential() {

        mIsSequential = true;

        return this;
    }

    /**
     * Tells the builder to create a routine within the specified try/catch clause.
     *
     * @param catchClause the catch clause.
     * @return this builder.
     * @throws NullPointerException if the specified clause is null.
     */
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public ClassRoutineBuilder withinTry(@NonNull final Catch catchClause) {

        if (catchClause == null) {

            throw new NullPointerException("the catch clause must not be null");
        }

        mCatchClause = catchClause;

        return this;
    }

    /**
     * Creates the routine.
     *
     * @param method       the method to wrap.
     * @param runner       the asynchronous runner instance.
     * @param isSequential whether a sequential runner must be used for synchronous invocations.
     * @param orderedInput whether the input data are forced to be delivered in insertion order.
     * @param log          the log instance.
     * @param level        the log level.
     * @return the routine instance.
     */
    @NonNull
    protected Routine<Object, Object> getRoutine(@NonNull final Method method,
            @Nullable final Runner runner, @Nullable final Boolean isSequential,
            final boolean orderedInput, @Nullable final Log log, @Nullable final LogLevel level) {

        final Object target = mTarget;
        final Class<?> targetClass = mTargetClass;
        Routine<Object, Object> routine;

        synchronized (sMutexMap) {

            final Log routineLog = (log != null) ? log : Logger.getDefaultLog();
            final LogLevel routineLogLevel = (level != null) ? level : Logger.getDefaultLogLevel();
            final WeakHashMap<Object, HashMap<RoutineInfo, Routine<Object, Object>>> routineCache =
                    sRoutineCache;
            HashMap<RoutineInfo, Routine<Object, Object>> routineMap = routineCache.get(target);

            if (routineMap == null) {

                routineMap = new HashMap<RoutineInfo, Routine<Object, Object>>();
                routineCache.put(target, routineMap);
            }

            final Catch catchClause = mCatchClause;
            final RoutineInfo routineInfo =
                    new RoutineInfo(method, runner, isSequential, orderedInput, catchClause,
                                    routineLog, routineLogLevel);
            routine = routineMap.get(routineInfo);

            if (routine != null) {

                return routine;
            }

            final WeakHashMap<Object, Object> mutexMap = sMutexMap;
            Object mutex = mutexMap.get(target);

            if (mutex == null) {

                mutex = new Object();
                mutexMap.put(target, mutex);
            }

            final RoutineBuilder<Object, Object> builder =
                    new RoutineBuilder<Object, Object>(METHOD_EXECUTION_TOKEN);

            if (runner != null) {

                builder.runBy(runner);
            }

            if (isSequential != null) {

                if (isSequential) {

                    builder.sequential();

                } else {

                    builder.queued();
                }
            }

            if (orderedInput) {

                builder.orderedInput();
            }

            routine = builder.loggedWith(routineLog)
                             .logLevel(routineLogLevel)
                             .withArgs(target, targetClass, method, catchClause, mutex)
                             .buildRoutine();
            routineMap.put(routineInfo, routine);
        }

        return routine;
    }

    private void fillMap(@NonNull final HashMap<String, Method> map,
            @NonNull final Method[] methods) {

        final boolean isClass = mIsClass;

        for (final Method method : methods) {

            final int staticFlag = method.getModifiers() & Modifier.STATIC;

            if (isClass) {

                if (staticFlag == 0) {

                    continue;
                }

            } else if (staticFlag != 0) {

                continue;
            }

            final Async annotation = method.getAnnotation(Async.class);

            if (annotation != null) {

                String name = annotation.name();

                if ((name == null) || (name.length() == 0)) {

                    name = method.getName();
                }

                if (map.containsKey(name)) {

                    throw new IllegalArgumentException(
                            "the name '" + name + "' has already been used to identify a different"
                                    + " method");
                }

                map.put(name, method);
            }
        }
    }

    /**
     * Interface defining a catch clause.
     */
    public interface Catch {

        /**
         * Called when an exception is caught.
         *
         * @param ex the exception.
         */
        public void exception(@NonNull RoutineInvocationException ex);
    }

    /**
     * Implementation of an execution body wrapping the target method.
     */
    private static class MethodExecutionBody extends ExecutionBody<Object, Object> {

        private final Catch mCatch;

        private final boolean mHasResult;

        private final Method mMethod;

        private final Object mMutex;

        private final Object mTarget;

        private final Class<?> mTargetClass;

        /**
         * Constructor.
         *
         * @param target      the target object.
         * @param targetClass the taregt class.
         * @param method      the method to wrap.
         * @param catchClause the catch clause.
         * @param mutex       the mutex used for synchronization.
         */
        public MethodExecutionBody(@Nullable final Object target,
                @NonNull final Class<?> targetClass, @NonNull final Method method,
                @NonNull final Catch catchClause, @NonNull final Object mutex) {

            mTarget = target;
            mTargetClass = targetClass;
            mMethod = method;
            mCatch = catchClause;
            mMutex = mutex;

            final Class<?> returnType = method.getReturnType();
            mHasResult = !Void.class.equals(boxingClass(returnType));
        }

        @Override
        public void onExec(@NonNull final List<?> objects,
                @NonNull final ResultChannel<Object> results) {

            synchronized (mMutex) {

                final Object target = mTarget;
                final Method method = mMethod;

                try {

                    final Object result =
                            method.invoke(target, objects.toArray(new Object[objects.size()]));

                    if (mHasResult) {

                        results.pass(result);
                    }

                } catch (final InvocationTargetException e) {

                    mCatch.exception(
                            new RoutineInvocationException(e.getCause(), target, mTargetClass,
                                                           method.getName(),
                                                           method.getParameterTypes()));

                } catch (final RoutineException e) {

                    mCatch.exception(
                            new RoutineInvocationException(e.getCause(), target, mTargetClass,
                                                           method.getName(),
                                                           method.getParameterTypes()));

                } catch (final Throwable t) {

                    mCatch.exception(new RoutineInvocationException(t, target, mTargetClass,
                                                                    method.getName(),
                                                                    method.getParameterTypes()));
                }
            }
        }
    }

    /**
     * Implementation of a catch clause simply rethrowing the caught exception.
     */
    private static class RethrowCatch implements Catch {

        @Override
        public void exception(@NonNull final RoutineInvocationException ex) {

            throw ex;
        }
    }

    /**
     * Class used as key to identify a specific routine instance.
     */
    private static class RoutineInfo {

        private final Catch mCatchClause;

        private final Boolean mIsSequential;

        private final Log mLog;

        private final LogLevel mLogLevel;

        private final Method mMethod;

        private final boolean mOrderedInput;

        private final Runner mRunner;

        /**
         * Constructor.
         *
         * @param method       the method to wrap.
         * @param runner       the runner instance.
         * @param isSequential whether a sequential runner must be used for synchronous
         * @param orderedInput whether the input data are forced to be delivered in insertion order.
         * @param catchClause  the catch clause.
         * @param log          the log instance.
         * @param level        the log level.
         */
        private RoutineInfo(@NonNull final Method method, @Nullable final Runner runner,
                @Nullable final Boolean isSequential, final boolean orderedInput,
                @NonNull final Catch catchClause, @NonNull final Log log,
                @NonNull final LogLevel level) {

            mMethod = method;
            mRunner = runner;
            mIsSequential = isSequential;
            mOrderedInput = orderedInput;
            mCatchClause = catchClause;
            mLog = log;
            mLogLevel = level;
        }

        @Override
        public int hashCode() {

            int result = mCatchClause.hashCode();
            result = 31 * result + (mIsSequential != null ? mIsSequential.hashCode() : 0);
            result = 31 * result + mLog.hashCode();
            result = 31 * result + mLogLevel.hashCode();
            result = 31 * result + mMethod.hashCode();
            result = 31 * result + (mOrderedInput ? 1 : 0);
            result = 31 * result + (mRunner != null ? mRunner.hashCode() : 0);
            return result;
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

            return mOrderedInput == that.mOrderedInput && mCatchClause.equals(that.mCatchClause)
                    && !(mIsSequential != null ? !mIsSequential.equals(that.mIsSequential)
                    : that.mIsSequential != null) && mLog.equals(that.mLog)
                    && mLogLevel == that.mLogLevel && mMethod.equals(that.mMethod) && !(
                    mRunner != null ? !mRunner.equals(that.mRunner) : that.mRunner != null);
        }
    }

    /**
     * Privileged action used to grant accessibility to a method.
     */
    private static class SetAccessibleAction implements PrivilegedAction<Void> {

        private final Method mMethod;

        /**
         * Constructor.
         *
         * @param method the method instance.
         */
        private SetAccessibleAction(@NonNull final Method method) {

            mMethod = method;
        }

        @Override
        public Void run() {

            mMethod.setAccessible(true);

            return null;
        }
    }
}