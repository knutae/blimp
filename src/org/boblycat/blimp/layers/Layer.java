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
}

class LayerEventSource extends EventSource<LayerChangeListener, LayerEvent> {
    protected void triggerListenerEvent(LayerChangeListener listener,
            LayerEvent event) {
        listener.handleChange(event);
    }
}
