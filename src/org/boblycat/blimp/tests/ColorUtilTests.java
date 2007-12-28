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

import static org.boblycat.blimp.ColorUtil.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ColorUtilTests {
    private boolean almostEquals(double x, double y) {
        return Math.abs(x - y) < 1e-10;
    }

    private boolean vecAlmostEquals(double[] v1, double[] v2) {
        if (v1.length != v2.length)
            return false;
        for (int i=0; i<v1.length; i++)
            if (!almostEquals(v1[i], v2[i]))
                return false;
        return true;
    }

    private boolean tripletAlmostEquals(double x, double y, double z, double[] vec) {
        return vecAlmostEquals(new double[] {x, y, z}, vec);
    }

    private void assertColorAlmostEquals(double x, double y, double z, double[] result) {
        assertTrue(
                String.format("[%f %f %f] ~= [%f %f %f]",
                        x, y, z, result[0], result[1], result[2]),
                tripletAlmostEquals(x, y, z, result));
    }

    private void checkRgbHsvRoundtrip(double r, double g, double b) {
        double[] hsv = rgbToHsv(r, g, b, null);
        double[] rgb = hsvToRgb(hsv[0], hsv[1], hsv[2], null);
        assertColorAlmostEquals(r, g, b, rgb);
        double[] tmp = new double[3];
        rgbToHsv(r, g, b, tmp);
        hsvToRgb(tmp[0], tmp[1], tmp[2], tmp);
        assertColorAlmostEquals(r, g, b, tmp);
    }

    private void checkHsvRgbRoundtrip(double h, double s, double v) {
        double[] rgb = hsvToRgb(h, s, v, null);
        double[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2], null);
        assertColorAlmostEquals(h, s, v, hsv);
        double[] tmp = new double[3];
        hsvToRgb(h, s, v, tmp);
        rgbToHsv(tmp[0], tmp[1], tmp[2], tmp);
        assertColorAlmostEquals(h, s, v, tmp);
    }

    private void checkRgbHslRoundtrip(double r, double g, double b) {
        double[] hsl = rgbToHsl(r, g, b, null);
        double[] rgb = hslToRgb(hsl[0], hsl[1], hsl[2], null);
        assertColorAlmostEquals(r, g, b, rgb);
        double[] tmp = new double[3];
        rgbToHsl(r, g, b, tmp);
        hslToRgb(tmp[0], tmp[1], tmp[2], tmp);
        assertColorAlmostEquals(r, g, b, tmp);
    }

    private void checkHslRgbRoundtrip(double h, double s, double l) {
        double[] rgb = hslToRgb(h, s, l, null);
        double[] hsl = rgbToHsl(rgb[0], rgb[1], rgb[2], null);
        assertColorAlmostEquals(h, s, l, hsl);
        double[] tmp = new double[3];
        hslToRgb(h, s, l, tmp);
        rgbToHsl(tmp[0], tmp[1], tmp[2], tmp);
        assertColorAlmostEquals(h, s, l, tmp);
    }

    @Test
    public void testRgbToHsv() {
        // black
        assertColorAlmostEquals(UNDEFINED_HUE, 0, 0, rgbToHsv(0, 0, 0, null));
        // gray
        assertColorAlmostEquals(UNDEFINED_HUE, 0, 0.5, rgbToHsv(0.5, 0.5, 0.5, null));
        // white
        assertColorAlmostEquals(UNDEFINED_HUE, 0, 1.0, rgbToHsv(1, 1, 1, null));
        // red
        assertColorAlmostEquals(0, 1, 1, rgbToHsv(1, 0, 0, null));
        // yellow
        assertColorAlmostEquals(60, 1, 1, rgbToHsv(1, 1, 0, null));
        // green
        assertColorAlmostEquals(120, 1, 1, rgbToHsv(0, 1, 0, null));
        // cyan
        assertColorAlmostEquals(180, 1, 1, rgbToHsv(0, 1, 1, null));
        // blue
        assertColorAlmostEquals(240, 1, 1, rgbToHsv(0, 0, 1, null));
        // magenta
        assertColorAlmostEquals(300, 1, 1, rgbToHsv(1, 0, 1, null));
    }

    @Test
    public void testHsvToRgb() {
        // black
        assertColorAlmostEquals(0, 0, 0, hsvToRgb(UNDEFINED_HUE, 0, 0, null));
        // gray
        assertColorAlmostEquals(0.5, 0.5, 0.5, hsvToRgb(UNDEFINED_HUE, 0, 0.5, null));
        // white
        assertColorAlmostEquals(1, 1, 1, hsvToRgb(UNDEFINED_HUE, 0, 1, null));
        // red
        assertColorAlmostEquals(1, 0, 0, hsvToRgb(0, 1, 1, null));
        // yellow
        assertColorAlmostEquals(1, 1, 0, hsvToRgb(60, 1, 1, null));
        // green
        assertColorAlmostEquals(0, 1, 0, hsvToRgb(120, 1, 1, null));
        // cyan
        assertColorAlmostEquals(0, 1, 1, hsvToRgb(180, 1, 1, null));
        // blue
        assertColorAlmostEquals(0, 0, 1, hsvToRgb(240, 1, 1, null));
        // magenta
        assertColorAlmostEquals(1, 0, 1, hsvToRgb(300, 1, 1, null));
    }

    @Test
    public void testRgbHsvRoundtrips() {
        checkRgbHsvRoundtrip(0.0, 0.0, 0.0);
        checkRgbHsvRoundtrip(0.3, 0.3, 0.3);
        checkRgbHsvRoundtrip(1.0, 1.0, 1.0);
        checkRgbHsvRoundtrip(0.1, 0.2, 0.3);
        checkRgbHsvRoundtrip(0.6, 0.5, 0.4);
        checkRgbHsvRoundtrip(0.8, 0.9, 1.0);
        checkRgbHsvRoundtrip(1.0, 1.0, 1.0);
    }

    @Test
    public void testHsvRgbRoundtrips() {
        checkHsvRgbRoundtrip(UNDEFINED_HUE, 0.0, 0.0);
        checkHsvRgbRoundtrip(UNDEFINED_HUE, 0.0, 0.5);
        checkHsvRgbRoundtrip(UNDEFINED_HUE, 0.0, 1.0);
        checkHsvRgbRoundtrip(0, 0.1, 0.2);
        checkHsvRgbRoundtrip(10, 0.4, 0.5);
        checkHsvRgbRoundtrip(22, 1.0, 0.5);
        checkHsvRgbRoundtrip(42, 0.3, 1.0);
        checkHsvRgbRoundtrip(198, 1.0, 0.1);
        checkHsvRgbRoundtrip(301, 0.7, 0.22);
        checkHsvRgbRoundtrip(359, 0.55, 0.66);
    }

    @Test
    public void testRgbToHsl() {
        // black
        assertColorAlmostEquals(UNDEFINED_HUE, 0, 0, rgbToHsl(0, 0, 0, null));
        // gray
        assertColorAlmostEquals(UNDEFINED_HUE, 0, 0.5, rgbToHsl(0.5, 0.5, 0.5, null));
        // white
        assertColorAlmostEquals(UNDEFINED_HUE, 0, 1.0, rgbToHsl(1, 1, 1, null));
        // red
        assertColorAlmostEquals(0, 1, 0.5, rgbToHsl(1, 0, 0, null));
        // yellow
        assertColorAlmostEquals(60, 1, 0.5, rgbToHsl(1, 1, 0, null));
        // green
        assertColorAlmostEquals(120, 1, 0.5, rgbToHsl(0, 1, 0, null));
        // cyan
        assertColorAlmostEquals(180, 1, 0.5, rgbToHsl(0, 1, 1, null));
        // blue
        assertColorAlmostEquals(240, 1, 0.5, rgbToHsl(0, 0, 1, null));
        // magenta
        assertColorAlmostEquals(300, 1, 0.5, rgbToHsl(1, 0, 1, null));
        // light red
        assertColorAlmostEquals(0, 1, 0.75, rgbToHsl(1, 0.5, 0.5, null));
    }

    @Test
    public void testHslToRgb() {
        // black
        assertColorAlmostEquals(0, 0, 0, hslToRgb(UNDEFINED_HUE, 0, 0, null));
        // gray
        assertColorAlmostEquals(0.5, 0.5, 0.5, hslToRgb(UNDEFINED_HUE, 0, 0.5, null));
        // white
        assertColorAlmostEquals(1, 1, 1, hslToRgb(UNDEFINED_HUE, 0, 1, null));
        // red
        assertColorAlmostEquals(1, 0, 0, hslToRgb(0, 1, 0.5, null));
        // yellow
        assertColorAlmostEquals(1, 1, 0, hslToRgb(60, 1, 0.5, null));
        // green
        assertColorAlmostEquals(0, 1, 0, hslToRgb(120, 1, 0.5, null));
        // cyan
        assertColorAlmostEquals(0, 1, 1, hslToRgb(180, 1, 0.5, null));
        // blue
        assertColorAlmostEquals(0, 0, 1, hslToRgb(240, 1, 0.5, null));
        // magenta
        assertColorAlmostEquals(1, 0, 1, hslToRgb(300, 1, 0.5, null));
        // light red
        assertColorAlmostEquals(1, 0.5, 0.5, hslToRgb(0, 1, 0.75, null));
    }

    @Test
    public void testRgbHslRoundtrips() {
        checkRgbHslRoundtrip(0.0, 0.0, 0.0);
        checkRgbHslRoundtrip(0.3, 0.3, 0.3);
        checkRgbHslRoundtrip(1.0, 1.0, 1.0);
        checkRgbHslRoundtrip(0.1, 0.2, 0.3);
        checkRgbHslRoundtrip(0.6, 0.5, 0.4);
        checkRgbHslRoundtrip(0.8, 0.9, 1.0);
        checkRgbHslRoundtrip(1.0, 1.0, 1.0);
    }

    @Test
    public void testHslRgbRoundtrips() {
        checkHslRgbRoundtrip(UNDEFINED_HUE, 0.0, 0.0);
        checkHslRgbRoundtrip(UNDEFINED_HUE, 0.0, 0.5);
        checkHslRgbRoundtrip(UNDEFINED_HUE, 0.0, 1.0);
        checkHslRgbRoundtrip(0, 0.1, 0.2);
        checkHslRgbRoundtrip(10, 0.4, 0.5);
        checkHslRgbRoundtrip(22, 1.0, 0.5);
        checkHslRgbRoundtrip(42, 0.3, 0.95);
        checkHslRgbRoundtrip(198, 1.0, 0.1);
        checkHslRgbRoundtrip(301, 0.7, 0.22);
        checkHslRgbRoundtrip(359, 0.55, 0.66);
    }
}
