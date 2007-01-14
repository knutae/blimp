package org.boblycat.blimp.tests;

import org.boblycat.blimp.PriorityCacheMap;
import org.junit.Test;
import static org.junit.Assert.*;

public class PriorityCacheMapTests {
    @Test
    public void testEmptyStringMap() {
        PriorityCacheMap<String, String> stringMap =
            new PriorityCacheMap<String, String>(10);
        assertNull(stringMap.get(null));
        assertNull(stringMap.get("key"));
        assertNull(stringMap.get(new Object()));
    }
    
    @Test
    public void testStringMapGetAndPut() {
        PriorityCacheMap<String, String> stringMap =
            new PriorityCacheMap<String, String>(10);
        stringMap.put("key", "value");
        assertEquals("value", stringMap.get("key"));
        assertEquals("value", stringMap.get(new String("key")));
        assertNull(stringMap.get("Key"));
    }
    
    @Test
    public void testStringMapEviction() {
        PriorityCacheMap<String, String> stringMap =
            new PriorityCacheMap<String, String>(2);
        // Note: cannot use interned strings for this test,
        // since they would not be gargage collected.
        // Hence the new String(...) calls.
        stringMap.put(new String("key1"), "value1");
        stringMap.put(new String("key2"), "value2");
        stringMap.put(new String("key3"), "value3");
        assertTrue(stringMap.isKeptAlive("key3"));
        assertTrue(stringMap.isKeptAlive("key2"));
        assertFalse(stringMap.isKeptAlive("key1"));
        // No GC yet, so key1 should be in the cache
        assertEquals("value1", stringMap.get("key1")); // safe?
        assertEquals("value2", stringMap.get("key2"));
        assertEquals("value3", stringMap.get("key3"));
        assertFalse(stringMap.isKeptAlive("key1"));
        // At this point, key1 should only be "weakly reachable",
        // so calling System.gc() should actually collect it.
        // This makes some assumptions about the JVM, but should
        // generally work with the default Hotspot JVM options.
        System.gc();
        assertNull(stringMap.get("key1")); // safe?
        assertEquals("value2", stringMap.get("key2"));
        assertEquals("value3", stringMap.get("key3"));
    }
}
