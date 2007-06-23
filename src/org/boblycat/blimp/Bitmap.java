package org.boblycat.blimp;

import net.sourceforge.jiu.data.PixelImage;

/**
 * A bitmap. Wraps JIU's PixelImage and adds Blimp-specific fields.
 */
public class Bitmap {
    PixelImage image;
    
    double pixelScaleFactor;

    static final boolean debug = false;

    public PixelImage getImage() {
        return this.image;
    }

    public void setImage(PixelImage image) {
        this.image = image;
    }

    public Bitmap(PixelImage image) {
        this.image = image;
    }

    public Bitmap() {
    }

    public int getWidth() {
        if (image == null)
            return -1;
        return image.getWidth();
    }

    public int getHeight() {
        if (image == null)
            return -1;
        return image.getHeight();
    }
    
    public BitmapSize getSize() {
        if (image == null)
            return null;
        return new BitmapSize(image.getWidth(), image.getHeight(),
                pixelScaleFactor);
    }

    public int getChannelBitDepth() {
        if (image == null)
            return -1;
        return image.getBitsPerPixel() / image.getNumChannels();
    }

    public void printDebugInfo(String prefix) {
        if (!debug)
            return;
        System.out.print(prefix + " ");
        if (image == null) {
            System.out.println("(null)");
            return;
        }
        System.out.println("yep");
    }

    /**
     * Set the pixel size factor, which describes how large each pixel is
     * compared to a pixel in the original input data.
     * 
     * Input layers only need to set this value if the layer itself can
     * perform scaling compared to the raw image data.
     * 
     * Layers that scale the image should always set this value based upon
     * the value of the input bitmap and how much it was scaled.  This can
     * usually be calculated as the source size divided by the new width.
     * Note that changing the aspect ratio is not currently supported.
     * 
     * See also getPixelScaleFactor().
     * 
     * @param pixelSizeFactor A new pixel scale factor, normally larger than 0.
     */
    public void setPixelScaleFactor(double pixelScaleFactor) {
        this.pixelScaleFactor = pixelScaleFactor;
    }

    /**
     * Returns a number describing how large each pixel is compared to a pixel
     * in the original input data.
     * 
     * Layers that can be configured to use specific pixel size/radius
     * should normally specify this using the original bitmap's size and
     * can divide it by this number to find a corresponding new size on the
     * scaled input image.
     * 
     * @return A pixel size factor.
     */
    public double getPixelScaleFactor() {
        return pixelScaleFactor;
    }
}