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

import org.boblycat.blimp.layers.GrayscaleMixerLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class GrayscaleMixerEditor extends LayerEditor {
    GrayscaleMixerLayer grayscaleLayer;

    ValueSlider redSlider, greenSlider, blueSlider;

    public GrayscaleMixerEditor(Composite parent, int style) {
        super(parent, style);
        redSlider = createWeightSlider("Red");
        greenSlider = createWeightSlider("Green");
        blueSlider = createWeightSlider("Blue");
        setLayout(new FillLayout(SWT.VERTICAL));
    }

    ValueSlider createWeightSlider(String caption) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE, caption,
                GrayscaleMixerLayer.MINIMUM_WEIGHT,
                GrayscaleMixerLayer.MAXIMUM_WEIGHT, 0);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }

    void updateLayer() {
        if (grayscaleLayer == null)
            return;
        grayscaleLayer.setRed(redSlider.getSelection());
        grayscaleLayer.setGreen(greenSlider.getSelection());
        grayscaleLayer.setBlue(blueSlider.getSelection());
        grayscaleLayer.invalidate();
    }

    void refreshGui() {
        if (grayscaleLayer == null)
            return;
        redSlider.setSelection(grayscaleLayer.getRed());
        greenSlider.setSelection(grayscaleLayer.getGreen());
        blueSlider.setSelection(grayscaleLayer.getBlue());
    }

    @Override
    protected void layerChanged() {
        grayscaleLayer = (GrayscaleMixerLayer) layer;
        refreshGui();
    }

}
