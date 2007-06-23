package org.boblycat.blimp;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.DimensionAdjustmentLayer;
import org.boblycat.blimp.layers.InputLayer;
import org.boblycat.blimp.layers.Layer;
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
    
    boolean isZoomedIn() {
        return zoomFactor.getMultiplier() > zoomFactor.getDivisor();
    }
}

public class BlimpSession extends InputLayer implements LayerChangeListener {
    Vector<Layer> layerList;

    Bitmap currentBitmap;

    ViewportInfo viewport;
    
    class SessionProgressListener implements ProgressListener {
        BlimpSession session;
        Layer layer;
        
        SessionProgressListener(BlimpSession session, Layer layer) {
            this.session = session;
            this.layer = layer;
        }
        
        public void reportProgress(ProgressEvent e) {
            session.reportLayerProgress(layer, e.progress);
        }
    }

    public BlimpSession() {
        layerList = new Vector<Layer>();
        currentBitmap = null;
        viewport = new ViewportInfo();
    }
    
    private void reportLayerProgress(Layer layer, double progress) {
        if (layer instanceof ViewResizeLayer)
            return;
        triggerProgress(layer.getProgressDescription(), progress);
    }
    
    protected Bitmap applyLayer(Bitmap source, AdjustmentLayer layer) {
        reportLayerProgress(layer, 0.0);
        ProgressListener listener = new SessionProgressListener(this, layer);
        layer.addProgressListener(listener);
        Bitmap result = layer.applyLayer(source);
        if (result != null && result.getPixelScaleFactor() <= 0)
            result.setPixelScaleFactor(source.getPixelScaleFactor());
        layer.removeProgressListener(listener);
        reportLayerProgress(layer, 1.0);
        return result;
    }
    
    protected Bitmap inputBitmap(InputLayer input) throws IOException {
        reportLayerProgress(input, 0.0);
        ProgressListener listener = new SessionProgressListener(this, input);
        input.addProgressListener(listener);
        Bitmap result = input.getBitmap();
        if (result != null && result.getPixelScaleFactor() <= 0)
            result.setPixelScaleFactor(1);
        input.removeProgressListener(listener);
        reportLayerProgress(input, 1.0);
        return result;
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

    public void applyLayers() throws IOException {
        currentBitmap = generateBitmap(true);
    }

    /**
     * Get a subset of layers that can be moved first in the process in order to
     * optimize editing.
     */ 
    Vector<AdjustmentLayer> getMovableLayers() {
        Vector<AdjustmentLayer> layers = new Vector<AdjustmentLayer>();
        for (Layer layer: layerList) {
            if (layer instanceof DimensionAdjustmentLayer)
                layers.add((AdjustmentLayer) layer);
        }
        return layers;
    }
    
    private Bitmap getInputBitmap() throws IOException {
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
        return bm;
    }
    
    private Bitmap internalGenerateBitmapBeforeLayer(String layerName,
            boolean useViewport) throws IOException {
        Bitmap bm = getInputBitmap();
        if (bm == null)
            return null;
        InputLayer input = getInput();
        assert(input != null);
        
        if (layerName != null && layerName.equals(input.getName()))
            // impossible to get a bitmap before the input layer
            return null;

        // TODO: let a view quality setting decide when/how to resize
        Vector<AdjustmentLayer> movableLayers = getMovableLayers();
        if (layerName != null && findLayerInList(layerName, movableLayers) != null)
            // cannot use movable layers in this case
            movableLayers.clear();
        
        for (AdjustmentLayer layer: movableLayers)
            if (layer.isActive())
                bm = applyLayer(bm, layer);
        
        if (useViewport && !viewport.isZoomedIn())
            bm = applyViewport(bm);

        for (Layer layer : layerList) {
            if (layerName != null && layerName.equals(layer.getName()))
                return bm;
            if (layer == input || movableLayers.contains(layer))
                continue;
            if (layer instanceof InputLayer) {
                System.err.println("Warning: more than one input layer?");
            }
            else if (layer.isActive() && layer instanceof AdjustmentLayer) {
                if (bm == null) {
                    Util.err("Warning: no input to apply "
                            + layer.getDescription());
                    continue;
                }
                AdjustmentLayer adjust = (AdjustmentLayer) layer;
                bm = applyLayer(bm, adjust);
            }
        }
        
        if (layerName != null)
            // specified layer not found
            return null;
        
        if (useViewport && viewport.isZoomedIn())
            bm = applyViewport(bm);
        
        return bm;
    }

    Bitmap generateBitmap(boolean useViewport) throws IOException {
        return internalGenerateBitmapBeforeLayer(null, useViewport);
    }
    
    /**
     * Returns a histogram for the bitmap generated just before a specific
     * adjustment layer is applied.
     * 
     * @param layerName
     *      the name of a layer.
     * @param useViewport
     *      if <code>true</code>, resize the image to the viewport,
     *      which is faster, but will create a less accurate histogram.
     * @return
     *      a histogram, or <code>null</code> if the layer did not exist.
     * @throws IOException
     *      if an I/O error occured when processing the input layer.
     */
    public Histogram getHistogramBeforeLayer(String layerName, boolean useViewport)
    throws IOException {
        Bitmap bm = internalGenerateBitmapBeforeLayer(layerName, useViewport);
        if (bm == null)
            return null;
        // TODO: figure out a more logical way to reduce the color weight
        if (bm.getChannelBitDepth() != 8)
            bm = BitmapUtil.create8BitCopy(bm);
        return new Histogram(bm);
    }
    
    /**
     * Returns the size of the bitmap before the specified adjustment layer.
     * The viewport size will not be applied.
     * 
     * @param layerName
     *      a layer name.
     * @return
     *      a bitmap size, or <code>null</code> if the layer did not exist.
     * @throws IOException
     *      if an I/O error occued when processing the input layer.
     */
    public BitmapSize getBitmapSizeBeforeLayer(String layerName)
    throws IOException {
        InputLayer input = getInput();
        if (input == null || input.getName().equals(layerName))
            return null;
        Bitmap bm = inputBitmap(input);
        if (bm == null)
            return null;
        for (Layer layer: layerList) {
            if (layerName != null && layerName.equals(layer.getName()))
                return bm.getSize();
            if (layer instanceof DimensionAdjustmentLayer) {
                bm = applyLayer(bm, (AdjustmentLayer) layer);
                if (bm == null)
                    return null;
            }
        }
        if (layerName == null)
            return bm.getSize();
        else
            return null;
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
        String layerName = otherLayer.getName();
        Layer foundLayer = findLayer(layerName);
        if (foundLayer != null && foundLayer.getClass() != otherLayer.getClass())
            // different layer classes with same name
            foundLayer = null;
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

    public Bitmap getBitmap() throws IOException {
        if (currentBitmap == null)
            applyLayers();
        return currentBitmap;
    }

    public Bitmap getSizedBitmap(int width, int height) throws IOException {
        viewport.viewWidth = width;
        viewport.viewHeight = height;
        applyLayers();
        return currentBitmap;
    }

    public Bitmap getFullBitmap() throws IOException {
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
    
    private static Layer findLayerInList(String name,
            Vector<? extends Layer> layers) {
        for (Layer layer: layers) {
            if (layer.getName().equals(name))
                return layer;
        }
        return null;
    }
    
    public Layer findLayer(String name) {
        return findLayerInList(name, layerList);
    }
    
    private void uniqifyLayerName(Layer layer) {
        Layer found = findLayer(layer.getName());
        if (found == null || found == layer)
            return;
        int i = 0;
        String newName;
        do {
            i++;
            newName = generateName(layer, i);
        } while (findLayer(newName) != null);
        layer.setName(newName);
    }

    public void addLayer(int index, AdjustmentLayer newLayer) {
        uniqifyLayerName(newLayer);
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
        String name = getName();
        if (!name.isEmpty())
            return name;
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
    
    /**
     * Test if the session data (the layers) is the same as the other session.
     * Unlike equals(), this function can return true even if the session
     * classes are not the same.
     * @param other Another session object.
     * @return <code>true</code> if all layers are equal and in the same order,
     * <code>false</code> otherwise.
     */
    public boolean sessionDataEquals(BlimpSession other) {
        if (layerCount() != other.layerCount())
            return false;
        for (int i=0; i < layerCount(); i++)
            if (!getLayer(i).equals(other.getLayer(i)))
                return false;
        return true;
    }
    
    /**
     * Create a deep copy of the given session.  This differs from clone()
     * because it will create a direct instance of BlimpSession even if the
     * parameter is a subclass.
     * @param session A blimp session object.
     * @return A deep copy of the session.
     */
    public static BlimpSession createCopy(BlimpSession session) {
        BlimpSession newSession = new BlimpSession();
        Serializer.copyBeanData(session, newSession);
        return newSession;
    }
    
    /**
     * Set the session's name from the given filename.
     * Call this explicitly after loading a session from file. 
     * @param filename The file name of an image or saved session, for instance.
     */
    public void setNameFromFilename(String filename) {
        File file = new File(filename);
        String shortName = Util.changeFileExtension(file.getName(), "");
        if (!shortName.isEmpty())
            setName(shortName);
    }
}
