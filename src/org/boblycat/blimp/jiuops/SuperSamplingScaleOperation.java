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
package org.boblycat.blimp.jiuops;

import java.util.Arrays;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Helper class that provides a memory-efficient upscaled view of an integer image.
 * No more than one line is upscaled at a time.
 */
class UpScaledImageWrapper {
    int scale;
    int width;
    int lastY;
    int[] buffer;
    IntegerImage image;
    
    UpScaledImageWrapper(IntegerImage image, int scale) {
        assert(scale >= 1);
        this.image = image;
        this.scale = scale;
        width = image.getWidth() * scale;
        lastY = -1;
        if (scale > 1)
            buffer = new int[width];
    }
    
    void getLine(int channelIndex, int y, int[] dest) {
        assert(dest.length >= width);
        if (scale == 1) {
            // optimize this very common case
            image.getSamples(channelIndex, 0, y, width, 1, dest, 0);
            return;
        }
        if (y / scale != lastY) {
            image.getSamples(channelIndex, 0, y / scale, image.getWidth(), 1, buffer, 0);
            // Duplicate all samples in the line "scale" times.
            // This is possible to do in-place by starting from the right.
            for (int x = width-1; x >= 0; x--) {
                buffer[x] = buffer[x / scale];
            }
            lastY = y / scale;
        }
        System.arraycopy(buffer, 0, dest, 0, width);
    }
}

/**
 * A simple supersampling operation for scaling integer images using a ratio
 * (rational number).
 * This is relatively fast (for small multipliers, at least) and gives a decent
 * antialiasing effect.
 * 
 * @author Knut Arild Erstad
 */
public class SuperSamplingScaleOperation extends ImageToImageOperation {
    private int multiplier;
    private int divisor;
    
    public SuperSamplingScaleOperation() {
        multiplier = 1;
        divisor = 1;
    }
    
    /**
     * Sets the scaling ratio.
     * @param multiplier
     *      the multiplier (numerator)
     * @param divisor
     *      the divisor (denominator)
     */
    public void setRatio(int multiplier, int divisor) {
        this.multiplier = multiplier;
        this.divisor = divisor;
    }
    
    public void process() throws MissingParameterException,
    WrongParameterException {
        PixelImage pInput = getInputImage();
        if (pInput == null)
            throw new MissingParameterException("missing input image");
        if (!(pInput instanceof IntegerImage))
            throw new WrongParameterException("unsupported image type: must be IntegerImage");
        if (divisor <= 0)
            throw new WrongParameterException("unsupported divisor, must be >= 1");
        if (multiplier <= 0)
            throw new WrongParameterException("unsupported multiplier, must be >= 1");
        IntegerImage input = (IntegerImage) pInput;
        int inWidth = input.getWidth() * multiplier;
        int inHeight = input.getHeight() * multiplier; 
        int outWidth = inWidth / divisor;
        int outHeight = inHeight / divisor;
        IntegerImage output = (IntegerImage) input.createCompatibleImage(outWidth, outHeight);
        int[] outLine = new int[outWidth];
        int[] inLine = new int[inWidth];
        UpScaledImageWrapper inWrapper = new UpScaledImageWrapper(input, multiplier);
        int divisor2 = divisor * divisor;
        for (int channel=0; channel<input.getNumChannels(); channel++) {
            for (int y=0; y<outHeight; y++) {
                Arrays.fill(outLine, 0);
                int startY = y*divisor;
                int endY = Math.min((y+1)*divisor, inHeight);
                for (int inY=startY; inY<endY; inY++) {
                    inWrapper.getLine(channel, inY, inLine);
                    int endX = outWidth * divisor;
                    for (int inX=0; inX < endX; inX++) {
                        outLine[inX / divisor] += inLine[inX];
                    }
                }
                for (int x=0; x<outWidth; x++) {
                    outLine[x] /= divisor2;
                }
                output.putSamples(channel, 0, y, outWidth, 1, outLine, 0);
                setProgress(outHeight*channel + y, outHeight*input.getNumChannels());
            }
        }
        setOutputImage(output);
    }
}
