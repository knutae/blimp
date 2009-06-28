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

import net.sourceforge.jiu.data.RGB24Image;

public class CroppedRGB24Image extends CroppedIntegerImage implements
        RGB24Image {

    private RGB24Image otherImage;

    public CroppedRGB24Image(RGB24Image other, int x1, int y1, int x2, int y2) {
        super(other, x1, y1, x2, y2);
        otherImage = other;
    }

    public void clear(byte newValue) {
        readOnlyError("clear");
    }

    public void clear(int channelIndex, byte newValue) {
        readOnlyError("clear");
    }

    public byte getByteSample(int x, int y) {
        return otherImage.getByteSample(x+x1, y+y1);
    }

    public byte getByteSample(int channel, int x, int y) {
        return otherImage.getByteSample(channel, x+x1, y+y1);
    }

    public void getByteSamples(int channelIndex, int x, int y, int w, int h,
            byte[] dest, int destOffset) {
        otherImage.getByteSamples(channelIndex, x1+x, y1+y, w, h, dest, destOffset);
    }

    public void putByteSample(int x, int y, byte newValue) {
        readOnlyError("put samples");
    }

    public void putByteSample(int channel, int x, int y, byte newValue) {
        readOnlyError("put samples");
    }

    public void putByteSamples(int channel, int x, int y, int w, int h,
            byte[] src, int srcOffset) {
        readOnlyError("put samples");
    }
}
