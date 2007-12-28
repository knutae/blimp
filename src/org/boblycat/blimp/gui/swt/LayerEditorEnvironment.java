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
