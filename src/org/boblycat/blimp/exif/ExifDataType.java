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

/**
 * An enumeration of the data types used in Exif.
 * This is a subset of the types supported by TIFF.
 *
 * @author Knut Arild Erstad
 */
public enum ExifDataType {
    BYTE      (1, 1),
    ASCII     (2, 1),
    SHORT     (3, 2),
    LONG      (4, 4),
    RATIONAL  (5, 8),
    UNDEFINED (7, 1),
    SLONG     (9, 4),
    SRATIONAL (10, 8);

    private int type;
    private int byteCount;

    private ExifDataType(int type, int byteCount) {
        this.type = type;
        this.byteCount = byteCount;
    }

    /**
     * An integer identifying this type in Exif.
     * @return The Exif type tag.
     */
    public int getTypeTag() {
        return type;
    }

    /**
     * The number of bytes used to represent a single element of this data type.
     * @return A byte count, one of 1, 2, 4 or 8.
     */
    public int getByteCount() {
        return byteCount;
    }

    /**
     * Get a type object from the given type tag.
     * @param type An Exif type tag.
     * @return A type object, or <code>null</code> if there is no corresponding type.
     */
    public static ExifDataType fromTypeTag(int type) {
        switch (type) {
        case 1:
            return BYTE;
        case 2:
            return ASCII;
        case 3:
            return SHORT;
        case 4:
            return LONG;
        case 5:
            return RATIONAL;
        case 7:
            return UNDEFINED;
        case 9:
            return SLONG;
        case 10:
            return SRATIONAL;
        }
        return null;
    }
}
