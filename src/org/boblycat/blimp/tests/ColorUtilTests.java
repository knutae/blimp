package org.boblycat.blimp.tests;

import static org.boblycat.blimp.ColorUtil.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ColorUtilTests {
    private void assertColorEquals(double x, double y, double z, double[] result) {
        assertEquals(3, result.length);
        assertEquals(x, result[0]);
        assertEquals(y, result[1]);
        assertEquals(z, result[2]);
    }
    
    private void assertAlmostEquals(double x, double y) {
        assertTrue(String.format("%.20f ~= %.20f", x, y),
                Math.abs(x - y) < 1e-10);
    }
    
    private void assertColorAlmostEquals(double x, double y, double z, double[] result) {
        assertEquals(3, result.length);
        assertAlmostEquals(x, result[0]);
        assertAlmostEquals(y, result[1]);
        assertAlmostEquals(z, result[2]);
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
    
    @Test
    public void testRgbToHsv() {
        // black
        assertColorEquals(UNDEFINED_HUE, 0, 0, rgbToHsv(0, 0, 0, null));
        // gray
        assertColorEquals(UNDEFINED_HUE, 0, 0.5, rgbToHsv(0.5, 0.5, 0.5, null));
        // white
        assertColorEquals(UNDEFINED_HUE, 0, 1.0, rgbToHsv(1, 1, 1, null));
        // red
        assertColorEquals(0, 1, 1, rgbToHsv(1, 0, 0, null));
        // yellow
        assertColorEquals(60, 1, 1, rgbToHsv(1, 1, 0, null));
        // green
        assertColorEquals(120, 1, 1, rgbToHsv(0, 1, 0, null));
        // cyan
        assertColorEquals(180, 1, 1, rgbToHsv(0, 1, 1, null));
        // blue
        assertColorEquals(240, 1, 1, rgbToHsv(0, 0, 1, null));
        // magenta
        assertColorEquals(300, 1, 1, rgbToHsv(1, 0, 1, null));
    }
    
    @Test
    public void testHsvToRgb() {
        // black
        assertColorEquals(0, 0, 0, hsvToRgb(UNDEFINED_HUE, 0, 0, null));
        // gray
        assertColorEquals(0.5, 0.5, 0.5, hsvToRgb(UNDEFINED_HUE, 0, 0.5, null));
        // white
        assertColorEquals(1, 1, 1, hsvToRgb(UNDEFINED_HUE, 0, 1, null));
        // red
        assertColorEquals(1, 0, 0, hsvToRgb(0, 1, 1, null));
        // yellow
        assertColorEquals(1, 1, 0, hsvToRgb(60, 1, 1, null));
        // green
        assertColorEquals(0, 1, 0, hsvToRgb(120, 1, 1, null));
        // cyan
        assertColorEquals(0, 1, 1, hsvToRgb(180, 1, 1, null));
        // blue
        assertColorEquals(0, 0, 1, hsvToRgb(240, 1, 1, null));
        // magenta
        assertColorEquals(1, 0, 1, hsvToRgb(300, 1, 1, null));
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
}
