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
package org.boblycat.blimp.exif;

import java.io.UnsupportedEncodingException;

/**
 * A base class for reading Exif or TIFF data from a binary source.
 *
 * @author Knut Arild Erstad
 */
public abstract class BinaryReader {
    protected boolean bigEndian;
    protected int baseOffset;

    public abstract long extractLong(int offset, int byteCount)
        throws ReaderError;
    public abstract String extractAscii(int offset, int size, boolean nullTerminated)
        throws ReaderError;

    public int extractInt(int offset, int byteCount) throws ReaderError {
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

    protected static String extractAsciiFromArray(byte[] array,
            int offset, int size, boolean nullTerminated) throws ReaderError {
        if (size == 0) {
            // special case that handles size == 0 even for null terminated strings
            return "";
        }
        try {
            int total = offset + size;
            if (array.length < total)
                throw new ReaderError("Premature end of data while extracting string");
            if (nullTerminated) {
                if (array[total-1] != 0)
                    throw new ReaderError("Extracted string not null terminated");
                size--;
            }
            StringBuilder buf = new StringBuilder(size);
            for (int i=offset; i<offset+size; ++i) {
                buf.append((char) (array[i] & 0xff));
            }
            return buf.toString();
        }
        catch (IndexOutOfBoundsException e) {
            // length has been checked, should never get here
            assert(false);
            throw new ReaderError("Premature end of data", e);
        }
    }

    protected static long extractLongFromArray(byte[] array,
            int offset, int byteCount, boolean bigEndian) throws ReaderError {
        assert(byteCount <= 8);
        try {
            long result = 0;
            if (bigEndian) {
                for (int i = offset; i < offset+byteCount; i++) {
                    int byteValue = array[i] & 0xff;
                    //System.out.println("   byte val " + i + " : " + byteValue);
                    result = (result << 8) | byteValue;
                }
            }
            else {
                for (int i = offset+byteCount-1; i >= offset; i--) {
                    int byteValue = array[i] & 0xff;
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
}
