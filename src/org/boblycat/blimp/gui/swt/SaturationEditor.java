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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SaturationEditor extends LayerEditor {
    SaturationLayer saturation;
    ValueSlider saturationSlider;
    ValueSlider lightnessSlider;
    ValueSlider hueSlider;

    public SaturationEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout(SWT.VERTICAL));
        hueSlider = createSlider("Hue", -180, 180);
        saturationSlider = createSlider("Saturation", 0, 400);
        lightnessSlider = createSlider("Lightness", 0, 400);
    }
    
    private ValueSlider createSlider(String caption, int min, int max) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE,
                caption, min, max, 0);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }
    
    private void updateLayer() {
        if (saturation == null)
            return;
        saturation.setSaturation(saturationSlider.getSelection());
        saturation.setLightness(lightnessSlider.getSelection());
        saturation.setHue(hueSlider.getSelection());
        saturation.invalidate();
    }
    
    private void updateGui() {
        if (saturation == null || isDisposed())
            return;
        saturationSlider.setSelection(saturation.getSaturation());
        lightnessSlider.setSelection(saturation.getLightness());
        hueSlider.setSelection(saturation.getHue());
    }

    @Override
    protected void layerChanged() {
        saturation = (SaturationLayer) layer;
        updateGui();
    }

}
