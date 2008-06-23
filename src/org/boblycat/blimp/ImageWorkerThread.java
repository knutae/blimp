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
package org.boblycat.blimp;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    enum RequestType {
        GENERATE_BITMAP,
        GENERATE_HISTOGRAMS,
        GENERATE_SIZE,
        QUERY_EXIF_DATA,
        ZOOM_IN,
        ZOOM_OUT,
        EXPORT_BITMAP,
        QUIT,
    }

    class Request {
        Object owner;
        RequestType type;
        Runnable runnable;
        BlimpSession sessionCopy;
        String layerName;
        HistogramGeneratedTask histogramTask;
        BitmapSizeGeneratedTask sizeTask;
        ExifQueryTask exifTask;
        int viewWidth;
        int viewHeight;
        BlimpSession.PreviewQuality previewQuality;
        File exportFile;
        double exportQuality;
        FileExportTask exportTask;

        Request(Object owner, RequestType type) {
            this.owner = owner;
            this.type = type;
        }

        Request(Object owner, RequestType type, BlimpSession session) {
            this(owner, type);
            this.sessionCopy = BlimpSession.createCopy(session);
        }

        Request(Object owner, RequestType type, BlimpSession session,
                Runnable runnable) {
            this(owner, type, session);
            this.runnable = runnable;
        }
    }

    private class FileExportSuccess implements Runnable {
        FileExportTask task;
        File filename;
        FileExportSuccess(FileExportTask task, File filename) {
            this.task = task;
            this.filename = filename;
        }
        public void run() {
            task.handleSuccess(filename);
        }
    }

    private class FileExportFailure implements Runnable {
        FileExportTask task;
        File filename;
        String errorMessage;
        FileExportFailure(FileExportTask task, File filename, String errorMessage) {
            this.task = task;
            this.filename = filename;
            this.errorMessage = errorMessage;
        }
        public void run() {
            task.handleError(filename, errorMessage);
        }
    }

    BlockingQueue<Request> requestQueue;

    protected BlimpSession session;

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

    protected abstract void asyncExec(Runnable runnable);

    protected abstract void progressReported(ProgressEvent event);

    protected abstract boolean isFinished();

    protected abstract void handleError(Runnable runnable, String errorMessage);

    private void handleExceptionError(Request req, String message) {
        if (req.exportTask != null) {
            assert(req.exportFile != null);
            asyncExec(new FileExportFailure(req.exportTask, req.exportFile, message));
        }
        else {
            assert(req.runnable != null);
            handleError(req.runnable, message);
        }
    }

    private void processRequest(Request req) {
        assert(Thread.currentThread() == this);

        if (req.sessionCopy != null) {
            session.synchronizeSessionData(req.sessionCopy);
        }
        String outOfMemoryMessage = String.format(
                "An out of memory error occured while processing %s.\n" +
                "Please close some open images to free more space.",
                session.getName());
        try {
            Bitmap bitmap = null;
            switch (req.type) {
            case GENERATE_BITMAP:
                assert(req.runnable != null);
                // Generate the bitmap on this thread.  It should not be transferred
                // to other threads.
                Debug.print(this, "generating bitmap");
                if (req.viewWidth > 0 && req.viewHeight > 0)
                    bitmap = session.getSizedBitmap(req.viewWidth, req.viewHeight,
                            req.previewQuality);
                else
                    bitmap = session.getBitmap();
                Debug.print(this, "finished generating bitmap");
                bitmapGenerated(req.runnable, bitmap);
                break;
            case GENERATE_HISTOGRAMS:
                assert(req.histogramTask != null && req.layerName != null);
                Debug.print(this, "generating histogram for layer " + req.layerName);
                RGBHistograms histograms = session.getHistogramsBeforeLayer(req.layerName, true);
                Debug.print(this, "finished generating histogram");
                // Note: the following should work without synchronization problems,
                // because the histogram task is only used by one thread at a time.
                req.histogramTask.setHistograms(histograms);
                asyncExec(req.histogramTask);
                break;
            case GENERATE_SIZE:
                assert(req.sizeTask != null && req.layerName != null);
                Debug.print(this, "generating size for layer " + req.layerName);
                BitmapSize size = session.getBitmapSizeBeforeLayer(req.layerName);
                Debug.print(this, "finished generating size");
                if (size == null)
                    Util.err("Failed to get size for layer " + req.layerName);
                req.sizeTask.setSize(size);
                asyncExec(req.sizeTask);
                break;
            case ZOOM_IN:
                session.zoomIn();
                bitmapGenerated(req.runnable, session.getBitmap());
                break;
            case ZOOM_OUT:
                session.zoomOut();
                bitmapGenerated(req.runnable, session.getBitmap());
                break;
            case EXPORT_BITMAP:
                assert(req.exportFile != null);
                assert(req.exportTask != null);
                Debug.print(this, "exporting bitmap to " + req.exportFile);
                bitmap = session.getFullBitmap();
                Debug.print(this, "finished generating full bitmap for export");
                String ext = Util.getFileExtension(req.exportFile);
                BitmapUtil.writeBitmap(bitmap, ext, req.exportFile, req.exportQuality);
                Debug.print(this, "finished writing bitmap");
                asyncExec(new FileExportSuccess(req.exportTask, req.exportFile));
                break;
            case QUERY_EXIF_DATA:
                assert(req.exifTask != null);
                bitmap = session.getBitmap();
                if (bitmap == null || bitmap.getExifTable() == null)
                    req.exifTask.data = null;
                else
                    req.exifTask.data = BitmapUtil.copyInterestingExifData(bitmap.getExifTable());
                asyncExec(req.exifTask);
                break;
            }
        }
        catch (IOException e) {
            handleExceptionError(req, e.getMessage());
        }
        catch (OutOfMemoryError e) {
            // While there is no guarantee that recovering from an out-of-memory
            // error will succeed, but the following attempt does no harm,
            // at least.
            handleExceptionError(req, outOfMemoryMessage);
            quit();
        }
        catch (Exception e) {
            // should never happen?
            e.printStackTrace(System.err);
            handleExceptionError(req, "Unexpected error on image thread: "
                    + e.getMessage());
        }
    }

    public void run() {
        while (!isFinished()) {
            try {
                Debug.print(this, "waiting for queue...");
                Request req = requestQueue.take();
                Debug.print(this, "got request from queue: " + req.type.toString());
                if (req.type == RequestType.QUIT)
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
        requestQueue.clear();
        // TODO: cancel ongoing operation (if possible)
    }

    /**
     * Cancel owned requests.
     * @param owner an owner.
     * @return The number of requests cancelled.
     */
    public int cancelRequestsByOwner(Object owner) {
        Vector<Request> tmp = new Vector<Request>();
        requestQueue.drainTo(tmp);
        int count = 0;
        for (Request req: tmp) {
            if (req.owner != owner)
                putRequest(req);
            else
                count++;
        }
        Debug.print(this, "cancelled " + count + " request(s)");
        return count;
    }

    public void quit() {
        cancelAllRequests();
        putRequest(new Request(this, RequestType.QUIT));
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
        Request req = new Request(owner, RequestType.GENERATE_BITMAP, session);
        req.runnable = runnable;
        req.viewWidth = width;
        req.viewHeight = height;
        req.previewQuality = quality;
        putRequest(req);
    }

    public void asyncGenerateHistogram(Object owner, BlimpSession session,
            String layerName, HistogramGeneratedTask task) {
        Request req = new Request(owner, RequestType.GENERATE_HISTOGRAMS, session);
        req.histogramTask = task;
        req.layerName = layerName;
        putRequest(req);
    }

    public void asyncGenerateBitmapSize(Object owner, BlimpSession session,
            String layerName, BitmapSizeGeneratedTask task) {
        Request req = new Request(owner, RequestType.GENERATE_SIZE, session);
        req.sizeTask = task;
        req.layerName = layerName;
        putRequest(req);
    }

    public void asyncExportBitmap(Object owner, BlimpSession session,
            File filePath, double quality, FileExportTask task) {
        Request req = new Request(owner, RequestType.EXPORT_BITMAP, session);
        req.exportTask = task;
        req.exportFile = filePath;
        req.exportQuality = quality;
        putRequest(req);
    }

    public void zoomIn(Object owner, BlimpSession session, Runnable runnable) {
        Request req = new Request(owner, RequestType.ZOOM_IN, session, runnable);
        putRequest(req);
    }

    public void zoomOut(Object owner, BlimpSession session, Runnable runnable) {
        Request req = new Request(owner, RequestType.ZOOM_OUT, session, runnable);
        putRequest(req);
    }

    public void getExifData(Object owner, BlimpSession session,
            ExifQueryTask task) {
        Request req = new Request(owner, RequestType.QUERY_EXIF_DATA, session);
        req.exifTask = task;
        putRequest(req);
    }
}
