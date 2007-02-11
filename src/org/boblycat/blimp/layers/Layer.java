package org.boblycat.blimp.layers;

import org.boblycat.blimp.BlimpBean;
import org.boblycat.blimp.EventSource;
import org.boblycat.blimp.LayerChangeListener;
import org.boblycat.blimp.LayerEvent;

/**
 * Abstract base class for all layers in Blimp, which includes adjustment layers
 * (for image modification) and input layers. In Blimp, all image operations are
 * based upon adjustment layers which must extend the AdjustmentLayer subclass.
 */
public abstract class Layer extends BlimpBean {
    boolean active;

    LayerEventSource eventSource;
    
    String name;

    public abstract String getDescription();

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Layer() {
        active = true;
        eventSource = new LayerEventSource();
        name = null;
    }

    public void addChangeListener(LayerChangeListener listener) {
        eventSource.addListener(listener);
    }

    public void removeChangeListener(LayerChangeListener listener) {
        eventSource.removeListener(listener);
    }

    public void triggerChangeEvent() {
        eventSource.triggerChangeWithEvent(new LayerEvent(this));
    }

    public void invalidate() {
        triggerChangeEvent();
    }

    /**
     * Overridden to return "layer" for serialization. Overriding this function
     * any further can break serialization, so be careful. (Some classes like
     * BlimpSession needs to override it, so it is not final.)
     */
    public String elementName() {
        return "layer";
    }
    
    /**
     * Get a name which can be used as an identifier for the layer.
     * Within a session all names should be unique.
     * @return A name.
     */
    public String getName() {
        if (name == null || name.length() == 0)
            name = generateName(this, 1);
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        // TODO: make sure it is unique within a session...?
    }
    
    protected String getBaseName() {
        String className = getClass().getSimpleName();
        if (className.endsWith("Layer"))
            className = className.substring(0, className.length()-5);
        return className;
    }
    
    protected static String generateName(Layer layer, int suffixNumber) {
        return layer.getBaseName() + Integer.toString(suffixNumber);
    }
}

class LayerEventSource extends EventSource<LayerChangeListener, LayerEvent> {
    protected void triggerListenerEvent(LayerChangeListener listener,
            LayerEvent event) {
        listener.handleChange(event);
    }
}
