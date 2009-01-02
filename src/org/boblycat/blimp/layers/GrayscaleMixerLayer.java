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

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

class GrayscaleMixerOperation extends ImageToImageOperation {
    int redWeight, greenWeight, blueWeight;

    void setWeights(int red, int green, int blue) {
        redWeight = red;
        greenWeight = green;
        blueWeight = blue;
    }

    public void process() throws MissingParameterException,
            WrongParameterException {
        PixelImage input = getInputImage();
        if (input == null)
            throw new MissingParameterException("missing input image");
        int width = input.getWidth();
        int height = input.getHeight();
        PixelImage output = input.createCompatibleImage(width, height);
        if (input instanceof RGB24Image) {
            RGB24Image input24 = (RGB24Image) input;
            RGB24Image output24 = (RGB24Image) output;
            byte[] redLine, greenLine, blueLine, grayLine;
            redLine = new byte[width];
            greenLine = new byte[width];
            blueLine = new byte[width];
            grayLine = new byte[width];
            for (int y = 0; y < height; y++) {
                input24.getByteSamples(RGBIndex.INDEX_RED, 0, y, width, 1,
                        redLine, 0);
                input24.getByteSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1,
                        greenLine, 0);
                input24.getByteSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1,
                        blueLine, 0);

                for (int x = 0; x < width; x++) {
                    int r, g, b;
                    r = 0xff & redLine[x];
                    g = 0xff & greenLine[x];
                    b = 0xff & blueLine[x];
                    assert (r >= 0);
                    assert (g >= 0);
                    assert (b >= 0);
                    int intensity = (redWeight * r + greenWeight * g + blueWeight
                            * b)
                            / GrayscaleMixerLayer.FULL_WEIGHT;
                    assert (intensity >= 0);
                    grayLine[x] = Util.cropToUnsignedByte(intensity);
                }

                output24.putByteSamples(RGBIndex.INDEX_RED, 0, y, width, 1,
                        grayLine, 0);
                output24.putByteSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1,
                        grayLine, 0);
                output24.putByteSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1,
                        grayLine, 0);

                setProgress(y, height);
            }
        }
        else if (input instanceof RGB48Image) {
            RGB48Image input48 = (RGB48Image) input;
            RGB48Image output48 = (RGB48Image) output;
            short[] redLine, greenLine, blueLine, grayLine;
            redLine = new short[width];
            greenLine = new short[width];
            blueLine = new short[width];
            grayLine = new short[width];
            for (int y = 0; y < height; y++) {
                input48.getShortSamples(RGBIndex.INDEX_RED, 0, y, width, 1,
                        redLine, 0);
                input48.getShortSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1,
                        greenLine, 0);
                input48.getShortSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1,
                        blueLine, 0);

                for (int x = 0; x < width; x++) {
                    int r, g, b;
                    r = redLine[x] & 0xffff;
                    g = greenLine[x] & 0xffff;
                    b = blueLine[x] & 0xffff;
                    int intensity = (redWeight * r + greenWeight * g + blueWeight
                            * b)
                            / GrayscaleMixerLayer.FULL_WEIGHT;
                    grayLine[x] = Util.cropToUnsignedShort(intensity);
                }

                output48.putShortSamples(RGBIndex.INDEX_RED, 0, y, width, 1,
                        grayLine, 0);
                output48.putShortSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1,
                        grayLine, 0);
                output48.putShortSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1,
                        grayLine, 0);

                setProgress(y, height);
            }
        }
        else {
            throw new WrongParameterException(
                    "unsupported image type: must be RGB24Image or RGB48Image");
        }
        setOutputImage(output);
    }
}

public class GrayscaleMixerLayer extends AdjustmentLayer {
    public static final int FULL_WEIGHT = 100;

    public static final int MINIMUM_WEIGHT = -2 * FULL_WEIGHT;

    public static final int MAXIMUM_WEIGHT = 2 * FULL_WEIGHT;

    public static final int DEFAULT_RED = 40;

    public static final int DEFAULT_GREEN = 30;

    public static final int DEFAULT_BLUE = 30;

    private int blue;

    private int green;

    private int red;

    public GrayscaleMixerLayer() {
        red = DEFAULT_RED;
        green = DEFAULT_GREEN;
        blue = DEFAULT_BLUE;
    }

    static int constrain(int value) {
        return Util.constrainedValue(value, MINIMUM_WEIGHT, MAXIMUM_WEIGHT);
    }

    public void setRed(int red) {
        this.red = constrain(red);
    }

    public int getRed() {
        return red;
    }

    public void setGreen(int green) {
        this.green = constrain(green);
    }

    public int getGreen() {
        return green;
    }

    public void setBlue(int blue) {
        this.blue = constrain(blue);
    }

    public int getBlue() {
        return blue;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        GrayscaleMixerOperation mixerOp = new GrayscaleMixerOperation();
        mixerOp.setWeights(red, green, blue);
        PixelImage image = source.getImage();
        image = applyJiuOperation(image, mixerOp);
        return new Bitmap(image);
    }

    @Override
    public String getDescription() {
        return "Grayscale Mixer";
    }

}
