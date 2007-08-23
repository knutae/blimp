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
package org.boblycat.blimp.layers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Vector;

import javax.imageio.IIOException;
//import net.sourceforge.jiu.codecs.PNMCodec;
import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.ColorDepth;
import org.boblycat.blimp.ColorSpace;
import org.boblycat.blimp.Debug;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.jiu.PNMCodec;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.ProgressListener;

/**
 * A file input layer which supports many camera raw formats.
 * This is a wrapper for Dave Coffin's dcraw program.
 * 
 * @author Knut Arild Erstad
 */
public class RawFileInputLayer extends InputLayer {
    public enum Quality {
        HalfSize,
        Low,
        Normal,
        High,
    }
    
    public enum WhiteBalance {
        Camera, // as shot
        Auto,
        CustomRaw,
        // TODO: add many more, including custom white balance
        // Need camera-specific tables to make this user friendly.
    }
    
    private static Quality DEFAULT_QUALITY = Quality.HalfSize;
    private static ColorDepth DEFAULT_COLOR_DEPTH = ColorDepth.Depth16Bit;
    private static String dcrawPath = null;
    
    String filePath;
    ColorDepth colorDepth;
    ColorSpace colorSpace;
    Quality quality;
    WhiteBalance whiteBalance;
    double[] rawWhiteBalance;

    public RawFileInputLayer() {
        filePath = "";
        colorDepth = DEFAULT_COLOR_DEPTH;
        colorSpace = ColorSpace.sRGB;
        quality = DEFAULT_QUALITY;
        whiteBalance = WhiteBalance.Camera;
        
        rawWhiteBalance = new double[4];
        rawWhiteBalance[0] = 1.0; // R
        rawWhiteBalance[1] = 0.5; // G1
        rawWhiteBalance[2] = 1.0; // B
        rawWhiteBalance[3] = 0.0; // G2 (or zero)
    }

    public RawFileInputLayer(String filePath) {
        this();
        setFilePath(filePath);
        // load();
    }
    
    private static String findDcrawExecutable() {
        // Configured by system property
        String path = System.getProperty("blimp.dcraw.path");
        if (Util.fileExists(path))
            return path;
        if (path != null)
            Util.warn("blimp.dcraw.path is set, but was not found: " + path);

        // Embedded as a jar file resource (for Java Web Start).
        String dcrawExe;
        if (Util.isWindowsOS())
            dcrawExe = "blimp-dcraw.exe";
        else
            dcrawExe = "blimp-dcraw";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL dcrawUrl = classLoader.getResource(dcrawExe);
        if (dcrawUrl != null) {
            try {
                Util.info("Found dcraw as resource: " + dcrawUrl.toString());
                URLConnection conn = dcrawUrl.openConnection();
                InputStream istream = conn.getInputStream();
                File tempFile = File.createTempFile("blimp-dcraw", "exe", null);
                tempFile.deleteOnExit();
                FileOutputStream ostream = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int count = istream.read(buffer);
                while (count > 0) {
                    ostream.write(buffer, 0, count);
                    count = istream.read(buffer);
                }
                istream.close();
                ostream.close();
                tempFile.setExecutable(true);
                Util.info("Extracted dcraw to " + tempFile);
                return tempFile.toString();
            }
            catch (IOException e) {
                Util.err(e.getMessage());
            }
        }
        else {
            Util.warn("Failed to find dcraw executable as a resource");
        }

        // return file name without path and hope for the best
        Util.err("Failed to find or extract dcraw executable");
        return dcrawExe;
    }

    private static String dcrawExecutable() {
        if (dcrawPath == null)
            dcrawPath = findDcrawExecutable();
        return dcrawPath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setColorDepth(ColorDepth depth) {
        colorDepth = depth;
    }

    public ColorDepth getColorDepth() {
        if (colorDepth == null)
            colorDepth = ColorDepth.Depth8Bit;
        return colorDepth;
    }
    
    public void setQuality(Quality q) {
        quality = q;
    }
    
    public Quality getQuality() {
        return quality;
    }

    static String dcrawColorSpaceArgument(ColorSpace colorSpace) {
        if (colorSpace == ColorSpace.Uncalibrated)
            return "0";
        else if (colorSpace == ColorSpace.sRGB)
            return "1";
        else if (colorSpace == ColorSpace.Adobe)
            return "2";
        else if (colorSpace == ColorSpace.AdobeWide)
            return "3";
        else if (colorSpace == ColorSpace.ProPhoto)
            return "4";
        else if (colorSpace == ColorSpace.XYZ)
            return "5";
        else {
            Util.err("Warning: unknown color space enum value, falling back to sRGB");
            return "1";
        }
    }

    public Bitmap load() throws IOException {
        if (!isActive())
            return null;
        try {
            Vector<String> commandLine = new Vector<String>();
            commandLine.add(dcrawExecutable());

            // 8 or 16-bit depth
            if (getColorDepth() == ColorDepth.Depth16Bit)
                commandLine.add("-4");
            
            // color space, probably only sRGB makes sense for now 
            String colorSpaceArg = dcrawColorSpaceArgument(colorSpace);
            if (colorSpaceArg != null && colorSpaceArg.length() > 0) {
                commandLine.add("-o");
                commandLine.add(colorSpaceArg);
            }
            
            // interpolation quality (or half-size)
            if (quality == Quality.HalfSize) {
                commandLine.add("-h");
            }
            else if (quality == Quality.Low) {
                commandLine.add("-q");
                commandLine.add("0");
            }
            else if (quality == Quality.Normal) {
                commandLine.add("-q");
                commandLine.add("2");
            }
            else if (quality == Quality.High) {
                commandLine.add("-q");
                commandLine.add("3");
            }
            
            // white balance
            if (whiteBalance == WhiteBalance.Auto)
                commandLine.add("-a");
            else if (whiteBalance == WhiteBalance.Camera)
                commandLine.add("-w");
            else if (whiteBalance == WhiteBalance.CustomRaw) {
                commandLine.add("-r");
                for (int i=0; i<rawWhiteBalance.length; i++)
                    commandLine.add(Double.toString(rawWhiteBalance[i]));
            }
            
            commandLine.add("-c"); // write to stdout
            commandLine.add(filePath); // raw file
            
            Debug.print(this, commandLine.toString());
            
            ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
            Process process = processBuilder.start();
            try {
                PNMCodec codec = new PNMCodec();
                ProgressListener codecListener = new ProgressListener() {
                    public void setProgress(float progress) {
                        triggerProgress(getDescription(), progress);
                    }
                    public void setProgress(int index, int size) {
                        triggerProgress(getDescription(),
                                (double) index / (double) size);
                    }
                };
                codec.addProgressListener(codecListener);
                codec.setInputStream(new BufferedInputStream(process
                        .getInputStream()));
                codec.process();
                //Debug.print(this, codec.getImage().getClass().toString());
                Bitmap bitmap = new Bitmap();
                bitmap.setImage(codec.getImage());
                if (quality == Quality.HalfSize)
                    // compensate for half-size performed by dcraw 
                    bitmap.setPixelScaleFactor(2);
                return bitmap;
            }
            finally {
                process.destroy();
            }
        }
        catch (OperationFailedException e) {
            String message = "Failed to read raw file " + filePath + ".";
            Util.err(message);
            throw new IIOException(message, e);
        }
        // catch (InterruptedException e) { e.printStackTrace(); }
    }

    public Bitmap getBitmap() throws IOException {
        return load();
    }

    public String getDescription() {
        return Util.getFileNameFromPath(filePath);
    }
    
    public String getProgressDescription() {
        return Util.getFileNameFromPath(filePath) + " raw input";
    }

    public void setColorSpace(ColorSpace colorSpace) {
        if (colorSpace == null)
            colorSpace = ColorSpace.sRGB;
        this.colorSpace = colorSpace;
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    public void setWhiteBalance(WhiteBalance whiteBalance) {
        this.whiteBalance = whiteBalance;
    }

    public WhiteBalance getWhiteBalance() {
        return whiteBalance;
    }

    /**
     * Set the "raw" white balance, four weights which are applied before the raw
     * photo is decoded.  The interpretation of the weights depends on the
     * camera-specific raw format.
     * For cameras using a Bayer filter, this is RGBG weights, where the last
     * green value can be zero to use the same as the first.
     * This will only take effect if the whiteBalance property is CustomRaw.
     * @param newWhiteBalance An array of length four.
     */
    public void setRawWhiteBalance(double[] newWhiteBalance) {
        if (newWhiteBalance == null || newWhiteBalance.length != 4
                || Arrays.equals(newWhiteBalance, rawWhiteBalance))
            return;
        rawWhiteBalance = newWhiteBalance;
    }

    public double[] getRawWhiteBalance() {
        return rawWhiteBalance;
    }
}
