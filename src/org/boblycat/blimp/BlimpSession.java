package org.boblycat.blimp;

import java.beans.PropertyDescriptor;
import java.util.Vector;

import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.InputLayer;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.layers.ResizeLayer;
import org.boblycat.blimp.layers.ViewResizeLayer;

class ViewportInfo {
    int viewWidth;
    int viewHeight;
    int imageWidth;
    int imageHeight;
    ZoomFactor zoomFactor;
    
    public ViewportInfo() {
        zoomFactor = new ZoomFactor();
    }
    
    boolean isActive() {
        return (viewWidth > 0 && viewHeight > 0);
    }

    ZoomFactor getAutoZoomFactor(int imageWidth, int imageHeight) {
        ZoomFactor autoZoomFactor = new ZoomFactor();
        while (autoZoomFactor.scale(imageWidth) > viewWidth
                || autoZoomFactor.scale(imageHeight) > viewHeight)
            autoZoomFactor.zoomOut();
        return autoZoomFactor;
    }

    static ViewResizeLayer resizeLayerFromZoom(ZoomFactor zoom, int w, int h) {
        return new ViewResizeLayer(zoom.scale(w), zoom.scale(h), true);
    }
    
    void setImageSize(int w, int h) {
        if (w != imageWidth || h != imageHeight)
            // invalidate current zoom when size changes
            zoomFactor = getAutoZoomFactor(w, h);
        imageWidth = w;
        imageHeight = h;
    }
    
    ViewResizeLayer getResizeLayer(int imageWidth, int imageHeight) {
        if (!isActive())
            return null;
        setImageSize(imageWidth, imageHeight);
        return resizeLayerFromZoom(zoomFactor, imageWidth, imageHeight);
    }

    void zoomIn() {
        zoomFactor.zoomIn();
    }

    void zoomOut() {
        zoomFactor.zoomOut();
    }

    void copyDataFrom(ViewportInfo other) {
        viewWidth = other.viewWidth;
        viewHeight = other.viewHeight;
        zoomFactor.multiplier = other.zoomFactor.multiplier;
        zoomFactor.divisor = other.zoomFactor.divisor;
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
    
    protected Bitmap applyLayer(Bitmap source, AdjustmentLayer layer) {
        return layer.applyLayer(source);
    }
    
    protected Bitmap inputBitmap(InputLayer input) {
        return input.getBitmap();
    }

    Bitmap applyViewport(Bitmap bm) {
        if (bm == null)
            return null;
        ViewResizeLayer resize = viewport.getResizeLayer(bm.getWidth(), bm
                .getHeight());
        if (resize != null)
            return applyLayer(bm, resize);
        return bm;
    }

    public void applyLayers() {
        currentBitmap = generateBitmap(true);
    }

    ResizeLayer getResizeLayer() {
        for (Layer layer : layerList)
            if (layer instanceof ResizeLayer && layer.isActive())
                return (ResizeLayer) layer;
        return null;
    }

    Bitmap generateBitmap(boolean useViewport) {
        InputLayer input = getInput();
        if (input == null) {
            System.err.println("No input!");
            return null;
        }
        if (!input.isActive()) {
            // non-active input layer: abort
            return null;
        }
        Bitmap bm = null;
        bm = inputBitmap(input);
        if (bm == null) {
            System.err.println("Input failed!");
            return null;
        }
        // TODO: let a view quality setting decide when/how to resize
        ResizeLayer resize = getResizeLayer();
        if (resize != null && resize.isActive())
            bm = applyLayer(bm, resize);
        if (useViewport)
            bm = applyViewport(bm);

        for (Layer layer : layerList) {
            if (layer == input || layer == resize)
                continue;
            if (layer instanceof InputLayer) {
                System.err.println("Warning: more than one input layer?");
            }
            else if (layer.isActive() && layer instanceof AdjustmentLayer) {
                if (bm == null) {
                    System.err.println("Warning: no input to apply "
                            + layer.getDescription());
                    continue;
                }
                AdjustmentLayer adjust = (AdjustmentLayer) layer;
                bm = applyLayer(bm, adjust);
            }
        }
        return bm;
    }
    
    /**
     * Copy session data from the other session.  The implementation will attempt
     * to reuse existing layer object, if possible.
     * 
     * @param other The session to copy layers from. 
     */
    public void synchronizeSessionData(BlimpSession other) {
        Vector<Layer> newList = new Vector<Layer>();
        for (Layer otherLayer: other.layerList)
            newList.add(findOrCloneLayer(otherLayer));
        layerList = newList;
        //viewport.copyDataFrom(other.viewport);
    }
    
    private Layer findOrCloneLayer(Layer otherLayer) {
        Class layerClass = otherLayer.getClass();
        Layer foundLayer = null;
        for (Layer layer: layerList) {
            if (layer.getClass() == layerClass) {
                foundLayer = layer;
                break;
            }
        }
        if (foundLayer == null)
            return (Layer) otherLayer.clone();
        layerList.remove(foundLayer);
        Serializer.copyBeanData(otherLayer, foundLayer);
        return foundLayer;
    }

    public void setInput(InputLayer newInput) {
        assert (newInput != null);
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
    
    private boolean validIndex(int index) {
        return (index >= 0) && (index < layerList.size());
    }
    
    public void moveLayer(int oldIndex, int newIndex) {
        if (!validIndex(oldIndex) || !validIndex(newIndex)
                || (oldIndex == newIndex)) {
            Util.err("invalid indexes in moveLayer("
                    + oldIndex + "," + newIndex + ")");
            return;
        }
        Layer layer = layerList.remove(oldIndex);
        layerList.add(newIndex, layer);
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
