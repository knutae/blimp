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
import org.boblycat.blimp.layers.GammaLayer;
import org.eclipse.swt.widgets.Composite;

public class GammaEditor extends GridBasedLayerEditor {
    GammaLayer gamma;

    ValueSlider gammaSlider;

    public GammaEditor(Composite parent, int style) {
        super(parent, style);
        // allow gamma values from 0.50 to 5.00
        gammaSlider = createSlider("Gamma", 50, 500, 2);
    }

    @Override
    protected void updateLayer() {
        gamma.setGamma(gammaSlider.getSelectionAsDouble());
    }

    @Override
    protected void layerChanged() {
        gamma = (GammaLayer) layer;
        gammaSlider.setSelectionAsDouble(gamma.getGamma());
    }
}
