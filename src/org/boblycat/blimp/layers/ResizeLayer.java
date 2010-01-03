/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.boblycat.blimp.layers;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.data.BitmapSize;
import org.boblycat.blimp.util.Util;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.geometry.Resample;
import net.sourceforge.jiu.geometry.Resample.FilterType;

public class ResizeLayer extends DimensionAdjustmentLayer {
    public enum Filter {
        BSpline  { FilterType getType() { return FilterType.B_SPLINE; } },
        Bell     { FilterType getType() { return FilterType.BELL; } },
        Hermite  { FilterType getType() { return FilterType.HERMITE; } },
        Lanczos3 { FilterType getType() { return FilterType.LANCZOS3; } },
        Mitchell { FilterType getType() { return FilterType.MITCHELL; } },
        Triangle { FilterType getType() { return FilterType.TRIANGLE; } };

        abstract FilterType getType();
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

    public BitmapSize calculateNewSize(int inputWidth, int inputHeight) {
        int width, height;
        if (inputWidth > inputHeight) {
            width = maxSize;
            height = Util.roundDiv(maxSize * inputHeight, inputWidth);
        }
        else {
            height = maxSize;
            width = Util.roundDiv(maxSize * inputWidth, inputHeight);
        }
        return new BitmapSize(width, height);
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        PixelImage input = source.getImage();
        Resample resampleOp = new Resample();
        resampleOp.setFilter(resampleFilter.getType());
        BitmapSize newSize = calculateNewSize(input.getWidth(), input.getHeight());
        resampleOp.setSize(newSize.width, newSize.height);
        Bitmap bitmap = new Bitmap(applyJiuOperation(input, resampleOp));
        double scaleFactor = source.getWidth() / (double) newSize.width;
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

    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        BitmapSize newSize = calculateNewSize(inputSize.width, inputSize.height);
        double scaleFactor = inputSize.width / (double) newSize.width;
        newSize.pixelScaleFactor = inputSize.pixelScaleFactor * scaleFactor;
        return newSize;
    }

}
