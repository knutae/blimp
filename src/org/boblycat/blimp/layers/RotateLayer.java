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
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.jiuops.MathUtil;


class RotateOperation extends ImageToImageOperation {
    double angle;
    int outputWidth;
    int outputHeight;
    boolean useAntiAliasing;

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
        double cosa = Math.cos(angle);
        double sina = Math.sin(angle);
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
        op.angle = Math.toRadians(angle);
        op.outputWidth = outputSize.width;
        op.outputHeight = outputSize.height;
        op.useAntiAliasing = (quality == Quality.AntiAliased);
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
