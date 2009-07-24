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

import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.BitmapSizeGeneratedTask;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.gui.swt.ValueSlider;
import org.boblycat.blimp.layers.ResizeLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class ResizeEditor extends LayerEditor {
    static final int MAX_SIZE = 2000;

    ResizeLayer resizeLayer;
    int inputWidth;
    int inputHeight;
    ValueSlider widthSlider;
    ValueSlider heightSlider;
    Label inputSizeLabel;
    boolean gotInputSize;

    public ResizeEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout());
        inputSizeLabel = new Label(this, SWT.NONE);
        inputSizeLabel.setText("...");
        fitInGrid(inputSizeLabel);
        widthSlider = new ValueSlider(this, SWT.NONE, "Width", 1, MAX_SIZE, 0);
        fitInGrid(widthSlider);
        widthSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                widthChanged(widthSlider.getSelection());
            }
        });
        heightSlider = new ValueSlider(this, SWT.NONE, "Height", 1, MAX_SIZE, 0);
        fitInGrid(heightSlider);
        heightSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                heightChanged(heightSlider.getSelection());
            }
        });
        gotInputSize = false;
    }

    void fitInGrid(Control control) {
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    void widthChanged(int newWidth) {
        int maxSize;
        if (inputWidth > inputHeight) {
            maxSize = newWidth;
            heightSlider.setSelection(
                    Util.roundDiv(maxSize * inputHeight, inputWidth));
        }
        else {
            maxSize = Util.roundDiv(newWidth * inputHeight, inputWidth);
            heightSlider.setSelection(maxSize);
        }
        resizeLayer.setMaxSize(maxSize);
        resizeLayer.invalidate();
    }

    void heightChanged(int newHeight) {
        int maxSize;
        if (inputHeight > inputWidth) {
            maxSize = newHeight;
            widthSlider.setSelection(
                    Util.roundDiv(maxSize * inputWidth, inputHeight));
        }
        else {
            maxSize = Util.roundDiv(newHeight * inputWidth, inputHeight);
            widthSlider.setSelection(maxSize);
        }
        resizeLayer.setMaxSize(maxSize);
        resizeLayer.invalidate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        widthSlider.setEnabled(enabled);
        heightSlider.setEnabled(enabled);
        inputSizeLabel.setEnabled(enabled);
    }

    void initLayerSize(int width, int height) {
        if (isDisposed() || width <= 0 || height <= 0)
            return;
        inputWidth = width;
        inputHeight = height;
        inputSizeLabel.setText(String.format(
                "Original size: %s x %s", width, height));
        BitmapSize size = resizeLayer.calculateNewSize(inputWidth, inputHeight);
        widthSlider.setSelection(size.width);
        heightSlider.setSelection(size.height);
        setEnabled(true);
        gotInputSize = true;
    }

    @Override
    protected void layerChanged() {
        resizeLayer = (ResizeLayer) layer;
        if (resizeLayer == null)
            return;
        if (!gotInputSize) {
            setEnabled(false);
            workerThread.asyncGenerateBitmapSize(this, session,
                    resizeLayer.getName(),
                    new BitmapSizeGeneratedTask() {
                public void handleSize(BitmapSize size) {
                    initLayerSize(size.width, size.height);
                }
            });
        }
    }

    @Override
    public boolean previewByDefault() {
        return false;
    }
}
