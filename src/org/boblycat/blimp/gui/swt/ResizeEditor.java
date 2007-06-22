package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.Util;
import org.boblycat.blimp.layers.ResizeLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ResizeEditor extends LayerEditor {
    ResizeLayer resizeLayer;
    Text maxSizeEdit;
    
    public ResizeEditor(Composite parent, int style) {
        super(parent, style);
        Label caption = new Label(this, SWT.NONE);
        caption.setText("Maximum size in pixels:");
        maxSizeEdit = new Text(this, SWT.BORDER);
        maxSizeEdit.addListener(SWT.Verify, new Listener() {
            public void handleEvent(Event e) {
                StringBuffer buf = new StringBuffer(e.text.length());
                for (char c : e.text.toCharArray()) {
                    if (Character.isDigit(c))
                        buf.append(c);
                }
                e.text = buf.toString();
            }
        });
        maxSizeEdit.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        /*
        maxSizeSlider = new ValueSlider(this, SWT.NONE, "Max Size (pixels)",
                1, 10000, 0);
        maxSizeSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        */
        setLayout(new FillLayout(SWT.VERTICAL));
    }
    
    void updateLayer() {
        if (resizeLayer == null)
            return;
        //resizeLayer.setMaxSize(maxSizeSlider.getSelection());
        try {
            int value = Integer.parseInt(maxSizeEdit.getText());
            resizeLayer.setMaxSize(value);
            resizeLayer.invalidate();
        }
        catch (NumberFormatException e) {
            Util.err(e.getMessage());
        }
    }
    
    @Override
    protected void layerChanged() {
        resizeLayer = (ResizeLayer) layer;
        if (resizeLayer == null)
            return;
        //maxSizeSlider.setSelection(resizeLayer.getMaxSize());
        maxSizeEdit.setText(Integer.toString(resizeLayer.getMaxSize()));
    }
    
    @Override
    public boolean previewByDefault() {
        return false;
    }
}
