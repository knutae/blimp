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

import net.sourceforge.jiu.data.RGB48Image;

public class CroppedRGB48Image extends CroppedIntegerImage implements
        RGB48Image {

    private RGB48Image otherImage;
    
    public CroppedRGB48Image(RGB48Image other, int x1, int y1, int x2, int y2) {
        super(other, x1, y1, x2, y2);
        otherImage = other;
    }

    public void clear(short newValue) {
        readOnlyError("clear");
    }

    public void clear(int channelIndex, short newValue) {
        readOnlyError("clear");
    }

    public short getShortSample(int x, int y) {
        return otherImage.getShortSample(x1+x, y1+y);
    }

    public short getShortSample(int channel, int x, int y) {
        return otherImage.getShortSample(channel, x1+x, y1+y);
    }

    public void getShortSamples(int channelIndex, int x, int y, int w, int h,
            short[] dest, int destOffset) {
        otherImage.getShortSamples(channelIndex, x1+x, y1+y, w, h, dest, destOffset);
    }

    public void putShortSample(int x, int y, short newValue) {
        readOnlyError("put samples");
    }

    public void putShortSample(int channel, int x, int y, short newValue) {
        readOnlyError("put samples");
    }

    public void putShortSamples(int channel, int x, int y, int w, int h,
            short[] src, int srcOffset) {
        readOnlyError("put samples");
    }
}
