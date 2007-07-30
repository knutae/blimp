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
package org.boblycat.blimp;

/**
 * A class for size information about a bitmap.
 * 
 * @author Knut Arild Erstad
 */
public class BitmapSize {
    /**
     * Width in pixels.
     */
    public int width;
    /**
     * Height in pixels.
     */
    public int height;
    /**
     * The scale factor of a pixel in the bitmap compared to the a pixel in the
     * original source image.
     */
    public double pixelScaleFactor;
    
    public BitmapSize(int w, int h) {
        this(w, h, 1.0);
    }
    
    public BitmapSize(int w, int h, double scale) {
        width = w;
        height = h;
        pixelScaleFactor = scale;
    }
    
    public int scaledWidth() {
        if (pixelScaleFactor <= 0)
            return width;
        else
            return (int) Math.round(pixelScaleFactor * width);
    }
    
    public int scaledHeight() {
        if (pixelScaleFactor <= 0)
            return height;
        else
            return (int) Math.round(pixelScaleFactor * height);
    }
}
