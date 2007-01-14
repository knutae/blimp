package org.boblycat.blimp.tests;

import net.sourceforge.jiu.data.PixelImage;

import org.boblycat.blimp.Bitmap;

public class TestBitmap extends Bitmap {
    String creator;
    String testValue;
    
    TestBitmap(PixelImage image) {
        super(image);
    }
}
