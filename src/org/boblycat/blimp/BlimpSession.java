package org.boblycat.blimp;

import java.beans.PropertyDescriptor;
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
    
    Bitmap applyViewport(Bitmap bm) {
    	if (bm == null)
    		return null;
    	ResizeLayer resize = viewport.getResizeLayer(bm.getWidth(), bm.getHeight());
    	if (resize != null)
    		return resize.applyLayer(bm);
    	return bm;
    }
    
    public void applyLayers() {
    	currentBitmap = generateBitmap(true);
    }
    
    Bitmap generateBitmap(boolean useViewport) {
    	Bitmap bm = null;
    	for (Layer layer: layerList) {
            if (layer instanceof InputLayer) {
            	if (bm != null)
            		System.err.println("Warning: more than one input layer?");
        		if (!layer.isActive())
        			// non-active input layer: abort
        			break;
            	InputLayer input = (InputLayer) layer;
            	bm = input.getBitmap();
            	if (useViewport)
            		bm = applyViewport(bm);
            }
            else if (layer.isActive() && layer instanceof AdjustmentLayer) {
            	if (bm == null) {
            		System.err.println("Warning: no input to apply "
            				+ layer.getDescription());
            		continue;
            	}
            	AdjustmentLayer adjust = (AdjustmentLayer) layer;
                bm = adjust.applyLayer(bm);
            }
    	}
    	return bm;
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
    
    public Bitmap getFullBitmap() {
    	return generateBitmap(false);
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
    
    public void removeLayer(Layer layer) {
    	int index = layerList.indexOf(layer);
    	if (index >= 0)
    		removeLayer(index);
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
    
    public String elementName() {
    	return "session";
    }
    
    /**
     * Overrides the BlimpBean function used by serialization.
     */
    public Vector<? extends BlimpBean> getChildren() {
    	return layerList;
    }
    
    /**
     * Overrides the BlimpBean function used by serialization.
     */
    public void addChild(BlimpBean bean) throws NotImplementedException {
    	if (bean instanceof AdjustmentLayer)
    		addLayer((AdjustmentLayer) bean);
    	else if (bean instanceof InputLayer)
    		setInput((InputLayer) bean);
    	else
    		super.addChild(bean);
    }
    
    /**
     * Overridden so that the input property is not serialized directly. 
     */
    protected boolean isSerializableProperty(PropertyDescriptor pd) {
    	if (pd.getName().equals("input"))
    		return false;
    	return super.isSerializableProperty(pd);
    }
}
