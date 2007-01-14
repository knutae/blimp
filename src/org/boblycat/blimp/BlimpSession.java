package org.boblycat.blimp;

import java.util.Vector;

public class BlimpSession extends InputLayer implements LayerChangeListener {
    Vector<Layer> layerList;
    Bitmap currentBitmap;
    String currentFilePath;
    ResizeLayer resizeLayer;
    
    public BlimpSession() {
        layerList = new Vector<Layer>();
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
        	setInput(new RawFileInputLayer(path));
        else
        	setInput(new FileInputLayer(path));
        invalidate();
    }
    
    public void applyLayers() {
    	currentBitmap = null;
    	for (Layer layer: layerList) {
            if (layer instanceof InputLayer) {
            	if (currentBitmap != null)
            		System.err.println("Warning: more than one input layer?");
            	InputLayer input = (InputLayer) layer;
            	currentBitmap = input.getBitmap();
            	if (currentBitmap != null && resizeLayer != null) {
            		currentBitmap = resizeLayer.applyLayer(currentBitmap);
            	}
            }
            else if (layer.isActive() && layer instanceof AdjustmentLayer) {
            	if (currentBitmap == null) {
            		System.err.println("Warning: no input to apply "
            				+ layer.getDescription());
            		continue;
            	}
            	AdjustmentLayer adjust = (AdjustmentLayer) layer;
                currentBitmap = adjust.applyLayer(currentBitmap);
            }
        }
    }
    
    public void setInput(InputLayer newInput) {
    	if (layerList.size() > 0) {
    		assert(layerList.get(0) instanceof InputLayer);
    		layerList.set(0, newInput);
    	}
    	else {
    		layerList.add(newInput);
    	}
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
    	if (layerList.isEmpty())
    		return "Empty session";
    	return layerList.get(0).getDescription();
    }
    
    public void handleChange(LayerEvent event) {
        invalidate();
        triggerChangeEvent();
    }
}
