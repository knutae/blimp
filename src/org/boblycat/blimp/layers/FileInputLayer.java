package org.boblycat.blimp.layers;

import java.io.IOException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.gui.awt.ToolkitLoader;

public class FileInputLayer extends InputLayer {
    String filePath;

    public FileInputLayer() {
        filePath = null;
    }

    public FileInputLayer(String filePath) {
        setFilePath(filePath);
    }

    public void setFilePath(String path) {
        filePath = path;
    }

    public String getFilePath() {
        return filePath;
    }

    public Bitmap getBitmap() throws IOException {
        PixelImage image = ToolkitLoader.loadViaToolkitOrCodecs(filePath);
        if (image == null)
            throw new IOException("Failed to load image from " + filePath);
        return new Bitmap(image);
    }

    public String getDescription() {
        return Util.getFileNameFromPath(filePath);
    }
}