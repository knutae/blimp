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
