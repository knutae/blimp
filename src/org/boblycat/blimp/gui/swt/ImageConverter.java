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

    public static ImageData jiuToSwtImageData(PixelImage pixelImage) {
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