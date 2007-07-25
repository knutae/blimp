package org.boblycat.blimp;

public class ColorUtil {
    public static final double UNDEFINED_HUE = -1;
    
    public static double[] rgbToHsv(double r, double g, double b,
            double hsv[]) {
        double h, s, v;
        double min = Util.min(r, g, b);
        double max = Util.max(r, g, b);
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
}
