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
package com.gh.bmd.jrt.common;

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exceptions unit tests.
 * <p/>
 * Created by davide on 10/2/14.
 */
public class ExceptionTest extends TestCase {

    public void testExceptions() {

        assertThat(new DeadlockException("")).hasNoCause();
    }

    public void testRoutineException() {

        assertThat(new InvocationException(new NullPointerException())).isExactlyInstanceOf(
                InvocationException.class);
        assertThat(
                new InvocationException(new NullPointerException()).getCause()).isExactlyInstanceOf(
                NullPointerException.class);
        assertThat(new InvocationException(null)).hasNoCause();
    }

    public void testRoutineInterruptedException() {

        assertThat(
                new InvocationInterruptedException(new InterruptedException())).isExactlyInstanceOf(
                InvocationInterruptedException.class);
        assertThat(new InvocationInterruptedException(
                new InterruptedException()).getCause()).isExactlyInstanceOf(
                InterruptedException.class);
        assertThat(new InvocationInterruptedException(null)).hasNoCause();

        try {

            throw InvocationInterruptedException.interrupt(new InterruptedException());

        } catch (final InvocationInterruptedException ignored) {

        }
    }
}