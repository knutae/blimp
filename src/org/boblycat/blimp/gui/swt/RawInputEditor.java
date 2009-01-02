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

import org.boblycat.blimp.ColorDepth;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.layers.RawFileInputLayer;
import org.boblycat.blimp.layers.RawFileInputLayer.Quality;
import org.boblycat.blimp.layers.RawFileInputLayer.WhiteBalance;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class RawInputEditor extends GridBasedLayerEditor {
    private RawFileInputLayer input;
    private Label filePathLabel;
    private Button radio8Bit;
    private Button radio16Bit;
    private Button radioQualityHalfSize;
    private Button radioQualityLow;
    private Button radioQualityNormal;
    private Button radioQualityHigh;
    private Button radioWBCamera;
    private Button radioWBAuto;

    public RawInputEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout());
        filePathLabel = new Label(this, SWT.NONE);

        Group group = createGroup("Color Depth per Channel");
        radio8Bit = createRadioButton(group, "8-bit");
        radio16Bit = createRadioButton(group, "16-bit");

        group = createGroup("Interpolation Quality");
        radioQualityHalfSize = createRadioButton(group, "Half-size (fastest)");
        radioQualityLow = createRadioButton(group, "Low (bilinear)");
        radioQualityNormal = createRadioButton(group, "Normal (Variable Number of Gradients)");
        radioQualityHigh = createRadioButton(group, "High (Adaptive Homogeneity-Directed)");

        group = createGroup("White Balance");
        radioWBCamera = createRadioButton(group, "Camera Settings");
        radioWBAuto = createRadioButton(group, "Auto");
    }

    @Override
    protected void updateLayer() {
        if (radio8Bit.getSelection())
            input.setColorDepth(ColorDepth.Depth8Bit);
        else if (radio16Bit.getSelection())
            input.setColorDepth(ColorDepth.Depth16Bit);
        else
            Util.err("No color depth selected?");

        if (radioQualityHalfSize.getSelection())
            input.setQuality(Quality.HalfSize);
        else if (radioQualityLow.getSelection())
            input.setQuality(Quality.Low);
        else if (radioQualityNormal.getSelection())
            input.setQuality(Quality.Normal);
        else if (radioQualityHigh.getSelection())
            input.setQuality(Quality.High);
        else
            Util.err("No quality selected?");

        if (radioWBCamera.getSelection())
            input.setWhiteBalance(WhiteBalance.Camera);
        else if (radioWBAuto.getSelection())
            input.setWhiteBalance(WhiteBalance.Auto);
        else
            Util.err("No white balance selected?");
    }

    protected void layerChanged() {
        input = (RawFileInputLayer) layer;
        filePathLabel.setText(input.getFilePath());
        boolean use16BitColor = input.getColorDepth() == ColorDepth.Depth16Bit;
        radio16Bit.setSelection(use16BitColor);
        radio8Bit.setSelection(!use16BitColor);

        RawFileInputLayer.Quality quality = input.getQuality();
        if (quality == Quality.HalfSize)
            radioQualityHalfSize.setSelection(true);
        else if (quality == Quality.Low)
            radioQualityLow.setSelection(true);
        else if (quality == Quality.Normal)
            radioQualityNormal.setSelection(true);
        else if (quality == Quality.High)
            radioQualityHigh.setSelection(true);

        WhiteBalance wb = input.getWhiteBalance();
        if (wb == WhiteBalance.Camera)
            radioWBCamera.setSelection(true);
        else if (wb == WhiteBalance.Auto)
            radioWBAuto.setSelection(true);
    }

    @Override
    public boolean previewByDefault() {
        return false;
    }
}
