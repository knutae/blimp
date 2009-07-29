/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
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
package org.boblycat.blimp.gui.swt.editors;

import org.boblycat.blimp.layers.PrintLayer;
import org.eclipse.swt.widgets.Shell;

public class PrintLayerDialog extends EditorDialog {

    public PrintLayerDialog(Shell parentShell,
            LayerEditorEnvironment environment) {
        super(parentShell, LayerEditorRegistry.getConstructor(PrintEditor.class),
                environment);
    }

    public void show() {
        env.layerWasJustAdded = true;
        env.session.beginDisableAutoRecord();
        try {
            PrintLayer layer = new PrintLayer();
            // New layers are always inactive before the editing starts
            layer.setActive(false);
            env.session.addLayer(layer);
            env.layerWasJustAdded = true;
            env.layer = layer;
            env.editorCallback = new LayerEditorCallback() {
                public void editingFinished(LayerEditorEnvironment env,
                        boolean cancelled) {
                    // unconditionally remove the print layer
                    env.session.removeLayer(env.layer);
                }
            };
            super.show();
        }
        finally {
            env.layerWasJustAdded = false;
            env.session.endDisableAutoRecord();
        }
    }
}
