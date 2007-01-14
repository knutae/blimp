package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.layers.BrightnessContrastLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class BrightnessContrastEditor extends LayerEditor {
    ValueSlider brightnessSlider;

    ValueSlider contrastSlider;

    ValueSlider createSlider(String caption) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE, caption, -100,
                100, 0);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }

    void updateLayer() {
        BrightnessContrastLayer bcLayer = (BrightnessContrastLayer) layer;
        bcLayer.setBrightness(brightnessSlider.getSelection());
        bcLayer.setContrast(contrastSlider.getSelection());
        bcLayer.invalidate();
    }

    protected void layerChanged() {
        BrightnessContrastLayer bcLayer = (BrightnessContrastLayer) layer;
        brightnessSlider.setSelection(bcLayer.getBrightness());
        contrastSlider.setSelection(bcLayer.getContrast());
    }

    public BrightnessContrastEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout(SWT.VERTICAL));
        brightnessSlider = createSlider("Brightness");
        contrastSlider = createSlider("Contrast");
    }
}
