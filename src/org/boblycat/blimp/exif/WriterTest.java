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
package org.boblycat.blimp.exif;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapUtil;
import org.boblycat.blimp.BlimpSession;
import org.boblycat.blimp.Serializer;
import org.boblycat.blimp.Util;

/**
 *
 *
 * @author Knut Arild Erstad
 */
public class WriterTest {
    private static void fatal(String message) {
        System.err.println(message);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            fatal("missing file name");
        String filename = args[0];
        BlimpSession session = Serializer.loadHistorySessionFromFile(filename);

        System.out.println("Generating bitmap");
        Bitmap bitmap = session.getFullBitmap();

        System.out.println("Writing JPEG");
        String imageFilename = Util.changeFileExtension(filename, "jpg");
        ImageWriter writer = BitmapUtil.getImageWriter("jpg");
        BufferedImage awtImage = BitmapUtil.toAwtImage(bitmap.getImage());
        IIOImage iioImage = new IIOImage(awtImage, null, null);
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(awtImage);

        ExifTable table = new ExifTable();
        table.put(new ExifField(ExifTag.Software, "Blimp Testing"));
        table.put(new ExifField(ExifTag.ShutterSpeedValue, new Rational(1, 100)));
        //table.put(new ExifField(ExifTag.BitsPerSample, "foo"));

        IIOMetadata metadata = MetaDataUtil.generateExifMetaData(
                writer, imageType, table);
        if (metadata == null) {
            System.out.println("no metadata");
        }
        else {
            CommandLine.printMetaData(metadata);
            iioImage.setMetadata(metadata);
        }

        ImageOutputStream output = ImageIO.createImageOutputStream(
                new File(imageFilename));
        writer.setOutput(output);
        writer.write(iioImage);

        System.out.println("Done.");
    }
}
