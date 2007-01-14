package org.boblycat.blimp;

import net.sourceforge.jiu.geometry.ScaleReplication;

public class ResizeLayer extends Layer {
    int width, height;
    boolean keepAspect;
    
    public ResizeLayer(int width, int height, boolean keepAspect) {
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
            resize.setSize(targetWidth, targetHeight);
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
        //ret.printDebugInfo("Invert dest");
        return ret;
    }
    
    public String getName() {
        return "Resize";
    }
}
