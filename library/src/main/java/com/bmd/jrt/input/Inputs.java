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
package com.bmd.jrt.input;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Helper class providing utility methods to convert native arrays into immutable object lists.
 * <p/>
 * Created by davide on 9/9/14.
 *
 * @param <DATA> the list element type.
 */
public class Inputs<DATA> implements List<DATA> {

    private final List<Object> mList;

    private final Class<DATA> mType;

    /**
     * Avoid direct instantiation.
     *
     * @param type    the list element type.
     * @param wrapped the wrapped list.
     */
    private Inputs(@NonNull final Class<DATA> type, @NonNull final List<Object> wrapped) {

        mType = type;
        mList = wrapped;
    }

    /**
     * Returns an input list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable input list.
     */
    @NonNull
    public static Inputs<Boolean> asList(@Nullable final boolean... inputs) {

        return new Inputs<Boolean>(boolean.class, asObjects(inputs));
    }

    /**
     * Returns an input list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable input list.
     */
    @NonNull
    public static Inputs<Byte> asList(@Nullable final byte... inputs) {

        return new Inputs<Byte>(byte.class, asObjects(inputs));
    }

    /**
     * Returns an input list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable input list.
     */
    @NonNull
    public static Inputs<Character> asList(@Nullable final char... inputs) {

        return new Inputs<Character>(char.class, asObjects(inputs));
    }

    /**
     * Returns an input list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable input list.
     */
    @NonNull
    public static Inputs<Double> asList(@Nullable final double... inputs) {

        return new Inputs<Double>(double.class, asObjects(inputs));
    }

    /**
     * Returns an input list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable input list.
     */
    @NonNull
    public static Inputs<Float> asList(@Nullable final float... inputs) {

        return new Inputs<Float>(float.class, asObjects(inputs));
    }

    /**
     * Returns an input list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable input list.
     */
    @NonNull
    public static Inputs<Integer> asList(@Nullable final int... inputs) {

        return new Inputs<Integer>(int.class, asObjects(inputs));
    }

    /**
     * Returns an input list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable input list.
     */
    @NonNull
    public static Inputs<Long> asList(@Nullable final long... inputs) {

        return new Inputs<Long>(long.class, asObjects(inputs));
    }

    /**
     * Returns an input list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable input list.
     */
    @NonNull
    public static Inputs<Short> asList(@Nullable final short... inputs) {

        return new Inputs<Short>(short.class, asObjects(inputs));
    }

    /**
     * Returns a list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable list.
     */
    @NonNull
    public static List<Object> asObjects(@Nullable final boolean... inputs) {

        if ((inputs == null) || (inputs.length == 0)) {

            return Collections.emptyList();
        }

        final int length = inputs.length;
        final Object[] array = new Object[length];

        for (int i = 0; i < length; i++) {

            array[i] = inputs[i];
        }

        return Arrays.asList(array);
    }

    /**
     * Returns a list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable list.
     */
    @NonNull
    public static List<Object> asObjects(@Nullable final byte... inputs) {

        if ((inputs == null) || (inputs.length == 0)) {

            return Collections.emptyList();
        }

        final int length = inputs.length;
        final Object[] array = new Object[length];

        for (int i = 0; i < length; i++) {

            array[i] = inputs[i];
        }

        return Arrays.asList(array);
    }

    /**
     * Returns a list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable list.
     */
    @NonNull
    public static List<Object> asObjects(@Nullable final char... inputs) {

        if ((inputs == null) || (inputs.length == 0)) {

            return Collections.emptyList();
        }

        final int length = inputs.length;
        final Object[] array = new Object[length];

        for (int i = 0; i < length; i++) {

            array[i] = inputs[i];
        }

        return Arrays.asList(array);
    }

    /**
     * Returns a list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable list.
     */
    @NonNull
    public static List<Object> asObjects(@Nullable final double... inputs) {

        if ((inputs == null) || (inputs.length == 0)) {

            return Collections.emptyList();
        }

        final int length = inputs.length;
        final Object[] array = new Object[length];

        for (int i = 0; i < length; i++) {

            array[i] = inputs[i];
        }

        return Arrays.asList(array);
    }

    /**
     * Returns a list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable list.
     */
    @NonNull
    public static List<Object> asObjects(@Nullable final float... inputs) {

        if ((inputs == null) || (inputs.length == 0)) {

            return Collections.emptyList();
        }

        final int length = inputs.length;
        final Object[] array = new Object[length];

        for (int i = 0; i < length; i++) {

            array[i] = inputs[i];
        }

        return Arrays.asList(array);
    }

    /**
     * Returns a list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable list.
     */
    @NonNull
    public static List<Object> asObjects(@Nullable final int... inputs) {

        if ((inputs == null) || (inputs.length == 0)) {

            return Collections.emptyList();
        }

        final int length = inputs.length;
        final Object[] array = new Object[length];

        for (int i = 0; i < length; i++) {

            array[i] = inputs[i];
        }

        return Arrays.asList(array);
    }

    /**
     * Returns a list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable list.
     */
    @NonNull
    public static List<Object> asObjects(@Nullable final long... inputs) {

        if ((inputs == null) || (inputs.length == 0)) {

            return Collections.emptyList();
        }

        final int length = inputs.length;
        final Object[] array = new Object[length];

        for (int i = 0; i < length; i++) {

            array[i] = inputs[i];
        }

        return Arrays.asList(array);
    }

    /**
     * Returns a list containing the specified native data.
     *
     * @param inputs the data to fill the list with.
     * @return the newly created immutable list.
     */
    @NonNull
    public static List<Object> asObjects(@Nullable final short... inputs) {

        if ((inputs == null) || (inputs.length == 0)) {

            return Collections.emptyList();
        }

        final int length = inputs.length;
        final Object[] array = new Object[length];

        for (int i = 0; i < length; i++) {

            array[i] = inputs[i];
        }

        return Arrays.asList(array);
    }

    @Override
    public int size() {

        return mList.size();
    }

    @Override
    public boolean isEmpty() {

        return mList.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {

        return mList.contains(o);
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Iterator<DATA> iterator() {

        return (Iterator<DATA>) mList.iterator();
    }

    @Override
    @NonNull
    public Object[] toArray() {

        return mList.toArray();
    }

    @Override
    @NonNull
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(@NonNull final T[] a) {

        return mList.toArray(a);
    }

    @Override
    public boolean add(final DATA e) {

        return mList.add(e);
    }

    @Override
    public boolean remove(final Object o) {

        return mList.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull final Collection<?> c) {

        return mList.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull final Collection<? extends DATA> c) {

        return mList.addAll(c);
    }

    @Override
    public boolean addAll(final int index, @NonNull final Collection<? extends DATA> c) {

        return mList.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NonNull final Collection<?> c) {

        return mList.removeAll(c);
    }

    @Override
    public boolean retainAll(@NonNull final Collection<?> c) {

        return mList.retainAll(c);
    }

    @Override
    public void clear() {

        mList.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public DATA get(final int index) {

        return (DATA) mList.get(index);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DATA set(final int index, final DATA element) {

        return (DATA) mList.set(index, element);
    }

    @Override
    public void add(final int index, final DATA element) {

        mList.add(index, element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DATA remove(final int index) {

        return (DATA) mList.remove(index);
    }

    @Override
    public int indexOf(final Object o) {

        return mList.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {

        return mList.lastIndexOf(o);
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public ListIterator<DATA> listIterator() {

        return (ListIterator<DATA>) mList.listIterator();
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public ListIterator<DATA> listIterator(final int index) {

        return (ListIterator<DATA>) mList.listIterator(index);
    }

    @Override
    @NonNull
    public Inputs<DATA> subList(final int fromIndex, final int toIndex) {

        return new Inputs<DATA>(mType, mList.subList(fromIndex, toIndex));
    }

    /**
     * Transforms this list in a list of booleans.
     *
     * @return a newly created input list.
     */
    @NonNull
    public Inputs<Boolean> toBooleans() {

        final List<Object> list = mList;
        final Class<DATA> type = mType;

        if (boolean.class.equals(type) || list.isEmpty()) {

            return new Inputs<Boolean>(boolean.class, list);
        }

        final int size = list.size();
        final Object[] array = new Object[size];

        if (char.class.equals(type)) {

            for (int i = 0; i < size; i++) {

                array[i] = (((Character) list.get(i)) != 0);
            }

        } else {

            for (int i = 0; i < size; i++) {

                array[i] = (((Number) list.get(i)).longValue() != 0);
            }
        }

        return new Inputs<Boolean>(boolean.class, Arrays.asList(array));
    }

    /**
     * Transforms this list in a list of bytes.
     *
     * @return a newly created input list.
     */
    @NonNull
    public Inputs<Byte> toBytes() {

        final List<Object> list = mList;
        final Class<DATA> type = mType;

        if (byte.class.equals(type) || list.isEmpty()) {

            return new Inputs<Byte>(byte.class, list);
        }

        final int size = list.size();
        final Object[] array = new Object[size];

        if (boolean.class.equals(type)) {

            for (int i = 0; i < size; i++) {

                array[i] = (byte) (((Boolean) list.get(i)) ? 1 : 0);
            }

        } else if (char.class.equals(mType)) {

            for (int i = 0; i < size; i++) {

                array[i] = (byte) ((Character) list.get(i)).charValue();
            }

        } else {

            for (int i = 0; i < size; i++) {

                array[i] = ((Number) list.get(i)).byteValue();
            }
        }

        return new Inputs<Byte>(byte.class, Arrays.asList(array));
    }

    /**
     * Transforms this list in a list of chars.
     *
     * @return a newly created input list.
     */
    @NonNull
    public Inputs<Character> toCharacters() {

        final List<Object> list = mList;
        final Class<DATA> type = mType;

        if (char.class.equals(type) || list.isEmpty()) {

            return new Inputs<Character>(char.class, list);
        }

        final int size = list.size();
        final Object[] array = new Object[size];

        if (boolean.class.equals(type)) {

            for (int i = 0; i < size; i++) {

                array[i] = (char) (((Boolean) list.get(i)) ? 1 : 0);
            }

        } else {

            for (int i = 0; i < size; i++) {

                array[i] = (char) ((Number) list.get(i)).shortValue();
            }
        }

        return new Inputs<Character>(char.class, Arrays.asList(array));
    }

    /**
     * Transforms this list in a list of doubles.
     *
     * @return a newly created input list.
     */
    @NonNull
    public Inputs<Double> toDoubles() {

        final List<Object> list = mList;
        final Class<DATA> type = mType;

        if (double.class.equals(type) || list.isEmpty()) {

            return new Inputs<Double>(double.class, list);
        }

        final int size = list.size();
        final Object[] array = new Object[size];

        if (boolean.class.equals(type)) {

            for (int i = 0; i < size; i++) {

                array[i] = (double) (((Boolean) list.get(i)) ? 1 : 0);
            }

        } else if (char.class.equals(mType)) {

            for (int i = 0; i < size; i++) {

                array[i] = (double) ((Character) list.get(i));
            }

        } else {

            for (int i = 0; i < size; i++) {

                array[i] = ((Number) list.get(i)).doubleValue();
            }
        }

        return new Inputs<Double>(double.class, Arrays.asList(array));
    }

    /**
     * Transforms this list in a list of floats.
     *
     * @return a newly created input list.
     */
    @NonNull
    public Inputs<Float> toFloats() {

        final List<Object> list = mList;
        final Class<DATA> type = mType;

        if (float.class.equals(type) || list.isEmpty()) {

            return new Inputs<Float>(float.class, list);
        }

        final int size = list.size();
        final Object[] array = new Object[size];

        if (boolean.class.equals(type)) {

            for (int i = 0; i < size; i++) {

                array[i] = (float) (((Boolean) list.get(i)) ? 1 : 0);
            }

        } else if (char.class.equals(mType)) {

            for (int i = 0; i < size; i++) {

                array[i] = (float) ((Character) list.get(i));
            }

        } else {

            for (int i = 0; i < size; i++) {

                array[i] = ((Number) list.get(i)).floatValue();
            }
        }

        return new Inputs<Float>(float.class, Arrays.asList(array));
    }

    /**
     * Transforms this list in a list of integers.
     *
     * @return a newly created input list.
     */
    @NonNull
    public Inputs<Integer> toIntegers() {

        final List<Object> list = mList;
        final Class<DATA> type = mType;

        if (int.class.equals(type) || list.isEmpty()) {

            return new Inputs<Integer>(int.class, list);
        }

        final int size = list.size();
        final Object[] array = new Object[size];

        if (boolean.class.equals(type)) {

            for (int i = 0; i < size; i++) {

                array[i] = (((Boolean) list.get(i)) ? 1 : 0);
            }

        } else if (char.class.equals(mType)) {

            for (int i = 0; i < size; i++) {

                array[i] = (int) ((Character) list.get(i));
            }

        } else {

            for (int i = 0; i < size; i++) {

                array[i] = ((Number) list.get(i)).intValue();
            }
        }

        return new Inputs<Integer>(int.class, Arrays.asList(array));
    }

    /**
     * Transforms this list in a list of longs.
     *
     * @return a newly created input list.
     */
    @NonNull
    public Inputs<Long> toLongs() {

        final List<Object> list = mList;
        final Class<DATA> type = mType;

        if (long.class.equals(type) || list.isEmpty()) {

            return new Inputs<Long>(long.class, list);
        }

        final int size = list.size();
        final Object[] array = new Object[size];

        if (boolean.class.equals(type)) {

            for (int i = 0; i < size; i++) {

                array[i] = (long) (((Boolean) list.get(i)) ? 1 : 0);
            }

        } else if (char.class.equals(mType)) {

            for (int i = 0; i < size; i++) {

                array[i] = (long) ((Character) list.get(i));
            }

        } else {

            for (int i = 0; i < size; i++) {

                array[i] = ((Number) list.get(i)).longValue();
            }
        }

        return new Inputs<Long>(long.class, Arrays.asList(array));
    }

    /**
     * Transforms this list in a list of objects.
     *
     * @return an immutable list.
     */
    @NonNull
    public List<Object> toObjects() {

        return mList;
    }

    /**
     * Transforms this list in a list of shorts.
     *
     * @return a newly created input list.
     */
    @NonNull
    public Inputs<Short> toShorts() {

        final List<Object> list = mList;
        final Class<DATA> type = mType;

        if (short.class.equals(type) || list.isEmpty()) {

            return new Inputs<Short>(short.class, list);
        }

        final int size = list.size();
        final Object[] array = new Object[size];

        if (boolean.class.equals(type)) {

            for (int i = 0; i < size; i++) {

                array[i] = (short) (((Boolean) list.get(i)) ? 1 : 0);
            }

        } else if (char.class.equals(mType)) {

            for (int i = 0; i < size; i++) {

                array[i] = (short) ((Character) list.get(i)).charValue();
            }

        } else {

            for (int i = 0; i < size; i++) {

                array[i] = ((Number) list.get(i)).shortValue();
            }
        }

        return new Inputs<Short>(short.class, Arrays.asList(array));
    }
}