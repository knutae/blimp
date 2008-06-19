/*
 * Copyright (C) 2007, 2008 Knut Arild Erstad
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.exif.ExifBlobReader;
import org.boblycat.blimp.exif.ExifTable;
import org.boblycat.blimp.exif.ReaderError;

/**
 * A base class for loading a bitmap from file.
 *
 * @author Knut Arild Erstad
 */
public abstract class FileInputLayer extends InputLayer {
    protected String filePath;

    public FileInputLayer() {
        filePath = "";
    }

    public FileInputLayer(String filePath) {
        setFilePath(filePath);
    }

    public void setFilePath(String path) {
        filePath = path;
    }

    public String getFilePath() {
        return filePath;
    }

    protected void tryLoadExifData(Bitmap bitmap) {
        try {
            ExifBlobReader reader = new ExifBlobReader(new File(filePath));
            ExifTable table = reader.extractIFDTable();
            bitmap.setExifTable(table);
            Util.info("Loaded Exif data from " + filePath);
        }
        catch (ReaderError e) {
            Util.info("No Exif data loaded from " + filePath);
        }
        catch (FileNotFoundException e) {
            Util.err("File not found while loading Exif data from " + filePath, e);
        }
        catch (IOException e) {
            Util.err("I/O error while loading Exif data from " + filePath, e);
        }
    }
}
