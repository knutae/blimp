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

import java.util.HashMap;

/**
 * A table of Exif data, and functions for converting to and from a set of IFDs.
 *
 * @author Knut Arild Erstad
 */
public class ExifTable {
    private HashMap<Integer, ExifField> map;

    public ExifTable() {
        map = new HashMap<Integer, ExifField>();
    }

    /**
     * Put an Exif field which will be placed into either the primary (0th) IFD
     * or the Exif IFD.  This function does not support thumbnail fields.
     * @param field
     */
    public void put(ExifField field) {
        map.put(field.getTag(), field);
    }

    public ExifField get(int tag) {
        return map.get(tag);
    }

    private void addCategoryFields(ImageFileDirectory ifd, ExifTag.Category cat) {
        for (ExifField field: map.values()) {
            ExifTag tag = ExifTag.fromTag(field.getTag());
            if (tag != null && tag.getCategory() == cat) {
                ifd.addField(field);
            }
        }
    }

    public ImageFileDirectory getPrimaryIFD() {
        ImageFileDirectory ifd = new ImageFileDirectory();
        addCategoryFields(ifd, ExifTag.Category.TIFF);
        ExifField exifPtr = new ExifField(
                ExifTag.Exif_IFD_Pointer.getTag(), ExifDataType.LONG);
        exifPtr.addValue(0);
        ifd.addField(exifPtr);
        return ifd;
    }

    public ImageFileDirectory getExifIFD() {
        ImageFileDirectory ifd = new ImageFileDirectory();
        ExifField version = new ExifField(
                ExifTag.ExifVersion.getTag(), ExifDataType.UNDEFINED);
        version.setStringValue("0220");
        ifd.addField(version);
        addCategoryFields(ifd, ExifTag.Category.Exif);
        return ifd;
    }
}
