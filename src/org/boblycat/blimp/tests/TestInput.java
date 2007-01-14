package org.boblycat.blimp.tests;

import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.PixelImage;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.layers.InputLayer;

public class TestInput extends InputLayer {
    String path;

    public void setPath(String newPath) {
        path = newPath;
    }

    public String getPath() {
        return path;
    }

    @Override
    public Bitmap getBitmap() {
        PixelImage image = new MemoryRGB24Image(100, 100);
        TestBitmap bitmap = new TestBitmap(image);
        bitmap.creator = "TestInput";
        bitmap.testValue = "";
        return bitmap;
    }

    @Override
    public String getDescription() {
        return "Test Input";
    }

}
