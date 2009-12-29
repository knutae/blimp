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

import static org.boblycat.blimp.util.Util.*;

import org.junit.*;
import static org.junit.Assert.*;

public class UtilTests {
    @Test
    public void testFixPointDecimalToStringNoDigits() {
        assertEquals("0", fixPointDecimalToString(0, 0));
        assertEquals("42", fixPointDecimalToString(42, 0));
        assertEquals("-137", fixPointDecimalToString(-137, 0));
    }

    @Test
    public void testFixPointDecimalToStringOneDigit() {
        assertEquals("0.0", fixPointDecimalToString(0, 1));
        assertEquals("0.2", fixPointDecimalToString(2, 1));
        assertEquals("22.3", fixPointDecimalToString(223, 1));
        assertEquals("-0.3", fixPointDecimalToString(-3, 1));
        assertEquals("-13.0", fixPointDecimalToString(-130, 1));
    }

    @Test
    public void testFixPointDecimalToStringTwoDigits() {
        assertEquals("0.00", fixPointDecimalToString(0, 2));
        assertEquals("0.07", fixPointDecimalToString(7, 2));
        assertEquals("3.14", fixPointDecimalToString(314, 2));
        assertEquals("-0.03", fixPointDecimalToString(-3, 2));
        assertEquals("-0.40", fixPointDecimalToString(-40, 2));
        assertEquals("-1.23", fixPointDecimalToString(-123, 2));
        assertEquals("-40.02", fixPointDecimalToString(-4002, 2));
    }

    @Test
    public void testValueOfFixPointDecimalNoDigits() {
        assertEquals(0, valueOfFixPointDecimal("0", 0));
        assertEquals(0, valueOfFixPointDecimal("0.0", 0));
        assertEquals(0, valueOfFixPointDecimal("0.4", 0));
        assertEquals(0, valueOfFixPointDecimal("-0.4", 0));
        assertEquals(42, valueOfFixPointDecimal("42", 0));
        assertEquals(-3, valueOfFixPointDecimal("-3.14", 0));
        assertEquals(-13, valueOfFixPointDecimal("-12.6", 0));
    }

    @Test
    public void testValueOfFixPointDecimalOneDigit() {
        assertEquals(0, valueOfFixPointDecimal("0", 1));
        assertEquals(0, valueOfFixPointDecimal("0.0", 1));
        assertEquals(4, valueOfFixPointDecimal("0.4", 1));
        assertEquals(-4, valueOfFixPointDecimal("-0.4", 1));
        assertEquals(420, valueOfFixPointDecimal("42", 1));
        assertEquals(100, valueOfFixPointDecimal("9.97", 1));
        assertEquals(-31, valueOfFixPointDecimal("-3.14", 1));
        assertEquals(-126, valueOfFixPointDecimal("-12.6", 1));
    }

    @Test
    public void testValueOfFixPointDecimalTwoDigits() {
        assertEquals(0, valueOfFixPointDecimal("0", 2));
        assertEquals(0, valueOfFixPointDecimal("0.0", 2));
        assertEquals(40, valueOfFixPointDecimal("0.4", 2));
        assertEquals(-40, valueOfFixPointDecimal("-0.4", 2));
        assertEquals(4200, valueOfFixPointDecimal("42", 2));
        assertEquals(1000, valueOfFixPointDecimal("9.997", 2));
        assertEquals(-314, valueOfFixPointDecimal("-3.14", 2));
        assertEquals(-1260, valueOfFixPointDecimal("-12.6", 2));
        assertEquals(103, valueOfFixPointDecimal("1.025", 2));
    }

    @Test
    public void testChangeFileExtension() {
        assertEquals("foo.bar", changeFileExtension("foo", "bar"));
        assertEquals("foo.bar", changeFileExtension("foo.bar", "bar"));
        assertEquals("foo.bar", changeFileExtension("foo.something", "bar"));
        assertEquals("FOO.BAR", changeFileExtension("FOO.BAR", "bar"));
        assertEquals(".bar", changeFileExtension("", "bar"));
        assertEquals("/some.path/foo.bar", changeFileExtension("/some.path/foo", "bar"));
        assertEquals("/some.path/foo.bar", changeFileExtension("/some.path/foo.bar", "bar"));
        assertEquals("/some.path/foo.bar", changeFileExtension("/some.path/foo.something", "bar"));
        assertEquals("foo", changeFileExtension("foo", ""));
        assertEquals("foo", changeFileExtension("foo.bar", ""));
        assertEquals("", changeFileExtension(".bar", ""));
        assertEquals("", changeFileExtension("", ""));
    }
}
