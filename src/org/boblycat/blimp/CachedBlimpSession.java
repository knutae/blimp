package org.boblycat.blimp;

import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.InputLayer;

public class CachedBlimpSession extends BlimpSession {
    private static final boolean debug = false; 
    BitmapCache cache;
    
    public CachedBlimpSession() {
        cache = new BitmapCache();
    }
    
    private void log(String msg) {
        if (debug) {
            System.out.println("cache " + msg);
            cache.printSizes();
        }
    }
    
    protected Bitmap applyLayer(Bitmap source, AdjustmentLayer layer) {
        Bitmap bitmap = cache.get(source, layer);
        if (bitmap == null) {
            log("miss: " + layer.getClass());
            bitmap = layer.applyLayer(source);
            cache.put(source, layer, bitmap);
        }
        else {
            log("hit: " + layer.getClass());
        }
        return bitmap;
    }
    
    protected Bitmap inputBitmap(InputLayer input) {
        Bitmap bitmap = cache.get(input);
        if (bitmap == null) {
            log("miss: " + input.getClass());
            bitmap = input.getBitmap();
            cache.put(input, bitmap);
        }
        else {
            log("hit: " + input.getClass());            
        }
        return bitmap;
    }
}
