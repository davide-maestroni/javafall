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

import junit.framework.TestCase;

import java.lang.reflect.Method;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Exception unit tests.
 * <p/>
 * Created by davide on 10/3/14.
 */
public class ExceptionTest extends TestCase {

    @SuppressWarnings("ConstantConditions")
    public void testRoutineInvocationException() throws NoSuchMethodException {

        final Method waitMethod = Object.class.getMethod("wait");

        assertThat(new RoutineInvocationException(new NullPointerException(), null, Object.class,
                                                  waitMethod.getName())).isExactlyInstanceOf(
                RoutineInvocationException.class);
        assertThat(new RoutineInvocationException(new NullPointerException(), this, Object.class,
                                                  waitMethod.getName()).getCause())
                .isExactlyInstanceOf(
                NullPointerException.class);
        assertThat(new RoutineInvocationException(null, null, Object.class,
                                                  waitMethod.getName())).hasNoCause();
        assertThat(new RoutineInvocationException(new NullPointerException(), this, Object.class,
                                                  waitMethod.getName()).getTarget()).isEqualTo(
                this);
        assertThat(new RoutineInvocationException(null, null, Object.class,
                                                  waitMethod.getName()).getTarget()).isNull();
        assertThat(new RoutineInvocationException(null, null, Object.class,
                                                  waitMethod.getName()).getTargetClass()
                                                                       .equals(Object.class))
                .isTrue();
        assertThat(new RoutineInvocationException(null, null, Object.class,
                                                  waitMethod.getName()).getMethodName()).isEqualTo(
                waitMethod.getName());
        assertThat(new RoutineInvocationException(null, null, Object.class,
                                                  waitMethod.getName()).getMethodParameterTypes()
        ).isEmpty();
        assertThat(new RoutineInvocationException(null, null, Object.class,
                                                  waitMethod.getName()).getMethod()).isEqualTo(
                waitMethod);

        try {

            throw new RoutineInvocationException(null, this, null, "test");

        } catch (final NullPointerException ignored) {

        }

        try {

            throw new RoutineInvocationException(null, this, Object.class, null);

        } catch (final NullPointerException ignored) {

        }

        try {

            throw new RoutineInvocationException(null, this, Object.class, "test",
                                                 (Class<?>[]) null);

        } catch (final NullPointerException ignored) {

        }
    }

    public void testRoutineNotAvailableException() {

        assertThat(new RoutineNotAvailableException()).hasNoCause();
    }
}