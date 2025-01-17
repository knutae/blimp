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
package org.boblycat.blimp.session;

import java.util.WeakHashMap;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.io.Serializer;
import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.InputLayer;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.util.SoftHashMap;

class BitmapTable extends SoftHashMap<String, Bitmap> {
    void put(Layer layer, Bitmap bitmap) {
        put(Serializer.layerToXml(layer), bitmap);
    }

    Bitmap get(Layer layer) {
        return get(Serializer.layerToXml(layer));
    }
}

public class BitmapCache {
    BitmapTable inputTable;
    WeakHashMap<Bitmap, BitmapTable> adjustmentTables;

    public BitmapCache() {
        inputTable = new BitmapTable();
        adjustmentTables = new WeakHashMap<Bitmap, BitmapTable>();
    }

    private BitmapTable getAdjustmentTable(Bitmap source) {
        if (adjustmentTables.containsKey(source))
            return adjustmentTables.get(source);
        BitmapTable newTable = new BitmapTable();
        adjustmentTables.put(source, newTable);
        return newTable;
    }

    public void put(Bitmap source, AdjustmentLayer layer, Bitmap result) {
        BitmapTable table = getAdjustmentTable(source);
        table.put(layer, result);
    }

    public void put(InputLayer layer, Bitmap result) {
        inputTable.put(layer, result);
    }

    public Bitmap get(Bitmap source, AdjustmentLayer layer) {
        BitmapTable table = adjustmentTables.get(source);
        if (table == null)
            return null;
        return table.get(layer);
    }

    public Bitmap get(InputLayer layer) {
        return inputTable.get(layer);
    }
}
