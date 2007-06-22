package org.boblycat.blimp.gui.swt;

import org.eclipse.swt.widgets.Composite;
import org.boblycat.blimp.BlimpSession;
import org.boblycat.blimp.ImageWorkerThread;
import org.boblycat.blimp.layers.Layer;

public abstract class LayerEditor extends Composite {
    protected Layer layer;
    protected ImageWorkerThread workerThread;
    protected BlimpSession session;

    public LayerEditor(Composite parent, int style) {
        super(parent, style);
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
        layerChanged();
    }
    
    protected abstract void layerChanged();
    
    /**
     * This function decides if the layer editor will enable previewing
     * while editing by default.
     * @return <code>true</code> by default, override to change the behaviour. 
     */
    public boolean previewByDefault() {
        return true;
    }
}
