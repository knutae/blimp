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

    public static String fixPointDecimalToString(int value, int digits) {
    	if (value < 0)
    		return '-' + fixPointDecimalToString(-value, digits);
		String ivalue = Integer.toString(value);
		if (digits <= 0)
			return ivalue;
		int len = ivalue.length();
		if (len <= digits) {
			StringBuffer buf = new StringBuffer("0.");
			for (int i=len; i<digits; i++)
				buf.append('0');
			buf.append(ivalue);
			return buf.toString();
		}
		return ivalue.substring(0, len-digits) + '.'
			+ ivalue.substring(len-digits);
	}
    
    public static int valueOfFixPointDecimal(String str, int digits)
    throws NumberFormatException {
    	// TODO: avoid using float as an intermediate value
    	float fval = Float.valueOf(str);
    	for (int i=0; i<digits; i++)
    		fval *= 10;
    	return Math.round(fval);
    }
	
}