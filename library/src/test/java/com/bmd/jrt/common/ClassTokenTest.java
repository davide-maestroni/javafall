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
package com.bmd.jrt.common;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Class token unit tests.
 * <p/>
 * Created by davide on 6/15/14.
 */
public class ClassTokenTest extends TestCase {

    public void testEquals() {

        final ClassToken<String> classToken1 = new ClassToken<String>() {};

        assertThat(classToken1).isEqualTo(classToken1);
        assertThat(classToken1).isEqualTo(new StringClassToken());
        assertThat(classToken1).isEqualTo(new SubStringClassToken());
        //noinspection ObjectEqualsNull
        assertThat(classToken1.equals(null)).isFalse();

        final ClassToken<List<String>> classToken2 = new ClassToken<List<String>>() {};

        assertThat(classToken2.isAssignableFrom(new ClassToken<List<String>>() {})).isTrue();
        assertThat(classToken2.isAssignableFrom(new ClassToken<List<Integer>>() {})).isTrue();
        assertThat(classToken2.isAssignableFrom(new ClassToken<ArrayList<String>>() {})).isTrue();

        assertThat(ClassToken.tokenOf(List.class)).isEqualTo(new ClassToken<List>() {});
        assertThat(ClassToken.classOf(new ArrayList())).isEqualTo(new ClassToken<ArrayList>() {});
        assertThat(ClassToken.classOf(new ArrayList<String>())).isEqualTo(
                new ClassToken<ArrayList<String>>() {});
    }

    @SuppressWarnings("ConstantConditions")
    public void testError() {

        try {

            ClassToken.tokenOf(null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            ClassToken.classOf(null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new TestClassToken<List<Integer>>().getRawClass();

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new SubTestClassToken().getRawClass();

            fail();

        } catch (final Exception ignored) {

        }
    }

    public void testType() {

        final ClassToken<String> classToken1 = new ClassToken<String>() {};

        assertThat(classToken1.getGenericType()).isEqualTo(String.class);
        assertThat(String.class.equals(classToken1.getRawClass())).isTrue();
        assertThat(classToken1.isInterface()).isFalse();

        final ClassToken<ArrayList<String>> classToken2 = new ClassToken<ArrayList<String>>() {};

        assertThat(classToken2.getGenericType()).isNotEqualTo(ArrayList.class);
        assertThat(ArrayList.class.equals(classToken2.getRawClass())).isTrue();
        assertThat(classToken1.isInterface()).isFalse();

        final ClassToken<List<String>> classToken3 = new ClassToken<List<String>>() {};

        assertThat(classToken3.getGenericType()).isNotEqualTo(List.class);
        assertThat(List.class.equals(classToken3.getRawClass())).isTrue();
        assertThat(classToken3.isInterface()).isTrue();

        final ClassToken<List<ArrayList<String>>> classToken4 =
                new ClassToken<List<ArrayList<String>>>() {};

        assertThat(classToken4.getGenericType()).isNotEqualTo(List.class);
        assertThat(List.class.equals(classToken4.getRawClass())).isTrue();
        assertThat(classToken4.isInterface()).isTrue();
    }

    private static class StringClassToken extends ClassToken<String> {

    }

    private static class SubStringClassToken extends StringClassToken {

    }

    private static class SubTestClassToken extends TestClassToken<List<Integer>> {

    }

    private static class TestClassToken<TEST extends List> extends ClassToken<TEST> {

    }
}