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

import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.data.RGBIntegerImage;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.ColorRGB;
import org.boblycat.blimp.Util;

/**
 * A layer which adds a solid color border around the image.
 * The four border sizes are given in pixels.
 *
 * @author Knut Arild Erstad
 */
public class SolidColorBorderLayer extends DimensionAdjustmentLayer {
    private int left;
    private int right;
    private int top;
    private int bottom;
    private ColorRGB color;

    public SolidColorBorderLayer() {
        color = ColorRGB.White;
    }

    private static int shiftChannelValue(int value, int bitDepth) {
        return value << (bitDepth - 8);
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        if (!(source.getImage() instanceof RGBIntegerImage)) {
            Util.err("Unsupported image type, RGBIntegerImage is required.");
            return source;
        }
        RGBIntegerImage input = (RGBIntegerImage) source.getImage();
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int outputWidth = input.getWidth() + left + right;
        int outputHeight = input.getHeight() + top + bottom;
        RGBIntegerImage output = (RGBIntegerImage)
            input.createCompatibleImage(outputWidth, outputHeight);

        // fill background
        int bitDepth = source.getChannelBitDepth();
        output.clear(RGBIndex.INDEX_RED,
                shiftChannelValue(color.getRed(), bitDepth));
        output.clear(RGBIndex.INDEX_GREEN,
                shiftChannelValue(color.getGreen(), bitDepth));
        output.clear(RGBIndex.INDEX_BLUE,
                shiftChannelValue(color.getBlue(), bitDepth));

        // copy input image (TODO: optimize)
        int[] samples = new int[inputWidth * inputHeight];
        for (int channel = 0; channel < input.getNumChannels(); channel++) {
            input.getSamples(channel, 0, 0, inputWidth, inputHeight,
                    samples, 0);
            output.putSamples(channel, left, top, inputWidth, inputHeight,
                    samples, 0);
        }
        return new Bitmap(output);
    }

    @Override
    public String getDescription() {
        return "Color Border";
    }

    public void setLeft(int left) {
        this.left = Util.constrainedLower(left, 0);
    }

    public int getLeft() {
        return left;
    }

    public void setRight(int right) {
        this.right = Util.constrainedLower(right, 0);
    }

    public int getRight() {
        return right;
    }

    public void setTop(int top) {
        this.top = Util.constrainedLower(top, 0);
    }

    public int getTop() {
        return top;
    }

    public void setBottom(int bottom) {
        this.bottom = Util.constrainedLower(bottom, 0);
    }

    public int getBottom() {
        return bottom;
    }

    public void setColor(ColorRGB color) {
        if (color == null)
            return;
        this.color = color;
    }

    public ColorRGB getColor() {
        return color;
    }

    @Override
    public boolean canChangeColors() {
        return true;
    }

    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        return new BitmapSize(
                inputSize.width + left + right,
                inputSize.height + top + bottom,
                inputSize.pixelScaleFactor);
    }
}
