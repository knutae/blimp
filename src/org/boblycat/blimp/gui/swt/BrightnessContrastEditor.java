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

import org.boblycat.blimp.layers.BrightnessContrastLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class BrightnessContrastEditor extends LayerEditor {
    ValueSlider brightnessSlider;

    ValueSlider contrastSlider;

    ValueSlider createSlider(String caption) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE, caption, -100,
                100, 0);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }

    void updateLayer() {
        BrightnessContrastLayer bcLayer = (BrightnessContrastLayer) layer;
        bcLayer.setBrightness(brightnessSlider.getSelection());
        bcLayer.setContrast(contrastSlider.getSelection());
        bcLayer.invalidate();
    }

    protected void layerChanged() {
        BrightnessContrastLayer bcLayer = (BrightnessContrastLayer) layer;
        brightnessSlider.setSelection(bcLayer.getBrightness());
        contrastSlider.setSelection(bcLayer.getContrast());
    }

    public BrightnessContrastEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout(SWT.VERTICAL));
        brightnessSlider = createSlider("Brightness");
        contrastSlider = createSlider("Contrast");
    }
}
