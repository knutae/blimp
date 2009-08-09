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

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

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
        //assertNull(field.getValues());
        assertEquals('t', field.valueAt(0));
        assertEquals('e', field.valueAt(1));
        assertEquals('s', field.valueAt(2));
        assertEquals('t', field.valueAt(3));
        assertEquals('1', field.valueAt(4));
        assertEquals('2', field.valueAt(5));
        assertEquals('3', field.valueAt(6));
    }

    @Test
    public void testFieldUndefined() {
        ExifField field = new ExifField(42, ExifDataType.UNDEFINED);
        field.setStringValue("test123");
        assertEquals("test123", field.getStringValue());
        assertEquals(7, field.getCount());
        assertEquals(7, field.getByteCount());
        //assertNull(field.getValues());
        assertEquals('t', field.valueAt(0));
        assertEquals('e', field.valueAt(1));
        assertEquals('s', field.valueAt(2));
        assertEquals('t', field.valueAt(3));
        assertEquals('1', field.valueAt(4));
        assertEquals('2', field.valueAt(5));
        assertEquals('3', field.valueAt(6));
    }

    @Test
    public void testFieldByte() {
        ExifField field = new ExifField(42, ExifDataType.BYTE);
        field.setStringValue("\u002b\u000e");
        assertEquals(2, field.getCount());
        assertEquals(2, field.getByteCount());
        assertEquals(43, field.valueAt(0));
        assertEquals(14, field.valueAt(1));
        assertEquals("\u002b\u000e", field.getStringValue());
    }

    @Test
    public void testFieldSignedByte() {
        ExifField field = new ExifField(42, ExifDataType.SBYTE);
        field.setStringValue("\u0001\u00ff\u0007\u00ee");
        assertEquals(4, field.getCount());
        assertEquals(4, field.getByteCount());
        assertEquals(1, field.valueAt(0));
        assertEquals(-1, field.valueAt(1));
        assertEquals(7, field.valueAt(2));
        assertEquals(-18, field.valueAt(3));
        assertEquals("\u0001\u00ff\u0007\u00ee", field.getStringValue());
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

    private class ByteWriter {
        ByteArrayOutputStream stream;
        int offset;

        ByteWriter() {
            stream = new ByteArrayOutputStream();
            offset = 0;
        }

        void writeLong(long value, int count) {
            for (int i=0; i<count; ++i) {
                byte b = (byte) (value & 0xff);
                stream.write(b);
                value = value >> 8;
            }
            offset += count;
        }

        void writeInt(int value) {
            writeLong(value, 4);
        }

        void writeShort(int value) {
            writeLong(value, 2);
        }

        void writeByte(int value) {
            stream.write(value);
            offset++;
        }

        void writeString(String value) {
            for (int i=0; i<value.length(); ++i)
                stream.write(value.charAt(i));
            offset += value.length();
        }
    }

    @Test
    public void testBinaryReader() throws Exception {
        ByteWriter writer = new ByteWriter();
        // Write some Exif data
        // Offsets are pretty hard to do by hand, so use only inline values
        // (max 4 bytes) except for the Exif IFD pointer
        writer.writeString("II");
        writer.writeShort(42);
        writer.writeInt(8); // offset to 0th IFD

        // 0th IFD
        writer.writeShort(3); // field count
        // Image width
        writer.writeShort(256);
        writer.writeShort(3); // SHORT
        writer.writeInt(1);
        writer.writeInt(640);
        // Write a dummy field with an undefined data type (42)
        // the reader must be able to handle this by skipping the field
        writer.writeShort(32123);
        writer.writeShort(42); // data type not defined in TIFF
        writer.writeInt(1);
        writer.writeInt(55555);
        // Exif IFD pointer
        // offset = 8 (header) + 2 (field count) + 12 * 3 (fields) + 4 (IFD offset)
        int exifOffset = 8 + 2 + 12 * 3 + 4;
        writer.writeShort(34665);
        writer.writeShort(4); // LONG
        writer.writeInt(1);
        writer.writeInt(exifOffset);
        // next IFD offset
        writer.writeInt(0);

        // Exif IFD
        assertEquals(exifOffset, writer.offset);
        writer.writeShort(5); // field count
        // Exif Version (use the fictional version "2.2.9")
        writer.writeShort(36864);
        writer.writeShort(7); // UNDEFINED
        writer.writeInt(4);
        writer.writeString("0229");
        // Metering Mode
        writer.writeShort(37383);
        writer.writeShort(3); // SHORT
        writer.writeInt(1);
        writer.writeShort(3);
        writer.writeShort(0); // padding
        // MakerNote with binary data (hex 00 FF EE DD)
        writer.writeShort(37500);
        writer.writeShort(7); // UNDEFINED
        writer.writeInt(4);
        writer.writeByte(0x00);
        writer.writeByte(0xFF);
        writer.writeByte(0xEE);
        writer.writeByte(0xDD);
        // Unknown tag (but with a known data type)
        writer.writeShort(54321);
        writer.writeShort(3); // SHORT
        writer.writeInt(2); // two values, 13 and 37
        writer.writeShort(13);
        writer.writeShort(37);
        // Unknown tag with an empty string value, count = 0
        // (Maybe count should be 1, but this case has been observed in
        // real images and should be handled.)
        writer.writeShort(54322);
        writer.writeShort(2); // ASCII
        writer.writeInt(0);
        writer.writeInt(0);
        // next IFD offset (should this be necessary to write after Exif IFD?)
        writer.writeInt(0);

        // We're finished writing, test the reader
        byte[] data = writer.stream.toByteArray();
        ExifBlobReader reader = new ExifBlobReader(data);
        ExifTable table = reader.extractIFDTable();
        List<ImageFileDirectory> ifds = table.getMainIFDs();

        assertEquals(1, ifds.size());
        // Test 0th IFD
        ImageFileDirectory ifd = ifds.get(0);
        assertSame(ifd, table.getPrimaryIFD());
        assertEquals(2, ifd.size()); // unknown data type not included
        Iterator<ExifField> it = ifd.iterator();

        ExifField field = it.next();
        assertEquals(ExifTag.ImageWidth.getTag(), field.getTag());
        assertEquals(1, field.getCount());
        assertEquals(640, field.valueAt(0));

        field = it.next();
        assertEquals(ExifTag.Exif_IFD_Pointer.getTag(), field.getTag());
        assertEquals(1, field.getCount());

        ifd = table.getExifIFD();
        assertNotNull(ifd);
        assertEquals(5, ifd.size());
        it = ifd.iterator();

        field = it.next();
        assertEquals(ExifTag.ExifVersion.getTag(), field.getTag());
        assertEquals(4, field.getCount());
        assertEquals("0229", field.getStringValue());

        field = it.next();
        assertEquals(ExifTag.MeteringMode.getTag(), field.getTag());
        assertEquals(1, field.getCount());
        assertEquals(3, field.valueAt(0));

        field = it.next();
        assertEquals(ExifTag.MakerNote.getTag(), field.getTag());
        assertEquals(4, field.getCount());
        String value = field.getStringValue();
        assertEquals(4, value.length());
        assertEquals(0x00, (int) value.charAt(0));
        assertEquals(0xFF, (int) value.charAt(1));
        assertEquals(0xEE, (int) value.charAt(2));
        assertEquals(0xDD, (int) value.charAt(3));

        field = it.next();
        assertEquals(54321, field.getTag()); // unknown tag
        assertEquals(2, field.getCount());
        assertEquals(13, field.valueAt(0));
        assertEquals(37, field.valueAt(1));

        field = it.next();
        assertEquals(54322, field.getTag()); // unknown tag 2
        //assertEquals(0, field.getCount());
        assertEquals(1, field.getCount());
        assertEquals("", field.getStringValue());
    }

    @Test
    public void testIFD() throws Exception {
        ImageFileDirectory ifd = new ImageFileDirectory();
        assertEquals(0, ifd.size());
        ifd.addField(new ExifField(ExifTag.Software, "Blimp"));
        assertEquals(1, ifd.size());
        ifd.addField(new ExifField(ExifTag.Model, "Boblycat"));
        assertEquals(2, ifd.size());

        Iterator<ExifField> it = ifd.iterator();
        ExifField field = ifd.get(ExifTag.Software);
        assertNotNull(field);
        assertEquals("Blimp", field.getStringValue());
        assertSame(field, ifd.get(ExifTag.Software.getTag()));
        assertSame(field, it.next());

        field = ifd.get(ExifTag.Model);
        assertNotNull(field);
        assertEquals("Boblycat", field.getStringValue());
        assertSame(field, ifd.get(ExifTag.Model.getTag()));
        assertSame(field, it.next());

        // replace an existing field
        ifd.addField(new ExifField(ExifTag.Software, "iBlimp"));
        assertEquals(2, ifd.size());
        it = ifd.iterator();

        field = ifd.get(ExifTag.Software);
        assertNotNull(field);
        assertEquals("iBlimp", field.getStringValue());
        assertSame(field, ifd.get(ExifTag.Software.getTag()));
        assertSame(field, it.next());
    }
}
