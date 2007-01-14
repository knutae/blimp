package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.*;

class SwtImageWorkerThread extends ImageWorkerThread {
    Display display;
    private ImageData imageData;
    private double zoom;
    
    public SwtImageWorkerThread(Display display) {
        this.display = display;
    }
    
    protected void bitmapGenerated(Runnable runnable, Bitmap bitmap) {
        if (isFinished())
            return;
        // convert to SWT image data on the worker thread
        if (bitmap != null) {
            ImageData data = ImageConverter.jiuToSwtImageData(bitmap.getImage());
            double currentZoom = session.getCurrentZoom();
            synchronized (this) {
                imageData = data;
                zoom = currentZoom;
            }
        }
        display.asyncExec(runnable);
    }
    
    protected boolean isFinished() {
        return display.isDisposed();
    }
    
    public synchronized ImageData getImageData() {
        return imageData;
    }
    
    public synchronized double getCurrentZoom() {
        return zoom;
    }
}

public class ImageView extends Composite {
    BlimpSession session;
    boolean dirty;
    Runnable bitmapGeneratedTask;
    Canvas canvas;
    Image currentImage;
    int paintCounter;
    CLabel zoomLabel;
    SwtImageWorkerThread workerThread;
    int asyncRequestCount;
    boolean needNewRequest;
    double currentZoom;

    public ImageView(Composite parent, int style, BlimpSession aSession) {
        super(parent, style);

        Listener redrawListener = new Listener() {
            public void handleEvent(Event e) {
                canvas.redraw();
            }
        };
        
        workerThread = new SwtImageWorkerThread(getDisplay());
        workerThread.start();

        // Create GUI components
        canvas = new Canvas(this, SWT.NO_BACKGROUND | SWT.H_SCROLL
                | SWT.V_SCROLL);
        canvas.getHorizontalBar().setEnabled(false);
        canvas.getVerticalBar().setEnabled(false);
        canvas.getHorizontalBar().addListener(SWT.Selection, redrawListener);
        canvas.getVerticalBar().addListener(SWT.Selection, redrawListener);

        canvas.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                // System.out.println("paint " + paintCounter);
                paintCounter++;
                if (currentImage == null) {
                    e.gc.fillRectangle(canvas.getClientArea());
                    asyncGenerateBitmap();
                    return;
                }
                updateImageParams();
                Rectangle clientArea = canvas.getClientArea();
                Image bufferImage = new Image(canvas.getDisplay(),
                        clientArea.width, clientArea.height);
                GC imageGC = new GC(bufferImage);
                imageGC.setBackground(new Color(imageGC.getDevice(), 0, 0, 0));
                imageGC.fillRectangle(bufferImage.getBounds());
                // System.out.println("canvas size: " + canvasSize.x + ","
                // + canvasSize.y);
                Rectangle imageBounds = currentImage.getBounds();
                // System.out.println("image size: "
                // + imageBounds.width + "," + imageBounds.height);
                int x, y;
                if (canvas.getHorizontalBar().isEnabled())
                    x = -canvas.getHorizontalBar().getSelection();
                else
                    x = (clientArea.width - imageBounds.width) / 2;
                if (canvas.getVerticalBar().isEnabled())
                    y = -canvas.getVerticalBar().getSelection();
                else
                    y = (clientArea.height - imageBounds.height) / 2;
                imageGC.drawImage(currentImage, x, y);
                imageGC.dispose();
                e.gc.drawImage(bufferImage, 0, 0);
                bufferImage.dispose(); // important!
            }
        });

        canvas.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) {
                invalidateImage();
            }
        });

        zoomLabel = new CLabel(this, SWT.NONE);
        zoomLabel.setText("100%");

        ToolBar toolBar = new ToolBar(this, SWT.BORDER);
        ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);
        toolItem.setText("Zoom In");
        toolItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                workerThread.zoomIn(bitmapGeneratedTask);
                
            }
        });
        toolItem = new ToolItem(toolBar, SWT.NONE);
        toolItem.setText("Zoom Out");
        toolItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                workerThread.zoomOut(bitmapGeneratedTask);
            }
        });

        // Layout logic
        FormData data = new FormData();
        data.top = new FormAttachment(0);
        data.right = new FormAttachment(100);
        zoomLabel.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(0);
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(zoomLabel);
        toolBar.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(toolBar);
        data.bottom = new FormAttachment(100);
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(100);
        canvas.setLayoutData(data);

        setLayout(new FormLayout());

        // Create session
        if (aSession != null)
            session = aSession;
        else
            session = new BlimpSession();
        session.addChangeListener(new LayerChangeListener() {
            public void handleChange(LayerEvent event) {
                asyncGenerateBitmap();
            }
        });

        // task that is run after the worker thread has generated a bitmap
        bitmapGeneratedTask = new Runnable() {
            public void run() {
                asyncRequestCount--;
                if (needNewRequest) {
                    asyncGenerateBitmap();
                    return;
                }
                ImageData data = workerThread.getImageData();
                if (data == null)
                    return;
                if (currentImage != null)
                    currentImage.dispose();
                currentImage = new Image(getDisplay(), data);
                currentZoom = workerThread.getCurrentZoom();
                invalidateImage();
            }
        };
        
        addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                workerThread.quit();
            }
        });
    }

    public BlimpSession getSession() {
        return session;
    }

    private static void prepareScrollBar(ScrollBar bar, int canvasPixels,
            int bitmapPixels) {
        int range = bitmapPixels - canvasPixels;
        boolean enabled = range > 0;
        bar.setEnabled(enabled);
        assert (enabled == bar.isEnabled());
        if (!enabled)
            return;
        bar.setMinimum(0);
        bar.setMaximum(range);
        // bar.setSelection(0);
    }

    private void updateImageParams() {
        if (!dirty || currentImage == null)
            return;
        Rectangle destArea = canvas.getClientArea();
        prepareScrollBar(canvas.getHorizontalBar(), destArea.width,
                currentImage.getBounds().width);
        prepareScrollBar(canvas.getVerticalBar(), destArea.height,
                currentImage.getBounds().height);
        int zoomPercentage = (int) (currentZoom * 100.0);
        zoomLabel.setText(Integer.toString(zoomPercentage) + "%");
        layout();
        dirty = false;
    }
    
    private void asyncGenerateBitmap() {
        if (asyncRequestCount > 0) {
            needNewRequest = true;
            return;
        }
        needNewRequest = false;
        workerThread.cancelRequests();
        Rectangle destArea = canvas.getClientArea();
        workerThread.asyncGenerateSizedBitmap(session, bitmapGeneratedTask,
                destArea.width, destArea.height);
        asyncRequestCount++;
    }

    public void invalidateImage() {
        if (needNewRequest) {
            asyncGenerateBitmap();
            return;
        }
        dirty = true;
        canvas.redraw();
    }
}