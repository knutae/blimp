package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.layers.GrayscaleMixerLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class GrayscaleMixerEditor extends LayerEditor {
    GrayscaleMixerLayer grayscaleLayer;

    ValueSlider redSlider, greenSlider, blueSlider;

    public GrayscaleMixerEditor(Composite parent, int style) {
        super(parent, style);
        redSlider = createWeightSlider("Red");
        greenSlider = createWeightSlider("Green");
        blueSlider = createWeightSlider("Blue");
        setLayout(new FillLayout(SWT.VERTICAL));
    }

    ValueSlider createWeightSlider(String caption) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE, caption,
                GrayscaleMixerLayer.MINIMUM_WEIGHT,
                GrayscaleMixerLayer.MAXIMUM_WEIGHT, 0);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }

    void updateLayer() {
        if (grayscaleLayer == null)
            return;
        grayscaleLayer.setRed(redSlider.getSelection());
        grayscaleLayer.setGreen(greenSlider.getSelection());
        grayscaleLayer.setBlue(blueSlider.getSelection());
        grayscaleLayer.invalidate();
    }

    void refreshGui() {
        if (grayscaleLayer == null)
            return;
        redSlider.setSelection(grayscaleLayer.getRed());
        greenSlider.setSelection(grayscaleLayer.getGreen());
        blueSlider.setSelection(grayscaleLayer.getBlue());
    }

    @Override
    protected void layerChanged() {
        grayscaleLayer = (GrayscaleMixerLayer) layer;
        refreshGui();
    }

}
