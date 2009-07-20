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

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.geometry.Resample;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.jiuops.MathUtil;

public class PrintLayer extends DimensionAdjustmentLayer {
    private int paperWidth;
    private int paperHeight;
    private double borderPercentage; // maybe make this absolute?
    private boolean preview;

    public PrintLayer() {
        paperWidth = 0;
        paperHeight = 0;
        borderPercentage = 10;
        preview = true;
    }

    public BitmapSize caluclateSizeWithoutBorder(BitmapSize inputSize) {
        double maxAmount = 1.0 - borderPercentage/100.0;
        int maxWidth = (int) (paperWidth * maxAmount);
        int maxHeight = (int) (paperHeight * maxAmount);
        int rescaleWidth, rescaleHeight;
        int inputHeight = inputSize.height;
        int inputWidth = inputSize.width;
        if (maxWidth * inputHeight < inputWidth * maxHeight) {
            // use maxWidth
            rescaleWidth = maxWidth;
            rescaleHeight = inputHeight * rescaleWidth / inputWidth;
        }
        else {
            // use maxHeight
            rescaleHeight = maxHeight;
            rescaleWidth = inputWidth * rescaleHeight / inputHeight;
        }
        return new BitmapSize(rescaleWidth, rescaleHeight);
    }

    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        if (paperWidth <= 0 || paperHeight <= 0)
            return inputSize;
        if (preview)
            return new BitmapSize(paperWidth, paperHeight);
        else
            return caluclateSizeWithoutBorder(inputSize);
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        if (paperWidth <= 0 || paperHeight <= 0) {
            // This is normal before a printer is selected, not necessarily an error
            return source;
        }

        BitmapSize rescaleSize = caluclateSizeWithoutBorder(source.getSize());
        int rescaleWidth = rescaleSize.width;
        int rescaleHeight = rescaleSize.height;
        Resample resample = new Resample();
        resample.setSize(rescaleWidth, rescaleHeight);
        resample.setInputImage(source.getImage());
        IntegerImage rescaled = (IntegerImage) applyJiuOperation(source.getImage(), resample);

        if (!isPreview())
            // don't add borders unless for previewing
            return new Bitmap(rescaled);

        // Create white image
        int whiteIntesity = (1 << source.getChannelBitDepth()) - 1;
        IntegerImage output = (IntegerImage) rescaled.createCompatibleImage(paperWidth, paperHeight);
        for (int channel=0; channel<output.getNumChannels(); channel++)
            output.clear(channel, whiteIntesity);

        // Copy resized image
        int[] samples = new int[rescaleWidth * rescaleHeight];
        int left = (paperWidth - rescaleWidth) / 2;
        int top = (paperHeight - rescaleHeight) / 2;
        for (int channel = 0; channel < rescaled.getNumChannels(); channel++) {
            rescaled.getSamples(channel, 0, 0, rescaleWidth, rescaleHeight, samples, 0);
            output.putSamples(channel, left, top, rescaleWidth, rescaleHeight,
                    samples, 0);
        }

        return new Bitmap(output);
    }

    @Override
    public String getDescription() {
        return "Printer Preparation";
    }

    /**
     * Set the paper width, in pixels.
     * @param paperWidth the new paper width
     */
    public void setPaperWidth(int paperWidth) {
        this.paperWidth = paperWidth;
    }

    /**
     * Get the paper width, in pixels.
     * @return the current paper width
     */
    public int getPaperWidth() {
        return paperWidth;
    }

    /**
     * Set the paper height, in pixels.
     * @param paperHeight the new paper height
     */
    public void setPaperHeight(int paperHeight) {
        this.paperHeight = paperHeight;
    }

    /**
     * Get the paper height, in pixels.
     * @return the current paper height
     */
    public int getPaperHeight() {
        return paperHeight;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public boolean isPreview() {
        return preview;
    }

    public void setBorderPercentage(double borderPercentage) {
        this.borderPercentage = MathUtil.clamp(borderPercentage, 0, 99);
    }

    public double getBorderPercentage() {
        return borderPercentage;
    }
}
