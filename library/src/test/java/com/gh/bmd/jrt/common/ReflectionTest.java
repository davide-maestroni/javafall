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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reflection utils unit tests.
 * <p/>
 * Created by davide on 10/4/14.
 */
public class ReflectionTest extends TestCase {

    public void testBoxingClass() {

        assertThat(Reflection.boxingClass(null)).isNull();
        assertThat(Void.class.equals(Reflection.boxingClass(void.class))).isTrue();
        assertThat(Integer.class.equals(Reflection.boxingClass(int.class))).isTrue();
        assertThat(Byte.class.equals(Reflection.boxingClass(byte.class))).isTrue();
        assertThat(Boolean.class.equals(Reflection.boxingClass(boolean.class))).isTrue();
        assertThat(Character.class.equals(Reflection.boxingClass(char.class))).isTrue();
        assertThat(Short.class.equals(Reflection.boxingClass(short.class))).isTrue();
        assertThat(Long.class.equals(Reflection.boxingClass(long.class))).isTrue();
        assertThat(Float.class.equals(Reflection.boxingClass(float.class))).isTrue();
        assertThat(Double.class.equals(Reflection.boxingClass(double.class))).isTrue();
        assertThat(TestCase.class.equals(Reflection.boxingClass(TestCase.class))).isTrue();
    }

    public void testConstructor() {

        assertThat(Reflection.findConstructor(TestClass.class)).isNotNull();
        assertThat(Reflection.findConstructor(TestClass.class, "test")).isNotNull();

        try {

            Reflection.findConstructor(TestClass.class, 4);

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        try {

            Reflection.findConstructor(TestClass.class, "test", 4);

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        assertThat(
                Reflection.findConstructor(TestClass.class, new ArrayList<String>())).isNotNull();

        try {

            Reflection.findConstructor(TestClass.class, (Object) null);

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class TestClass {

        public TestClass() {

        }

        public TestClass(final String ignored) {

        }

        public TestClass(final int ignored) {

        }

        public TestClass(final Integer ignored) {

        }

        private TestClass(final LinkedList<String> ignored) {

        }

        private TestClass(final ArrayList<String> ignored) {

        }

        private TestClass(final List<String> ignored) {

        }
    }
}