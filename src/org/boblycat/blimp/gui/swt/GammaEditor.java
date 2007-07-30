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

import org.boblycat.blimp.Util;
import org.boblycat.blimp.layers.GammaLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class GammaEditor extends LayerEditor {
    GammaLayer gamma;

    ValueSlider gammaSlider;

    public GammaEditor(Composite parent, int style) {
        super(parent, style);
        // allow gamma values from 0.50 to 5.00
        gammaSlider = new ValueSlider(this, SWT.NONE, "Gamma", 50, 500, 2);
        gammaSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        setLayout(new FillLayout());
    }

    void updateLayer() {
        if (gamma == null)
            return;
        double value = ((double) gammaSlider.getSelection()) / 100.0;
        gamma.setGamma(value);
        gamma.invalidate();
    }

    @Override
    protected void layerChanged() {
        gamma = (GammaLayer) layer;
        double value = gamma.getGamma();
        long lvalue = Math.round(value * 100.0);
        if (Math.abs(lvalue) > Integer.MAX_VALUE) {
            Util.err("Integer overflow while converting gamma value.");
            return;
        }
        gammaSlider.setSelection((int) lvalue);
    }

}
