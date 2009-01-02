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

import org.boblycat.blimp.layers.GrayscaleMixerLayer;
import org.eclipse.swt.widgets.Composite;

public class GrayscaleMixerEditor extends GridBasedLayerEditor {
    GrayscaleMixerLayer grayscaleLayer;

    ValueSlider redSlider, greenSlider, blueSlider;

    public GrayscaleMixerEditor(Composite parent, int style) {
        super(parent, style);
        redSlider = createWeightSlider("Red");
        greenSlider = createWeightSlider("Green");
        blueSlider = createWeightSlider("Blue");
    }

    ValueSlider createWeightSlider(String caption) {
        return createSlider(caption,
                GrayscaleMixerLayer.MINIMUM_WEIGHT,
                GrayscaleMixerLayer.MAXIMUM_WEIGHT, 0);
    }

    @Override
    protected void updateLayer() {
        grayscaleLayer.setRed(redSlider.getSelection());
        grayscaleLayer.setGreen(greenSlider.getSelection());
        grayscaleLayer.setBlue(blueSlider.getSelection());
    }

    @Override
    protected void layerChanged() {
        grayscaleLayer = (GrayscaleMixerLayer) layer;
        redSlider.setSelection(grayscaleLayer.getRed());
        greenSlider.setSelection(grayscaleLayer.getGreen());
        blueSlider.setSelection(grayscaleLayer.getBlue());
    }

}
