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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

/**
 * A class for extracting Exif data from binary data.
 *
 * @author Knut Arild Erstad
 */
public class ExifBlobReader {
    int currentOffset;
    int exifPointer;
    private BinaryReader reader;

    public ExifBlobReader(byte[] data) throws ReaderError {
        reader = new ByteArrayReader(data);
        currentOffset = 0;
    }

    public ExifBlobReader(File fileName) throws FileNotFoundException, ReaderError {
        reader = new FileBinaryReader(fileName);
        currentOffset = 0;
    }

    protected ImageFileDirectory extractIFD() throws ReaderError {
        ImageFileDirectory ifd = new ImageFileDirectory();
        int fieldCount = reader.extractInt(currentOffset, 2);
        currentOffset += 2;
        for (int n=0; n<fieldCount; n++) {
            // read a single field
            int tag = reader.extractInt(currentOffset, 2);
            int typeTag = reader.extractInt(currentOffset + 2, 2);
            int dataCount = reader.extractInt(currentOffset+4, 4);
            // try to interpret type
            ExifDataType type = ExifDataType.fromTypeTag(typeTag);
            if (type == null) {
                // skip unrecognized data types
                currentOffset += 12;
                continue;
            }
            // If the value fits in 4 bytes, it is stored "inline", otherwise
            // an offset is stored.
            int valueOffset;
            if (type.getByteCount() * dataCount <= 4)
                valueOffset = currentOffset + 8;
            else
                valueOffset = reader.extractInt(currentOffset + 8, 4);
            ExifField field = new ExifField(tag, type);
            switch (type) {
            case ASCII:
                field.setStringValue(reader.extractAscii(valueOffset, dataCount, true));
                break;
            case UNDEFINED:
            case BYTE:
            case SBYTE:
                field.setStringValue(reader.extractAscii(valueOffset, dataCount, false));
                break;
            case SHORT:
            case SSHORT:
            case LONG:
            case SLONG:
                for (int i=0; i<dataCount; i++) {
                    field.addValue(new Integer(reader.extractInt(valueOffset, type.getByteCount())));
                    valueOffset += type.getByteCount();
                }
                break;
            case RATIONAL:
            case SRATIONAL:
                for (int i=0; i<dataCount; i++) {
                    int numer = reader.extractInt(valueOffset, 4);
                    int denom = reader.extractInt(valueOffset+4, 4);
                    field.addValue(new Rational(numer, denom));
                    valueOffset += 8;
                }
                break;
            }
            ifd.addField(field);
            if (field.getTag() == ExifTag.Exif_IFD_Pointer.getTag())
                exifPointer = (Integer) field.valueAt(0);
            currentOffset += 12;
        }

        ExifField subIFDOffsets = ifd.get(ExifTag.SubIFDs);
        if (subIFDOffsets != null) {
            // extract subIFDs before returning
            int savedOffset = currentOffset;
            for (int i=0; i<subIFDOffsets.getCount(); ++i) {
                int offset = (Integer) subIFDOffsets.valueAt(i);
                currentOffset = offset;
                ifd.addSubIFD(extractIFD());
            }
            // restore offset
            currentOffset = savedOffset;
        }

        return ifd;
    }

    public Vector<ImageFileDirectory> extractIFDs() throws ReaderError {
        Vector<ImageFileDirectory> directories = new Vector<ImageFileDirectory>();
        currentOffset = reader.extractInt(4, 4);
        while (currentOffset != 0) {
            //System.out.println("current offset: " + currentOffset);
            directories.add(extractIFD());
            currentOffset = reader.extractInt(currentOffset, 4);
            if (currentOffset == 0 && exifPointer != 0) {
                //System.out.println("jumping to exif pointer: " + exifPointer);
                currentOffset = exifPointer;
                exifPointer = 0;
            }
        }
        return directories;
    }
}
