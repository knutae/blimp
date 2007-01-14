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
    Bitmap bitmap;
    String filePath;
    ColorDepth colorDepth;
    ColorSpace colorSpace;

    public RawFileInputLayer() {
        filePath = "";
        colorDepth = ColorDepth.Depth8Bit;
        colorSpace = ColorSpace.sRGB;
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
            if (getColorDepth() == ColorDepth.Depth16Bit)
                commandLine.add("-4");
            String colorSpaceArg = dcrawColorSpaceArgument(colorSpace);
            if (colorSpaceArg != null && colorSpaceArg.length() > 0) {
                commandLine.add("-o");
                commandLine.add(colorSpaceArg);
            }
            commandLine.add("-h");
            commandLine.add("-c");
            commandLine.add(filePath);
            ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
            Process process = processBuilder.start();
            PNMCodec codec = new PNMCodec();
            codec.setInputStream(new BufferedInputStream(process
                    .getInputStream()));
            codec.process();
            // System.out.println(codec.getImage().getClass());
            Bitmap tmpBitmap = new Bitmap();
            tmpBitmap.setImage(codec.getImage());
            process.destroy();
            bitmap = tmpBitmap;
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

}
