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
package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.layers.UnsharpMaskLayer;
import org.eclipse.swt.widgets.Composite;

/**
 * Editor for unsharp mask.
 *
 * @author Knut Arild Erstad
 */
public class UnsharpMaskEditor extends GridBasedLayerEditor {
    UnsharpMaskLayer unsharpMask;
    ValueSlider levelSlider;

    public UnsharpMaskEditor(Composite parent, int style) {
        super(parent, style);
        levelSlider = createSlider("Level", UnsharpMaskLayer.MIN_LEVEL,
                UnsharpMaskLayer.MAX_LEVEL, 0);
    }

    @Override
    protected void updateLayer() {
        unsharpMask.setLevel(levelSlider.getSelection());
    }

    @Override
    protected void layerChanged() {
        unsharpMask = (UnsharpMaskLayer) layer;
        levelSlider.setSelection(unsharpMask.getLevel());
    }

}
