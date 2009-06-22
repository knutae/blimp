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

/**
 * Base class for providing a read-only transformed view of an integer image.
 * 
 * @author Knut Arild Erstad
 */
public abstract class WrappedIntegerImage implements IntegerImage {
    protected IntegerImage otherIntegerImage;
    
    protected void readOnlyError(String operation) throws RuntimeException {
        throw new RuntimeException(getClass().getSimpleName() + " is read-only, cannot "
                + operation);
    }
    
    public WrappedIntegerImage(IntegerImage other) {
        this.otherIntegerImage = other;
    }

    @Override
    public void clear(int newValue) {
        readOnlyError("clear");
    }

    @Override
    public void clear(int channelIndex, int newValue) {
        readOnlyError("clear");
    }

    @Override
    public int getMaxSample(int channel) {
        return otherIntegerImage.getMaxSample(channel);
    }

    @Override
    public void putSample(int x, int y, int newValue) {
        readOnlyError("put samples");
    }

    @Override
    public void putSample(int channel, int x, int y, int newValue) {
        readOnlyError("put samples");
    }

    @Override
    public void putSamples(int channel, int x, int y, int w, int h, int[] src,
            int srcOffset) {
        readOnlyError("put samples");
    }

    @Override
    public long getAllocatedMemory() {
        // Is this useful for wrapped images?
        return 0;
    }

    @Override
    public int getBitsPerPixel() {
        return otherIntegerImage.getBitsPerPixel();
    }

    @Override
    public Class<?> getImageType() {
        return otherIntegerImage.getImageType();
    }

    @Override
    public int getNumChannels() {
        return otherIntegerImage.getNumChannels();
    }

    @Override
    public PixelImage createCompatibleImage(int width, int height) {
        return otherIntegerImage.createCompatibleImage(width, height);
    }
}
