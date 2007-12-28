/*
 * Copyright (C) 2007 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        if (layer == null || isDisposed())
            return;
        layerChanged();
    }

    /**
     * Called after the layer has been changed.
     * Implement this in subclasses to update the GUI.
     *
     * This function will not be called if the layer is null or the editor is
     * disposed.
     */
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
