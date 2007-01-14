package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.Layer;

/**
 * A simple callback interface for layer editors.
 * @author Knut Arild Erstad
 */
public interface LayerEditorCallback {
	public void editingFinished(Layer layer, boolean cancelled);
}
