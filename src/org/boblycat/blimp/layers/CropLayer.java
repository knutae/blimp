package org.boblycat.blimp.layers;

import net.sourceforge.jiu.geometry.Crop;
import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;

/**
 * Crop layer.
 * 
 * All values (left, right, top, bottom) are positive integers given in
 * pixels of the original image data, even if an earlier layer has resized
 * the image.
 *
 * @author Knut Arild Erstad
 */
public class CropLayer extends AdjustmentLayer {
    int left, right, top, bottom;

    @Override
    public Bitmap applyLayer(Bitmap source) {
        Crop crop = new Crop();
        double factor = source.getPixelScaleFactor();
        int cleft = (int) (left / factor);
        int cright = (int) (right / factor);
        int ctop = (int) (top / factor);
        int cbottom = (int) (bottom / factor);
        int x1 = cleft;
        int x2 = source.getWidth() - cright - 1;
        int y1 = ctop;
        int y2 = source.getHeight() - cbottom - 1;
        try {
            crop.setBounds(x1, y1, x2, y2);
        }
        catch (IllegalArgumentException e) {
            Util.err(e.getMessage());
            return new Bitmap(source.getImage());
        }
        return new Bitmap(applyJiuOperation(source.getImage(), crop));
    }

    @Override
    public String getDescription() {
        return "Crop";
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getLeft() {
        return left;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getRight() {
        return right;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getTop() {
        return top;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getBottom() {
        return bottom;
    }
}
