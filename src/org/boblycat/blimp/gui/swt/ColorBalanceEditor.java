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

import org.boblycat.blimp.layers.ColorBalanceLayer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Editor for {@link ColorBalanceLayer}.
 * 
 * @author Knut Arild Erstad
 */
public class ColorBalanceEditor extends GridBasedLayerEditor {
    ColorBalanceLayer colorBalance;
    
    ValueSlider redSlider;
    ValueSlider greenSlider;
    ValueSlider blueSlider;
    Button lightnessButton;
    
    public ColorBalanceEditor(Composite parent, int style) {
        super(parent, style);
        redSlider = createSlider("Cyan / Red", -100, 100, 0);
        greenSlider = createSlider("Magenta / Green", -100, 100, 0);
        blueSlider = createSlider("Yellow / Blue", -100, 100, 0);
        lightnessButton = createCheckButton(this, "Preserve Lightness");
    }

    @Override
    protected void updateLayer() {
        colorBalance.setCyanRed(redSlider.getSelection());
        colorBalance.setMagentaGreen(greenSlider.getSelection());
        colorBalance.setYellowBlue(blueSlider.getSelection());
        colorBalance.setPreserveLightness(lightnessButton.getSelection());
    }

    @Override
    protected void layerChanged() {
        colorBalance = (ColorBalanceLayer) layer;
        redSlider.setSelection(colorBalance.getCyanRed());
        greenSlider.setSelection(colorBalance.getMagentaGreen());
        blueSlider.setSelection(colorBalance.getYellowBlue());
        lightnessButton.setSelection(colorBalance.getPreserveLightness());
    }

}
