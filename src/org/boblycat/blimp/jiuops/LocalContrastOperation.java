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

import java.util.ArrayList;

import org.boblycat.blimp.util.ColorUtil;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Helper class: container for incrementally collecting the sum of
 * multiple lines of data, as a step towards calculating averages.
 * 
 * This collects line data for a single channel (red, green or blue),
 * and only sums values vertically (along the Y axis).
 * 
 * @author Knut Arild Erstad
 */
class MultiLineData {
    IntegerImage image;
    int width;
    ArrayList<int[]> activeLineData;
    int[] removedLine;
    int[] combined;
    int channel;

    MultiLineData(IntegerImage image, int channel) {
        this.image = image;
        this.channel = channel;
        width = image.getWidth();
        activeLineData = new ArrayList<int[]>();
        combined = new int[width];
    }

    void addLine(int y) {
        int[] lineData;
        if (removedLine != null) {
            // reuse allocated memory for removed line
            lineData = removedLine;
            removedLine = null;
        }
        else {
            lineData = new int[width];
        }
        image.getSamples(channel, 0, y, width, 1, lineData, 0);
        for (int x = 0; x < width; x++)
            combined[x] += lineData[x];
        activeLineData.add(lineData);
    }

    void popLine() {
        removedLine = activeLineData.remove(0);
        for (int x = 0; x < width; x++)
            combined[x] -= removedLine[x];
    }

    int[] getCombinedData() {
        return combined;
    }

    int getNumLines() {
        return activeLineData.size();
    }
}

/**
 * Helper class: container for incrementally collecting the sums and
 * calculating averages of multiple lines of data.
 * 
 * This class collects line data for all channels (red, green and blue)
 * and is able to calculate sums and averages of rectangular areas
 * efficiently.  It also calculates the average lightness (the L in HSL)
 * for each RGB value.
 * 
 * @author Knut Arild Erstad
 */
class MultiLineHSLData {
    IntegerImage image;

    int width;
    int height;
    int radius;
    double[] lightnessLineData;
    double[] redLineData;
    double[] greenLineData;
    double[] blueLineData;
    double maxSample;

    MultiLineData redData;
    MultiLineData greenData;
    MultiLineData blueData;

    MultiLineHSLData(IntegerImage image, int radius, double maxSample) {
        this.image = image;
        this.radius = radius;
        this.maxSample = maxSample;
        width = image.getWidth();
        height = image.getHeight();
        redData = new MultiLineData(image, RGBIndex.INDEX_RED);
        greenData = new MultiLineData(image, RGBIndex.INDEX_GREEN);
        blueData = new MultiLineData(image, RGBIndex.INDEX_BLUE);
        lightnessLineData = new double[width];
        redLineData = new double[width];
        greenLineData = new double[width];
        blueLineData = new double[width];
    }

    void addLine(int y) {
        redData.addLine(y);
        greenData.addLine(y);
        blueData.addLine(y);
    }

    void popLine() {
        redData.popLine();
        greenData.popLine();
        blueData.popLine();
    }

    /**
     * Combine the currently collected line data for all channels into
     * average red, green, blue and lightness (HSL) values, as doubles.
     */
    void updateHslLineData() {
        int[] combinedRed = redData.getCombinedData();
        int[] combinedGreen = greenData.getCombinedData();
        int[] combinedBlue = blueData.getCombinedData();
        int numLines = redData.getNumLines();
        // ints will overflow in some cases, so use longs
        long redValue = 0;
        long greenValue = 0;
        long blueValue = 0;
        long divisor = 0;
        double hsl[] = new double[3];
        for (int x = 0; x < Math.min(width, radius); x++) {
            redValue += combinedRed[x];
            greenValue += combinedGreen[x];
            blueValue += combinedBlue[x];
            divisor += numLines;
        }
        for (int x = 0; x < width; x++) {
            if (x > radius) {
                redValue -= combinedRed[x-radius-1];
                greenValue -= combinedGreen[x-radius-1];
                blueValue -= combinedBlue[x-radius-1];
                divisor -= numLines;
            }
            if (x + radius < width) {
                redValue += combinedRed[x+radius];
                greenValue += combinedGreen[x+radius];
                blueValue += combinedBlue[x+radius];
                divisor += numLines;
            }
            double r = redValue / (maxSample * divisor);
            double g = greenValue / (maxSample * divisor);
            double b = blueValue / (maxSample * divisor);
            redLineData[x] = r;
            greenLineData[x] = g;
            blueLineData[x] = b;
            ColorUtil.rgbToHsl(r, g, b, hsl);
            lightnessLineData[x] = hsl[2];
        }
    }
}

/**
 * An operation for adding contrast in local areas within an image.
 *
 * The local contrast enchancement is implemented like an unsharp mask filter
 * with a big radius, but optimized by taking the average intensity of square
 * (or near the edges, rectangle) areas around each pixel instead of using a
 * convolution matrix.
 *
 * The algorithm is adaptive, which means that is is able to add less contrast
 * for areas that already have a high local contrast.
 *
 * @author Knut Arild Erstad
 */
public class LocalContrastOperation extends ImageToImageOperation {
    public static final int MAX_ADAPTIVE = 100;
    private static final double AMOUNT_DIVISOR = 100.0;
    
    private int radius;
    private int amount;
    private int adaptive;
    
    /**
     * Set the parameters for the local contrast enhancement operation.
     * @param size
     *      Determines the size of the (square) area used for calculating
     *      averages.  The actual size in pixels will be <code>2*size+1</code>,
     *      or less near the edges.
     * @param amount
     *      The amount of local contrast to add.
     * @param adaptive
     *      How adaptive the algorithm should be, from 0 to 100.
     *      Higher values will cause areas that already have high contrast to
     *      get less added contrast, and will also reduce the overall contrast added.
     *      Note that a value of 100 will effectively disable any contrast changes.
     */
    public void setModifiers(int size, int amount, int adaptive) {
        this.radius = size;
        this.amount = amount;
        this.adaptive = adaptive;
    }

    public void process() throws MissingParameterException,
    WrongParameterException {
        PixelImage pinput = getInputImage();
        if (pinput == null)
            throw new MissingParameterException("missing input image");
        if (!(pinput instanceof RGB24Image || pinput instanceof RGB48Image))
            throw new WrongParameterException(
                "unsupported image type: must be RGB24Image or RGB48Image");
        int width = pinput.getWidth();
        int height = pinput.getHeight();
        PixelImage poutput = pinput.createCompatibleImage(width, height);
        IntegerImage input = (IntegerImage) pinput;
        IntegerImage output = (IntegerImage) poutput;
        int bitDepth = input.getBitsPerPixel() / input.getNumChannels();
        double maxSample = (1 << bitDepth) - 1;
        boolean is16Bit = (bitDepth == 16);
        double realAmount = amount / AMOUNT_DIVISOR;
        double adaptiveFactor = ((double) adaptive) / MAX_ADAPTIVE;
        // Transform adaptiveFactor through sine to get a better resolution
        // in the high end of the [0.0 1.0] interval
        adaptiveFactor = Math.sin(0.5 * Math.PI * adaptiveFactor);
        MultiLineHSLData hslData = new MultiLineHSLData(input, radius, maxSample);
        double[] hslSample = new double[3];
        double[] rgbSample = new double[3];
        for (int y = 0; y < Math.min(height, radius); y++) {
            hslData.addLine(y);
        }
        for (int y = 0; y < height; y++) {
            if (y > radius)
                hslData.popLine();
            if (y + radius < height)
                hslData.addLine(y+radius);
            hslData.updateHslLineData();
            for (int x = 0; x < width; x++) {
                double origR = input.getSample(RGBIndex.INDEX_RED, x, y) / maxSample;
                double origG = input.getSample(RGBIndex.INDEX_GREEN, x, y) / maxSample;
                double origB = input.getSample(RGBIndex.INDEX_BLUE, x, y) / maxSample;
                ColorUtil.rgbToHsl(origR, origG, origB, hslSample);
                double originalLightness = hslSample[2];
                double blurredLightness = hslData.lightnessLineData[x];
                // rgbDiff is a measurement of how much local contrast there
                // already is at this pixel.
                double rdiff = Math.abs(origR - hslData.redLineData[x]);
                double gdiff = Math.abs(origG - hslData.greenLineData[x]);
                double bdiff = Math.abs(origB - hslData.blueLineData[x]);
                double rgbDiff = (rdiff + gdiff + bdiff) / 3;
                double subtract = Math.pow(rgbDiff, 1 - adaptiveFactor);
                double newAmount = Math.max(0.0, realAmount * (1 - subtract));
                double originalMult = newAmount + 1;
                double newMult = (1 - originalMult);
                double newLightness = originalMult * originalLightness +
                    newMult * blurredLightness;
                ColorUtil.hslToRgb(hslSample[0], hslSample[1], newLightness,
                        rgbSample);
                if (is16Bit) {
                    output.putSample(RGBIndex.INDEX_RED, x, y,
                            clampToUnsignedShort((int) (rgbSample[0] * maxSample)));
                    output.putSample(RGBIndex.INDEX_GREEN, x, y,
                            clampToUnsignedShort((int) (rgbSample[1] * maxSample)));
                    output.putSample(RGBIndex.INDEX_BLUE, x, y,
                            clampToUnsignedShort((int) (rgbSample[2] * maxSample)));
                }
                else {
                    output.putSample(RGBIndex.INDEX_RED, x, y,
                            clampToUnsignedByte((int) (rgbSample[0] * maxSample)));
                    output.putSample(RGBIndex.INDEX_GREEN, x, y,
                            clampToUnsignedByte((int) (rgbSample[1] * maxSample)));
                    output.putSample(RGBIndex.INDEX_BLUE, x, y,
                            clampToUnsignedByte((int) (rgbSample[2] * maxSample)));
                }
            }
            setProgress(y, height);
        }
        setOutputImage(output);
    }
}
