package org.boblycat.blimp;

import net.sourceforge.jiu.gui.awt.ToolkitLoader;
import java.io.File;

public class FileBitmapSource extends BitmapSource {
    Bitmap bitmap;
    String filePath;
    
    public FileBitmapSource(String filePath) {
        bitmap = new Bitmap();
        setFilePath(filePath);
    }
    
    public void setFilePath(String path) {
        filePath = path;
        bitmap.setImage(ToolkitLoader.loadViaToolkitOrCodecs(path));
    }
    
    public Bitmap getBitmap() {
        return bitmap;
    }
    
    public String getDescription() {
        if (filePath == null || filePath == "")
            return "No file";
        File file = new File(filePath);
        return file.getName();
    }
}