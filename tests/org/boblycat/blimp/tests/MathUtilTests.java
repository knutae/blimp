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
package org.boblycat.blimp.tests;

import static org.boblycat.blimp.util.MathUtil.*;

import org.junit.*;
import static org.junit.Assert.*;

public class MathUtilTests {
    @Test
    public void testPositivePositiveModulus() {
        assertEquals(2.0, mod(5.0, 3.0));
        assertEquals(1.5, mod(5.0, 3.5));
        assertEquals(0.0, mod(1000.0, 2.0));
        assertEquals(1.0, mod(1000.0, 3.0));
        assertEquals(2.25, mod(1000.0, 3.25));
        assertEquals(0.25, mod(1000.0, 0.75));
    }
    
    @Test
    public void testNegativePositiveModulus() {
        assertEquals(1.0, mod(-5.0, 3.0));
        assertEquals(2.0, mod(-5.0, 3.5));
        assertEquals(-0.0, mod(-1000.0, 2.0));
        assertEquals(2.0, mod(-1000.0, 3.0));
        assertEquals(1.0, mod(-1000.0, 3.25));
        assertEquals(0.5, mod(-1000.0, 0.75));
    }

    @Test
    public void testPositiveNegativeModulus() {
        assertEquals(-1.0, mod(5.0, -3.0));
        assertEquals(-2.0, mod(5.0, -3.5));
        assertEquals(0.0, mod(1000.0, -2.0));
        assertEquals(-2.0, mod(1000.0, -3.0));
        assertEquals(-1.0, mod(1000.0, -3.25));
        assertEquals(-0.5, mod(1000.0, -0.75));
    }

    @Test
    public void testNegativeNegativeModulus() {
        assertEquals(-2.0, mod(-5.0, -3.0));
        assertEquals(-1.5, mod(-5.0, -3.5));
        assertEquals(-0.0, mod(-1000.0, -2.0));
        assertEquals(-1.0, mod(-1000.0, -3.0));
        assertEquals(-2.25, mod(-1000.0, -3.25));
        assertEquals(-0.25, mod(-1000.0, -0.75));
    }
}
