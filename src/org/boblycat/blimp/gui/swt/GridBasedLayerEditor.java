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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

/**
 * A subclass of LayerEditor which uses a single-column GridLayout
 * and has some useful utility functions for creating sliders and
 * radio groups.
 *
 * @author Knut Arild Erstad
 */
public abstract class GridBasedLayerEditor extends LayerEditor {
    protected Listener updateLayerListener;

    public GridBasedLayerEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout());
        updateLayerListener = new Listener() {
            public void handleEvent(Event e) {
                updateLayerFromGui();
            }
        };
    }

    /**
     * Update the layer based on the current GUI state.
     * This function must be implemented by subclasses.
     *
     * This function will not be called if the layer is null or
     * the editor is disposed.
     *
     * Note: do not invalidate the layer from this function, since
     * it will be done automatically after the function has returned.
     */
    protected abstract void updateLayer();

    /**
     * Update the layer with the current GUI values, then invalidate the
     * layer.  This function cannot be overridden, implement updateLayer()
     * instead.
     */
    protected final void updateLayerFromGui() {
        if (layer == null || isDisposed())
            return;
        updateLayer();
        layer.invalidate();
    }

    private Button createButton(Composite parent, String caption, int style) {
        Button button = new Button(parent, style);
        button.setText(caption);
        button.addListener(SWT.Selection, updateLayerListener);
        return button;
    }

    protected Button createRadioButton(Composite parent, String caption) {
        return createButton(parent, caption, SWT.RADIO);
    }

    protected Button createCheckButton(Composite parent, String caption) {
        return createButton(parent, caption, SWT.CHECK);
    }

    protected Group createGroup(String caption) {
        Group group = new Group(this, SWT.NONE);
        group.setText(caption);
        group.setLayout(new FillLayout(SWT.VERTICAL));
        group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        return group;
    }

    protected ValueSlider createSliderWithoutListener(String caption,
            int min, int max, int digits) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE,
                caption, min, max, digits);
        slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return slider;
    }

    protected ValueSlider createSlider(String caption, int min, int max, int digits) {
        ValueSlider slider = createSliderWithoutListener(caption, min, max, digits);
        slider.addListener(SWT.Selection, updateLayerListener);
        return slider;
    }
}
