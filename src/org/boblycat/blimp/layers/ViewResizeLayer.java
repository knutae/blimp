package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;

import net.sourceforge.jiu.geometry.ScaleReplication;

/**
 * A fast resize layer for e.g. zooming in the image view.
 * 
 * @author Knut Arild Erstad
 */
public class ViewResizeLayer extends DimensionAdjustmentLayer {
    int width, height;
    boolean keepAspect;

    public ViewResizeLayer(int width, int height, boolean keepAspect) {
        this.width = width;
        this.height = height;
        this.keepAspect = keepAspect;
    }

    public Bitmap applyLayer(Bitmap source) {
        ScaleReplication resize = new ScaleReplication();
        resize.setInputImage(source.getImage());
        if (keepAspect) {
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            int targetHeight = height;
            int targetWidth = width;
            if (width * sourceHeight > height * sourceWidth) {
                targetWidth = targetHeight * sourceWidth / sourceHeight;
            }
            else {
                targetHeight = targetWidth * sourceHeight / sourceWidth;
            }
            if (targetWidth <= 0)
                targetWidth = 1;
            if (targetHeight <= 0)
                targetHeight = 1;
            resize.setSize(targetWidth, targetHeight);
            if (targetWidth == sourceWidth && targetHeight == sourceHeight)
                // optimize by returning the source
                return source;
        }
        else {
            resize.setSize(width, height);
        }
        try {
            resize.process();
        }
        catch (Exception e) {
            System.out.println("Resize exception: " + e.getMessage());
            return source;
        }
        Bitmap ret = new Bitmap(resize.getOutputImage());
        double scaleFactor = source.getWidth() / (double) ret.getWidth(); 
        ret.setPixelScaleFactor(source.getPixelScaleFactor() * scaleFactor);
        // ret.printDebugInfo("Invert dest");
        return ret;
    }
    
    public void setWidth(int w) {
        width = w;
    }
    
    public int getWidth() {
        return width;
    }

    public void setHeight(int h) {
        height = h;
    }
    
    public int getHeight() {
        return height;
    }

    public String getDescription() {
        return "Resize";
    }
}
