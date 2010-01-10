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

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.geometry.Rotate;
import net.sourceforge.jiu.util.MathUtil;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.data.BitmapSize;

/**
 * A layer for rotating given an arbitrary angle.
 *
 * @author Knut Arild Erstad
 */
public class RotateLayer extends DimensionAdjustmentLayer {
    public enum Quality {
        Fast,
        AntiAliased,
    }

    public enum SizeStrategy {
        Keep,
        Expand,
        AutoCrop,
    }

    private double angle;

    private Quality quality;

    private SizeStrategy sizeStrategy;

    public RotateLayer() {
        quality = Quality.AntiAliased;
        sizeStrategy = SizeStrategy.AutoCrop;
    }

    private static double calculateAutoCropLongerSide(double inLonger, double inShorter, double radAngle) {
        double sina = Math.abs(Math.sin(radAngle));
        double cosa = Math.abs(Math.cos(radAngle));
        return inLonger * inShorter / (sina * inLonger + cosa * inShorter);
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.DimensionAdjustmentLayer#calculateSize(org.boblycat.blimp.BitmapSize)
     */
    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        switch (getSizeStrategy()) {
        case Keep:
            return inputSize;
        case Expand:
            {
                double a = Math.toRadians(angle);
                double inWidth = inputSize.width;
                double inHeight = inputSize.height;
                double cosa = Math.cos(a);
                double sina = Math.sin(a);
                double w = Math.abs(inWidth * cosa) + Math.abs(inHeight * sina);
                double h = Math.abs(inWidth * sina) + Math.abs(inHeight * cosa);
                return new BitmapSize((int) Math.ceil(w),
                        (int) Math.ceil(h), inputSize.pixelScaleFactor);
            }
        case AutoCrop:
            {
                double a = Math.toRadians(angle);
                double inWidth = inputSize.width;
                double inHeight = inputSize.height;
                double w, h;
                if (inWidth > inHeight) {
                    w = calculateAutoCropLongerSide(inWidth, inHeight, a);
                    h = inHeight * w / inWidth;
                }
                else {
                    h = calculateAutoCropLongerSide(inHeight, inWidth, a);
                    w = inWidth * h / inHeight;
                }
                return new BitmapSize((int) Math.ceil(w),
                        (int) Math.ceil(h), inputSize.pixelScaleFactor);
            }
        }
        assert(false); // should never get here
        return inputSize;
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.AdjustmentLayer#applyLayer(org.boblycat.blimp.Bitmap)
     */
    @Override
    public Bitmap applyLayer(Bitmap source) {
        BitmapSize outputSize = calculateSize(source.getSize());
        Rotate op = new Rotate();
        op.setAngle(angle);
        op.setOutputSize(outputSize.width, outputSize.height);
        op.setUseAntiAliasing(quality == Quality.AntiAliased);
        PixelImage image = applyJiuOperation(source.getImage(), op);
        return new Bitmap(image);
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.Layer#getDescription()
     */
    @Override
    public String getDescription() {
        return "Rotate";
    }

    /**
     * @param angle the angle to set
     */
    public void setAngle(double angle) {
        this.angle = MathUtil.clamp(angle, -180, 180);
    }

    /**
     * @return the angle
     */
    public double getAngle() {
        return angle;
    }

    /**
     * @param quality the quality to set
     */
    public void setQuality(Quality quality) {
        if (quality == null)
            return;
        this.quality = quality;
    }

    /**
     * @return the quality
     */
    public Quality getQuality() {
        return quality;
    }

    /**
     * Set the strategy for calculating the size of the output image.
     * Either expand the size with black borders, keep the original size, or auto-crop
     * the image while keeping the aspect ratio of the original.
     * @param sizeStrategy the new size strategy
     */
    public void setSizeStrategy(SizeStrategy sizeStrategy) {
        this.sizeStrategy = sizeStrategy;
    }

    /**
     * Get the strategy for calculating the size of the output image.
     * Either expand the size with black borders, keep the original size, or auto-crop
     * the image while keeping the aspect ratio of the original.
     * @return the current size strategy
     */
    public SizeStrategy getSizeStrategy() {
        return sizeStrategy;
    }
}
