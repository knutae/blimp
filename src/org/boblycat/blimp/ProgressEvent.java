package org.boblycat.blimp;

import org.boblycat.blimp.layers.Layer;

public class ProgressEvent extends LayerEvent {
    private static final long serialVersionUID = 1L;
    
    public int index;
    public int size;
    public String message;
    
    public ProgressEvent(Layer layer) {
        super(layer);
    }
}
