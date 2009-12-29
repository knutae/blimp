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
package org.boblycat.blimp.layers;

import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.PixelImage;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.data.TestBitmap;
import org.boblycat.blimp.layers.InputLayer;

public class TestInput extends InputLayer {
    String path;
    int width;
    int height;

    public TestInput() {
        width = 100;
        height = 100;
    }

    public void setFilePath(String newPath) {
        path = newPath;
    }

    public String getFilePath() {
        return path;
    }

    public void setInputSize(int w, int h) {
        width = w;
        height = h;
    }

    @Override
    public Bitmap getBitmap() {
        PixelImage image = new MemoryRGB24Image(width, height);
        TestBitmap bitmap = new TestBitmap(image);
        bitmap.creator = "TestInput";
        bitmap.testValue = "";
        return bitmap;
    }

    @Override
    public String getDescription() {
        return "Test Input";
    }
}
