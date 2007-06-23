package org.boblycat.blimp.tests;

import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.PixelImage;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.layers.InputLayer;

public class TestInput extends InputLayer {
    String path;
    int width;
    int height;
    
    public TestInput() {
        width = 100;
        height = 100;
    }

    public void setPath(String newPath) {
        path = newPath;
    }

    public String getPath() {
        return path;
    }
    
    public void setInputSize(int w, int h) {
        width = w;
        height = h;
    }

    @Override
    public Bitmap getBitmap() {
        PixelImage image = new MemoryRGB24Image(width, height);
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
