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
    public void setTablesFromSpline(NaturalCubicSpline spline, int bitDepth) {
        assert (bitDepth == 8 || bitDepth == 16);
        int size = 1 << bitDepth;
        int[] table = new int[size];
        double[] splineValues = spline.getSplineValues(0.0, 1.0, size);
        assert(splineValues.length == size);
        for (int i=0; i<size; i++) {
            int y = (int) (splineValues[i] * (size-1));
            table[i] = Util.constrainedValue(y, 0, size-1);
        }
        setTables(table);
    }
}
