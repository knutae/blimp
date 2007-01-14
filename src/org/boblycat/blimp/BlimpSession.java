package org.boblycat.blimp;

import java.util.Vector;

class ViewportInfo {
	int viewWidth;
	int viewHeight;
	ZoomFactor zoomFactor;
	
	boolean isActive() {
		return (viewWidth > 0 && viewHeight > 0);
	}
	
	ZoomFactor getAutoZoomFactor(int imageWidth, int imageHeight) {
		ZoomFactor autoZoomFactor = new ZoomFactor();
		while (autoZoomFactor.scale(imageWidth) > viewWidth ||
				autoZoomFactor.scale(imageHeight) > viewWidth)
			autoZoomFactor.zoomOut();
		return autoZoomFactor;
	}
	
	static ResizeLayer resizeLayerFromZoom(ZoomFactor zoom, int w, int h) {
		return new ResizeLayer(zoom.scale(w), zoom.scale(h), true);
	}
	
	ResizeLayer getResizeLayer(int imageWidth, int imageHeight) {
		if (!isActive())
			return null;
		if (zoomFactor == null)
			zoomFactor = getAutoZoomFactor(imageWidth, imageHeight);
		return resizeLayerFromZoom(zoomFactor, imageWidth, imageHeight);
	}
	
	void zoomIn() {
		if (zoomFactor != null)
			zoomFactor.zoomIn();
	}
	
	void zoomOut() {
		if (zoomFactor != null)
			zoomFactor.zoomOut();
	}
}

public class BlimpSession extends InputLayer implements LayerChangeListener {
    Vector<Layer> layerList;
    Bitmap currentBitmap;
    ViewportInfo viewport;
    
    public BlimpSession() {
    	layerList = new Vector<Layer>();
        currentBitmap = null;
        viewport = new ViewportInfo();
    }

    /*
    public void openFile(String path) {
        setInput(Util.getInputLayerFromFile(path));
    }
    */
    
    void applyViewport() {
    	if (currentBitmap == null)
    		return;
    	ResizeLayer resize = viewport.getResizeLayer(currentBitmap.getWidth(),
    			currentBitmap.getWidth());
    	if (resize != null)
    		currentBitmap = resize.applyLayer(currentBitmap);
    }
    
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
            	applyViewport();
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
        if (currentBitmap == null)
            applyLayers();
        return currentBitmap;
    }
    
    public Bitmap getSizedBitmap(int width, int height) {
    	viewport.viewWidth = width;
    	viewport.viewHeight = height;
        applyLayers();
        return currentBitmap;
    }
    
    public void zoomIn() {
    	viewport.zoomIn();
    	invalidate();
    	triggerChangeEvent();
    }
    
    public void zoomOut() {
    	viewport.zoomOut();
    	invalidate();
    	triggerChangeEvent();
    }
    
    public double getCurrentZoom() {
    	if (viewport.zoomFactor == null)
    		return 1.0;
    	else
    		return viewport.zoomFactor.toDouble();
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
