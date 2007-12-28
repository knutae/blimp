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

import java.io.IOException;

import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.InputLayer;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.layers.ResizeLayer;

public class CachedBlimpSession extends BlimpSession {
    BitmapCache cache;
    // some bitmaps are kept alive for speed purposes:
    Bitmap activeInputBitmap;
    Bitmap activeResizedBitmap;

    public CachedBlimpSession() {
        cache = new BitmapCache();
    }

    private void log(String msg) {
        if (Debug.debugEnabled(this)) {
            Debug.print(this, msg);
            cache.printSizes();
        }
    }

    protected Bitmap applyLayer(Bitmap source, AdjustmentLayer layer) {
        Bitmap bitmap = cache.get(source, layer);
        if (bitmap == null) {
            log("miss: " + layer.getClass());
            bitmap = super.applyLayer(source, layer);
            cache.put(source, layer, bitmap);
        }
        else {
            log("hit: " + layer.getClass());
        }
        if (layer instanceof ResizeLayer)
            activeResizedBitmap = bitmap;
        return bitmap;
    }

    protected Bitmap inputBitmap(InputLayer input) throws IOException {
        activeInputBitmap = null; // allow last input to be garbage collected
        Bitmap bitmap = cache.get(input);
        if (bitmap == null) {
            log("miss: " + input.getClass());
            bitmap = super.inputBitmap(input);
            cache.put(input, bitmap);
        }
        else {
            log("hit: " + input.getClass());

        }
        activeInputBitmap = bitmap;
        return bitmap;
    }

    protected BitmapSize inputSize(InputLayer input) throws IOException {
        Bitmap bitmap = cache.get(input);
        if (bitmap == null) {
            log("size miss: " + input.getClass());
            // TODO: if we get here, the bitmap may be loaded without being
            // inserted into the cache.  Should fix this somehow.
            return input.getBitmapSize();
        }
        else {
            log("size hit: " + input.getClass());
        }
        return bitmap.getSize();
    }

    private boolean hasActiveResizeLayer() {
        for (Layer layer: layerList)
            if (layer.isActive() && (layer instanceof ResizeLayer))
                return true;
        return false;
    }

    protected Bitmap generateBitmap(boolean useViewport) throws IOException {
        if (!hasActiveResizeLayer())
            activeResizedBitmap = null; // allow it to be garbage collected
        return super.generateBitmap(useViewport);
    }
}
