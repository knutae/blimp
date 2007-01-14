package org.boblycat.blimp.tests;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.AdjustmentLayer;

public class DummyLayer extends AdjustmentLayer {
	public enum Enum { ONE, TWO, THREE };

	int intValue;
	String stringValue;
	Enum enumValue;
	
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
	
	public Enum getEnumValue() {
		return enumValue;
	}
	
	public void setEnumValue(Enum e) {
		enumValue = e;
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
