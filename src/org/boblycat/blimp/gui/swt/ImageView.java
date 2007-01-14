package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;

public class ImageView extends Composite {
    BlimpSession session;
    boolean dirty;
    Runnable redrawTask;
    Canvas canvas;
    Image currentImage;
    int paintCounter;
    
    public ImageView(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());
        canvas = new Canvas(this, SWT.NO_BACKGROUND);
        canvas.addListener(SWT.Paint, new Listener() {
        	public void handleEvent(Event e) {
        		//System.out.println("paint " + paintCounter);
        		paintCounter++;
        		updateImage();
        		//GC gc = e.gc;
        		if (currentImage == null) {
        			e.gc.fillRectangle(canvas.getBounds());
        			return;
        		}
        		Image bufferImage = new Image(canvas.getDisplay(),
        				canvas.getSize().x, canvas.getSize().y);
        		GC imageGC = new GC(bufferImage);
    			imageGC.setBackground(new Color(imageGC.getDevice(), 0, 0, 0));
    			imageGC.fillRectangle(canvas.getBounds());
        		Point canvasSize = canvas.getSize();
        		//System.out.println("canvas size: " + canvasSize.x + ","
        		//		+ canvasSize.y);
        		Rectangle imageBounds = currentImage.getBounds();
        		//System.out.println("image size: "
        		//		+ imageBounds.width + "," + imageBounds.height);
        		int x = (canvasSize.x - imageBounds.width) / 2;
        		int y = (canvasSize.y - imageBounds.height) / 2;
        		imageGC.drawImage(currentImage, x, y);
        		e.gc.drawImage(bufferImage, 0, 0);
        	}
        });
        session = new BlimpSession();
        session.addChangeListener(new BitmapSourceListener() {
            public void handleChange(BitmapSource source) {
            	//invalidateImage();
            	invalidateWithDelay(100);
            }
        });
        redrawTask = new Runnable() {
        	public void run() {
        		if (!canvas.isDisposed())
        			canvas.redraw();
        	}
        };
    }
    
    public BlimpSession getSession() {
        return session;
    }
    
    private void updateImage() {
    	if (!dirty)
    		return;
    	Point destSize = canvas.getSize();
        Bitmap bitmap = session.getSizedBitmap(destSize.x, destSize.y);
        if (bitmap == null || bitmap.getImage() == null)
            return;
        try {
            currentImage = ImageConverter.bitmapToSwtImage(getDisplay(), bitmap);
        	dirty = false;
        }
        catch (Exception e) {
            //status("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void invalidateWithDelay(int delay) {
    	dirty = true;
    	getDisplay().timerExec(delay, redrawTask);
    }
    
    public void invalidateImage() {
    	dirty = true;
    	//imageLabel.redraw();
    	canvas.redraw();
    }
}