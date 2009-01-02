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
package org.boblycat.blimp;

/**
 * This class creates and holds histograms for an RGB image.
 * Separate red, green and blue histograms are created.
 *
 * @author Knut Arild Erstad
 */
public class RGBHistograms {
    Histogram hRed;
    Histogram hGreen;
    Histogram hBlue;

    public RGBHistograms(Bitmap bitmap) {
        hRed = new Histogram(RGBChannel.Red, bitmap);
        hGreen = new Histogram(RGBChannel.Green, bitmap);
        hBlue = new Histogram(RGBChannel.Blue, bitmap);
    }

    public Histogram getHistogram(RGBChannel channel) {
        switch (channel) {
        case Red:
            return hRed;
        case Green:
            return hGreen;
        case Blue:
            return hBlue;
        }
        return null;
    }
}
