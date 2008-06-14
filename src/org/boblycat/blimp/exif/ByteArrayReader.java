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
 * Reads binary Exif or TIFF data from a byte array.
 *
 * @author Knut Arild Erstad
 */
public class ByteArrayReader extends BinaryReader {
    private byte[] data;

    public ByteArrayReader(byte[] data) throws ReaderError {
        this.data = data;
        detectBaseOffset();
        detectEndianness();
    }

    @Override
    public String extractAscii(int offset, int size, boolean nullTerminated)
            throws ReaderError {
        try {
            int total = baseOffset + offset + size;
            if (data.length < total)
                throw new ReaderError("Premature end of data while extracting string");
            if (nullTerminated) {
                if (data[total-1] != 0)
                    throw new ReaderError("Extracted string not null terminated");
                size--;
            }
            return new String(data, baseOffset + offset, size, "US-ASCII");
        }
        catch (UnsupportedEncodingException e) {
            throw new ReaderError("Ascii encoding not supported?", e);
        }
        catch (IndexOutOfBoundsException e) {
            // length has been checked, should never get here
            assert(false);
            throw new ReaderError("Premature end of data", e);
        }
    }

    @Override
    public long extractLong(int offset, int byteCount) throws ReaderError {
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

}
