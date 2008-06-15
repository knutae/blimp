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

    private void checkCoercedValid(ExifTag tag, Object value, Object expectedValue) {
        try {
            ExifField field = new ExifField(tag, value);
            assertEquals(expectedValue, field.valueAt(0));
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
    }

    @Test
    public void testValidFieldsWithCoercedValue() {
        checkCoercedValid(ExifTag.XResolution, 123, new Rational(123, 1));
    }

    private static long getLong(byte[] data, int offset, int count) {
        long result = 0;
        for (int i = offset+count-1; i >= offset; --i) {
            result = (result << 8) + (data[i] & 0xff);
        }
        return result;
    }

    private static int getShort(byte[] data, int offset) {
        return (int) getLong(data, offset, 2);
    }

    private static int getInt(byte[] data, int offset) {
        return (int) getLong(data, offset, 4);
    }

    private static void checkString(byte[] data, int offset, String expected) {
        String sub = new String(data, offset, expected.length());
        assertEquals(expected, sub);
    }

    private static void checkShort(byte[] data, int offset, int expected) {
        assertEquals(expected, getShort(data, offset));
    }

    private static void checkInt(byte[] data, int offset, int expected) {
        assertEquals(expected, getInt(data, offset));
    }

    private static void checkOffset(byte[] data, int offset) {
        assertTrue("offset must be even, was " + offset,
                offset % 2 == 0);
        assertTrue("offset out of range: " + offset,
                offset >= 8 && offset < data.length);
    }

    @Test
    public void testBinaryWriter() throws ValidationError {
        ExifTable table = new ExifTable();
        table.put(new ExifField(ExifTag.Model, "Bobly"));
        table.put(new ExifField(ExifTag.Artist, "Bach"));
        table.put(new ExifField(ExifTag.ISOSpeedRatings, 100));
        table.put(new ExifField(ExifTag.ShutterSpeedValue, new Rational(1, 50)));

        // Check the binary data, using as little as possible of the
        // Exif classes.
        byte[] origdata = BlobCreator.dataFromExifTable(table);
        // Check data (little endian expected)
        checkString(origdata, 0, "Exif\0\0");
        // Strip "Exif\0\0" header to make offset calculation easier
        byte[] data = new byte[origdata.length-6];
        System.arraycopy(origdata, 6, data, 0, data.length);
        checkString(data, 0, "II");
        checkShort(data, 2, 42);
        int offset = getInt(data, 4);
        checkOffset(data, offset);
        int exifOffset = -1;
        // 0th IFD
        int count = getShort(data, offset);
        assertEquals(3, count); // Model, Artist and Exif pointer
        offset += 2;
        for (int i=0; i<count; ++i) {
            int tag = getShort(data, offset);
            int type = getShort(data, offset + 2);
            int valueCount = getInt(data, offset + 4);
            int value = getInt(data, offset + 8);
            if (tag == 272) {
                // Model
                assertEquals(2, type); // ASCII
                assertEquals(6, valueCount);
                checkOffset(data, value);
                checkString(data, value, "Bobly\0");
            }
            else if (tag == 315) {
                // Artist
                assertEquals(2, type); // ASCII
                assertEquals(5, valueCount);
                checkOffset(data, value);
                checkString(data, value, "Bach\0");
            }
            else if (tag == 34665) {
                // Exif IFD pointer
                assertEquals(4, type); // LONG
                assertEquals(1, valueCount);
                checkOffset(data, value);
                exifOffset = value;
            }
            else {
                fail("unexpected tag " + tag);
            }
            offset += 12;
        }
        checkInt(data, offset, 0); // last IFD pointer

        // Check Exif IFD
        offset = exifOffset;
        checkOffset(data, offset);
        count = getShort(data, offset);
        assertEquals(3, count); // Exif Version, ISO Speed, Shutter Speed
        offset += 2;
        for (int i=0; i<count; ++i) {
            int tag = getShort(data, offset);
            int type = getShort(data, offset + 2);
            int valueCount = getInt(data, offset + 4);
            int value = getInt(data, offset + 8);
            if (tag == 36864) {
                // Exif Version
                assertEquals(7, type); // UNDEFINED
                assertEquals(4, valueCount);
                checkString(data, offset + 8, "0220");
            }
            else if (tag == 34855) {
                // ISO Speed Ratings
                assertEquals(3, type); // SHORT
                assertEquals(1, valueCount);
                assertEquals(100, value);
            }
            else if (tag == 37377) {
                // Shutter speed
                assertEquals(10, type); // SRATIONAL
                assertEquals(1, valueCount);
                checkOffset(data, value);
                // check rational value (1/50) at value offset
                checkInt(data, value, 1);
                checkInt(data, value+4, 50);
            }
            else {
                fail("unexpected tag " + tag);
            }
            offset += 12;
        }
    }
}
