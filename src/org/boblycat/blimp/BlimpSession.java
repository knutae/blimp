package org.boblycat.blimp;

import java.util.Vector;

public class BlimpSession extends InputLayer implements LayerChangeListener {
    InputLayer input;
    Vector<AdjustmentLayer> layerList;
    Bitmap currentBitmap;
    String currentFilePath;
    ResizeLayer resizeLayer;
    
    public BlimpSession() {
        layerList = new Vector<AdjustmentLayer>();
        currentBitmap = null;
    }
    
    private static boolean isRawFile(String path) {
    	int dotpos = path.lastIndexOf('.');
    	if (dotpos < 0)
    		return false;
    	String ext = path.substring(dotpos + 1).toLowerCase();
    	return ext.equals("raw") || ext.equals("crw") || ext.equals("cr2")
    		|| ext.equals("dng");
    	// todo: add more raw extensions
    }

    public void openFile(String path) {
        currentFilePath = path;
        if (isRawFile(path))
        	input = new RawFileInputLayer(path);
        else
        	input = new FileInputLayer(path);
        invalidate();
    }
    
    public void applyLayers() {
        if (input == null)
            return;
        currentBitmap = input.getBitmap();
        if (currentBitmap == null)
            return;
        if (resizeLayer != null) {
            currentBitmap = resizeLayer.applyLayer(currentBitmap);
        }
        for (int i=0; i<layerList.size(); i++) {
            AdjustmentLayer layer = layerList.get(i);
            if (layer.isActive() && layer instanceof AdjustmentLayer) {
                currentBitmap = layer.applyLayer(currentBitmap);
                if (currentBitmap == null)
                    return;
            }
        }
    }
    
    public void setInput(InputLayer newInput) {
        input = newInput;
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
    
    public void addLayer(int index, AdjustmentLayer newLayer) {
    	if (index < 0)
    		layerList.add(newLayer);
    	else
    		layerList.add(index, newLayer);
        newLayer.addChangeListener(this);
        if (newLayer.isActive())
            invalidate();
        triggerChangeEvent();
    }
    
    public void addLayer(AdjustmentLayer newLayer) {
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
            triggerChangeEvent();
        }
        layer.addChangeListener(this);
    }
    
    public void removeLayer(int index) {
        Layer layer = getLayer(index);
        layer.removeChangeListener(this);
        layerList.removeElementAt(index);
        if (layer.isActive())
            invalidate();
        triggerChangeEvent();
    }
    
    public String getDescription() {
        if (input != null)
            return input.getDescription();
        return "Empty session";
    }
    
    public void handleChange(LayerEvent event) {
        invalidate();
        triggerChangeEvent();
    }
}
