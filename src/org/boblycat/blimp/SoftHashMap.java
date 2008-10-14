/*
 * Copyright (C) 2007 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.boblycat.blimp;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A hash map in which the values are SoftReferences, which makes it suitable
 * for memory-sensitive caches.
 *
 * @author Knut Arild Erstad
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class SoftHashMap<K, V> implements Map<K, V> {
    public class Entry implements Map.Entry<K, V> {
        K key;
        SoftReference<V> valueRef;

        Entry(K key, V value) {
            this.key = key;
            valueRef = new SoftReference<V>(value);
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return valueRef.get();
        }

        public V setValue(V value) {
            SoftReference<V> oldRef = valueRef;
            valueRef = new SoftReference<V>(value);
            return oldRef.get();
        }
    }

    HashMap<K, Entry> map;

    private final V entryValue(Entry entry) {
        if (entry == null)
            return null;
        return entry.getValue();
    }

    public SoftHashMap() {
        map = new HashMap<K, Entry>();
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        for (Entry e: map.values()) {
            if (value.equals(e.getValue()))
                return true;
        }
        return false;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return new HashSet<Map.Entry<K,V>>(map.values());
    }

    public V get(Object key) {
        return entryValue(map.get(key));
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public V put(K key, V value) {
       return entryValue(map.put(key, new Entry(key, value)));
    }

    public void putAll(Map<? extends K, ? extends V> otherMap) {
        for (Map.Entry<? extends K, ? extends V> entry: otherMap.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public V remove(Object key) {
        return entryValue(map.remove(key));
    }

    public int size() {
        return map.size();
    }

    public Collection<V> values() {
        ArrayList<V> vec = new ArrayList<V>(size());
        for (Entry entry: map.values())
            vec.add(entry.getValue());
        return vec;
    }
}
