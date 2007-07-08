package org.boblycat.blimp.layers;

import java.io.IOException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapSize;

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
