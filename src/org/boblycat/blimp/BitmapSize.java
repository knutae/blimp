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
