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
package org.boblycat.blimp.exif;

import java.util.ArrayList;
import java.util.List;

/**
 * A table of Exif data, and functions for converting to and from a set of IFDs.
 *
 * @author Knut Arild Erstad
 */
public class ExifTable {
    private List<ImageFileDirectory> mainIFDs;
    ImageFileDirectory primaryIFD;
    ImageFileDirectory exifIFD;

    public ExifTable() {
        primaryIFD = new ImageFileDirectory();

        exifIFD = new ImageFileDirectory();
        ExifField version = new ExifField(ExifTag.ExifVersion);
        version.setStringValue("0220");
        exifIFD.addField(version);

        mainIFDs = new ArrayList<ImageFileDirectory>();
        mainIFDs.add(primaryIFD);
    }

    /**
     * Put an Exif field which will be placed into either the primary (0th) IFD
     * or the Exif IFD.  This function does not support thumbnail fields.
     * @param field
     */
    public void put(ExifField field) {
        ImageFileDirectory ifd = primaryIFD;
        ExifTag tag = ExifTag.fromTag(field.getTag());
        if (tag != null && tag.getCategory() == ExifTag.Category.Exif)
            ifd = exifIFD;
        ifd.addField(field);
    }

    public ExifField get(int tag) {
        for (ImageFileDirectory ifd: mainIFDs) {
            ExifField field = ifd.get(tag);
            if (field != null)
                return field;
        }
        return exifIFD.get(tag);
    }

    public ExifField get(ExifTag tag) {
        return get(tag.getTag());
    }

    private void preparePrimaryIFD() {
        // add default fields to primary IFD
        if (primaryIFD.get(ExifTag.Exif_IFD_Pointer) == null) {
            ExifField pointer = new ExifField(ExifTag.Exif_IFD_Pointer);
            pointer.addValue(0);
            primaryIFD.addField(pointer);
        }
    }

    public ImageFileDirectory getPrimaryIFD() {
        preparePrimaryIFD();
        return primaryIFD;
    }

    public ImageFileDirectory getExifIFD() {
        return exifIFD;
    }

    public List<ImageFileDirectory> getMainIFDs() {
        return mainIFDs;
    }

    public void setIFDs(List<ImageFileDirectory> mainIFDs,
            ImageFileDirectory exifIFD) {
        if (mainIFDs.size() == 0)
            throw new IllegalArgumentException("At least one IFD is required");
        this.mainIFDs = mainIFDs;
        primaryIFD = mainIFDs.get(0);
        this.exifIFD = exifIFD;
    }
}
