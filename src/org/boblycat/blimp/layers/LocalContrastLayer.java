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

import java.util.ArrayList;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Debug;
import org.boblycat.blimp.jiuops.ColorUtil;
import org.boblycat.blimp.jiuops.MathUtil;

import static org.boblycat.blimp.jiuops.MathUtil.*;

class MultiLineData {
    IntegerImage image;
    int width;
    int height;
    ArrayList<int[]> activeLineData;
    int[] removedLine;
    int[] combined;
    int channel;

    MultiLineData(IntegerImage image, int channel) {
        this.image = image;
        this.channel = channel;
        width = image.getWidth();
        height = image.getHeight();
        activeLineData = new ArrayList<int[]>();
        combined = new int[width];
    }

    void addLine(int y) {
        int[] lineData;
        if (removedLine != null) {
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

    void updateHslLineData() {
        int[] combinedRed = redData.getCombinedData();
        int[] combinedGreen = greenData.getCombinedData();
        int[] combinedBlue = blueData.getCombinedData();
        int numLines = redData.getNumLines();
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

class LocalContrastOperation extends ImageToImageOperation {
    static final double AMOUNT_DIVISOR = 100.0;
    int radius;
    int amount;
    int adaptive;

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
        boolean is16Bit = (output instanceof RGB48Image);
        double maxSample;
        if (is16Bit)
            maxSample = (1 << 16) - 1;
        else
            maxSample = (1 << 8) - 1;
        double realAmount = amount / AMOUNT_DIVISOR;
        double adaptiveFactor = ((double) adaptive) / LocalContrastLayer.MAX_ADAPTIVE;
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

/**
 * A layer for adding contrast in local areas within an image.
 *
 * The local contrast enchancement is implemented like an unsharp mask filter
 * with a big radius, but optimized by taking the average intensity of square
 * areas around each pixel instead of using a convolution matrix.
 *
 * The algorithm is adaptive, which means that is is able to add less contrast
 * for areas that already have a high local contrast.
 *
 * @author Knut Arild Erstad
 */
public class LocalContrastLayer extends AdjustmentLayer {
    public static final int MIN_AMOUNT = 1;
    public static final int MIN_RADIUS = 1;
    public static final int MIN_ADAPTIVE = 0;
    public static final int MAX_AMOUNT = 1000;
    public static final int MAX_RADIUS = 1000;
    public static final int MAX_ADAPTIVE = 100;
    private int radius = 100;
    private int amount = 100;
    private int adaptive = 70;

    @Override
    public Bitmap applyLayer(Bitmap source) {
        LocalContrastOperation op = new LocalContrastOperation();
        op.radius = (int) (radius / source.getPixelScaleFactor());
        Debug.print(this, "defined radius: " + radius);
        Debug.print(this, "used radius: " + op.radius);
        op.amount = amount;
        op.adaptive = adaptive;
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Local Contrast Enhancement";
    }

    public void setRadius(int radius) {
        this.radius = MathUtil.clamp(radius, MIN_RADIUS, MAX_RADIUS);
    }

    public int getRadius() {
        return radius;
    }

    public void setAmount(int level) {
        this.amount = MathUtil.clamp(level, MIN_AMOUNT, MAX_AMOUNT);
    }

    public int getAmount() {
        return amount;
    }

    public void setAdaptive(int adaptive) {
        this.adaptive = MathUtil.clamp(adaptive, MIN_ADAPTIVE, MAX_ADAPTIVE);
    }

    public int getAdaptive() {
        return adaptive;
    }

}
