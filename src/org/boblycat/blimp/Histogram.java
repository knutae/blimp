package org.boblycat.blimp;

import net.sourceforge.jiu.color.analysis.Histogram1DCreator;
import net.sourceforge.jiu.color.data.ArrayHistogram1D;
import net.sourceforge.jiu.color.data.Histogram1D;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * A one-dimensional histogram.
 * This is a subclass of JIU's ArrayHistogram1D with some Blimp-specific
 * functions. 
 * 
 * @author Knut Arild Erstad
 */
public class Histogram extends ArrayHistogram1D {
    private static final int DEFAULT_NUM_ENTRIES = 256;
    
    Histogram1DCreator creator;
    
    public Histogram(int numEntries) {
        super(numEntries);
        creator = new Histogram1DCreator();
    }
    
    public Histogram() {
        this(DEFAULT_NUM_ENTRIES);
    }
    
    public Histogram(int numEntries, Bitmap bm) {
        this(numEntries);
        getAllChannels(bm);
    }
    
    public Histogram(Bitmap bm) {
        this(DEFAULT_NUM_ENTRIES, bm);
    }
    
    public void getAllChannels(Bitmap bitmap) {
        IntegerImage image = (IntegerImage) bitmap.getImage();
        clear();
        for (int channel=0; channel<image.getNumChannels(); channel++) {
            creator.setImage((IntegerImage) bitmap.getImage());
            try {
                creator.process();
            }
            catch (OperationFailedException e) {
                Util.err("Failed to create histogram for channel" + channel);
                return;
            }
            Histogram1D channelHistogram = creator.getHistogram();
            if (channelHistogram.getMaxValue() != getMaxValue()) {
                Util.err("Size mismatch while creating histogram: "
                        + channelHistogram.getMaxValue() + " <> "
                        + getMaxValue());
                return;
            }
            for (int i=0; i<getMaxValue(); i++) {
                setEntry(i, getEntry(i) + channelHistogram.getEntry(i));
            }
        }
    }
}
