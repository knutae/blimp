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

import java.io.IOException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapUtil;
import org.boblycat.blimp.BlimpSession;
import org.boblycat.blimp.ImageWorkerThread;
import org.boblycat.blimp.ProgressEvent;
import org.boblycat.blimp.ProgressEventSource;
import org.boblycat.blimp.ProgressListener;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.BlimpSession.PreviewQuality;
import org.boblycat.blimp.layers.PrintLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.printing.Printer;
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
    public class SharedData {
        ImageData imageData;
        double zoom;
        Bitmap viewBitmap;
        String errorMessage;
    }

    private Display display;
    private SharedData sharedData;
    private boolean finished;
    private ProgressEventSource guiProgressEventSource;
    private Listener disposeListener;

    private class PrintRequest extends Request {
        private Printer printer;
        private PrintLayer printLayerCopy;

        PrintRequest(Object owner, BlimpSession session, Runnable runnable, PrinterData printerData,
                PrintLayer printLayer) {
            super(owner, session, runnable);
            printer = new Printer(printerData);
            printLayerCopy = (PrintLayer) sessionCopy.findLayer(printLayer.getName());
            assert (printLayerCopy != null);
        }

        @Override
        protected void execute() throws IOException {
            printLayerCopy.setActive(true);
            printLayerCopy.setPreview(false);
            sessionCopy.setPreviewQuality(PreviewQuality.Accurate);
            Bitmap bitmap = sessionCopy.getFullBitmap();
            ImageData imageData = ImageConverter.jiuToSwtImageData(bitmap.getImage());
            Image swtImage = new Image(printer, imageData);
            String jobName = "Blimp: " + sessionCopy.getName();
            if (printer.startJob(jobName)) {
                Util.info("Started printer job " + jobName);
                GC gc = new GC(printer);
                int left = (printLayerCopy.getPaperWidth() - imageData.width) / 2;
                int top = (printLayerCopy.getPaperHeight() - imageData.height) / 2;
                gc.drawImage(swtImage, left, top);
                printer.endPage();
                printer.endJob();
                Util.info("Finished printer job " + jobName);
                gc.dispose();
            }
            else {
                Util.err("Failed to start printer job " + jobName);
            }
            // TODO: make safe
            printer.dispose();
        }

    }

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

    @Override
    protected void asyncExec(Runnable runnable) {
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

    public void asyncPrint(Object owner, BlimpSession session, Runnable runnable, PrinterData printerData,
            PrintLayer printLayer) {
        putRequest(new PrintRequest(owner, session, runnable, printerData, printLayer));
    }
}
