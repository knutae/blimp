package org.boblycat.blimp;

import net.sourceforge.jiu.gui.awt.ToolkitLoader;
import java.io.File;

public class FileInputLayer extends InputLayer {
    Bitmap bitmap;
    String filePath;
    
    public FileInputLayer(String filePath) {
        bitmap = new Bitmap();
        setFilePath(filePath);
    }
    
    public void setFilePath(String path) {
        filePath = path;
        bitmap.setImage(ToolkitLoader.loadViaToolkitOrCodecs(path));
    }
    
    public String getFilePath() {
    	return filePath;
    }
    
    public Bitmap getBitmap() {
        return bitmap;
    }
    
    public String getDescription() {
    	return Util.getFileNameFromPath(filePath);
    }
}