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

public class ColorUtil {
    public static final double UNDEFINED_HUE = -1;
    
    private static final double ONE_THIRD = 1.0/3.0;
    private static final double ONE_SIXTH = 1.0/6.0;
    private static final double TWO_THIRDS = 2.0/3.0;
    
    /**
     * Optimized min function for three doubles.
     * @param x a number
     * @param y another number
     * @param z yet another number
     * @return the smallest of the three numbers.
     */
    private static double min(double x, double y, double z) {
        if (x < y) {
            if (x < z)
                return x;
            else
                return z;
        }
        if (y < z)
            return y;
        else
            return z; 
    }
    
    /**
     * Optimized max function for three doubles.
     * @param x a number
     * @param y another number
     * @param z yet another number
     * @return the largest of the three numbers.
     */
    private static double max(double x, double y, double z) {
        if (x > y) {
            if (x > z)
                return x;
            else
                return z;
        }
        if (y > z)
            return y;
        else
            return z; 
    }
    
    public static double[] rgbToHsv(double r, double g, double b,
            double hsv[]) {
        double h, s, v;
        double min = min(r, g, b);
        double max = max(r, g, b);
        v = max;
        double delta = max - min;
        if (max != 0)
            s = delta / max;
        else
            s = 0;
        if (s == 0) {
            h = UNDEFINED_HUE;
        }
        else {
            assert(delta > 0);
            if (r == max)
                h = (g - b) / delta; 
            else if (g == max)
                h = 2 + (b - r) / delta; // cyan to yellow
            else
                h = 4 + (r - g) / delta; // magenta to cyan
            // convert h to degrees
            h *= 60;
            if (h < 0)
                h += 360;
        }
        if (hsv != null) {
            assert(hsv.length >= 3);
            hsv[0] = h;
            hsv[1] = s;
            hsv[2] = v;
            return hsv;
        }
        return new double[] {h, s, v};
    }
    
    public static double[] hsvToRgb(double h, double s, double v,
            double[] rgb) {
        double r, g, b;
        if (s == 0) {
            // black or gray
            r = v;
            g = v;
            b = v;
        }
        else {
            h /= 60; // sector 0 to 5
            double di = Math.floor(h);
            int i = (int) di;
            double f = h - di;
            double p = v * (1 - s);
            double q = v * (1 - s * f);
            double t = v * (1 - s * (1 - f));
            switch (i) {
            case 0:
                r = v; g = t; b = p;
                break;
            case 1:
                r = q; g = v; b = p;
                break;
            case 2:
                r = p; g = v; b = t;
                break;
            case 3:
                r = p; g = q; b = v;
                break;
            case 4:
                r = t; g = p; b = v;
                break;
            default:
                r = v; g = p; b = q;
                break;
            }
        }
        if (rgb != null) {
            assert(rgb.length >= 3);
            rgb[0] = r;
            rgb[1] = g;
            rgb[2] = b;
            return rgb;
        }
        return new double[] {r, g, b};
    }

    /**
     * Convert a color from a RGB color space to the corresponding HSL
     * (Hue, Saturation, Lightness) color space.
     * See http://en.wikipedia.org/wiki/HSL_color_space for a description
     * of the algorithm used.
     * @param r red, 0 <= r <= 1
     * @param g green, 0 <= g <= 1
     * @param b blue, 0 <= b <= 1
     * @param hsl a result array, or <code>null</code>
     * @return a result array, either <code>hsl</code> or a newly allocated one.
     */
    public static double[] rgbToHsl(double r, double g, double b,
            double hsl[]) {
        double H, S, L;
        double min = min(r, g, b);
        double max = max(r, g, b);
        double delta = max - min;
        L = 0.5 * (min + max);
        if (L == 0 || max == min)
            S = 0;
        else if (L <= 0.5)
            S = 0.5 * delta / L;
        else
            S = 0.5 * delta / (1 - L);
        
        if (S == 0) {
            H = UNDEFINED_HUE;
        }
        else {
            assert(delta > 0);
            if (r == max)
                H = (g - b) / delta; 
            else if (g == max)
                H = 2 + (b - r) / delta; // cyan to yellow
            else
                H = 4 + (r - g) / delta; // magenta to cyan
            // convert h to degrees
            H *= 60;
            if (H < 0)
                H += 360;
        }
        if (hsl != null) {
            assert(hsl.length >= 3);
            hsl[0] = H;
            hsl[1] = S;
            hsl[2] = L;
            return hsl;
        }
        return new double[] {H, S, L};
    }
    
    private static double tcolor(double Tc, double Q, double P) {
        if (Tc < ONE_SIXTH)
            return P + 6 * (Q - P) * Tc;
        else if (Tc < 0.5)
            return Q;
        else if (Tc < TWO_THIRDS)
            return P + 6 * (Q - P) * (TWO_THIRDS - Tc);
        else
            return P;
    }

    /**
     * Convert a color from a HSL (Hue, Saturation, Lightness) to the
     * corresponding RGB color space.
     * See http://en.wikipedia.org/wiki/HSL_color_space for a description of
     * the algorithm used.
     * @param H hue, 0 <= H < 360, or UNDEFINED_HUE
     * @param S saturation, 0 <= S <= 1
     * @param L lightness (luminance), 0 <= L <= 1
     * @param rgb a result array, or <code>null</code>.
     * @return a result array, either <code>rgb</code> or a newly allocated one.
     */
    public static double[] hslToRgb(double H, double S, double L,
            double[] rgb) {
        double r, g, b;
        if (S == 0) {
            // black or gray
            r = L;
            g = L;
            b = L;
        }
        else {
            double Q;
            if (L < 0.5)
                Q = L * (1 + S);
            else
                Q = L + S - L * S;
            double P = 2 * L - Q;
            double Hk = H / 360;
            double Tr = Hk + ONE_THIRD;
            if (Tr >= 1)
                Tr -= 1;
            double Tg = Hk;
            double Tb = Hk - ONE_THIRD;
            if (Tb < 0)
                Tb += 1;
            r = tcolor(Tr, Q, P);
            g = tcolor(Tg, Q, P);
            b = tcolor(Tb, Q, P);
        }
        if (rgb != null) {
            assert(rgb.length >= 3);
            rgb[0] = r;
            rgb[1] = g;
            rgb[2] = b;
            return rgb;
        }
        return new double[] {r, g, b};
    }
}
