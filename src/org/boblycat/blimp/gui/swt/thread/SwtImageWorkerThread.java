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
package org.boblycat.blimp.gui.swt.thread;


import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.data.BitmapUtil;
import org.boblycat.blimp.event.ProgressEvent;
import org.boblycat.blimp.event.ProgressEventSource;
import org.boblycat.blimp.event.ProgressListener;
import org.boblycat.blimp.gui.swt.ImageConverter;
import org.boblycat.blimp.layers.PrintLayer;
import org.boblycat.blimp.session.BlimpSession;
import org.boblycat.blimp.thread.ImageWorkerThread;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * SWT implementation of an image worker thread.
 *
 * @author Knut Arild Erstad
 */
public class SwtImageWorkerThread extends ImageWorkerThread {
    public interface PrintTask {
        void handleSuccess(String printJobName);
        void handleError(String printJobName, String errorMessage);
    }
    
    public class SharedData {
        public ImageData imageData;
        public double zoom;
        public Bitmap viewBitmap;
        public String errorMessage;
    }

    private Display display;
    private SharedData sharedData;
    private boolean finished;
    private ProgressEventSource guiProgressEventSource;
    private Listener disposeListener;

    class ProgressGuiEventRunner implements Runnable {
        ProgressEvent event;

        ProgressGuiEventRunner(ProgressEvent event) {
            this.event = event;
        }

        public void run() {
            guiProgressEventSource.triggerChangeWithEvent(event);
        }
    }

    public SwtImageWorkerThread(Display display) {
        this.display = display;
        Listener displayDisposeListener = new Listener() {
            public void handleEvent(Event e) {
                setFinished(true);
            }
        };
        display.addListener(SWT.Dispose, displayDisposeListener);
        sharedData = new SharedData();
        guiProgressEventSource = new ProgressEventSource();

        disposeListener = new Listener() {
            public void handleEvent(Event e) {
                cancelRequestsByOwner(e.widget);
            }
        };
    }

    @Override
    protected void bitmapGenerated(Runnable runnable, Bitmap bitmap) {
        if (isFinished())
            return;
        // convert to SWT image data on the worker thread
        if (bitmap != null) {
            Bitmap tmpBitmap = BitmapUtil.create8BitCopy(bitmap);
            ImageData data = ImageConverter.jiuToSwtImageData(tmpBitmap.getImage());
            double currentZoom = getSession().getCurrentZoom();
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

    @Override
    public void asyncExec(Runnable runnable) {
        if (isFinished() || display.isDisposed())
            return;
        display.asyncExec(runnable);
    }

    @Override
    protected void progressReported(ProgressEvent event) {
        if (isFinished())
            return;
        display.asyncExec(new ProgressGuiEventRunner(event));
        // Note: the event must not be used on the worker thread
        // from here on.
    }

    @Override
    protected void handleError(Runnable runnable, String errorMessage) {
        if (isFinished())
            return;
        synchronized (sharedData) {
            sharedData.errorMessage = errorMessage;
        }
        if (!display.isDisposed())
            display.asyncExec(runnable);
    }

    @Override
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

    /**
     * Register a widget as a possible owner for asynchronous events.
     * Causes outstanding events to be canceled when the widget is disposed
     * (except for events that are already started).
     *
     * @param widget a widget that is also an event owner.
     */
    public void registerOwnerWidget(Widget widget) {
        widget.addListener(SWT.Dispose, disposeListener);
    }

    public void asyncPrint(Object owner, BlimpSession session, PrintTask task, PrinterData printerData,
            PrintLayer printLayer) {
        putRequest(new PrintRequest(this, owner, session, task, printerData, printLayer));
    }
}
