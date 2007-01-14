package org.boblycat.blimp;

import java.util.Vector;

public class BlimpSession extends InputLayer implements LayerChangeListener {
    Vector<Layer> layerList;
    Bitmap currentBitmap;
    ResizeLayer resizeLayer;
    
    public BlimpSession() {
    	layerList = new Vector<Layer>();
        currentBitmap = null;
    }

    /*
    public void openFile(String path) {
        setInput(Util.getInputLayerFromFile(path));
    }
    */
    
    public void applyLayers() {
    	currentBitmap = null;
    	for (Layer layer: layerList) {
            if (layer instanceof InputLayer) {
            	if (currentBitmap != null)
            		System.err.println("Warning: more than one input layer?");
        		if (!layer.isActive())
        			// non-active input layer: abort
        			break;
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
    	assert(newInput != null);
    	if (layerList.isEmpty()) {
    		layerList.add(newInput);
    	}
    	else if (layerList.firstElement() instanceof InputLayer) {
    		layerList.firstElement().removeChangeListener(this);
    		layerList.set(0, newInput);
    	}
    	else {
    		Util.warn("the first layer is not an input layer (setInput)");
    		layerList.add(0, newInput);
    	}
    	newInput.addChangeListener(this);
        invalidate();
    }
    
    public InputLayer getInput() {
    	if (layerList.isEmpty())
    		return null;
    	Layer first = layerList.firstElement();
    	if (first instanceof InputLayer)
    		return (InputLayer) first;
		Util.warn("the first layer is not an input layer (getInput)");
    	return null;
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
