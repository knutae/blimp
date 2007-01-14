package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.ColorDepth;
import org.boblycat.blimp.layers.RawFileInputLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class RawInputEditor extends LayerEditor {
    RawFileInputLayer input;
    Label filePathLabel;
    Button radio8Bit;
    Button radio16Bit;

    public RawInputEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout());
        filePathLabel = new Label(this, SWT.NONE);
        Group group = new Group(this, SWT.NONE);
        group.setText("Color Depth per Channel");
        group.setLayout(new FillLayout(SWT.VERTICAL));
        Listener buttonListener = new Listener() {
            public void handleEvent(Event e) {
                if (input == null)
                    return;
                if (e.widget == radio8Bit)
                    input.setColorDepth(ColorDepth.Depth8Bit);
                else if (e.widget == radio16Bit)
                    input.setColorDepth(ColorDepth.Depth16Bit);
            }
        };
        radio8Bit = new Button(group, SWT.RADIO);
        radio8Bit.setText("8-bit");
        radio8Bit.addListener(SWT.Selection, buttonListener);
        radio16Bit = new Button(group, SWT.RADIO);
        radio16Bit.setText("16-bit");
        radio16Bit.addListener(SWT.Selection, buttonListener);
    }

    protected void layerChanged() {
        input = (RawFileInputLayer) layer;
        filePathLabel.setText(input.getFilePath());
        boolean use16BitColor = input.getColorDepth() == ColorDepth.Depth16Bit;
        radio16Bit.setSelection(use16BitColor);
        radio8Bit.setSelection(!use16BitColor);
    }
}
