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

import java.io.File;
import java.util.logging.Logger;

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
    
    public static double constrainedValue(double value, double min, double max) {
        if (max < min)
            throw new IllegalArgumentException("max (" + max
                    + ") must be larger than min (" + min + ")");
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }
    
    public static int constrainedLower(int value, int min) {
        if (value < min)
            return min;
        return value;
    }
    
    /**
     * Returns the smallest of the given arguments.
     * 
     * @param <T>
     *      a comparable type
     * @param first
     *      the first argument
     * @param rest
     *      the rest of the arguments
     * @return
     *      the smallest object in the objects' natural order.
     */
    public static <T extends Comparable<? super T>> T min(T first, T... rest) {
        T ret = first;
        for (int i = 0; i < rest.length; i++) {
            if (ret.compareTo(rest[i]) > 0)
                ret = rest[i];
        }
        return ret;
    }

    /**
     * Returns the largest of the given arguments.
     * 
     * @param <T>
     *      a comparable type
     * @param first
     *      the first argument
     * @param rest
     *      the rest of the arguments
     * @return
     *      the largest object in the objects' natural order.
     */
    public static <T extends Comparable<? super T>> T max(T first, T... rest) {
        T ret = first;
        for (int i = 0; i < rest.length; i++) {
            if (ret.compareTo(rest[i]) < 0)
                ret = rest[i];
        }
        return ret;
    }

    public static String getFileNameFromPath(String path) {
        if (path == null || path.length() == 0)
            return "<No file>";
        File file = new File(path);
        return file.getName();
    }
    
    public static Logger logger = Logger.getLogger("org.boblycat.blimp");

    /**
     * Print a warning message.
     * @param message a warning message.
     */
    public static void warn(String message) {
        logger.warning(message);
    }
    
    /**
     * Print a non-fatal error message. 
     * @param message an error message.
     */
    public static void err(String message) {
        logger.severe(message);
    }
    
    /**
     * Print (log) an informational message.
     * @param message a message.
     */
    public static void info(String message) {
        logger.info(message);
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
    
    public static boolean fileExists(String path) {
        if (path == null || path.length() == 0)
            return false;
        File file = new File(path);
        return file.exists();
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
            err("Failed to open link: " + link);
        }
    }
    
    /**
     * Add or change a file name extension.
     * @param filename A file name or path, with or without an extension.
     * @param ext A file extension, not including a leading dot.
     * @return A file name ending with the given extension.
     */
    public static String changeFileExtension(String filename, String ext) {
        if (filename.toLowerCase().endsWith("." + ext.toLowerCase()))
            return filename;
        int lastDotPos = filename.lastIndexOf('.');
        int lastSlashPos = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        String suffix;
        if (ext.isEmpty())
            suffix = "";
        else
            suffix = '.' + ext;
        if (lastDotPos < 0 || lastSlashPos > lastDotPos)
            return filename + suffix;
        else
            return filename.substring(0, lastDotPos) + suffix;
    }
    
    /**
     * Perform an integer division which rounds the answer to the closest
     * whole number.
     * Note: currently only implemented propertly for positive integers.
     * @param dividend
     *      the number to divide, which must be larger or equal to zero.
     * @param divisor
     *      the divisor, which must be larger than zero.
     * @return
     *      the integer closest to the mathematical divison, rounded up
     *      if the result is exactly between two integers.
     */
    public static int roundDiv(int dividend, int divisor) {
        int div = dividend / divisor;
        int mod = dividend % divisor;
        if (mod * 2 >= divisor)
            div++;
        return div;
    }
}