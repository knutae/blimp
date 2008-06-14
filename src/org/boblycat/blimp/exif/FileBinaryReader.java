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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads binary Exif or TIFF data from a file.
 *
 * @author Knut Arild Erstad
 */
public class FileBinaryReader extends BinaryReader {
    byte[] data;
    int readSize;
    InputStream stream;

    public FileBinaryReader(File fileName) throws FileNotFoundException, ReaderError {
        //stream = new FileInputStream(fileName);
        stream = new BufferedInputStream(new FileInputStream(fileName));
        data = new byte[1024];
        readSize = 0;
        detectBaseOffset();
        detectEndianness();
    }

    private void growData(int requiredSize) throws ReaderError {
        if (readSize >= requiredSize)
            return;
        // grow buffer if needed
        int dataLength = data.length;
        while (dataLength < requiredSize) {
            dataLength *= 2;
        }
        if (dataLength > data.length) {
            byte[] newData = new byte[dataLength];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
        assert(data.length == dataLength);
        // read from stream
        try {
            int lengthToRead = requiredSize - readSize;
            int bytes = stream.read(data, readSize, lengthToRead);
            readSize += bytes;
        }
        catch (IOException e) {
            throw new ReaderError("Input stream error (" + e.getMessage() + ")", e);
        }
        if (readSize < requiredSize)
            throw new ReaderError("Premature end of data");
    }

    @Override
    public String extractAscii(int offset, int size, boolean nullTerminated)
            throws ReaderError {
        growData(baseOffset + offset + size);
        return extractAsciiFromArray(data, baseOffset + offset, size, nullTerminated);
    }

    @Override
    public long extractLong(int offset, int byteCount) throws ReaderError {
        growData(baseOffset + offset + byteCount);
        return extractLongFromArray(data, baseOffset + offset, byteCount, bigEndian);
    }

}
