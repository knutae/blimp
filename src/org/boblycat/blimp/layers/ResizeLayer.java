package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.geometry.Resample;

public class ResizeLayer extends AdjustmentLayer {
    public enum Filter {
        BSpline  { int getType() { return Resample.FILTER_TYPE_B_SPLINE; } },
        Bell     { int getType() { return Resample.FILTER_TYPE_BELL; } },
        // Box     { int getType() { return Resample.FILTER_TYPE_BOX; } },
        Hermite  { int getType() { return Resample.FILTER_TYPE_HERMITE; } },
        Lanczos3 { int getType() { return Resample.FILTER_TYPE_LANCZOS3; } },
        Mitchell { int getType() { return Resample.FILTER_TYPE_MITCHELL; } },
        Triangle { int getType() { return Resample.FILTER_TYPE_TRIANGLE; } };

        abstract int getType();
    }

    static final Filter DEFAULT_FILTER = Filter.Lanczos3;

    static final int DEFAULT_SIZE = 640;

    Filter resampleFilter;

    int maxSize;

    // double radius; // TODO: add this
    public ResizeLayer() {
        resampleFilter = DEFAULT_FILTER;
        maxSize = DEFAULT_SIZE;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        PixelImage input = source.getImage();
        Resample resampleOp = new Resample();
        resampleOp.setFilter(resampleFilter.getType());
        int width, height;
        if (input.getWidth() > input.getHeight()) {
            width = maxSize;
            height = maxSize * input.getHeight() / input.getWidth();
        }
        else {
            height = maxSize;
            width = maxSize * input.getWidth() / input.getHeight();
        }
        resampleOp.setSize(width, height);
        Bitmap bitmap = new Bitmap(applyJiuOperation(input, resampleOp));
        double scaleFactor = source.getWidth() / (double) width;
        bitmap.setPixelScaleFactor(source.getPixelScaleFactor() * scaleFactor);
        return bitmap;
    }

    @Override
    public String getDescription() {
        return "Resize Image";
    }

    public void setResampleFilter(Filter resampleFilter) {
        if (resampleFilter == null)
            resampleFilter = DEFAULT_FILTER;
        this.resampleFilter = resampleFilter;
    }

    public Filter getResampleFilter() {
        return resampleFilter;
    }

    public void setMaxSize(int maxPixelSize) {
        this.maxSize = maxPixelSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

}
