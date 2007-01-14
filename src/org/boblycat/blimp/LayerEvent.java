package org.boblycat.blimp;

import java.util.EventObject;

import org.boblycat.blimp.layers.Layer;

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