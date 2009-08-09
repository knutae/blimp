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

import org.boblycat.blimp.data.ZoomFactor;
import org.junit.*;
import static org.junit.Assert.*;

public class ZoomTests {
    @Test
    public void testDefaultValue() {
        ZoomFactor zoom = new ZoomFactor();
        assertEquals(1, zoom.getMultiplier());
        assertEquals(1, zoom.getDivisor());
    }

    @Test
    public void testZoomIn() {
        ZoomFactor zoom = new ZoomFactor();
        for (int i = 2; i <= 10; i++) {
            zoom.zoomIn();
            assertEquals(i, zoom.getMultiplier());
            assertEquals(1, zoom.getDivisor());
        }
    }

    @Test
    public void testZoomInThenOut() {
        ZoomFactor zoom = new ZoomFactor();
        for (int i = 2; i <= 10; i++) {
            zoom.zoomIn();
        }
        for (int i = 9; i >= 1; i--) {
            zoom.zoomOut();
            assertEquals(i, zoom.getMultiplier());
            assertEquals(1, zoom.getDivisor());
        }
    }

    // 1/d divisors after zooming further out than 2/3
    static final int[] divisors = { 2, 3, 4, 6, 8, 12, 16, 20, 24 };

    @Test
    public void testZoomOut() {
        ZoomFactor zoom = new ZoomFactor();
        zoom.zoomOut();
        assertEquals(2, zoom.getMultiplier());
        assertEquals(3, zoom.getDivisor());
        for (int div : divisors) {
            zoom.zoomOut();
            assertEquals(1, zoom.getMultiplier());
            assertEquals(div, zoom.getDivisor());
        }
    }

    @Test
    public void testZoomOutThenIn() {
        ZoomFactor zoom = new ZoomFactor();
        for (int i = 0; i < divisors.length + 2; i++) {
            zoom.zoomOut();
        }
        for (int i = divisors.length - 1; i >= 0; i--) {
            zoom.zoomIn();
            assertEquals(1, zoom.getMultiplier());
            assertEquals(divisors[i], zoom.getDivisor());
        }
        zoom.zoomIn();
        assertEquals(2, zoom.getMultiplier());
        assertEquals(3, zoom.getDivisor());
        zoom.zoomIn();
        assertEquals(1, zoom.getMultiplier());
        assertEquals(1, zoom.getDivisor());
    }
}
