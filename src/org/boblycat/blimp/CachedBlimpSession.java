package org.boblycat.blimp;

import java.io.IOException;

import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.InputLayer;

public class CachedBlimpSession extends BlimpSession {
    BitmapCache cache;
    
    public CachedBlimpSession() {
        cache = new BitmapCache();
    }
    
    private void log(String msg) {
        if (Debug.debugEnabled(this)) {
            Debug.print(this, msg);
            cache.printSizes();
        }
    }
    
    protected Bitmap applyLayer(Bitmap source, AdjustmentLayer layer) {
        Bitmap bitmap = cache.get(source, layer);
        if (bitmap == null) {
            log("miss: " + layer.getClass());
            bitmap = super.applyLayer(source, layer);
            cache.put(source, layer, bitmap);
        }
        else {
            log("hit: " + layer.getClass());
        }
        return bitmap;
    }
    
    protected Bitmap inputBitmap(InputLayer input) throws IOException {
        Bitmap bitmap = cache.get(input);
        if (bitmap == null) {
            log("miss: " + input.getClass());
            bitmap = super.inputBitmap(input);
            cache.put(input, bitmap);
        }
        else {
            log("hit: " + input.getClass());            
        }
        return bitmap;
    }
}
