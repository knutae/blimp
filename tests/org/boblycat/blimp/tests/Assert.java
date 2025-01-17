/*
 * Copyright (C) 2007, 2008, 2009, 2010 Knut Arild Erstad
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

import static org.junit.Assert.*;

/**
 * Wraps JUnit Assert functionality.
 *
 * @author Knut Arild Erstad
 */
public class Assert {
    /**
     * Pretty much like the deprecated assertEquals(double, double) variant, which
     * is sometimes still useful in my opinion.
     * @param expected the expected value
     * @param actual the actual value
     */
    public static void assertEqualsD(double expected, double actual) {
        // Use a very small delta value instead of an exact match.
        assertEquals(expected, actual, 1e-100);
    }
}
