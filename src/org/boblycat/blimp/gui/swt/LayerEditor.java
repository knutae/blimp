package org.boblycat.blimp.gui.swt;

import org.eclipse.swt.widgets.Composite;
import org.boblycat.blimp.layers.Layer;

public abstract class LayerEditor extends Composite {
    protected Layer layer;

    public LayerEditor(Composite parent, int style) {
        super(parent, style);
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
        layerChanged();
    }

    protected abstract void layerChanged();
}
