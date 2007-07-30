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

import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.BitmapSizeGeneratedTask;
import org.boblycat.blimp.layers.CropLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CropEditor extends LayerEditor {
    CropLayer crop;
    BitmapSize bitmapSize;
    ValueSlider leftSlider;
    ValueSlider rightSlider;
    ValueSlider topSlider;
    ValueSlider bottomSlider;
    
    public CropEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout());
        
        leftSlider = createSlider("Left", false);
        rightSlider = createSlider("Right", true);
        topSlider = createSlider("Top", false);
        bottomSlider = createSlider("Bottom", true);

        leftSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                resolveAndUpdate(leftSlider, rightSlider,
                        bitmapSize.scaledWidth());
            }
        });
        rightSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                resolveAndUpdate(rightSlider, leftSlider,
                        bitmapSize.scaledWidth());
            }
        });
        topSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                resolveAndUpdate(topSlider, bottomSlider,
                        bitmapSize.scaledHeight());
            }
        });
        bottomSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                resolveAndUpdate(bottomSlider, topSlider,
                        bitmapSize.scaledHeight());
            }
        });
    }
    
    private void resolveAndUpdate(ValueSlider master, ValueSlider slave,
            int totalAvailableSize) {
        int size1 = master.getSelection();
        int size2 = slave.getSelection();
        if (size1 + size2 >= totalAvailableSize)
            slave.setSelection(totalAvailableSize - size1 - 1);
        updateLayer();
    }
    
    ValueSlider createSlider(String caption, boolean flipDirection) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE, caption,  0, 1000, 0);
        slider.setFlipDirection(flipDirection);
        slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return slider;
    }
    
    void updateGui() {
        if (isDisposed())
            return;
        setEnabled(true);
        if (bitmapSize != null) {
            int w = bitmapSize.scaledWidth();
            int h = bitmapSize.scaledHeight();
            leftSlider.updateMinMax(0, w-1);
            rightSlider.updateMinMax(0, w-1);
            topSlider.updateMinMax(0, h-1);
            bottomSlider.updateMinMax(0, h-1);
        }
        if (crop != null) {
            leftSlider.setSelection(crop.getLeft());
            rightSlider.setSelection(crop.getRight());
            topSlider.setSelection(crop.getTop());
            bottomSlider.setSelection(crop.getBottom());
        }
    }
    
    void updateLayer() {
        crop.setLeft(leftSlider.getSelection());
        crop.setRight(rightSlider.getSelection());
        crop.setTop(topSlider.getSelection());
        crop.setBottom(bottomSlider.getSelection());
        crop.invalidate();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        leftSlider.setEnabled(enabled);
        rightSlider.setEnabled(enabled);
        topSlider.setEnabled(enabled);
        bottomSlider.setEnabled(enabled);
    }
    
    @Override
    protected void layerChanged() {
        crop = (CropLayer) layer;
        if (bitmapSize == null) {
            setEnabled(false);
            workerThread.asyncGenerateBitmapSize(this, session, crop.getName(),
                    new BitmapSizeGeneratedTask() {
                public void handleSize(BitmapSize size) {
                    bitmapSize = size;
                    updateGui();
                }
            });
        }
    }

    @Override
    public boolean previewByDefault() {
        return false;
    }
}
