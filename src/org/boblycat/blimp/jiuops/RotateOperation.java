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

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Operation for rotating an image an arbitrary angle.
 * 
 * @author Knut Arild Erstad
 */
public class RotateOperation extends ImageToImageOperation {
    private double angleDegrees;
    private int outputWidth;
    private int outputHeight;
    private boolean useAntiAliasing;
    
    public RotateOperation() {
        angleDegrees = 0;
        outputWidth = 0;
        outputHeight = 0;
        useAntiAliasing = true;
    }
    
    /**
     * Set the angle to rotate.
     * A positive angle will rotate the image clockwise, a negative angle counterclockwise.
     * @param angle an angle in degrees
     */
    public void setAngle(double angle) {
        this.angleDegrees = angle;
    }

    /**
     * Specify the size of the output image.
     * @param width output image width
     * @param height output image height
     */
    public void setOutputSize(int width, int height) {
        outputWidth = width;
        outputHeight = height;
    }
    
    /**
     * Specify whether to use antialiasing when rotating the image.
     * The significantly improves the quality of the output image, and the default is
     * <code>true</code>.
     * @param useAntiAliasing if <code>true</code>, use antialiasing
     */
    public void setUseAntiAliasing(boolean useAntiAliasing) {
        this.useAntiAliasing = useAntiAliasing;
    }

    private static int getSampleOrZero(IntegerImage input,
            int channel, int x, int y) {
        if (x < 0 || y < 0 || x >= input.getWidth() || y >= input.getHeight())
            return 0;
        return input.getSample(channel, x, y);
    }

    private static int getAntiAliasedSample(IntegerImage input,
            int channel, double x, double y) {
        int floorx = (int) Math.floor(x);
        int floory = (int) Math.floor(y);
        double dx = x - floorx;
        double dy = y - floory;
        double sample =
            getSampleOrZero(input, channel, floorx, floory) * (1-dx) * (1-dy) +
            getSampleOrZero(input, channel, floorx+1, floory) * dx * (1-dy) +
            getSampleOrZero(input, channel, floorx, floory+1) * (1-dx) * dy +
            getSampleOrZero(input, channel, floorx+1, floory+1) * dx * dy;
        return MathUtil.clamp((int) sample, 0, input.getMaxSample(channel));
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
        double angleRadians = Math.toRadians(angleDegrees);
        double cosa = Math.cos(angleRadians);
        double sina = Math.sin(angleRadians);
        double xCenterOutput = 0.5 * outputWidth;
        double yCenterOutput = 0.5 * outputHeight;
        double xCenterInput = 0.5 * input.getWidth();
        double yCenterInput = 0.5 * input.getHeight();
        int maxProgress = outputHeight * input.getNumChannels();

        for (int channel = 0; channel < input.getNumChannels(); channel++) {
            for (int y = 0; y < outputHeight; y++) {
                double yoff = y - yCenterOutput;
                for (int x = 0; x < outputWidth; x++) {
                    double xoff = x - xCenterOutput;
                    double srcx = xoff * cosa - yoff * sina + xCenterInput;
                    double srcy = yoff * cosa + xoff * sina + yCenterInput;

                    int sample;
                    if (useAntiAliasing)
                        sample = getAntiAliasedSample(input, channel, srcx, srcy);
                    else {
                        int floorx = (int) Math.floor(srcx);
                        int floory = (int) Math.floor(srcy);
                        sample = getSampleOrZero(input, channel, floorx, floory);
                    }
                    output.putSample(channel, x, y, sample);
                }
                setProgress(channel * outputHeight + y, maxProgress);
            }
        }
        setOutputImage(output);
    }
}
