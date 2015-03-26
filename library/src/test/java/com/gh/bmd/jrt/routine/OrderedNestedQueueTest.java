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
package com.gh.bmd.jrt.routine;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Ordered nested queue unit tests.
 * <p/>
 * Created by davide on 10/2/14.
 */
public class OrderedNestedQueueTest {

    @Test
    public void testAdd() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        queue.add(13);
        final NestedQueue<Integer> nested0 = queue.addNested();
        queue.add(7);
        final NestedQueue<Integer> nested1 = queue.addNested();
        nested1.addAll(Arrays.asList(11, 5));
        final NestedQueue<Integer> nested2 = nested1.addNested();
        nested2.add(-77);
        final NestedQueue<Integer> nested3 = nested2.addNested();
        nested3.add(-33);
        queue.add(1);

        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.removeFirst()).isEqualTo(13);
        assertThat(queue.isEmpty()).isTrue();
        nested0.close();
        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.removeFirst()).isEqualTo(7);
        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.removeFirst()).isEqualTo(11);
        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.removeFirst()).isEqualTo(5);
        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.removeFirst()).isEqualTo(-77);
        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.removeFirst()).isEqualTo(-33);
        assertThat(queue.isEmpty()).isTrue();
        nested1.close();
        assertThat(queue.isEmpty()).isTrue();
        nested3.close();
        assertThat(queue.isEmpty()).isTrue();
        nested2.close();
        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.removeFirst()).isEqualTo(1);
        assertThat(queue.isEmpty()).isTrue();
    }

    @Test
    public void testAddCloseError() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        queue.close();

        try {

            queue.add(1);

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            queue.addAll(Arrays.asList(1, 2, 3, 4));

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            queue.addNested();

            fail();

        } catch (final IllegalStateException ignored) {

        }
    }

    @Test
    public void testAddNestedError() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        NestedQueue<Integer> nested = queue.addNested();
        nested.addAll(Arrays.asList(1, 2, 3, 4));
        nested.close();
        nested.clear();

        try {

            queue.removeFirst();

            fail();

        } catch (final NoSuchElementException ignored) {

        }

        try {

            nested = queue.addNested();
            nested = nested.addNested();
            nested.close();
            nested.add(1);

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            nested = queue.addNested();
            nested = nested.addNested();
            nested.close();
            nested.addAll(Arrays.asList(1, 2, 3, 4));

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            nested = queue.addNested();
            nested = nested.addNested();
            nested.close();
            nested.addNested();

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            nested = queue.addNested();
            nested.close();
            nested.add(1);

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            nested = queue.addNested();
            nested.close();
            nested.addAll(Arrays.asList(1, 2, 3, 4));

            fail();

        } catch (final IllegalStateException ignored) {

        }

        try {

            nested = queue.addNested();
            nested.close();
            nested.addNested();

            fail();

        } catch (final IllegalStateException ignored) {

        }
    }

    @Test
    public void testClear() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        queue.add(13);
        queue.addNested();
        queue.add(7);
        NestedQueue<Integer> nested = queue.addNested();
        nested.addAll(Arrays.asList(11, 5));
        nested = nested.addNested();
        nested.add(-77);
        nested.addNested().add(-33);
        queue.add(1);

        nested.close();
        nested.clear();
        assertThat(queue.isEmpty()).isFalse();

        queue.clear();
        assertThat(queue.isEmpty()).isTrue();
    }

    @Test
    public void testMove() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        queue.add(13);
        final NestedQueue<Integer> nested0 = queue.addNested();
        queue.add(7);
        final NestedQueue<Integer> nested1 = queue.addNested();
        nested1.addAll(Arrays.asList(11, 5));
        final NestedQueue<Integer> nested2 = nested1.addNested();
        nested2.add(-77);
        final NestedQueue<Integer> nested3 = nested2.addNested();
        nested3.add(-33);
        queue.add(1);

        final ArrayList<Integer> list = new ArrayList<Integer>();

        queue.moveTo(list);
        assertThat(list).containsExactly(13);

        list.clear();
        nested0.close();
        queue.moveTo(list);
        assertThat(list).containsExactly(7, 11, 5, -77, -33);

        list.clear();
        nested3.close();
        queue.moveTo(list);
        assertThat(list).isEmpty();

        nested1.close();
        queue.moveTo(list);
        assertThat(list).isEmpty();

        nested2.close();
        queue.moveTo(list);
        assertThat(list).containsExactly(1);
    }

    @Test
    public void testMove2() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        queue.add(13);
        final NestedQueue<Integer> nested0 = queue.addNested();
        queue.add(7);
        final NestedQueue<Integer> nested1 = queue.addNested();
        nested1.addAll(Arrays.asList(11, 5));
        final NestedQueue<Integer> nested2 = nested1.addNested();
        nested2.add(-77);
        final NestedQueue<Integer> nested3 = nested2.addNested();
        nested3.add(-33);
        queue.add(1);

        nested0.close();
        nested1.close();
        nested2.close();
        nested3.close();

        final ArrayList<Integer> list = new ArrayList<Integer>();

        queue.moveTo(list);
        assertThat(list).containsExactly(13, 7, 11, 5, -77, -33, 1);
    }

    @Test
    public void testRemoveAllError() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        for (int i = 0; i < 7; i++) {

            queue.add(i);
        }

        for (int i = 0; i < 7; i++) {

            assertThat(queue.isEmpty()).isFalse();
            assertThat(queue.removeFirst()).isEqualTo(i);
        }

        try {

            queue.removeFirst();

            fail();

        } catch (final NoSuchElementException ignored) {

        }
    }

    @Test
    public void testRemoveClearError() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        for (int i = 0; i < 7; i++) {

            queue.add(i);
        }

        queue.clear();

        try {

            queue.removeFirst();

            fail();

        } catch (final NoSuchElementException ignored) {

        }
    }

    @Test
    public void testRemoveEmptyError() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        try {

            queue.removeFirst();

            fail();

        } catch (final NoSuchElementException ignored) {

        }
    }

    @Test
    public void testRemoveNestedError() {

        final OrderedNestedQueue<Integer> queue = new OrderedNestedQueue<Integer>();

        queue.addNested();

        try {

            queue.removeFirst();

            fail();

        } catch (final NoSuchElementException ignored) {

        }
    }
}
