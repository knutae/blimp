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
import org.boblycat.blimp.Util;

public class PrintLayer extends DimensionAdjustmentLayer {
    private int paperWidth;
    private int paperHeight;
    private double borderPercentage; // maybe make this absolute?

    public PrintLayer() {
        //paperWidth = 0;
        //paperHeight = 0;
        paperWidth = 1000;
        paperHeight = 700;
        borderPercentage = 10;
    }

    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        return new BitmapSize(paperWidth, paperHeight);
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        if (paperWidth <= 0 || paperHeight <= 0) {
            Util.err("Need a paper size");
            return source;
        }

        double maxAmount = 1.0 - borderPercentage/100.0;
        int maxWidth = (int) (paperWidth * maxAmount);
        int maxHeight = (int) (paperHeight * maxAmount);
        int rescaleWidth, rescaleHeight;
        if (maxWidth * source.getHeight() < source.getWidth() * maxHeight) {
            // use maxWidth
            rescaleWidth = maxWidth;
            rescaleHeight = source.getHeight() * rescaleWidth / source.getWidth();
        }
        else {
            // use maxHeight
            rescaleHeight = maxHeight;
            rescaleWidth = source.getWidth() * rescaleHeight / source.getHeight();
        }
        Resample resample = new Resample();
        resample.setSize(rescaleWidth, rescaleHeight);
        resample.setInputImage(source.getImage());
        IntegerImage rescaled = (IntegerImage) applyJiuOperation(source.getImage(), resample);

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

}
