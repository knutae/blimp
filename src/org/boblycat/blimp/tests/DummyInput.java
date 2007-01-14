package org.boblycat.blimp.tests;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.InputLayer;

public class DummyInput extends InputLayer {
	String path;
	
	public void setPath(String newPath) {
		path = newPath;
	}
	
	public String getPath() {
		return path;
	}

	@Override
	public Bitmap getBitmap() {
		return new Bitmap();
	}

	@Override
	public String getDescription() {
		return "Dummy Input";
	}

}
