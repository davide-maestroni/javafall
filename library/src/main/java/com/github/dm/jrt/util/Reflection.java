/*
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
package com.github.dm.jrt.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Reflection utility class.
 * <p/>
 * Created by davide-maestroni on 09/09/2014.
 */
public class Reflection {

    /**
     * Constant defining an empty argument array for methods or constructors.
     */
    public static final Object[] NO_ARGS = new Object[0];

    private static final HashMap<Class<?>, Class<?>> sBoxingClasses =
            new HashMap<Class<?>, Class<?>>(9);

    /**
     * Avoid direct instantiation.
     */
    protected Reflection() {

    }

    /**
     * Returns the class boxing the specified primitive type.
     * <p/>
     * If the passed class does not represent a primitive type the same class is returned.
     *
     * @param type the primitive type.
     * @return the boxing class.
     */
    @Nonnull
    public static Class<?> boxingClass(@Nonnull final Class<?> type) {

        if (!type.isPrimitive()) {

            return type;
        }

        return sBoxingClasses.get(type);
    }

    /**
     * Finds the constructor of the specified class best matching the passed arguments.
     * <p/>
     * Note that clashing of signature is automatically avoided, since constructors are not
     * identified by their name. Hence the best match will be always unique in the class.
     *
     * @param type   the target class.
     * @param args   the constructor arguments.
     * @param <TYPE> the target type.
     * @return the best matching constructor.
     * @throws java.lang.IllegalArgumentException if no constructor taking the specified objects as
     *                                            parameters was found.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <TYPE> Constructor<TYPE> findConstructor(@Nonnull final Class<TYPE> type,
            @Nonnull final Object... args) {

        Constructor<?> constructor = findBestMatchingConstructor(type.getConstructors(), args);

        if (constructor == null) {

            constructor = findBestMatchingConstructor(type.getDeclaredConstructors(), args);

            if (constructor == null) {

                throw new IllegalArgumentException(
                        "no suitable constructor found for type: " + type.getName());
            }
        }

        return (Constructor<TYPE>) makeAccessible(constructor);
    }

    /**
     * Finds the method matching the specified parameters.
     * <p/>
     * Note that the returned method may not be accessible.
     *
     * @param type           the target class.
     * @param name           the method name.
     * @param parameterTypes the method parameter types.
     * @return the matching method.
     * @throws java.lang.IllegalArgumentException if no method matching the specified parameters was
     *                                            found.
     */
    @Nonnull
    public static Method findMethod(@Nonnull final Class<?> type, @Nonnull final String name,
            @Nonnull final Class<?>... parameterTypes) {

        Method method;

        try {

            method = type.getMethod(name, parameterTypes);

        } catch (final NoSuchMethodException ignored) {

            try {

                method = type.getDeclaredMethod(name, parameterTypes);

            } catch (final NoSuchMethodException e) {

                throw new IllegalArgumentException(e);
            }
        }

        return method;
    }

    /**
     * Checks if the specified class is static or is a top level class.
     *
     * @param type the class.
     * @return whether the class is static or a top level class.
     */
    public static boolean isStaticClass(@Nonnull final Class<?> type) {

        return ((type.getEnclosingClass() == null) || Modifier.isStatic(type.getModifiers()));
    }

    /**
     * Makes the specified constructor accessible.
     *
     * @param constructor the constructor instance.
     * @return the constructor.
     */
    @Nonnull
    public static Constructor<?> makeAccessible(@Nonnull final Constructor<?> constructor) {

        if (!constructor.isAccessible()) {

            AccessController.doPrivileged(new SetAccessibleConstructorAction(constructor));
        }

        return constructor;
    }

    /**
     * Makes the specified method accessible.
     *
     * @param method the method instance.
     * @return the method.
     */
    @Nonnull
    public static Method makeAccessible(@Nonnull final Method method) {

        if (!method.isAccessible()) {

            AccessController.doPrivileged(new SetAccessibleMethodAction(method));
        }

        return method;
    }

    @Nullable
    private static Constructor<?> findBestMatchingConstructor(
            @Nonnull final Constructor<?>[] constructors, @Nonnull final Object[] args) {

        final int argsLength = args.length;
        Constructor<?> bestMatch = null;
        int maxConfidence = 0;

        for (final Constructor<?> constructor : constructors) {

            final Class<?>[] params = constructor.getParameterTypes();
            final int length = params.length;

            if (length != argsLength) {

                continue;
            }

            boolean isValid = true;
            int confidence = 0;

            for (int i = 0; i < argsLength; ++i) {

                final Object contextArg = args[i];
                final Class<?> param = params[i];

                if (contextArg != null) {

                    final Class<?> boxingClass = boxingClass(param);

                    if (!boxingClass.isInstance(contextArg)) {

                        isValid = false;
                        break;
                    }

                    if (contextArg.getClass().equals(boxingClass)) {

                        ++confidence;
                    }

                } else if (param.isPrimitive()) {

                    isValid = false;
                    break;
                }
            }

            if (!isValid) {

                continue;
            }

            if ((bestMatch == null) || (confidence > maxConfidence)) {

                bestMatch = constructor;
                maxConfidence = confidence;

            } else if (confidence == maxConfidence) {

                throw new IllegalArgumentException(
                        "more than one constructor found for arguments: " + Arrays.toString(args));

            }
        }

        return bestMatch;
    }

    /**
     * Privileged action used to grant accessibility to a constructor.
     */
    private static class SetAccessibleConstructorAction implements PrivilegedAction<Void> {

        private final Constructor<?> mmConstructor;

        /**
         * Constructor.
         *
         * @param constructor the constructor instance.
         */
        private SetAccessibleConstructorAction(@Nonnull final Constructor<?> constructor) {

            mmConstructor = constructor;
        }

        public Void run() {

            mmConstructor.setAccessible(true);
            return null;
        }
    }

    /**
     * Privileged action used to grant accessibility to a method.
     */
    private static class SetAccessibleMethodAction implements PrivilegedAction<Void> {

        private final Method mMethod;

        /**
         * Constructor.
         *
         * @param method the method instance.
         */
        private SetAccessibleMethodAction(@Nonnull final Method method) {

            mMethod = method;
        }

        public Void run() {

            mMethod.setAccessible(true);
            return null;
        }
    }

    static {

        final HashMap<Class<?>, Class<?>> boxMap = sBoxingClasses;
        boxMap.put(boolean.class, Boolean.class);
        boxMap.put(byte.class, Byte.class);
        boxMap.put(char.class, Character.class);
        boxMap.put(double.class, Double.class);
        boxMap.put(float.class, Float.class);
        boxMap.put(int.class, Integer.class);
        boxMap.put(long.class, Long.class);
        boxMap.put(short.class, Short.class);
        boxMap.put(void.class, Void.class);
    }
}