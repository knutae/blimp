package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.HistoryBlimpSession;
import org.boblycat.blimp.ImageWorkerThread;
import org.boblycat.blimp.layers.Layer;

/**
 * A collection of variables used by layer editors.
 * 
 * @author Knut Arild Erstad
 */
public class LayerEditorEnvironment {
    public LayerEditorCallback editorCallback;
    public ImageWorkerThread workerThread;
    public HistoryBlimpSession session;
    public Layer layer;
    public boolean layerWasJustAdded;
    
    public LayerEditorEnvironment clone() {
        LayerEditorEnvironment env = new LayerEditorEnvironment();
        env.editorCallback = editorCallback;
        env.workerThread = workerThread;
        env.session = session;
        env.layer = layer;
        env.layerWasJustAdded = layerWasJustAdded;
        return env;
    }
}
