package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.layers.LocalContrastLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import static org.boblycat.blimp.layers.LocalContrastLayer.*;

public class LocalContrastEditor extends LayerEditor {
    LocalContrastLayer localContrast;
    ValueSlider radiusSlider;
    ValueSlider amountSlider;
    ValueSlider adaptiveSlider;

    public LocalContrastEditor(Composite parent, int style) {
        super(parent, style);
        radiusSlider = createSlider("Radius", MIN_RADIUS, MAX_RADIUS);
        amountSlider = createSlider("Amount", MIN_AMOUNT, MAX_AMOUNT);
        adaptiveSlider = createSlider("Adaptive", MIN_ADAPTIVE, MAX_ADAPTIVE);
        setLayout(new FillLayout(SWT.VERTICAL));
    }

    ValueSlider createSlider(String caption, int min, int max) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE, caption, min, max, 0);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }
    
    void updateLayer() {
        if (localContrast == null)
            return;
        localContrast.setRadius(radiusSlider.getSelection());
        localContrast.setAmount(amountSlider.getSelection());
        localContrast.setAdaptive(adaptiveSlider.getSelection());
        localContrast.invalidate();
    }
    
    @Override
    protected void layerChanged() {
        localContrast = (LocalContrastLayer) layer;
        radiusSlider.setSelection(localContrast.getRadius());
        amountSlider.setSelection(localContrast.getAmount());
        adaptiveSlider.setSelection(localContrast.getAdaptive());
    }

}
