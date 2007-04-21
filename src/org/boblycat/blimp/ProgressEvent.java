package org.boblycat.blimp;

import org.boblycat.blimp.layers.Layer;

public class ProgressEvent extends LayerEvent {
    private static final long serialVersionUID = 1L;
    
    public double progress;
    public String message;

    public ProgressEvent(Layer layer, String message, double progress) {
        super(layer);
        this.message = message;
        this.progress = progress;
    }

    public ProgressEvent(Layer layer) {
        this(layer, null, 0.0);
    }
}
