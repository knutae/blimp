/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
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
package org.boblycat.blimp.data;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.boblycat.blimp.exif.ExifField;
import org.boblycat.blimp.exif.ExifTable;
import org.boblycat.blimp.exif.ExifTag;
import org.boblycat.blimp.exif.MetaDataUtil;
import org.boblycat.blimp.exif.ValidationError;
import org.boblycat.blimp.util.Util;

import net.sourceforge.jiu.data.MemoryRGB24Image;
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

    /**
     * Create a copy of the bitmap, reducing to 8-bit color depth if necessary.
     * @param bitmap The bitmap to copy.
     * @return A copy of the bitmap, containing a MemoryRGB24Image.
     */
    public static Bitmap create8BitCopy(Bitmap bitmap) {
        if (bitmap.getImage() == null)
            return new Bitmap();
        PixelImage inPixelImage = bitmap.getImage();
        int width = inPixelImage.getWidth();
        int height = inPixelImage.getHeight();
        MemoryRGB24Image outImage = new MemoryRGB24Image(width, height);
        if (inPixelImage instanceof RGB24Image) {
            RGB24Image inImage = (RGB24Image) inPixelImage;
            byte[] channelData = new byte[width*height];
            for (int channel=0; channel<outImage.getNumChannels(); channel++) {
                inImage.getByteSamples(channel, 0, 0, width, height,
                        channelData, 0);
                outImage.putByteSamples(channel, 0, 0, width, height,
                        channelData, 0);
            }
        }
        else if (inPixelImage instanceof RGB48Image) {
            RGB48Image inImage = (RGB48Image) inPixelImage;
            short[] inChannelData = new short[width*height];
            byte[] outChannelData = new byte[width*height];
            for (int channel=0; channel<outImage.getNumChannels(); channel++) {
                inImage.getShortSamples(channel, 0, 0, width, height,
                        inChannelData, 0);
                for (int i=0; i<inChannelData.length; i++) {
                    outChannelData[i] = (byte) ((inChannelData[i] >> 8) & 0xff);
                }
                outImage.putByteSamples(channel, 0, 0, width, height,
                        outChannelData, 0);
            }
        }
        else {
            Util.err("Unknown image class: " + inPixelImage.getClass().getName());
        }
        return new Bitmap(outImage);
    }

    public static BufferedImage toAwtImage(PixelImage pixelImage) {
        BufferedImage awtImage = new BufferedImage(pixelImage.getWidth(),
                pixelImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        // bytes need to be RGB even though the type is BGR, not sure why
        byte[] bytes = get8BitRGBData(pixelImage);
        awtImage.getRaster().setDataElements(0, 0, pixelImage.getWidth(),
                pixelImage.getHeight(), bytes);
        return awtImage;
    }

    public static void writeBitmap(Bitmap bitmap, String formatName,
            File fileName, double quality) throws IOException {
        writeBitmap(bitmap, formatName, fileName.toString(), quality);
    }

    public static void writeBitmap(Bitmap bitmap, String formatName,
            String fileName, double quality) throws IOException {
        ImageWriter writer = getImageWriter(formatName);
        if (writer == null) {
            throw new IOException("Failed to get image writer for format: "
                    + formatName);
        }
        ImageOutputStream output = ImageIO.createImageOutputStream(new File(fileName));
        if (output == null) {
            // For some strange reason, a null value can be returned instead of throwing
            // an IO exception.  Create an exception here instead.
            throw new IOException("Could not write to file " + fileName);
        }
        writer.setOutput(output);
        ImageWriteParam param = writer.getDefaultWriteParam();
        boolean isJpeg = (param instanceof JPEGImageWriteParam);
        if (isJpeg) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) quality);
        }
        IIOImage image = toIIOImage(bitmap);
        if (isJpeg && bitmap.getExifTable() != null) {
            RenderedImage rendered = image.getRenderedImage();
            if (rendered == null) {
                Util.err("Exif writer: failed to get rendered image");
            }
            else {
                ExifTable newTable = copyInterestingExifData(bitmap.getExifTable());
                IIOMetadata metadata = MetaDataUtil.generateExifMetaData(
                        writer, new ImageTypeSpecifier(rendered),
                        newTable);
                image.setMetadata(metadata);
                //CommandLine.printMetaData(metadata);
            }
        }
        try {
            writer.write(null, image, param);
        }
        finally {
            if (output != null)
                output.close();
            writer.dispose();
        }
    }

    public static boolean canSaveToFormat(String formatName) {
        return getImageWriter(formatName) != null;
    }

    public static ImageWriter getImageWriter(String formatName) {
        Iterator<ImageWriter> iter = ImageIO
                .getImageWritersByFormatName(formatName);
        if (!iter.hasNext())
            return null;
        ImageWriter writer = iter.next();
        return writer;
    }

    public static ImageReader getImageReader(String formatName) {
        Iterator<ImageReader> iter
            = ImageIO.getImageReadersByFormatName(formatName);
        if (!iter.hasNext())
            return null;
        ImageReader reader = iter.next();
        return reader;
    }

    private static IIOImage toIIOImage(Bitmap bitmap) {
        return new IIOImage(toAwtImage(bitmap.getImage()), null, null);
    }

    private static void copyTag(ExifTag tag, ExifTable fromTable, ExifTable toTable) {
        ExifField field = fromTable.get(tag);
        if (field != null)
            toTable.put(field);
    }

    private static void copyTags(ExifTable fromTable, ExifTable toTable,
            ExifTag[] tags) {
        for (ExifTag tag: tags) {
            copyTag(tag, fromTable, toTable);
        }
    }

    public static ExifTable copyInterestingExifData(ExifTable fromTable) {
        ExifTable newTable = new ExifTable();
        copyTags(fromTable, newTable, new ExifTag[] {
                ExifTag.XResolution,
                ExifTag.YResolution,
                ExifTag.ResolutionUnit,
                ExifTag.Make,
                ExifTag.Model,
                //ExifTag.ShutterSpeedValue,
                //ExifTag.ApertureValue,
                ExifTag.Flash,
                ExifTag.FlashEnergy,
                ExifTag.FNumber,
                ExifTag.ExposureTime,
                ExifTag.ISOSpeedRatings,
                ExifTag.MeteringMode,
                ExifTag.ExposureProgram,
                ExifTag.ExposureBiasValue,
                ExifTag.FocalLength,
                ExifTag.FocalLengthIn35mmFilm,
                ExifTag.SubjectDistance,
                ExifTag.LightSource,
                ExifTag.WhiteBalance,
                ExifTag.DateTime,
                ExifTag.DateTimeOriginal,
                ExifTag.DateTimeDigitized,
                ExifTag.SubsecTime,
                ExifTag.SubsecTimeOriginal,
                ExifTag.SubsecTimeDigitized,
                ExifTag.ImageDescription,
                ExifTag.Artist,
                ExifTag.Copyright,
                ExifTag.ImageUniqueID,
        });
        try {
            newTable.put(new ExifField(ExifTag.Software, "Blimp Photo Editor"));
        }
        catch (ValidationError e) {
            Util.err("Internal Exif error", e);
        }
        return newTable;
    }
}
