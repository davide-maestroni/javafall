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
package com.gh.bmd.jrt.invocation;

import com.gh.bmd.jrt.core.InvocationTarget;

import org.junit.Test;

import static com.gh.bmd.jrt.core.InvocationTarget.targetClass;
import static com.gh.bmd.jrt.core.InvocationTarget.targetObject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Invocation target unit test.
 * <p/>
 * Created by davide-maestroni on 22/08/15.
 */
public class InvocationTargetTest {

    @Test
    public void testClassTarget() {

        final InvocationTarget target = targetClass(TargetClass.class);
        assertThat(target.getTarget()).isSameAs(TargetClass.class);
        assertThat(target.getTargetClass()).isSameAs(TargetClass.class);
        assertThat(target.isAssignableTo(TargetClass.class)).isTrue();
        assertThat(target.isAssignableTo(TestClass.class)).isTrue();
        assertThat(target.isAssignableTo(String.class)).isFalse();
    }

    @Test
    public void testClassTargetEquals() {

        final InvocationTarget target = targetClass(TargetClass.class);
        assertThat(target).isEqualTo(target);
        assertThat(target).isNotEqualTo("");
        assertThat(target.hashCode()).isEqualTo(targetClass(TargetClass.class).hashCode());
        assertThat(target).isEqualTo(targetClass(TargetClass.class));
        assertThat(target.hashCode()).isNotEqualTo(targetClass(TestClass.class).hashCode());
        assertThat(target).isNotEqualTo(targetClass(TestClass.class));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testClassTargetError() {

        try {

            targetClass(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    @Test
    public void testObjectTarget() {

        final TargetClass t = new TargetClass();
        final InvocationTarget target = targetObject(t);
        assertThat(target.getTarget()).isSameAs(t);
        assertThat(target.getTargetClass()).isSameAs(TargetClass.class);
        assertThat(target.isAssignableTo(TargetClass.class)).isTrue();
        assertThat(target.isAssignableTo(TestClass.class)).isTrue();
        assertThat(target.isAssignableTo(String.class)).isFalse();
    }

    @Test
    public void testObjectTargetEquals() {

        final TargetClass t = new TargetClass();
        final InvocationTarget target = targetObject(t);
        assertThat(target).isEqualTo(target);
        assertThat(target).isNotEqualTo("");
        assertThat(target.hashCode()).isEqualTo(targetObject(t).hashCode());
        assertThat(target).isEqualTo(targetObject(t));
        assertThat(target.hashCode()).isNotEqualTo(targetObject(new TestClass()).hashCode());
        assertThat(target).isNotEqualTo(targetObject(new TestClass()));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testObjectTargetError() {

        try {

            targetObject(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    private static class TargetClass extends TestClass {

    }

    private static class TestClass {

    }
}