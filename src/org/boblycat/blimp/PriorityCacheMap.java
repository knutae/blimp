package org.boblycat.blimp;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;


/**
 * A wrapper around a weak hash map which also guarantees that the last accessed
 * elements, up to a certain capacity, are not garbage collected.
 * 
 * @author Knut Arild Erstad
 *
 * @param <K>
 * @param <V>
 */
public class PriorityCacheMap<K, V> implements Map<K, V> {
    public class Entry implements Map.Entry<K, V> {
        int priority;
        WeakReference<K> keyRef;
        V value;
        
        Entry(K key, V value) {
            this.keyRef = new WeakReference<K>(key);
            this.value = value;
            //this.priority = ++priorityCounter;
            this.priority = -1;
        }
        
        public K getKey() {
            return keyRef.get();
        }
        
        public V getValue() {
            return value;
        }
        
        public V setValue(V val) {
            V oldVal = value;
            value = val;
            return oldVal;
        }
    }
    
    private class PriorityQueueComparator implements Comparator<K> {
        public int compare(K key1, K key2) {
            Entry entry1 = weakEntryMap.get(key1);
            Entry entry2 = weakEntryMap.get(key2);
            assert(entry1 != null);
            assert(entry2 != null);
            return entry1.priority - entry2.priority;
        }
    }

    private int priorityCounter;
    private int capacity;
    private WeakHashMap<K, Entry> weakEntryMap;
    private PriorityQueue<K> keepAliveQueue;
    
    private void promotePriority(Entry entry) {
        entry.priority = ++priorityCounter;
        keepAliveQueue.offer(entry.getKey());
        if (keepAliveQueue.size() > capacity)
            keepAliveQueue.remove();
        assert(keepAliveQueue.size() <= capacity);
    }
    
    public PriorityCacheMap(int capacity) {
        this.capacity = capacity;
        priorityCounter = 0;
        weakEntryMap = new WeakHashMap<K, Entry>();
        keepAliveQueue = new PriorityQueue<K>(capacity+1,
                new PriorityQueueComparator());
    }
    
    public void clear() {
        weakEntryMap.clear();
        keepAliveQueue.clear();
    }

    public boolean containsKey(Object key) {
        return weakEntryMap.containsKey(key);
    }

    public boolean containsValue(Object val) {
        for (Entry entry: weakEntryMap.values())
            if (val.equals(entry.value))
                return true;
        return false;
    }

    public Set<Map.Entry<K,V>> entrySet() {
        return new HashSet<Map.Entry<K,V>>(weakEntryMap.values());
    }

    public V get(Object key) {
        Entry entry = weakEntryMap.get(key);
        if (entry == null)
            return null;
        promotePriority(entry);
        return entry.value;
    }

    public boolean isEmpty() {
        return weakEntryMap.isEmpty();
    }

    public Set<K> keySet() {
        return weakEntryMap.keySet();
    }

    public V put(K key, V value) {
        V oldValue = null;
        Entry entry = weakEntryMap.get(key);
        if (entry != null)
            oldValue = entry.setValue(value);
        else {
            entry = new Entry(key, value);
            weakEntryMap.put(key, entry);
        }
        promotePriority(entry);
        return oldValue;
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry: map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public V remove(Object key) {
        Entry entry = weakEntryMap.remove(key);
        if (entry == null)
            return null;
        keepAliveQueue.remove(key);
        return entry.value;
    }

    public int size() {
        return weakEntryMap.size();
    }

    public Collection<V> values() {
        Vector<V> vec = new Vector<V>();
        for (Entry entry: weakEntryMap.values())
            vec.add(entry.value);
        return vec;
    }
    
    public boolean isKeptAlive(K key) {
        return keepAliveQueue.contains(key);
    }
}
