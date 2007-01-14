package org.boblycat.blimp;

import java.util.EventObject;

public class LayerEvent extends EventObject {
	private Layer layer;
    
    public LayerEvent(Layer layer) {
        super(layer);
        this.layer = layer;
    }
    
    public Layer getLayer() {
        return layer;
    }
}