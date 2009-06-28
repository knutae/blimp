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

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;

public class CroppedIntegerImage extends WrappedIntegerImage {
    
    protected int x1;
    protected int y1;
    protected int x2;
    protected int y2;
    
    public CroppedIntegerImage(IntegerImage other, int x1, int y1, int x2, int y2) {
        super(other);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        checkBounds();
    }
    
    private void checkBounds() {
        // TODO: improve error messages
        if (x1 < 0)
            throw new IllegalArgumentException("x1 < 0");
        if (x2 < x1)
            throw new IllegalArgumentException("x2 < x1");
        if (otherIntegerImage.getWidth() < x2)
            throw new IllegalArgumentException("width < x2");
        if (y1 < 0)
            throw new IllegalArgumentException("y1 < 0");
        if (y2 < y1)
            throw new IllegalArgumentException("y2 < y1");
        if (otherIntegerImage.getHeight() < y2)
            throw new IllegalArgumentException("height < y2");
    }

    public int getSample(int x, int y) {
        return otherIntegerImage.getSample(x1+x, y1+y);
    }

    public int getSample(int channel, int x, int y) {
        return otherIntegerImage.getSample(channel, x1+x, y1+y);
    }

    public void getSamples(int channelIndex, int x, int y, int w, int h,
            int[] dest, int destOffs) {
        otherIntegerImage.getSamples(channelIndex, x1+x, y1+y, w, h, dest, destOffs);
    }

    public PixelImage createCopy() {
        return new CroppedIntegerImage(otherIntegerImage, x1, y1, x2, y2);
    }

    public int getHeight() {
        return y2 - y1;
    }

    public int getWidth() {
        return x2 - x1;
    }

}
