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

import static org.boblycat.blimp.jiuops.MathUtil.*;

import net.sourceforge.jiu.data.GrayImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Converts an RBG image to grayscale by supplying red, green and blue weights.
 * 
 * Note that the output image will be of the same type as the input
 * (which must be {@link RGB24Image} or {@link RGB48Image}), and not a
 * {@link GrayImage}.
 * 
 * @author Knut Arild Erstad
 */
public class GrayscaleMixerOperation extends ImageToImageOperation {
    private int redWeight, greenWeight, blueWeight;

    /**
     * Set the red, green and blue weights.
     * A "full" weight is 100, and it is often a good idea to make the
     * sum of the weights close to 100.  Negative weights are allowed.
     * 
     * @param red the red weight
     * @param green the green weight
     * @param blue the blue weight
     */
    public void setWeights(int red, int green, int blue) {
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
                    int intensity = (redWeight * r + greenWeight * g + blueWeight * b)
                            / 100;
                    assert (intensity >= 0);
                    grayLine[x] = clampToUnsignedByte(intensity);
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
                    int intensity = (redWeight * r + greenWeight * g + blueWeight * b)
                            / 100;
                    grayLine[x] = clampToUnsignedShort(intensity);
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
