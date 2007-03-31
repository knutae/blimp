package org.boblycat.blimp.layers;

import java.io.IOException;

import org.boblycat.blimp.Bitmap;

/**
 * An input layer generates a bitmap from some source, such as a file.
 */
public abstract class InputLayer extends Layer {
    public abstract Bitmap getBitmap() throws IOException;
}
