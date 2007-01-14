package org.boblycat.blimp;

import java.io.File;

public class Util {
    public static int constrainedValue(int value, int min, int max) {
        if (max < min)
            throw new IllegalArgumentException("max (" + max + ") must be larger than min (" + min + ")");
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }
    
    public static String getFileNameFromPath(String path) {
        if (path == null || path.length() == 0)
            return "<No file>";
        File file = new File(path);
        return file.getName();
    }
    
    public static void warn(String message) {
    	System.err.println("Warning: " + message);
    }
    
    private static boolean isRawFile(String path) {
    	int dotpos = path.lastIndexOf('.');
    	if (dotpos < 0)
    		return false;
    	String ext = path.substring(dotpos + 1).toLowerCase();
    	return ext.equals("raw") || ext.equals("crw") || ext.equals("cr2")
    		|| ext.equals("dng");
    	// todo: add more raw extensions
    }
    
    public static InputLayer getInputLayerFromFile(String filePath) {
    	if (isRawFile(filePath))
    		return new RawFileInputLayer(filePath);
    	else
    		return new FileInputLayer(filePath);
    }
}