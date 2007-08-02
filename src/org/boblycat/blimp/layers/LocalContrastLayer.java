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

import java.util.Vector;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.ColorUtil;
import org.boblycat.blimp.Debug;
import org.boblycat.blimp.Util;

class MultiLineData {
    IntegerImage image;
    int width;
    int height;
    Vector<int[]> activeLineData;
    int[] removedLine;
    int[] combined;
    
    MultiLineData(IntegerImage image) {
        this.image = image;
        width = image.getWidth();
        height = image.getHeight();
        activeLineData = new Vector<int[]>();
        combined = new int[width];
    }
    
    void addLine(int channel, int y) {
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

class MultiLineHslData {
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
    
    MultiLineHslData(IntegerImage image, int radius, double maxSample) {
        this.image = image;
        this.radius = radius;
        this.maxSample = maxSample;
        width = image.getWidth();
        height = image.getHeight();
        redData = new MultiLineData(image);
        greenData = new MultiLineData(image);
        blueData = new MultiLineData(image);
        lightnessLineData = new double[width];
        redLineData = new double[width];
        greenLineData = new double[width];
        blueLineData = new double[width];
    }
    
    void addLine(int y) {
        redData.addLine(RGBIndex.INDEX_RED, y);
        greenData.addLine(RGBIndex.INDEX_GREEN, y);
        blueData.addLine(RGBIndex.INDEX_BLUE, y);
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

/**
 * Local contrast enhancement, implemented like an unsharp mask filter with a big radius,
 * but optimized by taking the average intensity of square areas around each sample instead
 * of using a convolution matrix.
 * 
 * @author Knut Arild Erstad
 */
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
        MultiLineHslData hslData = new MultiLineHslData(input, radius, maxSample);
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
                double origLightness = hslSample[2];
                double blurredLightness = hslData.lightnessLineData[x];
                double lightnessDiff = Math.abs(origLightness - blurredLightness);
                // rgbDiff is a measurement of how much local contrast there
                // already is at this pixel.
                double rgbDiff = (
                        Math.abs(origR - hslData.redLineData[x]) +
                        Math.abs(origG - hslData.greenLineData[x]) +
                        Math.abs(origB - hslData.blueLineData[x])) / 3;
                double adaptiveExponent = (1 - (1 - rgbDiff) * adaptiveFactor);
                lightnessDiff = Math.pow(lightnessDiff, adaptiveExponent);
                double newAmount = realAmount * (1 - lightnessDiff);
                double originalMult = newAmount + 1;
                double newMult = (1 - originalMult);
                double newLightness = originalMult * origLightness +
                    newMult * blurredLightness;
                if (x == 100 && y < 50)
                    System.out.println("y " + y
                            + " orig " + origLightness
                            + " blurred " + blurredLightness
                            + " new " + newLightness);
                ColorUtil.hslToRgb(hslSample[0], hslSample[1], newLightness,
                        rgbSample);
                if (is16Bit) {
                    output.putSample(RGBIndex.INDEX_RED, x, y,
                            Util.cropToUnsignedShort((int) (rgbSample[0] * maxSample)));
                    output.putSample(RGBIndex.INDEX_GREEN, x, y,
                            Util.cropToUnsignedShort((int) (rgbSample[1] * maxSample)));
                    output.putSample(RGBIndex.INDEX_BLUE, x, y,
                            Util.cropToUnsignedShort((int) (rgbSample[2] * maxSample)));
                }
                else {
                    output.putSample(RGBIndex.INDEX_RED, x, y,
                            Util.cropToUnsignedByte((int) (rgbSample[0] * maxSample)));
                    output.putSample(RGBIndex.INDEX_GREEN, x, y,
                            Util.cropToUnsignedByte((int) (rgbSample[1] * maxSample)));
                    output.putSample(RGBIndex.INDEX_BLUE, x, y,
                            Util.cropToUnsignedByte((int) (rgbSample[2] * maxSample)));
                }
            }
            setProgress(y, height);
        }
        setOutputImage(output);
    }
    
}

public class LocalContrastLayer extends AdjustmentLayer {
    public static final int MIN_AMOUNT = 1;
    public static final int MIN_RADIUS = 1;
    public static final int MIN_ADAPTIVE = 0;
    public static final int MAX_AMOUNT = 400;
    public static final int MAX_RADIUS = 1000;
    public static final int MAX_ADAPTIVE = 100;
    private int radius = 100;
    private int amount = 50;
    private int adaptive = 0;
    
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
        this.radius = Util.constrainedValue(radius, MIN_RADIUS, MAX_RADIUS);
    }

    public int getRadius() {
        return radius;
    }

    public void setAmount(int level) {
        this.amount = Util.constrainedValue(level, MIN_AMOUNT, MAX_AMOUNT);
    }

    public int getAmount() {
        return amount;
    }

    public void setAdaptive(int adaptive) {
        this.adaptive = Util.constrainedValue(adaptive, MIN_ADAPTIVE, MAX_ADAPTIVE);
    }

    public int getAdaptive() {
        return adaptive;
    }

}
