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

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.ColorUtil;
import org.boblycat.blimp.Util;

// TODO: factor out common code from SaturationOperation
class ColorMixerOperation extends ImageToImageOperation {
    int cyanRed;
    int magentaGreen;
    int yellowBlue;
    boolean preserveLightness;

    private static final double MAX_8BIT = 255.0;
    private static final double MAX_16BIT = 65535.0;

    private static double adjust(double input, double factor) {
        if (factor <= 0.0)
            return input * (1.0 + factor);
        else
            return input + (1.0 - input) * factor;
    }

    private static void adjustColor(
            double r, double g, double b,
            double cyanRedFactor,
            double magentaGreenFactor,
            double yellowBlueFactor,
            boolean preserveLightness,
            double[] out) {
        out[0] = adjust(r, cyanRedFactor);
        out[1] = adjust(g, magentaGreenFactor);
        out[2] = adjust(b, yellowBlueFactor);
        if (preserveLightness) {
            // get the original lightness
            double[] tmp = ColorUtil.rgbToHsl(r, g, b, null);
            double lightness = tmp[2];
            // restore it
            ColorUtil.rgbToHsl(out[0], out[1], out[2], tmp);
            ColorUtil.hslToRgb(tmp[0], tmp[1], lightness, out);
        }
    }

    private static double toFactor(int ivalue) {
        return ivalue / 100.0;
    }

    public void process() throws
            MissingParameterException,
            WrongParameterException {
        PixelImage input = getInputImage();
        if (input == null)
            throw new MissingParameterException("missing input image");
        int width = input.getWidth();
        int height = input.getHeight();
        PixelImage output = input.createCompatibleImage(width, height);
        double cyanRedFactor = toFactor(cyanRed);
        double magentaGreenFactor = toFactor(magentaGreen);
        double yellowBlueFactor = toFactor(yellowBlue);
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
                double[] rgb = new double[3];

                for (int x = 0; x < width; x++) {
                    double r, g, b;
                    r = (0xff & redLine[x]) / MAX_8BIT;
                    g = (0xff & greenLine[x]) / MAX_8BIT;
                    b = (0xff & blueLine[x]) / MAX_8BIT;
                    adjustColor(r, g, b, cyanRedFactor, magentaGreenFactor,
                            yellowBlueFactor, preserveLightness, rgb);
                    redLine[x] = Util.cropToUnsignedByte((int) (rgb[0] * MAX_8BIT));
                    greenLine[x] = Util.cropToUnsignedByte((int) (rgb[1] * MAX_8BIT));
                    blueLine[x] = Util.cropToUnsignedByte((int) (rgb[2] * MAX_8BIT));
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
                double[] rgb = new double[3];

                for (int x = 0; x < width; x++) {
                    double r, g, b;
                    r = (0xffff & redLine[x]) / MAX_16BIT;
                    g = (0xffff & greenLine[x]) / MAX_16BIT;
                    b = (0xffff & blueLine[x]) / MAX_16BIT;
                    adjustColor(r, g, b, cyanRedFactor, magentaGreenFactor,
                            yellowBlueFactor, preserveLightness, rgb);
                    redLine[x] = Util.cropToUnsignedShort((int) (rgb[0] * MAX_16BIT));
                    greenLine[x] = Util.cropToUnsignedShort((int) (rgb[1] * MAX_16BIT));
                    blueLine[x] = Util.cropToUnsignedShort((int) (rgb[2] * MAX_16BIT));
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


/**
 * A layer for modifying specific color tones (hues).
 *
 * @author Knut Arild Erstad
 */
public class ColorBalanceLayer extends AdjustmentLayer {
    public static final int MIN_VALUE = -100;
    public static final int MAX_VALUE = 100;

    private static int constrain(int value) {
        return Util.constrainedValue(value, MIN_VALUE, MAX_VALUE);
    }

    private int cyanRed;
    private int magentaGreen;
    private int yellowBlue;
    private boolean preserveLightness;

    /**
     * Constructs a color balance layer with preserveLightness enabled.
     */
    public ColorBalanceLayer() {
        cyanRed = 0;
        magentaGreen = 0;
        yellowBlue = 0;
        preserveLightness = true;
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.AdjustmentLayer#applyLayer(org.boblycat.blimp.Bitmap)
     */
    @Override
    public Bitmap applyLayer(Bitmap source) {
        ColorMixerOperation op = new ColorMixerOperation();
        op.cyanRed = cyanRed;
        op.magentaGreen = magentaGreen;
        op.yellowBlue = yellowBlue;
        op.preserveLightness = preserveLightness;
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.Layer#getDescription()
     */
    @Override
    public String getDescription() {
        return "Color Balance";
    }

    /**
     * @param cyanRed the cyan-red modifier to set.
     */
    public void setCyanRed(int cyanRed) {
        this.cyanRed = constrain(cyanRed);
    }

    /**
     * @return the cyan-red modifier.
     */
    public int getCyanRed() {
        return cyanRed;
    }

    /**
     * @param magentaGreen the magenta-green modifier to set
     */
    public void setMagentaGreen(int magentaGreen) {
        this.magentaGreen = constrain(magentaGreen);
    }

    /**
     * @return the magenta-green modifier
     */
    public int getMagentaGreen() {
        return magentaGreen;
    }

    /**
     * @param yellowBlue the yellow-blue modifier to set
     */
    public void setYellowBlue(int yellowBlue) {
        this.yellowBlue = constrain(yellowBlue);
    }

    /**
     * @return the yellow-blue modifier
     */
    public int getYellowBlue() {
        return yellowBlue;
    }

    /**
     * @param preserveLuminosity the preserveLuminosity to set
     */
    public void setPreserveLightness(boolean preserveLuminosity) {
        this.preserveLightness = preserveLuminosity;
    }

    /**
     * @return the preserveLuminosity
     */
    public boolean getPreserveLightness() {
        return preserveLightness;
    }

}
