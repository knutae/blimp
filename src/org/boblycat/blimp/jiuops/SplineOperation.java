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

import org.boblycat.blimp.util.MathUtil;
import org.boblycat.blimp.util.NaturalCubicSpline;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Operation that uses a {@link NaturalCubicSpline} to create its
 * lookup tables.
 *
 * @author Knut Arild Erstad
 */
public class SplineOperation extends LookupTableOperation {
    private int channelIndex;
    private NaturalCubicSpline spline;
    
    public SplineOperation() {
        channelIndex = -1;
    }
    
    /**
     * Set the spline to use for creating lookup tables.
     * @param spline
     */
    public void setSpline(NaturalCubicSpline spline) {
        this.spline = spline;
    }
    
    /**
     * Set the channel to modify, or -1 to modify all channels (default).
     * @param channelIndex
     */
    public void setChannel(int channelIndex) {
        this.channelIndex = channelIndex;
    }
    
    public void process() throws MissingParameterException,
    WrongParameterException {
        if (spline == null)
            throw new MissingParameterException("no spline");
        PixelImage input = getInputImage();
        if (input == null)
            throw new MissingParameterException("no input image");
        // Create and set tables based on the bit depth of the input image
        int bitDepth = input.getBitsPerPixel() / input.getNumChannels();
        int size = 1 << bitDepth;
        int[] table = new int[size];
        double[] splineValues = spline.getSplineValues(0.0, 1.0, size);
        assert(splineValues.length == size);
        for (int i=0; i<size; i++) {
            int y = (int) (splineValues[i] * (size-1));
            table[i] = MathUtil.clamp(y, 0, size-1);
        }
        if (channelIndex < 0) {
            setTables(table);
        }
        else {
            // We only want a single channel to be affected.  Since null
            // tables are not allowed, create an "identity table" for
            // all other channels.
            setNumTables(input.getNumChannels());
            int[] identityTable = new int[size];
            for (int i=0; i<size; i++)
                identityTable[i] = i;
            for (int i=0; i<input.getNumChannels(); i++) {
                if (i == channelIndex)
                    setTable(i, table);
                else
                    setTable(i, identityTable);
            }
        }
        // the superclass does the actual work
        super.process();
    }
}
