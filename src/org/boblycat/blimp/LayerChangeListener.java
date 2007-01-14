package org.boblycat.blimp;

import java.util.EventListener;

public interface LayerChangeListener extends EventListener {
    void handleChange(LayerEvent event);
}