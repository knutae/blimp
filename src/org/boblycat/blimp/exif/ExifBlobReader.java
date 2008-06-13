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

import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * A class for extracting Exif data from binary data (byte arrays).
 *
 * @author Knut Arild Erstad
 */
public class ExifBlobReader {
    byte[] data;
    int baseOffset;
    int currentOffset;
    int exifPointer;
    boolean bigEndian;

    public ExifBlobReader(byte[] data, int baseOffset) throws ReaderError {
        this.data = data;
        this.baseOffset = baseOffset;
        detectEndianness();
    }

    public ExifBlobReader(byte[] data) throws ReaderError {
        //System.out.println("ExifBlobReader... ");
        //String str = new String(data, 0, 100);
        //System.out.println("first bytes: " + str);
        this.data = data;
        detectBaseOffset();
        detectEndianness();
    }

    protected String extractAscii(int offset, int size, boolean nullTerminated) throws ReaderError {
        try {
            if (nullTerminated)
                size--;
            return new String(data, baseOffset + offset, size, "US-ASCII");
        }
        catch (UnsupportedEncodingException e) {
            throw new ReaderError("Ascii encoding not supported?", e);
        }
        catch (IndexOutOfBoundsException e) {
            throw new ReaderError("Premature end of data", e);
        }
    }

    protected long extractLong(int offset, int byteCount) throws ReaderError {
        assert(byteCount <= 8);
        try {
            offset += baseOffset;
            long result = 0;
            if (bigEndian) {
                for (int i = offset; i < offset+byteCount; i++) {
                    int byteValue = data[i] & 0xff;
                    //System.out.println("   byte val " + i + " : " + byteValue);
                    result = (result << 8) | byteValue;
                }
            }
            else {
                for (int i = offset+byteCount-1; i >= offset; i--) {
                    int byteValue = data[i] & 0xff;
                    //System.out.println("   byte val " + i + " : " + byteValue);
                    result = (result << 8) | byteValue;
                }
            }
            return result;
        }
        catch (IndexOutOfBoundsException e) {
            throw new ReaderError("Premature end of data", e);
        }
    }

    protected int extractInt(int offset, int byteCount) throws ReaderError {
        assert(byteCount <= 4);
        long result = extractLong(offset, byteCount);
        return (int) (result & 0xffffffff);
    }

    protected void detectEndianness() throws ReaderError {
        String byteOrderIndicator = extractAscii(0, 2, false);
        if (byteOrderIndicator.equals("MM"))
            bigEndian = true;
        else if (byteOrderIndicator.equals("II"))
            bigEndian = false;
        else
            throw new ReaderError("No byte order indicator found in Exif data");
        int answer = extractInt(2, 2);
        if (answer != 42)
            throw new ReaderError("Error in Exif header, expected 42 but got " + answer);
    }

    protected void detectBaseOffset() throws ReaderError {
        String tmp = extractAscii(0, 2, false);
        if (tmp.equals("MM") || tmp.equals("II")) {
            baseOffset = 0;
            return;
        }
        tmp = extractAscii(0, 6, false);
        if (tmp.equals("Exif\0\0")) {
            baseOffset = 6;
            return;
        }
        throw new ReaderError("Failed to detect a valid Exif header.");
    }

    protected ImageFileDirectory extractIFD() throws ReaderError {
        ImageFileDirectory ifd = new ImageFileDirectory();
        int fieldCount = extractInt(currentOffset, 2);
        currentOffset += 2;
        for (int n=0; n<fieldCount; n++) {
            // read a single field
            int tag = extractInt(currentOffset, 2);
            int typeTag = extractInt(currentOffset + 2, 2);
            int dataCount = extractInt(currentOffset+4, 4);
            // try to interpret type
            ExifDataType type = ExifDataType.fromTypeTag(typeTag);
            if (type == null)
                throw new ReaderError("Unrecognized Exif type " + typeTag);
            // If the value fits in 4 bytes, it is stored "inline", otherwise
            // an offset is stored.
            int valueOffset;
            if (type.getByteCount() * dataCount <= 4)
                valueOffset = currentOffset + 8;
            else
                valueOffset = extractInt(currentOffset + 8, 4);
            ExifField field = new ExifField(tag, type);
            switch (type) {
            case ASCII:
                field.setStringValue(extractAscii(valueOffset, dataCount, true));
                break;
            case UNDEFINED:
                field.setStringValue(extractAscii(valueOffset, dataCount, false));
                break;
            case BYTE:
                // TODO: implement something here...
                break;
            case SHORT:
            case LONG:
            case SLONG:
                for (int i=0; i<dataCount; i++) {
                    field.addValue(new Integer(extractInt(valueOffset, type.getByteCount())));
                    valueOffset += type.getByteCount();
                }
                break;
            case RATIONAL:
            case SRATIONAL:
                for (int i=0; i<dataCount; i++) {
                    int numer = extractInt(valueOffset, 4);
                    int denom = extractInt(valueOffset+4, 4);
                    field.addValue(new Rational(numer, denom));
                    valueOffset += 8;
                }
                break;
            }
            ifd.addField(field);
            if (field.getTag() == ExifTag.Exif_IFD_Pointer.getTag())
                exifPointer = (Integer) field.getValue();
            currentOffset += 12;
        }
        return ifd;
    }

    public Vector<ImageFileDirectory> extractIFDs() throws ReaderError {
        Vector<ImageFileDirectory> directories = new Vector<ImageFileDirectory>();
        currentOffset = extractInt(4, 4);
        while (currentOffset != 0) {
            //System.out.println("current offset: " + currentOffset);
            directories.add(extractIFD());
            currentOffset = extractInt(currentOffset, 4);
            if (currentOffset == 0 && exifPointer != 0) {
                //System.out.println("jumping to exif pointer: " + exifPointer);
                currentOffset = exifPointer;
                exifPointer = 0;
            }
        }
        return directories;
    }
}
