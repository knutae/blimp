package org.boblycat.blimp;

import java.util.WeakHashMap;

import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.InputLayer;
import org.boblycat.blimp.layers.Layer;

class BitmapTable extends PriorityCacheMap<String, Bitmap> {
    BitmapTable(int capacity) {
        super(capacity);
    }
    
    void put(Layer layer, Bitmap bitmap) {
        put(Serializer.layerToXml(layer), bitmap);
    }
    
    Bitmap get(Layer layer) {
        return get(Serializer.layerToXml(layer));
    }
}

public class BitmapCache {
    // Note: in order to avoid a potentially exponential memory growth when editing,
    // the capacities are currently set to 1.  Maybe rethink the caching strategy
    // and have some sort of global priority system instead?
    static final int INPUT_CAPACITY = 1;
    static final int ADJUSTMENT_CAPACITIES = 1;
    BitmapTable inputTable;
    //PriorityCacheMap<Bitmap, BitmapTable> adjustmentTables;
    WeakHashMap<Bitmap, BitmapTable> adjustmentTables;
    
    public BitmapCache() {
        inputTable = new BitmapTable(INPUT_CAPACITY);
        //adjustmentTables = new PriorityCacheMap<Bitmap, BitmapTable>(POOL_SIZE);
        adjustmentTables = new WeakHashMap<Bitmap, BitmapTable>();
    }
    
    private BitmapTable getAdjustmentTable(Bitmap source) {
        if (adjustmentTables.containsKey(source))
            return adjustmentTables.get(source);
        BitmapTable newTable = new BitmapTable(ADJUSTMENT_CAPACITIES);
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
    
    void printSizes() {
        int size = inputTable.size();
        for (BitmapTable atable: adjustmentTables.values()) {
            size += atable.size();
        }
        System.out.println("Number of cached bitmaps: " + size);
    }
}
