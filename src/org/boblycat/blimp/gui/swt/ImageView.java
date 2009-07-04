/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.boblycat.blimp.BlimpSession.PreviewQuality;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.*;

class BitmapEventSource extends EventSource<BitmapChangeListener, BitmapEvent> {
    protected void triggerListenerEvent(BitmapChangeListener listener,
            BitmapEvent event) {
        listener.handleChange(event);
    }
}

/*
 * This helper class is used to uniquely identify the currently displayed
 * image in order to avoid multiple worker thread requests in a row for the
 * same image.
 */
class ImageInfo {
    String sessionXml;
    int zoomLevel;
    BlimpSession.PreviewQuality quality;

    ImageInfo(String sessionXml, int zoomLevel,
            BlimpSession.PreviewQuality quality) {
        this.sessionXml = sessionXml;
        this.zoomLevel = zoomLevel;
        this.quality = quality;
    }

    boolean equals(String sessionXml, int zoomLevel,
            BlimpSession.PreviewQuality quality) {
        if (this.zoomLevel != zoomLevel || this.quality != quality)
            return false;
        if (sessionXml == null)
            return this.sessionXml == null;
        return sessionXml.equals(this.sessionXml);
    }
}

public class ImageView extends Composite {
    static final int PROGRESS_REDRAW_DELAY = 500;
    static final int CHANGE_EVENT_DELAY = 100;

    HistoryBlimpSession session;
    Runnable bitmapGeneratedTask;
    int delayedRequestCount;
    ImageCanvas imageCanvas;
    CLabel zoomLabel;
    Combo qualityCombo;
    ProgressBar progressBar;
    boolean showProgressBar;
    SwtImageWorkerThread workerThread;
    int asyncRequestCount;
    boolean needNewRequest;
    ImageInfo lastRequestedImageInfo;
    String cachedSessionXml;
    SwtImageWorkerThread.SharedData threadData;
    BitmapEventSource bitmapEventSource;
    ProgressBarTimer progressBarTimer;
    int zoomLevel;

    class ProgressBarTimer implements Runnable {
        boolean cancelled;

        public void run() {
            if (!cancelled)
                showProgressBar = true;
        }
    }

    public ImageView(Composite parent, int style, HistoryBlimpSession aSession) {
        super(parent, style);

        workerThread = new SwtImageWorkerThread(getDisplay());
        workerThread.addProgressListener(new ProgressListener() {
            public void reportProgress(ProgressEvent e) {
                setProgress(e.message, e.progress);
            }
        });
        workerThread.start();

        // Create GUI components
        imageCanvas = new ImageCanvas(this, SWT.NONE);

        zoomLabel = new CLabel(this, SWT.NONE);
        zoomLabel.setText("100%");

        qualityCombo = new Combo(this, SWT.READ_ONLY);
        qualityCombo.setItems(new String[] {"Fast", "Accurate"});
        qualityCombo.select(0);
        qualityCombo.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                asyncGenerateBitmap();
            }
        });

        progressBar = new ProgressBar(this, SWT.HORIZONTAL);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setSelection(0);

        ToolBar toolBar = new ToolBar(this, SWT.BORDER);
        ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);
        toolItem.setText("Zoom In");
        toolItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                zoomLevel++;
                workerThread.zoomIn(null, session, bitmapGeneratedTask);
                asyncImageRequestSent();
            }
        });
        toolItem = new ToolItem(toolBar, SWT.NONE);
        toolItem.setText("Zoom Out");
        toolItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                zoomLevel--;
                workerThread.zoomOut(null, session, bitmapGeneratedTask);
                asyncImageRequestSent();
            }
        });

        // Layout logic
        FormData data = new FormData();
        data.top = new FormAttachment(0);
        data.right = new FormAttachment(100);
        zoomLabel.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(0);
        data.right = new FormAttachment(zoomLabel);
        qualityCombo.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(0);
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(qualityCombo);
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
        imageCanvas.setLayoutData(data);

        setLayout(new FormLayout());

        // Create session
        if (aSession != null)
            session = aSession;
        else
            session = new HistoryBlimpSession();
        session.addChangeListener(new LayerChangeListener() {
            public void handleChange(LayerEvent event) {
                startDelayedRequest();
            }
        });

        bitmapEventSource = new BitmapEventSource();

        // task that is run after the worker thread has generated a bitmap
        bitmapGeneratedTask = new Runnable() {
            public void run() {
                assert(asyncRequestCount > 0);
                asyncRequestCount--;
                threadData = workerThread.getSharedData();
                ImageData data = threadData.imageData;
                if (data == null) {
                    // create a dummy image
                    PaletteData paletteData = new PaletteData(0xff, 0xff00, 0xff0000);
                    data = new ImageData(1, 1, 24, paletteData);
                }
                imageCanvas.setImageData(data);
                updateZoomLabel();
                if (needNewRequest)
                    asyncGenerateBitmap();
                triggerBitmapChange();
                // The error dialog must be shown after currentImage has been updated
                if (threadData.errorMessage != null)
                    SwtUtil.errorDialog(getShell(), "Image processing error",
                            threadData.errorMessage);
            }
        };

        addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                asyncRequestCount -= workerThread.cancelRequestsByOwner(this);
                workerThread.quit();
                // Note that while the image view calls quit() here, the worker
                // thread may still be processing, and outlive the image view.
                // For instance, an image export could be in progress.
            }
        });
    }
    
    private void startDelayedRequest() {
        if (getDisplay().isDisposed())
            return;
        assert (delayedRequestCount >= 0);
        delayedRequestCount++;
        //Util.info("start, count=" + delayedRequestCount);
        getDisplay().timerExec(CHANGE_EVENT_DELAY, new Runnable() {
            public void run() {
                endDelayedRequest();
                if (delayedRequestCount == 0) {
                    cachedSessionXml = null;
                    asyncGenerateBitmap();
                }
            }
        });
    }
    
    private void endDelayedRequest() {
        assert (delayedRequestCount > 0);
        delayedRequestCount--;
        //Util.info("end, count=" + delayedRequestCount);
    }

    public HistoryBlimpSession getSession() {
        return session;
    }

    private void updateZoomLabel() {
        int zoomPercentage = (int) (threadData.zoom * 100.0);
        zoomLabel.setText(Integer.toString(zoomPercentage) + "%");
        layout();
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
            imageCanvas.setProgressMessage(null);
        else
            imageCanvas.setProgressMessage(message);
    }

    private BlimpSession.PreviewQuality getPreviewQuality() {
        if (qualityCombo.getSelectionIndex() == 0)
            return PreviewQuality.Fast;
        else
            return PreviewQuality.Accurate;
    }

    private void asyncImageRequestSent() {
        if (cachedSessionXml == null)
            cachedSessionXml = Serializer.beanToXml(session);
        lastRequestedImageInfo = new ImageInfo(cachedSessionXml, zoomLevel,
                getPreviewQuality());
        asyncRequestCount++;
    }

    private boolean lastRequestEqualsCurrent() {
        return lastRequestedImageInfo != null
            && lastRequestedImageInfo.equals(cachedSessionXml, zoomLevel,
                    getPreviewQuality());
    }
    
    private void asyncGenerateBitmap() {
        if (isDisposed())
            return;
        assert(asyncRequestCount >= 0);
        if (asyncRequestCount > 0) {
            needNewRequest = true;
            return;
        }

        needNewRequest = false;

        if (lastRequestEqualsCurrent())
            return;

        asyncRequestCount -= workerThread.cancelRequestsByOwner(this);
        Rectangle destArea = imageCanvas.getCanvasClientArea();
        workerThread.asyncGenerateSizedBitmap(this, session, bitmapGeneratedTask,
                destArea.width, destArea.height,
                getPreviewQuality());
        asyncImageRequestSent();
    }

    public void addBitmapListener(BitmapChangeListener listener) {
        bitmapEventSource.addListener(listener);
    }

    public void removeBitmapListener(BitmapChangeListener listener) {
        bitmapEventSource.removeListener(listener);
    }

    public void triggerBitmapChange() {
        if (threadData == null)
            return;
        bitmapEventSource.triggerChangeWithEvent(
                new BitmapEvent(this, threadData.viewBitmap));
    }
}