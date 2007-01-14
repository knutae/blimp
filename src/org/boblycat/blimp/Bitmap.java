package org.boblycat.blimp;

import net.sourceforge.jiu.data.PixelImage;

/** A bitmap.  Currently just a wrapper for JIU's PixelImage. */
public class Bitmap {
    PixelImage image;
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
}