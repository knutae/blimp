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

public class PointDouble {
    public double x;
    public double y;

    public PointDouble() {
    }

    public PointDouble(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toCommaString() {
        return Double.toString(x) + ',' + Double.toString(y);
    }

    public static PointDouble valueOfCommaString(String input)
            throws NumberFormatException {
        int comma = input.indexOf(',');
        if (comma < 0)
            throw new NumberFormatException("Missing comma in point: " + input);
        double x = Double.valueOf(input.substring(0, comma));
        double y = Double.valueOf(input.substring(comma + 1, input.length()));
        return new PointDouble(x, y);
    }
}
