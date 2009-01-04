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

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.jiuops.MathUtil;
import org.boblycat.blimp.jiuops.RotateOperation;

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

    private double angle;

    private Quality quality;

    public RotateLayer() {
        quality = Quality.AntiAliased;
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.DimensionAdjustmentLayer#calculateSize(org.boblycat.blimp.BitmapSize)
     */
    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        double a = Math.toRadians(angle);
        double cosa = Math.cos(a);
        double sina = Math.sin(a);
        double w = Math.abs(inputSize.width * cosa) +
            Math.abs(inputSize.height * sina);
        double h = Math.abs(inputSize.width * sina) +
            Math.abs(inputSize.height * cosa);
        return new BitmapSize((int) Math.ceil(w),
                (int) Math.ceil(h), inputSize.pixelScaleFactor);
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.AdjustmentLayer#applyLayer(org.boblycat.blimp.Bitmap)
     */
    @Override
    public Bitmap applyLayer(Bitmap source) {
        BitmapSize outputSize = calculateSize(source.getSize());
        RotateOperation op = new RotateOperation();
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
}
