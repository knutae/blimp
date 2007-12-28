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

import java.util.Iterator;
import java.util.Vector;

/**
 * An image file directory (IFD) with Exif data, as defined in TIFF.
 *
 * @author Knut Arild Erstad
 */
public class ImageFileDirectory implements Iterable<ExifField> {
    Vector<ExifField> fields;

    public ImageFileDirectory() {
        fields = new Vector<ExifField>();
    }

    public void addField(ExifField field) {
        fields.add(field);
    }

    public Iterator<ExifField> iterator() {
        return fields.iterator();
    }
}
