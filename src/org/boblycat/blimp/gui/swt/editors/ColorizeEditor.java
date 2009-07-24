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
import org.boblycat.blimp.layers.ColorizeLayer;
import org.eclipse.swt.widgets.Composite;

/**
 * Editor for {@link ColorizeLayer}.
 *
 * @author Knut Arild Erstad
 */
public class ColorizeEditor extends GridBasedLayerEditor {
    ColorizeLayer colorize;
    ValueSlider hueSlider;
    ValueSlider lightnessSlider;
    ValueSlider baseSaturationSlider;
    ValueSlider saturationMultiplierSlider;

    /**
     * Sole constructor.
     * @param parent
     * @param style
     */
    public ColorizeEditor(Composite parent, int style) {
        super(parent, style);
        hueSlider = createSlider("Hue", 0, 360, 0);
        lightnessSlider = createSlider("Lightness", 0, 400, 0);
        baseSaturationSlider = createSlider("Base Saturation", 0, 400, 0);
        saturationMultiplierSlider = createSlider("Relative Saturation", 0, 400, 0);
    }

    @Override
    protected void updateLayer() {
        colorize.setHue(hueSlider.getSelection());
        colorize.setLightness(lightnessSlider.getSelection());
        colorize.setBaseSaturation(baseSaturationSlider.getSelection());
        colorize.setSaturationMultiplier(saturationMultiplierSlider.getSelection());
    }

    @Override
    protected void layerChanged() {
        colorize = (ColorizeLayer) layer;
        hueSlider.setSelection(colorize.getHue());
        lightnessSlider.setSelection(colorize.getLightness());
        baseSaturationSlider.setSelection(colorize.getBaseSaturation());
        saturationMultiplierSlider.setSelection(colorize.getSaturationMultiplier());
    }
}
