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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * An image file directory (IFD) with Exif data, as defined in TIFF.
 *
 * @author Knut Arild Erstad
 */
public class ImageFileDirectory implements Iterable<ExifField> {
    private List<ExifField> fields;
    private HashMap<Integer, Integer> indexMap;
    private List<ImageFileDirectory> subIFDs;

    public ImageFileDirectory() {
        fields = new ArrayList<ExifField>();
        indexMap = new HashMap<Integer, Integer>();
        subIFDs = new ArrayList<ImageFileDirectory>();
    }

    public void addField(ExifField field) {
        if (indexMap.containsKey(field.getTag())) {
            // replace existing field
            int index = indexMap.get(field.getTag());
            assert(index >= 0 && index < fields.size());
            fields.set(index, field);
        }
        else {
            // append new element
            indexMap.put(field.getTag(), fields.size());
            fields.add(field);
        }
    }

    public Iterator<ExifField> iterator() {
        return fields.iterator();
    }

    public int size() {
        return fields.size();
    }

    public ExifField get(int tag) {
        if (indexMap.containsKey(tag)) {
            int index = indexMap.get(tag);
            assert(index >= 0 && index < fields.size());
            return fields.get(index);
        }
        return null;
    }

    public ExifField get(ExifTag tag) {
        return get(tag.getTag());
    }

    public Iterable<ImageFileDirectory> getSubIFDs() {
        return subIFDs;
    }

    public void addSubIFD(ImageFileDirectory ifd) {
        subIFDs.add(ifd);
    }
}
