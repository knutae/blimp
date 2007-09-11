/*
 * Copyright (C) 2007 Knut Arild Erstad
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
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapSize;


class RotateOperation extends ImageToImageOperation {
    double angle;
    int outputWidth;
    int outputHeight;
    
    private static void incrSampleValue(IntegerImage output,
            int channel, int x, int y, int sample, double fraction) {
        if (x < 0 || y < 0 || x >= output.getWidth() || y >= output.getHeight())
            return;
        int oldSample = output.getSample(channel, x, y);
        int increment = (int) (fraction * sample);
        int newSample = oldSample + increment;
        if (newSample > output.getMaxSample(channel))
            newSample = output.getMaxSample(channel);
        output.putSample(channel, x, y, newSample);
    }
    
    private static void incrSamples(IntegerImage output,
            int channel, double x, double y, int sample) {
        int floorx = (int) Math.floor(x);
        int floory = (int) Math.floor(y);
        double dx = x - floorx;
        double dy = y - floory;
        incrSampleValue(output, channel, floorx, floory, sample,
                (1 - dx) * (1 - dy));
        incrSampleValue(output, channel, floorx+1, floory, sample,
                dx * (1 - dy));
        incrSampleValue(output, channel, floorx, floory+1, sample,
                (1 - dx) * dy);
        incrSampleValue(output, channel, floorx+1, floory+1, sample,
                dx * dy);
    }
    
    public void process() throws MissingParameterException,
    WrongParameterException {
        PixelImage pinput = getInputImage();
        if (pinput == null)
            throw new MissingParameterException("missing input image");
        if (!(pinput instanceof IntegerImage))
            throw new WrongParameterException(
                    "unsupported image type, expected IntegerImage");
        if (outputWidth <= 0)
            throw new WrongParameterException(
                    "the output width must be larger than zero");
        if (outputHeight <= 0)
            throw new WrongParameterException(
                    "the output height must be larger than zero");
        
        IntegerImage input = (IntegerImage) pinput;
        IntegerImage output = (IntegerImage) input.createCompatibleImage(
                outputWidth, outputHeight);
        double cosa = Math.cos(angle);
        double sina = Math.sin(angle);
        double xCenterOutput = 0.5 * outputWidth;
        double yCenterOutput = 0.5 * outputHeight;
        double xCenterInput = 0.5 * input.getWidth();
        double yCenterInput = 0.5 * input.getHeight();
        
        for (int channel = 0; channel < input.getNumChannels(); channel++) {
            for (int y = 0; y < outputHeight; y++) {
                double yoff = y - yCenterOutput;
                for (int x = 0; x < outputWidth; x++) {
                    double xoff = x - xCenterOutput;
                    double tmpx = xoff * cosa - yoff * sina;
                    double srcx = tmpx + xCenterInput;
                    int floorx = (int) Math.floor(srcx);
                    if (floorx < 0 || floorx >= input.getWidth())
                        continue;
                    
                    double tmpy = yoff * cosa + xoff * sina;
                    double srcy = tmpy + yCenterInput;
                    int floory = (int) Math.floor(srcy);
                    if (floory < 0 || floory >= input.getHeight())
                        continue;
                    
                    // TODO: add some sort of interpolation
                    int sample = input.getSample(channel, floorx, floory);
                    output.putSample(channel, x, y, sample);
                }
            }
        }
        setOutputImage(output);
    }
}

/**
 * A layer for rotating given an arbitrary angle.
 * 
 * @author Knut Arild Erstad
 */
public class RotateLayer extends DimensionAdjustmentLayer {
    private double angle;

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.DimensionAdjustmentLayer#calculateSize(org.boblycat.blimp.BitmapSize)
     */
    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        return inputSize;
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.AdjustmentLayer#applyLayer(org.boblycat.blimp.Bitmap)
     */
    @Override
    public Bitmap applyLayer(Bitmap source) {
        RotateOperation op = new RotateOperation();
        op.angle = Math.toRadians(angle);
        op.outputWidth = source.getWidth();
        op.outputHeight = source.getHeight();
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
        this.angle = angle;
    }

    /**
     * @return the angle
     */
    public double getAngle() {
        return angle;
    }

    
}
