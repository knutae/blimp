package org.boblycat.blimp;

import java.util.Vector;

public class BlimpSession extends BitmapSource implements LayerChangeListener {
    BitmapSource source;
    Vector<Layer> layerList;
    Bitmap currentBitmap;
    String currentFilePath;
    ResizeLayer resizeLayer;
    
    public BlimpSession() {
        layerList = new Vector<Layer>();
        currentBitmap = null;
    }

    public void openFile(String path) {
        currentFilePath = path;
        source = new FileBitmapSource(path);
        invalidate();
    }
    
    public void applyLayers() {
        if (source == null)
            return;
        currentBitmap = source.getBitmap();
        if (currentBitmap == null)
            return;
        if (resizeLayer != null) {
            currentBitmap = resizeLayer.applyLayer(currentBitmap);
        }
        for (int i=0; i<layerList.size(); i++) {
            Layer layer = layerList.get(i);
            if (layer.isActive()) {
                currentBitmap = layer.applyLayer(currentBitmap);
                if (currentBitmap == null)
                    return;
            }
        }
    }
    
    public void setBitmapSource(BitmapSource newSource) {
        source = newSource;
        invalidate();
    }
    
    public Bitmap getBitmap() {
        if (resizeLayer != null)
            invalidate();
        if (currentBitmap == null)
            applyLayers();
        return currentBitmap;
    }
    
    public Bitmap getSizedBitmap(int width, int height) {
        resizeLayer = new ResizeLayer(width, height, true);
        applyLayers();
        return currentBitmap;
    }
    
    public int layerCount() {
        return layerList.size();
    }
    
    public void addLayer(int index, Layer newLayer) {
    	if (index < 0)
    		layerList.add(newLayer);
    	else
    		layerList.add(index, newLayer);
        newLayer.addChangeListener(this);
        if (newLayer.isActive())
            invalidate();
        notifyChangeListeners();
    }
    
    public void addLayer(Layer newLayer) {
    	addLayer(-1, newLayer);
    }
    
    public Layer getLayer(int index) {
        return layerList.get(index);
    }
    
    public void invalidate() {
        currentBitmap = null;
        resizeLayer = null;
    }
    
    public void activateLayer(int index, boolean active) {
        Layer layer = getLayer(index);
        if (layer.isActive() != active) {
            layer.setActive(active);
            invalidate();
            notifyChangeListeners();
        }
        layer.addChangeListener(this);
    }
    
    public void removeLayer(int index) {
        Layer layer = getLayer(index);
        layer.removeChangeListener(this);
        layerList.removeElementAt(index);
        if (layer.isActive())
            invalidate();
        notifyChangeListeners();
    }
    
    public String getDescription() {
        if (source != null)
            return source.getDescription();
        return "Empty session";
    }
    
    public void handleChange(LayerEvent event) {
        invalidate();
        notifyChangeListeners();
    }
}
