package org.boblycat.blimp;

/**
 * An input layer generates a bitmap from some source, such as a file.
 */
public abstract class InputLayer extends Layer {
    public abstract Bitmap getBitmap();
}
