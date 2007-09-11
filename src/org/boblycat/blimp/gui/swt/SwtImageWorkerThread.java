/*
 * Copyright (C) 2007 Knut Arild Erstad
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

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapUtil;
import org.boblycat.blimp.ImageWorkerThread;
import org.boblycat.blimp.ProgressEvent;
import org.boblycat.blimp.ProgressEventSource;
import org.boblycat.blimp.ProgressListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

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
        Listener disposeListener = new Listener() {
            public void handleEvent(Event e) {
                setFinished(true);
            }
        };
        display.addListener(SWT.Dispose, disposeListener);
        sharedData = new SharedData();
        guiProgressEventSource = new ProgressEventSource();
    }
    
    @Override
    public void quit() {
        setFinished(true);
        super.quit();
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
