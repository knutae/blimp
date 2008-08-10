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
package org.boblycat.blimp.exif;

/**
 * A rational type used by the ExifField class.
 *
 * @author Knut Arild Erstad
 */
public class Rational {
    int numerator;
    int denominator;

    public Rational(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public String toString() {
        return Integer.toString(numerator) + '/' + Integer.toString(denominator);
    }
    
    public double toDouble() {
        if (denominator == 0)
            return Double.NaN;
        return ((double) numerator) / (double) denominator;
    }
    
    public String toDecimalString() {
        if (denominator == 0)
            return "NaN";
        if (denominator == 1 || numerator == 0)
            return Integer.toString(numerator);
        return Double.toString(toDouble());
    }
    
    public String toSimpleString() {
        if (denominator == 0)
            return "NaN";
        if (denominator == 1 || numerator == 0)
            return Integer.toString(numerator);
        return toString();
    }

    public boolean equals(Rational other) {
        return (numerator == other.numerator) && (denominator == other.denominator);
    }

    public boolean equals(Object other) {
        if (other instanceof Rational)
            return equals((Rational) other);
        return false;
    }
}
