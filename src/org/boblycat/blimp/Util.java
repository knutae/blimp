package org.boblycat.blimp;

import java.io.File;

import org.boblycat.blimp.layers.FileInputLayer;
import org.boblycat.blimp.layers.InputLayer;
import org.boblycat.blimp.layers.RawFileInputLayer;

import edu.stanford.ejalbert.BrowserLauncher;

public class Util {
    public static int constrainedValue(int value, int min, int max) {
        if (max < min)
            throw new IllegalArgumentException("max (" + max
                    + ") must be larger than min (" + min + ")");
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

    /**
     * Print a warning message.
     * @param message a warning message.
     */
    public static void warn(String message) {
        System.err.println("Warning: " + message);
    }
    
    /**
     * Print a non-fatal error message. 
     * @param message an error message.
     */
    public static void err(String message) {
        System.err.println("Error: " + message);
    }

    public static String getFileExtension(String filePath) {
        int dotpos = filePath.lastIndexOf('.');
        if (dotpos < 0)
            return "";
        return filePath.substring(dotpos + 1).toLowerCase();
    }

    private static boolean isRawFile(String path) {
        String ext = getFileExtension(path);
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
            for (int i = len; i < digits; i++)
                buf.append('0');
            buf.append(ivalue);
            return buf.toString();
        }
        return ivalue.substring(0, len - digits) + '.'
                + ivalue.substring(len - digits);
    }

    public static int valueOfFixPointDecimal(String str, int digits)
            throws NumberFormatException {
        // TODO: avoid using float as an intermediate value
        float fval = Float.valueOf(str);
        for (int i = 0; i < digits; i++)
            fval *= 10;
        return Math.round(fval);
    }

    public static byte cropToUnsignedByte(int signedIntValue) {
        if (signedIntValue < 0)
            signedIntValue = 0;
        else if (signedIntValue > 255)
            signedIntValue = 255;
        return (byte) (0xff & signedIntValue);
    }

    public static short cropToUnsignedShort(int signedIntValue) {
        final int UNSIGNED_SHORT_MAX = 65535;
        if (signedIntValue < 0)
            signedIntValue = 0;
        else if (signedIntValue > UNSIGNED_SHORT_MAX)
            signedIntValue = UNSIGNED_SHORT_MAX;
        return (short) (0xffff & signedIntValue);
    }

    /** Detect if the OS is MS Windows */
    public static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static void openLinkInBrowser(String link) {
        try {
            // For some reason, BrowserLauncher can be really slow on windows,
            // so avoid it if possible (sigh!)
            if (isWindowsOS()) {
                Runtime rt = Runtime.getRuntime();
                rt.exec(new String[] { "cmd", "/c", "start " + link });
            }
            else {
                BrowserLauncher launcher = new BrowserLauncher(null);
                launcher.openURLinBrowser(link);
            }
        }
        catch (Exception e) {
            System.err.println("Failed to open link: " + link);
        }
    }
}