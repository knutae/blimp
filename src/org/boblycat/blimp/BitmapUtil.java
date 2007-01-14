package org.boblycat.blimp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.sun.imageio.plugins.jpeg.JPEGImageWriter;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;

public class BitmapUtil {
    private static byte[] getShortRGBData(RGB48Image rgb, int index1,
            int index2, int index3) {
        int width = rgb.getWidth();
        int height = rgb.getHeight();
        byte[] bytes = new byte[width * height * 3];
        short[] line1 = new short[width];
        short[] line2 = new short[width];
        short[] line3 = new short[width];
        int bytepos = 0;
        for (int y = 0; y < height; y++) {
            rgb.getShortSamples(index1, 0, y, width, 1, line1, 0);
            rgb.getShortSamples(index2, 0, y, width, 1, line2, 0);
            rgb.getShortSamples(index3, 0, y, width, 1, line3, 0);
            for (int x = 0; x < width; x++) {
                bytes[bytepos++] = (byte) ((line1[x] >> 8) & 0xff);
                bytes[bytepos++] = (byte) ((line2[x] >> 8) & 0xff);
                bytes[bytepos++] = (byte) ((line3[x] >> 8) & 0xff);
            }
        }
        return bytes;
    }

    private static byte[] getByteRGBData(RGB24Image rgb, int index1,
            int index2, int index3) {
        int width = rgb.getWidth();
        int height = rgb.getHeight();
        byte[] bytes = new byte[width * height * 3];
        byte[] line1 = new byte[width];
        byte[] line2 = new byte[width];
        byte[] line3 = new byte[width];
        int bytepos = 0;
        for (int y = 0; y < height; y++) {
            rgb.getByteSamples(index1, 0, y, width, 1, line1, 0);
            rgb.getByteSamples(index2, 0, y, width, 1, line2, 0);
            rgb.getByteSamples(index3, 0, y, width, 1, line3, 0);
            for (int x = 0; x < width; x++) {
                bytes[bytepos++] = line1[x];
                bytes[bytepos++] = line2[x];
                bytes[bytepos++] = line3[x];
            }
        }
        return bytes;
    }

    private static byte[] getRGBData(PixelImage pixelImage, int index1,
            int index2, int index3) {
        if (pixelImage instanceof RGB48Image)
            return getShortRGBData((RGB48Image) pixelImage, index1, index2,
                    index3);
        if (pixelImage instanceof RGB24Image)
            return getByteRGBData((RGB24Image) pixelImage, index1, index2,
                    index3);
        return null;
    }

    /**
     * Get the BGR image data as a byte array. If the color depth is 16 bit, the
     * least significant byte will be dropped.
     * 
     * @param pixelImage
     *            The image to convert.
     * @return A byte array of BGR triplets, or <code>null</code> if the image
     *         type is not supported. The size of the array will be
     *         width*height*3.
     */
    public static byte[] get8BitBGRData(PixelImage pixelImage) {
        return getRGBData(pixelImage, RGBIndex.INDEX_BLUE,
                RGBIndex.INDEX_GREEN, RGBIndex.INDEX_RED);
    }

    /**
     * Get the RGB image data as a byte array. If the color depth is 16 bit, the
     * least significant byte will be dropped.
     * 
     * @param pixelImage
     *            The image to convert.
     * @return A byte array of RGB triplets, or <code>null</code> if the image
     *         type is not supported. The size of the array will be
     *         width*height*3.
     */
    public static byte[] get8BitRGBData(PixelImage pixelImage) {
        return getRGBData(pixelImage, RGBIndex.INDEX_RED, RGBIndex.INDEX_GREEN,
                RGBIndex.INDEX_BLUE);
    }

    private static BufferedImage toAwtImage(PixelImage pixelImage) {
        BufferedImage awtImage = new BufferedImage(pixelImage.getWidth(),
                pixelImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        // bytes need to be RGB even though the type is BGR, not sure why
        byte[] bytes = get8BitRGBData(pixelImage);
        awtImage.getRaster().setDataElements(0, 0, pixelImage.getWidth(),
                pixelImage.getHeight(), bytes);
        return awtImage;
    }

    public static void writeBitmap(Bitmap bitmap, String formatName,
            String fileName, double quality) throws IOException {
        ImageWriter writer = getImageWriter(formatName);
        if (writer == null) {
            System.err.println("Failed to get image writer for format: "
                    + formatName);
            return;
        }
        ImageOutputStream output = ImageIO.createImageOutputStream(new File(
                fileName));
        writer.setOutput(output);
        ImageWriteParam param = null;
        if (writer instanceof JPEGImageWriter) {
            param = new JPEGImageWriteParam(null);
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) quality);
        }
        try {
            writer.write(null, toIIOImage(bitmap), param);
        }
        finally {
            output.close();
            writer.dispose();
        }
    }

    public static boolean canSaveToFormat(String formatName) {
        return getImageWriter(formatName) != null;
    }

    private static ImageWriter getImageWriter(String formatName) {
        Iterator<ImageWriter> iter = ImageIO
                .getImageWritersByFormatName(formatName);
        if (!iter.hasNext())
            return null;
        ImageWriter writer = iter.next();
        return writer;
    }

    private static IIOImage toIIOImage(Bitmap bitmap) {
        return new IIOImage(toAwtImage(bitmap.getImage()), null, null);
    }
}
