/*
 * Copyright (C) 2007, 2008 Knut Arild Erstad
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

import org.boblycat.blimp.exif.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ExifTests {
    @Test
    public void testFieldAscii() {
        ExifField field = new ExifField(42, ExifDataType.ASCII);
        field.setStringValue("test123");
        assertEquals("test123", field.getStringValue());
        assertEquals(8, field.getCount()); // string length + null byte
        assertEquals(8, field.getByteCount());
        assertNull(field.getValues());
    }

    @Test
    public void testFieldUndefined() {
        ExifField field = new ExifField(42, ExifDataType.UNDEFINED);
        field.setStringValue("test123");
        assertEquals("test123", field.getStringValue());
        assertEquals(7, field.getCount());
        assertEquals(7, field.getByteCount());
        assertNull(field.getValues());
    }

    @Test
    public void testFieldByte() {
        ExifField field = new ExifField(42, ExifDataType.BYTE);
        field.addValue(43);
        field.addValue(13);
        assertEquals(43, field.valueAt(0));
        assertEquals(13, field.valueAt(1));
        assertEquals(2, field.getCount());
        assertEquals(2, field.getByteCount());
        assertNull(field.getStringValue());
    }

    @Test
    public void testFieldShort() {
        ExifField field = new ExifField(42, ExifDataType.SHORT);
        field.addValue(1337);
        assertEquals(1337, field.valueAt(0));
        assertEquals(1, field.getCount());
        assertEquals(2, field.getByteCount());
    }

    @Test
    public void testFieldLong() {
        ExifField field = new ExifField(42, ExifDataType.LONG);
        field.addValue(1337);
        field.addValue(1773);
        assertEquals(1337, field.valueAt(0));
        assertEquals(1773, field.valueAt(1));
        assertEquals(2, field.getCount());
        assertEquals(8, field.getByteCount());
    }

    @Test
    public void testFieldRational() {
        ExifField field = new ExifField(42, ExifDataType.RATIONAL);
        field.addValue(new Rational(3, 500));
        assertEquals(new Rational(3, 500), field.valueAt(0));
        assertEquals(1, field.getCount());
        assertEquals(8, field.getByteCount());
    }

    private void checkValid(ExifTag tag, Object value) {
        try {
            ExifField field = new ExifField(tag, value);
            if (value instanceof String)
                assertEquals(value, field.getStringValue());
            else
                assertEquals(value, field.valueAt(0));
        }
        catch (ValidationError e) {
            fail(e.getMessage());
        }
    }

    private void checkInvalid(ExifTag tag, Object value) {
        try {
            new ExifField(tag, value);
            fail("Expected a validation error for " + tag + " with value " + value
                    + " of type " + value.getClass().getSimpleName());
        }
        catch (ValidationError e) {
            // pass
        }
    }

    @Test
    public void testValidFieldsByTag() {
        checkValid(ExifTag.Software, "Boblycat");
        checkValid(ExifTag.Exif_IFD_Pointer, 12345);
        checkValid(ExifTag.ImageWidth, 800);
        checkValid(ExifTag.XResolution, new Rational(123, 1));
    }

    @Test
    public void testInvalidFieldsByTag() {
        checkInvalid(ExifTag.Software, 41139);
        checkInvalid(ExifTag.ImageWidth, "800");
        checkInvalid(ExifTag.XResolution, "123");
        // TODO: coerce types to make the following work
        checkInvalid(ExifTag.XResolution, 123);
    }
}
