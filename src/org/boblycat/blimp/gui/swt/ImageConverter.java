package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapUtil;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.ImageLoader;

import net.sourceforge.jiu.data.*;
import net.sourceforge.jiu.codecs.*;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;

public class ImageConverter {
    static Image jiuToSwtImageViaPNG(Device device, PixelImage pixelImage)
            throws InvalidFileStructureException, MissingParameterException,
            OperationFailedException {
        // pixelImage.getAllocatedMemory() should be enough to avoid reallocs
        ByteArrayOutputStream outputStream =
        // new ByteArrayOutputStream(pixelImage.getAllocatedMemory());
        new ByteArrayOutputStream();

        // PNG supports everything we need
        PNGCodec codec = new PNGCodec();
        codec.setCompressionLevel(0); // fast, but uses more temp memory
        codec.setDataOutput(new DataOutputStream(outputStream));
        codec.setImage(pixelImage);
        codec.process();

        // Whee, we have a PNG in memory! Now create an SWT image
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                outputStream.toByteArray());
        return new Image(device, inputStream);
    }

    static ImageData jiuToSwtImageData(PixelImage pixelImage) {
        byte[] bytes = BitmapUtil.get8BitBGRData(pixelImage);
        if (bytes == null)
            return null;
        PaletteData paletteData = new PaletteData(0xff, 0xff00, 0xff0000);
        ImageData data = new ImageData(
                pixelImage.getWidth(), pixelImage.getHeight(),
                24, paletteData, 1, bytes);
        return data;
    }

    static Image jiuToSwtImageViaPixels(Device device, PixelImage pixelImage) {
        ImageData data = jiuToSwtImageData(pixelImage);
        if (data != null)
            return new Image(device, data);
        return null;
    }

    public static Image bitmapToSwtImage(Device device, Bitmap bitmap)
            throws InvalidFileStructureException, MissingParameterException,
            OperationFailedException {
        if (bitmap.getImage() == null)
            return null;
        Image image = jiuToSwtImageViaPixels(device, bitmap.getImage());
        if (image == null)
            image = jiuToSwtImageViaPNG(device, bitmap.getImage());
        return image;
    }

    public static void saveBitmap(Bitmap bitmap, String filename, int format) {
        ImageData data = jiuToSwtImageData(bitmap.getImage());
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[1];
        loader.data[0] = data;
        loader.save(filename, format);

    }
}