package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.ColorDepth;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.layers.RawFileInputLayer;
import org.boblycat.blimp.layers.RawFileInputLayer.Quality;
import org.boblycat.blimp.layers.RawFileInputLayer.WhiteBalance;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
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
    Button radioQualityHalfSize;
    Button radioQualityLow;
    Button radioQualityNormal;
    Button radioQualityHigh;
    Button radioWBCamera;
    Button radioWBAuto;
    Listener buttonListener;

    public RawInputEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout());
        filePathLabel = new Label(this, SWT.NONE);
        buttonListener = new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        };
        
        Group group = createRadioGroup("Color Depth per Channel");
        radio8Bit = createRadioButton(group, "8-bit");
        radio16Bit = createRadioButton(group, "16-bit");

        group = createRadioGroup("Interpolation Quality");
        radioQualityHalfSize = createRadioButton(group, "Half-size (fastest)");
        radioQualityLow = createRadioButton(group, "Low (bilinear)");
        radioQualityNormal = createRadioButton(group, "Normal (Variable Number of Gradients)");
        radioQualityHigh = createRadioButton(group, "High (Adaptive Homogeneity-Directed)");
        
        group = createRadioGroup("White Balance");
        radioWBCamera = createRadioButton(group, "Camera Settings");
        radioWBAuto = createRadioButton(group, "Auto");
    }
    
    private Button createRadioButton(Group group, String caption) {
        Button button = new Button(group, SWT.RADIO);
        button.setText(caption);
        button.addListener(SWT.Selection, buttonListener);
        return button;
    }
    
    private Group createRadioGroup(String caption) {
        Group group = new Group(this, SWT.NONE);
        group.setText(caption);
        group.setLayout(new FillLayout(SWT.VERTICAL));
        group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        return group;
    }
    
    private void updateLayer() {
        if (input == null)
            return;
        
        if (radio8Bit.getSelection())
            input.setColorDepth(ColorDepth.Depth8Bit);
        else if (radio16Bit.getSelection())
            input.setColorDepth(ColorDepth.Depth16Bit);
        else
            Util.err("No color depth selected?");
        
        if (radioQualityHalfSize.getSelection())
            input.setQuality(Quality.HalfSize);
        else if (radioQualityLow.getSelection())
            input.setQuality(Quality.Low);
        else if (radioQualityNormal.getSelection())
            input.setQuality(Quality.Normal);
        else if (radioQualityHigh.getSelection())
            input.setQuality(Quality.High);
        else
            Util.err("No quality selected?");
        
        if (radioWBCamera.getSelection())
            input.setWhiteBalance(WhiteBalance.Camera);
        else if (radioWBAuto.getSelection())
            input.setWhiteBalance(WhiteBalance.Auto);
        else
            Util.err("No white balance selected?");
        
    }

    protected void layerChanged() {
        input = (RawFileInputLayer) layer;
        filePathLabel.setText(input.getFilePath());
        boolean use16BitColor = input.getColorDepth() == ColorDepth.Depth16Bit;
        radio16Bit.setSelection(use16BitColor);
        radio8Bit.setSelection(!use16BitColor);
        
        RawFileInputLayer.Quality quality = input.getQuality();
        if (quality == Quality.HalfSize)
            radioQualityHalfSize.setSelection(true);
        else if (quality == Quality.Low)
            radioQualityLow.setSelection(true);
        else if (quality == Quality.Normal)
            radioQualityNormal.setSelection(true);
        else if (quality == Quality.High)
            radioQualityHigh.setSelection(true);
        
        WhiteBalance wb = input.getWhiteBalance();
        if (wb == WhiteBalance.Camera)
            radioWBCamera.setSelection(true);
        else if (wb == WhiteBalance.Auto)
            radioWBAuto.setSelection(true);
    }
}
