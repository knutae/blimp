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

import java.io.IOException;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.data.BitmapSize;

/**
 * An input layer generates a bitmap from some source, such as a file.
 */
public abstract class InputLayer extends Layer {
    /**
     * All input layers must override and implement this.
     * @return
     *      A new bitmap.  If the loading failed, implementors should
     *      throw an I/O exception instead of returning <code>null</code>.
     * @throws IOException
     *      If loading the bitmap failed for some reason.
     */
    public abstract Bitmap getBitmap() throws IOException;

    /**
     * Returns the size of the bitmap which is to be loaded.
     * The default implementation calls getBitmap() and returns its
     * size, but it is recommended to override this function and
     * provide a more efficient implementation.
     *
     * @return
     *      The size of the new bitmap.
     * @throws IOException
     *      If loading the bitmap (or meta info) failed for some reason.
     */
    public BitmapSize getBitmapSize() throws IOException {
        Bitmap bm = getBitmap();
        if (bm == null)
            return null;
        return bm.getSize();
    }
}
