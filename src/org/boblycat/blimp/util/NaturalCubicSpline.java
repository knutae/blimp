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
package org.boblycat.blimp.util;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * This class interpolates a sorted set of x-y-coordinates as a natural cubic
 * spline function.  That is, a cubic spline function in which the second
 * derivatives of the end points are zero.
 * 
 * @author Knut Arild Erstad
 */
public class NaturalCubicSpline {
    /**
     * Solve a tri-diagonal set of equations. The matrix is on the form:
     * <pre>
     * |  d0   c0                 :  b0  |
     * |  a1   d1   c1            :  b1  |
     * |       a2   d2   ..       :  ..  |
     * |            ..   ..  cN-2 : bN-2 |
     * |                aN-1 dN-1 : bN-1 |
     * </pre>
     * The numbers a0 and cN-1 are ignored. Note that the input arrays will be
     * modified by this function.
     *
     * @param va
     *            Lower diagonal vector.
     * @param vd
     *            Main diagonal vector.
     * @param vc
     *            Upper diagonal vector.
     * @param vb
     *            The right-hand vector.
     * @return A solution vector (x) for the matrix.
     */
    private static double[] solveTriDiagonalEquation(double[] va, double[] vd,
            double[] vc, double[] vb) {
        int size = va.length;
        assert (vd.length == size);
        assert (vc.length == size || vc.length == size - 1);
        assert (vb.length == size);
        // double[] x = new double[size];
        // Gauss elimination (eliminating a)
        for (int k = 1; k < size; k++) {
            double mult = va[k] / vd[k - 1];
            vd[k] -= mult * vc[k - 1];
            vb[k] -= mult * vb[k - 1];
        }
        // back substitution (eliminating c)
        for (int k = size - 1; k > 0; k--) {
            double mult = vc[k - 1] / vd[k];
            vb[k - 1] -= mult * vb[k];
        }
        // final step: divide b by d, then return b
        for (int k = 0; k < size; k++) {
            vb[k] /= vd[k];
        }
        return vb;
    }

    private static double[] subArray(double[] array, int startIndex, int count) {
        assert (startIndex >= 0);
        assert (startIndex < array.length || count == 0);
        if (count < 0) {
            count = array.length - startIndex;
        }
        assert (startIndex + count <= array.length);
        double[] ret = new double[count];
        System.arraycopy(array, startIndex, ret, 0, count);
        return ret;
    }

    private static double arrayValueOrZero(double[] array, int index) {
        if (index < 0 || index >= array.length)
            return 0.0;
        return array[index];
    }

    /**
     * Wraps the coefficient of a natural cubic spline.
     */
    class Coefficients {
        double[] a, b, c, d;
        int size;

        Coefficients(int length) {
            size = length;
            a = new double[length];
            b = new double[length];
            c = new double[length];
            d = new double[length];
        }
    }

    private TreeMap<Double, Double> points;
    private Coefficients coefficients;

    /**
     * Calculate the natural spline coefficients of the current points. The
     * coefficients consist of four arrays A, B, C and D so that p(x) = A[i] *
     * (x-X[i])^3 + B[i] * (x-X[i]]^2 + C[i] * (x-X[i]) + D[i] where X[i] <= x <=
     * X[i+i] and X[1..n+1] is the sorted array of x values in the control
     * points.)
     *
     * @return A wrapper of the coefficient vectors.
     */
    private Coefficients calculateCoefficients() {
        double[] vx = new double[points.size()];
        double[] vy = new double[points.size()];
        int i = 0;
        for (double x : points.keySet()) {
            vx[i] = x;
            vy[i] = points.get(x);
            i++;
        }
        int size = points.size() - 1;
        // H vector: differences between x values
        double[] vh = new double[size];
        for (i = 0; i < size; i++)
            vh[i] = vx[i + 1] - vx[i];
        // build tridiagonal matrix
        double[] ta = subArray(vh, 0, size - 1);
        double[] td = new double[size - 1];
        for (i = 0; i < size - 1; i++)
            td[i] = 2 * (vh[i] + vh[i + 1]);
        double[] tc = subArray(vh, 1, size - 1);
        double[] tb = new double[size - 1];
        for (i = 0; i < size - 1; i++) {
            double h0 = vh[i];
            double h1 = vh[i + 1];
            double y0 = vy[i];
            double y1 = vy[i + 1];
            double y2 = vy[i + 2];
            tb[i] = 6 * ((y2 - y1) / h1 - (y1 - y0) / h0);
        }
        double[] sol = solveTriDiagonalEquation(ta, td, tc, tb);
        Coefficients cof = new Coefficients(size);
        for (i = 0; i < size; i++) {
            double h0 = vh[i];
            double s0 = arrayValueOrZero(sol, i - 1);
            double s1 = arrayValueOrZero(sol, i);
            double y0 = vy[i];
            double y1 = vy[i + 1];
            cof.a[i] = (s1 - s0) / (6 * h0);
            cof.b[i] = s0 / 2;
            cof.c[i] = (y1 - y0) / h0 - ((2 * h0 * s0) + (h0 * s1)) / 6;
            cof.d[i] = y0;
        }
        return cof;
    }

    private void invalidateCoefficients() {
        coefficients = null;
    }

    public NaturalCubicSpline() {
        points = new TreeMap<Double, Double>();
    }

    /**
     * Add a point, or replace a point at the same x-coordinate. 
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void addPoint(double x, double y) {
        points.put(x, y);
        invalidateCoefficients();
    }

    /**
     * Move a point.
     * If another point exists on the same x-coordinate as <code>newX</code>,
     * it will be replaced.
     * @param oldX old x-coordinate
     * @param newX new x-coordinate
     * @param newY new y-coordinate
     */
    public void movePoint(double oldX, double newX, double newY) {
        if (!points.containsKey(oldX))
            return;
        points.remove(oldX);
        addPoint(newX, newY);
        invalidateCoefficients();
    }

    /**
     * Find the point whose x-coordinate is closest to <code>x</code>.
     * @param x x-coordinate
     * @return an existing x-coordinate or <code>Double.MAX_VALUE</code> if there are no points
     */
    public double findClosestPoint(double x) {
        double closest = Double.MAX_VALUE;
        for (double px : points.keySet()) {
            if (Math.abs(x - px) < Math.abs(x - closest))
                closest = px;
        }
        return closest;
    }

    /**
     * Remove a point.  If the point does not exist, this is a no-op.
     * @param x the x-coordinate of the point to remove
     */
    public void removePoint(double x) {
        points.remove(x);
        invalidateCoefficients();
    }

    /**
     * Returns the points as a tree map.
     * Modifying the return value can lead to undefined behavior.
     * @return a tree map of x-y-coordinates
     */
    public TreeMap<Double, Double> getPoints() {
        return points;
    }

    /**
     * Set the x-y-coordinates as a tree map.
     * Modifying the parameter after calling this function can lead to undefined behavior.
     * @param points a tree map of x-y-coordinates
     */
    public void setPoints(TreeMap<Double, Double> points) {
        this.points = points;
        invalidateCoefficients();
    }

    /**
     * Get the interpolated y-coordinate corresponding to x.
     * @param x an x-coordinate
     * @return the corresponding y-coordinate calculated using spline interpolation
     */
    public double getSplineValue(double x) {
        if (points.size() == 0)
            return 0;
        if (points.size() == 1)
            return points.values().iterator().next();
        if (coefficients == null)
            coefficients = calculateCoefficients();
        int i = 0;
        Iterator<Double> xiter = points.keySet().iterator();
        double xcur = xiter.next();
        double xnext = xiter.next();
        while (xnext < x && xiter.hasNext()) {
            i++;
            xcur = xnext;
            xnext = xiter.next();
        }
        assert (i < coefficients.size);
        double h = x - xcur;
        double hh = h * h;
        double hhh = hh * h;
        return coefficients.a[i] * hhh + coefficients.b[i] * hh
                + coefficients.c[i] * h + coefficients.d[i];
    }

    /**
     * Get an array of interpolated y-coordinates according to the range of x-coordinates
     * specified.  The resulting y-coordinates are the same as if calling
     * <code>getSplineValue(x)</code> for each <code>x</code> in the range, but
     * using this function is more efficient.
     * 
     * @param startX the start of the range of x-coordinates
     * @param endX the end of the range of x-coordinates
     * @param steps the number of steps
     * @return an array of size <code>steps</code> with y-coordinates
     */
    public double[] getSplineValues(double startX, double endX, int steps) {
        double[] values = new double[steps];
        if (points.size() == 0)
            return values;
        if (points.size() == 1) {
            double y = points.values().iterator().next();
            for (int i = 0; i < steps; i++)
                values[i] = y;
            return values;
        }
        if (coefficients == null)
            coefficients = calculateCoefficients();
        double xinterval = endX - startX;
        int cofIndex = 0;
        Iterator<Double> xiter = points.keySet().iterator();
        double xcur = xiter.next();
        double xnext = xiter.next();
        for (int step = 0; step < steps; step++) {
            double xdist = (step * xinterval) / steps;
            double x = startX + xdist;
            while (xnext < x && xiter.hasNext()) {
                cofIndex++;
                xcur = xnext;
                xnext = xiter.next();
            }
            assert (cofIndex < coefficients.size);
            double h = x - xcur;
            double hh = h * h;
            double hhh = hh * h;
            double y = coefficients.a[cofIndex] * hhh
                    + coefficients.b[cofIndex] * hh + coefficients.c[cofIndex]
                    * h + coefficients.d[cofIndex];
            values[step] = y;
        }
        return values;
    }
}
