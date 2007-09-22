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

import org.boblycat.blimp.layers.RotateLayer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * SWT RotateLayer editor.
 * 
 * @author Knut Arild Erstad
 */
public class RotateEditor extends GridBasedLayerEditor {
    private RotateLayer rotateLayer;
    private ValueSlider angleSlider;
    private Button fastRadioButton;
    private Button antiAliasedRadioButton;

    public RotateEditor(Composite parent, int style) {
        super(parent, style);
        angleSlider = createSlider("Angle", -1800, 1800, 1);
        Group group = createRadioGroup("Quality");
        fastRadioButton = createRadioButton(group, "Fast");
        antiAliasedRadioButton = createRadioButton(group, "Anti-Aliased");
    }
    
    @Override
    protected void updateLayer() {
        rotateLayer.setAngle(angleSlider.getSelectionAsDouble());
        if (fastRadioButton.getSelection())
            rotateLayer.setQuality(RotateLayer.Quality.Fast);
        else
            rotateLayer.setQuality(RotateLayer.Quality.AntiAliased);
    }
    
    @Override
    protected void layerChanged() {
        rotateLayer = (RotateLayer) layer;
        if (rotateLayer == null)
            return;
        angleSlider.setSelectionAsDouble(rotateLayer.getAngle());
        switch (rotateLayer.getQuality()) {
        case Fast:
            fastRadioButton.setSelection(true);
            break;
        case AntiAliased:
            antiAliasedRadioButton.setSelection(true);
            break;
        }
    }

}
