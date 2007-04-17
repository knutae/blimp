package org.boblycat.blimp;

public class LayerEventSource extends
        EventSource<LayerChangeListener, LayerEvent> {

    protected void triggerListenerEvent(LayerChangeListener listener,
            LayerEvent event) {
        listener.handleChange(event);
    }

}
