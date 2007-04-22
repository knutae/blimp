package org.boblycat.blimp.gui.swt;

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
    String errorMessage;
}

class SwtImageWorkerThread extends ImageWorkerThread {
    Display display;
    private SharedData sharedData;
    private boolean finished;
    ProgressEventSource guiProgressEventSource;
    
    class ProgressGuiEventRunner implements Runnable {
        ProgressEvent event;
        
        ProgressGuiEventRunner(ProgressEvent event) {
            this.event = event;
        }
        
        public void run() {
            guiProgressEventSource.triggerChangeWithEvent(event);
        }
    }
    
    public SwtImageWorkerThread(Composite imageView) {
        this.display = imageView.getDisplay();
        Listener disposeListener = new Listener() {
            public void handleEvent(Event e) {
                setFinished(true);
            }
        };
        display.addListener(SWT.Dispose, disposeListener);
        imageView.addListener(SWT.Dispose, disposeListener);
        sharedData = new SharedData();
        guiProgressEventSource = new ProgressEventSource();
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
                sharedData.errorMessage = null;
            }
        }
        if (!display.isDisposed())
            display.asyncExec(runnable);
    }
    
    protected void histogramGenerated(HistogramGeneratedTask task) {
        if (isFinished() || display.isDisposed())
            return;
        display.asyncExec(task);
    }
    
    protected void progressReported(ProgressEvent event) {
        if (isFinished())
            return;
        display.asyncExec(new ProgressGuiEventRunner(event));
        // Note: the event must not be used on the worker thread
        // from here on.
    }
    
    protected void handleError(Runnable runnable, String errorMessage) {
        if (isFinished())
            return;
        synchronized (sharedData) {
            sharedData.errorMessage = errorMessage;
        }
        if (!display.isDisposed())
            display.asyncExec(runnable);
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
            returnData.errorMessage = sharedData.errorMessage;
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
    
    HistoryBlimpSession session;
    boolean dirty;
    Runnable bitmapGeneratedTask;
    Canvas canvas;
    Image currentImage;
    int paintCounter;
    CLabel zoomLabel;
    ProgressBar progressBar;
    boolean showProgressBar;
    SwtImageWorkerThread workerThread;
    int asyncRequestCount;
    boolean needNewRequest;
    SharedData threadData;
    BitmapEventSource bitmapEventSource;
    String currentProgressMessage;
    boolean delayedRedrawInProgress;
    ProgressBarTimer progressBarTimer;
    
    class ProgressBarTimer implements Runnable {
        boolean cancelled;
        
        public void run() {
            if (!cancelled)
                showProgressBar = true;
        }
    }

    public ImageView(Composite parent, int style, HistoryBlimpSession aSession) {
        super(parent, style);

        Listener redrawListener = new Listener() {
            public void handleEvent(Event e) {
                canvas.redraw();
            }
        };
        
        workerThread = new SwtImageWorkerThread(this);
        workerThread.addProgressListener(new ProgressListener() {
            public void reportProgress(ProgressEvent e) {
                setProgress(e.message, e.progress);
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
                    SwtUtil.fillBlackRect(e.gc, canvas.getClientArea());
                    drawProgressMessage(e.gc);
                    asyncGenerateBitmap();
                    return;
                }
                updateImageParams();
                Rectangle clientArea = canvas.getClientArea();
                Image bufferImage = new Image(canvas.getDisplay(),
                        clientArea.width, clientArea.height);
                GC imageGC = new GC(bufferImage);
                SwtUtil.fillBlackRect(imageGC, bufferImage.getBounds());
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
        
        progressBar = new ProgressBar(this, SWT.HORIZONTAL);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setSelection(0);
        
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
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(100);
        data.bottom = new FormAttachment(100);
        progressBar.setLayoutData(data);
        
        data = new FormData();
        data.top = new FormAttachment(toolBar);
        //data.bottom = new FormAttachment(100);
        data.bottom = new FormAttachment(progressBar);
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(100);
        canvas.setLayoutData(data);

        setLayout(new FormLayout());

        // Create session
        if (aSession != null)
            session = aSession;
        else
            session = new HistoryBlimpSession();
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
                if (data == null) {
                    // create a dummy image
                    PaletteData paletteData = new PaletteData(0xff, 0xff00, 0xff0000);
                    data = new ImageData(1, 1, 24, paletteData);
                }
                if (currentImage != null)
                    currentImage.dispose();
                currentImage = new Image(getDisplay(), data);
                invalidateImage();
                triggerBitmapChange();
                // The error dialog must be shown after currentImage has been updated
                if (threadData.errorMessage != null)
                    SwtUtil.errorDialog(getShell(), "Image processing error",
                            threadData.errorMessage);
            }
        };
        
        addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                workerThread.quit();
                SwtUtil.dispose(currentImage);
            }
        });
    }

    public HistoryBlimpSession getSession() {
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
    
    private void cancelProgressBarTimer() {
        if (progressBarTimer != null)
            progressBarTimer.cancelled = true;
        progressBarTimer = null;
    }
    
    private void setProgress(String message, double progress) {
        if (isDisposed())
            return;
        // Progress bar
        if (progress == 1.0) {
            cancelProgressBarTimer();
            showProgressBar = false;
        }
        else if (progress == 0.0) {
            cancelProgressBarTimer();
            showProgressBar = false;
            progressBarTimer = new ProgressBarTimer();
            getDisplay().timerExec(PROGRESS_REDRAW_DELAY, progressBarTimer);
        }
        if (showProgressBar) {
            int percentage = (int) (progress * 100);
            progressBar.setSelection(percentage);
        }
        else {
            progressBar.setSelection(0);
        }
        // Progress message
        if (progress == 1.0)
            currentProgressMessage = null;
        else
            currentProgressMessage = message;
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
        Color color = new Color(gc.getDevice(), 255, 255, 255);
        gc.setForeground(color);
        gc.drawText("Processing: " + currentProgressMessage, 10, 10);
        color.dispose();
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