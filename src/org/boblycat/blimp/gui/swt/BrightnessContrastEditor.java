package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;

public class BrightnessContrastEditor extends LayerEditor {
	Scale brightnessScale;
	Scale contrastScale;
	
	Scale createScale(String caption) {
		Label label = new Label(this, SWT.NONE);
		label.setText(caption);
		Scale scale = new Scale(this, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(200);
		scale.setSelection(100);
		scale.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				updateLayer();
			}
		});
		return scale;
	}
	
	void updateLayer() {
		BrightnessContrastLayer bcLayer = (BrightnessContrastLayer) layer;
		bcLayer.setBrightness(brightnessScale.getSelection() - 100);
		bcLayer.setContrast(contrastScale.getSelection() - 100);
		bcLayer.invalidate();
	}
	
	protected void layerChanged() {
		BrightnessContrastLayer bcLayer = (BrightnessContrastLayer) layer;
		brightnessScale.setSelection(bcLayer.getBrightness() + 100);
		contrastScale.setSelection(bcLayer.getContrast() + 100);
	}
	
	public BrightnessContrastEditor(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());
		
		brightnessScale = createScale("Brightness");
		contrastScale = createScale("Contrast");
	}
}
