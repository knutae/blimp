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
package org.boblycat.blimp;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * An abstract operation for modifying RGB pixels individually as doubles.
 *
 * @author Knut Arild Erstad
 */
public abstract class RGBDoubleOperation extends ImageToImageOperation {
    /**
     * Modify each pixel and put the result in the <i>out</i> array.
     *
     * Each input value is in the range [0, 1].  Output values that are
     * not in the [0, 1] range will be cropped automatically.
     *
     * @param r
     *            the red value
     * @param g
     *            the green value
     * @param b
     *            the blue value
     * @param out
     *            an uninitialized array of length 3.
     */
    protected abstract void adjustColor(double r, double g, double b,
            double[] out);

    private static final double MAX_8BIT = 255.0;
    private static final double MAX_16BIT = 65535.0;

    /**
     * Implements the main algorithm for processing images of the types
     * {@link RGB24Image} and {@link RGB48Image}.  Other image types
     * will cause errors.
     */
    public void process() throws MissingParameterException,
            WrongParameterException {
        PixelImage input = getInputImage();
        if (input == null)
            throw new MissingParameterException("missing input image");
        int width = input.getWidth();
        int height = input.getHeight();
        PixelImage output = input.createCompatibleImage(width, height);
        double[] rgb = new double[3];
        if (input instanceof RGB24Image) {
            RGB24Image input24 = (RGB24Image) input;
            RGB24Image output24 = (RGB24Image) output;
            byte[] redLine, greenLine, blueLine;
            redLine = new byte[width];
            greenLine = new byte[width];
            blueLine = new byte[width];
            for (int y = 0; y < height; y++) {
                input24.getByteSamples(RGBIndex.INDEX_RED, 0, y, width, 1,
                        redLine, 0);
                input24.getByteSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1,
                        greenLine, 0);
                input24.getByteSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1,
                        blueLine, 0);

                for (int x = 0; x < width; x++) {
                    double r, g, b;
                    r = (0xff & redLine[x]) / MAX_8BIT;
                    g = (0xff & greenLine[x]) / MAX_8BIT;
                    b = (0xff & blueLine[x]) / MAX_8BIT;
                    adjustColor(r, g, b, rgb);
                    redLine[x] = Util
                            .cropToUnsignedByte((int) (rgb[0] * MAX_8BIT));
                    greenLine[x] = Util
                            .cropToUnsignedByte((int) (rgb[1] * MAX_8BIT));
                    blueLine[x] = Util
                            .cropToUnsignedByte((int) (rgb[2] * MAX_8BIT));
                }

                output24.putByteSamples(RGBIndex.INDEX_RED, 0, y, width, 1,
                        redLine, 0);
                output24.putByteSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1,
                        greenLine, 0);
                output24.putByteSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1,
                        blueLine, 0);

                setProgress(y, height);
            }
        }
        else if (input instanceof RGB48Image) {
            RGB48Image input48 = (RGB48Image) input;
            RGB48Image output48 = (RGB48Image) output;
            short[] redLine, greenLine, blueLine;
            redLine = new short[width];
            greenLine = new short[width];
            blueLine = new short[width];
            for (int y = 0; y < height; y++) {
                input48.getShortSamples(RGBIndex.INDEX_RED, 0, y, width, 1,
                        redLine, 0);
                input48.getShortSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1,
                        greenLine, 0);
                input48.getShortSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1,
                        blueLine, 0);

                for (int x = 0; x < width; x++) {
                    double r, g, b;
                    r = (0xffff & redLine[x]) / MAX_16BIT;
                    g = (0xffff & greenLine[x]) / MAX_16BIT;
                    b = (0xffff & blueLine[x]) / MAX_16BIT;
                    adjustColor(r, g, b, rgb);
                    redLine[x] = Util
                            .cropToUnsignedShort((int) (rgb[0] * MAX_16BIT));
                    greenLine[x] = Util
                            .cropToUnsignedShort((int) (rgb[1] * MAX_16BIT));
                    blueLine[x] = Util
                            .cropToUnsignedShort((int) (rgb[2] * MAX_16BIT));
                }

                output48.putShortSamples(RGBIndex.INDEX_RED, 0, y, width, 1,
                        redLine, 0);
                output48.putShortSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1,
                        greenLine, 0);
                output48.putShortSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1,
                        blueLine, 0);

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
