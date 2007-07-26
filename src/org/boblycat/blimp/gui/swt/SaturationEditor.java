package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.layers.SaturationLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SaturationEditor extends LayerEditor {
    SaturationLayer saturation;
    ValueSlider saturationSlider;
    ValueSlider lightnessSlider;

    public SaturationEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout(SWT.VERTICAL));
        saturationSlider = createSlider("Saturation", 0, 400);
        lightnessSlider = createSlider("Lightness", 0, 400);
    }
    
    private ValueSlider createSlider(String caption, int min, int max) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE,
                caption, min, max, 0);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }
    
    private void updateLayer() {
        if (saturation == null)
            return;
        saturation.setSaturation(saturationSlider.getSelection());
        saturation.setLightness(lightnessSlider.getSelection());
        saturation.invalidate();
    }
    
    private void updateGui() {
        if (saturation == null || isDisposed())
            return;
        saturationSlider.setSelection(saturation.getSaturation());
        lightnessSlider.setSelection(saturation.getLightness());
    }

    @Override
    protected void layerChanged() {
        saturation = (SaturationLayer) layer;
        updateGui();
    }

}
