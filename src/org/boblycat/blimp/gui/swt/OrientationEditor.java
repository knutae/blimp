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

import org.boblycat.blimp.layers.OrientationLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import static org.boblycat.blimp.layers.OrientationLayer.*;

public class OrientationEditor extends LayerEditor {
    OrientationLayer orientation;
    Listener buttonListener;
    Button radioRotateNone;
    Button radioRotate90Left;
    Button radioRotate90Right;
    Button radioRotate180;
    Button checkFlipHorizontal;
    Button checkFlipVertical;
    
    public OrientationEditor(Composite parent, int style) {
        super(parent, style);
        GridLayout grid = new GridLayout();
        grid.numColumns = 1;
        setLayout(grid);
        
        buttonListener = new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        };
        
        Group group = createGroup("Rotation");
        radioRotateNone = createRadioButton(group, "None");
        radioRotate90Left = createRadioButton(group, "90 Degrees Left");
        radioRotate90Right = createRadioButton(group, "90 Degrees Right");
        radioRotate180 = createRadioButton(group, "180 Degrees");
        
        group = createGroup("Flip / Mirror");
        checkFlipHorizontal = createCheckButton(group, "Flip Horizontally");
        checkFlipVertical = createCheckButton(group, "Flip Vertically");
    }

    private Button createRadioButton(Composite parent, String caption) {
        Button button = new Button(parent, SWT.RADIO);
        button.setText(caption);
        button.addListener(SWT.Selection, buttonListener);
        return button;
    }
    
    private Group createGroup(String caption) {
        Group group = new Group(this, SWT.NONE);
        group.setText(caption);
        group.setLayout(new FillLayout(SWT.VERTICAL));
        group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        return group;
    }
    
    private Button createCheckButton(Composite parent, String caption) {
        Button button = new Button(parent, SWT.CHECK);
        button.setText(caption);
        button.addListener(SWT.Selection, buttonListener);
        return button;
    }
    
    private void updateLayer() {
        if (orientation == null)
            return;
        if (radioRotate90Left.getSelection())
            orientation.setRotation(Rotation.Rotate90Left);
        else if (radioRotate90Right.getSelection())
            orientation.setRotation(Rotation.Rotate90Right);
        else if (radioRotate180.getSelection())
            orientation.setRotation(Rotation.Rotate180);
        else
            orientation.setRotation(Rotation.None);
        orientation.setFlipHorizontal(checkFlipHorizontal.getSelection());
        orientation.setFlipVertical(checkFlipVertical.getSelection());
        orientation.invalidate();
    }
    
    private void updateGui() {
        if (orientation == null)
            return;
        Rotation r = orientation.getRotation();
        radioRotateNone.setSelection(r == Rotation.None);
        radioRotate90Left.setSelection(r == Rotation.Rotate90Left);
        radioRotate90Right.setSelection(r == Rotation.Rotate90Right);
        radioRotate180.setSelection(r == Rotation.Rotate180);
        checkFlipHorizontal.setSelection(orientation.getFlipHorizontal());
        checkFlipVertical.setSelection(orientation.getFlipVertical());
    }
    
    @Override
    protected void layerChanged() {
        orientation = (OrientationLayer) layer;
        updateGui();
    }

}
