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

import org.boblycat.blimp.ColorRGB;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.layers.SolidColorBorderLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SolidColorBorderEditor extends LayerEditor {

    SolidColorBorderLayer border;

    ValueSlider leftSlider;
    ValueSlider rightSlider;
    ValueSlider topSlider;
    ValueSlider bottomSlider;
    ValueSlider allValuesSlider;

    Canvas colorCanvas;

    public SolidColorBorderEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout());

        Listener sliderListener = new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        };

        leftSlider = createSlider("Left", sliderListener);
        rightSlider = createSlider("Right", sliderListener);
        topSlider = createSlider("Top", sliderListener);
        bottomSlider = createSlider("Bottom", sliderListener);

        allValuesSlider = createSlider("All Borders", new Listener() {
            public void handleEvent(Event e) {
                int selection = allValuesSlider.getSelection();
                leftSlider.setSelection(selection);
                rightSlider.setSelection(selection);
                topSlider.setSelection(selection);
                bottomSlider.setSelection(selection);
                updateLayer();
            }
        });

        Composite c = new Composite(this, SWT.NONE);
        c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.justify = true;
        c.setLayout(rowLayout);

        Listener colorChangeListener = new Listener() {
            public void handleEvent(Event e) {
                ColorDialog dlg = new ColorDialog(getShell(), SWT.NONE);
                RGB rgb = dlg.open();
                if (rgb == null || border == null)
                    return;
                ColorRGB col = new ColorRGB(rgb.red, rgb.green, rgb.blue);
                border.setColor(col);
                border.invalidate();
                colorCanvas.redraw();
            }
        };

        colorCanvas = new Canvas(c, SWT.BORDER);
        colorCanvas.setLayoutData(new RowData(20, 20));
        colorCanvas.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                ColorRGB col;
                if (border == null)
                    col = ColorRGB.Black;
                else
                    col = border.getColor();
                SwtUtil.fillColorRect(e.gc, colorCanvas.getClientArea(),
                        col.getRed(), col.getGreen(), col.getBlue());
            }
        });
        colorCanvas.addListener(SWT.MouseDoubleClick, colorChangeListener);
        Button b = new Button(c, SWT.PUSH);
        b.setText("&Change Color");
        b.addListener(SWT.Selection, colorChangeListener);
    }

    ValueSlider createSlider(String caption, Listener selectionListener) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE, caption,
                0, 500, 0);
        slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        slider.addListener(SWT.Selection, selectionListener);
        return slider;
    }

    void updateLayer() {
        border.setLeft(leftSlider.getSelection());
        border.setRight(rightSlider.getSelection());
        border.setTop(topSlider.getSelection());
        border.setBottom(bottomSlider.getSelection());
        border.invalidate();
    }

    void updateGui() {
        leftSlider.setSelection(border.getLeft());
        rightSlider.setSelection(border.getRight());
        topSlider.setSelection(border.getTop());
        bottomSlider.setSelection(border.getBottom());
        // simply use the minimum value for the all values slider
        int min = Util.min(border.getLeft(), border.getRight(),
                border.getTop(), border.getBottom());
        allValuesSlider.setSelection(min);
    }

    @Override
    protected void layerChanged() {
        border = (SolidColorBorderLayer) layer;
        updateGui();
    }

}
