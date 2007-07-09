package org.boblycat.blimp;

import java.util.HashSet;
import java.util.Set;

/**
 * A static utility class for debugging.
 * 
 * @author Knut Arild Erstad
 */
public class Debug {
    private static Set<Class> classRegistry = new HashSet<Class>();
    
    private static String className(Object obj) {
        return obj.getClass().getSimpleName();
    }
    
    private static boolean isRegistered(Class c) {
        if (c == null)
            return false;
        else if (classRegistry.contains(c))
            return true;
        else
            return isRegistered(c.getSuperclass());
    }
    
    public static void register(Class c) {
        classRegistry.add(c);
    }
    
    /**
     * Test if the given object should be debugged.
     * @param obj an object
     * @return <code>true</code> if the object is registered for debugging,
     *  <code>false</code> otherwise.
     */
    public static boolean debugEnabled(Object obj) {
        return isRegistered(obj.getClass());
    }
    
    /**
     * Print a debug string if the given object is registered for debugging.
     * @param obj an object
     * @param str a debug string
     */
    public static void print(Object obj, String str) {
        if (debugEnabled(obj))
            Util.info(className(obj) + ": " + str);
    }
}
