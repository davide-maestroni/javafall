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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Map implementation combining the features of {@link java.util.IdentityHashMap} and
 * {@link java.util.WeakHashMap}.
 * <p/>
 * Note that the map entries might change each time the object is explicitly accessed.
 * <p/>
 * Created by davide on 11/17/14.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class CacheHashMap<K, V> implements Map<K, V> {

    private final HashMap<IdentityWeakReference, V> mMap;

    private final ReferenceQueue<Object> mQueue = new ReferenceQueue<Object>();

    private AbstractSet<Entry<K, V>> mEntrySet;

    private AbstractSet<K> mKeySet;

    /**
     * Constructor.
     */
    public CacheHashMap() {

        mMap = new HashMap<IdentityWeakReference, V>();
    }

    /**
     * Constructor.
     *
     * @param map the initial content.
     * @throws NullPointerException if the specified map is null.
     * @see HashMap#HashMap(java.util.Map)
     */
    public CacheHashMap(@Nonnull final Map<? extends K, ? extends V> map) {

        mMap = new HashMap<IdentityWeakReference, V>(map.size());

        putAll(map);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial capacity.
     * @see HashMap#HashMap(int)
     */
    public CacheHashMap(final int initialCapacity) {

        mMap = new HashMap<IdentityWeakReference, V>(initialCapacity);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor      the load factor.
     * @see HashMap#HashMap(int, float)
     */
    public CacheHashMap(final int initialCapacity, final float loadFactor) {

        mMap = new HashMap<IdentityWeakReference, V>(initialCapacity, loadFactor);
    }

    @Override
    public int hashCode() {

        return mMap.hashCode();
    }

    @Override
    public int size() {

        cleanUp();

        return mMap.size();
    }

    @Override
    public boolean isEmpty() {

        cleanUp();

        return mMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object o) {

        cleanUp();

        return mMap.containsKey(new IdentityWeakReference(o));
    }

    @Override
    public boolean containsValue(final Object o) {

        cleanUp();

        return mMap.containsValue(o);
    }

    @Override
    public V get(final Object o) {

        cleanUp();

        return mMap.get(new IdentityWeakReference(o));
    }

    @Override
    public V put(final K k, final V v) {

        cleanUp();

        return mMap.put(new IdentityWeakReference(k, mQueue), v);
    }

    @Override
    public V remove(final Object o) {

        cleanUp();

        return mMap.remove(new IdentityWeakReference(o));
    }

    @Override
    public void putAll(@Nonnull final Map<? extends K, ? extends V> map) {

        cleanUp();

        final ReferenceQueue<Object> queue = mQueue;
        final HashMap<IdentityWeakReference, V> referenceMap = mMap;

        for (final Entry<? extends K, ? extends V> entry : map.entrySet()) {

            referenceMap.put(new IdentityWeakReference(entry.getKey(), queue), entry.getValue());
        }
    }

    @Override
    public void clear() {

        mMap.clear();
    }

    @Nonnull
    @Override
    public Set<K> keySet() {

        if (mKeySet == null) {

            mKeySet = new AbstractSet<K>() {

                @Nonnull
                @Override
                public Iterator<K> iterator() {

                    return new KeyIterator();
                }

                @Override
                public int size() {

                    return mMap.size();
                }
            };
        }

        return mKeySet;
    }

    @Nonnull
    @Override
    public Collection<V> values() {

        return mMap.values();
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {

        if (mEntrySet == null) {

            mEntrySet = new AbstractSet<Entry<K, V>>() {

                @Nonnull
                @Override
                public Iterator<Entry<K, V>> iterator() {

                    return new EntryIterator();
                }

                @Override
                public int size() {

                    return mMap.size();
                }
            };
        }

        return mEntrySet;
    }

    @SuppressWarnings("unchecked")
    private void cleanUp() {

        final HashMap<IdentityWeakReference, V> map = mMap;
        final ReferenceQueue<Object> queue = mQueue;

        IdentityWeakReference reference = (IdentityWeakReference) queue.poll();

        while (reference != null) {

            map.remove(reference);

            reference = (IdentityWeakReference) queue.poll();
        }
    }

    /**
     * Weak reference using object identity for comparison.
     */
    private static class IdentityWeakReference extends WeakReference<Object> {

        private final int mHashCode;

        /**
         * Constructor.
         *
         * @param referent the referent instance.
         * @param queue    the reference queue.
         * @see WeakReference#WeakReference(Object, ReferenceQueue)
         */
        public IdentityWeakReference(final Object referent,
                final ReferenceQueue<? super Object> queue) {

            super(referent, queue);

            mHashCode = System.identityHashCode(referent);
        }

        /**
         * Constructor.
         *
         * @param referent the referent instance.
         * @see WeakReference#WeakReference(Object)
         */
        private IdentityWeakReference(final Object referent) {

            super(referent);

            mHashCode = System.identityHashCode(referent);
        }

        @Override
        public int hashCode() {

            return mHashCode;
        }

        @Override
        public boolean equals(final Object obj) {

            if (this == obj) {

                return true;
            }

            if (!(obj instanceof IdentityWeakReference)) {

                return false;
            }

            final IdentityWeakReference that = (IdentityWeakReference) obj;

            if (mHashCode != that.mHashCode) {

                return false;
            }

            final Object referent = get();

            return (referent == that.get()) && ((referent != null) || (this == that));
        }
    }

    /**
     * Map entry iterator.
     */
    private class EntryIterator implements Iterator<Entry<K, V>> {

        private final Iterator<IdentityWeakReference> mIterator = mMap.keySet().iterator();

        @Override
        public boolean hasNext() {

            return mIterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {

            return new WeakEntry(mIterator.next());
        }

        @Override
        public void remove() {

            mIterator.remove();
        }
    }

    /**
     * Map key iterator.
     */
    private class KeyIterator implements Iterator<K> {

        private final Iterator<IdentityWeakReference> mIterator = mMap.keySet().iterator();

        @Override
        public boolean hasNext() {

            return mIterator.hasNext();
        }

        @Override
        @SuppressWarnings("unchecked")
        public K next() {

            return (K) mIterator.next().get();
        }

        @Override
        public void remove() {

            mIterator.remove();
        }
    }

    /**
     * Map entry implementation.
     */
    private class WeakEntry implements Entry<K, V> {

        private final IdentityWeakReference mReference;

        /**
         * Constructor.
         *
         * @param key the key reference.
         */
        private WeakEntry(@Nonnull final IdentityWeakReference key) {

            mReference = key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public K getKey() {

            return (K) mReference.get();
        }

        @Override
        public V getValue() {

            return mMap.get(mReference);
        }

        @Override
        public V setValue(final V v) {

            return mMap.put(mReference, v);
        }
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {

            return true;
        }

        if (!(o instanceof CacheHashMap)) {

            return (o instanceof Map) && o.equals(this);
        }

        final CacheHashMap that = (CacheHashMap) o;

        return mMap.equals(that.mMap);
    }
}