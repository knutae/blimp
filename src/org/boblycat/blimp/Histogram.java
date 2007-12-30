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
    RGBChannel channel;

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

    public Histogram(RGBChannel channel, Bitmap bm) {
        this(DEFAULT_NUM_ENTRIES);
        this.channel = channel;
        getForChannel(bm, channel);
    }

    public RGBChannel getChannel() {
        return channel;
    }

    private void getForChannelRange(Bitmap bitmap, int startIndex, int endIndex) {
        IntegerImage image = (IntegerImage) bitmap.getImage();
        clear();
        for (int channel = startIndex; channel < endIndex; channel++) {
            creator.setImage(image, channel);
            try {
                creator.process();
            }
            catch (OperationFailedException e) {
                e.printStackTrace();
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

    /**
     * Generate a joint histogram for all channels in the bitmap.
     * @param bitmap a bitmap
     */
    public void getAllChannels(Bitmap bitmap) {
        getForChannelRange(bitmap, 0, bitmap.getImage().getNumChannels());
    }

    /**
     * Generate a histogram for the specified channel in the bitmap.
     * @param bitmap a bitmap
     * @param channel an RGB channel
     */
    public void getForChannel(Bitmap bitmap, RGBChannel channel) {
        int index = channel.toJiuIndex();
        if (index < 0)
            getAllChannels(bitmap);
        else
            getForChannelRange(bitmap, index, index+1);
    }
}
