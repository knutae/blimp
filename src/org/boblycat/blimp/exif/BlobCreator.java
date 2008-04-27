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
import java.util.Vector;

/**
 * A class for creating an in-memory blob (byte array) of Exif data.
 *
 * @author Knut Arild Erstad
 */
public class BlobCreator {
    private byte[] data;
    private int currentOffset;

    class DelayedValueInfo {
        int offset;
        ExifField field;
        DelayedValueInfo(int offset, ExifField field) {
            this.offset = offset;
            this.field = field;
        }
    }

    public BlobCreator() {
        data = new byte[1024];
        currentOffset = 0;
    }

    private void grow(int requiredOffset) {
        int length = data.length;
        if (requiredOffset < length)
            return;
        while (requiredOffset >= length) {
            length *= 2;
        }
        byte[] newData = new byte[length];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }

    private void putByte(int offset, byte value) {
        grow(offset);
        data[offset] = value;
    }

    private void writeByte(byte value) {
        putByte(currentOffset, value);
        currentOffset += 1;
    }

    private void putLong(int offset, long value, int byteCount) {
        assert(byteCount <= 8);
        long tmp = value;
        for (int i=0; i<byteCount; ++i) {
            putByte(offset + i, (byte) (tmp & 0xff));
            tmp >>= 8;
        }
    }

    private void putInt(int offset, int value, int byteCount) {
        assert(byteCount <= 4);
        putLong(offset, value, byteCount);
    }

    private void writeInt(int value, int byteCount) {
        putInt(currentOffset, value, byteCount);
        currentOffset += byteCount;
    }

    private int putAscii(int offset, String value) {
        try {
            byte[] bytes = value.getBytes("US-ASCII");
            for (int i=0; i<bytes.length; i++) {
                putByte(offset + i, bytes[i]);
            }
            putByte(offset + bytes.length, (byte) 0);
            return bytes.length + 1;
        }
        catch (UnsupportedEncodingException e) {
            // ugly... improve this
            e.printStackTrace();
        }
        return 0;
    }

    private void writeAscii(String value) {
        int length = putAscii(currentOffset, value);
        currentOffset += length;
    }

    private void writeValues(Vector<Object> values, ExifDataType type) {
        for (Object obj: values) {
            switch (type) {
            case BYTE:
                writeByte((Byte) obj);
                break;
            case SHORT:
            case LONG:
            case SLONG:
                writeInt((Integer) obj, type.getByteCount());
                break;
            case RATIONAL:
            case SRATIONAL:
                Rational r = (Rational) obj;
                writeInt(r.getNumerator(), 4);
                writeInt(r.getDenominator(), 4);
                break;
            case UNDEFINED:
                // anything to do here?
                break;
            }
        }
    }

    private void writeExifFieldValue(ExifField field) {
        switch (field.getType()) {
        case ASCII:
            // special case
            writeAscii(field.getStringValue());
            break;
        default:
            writeValues(field.getValues(), field.getType());
            break;
        }
    }

    private void writeDelayedValues(Vector<DelayedValueInfo> values) {
        for (DelayedValueInfo dv: values) {
            putInt(dv.offset, currentOffset, 4);
            writeExifFieldValue(dv.field);
        }
        values.clear();
    }

    private void writeEndianInfo() {
        assert(currentOffset == 0);
        writeByte((byte) 73);
        writeByte((byte) 73);
        writeInt(42, 2);
        assert(currentOffset == 4);
    }

    public void writeIFDs(Vector<ImageFileDirectory> ifds) {
        if (currentOffset == 0)
            writeEndianInfo();
        Vector<DelayedValueInfo> delayedValues = new Vector<DelayedValueInfo>();
        for (ImageFileDirectory ifd: ifds) {
            writeInt(currentOffset + 4, 4); // offset to next IFD
            writeInt(ifd.size(), 2);
            for (ExifField field: ifd) {
                writeInt(field.getTag(), 2);
                writeInt(field.getType().getTypeTag(), 2);
                writeInt(field.getCount(), 4);
                if (field.getByteCount() <= 4) {
                    // write value inline and increase offset by exactly 4
                    int tmpOffset = currentOffset;
                    writeExifFieldValue(field);
                    assert(currentOffset <= tmpOffset + 4);
                    currentOffset = tmpOffset + 4;
                }
                else {
                    // write the field value later
                    delayedValues.add(new DelayedValueInfo(currentOffset, field));
                    writeInt(0, 4); // write dummy value
                }
            }
            // zero offset (no more IFDs)
            writeInt(0, 4);
            // write all delayed values and update offsets
            writeDelayedValues(delayedValues);
        }
    }

    private void dump(byte[] arr) {
        for (byte b: arr) {
            //System.out.print(Integer.toHexString(b & 0xff) + " ");
            System.out.print(Integer.toString(b & 0xff) + " ");
        }
        System.out.println();
    }

    public byte[] getDataWithHeader() {
        byte[] header = "Exif\0\0".getBytes();
        dump(header);
        grow(currentOffset-1);
        assert(data.length >= currentOffset);
        byte[] result = new byte[header.length + currentOffset];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(data, 0, result, header.length, currentOffset);
        dump(result);
        return result;
    }
}
