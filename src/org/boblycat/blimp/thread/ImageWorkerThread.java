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
package org.boblycat.blimp.thread;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.ProgressEvent;
import org.boblycat.blimp.ProgressListener;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.session.BlimpSession;
import org.boblycat.blimp.session.CachedBlimpSession;

/**
 * A worker thread for doing image (layer) processing.
 * Useful for GUI applications.
 * @author Knut Arild Erstad
 */
public abstract class ImageWorkerThread extends Thread {
    public interface FileExportTask {
        void handleSuccess(File filename);
        void handleError(File filename, String errorMessage);
    }

    private BlockingQueue<Request> requestQueue;
    private BlimpSession session;

    public ImageWorkerThread() {
        super("Blimp Image Worker");
        requestQueue = new LinkedBlockingQueue<Request>();
        //session = new BlimpSession();
        session = new CachedBlimpSession();
        session.addProgressListener(new ProgressListener() {
            public void reportProgress(ProgressEvent event) {
                progressReported(event);
            }
        });
    }

    /**
     * This function is called <i>on the worker thread</i> when a bitmap has been
     * generated.  It is up to subclasses how to handle this, but in general,
     * the bitmap should be converted (if needed), and then the runnable object
     * should be executed on the main/GUI thread.
     *
     * @param runnable A runnable object.
     * @param bitmap A generated bitmap, which could be <code>null</code>.
     */
    protected abstract void bitmapGenerated(Runnable runnable, Bitmap bitmap);

    public abstract void asyncExec(Runnable runnable);

    protected abstract void progressReported(ProgressEvent event);

    protected abstract boolean isFinished();

    protected abstract void handleError(Runnable runnable, String errorMessage);

    private void processRequest(Request req) {
        assert(Thread.currentThread() == this);

        if (req.sessionCopy != null) {
            session.synchronizeSessionData(req.sessionCopy, false);
        }
        String outOfMemoryMessage = String.format(
                "An out of memory error occured while processing %s.\n" +
                "Please close some open images to free more space.",
                session.getName());
        try {
            req.execute();
        }
        catch (IOException e) {
            handleError(req.runnable, e.getMessage());
        }
        catch (OutOfMemoryError e) {
            // While there is no guarantee that recovering from an out-of-memory
            // error will succeed, but the following attempt does no harm,
            // at least.
            handleError(req.runnable, outOfMemoryMessage);
            cancelAllRequests();
            quit();
        }
        catch (Exception e) {
            // should never happen?
            e.printStackTrace(System.err);
            handleError(req.runnable, "Unexpected error on image thread: "
                    + e.getMessage());
        }
        req.dispose();
    }

    @Override
    public void run() {
        while (!isFinished()) {
            try {
                Request req = requestQueue.take();
                if (req instanceof QuitRequest)
                    break;
                processRequest(req);
            }
            catch (InterruptedException e) {
                Util.err("Image worker thread interrupted");
                e.printStackTrace();
                break;
            }
        }
        session = null;
        System.gc();
    }

    private void cancelAllRequests() {
        List<Request> tmp = new ArrayList<Request>();
        requestQueue.drainTo(tmp);
        for (Request req: tmp) {
            req.dispose();
        }
        // TODO: cancel ongoing operation (if possible)
    }

    /**
     * Cancel owned requests.
     * @param owner an owner.
     * @return The number of requests cancelled.
     */
    public int cancelRequestsByOwner(Object owner) {
        List<Request> tmp = new ArrayList<Request>();
        requestQueue.drainTo(tmp);
        int count = 0;
        for (Request req: tmp) {
            if (req.owner != owner)
                putRequest(req);
            else {
                count++;
                req.dispose();
            }
        }
        return count;
    }

    public void quit() {
        putRequest(new QuitRequest(this));
    }

    protected void putRequest(Request req) {
        try {
            requestQueue.put(req);
        }
        catch (InterruptedException e) {
            // should never happen since the capacity is large ...?
            e.printStackTrace();
            assert(false);
        }
    }

    public void asyncGenerateSizedBitmap(Object owner, BlimpSession session,
            Runnable runnable, int width, int height,
            BlimpSession.PreviewQuality quality) {
        // the following can happen on any thread
        putRequest(new BitmapRequest(this, owner, session, runnable, width, height, quality));
    }

    public void asyncGenerateHistogram(Object owner, BlimpSession session,
            String layerName, HistogramGeneratedTask task) {
        putRequest(new HistogramRequest(this, owner, session, task, layerName));
    }

    public void asyncGenerateBitmapSize(Object owner, BlimpSession session,
            String layerName, BitmapSizeGeneratedTask task) {
        putRequest(new SizeRequest(this, owner, session, task, layerName));
    }

    public void asyncExportBitmap(Object owner, BlimpSession session,
            File filePath, double quality, FileExportTask task) {
        putRequest(new ExportBitmapRequest(this, owner, session, task, filePath, quality));
    }

    public void zoomIn(Object owner, BlimpSession session, Runnable runnable) {
        putRequest(new ZoomInRequest(this, owner, session, runnable));
    }

    public void zoomOut(Object owner, BlimpSession session, Runnable runnable) {
        putRequest(new ZoomOutRequest(this, owner, session, runnable));
    }

    public void getExifData(Object owner, BlimpSession session,
            ExifQueryTask task) {
        putRequest(new ExifRequest(this, owner, session, task));
    }

    public BlimpSession getSession() {
        return session;
    }
}
