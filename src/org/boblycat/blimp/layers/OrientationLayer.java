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
import net.sourceforge.jiu.geometry.Flip;
import net.sourceforge.jiu.geometry.Mirror;
import net.sourceforge.jiu.geometry.Rotate180;
import net.sourceforge.jiu.geometry.Rotate90Left;
import net.sourceforge.jiu.geometry.Rotate90Right;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.data.BitmapSize;

public class OrientationLayer extends DimensionAdjustmentLayer {
    public enum Rotation {
        None,
        Rotate90Left,
        Rotate90Right,
        Rotate180,
    }

    private Rotation rotation;

    boolean flipHorizontal;

    boolean flipVertical;

    public OrientationLayer() {
        rotation = Rotation.None;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        PixelImage image = source.getImage();
        switch (rotation) {
        case None:
            break;
        case Rotate90Left:
            image = applyJiuOperation(image, new Rotate90Left());
            break;
        case Rotate90Right:
            image = applyJiuOperation(image, new Rotate90Right());
            break;
        case Rotate180:
            image = applyJiuOperation(image, new Rotate180());
            break;
        }
        if (flipVertical)
            image = applyJiuOperation(image, new Flip());
        if (flipHorizontal)
            image = applyJiuOperation(image, new Mirror());
        return new Bitmap(image);
    }

    @Override
    public String getDescription() {
        return "Image Orientation";
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setFlipVertical(boolean flip) {
        this.flipVertical = flip;
    }

    public boolean getFlipVertical() {
        return flipVertical;
    }

    public void setFlipHorizontal(boolean flip) {
        this.flipHorizontal = flip;
    }

    public boolean getFlipHorizontal() {
        return flipHorizontal;
    }

    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        switch (rotation) {
        case Rotate90Left:
        case Rotate90Right:
            return new BitmapSize(inputSize.height, inputSize.width,
                    inputSize.pixelScaleFactor);
        }
        return inputSize;
    }

}
