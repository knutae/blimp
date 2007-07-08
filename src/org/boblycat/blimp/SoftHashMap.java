package org.boblycat.blimp;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
        Vector<V> vec = new Vector<V>(size());
        for (Entry entry: map.values())
            vec.add(entry.getValue());
        return vec;
    }
}
