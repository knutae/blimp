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

import org.boblycat.blimp.layers.LocalContrastLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import static org.boblycat.blimp.layers.LocalContrastLayer.*;

public class LocalContrastEditor extends LayerEditor {
    LocalContrastLayer localContrast;
    ValueSlider radiusSlider;
    ValueSlider amountSlider;
    ValueSlider adaptiveSlider;

    public LocalContrastEditor(Composite parent, int style) {
        super(parent, style);
        radiusSlider = createSlider("Radius", MIN_RADIUS, MAX_RADIUS);
        amountSlider = createSlider("Amount", MIN_AMOUNT, MAX_AMOUNT);
        adaptiveSlider = createSlider("Adaptive", MIN_ADAPTIVE, MAX_ADAPTIVE);
        setLayout(new FillLayout(SWT.VERTICAL));
    }

    ValueSlider createSlider(String caption, int min, int max) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE, caption, min, max, 0);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }
    
    void updateLayer() {
        if (localContrast == null)
            return;
        localContrast.setRadius(radiusSlider.getSelection());
        localContrast.setAmount(amountSlider.getSelection());
        localContrast.setAdaptive(adaptiveSlider.getSelection());
        localContrast.invalidate();
    }
    
    @Override
    protected void layerChanged() {
        localContrast = (LocalContrastLayer) layer;
        radiusSlider.setSelection(localContrast.getRadius());
        amountSlider.setSelection(localContrast.getAmount());
        adaptiveSlider.setSelection(localContrast.getAdaptive());
    }

}
