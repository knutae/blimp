package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.ZoomFactor;

import net.sourceforge.jiu.geometry.ScaleReplication;

/**
 * A fast resize layer for e.g. zooming in the image view.
 * 
 * @author Knut Arild Erstad
 */
public class ViewResizeLayer extends DimensionAdjustmentLayer {
    int viewWidth;
    int viewHeight;
    int imageWidth;
    int imageHeight;
    ZoomFactor zoomFactor;

    public ViewResizeLayer() {
        zoomFactor = new ZoomFactor();
        viewWidth = 100;
        viewHeight = 100;
    }

    private void setImageSize(int w, int h) {
        if (w != imageWidth || h != imageHeight)
            // invalidate current zoom when size changes
            zoomFactor = getAutoZoomFactor(w, h);
        imageWidth = w;
        imageHeight = h;
    }
    
    private ZoomFactor getAutoZoomFactor(int imageWidth, int imageHeight) {
        ZoomFactor autoZoomFactor = new ZoomFactor();
        while (autoZoomFactor.scale(imageWidth) > viewWidth
                || autoZoomFactor.scale(imageHeight) > viewHeight)
            autoZoomFactor.zoomOut();
        return autoZoomFactor;
    }

    public Bitmap applyLayer(Bitmap source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        setImageSize(sourceWidth, sourceHeight);
        ScaleReplication resize = new ScaleReplication();
        resize.setInputImage(source.getImage());
        int width = zoomFactor.scale(sourceWidth);
        int height = zoomFactor.scale(sourceHeight);
        if (width == sourceWidth && height == sourceHeight)
            // Optimize by returning the source.
            // Some unit tests also depends on this behaviour
            return source;
        
        resize.setSize(width, height);
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
        return ret;
    }
    
    public ZoomFactor zoom() {
        return zoomFactor;
    }

    public String getDescription() {
        return "View Resize";
    }

    public double getZoomValue() {
        return zoomFactor.toDouble();
    }
    
    public void setZoomValue(double value) {
        // Currently this function is only here in order to get the bean
        // serialization to work, needed for the bitmap cache
        // TODO: either implement the function, or find a different strategy
    }

    public void setViewWidth(int viewWidth) {
        this.viewWidth = viewWidth;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public void setViewHeight(int viewHeight) {
        this.viewHeight = viewHeight;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        int w = zoomFactor.scale(inputSize.width);
        int h = zoomFactor.scale(inputSize.height);
        double scaleFactor = inputSize.width / (double) w;
        return new BitmapSize(w, h, scaleFactor * inputSize.pixelScaleFactor);
    }
}
