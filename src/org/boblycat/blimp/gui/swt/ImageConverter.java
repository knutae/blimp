package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.Bitmap;
import org.eclipse.swt.graphics.*;
import net.sourceforge.jiu.data.*;
import net.sourceforge.jiu.codecs.*;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;

public class ImageConverter {
    static Image jiuToSwtImageViaPNG(Device device, PixelImage pixelImage)
        throws InvalidFileStructureException,
            MissingParameterException,
            OperationFailedException
    {
        // pixelImage.getAllocatedMemory() should be enough to avoid reallocs
        ByteArrayOutputStream outputStream =
            //new ByteArrayOutputStream(pixelImage.getAllocatedMemory());
            new ByteArrayOutputStream();

        // PNG supports everything we need
        PNGCodec codec = new PNGCodec();
        codec.setCompressionLevel(0); // fast, but uses more temp memory
        codec.setDataOutput(new DataOutputStream(outputStream));
        codec.setImage(pixelImage);
        codec.process();
        
        // Whee, we have a PNG in memory!  Now create an SWT image
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return new Image(device, inputStream);
    }
    
    static Image jiuToSwtImageViaPixels(Device device, PixelImage pixelImage)
        throws OperationFailedException
    {
    	if (pixelImage instanceof RGB48Image) {
    		RGB48Image rgb = (RGB48Image) pixelImage;
    		int width = rgb.getWidth();
    		int height = rgb.getHeight();
            byte[] bytes = new byte[width*height*3];
            PaletteData paletteData = new PaletteData(0xff, 0xff00, 0xff0000);
            ImageData data = new ImageData(width, height, 24, paletteData, 1, bytes);
            short[] redLine = new short[width];
            short[] greenLine = new short[width];
            short[] blueLine = new short[width];
            //System.out.println("length: " + data.data.length);
            //System.out.println("w*h*3 = " + width + "*" + height + "*3 = " + (width*height*3));
            int bytepos = 0;
            for (int y=0; y<height; y++) {
                rgb.getShortSamples(RGBIndex.INDEX_RED, 0, y, width, 1, redLine, 0);
                rgb.getShortSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1, greenLine, 0);
                rgb.getShortSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1, blueLine, 0);
                for (int x=0; x<width; x++) {
                    bytes[bytepos++] = (byte) ((blueLine[x] >> 8) & 0xff);
                    bytes[bytepos++] = (byte) ((greenLine[x] >> 8) & 0xff);
                    bytes[bytepos++] = (byte) ((redLine[x] >> 8) & 0xff);
                }
            }
            return new Image(device, data);
    	}
        if (pixelImage instanceof RGB24Image) {
            RGB24Image rgb = (RGB24Image) pixelImage;
            int width = rgb.getWidth();
            int height = rgb.getHeight();
            PaletteData paletteData = new PaletteData(0xff, 0xff00, 0xff0000);
            byte[] bytes = new byte[width*height*3];
            ImageData data = new ImageData(width, height, 24, paletteData, 1, bytes);
            byte[] redLine = new byte[width];
            byte[] greenLine = new byte[width];
            byte[] blueLine = new byte[width];
            //System.out.println("length: " + data.data.length);
            //System.out.println("w*h*3 = " + width + "*" + height + "*3 = " + (width*height*3));
            int bytepos = 0;
            for (int y=0; y<height; y++) {
                rgb.getByteSamples(RGBIndex.INDEX_RED, 0, y, width, 1, redLine, 0);
                rgb.getByteSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1, greenLine, 0);
                rgb.getByteSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1, blueLine, 0);
                for (int x=0; x<width; x++) {
                    bytes[bytepos++] = blueLine[x];
                    bytes[bytepos++] = greenLine[x];
                    bytes[bytepos++] = redLine[x];
                }
            }
            return new Image(device, data);
        }
        return null;
    }
    
    public static Image bitmapToSwtImage(Device device, Bitmap bitmap)
        throws InvalidFileStructureException,
            MissingParameterException,
            OperationFailedException
    {
        if (bitmap.getImage() == null)
            return null;
        Image image = jiuToSwtImageViaPixels(device, bitmap.getImage());
        if (image == null)
        	image = jiuToSwtImageViaPNG(device, bitmap.getImage());
        return image;
    }
}