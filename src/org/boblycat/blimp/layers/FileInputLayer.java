package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;

import net.sourceforge.jiu.gui.awt.ToolkitLoader;

public class FileInputLayer extends InputLayer {
    Bitmap bitmap;
    String filePath;

    public FileInputLayer() {
        bitmap = null;
        filePath = null;
    }

    public FileInputLayer(String filePath) {
        bitmap = null;
        setFilePath(filePath);
    }

    public void setFilePath(String path) {
        if (filePath != null && filePath.equals(path))
            return;
        filePath = path;
        bitmap = null;
    }

    public String getFilePath() {
        return filePath;
    }

    public Bitmap getBitmap() {
        if (bitmap == null && filePath != null) {
            System.out.println("Loading bitmap from file...");
            bitmap = new Bitmap();
            bitmap.setImage(ToolkitLoader.loadViaToolkitOrCodecs(filePath));
        }
        return bitmap;
    }

    public String getDescription() {
        return Util.getFileNameFromPath(filePath);
    }
}