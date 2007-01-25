package org.boblycat.blimp.gui.swt;

import java.util.concurrent.LinkedBlockingQueue;

import org.boblycat.blimp.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.*;

class SharedData {
    ImageData imageData;
    double zoom;
    Bitmap viewBitmap;
}

class SwtImageWorkerThread extends ImageWorkerThread {
    Display display;
    private SharedData sharedData;
    private boolean finished;
    ProgressEventSource guiProgressEventSource;
    LinkedBlockingQueue<ProgressEvent> progressEventQueue;
    
    public SwtImageWorkerThread(Display display) {
        this.display = display;
        display.addListener(SWT.Dispose, new Listener() {
           public void handleEvent(Event e) {
               setFinished(true);
           }
        });
        sharedData = new SharedData();
        guiProgressEventSource = new ProgressEventSource();
        progressEventQueue = new LinkedBlockingQueue<ProgressEvent>();
    }
    
    protected void bitmapGenerated(Runnable runnable, Bitmap bitmap) {
        if (isFinished())
            return;
        // convert to SWT image data on the worker thread
        if (bitmap != null) {
            Bitmap tmpBitmap = BitmapUtil.create8BitCopy(bitmap);
            ImageData data = ImageConverter.jiuToSwtImageData(tmpBitmap.getImage());
            double currentZoom = session.getCurrentZoom();
            synchronized (sharedData) {
                sharedData.viewBitmap = tmpBitmap;
                sharedData.imageData = data;
                sharedData.zoom = currentZoom;
            }
        }
        display.asyncExec(runnable);
    }
    
    protected void progressReported(ProgressEvent event) {
        progressEventQueue.add(event);
        display.asyncExec(new Runnable() {
            public void run() {
                try {
                    guiProgressEventSource.triggerChangeWithEvent(
                            progressEventQueue.take());
                }
                catch (InterruptedException e) {
                    Util.err(e.getMessage());
                }
            }
        });
    }
    
    protected synchronized boolean isFinished() {
        return finished;
    }
    
    private synchronized void setFinished(boolean finished) {
        this.finished = finished;
    }
    
    public SharedData getSharedData() {
        SharedData returnData = new SharedData();
        synchronized (sharedData) {
            returnData.imageData = sharedData.imageData;
            returnData.viewBitmap = sharedData.viewBitmap;
            returnData.zoom = sharedData.zoom;
        }
        return returnData;
    }
    
    public void addProgressListener(ProgressListener listener) {
        guiProgressEventSource.addListener(listener);
    }
    
    public void removeProgressListener(ProgressListener listener) {
        guiProgressEventSource.removeListener(listener);
    }
}

class BitmapEventSource extends EventSource<BitmapChangeListener, BitmapEvent> {
    protected void triggerListenerEvent(BitmapChangeListener listener,
            BitmapEvent event) {
        listener.handleChange(event);
    }
}

public class ImageView extends Composite {
    static final int PROGRESS_REDRAW_DELAY = 500;
    
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
    SharedData threadData;
    BitmapEventSource bitmapEventSource;
    String currentProgressMessage;
    boolean delayedRedrawInProgress;

    public ImageView(Composite parent, int style, BlimpSession aSession) {
        super(parent, style);

        Listener redrawListener = new Listener() {
            public void handleEvent(Event e) {
                canvas.redraw();
            }
        };
        
        workerThread = new SwtImageWorkerThread(getDisplay());
        workerThread.addProgressListener(new ProgressListener() {
            public void reportProgress(ProgressEvent e) {
                //System.out.println("Worker thread: " + e.message);
                // TODO: show a nice progress bar instead of this?
                if (e.index < e.size)
                    setProgressMessage(e.message);
                else
                    setProgressMessage(null);
                layout();
            }
        });
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
                    e.gc.setBackground(new Color(e.gc.getDevice(), 0, 0, 0));
                    e.gc.fillRectangle(canvas.getClientArea());
                    drawProgressMessage(e.gc);
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
                drawProgressMessage(imageGC);
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
        
        bitmapEventSource = new BitmapEventSource();

        // task that is run after the worker thread has generated a bitmap
        bitmapGeneratedTask = new Runnable() {
            public void run() {
                asyncRequestCount--;
                if (needNewRequest) {
                    asyncGenerateBitmap();
                    return;
                }
                threadData = workerThread.getSharedData();
                ImageData data = threadData.imageData;
                if (data == null)
                    return;
                if (currentImage != null)
                    currentImage.dispose();
                currentImage = new Image(getDisplay(), data);
                invalidateImage();
                triggerBitmapChange();
            }
        };
        
        addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                workerThread.quit();
                SwtUtil.dispose(currentImage);
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
        if (!dirty || currentImage == null || threadData == null)
            return;
        Rectangle destArea = canvas.getClientArea();
        prepareScrollBar(canvas.getHorizontalBar(), destArea.width,
                currentImage.getBounds().width);
        prepareScrollBar(canvas.getVerticalBar(), destArea.height,
                currentImage.getBounds().height);
        int zoomPercentage = (int) (threadData.zoom * 100.0);
        zoomLabel.setText(Integer.toString(zoomPercentage) + "%");
        layout();
        dirty = false;
    }
    
    private void setProgressMessage(String message) {
        currentProgressMessage = message;
        //canvas.redraw();
        if (delayedRedrawInProgress)
            return;
        delayedRedrawInProgress = true;
        getDisplay().timerExec(PROGRESS_REDRAW_DELAY, new Runnable() {
            public void run() {
                if (!canvas.isDisposed())
                    canvas.redraw();
                delayedRedrawInProgress = false;
            }
        });
    }
    
    private void drawProgressMessage(GC gc) {
        if (currentProgressMessage == null)
            return;
        gc.setForeground(new Color(gc.getDevice(), 255, 255, 255));
        gc.drawText("Processing: " + currentProgressMessage, 10, 10);
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
    
    public void addBitmapListener(BitmapChangeListener listener) {
        bitmapEventSource.addListener(listener);
    }
    
    public void removeBitmapListener(BitmapChangeListener listener) {
        bitmapEventSource.removeListener(listener);
    }
    
    public void triggerBitmapChange() {
        bitmapEventSource.triggerChangeWithEvent(
                new BitmapEvent(this, threadData.viewBitmap));
    }
}