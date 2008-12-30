/*
 * Copyright (C) 2007 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.boblycat.blimp;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.boblycat.blimp.exif.ExifTable;
import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.DimensionAdjustmentLayer;
import org.boblycat.blimp.layers.InputLayer;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.layers.ViewResizeLayer;

public class BlimpSession extends InputLayer implements LayerChangeListener {
    public static enum PreviewQuality {
        Fast,
        Accurate
    }

    List<Layer> layerList;

    Bitmap currentBitmap;

    ViewResizeLayer viewLayer = new ViewResizeLayer();

    PreviewQuality previewQuality;

    private String projectFilePath;

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
        layerList = new ArrayList<Layer>();
        currentBitmap = null;
        viewLayer = new ViewResizeLayer();
        previewQuality = PreviewQuality.Accurate;
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
        if (result != null) {
            if (result.getPixelScaleFactor() <= 0)
                result.setPixelScaleFactor(source.getPixelScaleFactor());
            if (result.getExifTable() == null)
                result.setExifTable(source.getExifTable());
        }
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

    protected BitmapSize inputSize(InputLayer input) throws IOException {
        return input.getBitmapSize();
    }

    public void applyLayers() throws IOException {
        currentBitmap = generateBitmap(true);
    }

    private Bitmap getInputBitmap() throws IOException {
        InputLayer input = getInput();
        if (input == null) {
            Util.err("No input!");
            return null;
        }
        if (!input.isActive()) {
            // non-active input layer: abort
            return null;
        }
        Bitmap bm = null;
        bm = inputBitmap(input);
        if (bm == null) {
            Util.err("Input failed!");
            return null;
        }
        return bm;
    }

    private List<AdjustmentLayer> layersBefore(String layerName) {
        List<AdjustmentLayer> list = new ArrayList<AdjustmentLayer>();
        for (Layer layer: layerList) {
            if (layerName != null && layerName.equals(layer.getName()))
                break;
            if (layer instanceof AdjustmentLayer)
                list.add((AdjustmentLayer) layer);
        }
        return list;
    }

    private List<AdjustmentLayer> tryRearrangeLayersBefore(String layerName,
            boolean useViewport) {
        List<AdjustmentLayer> list = layersBefore(layerName);
        if (useViewport)
            list.add(viewLayer);
        if (previewQuality == PreviewQuality.Fast)
            list = LayerRearranger.optimizeLayerOrder(list);
        return list;
    }

    private Bitmap internalGenerateBitmapBeforeLayer(String layerName,
            boolean useViewport) throws IOException {
        if (layerName != null) {
            Layer test = findLayer(layerName);
            if (test == null) {
                Util.warn("Failed to find layer: " + layerName);
                return null;
            }
            if (test == getInput()) {
                Util.warn("Cannot get a bitmap before the input layer: "
                        + layerName);
                return null;
            }
        }

        Bitmap bm = getInputBitmap();
        if (bm == null)
            return null;

        Debug.print(this, "preview quality " + previewQuality);
        List<AdjustmentLayer> layers = tryRearrangeLayersBefore(layerName,
                useViewport);

        for (AdjustmentLayer layer : layers) {
            if (layer.isActive())
                bm = applyLayer(bm, layer);
        }

        return bm;
    }

    protected Bitmap generateBitmap(boolean useViewport) throws IOException {
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
    public RGBHistograms getHistogramsBeforeLayer(String layerName, boolean useViewport)
    throws IOException {
        Bitmap bm = internalGenerateBitmapBeforeLayer(layerName, useViewport);
        if (bm == null)
            return null;
        // TODO: figure out a more logical way to reduce the color weight
        if (bm.getChannelBitDepth() != 8)
            bm = BitmapUtil.create8BitCopy(bm);
        return new RGBHistograms(bm);
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
        if (layerName != null && findLayer(layerName) == null)
            return null;
        BitmapSize size = inputSize(input);
        if (size.pixelScaleFactor <= 0)
            size.pixelScaleFactor = 1.0;
        Debug.print(this, "input size: " + size.width + "x" + size.height);
        List<AdjustmentLayer> layers = layersBefore(layerName);
        double lastFactor = size.pixelScaleFactor;
        for (AdjustmentLayer layer: layers) {
            if (layer.isActive() && (layer instanceof DimensionAdjustmentLayer)) {
                DimensionAdjustmentLayer dLayer = (DimensionAdjustmentLayer) layer;
                size = dLayer.calculateSize(size);
                if (size.pixelScaleFactor <= 0)
                    size.pixelScaleFactor = lastFactor;
                lastFactor = size.pixelScaleFactor;
                Debug.print(this, dLayer.getDescription() + ": " + size.width + "x" + size.height);
            }
        }
        return size;
    }

    @Override
    public BitmapSize getBitmapSize() throws IOException {
        return getBitmapSizeBeforeLayer(null);
    }

    /**
     * Copy session data from the other session.  The implementation will attempt
     * to reuse existing layer object, if possible.
     *
     * The data copied includes the layers.
     * If the <code>forHistory</code> parameter is <code>false</code>,
     * it will also include properties like the name and project file path.
     *
     * @param other The session to copy layers from.
     * @param forHistory <code>true</code> if this is done for history purposes (such as undo/redo)
     */
    public void synchronizeSessionData(BlimpSession other, boolean forHistory) {
        List<Layer> newList = new ArrayList<Layer>();
        for (Layer otherLayer: other.layerList)
            newList.add(findOrCloneLayer(otherLayer));
        layerList = newList;
        if (!forHistory) {
            // Don't overwrite name or projectFilePath during e.g. undo()
            setName(other.getName());
            setProjectFilePath(other.getProjectFilePath());
        }
        //viewport.copyDataFrom(other.viewport);
    }

    private Layer findOrCloneLayer(Layer otherLayer) {
        String layerName = otherLayer.getName();
        int i = indexOfLayer(layerName, layerList);
        if (i >= 0) {
            assert (layerList.get(i) != null);
            if (layerList.get(i).getClass() != otherLayer.getClass())
                // different layer classes with same name
                i = -1;
        }
        if (i < 0)
            return (Layer) otherLayer.clone();
        Layer foundLayer = layerList.get(i);
        // Note: remove by index avoids equals(), so it is faster than remove(Object)
        layerList.remove(i); 
        Serializer.copyBeanData(otherLayer, foundLayer);
        return foundLayer;
    }

    public void setInput(InputLayer newInput) {
        assert (newInput != null);
        if (layerList.isEmpty()) {
            layerList.add(newInput);
        }
        else if (layerList.get(0) instanceof InputLayer) {
            layerList.get(0).removeChangeListener(this);
            layerList.set(0, newInput);
        }
        else {
            Util.warn("the first layer is not an input layer (setInput)");
            layerList.add(0, newInput);
        }
        newInput.addChangeListener(this);
        invalidate();
    }
    
    /**
     * Get an subset of "interesting" Exif data for the current session. 
     * @return A table of Exif data, or <code>null</code>.
     * @throws IOException if an unexpected I/O error occurs
     */
    public ExifTable getInterestingExifData() throws IOException {
        InputLayer input = getInput();
        if (input == null)
            return null;
        Bitmap bm = inputBitmap(input);
        if (bm == null)
            return null;
        return BitmapUtil.copyInterestingExifData(bm.getExifTable());
    }

    public InputLayer getInput() {
        if (layerList.isEmpty())
            return null;
        Layer first = layerList.get(0);
        if (first instanceof InputLayer)
            return (InputLayer) first;
        Util.warn("the first layer is not an input layer (getInput)");
        return null;
    }

    public Bitmap getBitmap() throws IOException {
        ZoomFactor zoom = viewLayer.zoom();
        if (currentBitmap == null)
            applyLayers();
        viewLayer.setZoom(zoom);
        return currentBitmap;
    }

    public Bitmap getSizedBitmap(int width, int height,
            PreviewQuality quality) throws IOException {
        viewLayer.setViewWidth(width);
        viewLayer.setViewHeight(height);
        previewQuality = quality;
        applyLayers();
        return currentBitmap;
    }

    public Bitmap getFullBitmap() throws IOException {
        return generateBitmap(false);
    }

    public void zoomIn() {
        viewLayer.zoom().zoomIn();
        invalidate();
        triggerChangeEvent();
    }

    public void zoomOut() {
        viewLayer.zoom().zoomOut();
        invalidate();
        triggerChangeEvent();
    }

    public double getCurrentZoom() {
        ZoomFactor zoom = viewLayer.zoom();
        if (zoom == null)
            return 1.0;
        else
            return zoom.toDouble();
    }

    public int layerCount() {
        return layerList.size();
    }
    
    private static <T extends Layer> int indexOfLayer(String name, List<T> layers) {
        if (name == null)
            return -1;
        for (int i=0; i<layers.size(); i++) {
            if (layers.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    public Layer findLayer(String name) {
        int i = indexOfLayer(name, layerList);
        if (i < 0)
            return null;
        return layerList.get(i);
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
        layerList.remove(index);
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
    public List<? extends BlimpBean> getChildren() {
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
     * Overrides the BlimpBean function used by serialization.
     */
    public void removeAllChildren() {
        layerList.clear();
    }

    /**
     * Overridden so that the input property is not serialized directly.
     */
    protected boolean isSerializableProperty(PropertyDescriptor pd) {
        if (pd.getName().equals("input") || pd.getName().equals("projectFilePath"))
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

    public void setPreviewQuality(PreviewQuality previewQuality) {
        this.previewQuality = previewQuality;
    }

    public PreviewQuality getPreviewQuality() {
        return previewQuality;
    }

    /**
     * Set the project file path, which should either be an absolute file path
     * or <code>null</code>.
     * @param projectFilePath the project file path to set.
     */
    public void setProjectFilePath(String projectFilePath) {
        this.projectFilePath = projectFilePath;
        // maybe call setNameFromFilename() here...
    }

    /**
     * Get the project file path, which is either an absolute file path to a
     * project (.blimp) file, or <code>null</code>.
     * @return the project file path.
     */
    public String getProjectFilePath() {
        return projectFilePath;
    }

    /**
     * Overridden to automatically set the project file path.
     */
    protected void beanLoaded(String filename) {
        setProjectFilePath(filename);
    }

    /**
     * Overridden to automatically set the project file path.
     */
    protected void beanSaved(String filename) {
        setProjectFilePath(filename);
    }
}
