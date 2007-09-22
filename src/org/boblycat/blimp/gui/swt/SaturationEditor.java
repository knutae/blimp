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

import org.boblycat.blimp.layers.SaturationLayer;
import org.eclipse.swt.widgets.Composite;

public class SaturationEditor extends GridBasedLayerEditor {
    SaturationLayer saturation;
    ValueSlider saturationSlider;
    ValueSlider lightnessSlider;
    ValueSlider hueSlider;

    public SaturationEditor(Composite parent, int style) {
        super(parent, style);
        hueSlider = createSlider("Hue", -180, 180, 0);
        saturationSlider = createSlider("Saturation", 0, 400, 0);
        lightnessSlider = createSlider("Lightness", 0, 400, 0);
    }
    
    @Override
    protected void updateLayer() {
        saturation.setSaturation(saturationSlider.getSelection());
        saturation.setLightness(lightnessSlider.getSelection());
        saturation.setHue(hueSlider.getSelection());
    }
    
    @Override
    protected void layerChanged() {
        saturation = (SaturationLayer) layer;
        saturationSlider.setSelection(saturation.getSaturation());
        lightnessSlider.setSelection(saturation.getLightness());
        hueSlider.setSelection(saturation.getHue());
    }

}
