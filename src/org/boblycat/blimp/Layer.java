package org.boblycat.blimp;

import java.util.Vector;

/**
 * Abstract base class for all layers in Blimp, which includes adjustment
 * layers (for image modification) and input layers.
 * In Blimp, all image operations are based upon adjustment layers which
 * must extend the AdjustmentLayer subclass.
 */
public abstract class Layer extends BlimpBean {
    boolean active;
    Vector<LayerChangeListener> changeListeners;
    
    public abstract String getDescription();

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
    
    public void triggerChangeEvent() {
        LayerEvent event = new LayerEvent(this);
        for (LayerChangeListener listener: changeListeners) {
            if (listener == null)
                continue;
            listener.handleChange(event);
        }
    }
    
    public void invalidate() {
        triggerChangeEvent();
    }
    
    /**
     * Overridden to return "layer" for serialization.
     * Overriding this function any further can break serialization,
     * so be careful.  (Some classes like BlimpSession needs to
     * override it, so it is not final.)
     */
    public String elementName() {
    	return "layer";
    }
}
