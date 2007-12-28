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

import net.sourceforge.jiu.ops.LookupTableOperation;

/**
 * Operation that uses a {@link NaturalCubicSpline} to create its
 * lookup tables.
 *
 * @author Knut Arild Erstad
 */
public class SplineOperation extends LookupTableOperation {
    /**
     * Set all tables to spline values, using the values from from 0 to 1.
     * @param spline a spline.
     * @param bitDepth the bit depth per color channel, must be 8 or 16.
     */
    public void setTablesFromSpline(NaturalCubicSpline spline, int bitDepth,
            RGBChannel channel) {
        assert (bitDepth == 8 || bitDepth == 16);
        int size = 1 << bitDepth;
        int[] table = new int[size];
        double[] splineValues = spline.getSplineValues(0.0, 1.0, size);
        assert(splineValues.length == size);
        for (int i=0; i<size; i++) {
            int y = (int) (splineValues[i] * (size-1));
            table[i] = Util.constrainedValue(y, 0, size-1);
        }
        if (channel == null || channel.toJiuIndex() < 0) {
            setTables(table);
            return;
        }
        setNumTables(3);
        int[] identityTable = new int[size];
        for (int i=0; i<size; i++)
            identityTable[i] = i;
        int rgbIndex = channel.toJiuIndex();
        for (int i=0; i<3; i++) {
            if (i == rgbIndex)
                setTable(i, table);
            else
                setTable(i, identityTable);
        }
    }
}
