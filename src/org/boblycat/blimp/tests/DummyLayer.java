package org.boblycat.blimp.tests;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.AdjustmentLayer;

public class DummyLayer extends AdjustmentLayer {

	int intValue;
	String stringValue;
	
	public void setIntValue(int i) {
		intValue = i;
	}
	
	public int getIntValue() {
		return intValue;
	}
	
	public void setStringValue(String str) {
		stringValue = str;
	}
	
	public String getStringValue() {
		if (stringValue == null)
			return "";
		return stringValue;
	}
	
	@Override
	public Bitmap applyLayer(Bitmap source) {
		return source;
	}

	@Override
	public String getDescription() {
		return "Dummy";
	}

}
