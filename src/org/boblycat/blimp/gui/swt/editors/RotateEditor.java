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

import org.boblycat.blimp.gui.swt.ValueSlider;
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
    private Button keepSizeButton;
    private Button expandSizeButton;
    private Button autoCropButton;

    public RotateEditor(Composite parent, int style) {
        super(parent, style);
        angleSlider = createSlider("Angle", -1800, 1800, 1);
        Group group = createGroup("Quality");
        fastRadioButton = createRadioButton(group, "Fast");
        antiAliasedRadioButton = createRadioButton(group, "Anti-Aliased");
        group = createGroup("Sizing Strategy");
        keepSizeButton = createRadioButton(group, "Keep Existing Size");
        expandSizeButton = createRadioButton(group, "Expand");
        autoCropButton = createRadioButton(group, "Auto-Crop (Keep Aspect)");
    }

    @Override
    protected void updateLayer() {
        rotateLayer.setAngle(angleSlider.getSelectionAsDouble());
        if (fastRadioButton.getSelection())
            rotateLayer.setQuality(RotateLayer.Quality.Fast);
        else
            rotateLayer.setQuality(RotateLayer.Quality.AntiAliased);
        if (keepSizeButton.getSelection())
            rotateLayer.setSizeStrategy(RotateLayer.SizeStrategy.Keep);
        else if (expandSizeButton.getSelection())
            rotateLayer.setSizeStrategy(RotateLayer.SizeStrategy.Expand);
        else
            rotateLayer.setSizeStrategy(RotateLayer.SizeStrategy.AutoCrop);
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
        switch (rotateLayer.getSizeStrategy()) {
        case Keep:
            keepSizeButton.setSelection(true);
            break;
        case Expand:
            expandSizeButton.setSelection(true);
            break;
        case AutoCrop:
            autoCropButton.setSelection(true);
            break;
        }
    }

}
