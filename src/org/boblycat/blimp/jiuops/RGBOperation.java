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

import static org.boblycat.blimp.util.MathUtil.clampToUnsignedByte;
import static org.boblycat.blimp.util.MathUtil.clampToUnsignedShort;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * An abstract base operation for modifying RGB pixels individually.
 * Supported image types are {@link RGB24Image} and {@link RGB48Image}.
 * 
 * @author Knut Arild Erstad
 */
public abstract class RGBOperation extends ImageToImageOperation {
    /**
     * Placeholder for a RGB triplet.
     */
    protected class RGB {
        public int r, g, b;
    }

    /**
     * Modify a 24-bit RGB pixel and put the result in <code>out</out>.
     * The result will be automatically clamped to an unsigned byte range.
     * @param r red intensity
     * @param g green intensity
     * @param b blue intensity
     * @param out placeholder for the result
     */
    protected abstract void adjust24BitColor(int r, int g, int b, RGB out);

    /**
     * Modify a 48-bit RGB pixel and put the result in <code>out</out>.
     * The result will be automatically clamped to an unsigned short range.
     * @param r red intensity
     * @param g green intensity
     * @param b blue intensity
     * @param out placeholder for the result
     */
    protected abstract void adjust48BitColor(int r, int g, int b, RGB out);
    
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
        RGB rgb = new RGB();
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
                    int r, g, b;
                    r = 0xff & redLine[x];
                    g = 0xff & greenLine[x];
                    b = 0xff & blueLine[x];
                    adjust24BitColor(r, g, b, rgb);
                    redLine[x] = clampToUnsignedByte(rgb.r);
                    greenLine[x] = clampToUnsignedByte(rgb.g);
                    blueLine[x] = clampToUnsignedByte(rgb.b);
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
                    int r, g, b;
                    r = 0xffff & redLine[x];
                    g = 0xffff & greenLine[x];
                    b = 0xffff & blueLine[x];
                    adjust48BitColor(r, g, b, rgb);
                    redLine[x] = clampToUnsignedShort(rgb.r);
                    greenLine[x] = clampToUnsignedShort(rgb.g);
                    blueLine[x] = clampToUnsignedShort(rgb.b);
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
