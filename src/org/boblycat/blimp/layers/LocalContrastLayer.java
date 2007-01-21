package org.boblycat.blimp.layers;

import java.util.Vector;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.ShortChannelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import org.boblycat.blimp.Bitmap;
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

/**
 * Local contrast enhancement, implemented like an unsharp mask filter with a big radius,
 * but optimized by taking the average intensity of square areas around each sample instead
 * of using a convolution matrix.
 * 
 * @author Knut Arild Erstad
 */
class LocalContrastOperation extends ImageToImageOperation {
    static final double AMOUNT_DIVISOR = 100.0;
    static final double ADAPTIVE_MULTIPLIER = 10.0;
    int radius;
    int amount;
    int adaptive;
    
    public void process() throws MissingParameterException,
    WrongParameterException {
        PixelImage pinput = getInputImage();
        if (pinput == null)
            throw new MissingParameterException("missing input image");
        if (!(pinput instanceof IntegerImage))
            throw new WrongParameterException(
                "unsupported image type: must be IntegerImage");
        int width = pinput.getWidth();
        int height = pinput.getHeight();
        int channels = pinput.getNumChannels();
        PixelImage poutput = pinput.createCompatibleImage(width, height);
        IntegerImage input = (IntegerImage) pinput;
        IntegerImage output = (IntegerImage) poutput;
        boolean is16Bit = (output instanceof ShortChannelImage);
        int maxSample;
        if (is16Bit)
            maxSample = 1 << 16;
        else
            maxSample = 1 << 8;
        double realAmount = amount / AMOUNT_DIVISOR;
        double adaptiveExponent = ADAPTIVE_MULTIPLIER / (adaptive + ADAPTIVE_MULTIPLIER);
        assert(adaptiveExponent > 0 && adaptiveExponent <= 1);
        
        // Note: when increasing the adaptive parameter, also increase the amount,
        // otherwise the overall effect will be reduced too much.
        // TODO: figure out a way to do this that is mathematically sensible.
        realAmount = realAmount + Math.pow(realAmount, adaptiveExponent);
        
        for (int channel = 0; channel<channels; channel++) {
            MultiLineData activeLineData = new MultiLineData(input);
            
            for (int y = 0; y < Math.min(height, radius + 1); y++) {
                activeLineData.addLine(channel, y);
            }
            
            for (int y = 0; y < height; y++) {
                if (y > radius)
                    activeLineData.popLine();
                if (y + radius < height)
                    activeLineData.addLine(channel, y+radius);

                int[] combinedLineData = activeLineData.getCombinedData();
                int numLines = activeLineData.getNumLines();
                // an int can be too small for 16-bit depths with a large radius
                long value = 0;
                long divisor = 0;
                
                for (int x = 0; x < Math.min(width, radius+1); x++) {
                    value += combinedLineData[x];
                    divisor += numLines;
                }
                
                for (int x = 0; x < width; x++) {
                    if (x > radius) {
                        value -= combinedLineData[x-radius-1];
                        divisor -= numLines;
                    }
                    if (x + radius < width) {
                        value += combinedLineData[x+radius];
                        divisor += numLines;
                    }
                    assert(divisor > 0);
                    int originalSample = input.getSample(channel, x, y);
                    double blurredSample = ((double) value) / divisor;
                    double sampleDiff = Math.abs(originalSample - blurredSample);
                    double relativeDiff = ((double) sampleDiff) / maxSample; 
                    relativeDiff = Math.pow(relativeDiff, adaptiveExponent);
                    double newAmount = realAmount * (1 - relativeDiff);
                    double originalMult = newAmount + 1;
                    double newMult = (1 - originalMult);
                    double newValue = originalMult * originalSample +
                        newMult * blurredSample;
                    int newSample = (int) newValue;
                    if (is16Bit)
                        output.putSample(channel, x, y, Util.cropToUnsignedShort(newSample));
                    else
                        output.putSample(channel, x, y, Util.cropToUnsignedByte(newSample));
                }
            }
        }
        setOutputImage(output);
    }
    
}

public class LocalContrastLayer extends AdjustmentLayer {
    public static final int MIN_AMOUNT = 1;
    public static final int MIN_RADIUS = 1;
    public static final int MIN_ADAPTIVE = 0;
    public static final int MAX_AMOUNT = 10000;
    public static final int MAX_RADIUS = 1000;
    public static final int MAX_ADAPTIVE = 1000;
    private int radius = 50;
    private int amount = 30;
    private int adaptive = 0;
    
    @Override
    public Bitmap applyLayer(Bitmap source) {
        LocalContrastOperation op = new LocalContrastOperation();
        op.radius = radius;
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
