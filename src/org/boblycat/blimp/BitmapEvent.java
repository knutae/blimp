package org.boblycat.blimp;

import java.util.EventObject;

public class BitmapEvent extends EventObject {
    private static final long serialVersionUID = 1L;

    transient Bitmap bitmap;
    
    public BitmapEvent(Object source, Bitmap bitmap) {
        super(source);
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
