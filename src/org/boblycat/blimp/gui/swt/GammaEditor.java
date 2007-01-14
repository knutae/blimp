package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.GammaLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class GammaEditor extends LayerEditor {
    GammaLayer gamma;

    ValueSlider gammaSlider;

    public GammaEditor(Composite parent, int style) {
        super(parent, style);
        // allow gamma values from 0.50 to 5.00
        gammaSlider = new ValueSlider(this, SWT.NONE, "Gamma", 50, 500, 2);
        gammaSlider.setPageIncrement(50);
        gammaSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        setLayout(new FillLayout());
    }

    void updateLayer() {
        if (gamma == null)
            return;
        double value = ((double) gammaSlider.getSelection()) / 100.0;
        gamma.setGamma(value);
        gamma.invalidate();
    }

    @Override
    protected void layerChanged() {
        gamma = (GammaLayer) layer;
        double value = gamma.getGamma();
        long lvalue = Math.round(value * 100.0);
        if (Math.abs(lvalue) > Integer.MAX_VALUE) {
            System.err
                    .println("Integer overflow while converting gamma value.");
            return;
        }
        gammaSlider.setSelection((int) lvalue);
    }

}
