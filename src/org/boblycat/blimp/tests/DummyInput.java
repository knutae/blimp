package org.boblycat.blimp.tests;

import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.PixelImage;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.layers.InputLayer;

public class DummyInput extends InputLayer {
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
        return new Bitmap(image);
    }

    @Override
    public String getDescription() {
        return "Dummy Input";
    }

}
