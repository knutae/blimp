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

import java.util.Iterator;
import java.util.TreeMap;

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
            // save multiplicator in a (but why?)
            // a[k] /= d[k-1];
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

    static private double[] subArray(double[] array, int startIndex, int count) {
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

    static private double arrayValueOrZero(double[] array, int index) {
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

    TreeMap<Double, Double> points;

    Coefficients coefficients;

    /**
     * Calculate the natural spline coefficients of the current points. The
     * coefficients consist of four arrays A, B, C and D so that p(x) = A[i] *
     * (x-X[i])^3 + B[i] * (x-X[i]]^2 + C[i] * (x-X[i]) + D[i] where X[i] <= x <=
     * X[i+i] and X[1..n+1] is the sorted array of x values in the control
     * points.)
     *
     * @return A wrapper of the coefficient vectors.
     */
    Coefficients calculateCoefficients() {
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

    void invalidateCoefficients() {
        coefficients = null;
    }

    public NaturalCubicSpline() {
        points = new TreeMap<Double, Double>();
    }

    public void addPoint(double x, double y) {
        points.put(x, y);
        invalidateCoefficients();
    }

    public void movePoint(double oldX, double newX, double newY) {
        if (!points.containsKey(oldX))
            return;
        points.remove(oldX);
        addPoint(newX, newY);
        invalidateCoefficients();
    }

    public double findClosestPoint(double x) {
        double closest = Double.MAX_VALUE;
        for (double px : points.keySet()) {
            if (Math.abs(x - px) < Math.abs(x - closest))
                closest = px;
        }
        return closest;
    }

    public void removePoint(double x) {
        points.remove(x);
        invalidateCoefficients();
    }

    public TreeMap<Double, Double> getPoints() {
        return points;
    }

    public void setPoints(TreeMap<Double, Double> points) {
        this.points = points;
        invalidateCoefficients();
    }

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
