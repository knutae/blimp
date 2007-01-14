package org.boblycat.blimp.layers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Vector;
//import net.sourceforge.jiu.codecs.PNMCodec;
import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.ColorDepth;
import org.boblycat.blimp.ColorSpace;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.jiu.PNMCodec;
import net.sourceforge.jiu.ops.OperationFailedException;

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
        // TODO: add many more, including custom white balance
        // Need camera-specific tables to make this user friendly.
    }
    
    private static Quality DEFAULT_QUALITY = Quality.Normal;
    
    Bitmap bitmap;
    String filePath;
    ColorDepth colorDepth;
    ColorSpace colorSpace;
    Quality quality;
    WhiteBalance whiteBalance;

    public RawFileInputLayer() {
        filePath = "";
        colorDepth = ColorDepth.Depth8Bit;
        colorSpace = ColorSpace.sRGB;
        quality = DEFAULT_QUALITY;
        whiteBalance = WhiteBalance.Camera;
    }

    public RawFileInputLayer(String filePath) {
        this();
        setFilePath(filePath);
        // load();
    }

    private String dcrawExecutable() {
        // TODO: make this configurable
        // return "c:/projects/java-imaging/build/dcraw.exe";
        String path = System.getProperty("blimp.dcraw.path");
        if (path == null || path.length() == 0)
            path = "dcraw";
        return path;
    }

    public void setFilePath(String filePath) {
        if (filePath != null && filePath.equals(this.filePath))
            return;
        this.filePath = filePath;
        bitmap = null;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setColorDepth(ColorDepth depth) {
        if (depth == colorDepth)
            return;
        colorDepth = depth;
        bitmap = null;
    }

    public ColorDepth getColorDepth() {
        if (colorDepth == null)
            colorDepth = ColorDepth.Depth8Bit;
        return colorDepth;
    }
    
    public void setQuality(Quality q) {
        if (q == null || q == quality)
            return;
        quality = q;
        bitmap = null;
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
            System.err
                    .println("Warning: unknown color space enum value, falling back to sRGB");
            return "1";
        }
    }

    public void load() {
        if (!isActive())
            return;
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
            
            commandLine.add("-c"); // write to stdout
            commandLine.add(filePath); // raw file
            
            ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
            Process process = processBuilder.start();
            try {
                PNMCodec codec = new PNMCodec();
                codec.setInputStream(new BufferedInputStream(process
                        .getInputStream()));
                codec.process();
                // System.out.println(codec.getImage().getClass());
                Bitmap tmpBitmap = new Bitmap();
                tmpBitmap.setImage(codec.getImage());
                bitmap = tmpBitmap;
            }
            finally {
                process.destroy();
            }
        }
        catch (IOException e) {
            System.err.println("Error executing dcraw or loading RAW file: "
                    + filePath);
            System.err.println(e.getMessage());
        }
        catch (OperationFailedException e) {
            System.err.println("Error reading RAW file: " + filePath);
            System.err.println(e.getMessage());
        }
        // catch (InterruptedException e) { e.printStackTrace(); }
    }

    public Bitmap getBitmap() {
        if (bitmap == null)
            load();
        return bitmap;
    }

    public String getDescription() {
        return Util.getFileNameFromPath(filePath);
    }

    public void setColorSpace(ColorSpace colorSpace) {
        if (colorSpace == null)
            colorSpace = ColorSpace.sRGB;
        if (colorSpace == this.colorSpace)
            return;
        this.colorSpace = colorSpace;
        bitmap = null;
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    public void setWhiteBalance(WhiteBalance whiteBalance) {
        if (whiteBalance == null || whiteBalance == this.whiteBalance)
            return;
        this.whiteBalance = whiteBalance;
        bitmap = null;
    }

    public WhiteBalance getWhiteBalance() {
        return whiteBalance;
    }
}
