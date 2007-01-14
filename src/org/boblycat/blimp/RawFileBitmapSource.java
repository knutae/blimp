package org.boblycat.blimp;

import java.io.BufferedInputStream;
import java.io.IOException;
//import net.sourceforge.jiu.codecs.PNMCodec;
import org.boblycat.blimp.jiu.PNMCodec;
import net.sourceforge.jiu.ops.OperationFailedException;

public class RawFileBitmapSource extends BitmapSource {
	Bitmap bitmap;
	String filePath;
	
	public RawFileBitmapSource(String filePath) {
		bitmap = new Bitmap();
		this.filePath = filePath;
		load();
	}
	
	private String dcrawExecutable() {
		// TODO: make this configurable
		//return "c:/projects/java-imaging/build/dcraw.exe";
		String path = System.getProperty("blimp.dcraw.path");
		if (path == null || path.length() == 0)
			path = "dcraw";
		return path;
	}
	
	public void load() {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(dcrawExecutable(),
					"-4", "-c", filePath);
			Process process = processBuilder.start();
			PNMCodec codec = new PNMCodec();
			codec.setInputStream(new BufferedInputStream(process.getInputStream()));
			codec.process();
			System.out.println(codec.getImage().getClass());
			bitmap.setImage(codec.getImage());
			process.destroy();
		}
		catch (IOException e) {
			System.err.println("Error executing dcraw or loading RAW file: "
					+ filePath);
			System.err.println(e.getMessage());
		}
		catch (OperationFailedException e) {
			System.err.println("Error reading RAW file: " + filePath);
			System.err.println(e.getMessage());
		}
		//catch (InterruptedException e) { e.printStackTrace(); }
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public String getDescription() {
		return "Raw image loader (dcraw)";
	}

}
