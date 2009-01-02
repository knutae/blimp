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

import org.boblycat.blimp.layers.OrientationLayer;
import org.boblycat.blimp.layers.OrientationLayer.Rotation;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class OrientationEditor extends GridBasedLayerEditor {
    OrientationLayer orientation;
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

        Group group = createGroup("Rotation");
        radioRotateNone = createRadioButton(group, "None");
        radioRotate90Left = createRadioButton(group, "90 Degrees Left");
        radioRotate90Right = createRadioButton(group, "90 Degrees Right");
        radioRotate180 = createRadioButton(group, "180 Degrees");

        group = createGroup("Flip / Mirror");
        checkFlipHorizontal = createCheckButton(group, "Flip Horizontally");
        checkFlipVertical = createCheckButton(group, "Flip Vertically");
    }

    @Override
    protected void updateLayer() {
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
    }

    @Override
    protected void layerChanged() {
        orientation = (OrientationLayer) layer;
        Rotation r = orientation.getRotation();
        radioRotateNone.setSelection(r == Rotation.None);
        radioRotate90Left.setSelection(r == Rotation.Rotate90Left);
        radioRotate90Right.setSelection(r == Rotation.Rotate90Right);
        radioRotate180.setSelection(r == Rotation.Rotate180);
        checkFlipHorizontal.setSelection(orientation.getFlipHorizontal());
        checkFlipVertical.setSelection(orientation.getFlipVertical());
    }

}
