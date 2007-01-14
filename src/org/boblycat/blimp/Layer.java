package org.boblycat.blimp;

import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.data.PixelImage;

import java.util.Vector;

/**
 * Abstract base class for adjustment layers in Blimp.
 * In Blimp, all image operations are based upon adjustment layers which
 * must extend this class.
 */
public abstract class Layer extends BlimpBean {
    boolean active;
    Vector<LayerChangeListener> changeListeners;
    
    public abstract Bitmap applyLayer(Bitmap source);
    
    public abstract String getName();

    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Layer() {
        active = true;
        changeListeners = new Vector<LayerChangeListener>();
    }
    
    public void addChangeListener(LayerChangeListener listener) {
        int i = changeListeners.indexOf(null);
        if (i >= 0)
            changeListeners.setElementAt(listener, i);
        else
            changeListeners.add(listener);
    }
    
    public void removeChangeListener(LayerChangeListener listener) {
        int i = changeListeners.indexOf(listener);
        if (i >= 0)
            changeListeners.setElementAt(null, i);
    }
    
    void triggerChangeEvent() {
        LayerEvent event = new LayerEvent(this);
        for (LayerChangeListener listener: changeListeners) {
            if (listener == null)
                continue;
            listener.handleChange(event);
        }
    }
    
    protected PixelImage applyJiuOperation(PixelImage input, ImageToImageOperation op) {
        PixelImage image = input;
        op.setInputImage(image);
        try {
            op.process();
            image = op.getOutputImage();
        }
        catch (Exception e) {
            System.out.println(op.getClass().getName() + ": " + e.getMessage());
        }
        return image;
    }
    
    public void invalidate() {
        triggerChangeEvent();
    }
    
    /**
     * Overridden to return "layer" for serialization.
     * This function cannot be overridden any further since it could
     * break serialization.
     */
    public final String elementName() {
    	return "layer";
    }
}
