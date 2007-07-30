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
package org.boblycat.blimp.tests;

import org.boblycat.blimp.NaturalCubicSpline;
import org.junit.*;
import static org.junit.Assert.*;

public class SplineTests {
    @Test
    public void testZeroPoints() {
        NaturalCubicSpline spline = new NaturalCubicSpline();
        assertEquals(0.0, spline.getSplineValue(-0.5));
        assertEquals(0.0, spline.getSplineValue(0.0));
        assertEquals(0.0, spline.getSplineValue(0.5));
        assertEquals(0.0, spline.getSplineValue(1.0));
        assertEquals(0.0, spline.getSplineValue(1.5));
    }

    @Test
    public void testOnePoint() {
        NaturalCubicSpline spline = new NaturalCubicSpline();
        spline.addPoint(0.1, 0.2);
        assertEquals(0.2, spline.getSplineValue(-0.5));
        assertEquals(0.2, spline.getSplineValue(0.0));
        assertEquals(0.2, spline.getSplineValue(0.5));
        assertEquals(0.2, spline.getSplineValue(1.0));
        assertEquals(0.2, spline.getSplineValue(1.5));
    }

    @Test
    public void testTwoPoints() {
        NaturalCubicSpline spline = new NaturalCubicSpline();
        spline.addPoint(0.0, 0.0);
        spline.addPoint(1.0, 1.0);
        // assertEquals(0.0, spline.getSplineValue(-0.5));
        assertTrue(spline.getSplineValue(-0.5) <= 0.0);
        assertEquals(0.0, spline.getSplineValue(0.0));
        assertEquals(0.1, spline.getSplineValue(0.1));
        assertEquals(0.5, spline.getSplineValue(0.5));
        assertEquals(1.0, spline.getSplineValue(1.0));
        // assertEquals(1.0, spline.getSplineValue(1.5));
        assertTrue(spline.getSplineValue(1.5) >= 1.0);
    }

    @Test
    public void testThreePoints() {
        NaturalCubicSpline spline = new NaturalCubicSpline();
        spline.addPoint(0.0, 0.0);
        spline.addPoint(0.5, 0.2);
        spline.addPoint(1.0, 1.0);

        // assertEquals(0.0, spline.getSplineValue(-0.5));
        assertTrue(spline.getSplineValue(-0.5) <= 0.0);
        assertEquals(0.0, spline.getSplineValue(0.0));
        assertEquals(0.2, spline.getSplineValue(0.5));
        assertEquals(1.0, spline.getSplineValue(1.0));
        // assertEquals(1.0, spline.getSplineValue(1.5));
        assertTrue(spline.getSplineValue(1.5) >= 1.0);

        double y;
        y = spline.getSplineValue(0.25);
        assertTrue(0 < y);
        assertTrue(y < 0.2);
        y = spline.getSplineValue(0.75);
        assertTrue(0.2 < y);
        assertTrue(y < 1.0);
    }
}
