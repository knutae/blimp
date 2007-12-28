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

import java.io.IOException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.gui.awt.ToolkitLoader;

/**
 * A file input layer which does not support camera raw formats.
 *
 * @author Knut Arild Erstad
 */
public class FileInputLayer extends InputLayer {
    String filePath;

    public FileInputLayer() {
        filePath = null;
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

    public Bitmap getBitmap() throws IOException {
        PixelImage image = ToolkitLoader.loadViaToolkitOrCodecs(filePath);
        if (image == null)
            throw new IOException("Failed to load image from " + filePath);
        return new Bitmap(image);
    }

    public String getDescription() {
        return Util.getFileNameFromPath(filePath);
    }
}