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
        return extractAsciiFromArray(data, baseOffset + offset, size, nullTerminated);
    }

    @Override
    public long extractLong(int offset, int byteCount) throws ReaderError {
        assert(byteCount <= 8);
        return extractLongFromArray(data, baseOffset + offset, byteCount, bigEndian);
    }

}
