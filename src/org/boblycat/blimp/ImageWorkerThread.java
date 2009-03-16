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
package org.boblycat.blimp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.boblycat.blimp.BlimpSession.PreviewQuality;

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

    protected abstract class Request {
        Object owner;
        Runnable runnable;
        BlimpSession sessionCopy;

        Request(Object owner, BlimpSession session, Runnable runnable) {
            this.owner = owner;
            this.runnable = runnable;
            if (session != null)
                this.sessionCopy = BlimpSession.createCopy(session);
        }
        
        protected abstract void execute() throws IOException;
    }
    
    private class BitmapRequest extends Request {
        private int viewWidth;
        private int viewHeight;
        private PreviewQuality previewQuality;
        
        BitmapRequest(Object owner, BlimpSession session, Runnable runnable,
                int viewWidth, int viewHeight, PreviewQuality previewQuality) {
            super(owner, session, runnable);
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;
            this.previewQuality = previewQuality;
        }
        
        protected void execute() throws IOException {
            assert(runnable != null);
            // Generate the bitmap on this thread.  It should not be transferred
            // to other threads.
            Bitmap bitmap;
            Debug.print(this, "generating bitmap");
            if (viewWidth > 0 && viewHeight > 0)
                bitmap = session.getSizedBitmap(viewWidth, viewHeight, previewQuality);
            else
                bitmap = session.getBitmap();
            Debug.print(this, "finished generating bitmap");
            bitmapGenerated(runnable, bitmap);
        }
    }
    
    private class HistogramRequest extends Request {
        private HistogramGeneratedTask histogramTask;
        private String layerName;
        
        HistogramRequest(Object owner, BlimpSession session,
                HistogramGeneratedTask task, String layerName) {
            super(owner, session, task);
            this.histogramTask = task;
            this.layerName = layerName;
        }
        
        protected void execute() throws IOException {
            assert(histogramTask != null && layerName != null);
            Debug.print(this, "generating histogram for layer " + layerName);
            RGBHistograms histograms = session.getHistogramsBeforeLayer(layerName, true);
            Debug.print(this, "finished generating histogram");
            // Note: the following should work without synchronization problems,
            // because the histogram task is only used by one thread at a time.
            histogramTask.setHistograms(histograms);
            asyncExec(histogramTask);
        }
    }
    
    private class SizeRequest extends Request {
        private BitmapSizeGeneratedTask sizeTask;
        private String layerName;
        
        SizeRequest(Object owner, BlimpSession session,
                BitmapSizeGeneratedTask task, String layerName) {
            super(owner, session, task);
            this.sizeTask = task;
            this.layerName = layerName;
        }
        
        protected void execute() throws IOException {
            assert(sizeTask != null && layerName != null);
            Debug.print(this, "generating size for layer " + layerName);
            BitmapSize size = session.getBitmapSizeBeforeLayer(layerName);
            Debug.print(this, "finished generating size");
            if (size == null)
                Util.err("Failed to get size for layer " + layerName);
            sizeTask.setSize(size);
            asyncExec(sizeTask);
        }
    }

    private class ZoomInRequest extends Request {
        ZoomInRequest(Object owner, BlimpSession session, Runnable runnable) {
            super(owner, session, runnable);
        }
        
        protected void execute() throws IOException {
            session.zoomIn();
            bitmapGenerated(runnable, session.getBitmap());
        }
    }

    private class ZoomOutRequest extends Request {
        ZoomOutRequest(Object owner, BlimpSession session, Runnable runnable) {
            super(owner, session, runnable);
        }
        
        protected void execute() throws IOException {
            session.zoomOut();
            bitmapGenerated(runnable, session.getBitmap());
        }
    }
    
    private class ExportBitmapRequest extends Request {
        private File file;
        private double exportQuality;
        private FileExportTask exportTask;
        private String errorMessage;
        
        ExportBitmapRequest(Object owner, BlimpSession session,
                FileExportTask task, File file, double quality) {
            // runnable can be null because IOException is handled internally
            super(owner, session, null);
            this.file = file;
            this.exportQuality = quality;
            this.exportTask = task;
        }

        protected void execute() throws IOException {
            assert(file != null);
            assert(exportTask != null);
            try {
                Debug.print(this, "exporting bitmap to " + file);
                Bitmap bitmap = session.getFullBitmap();
                Debug.print(this, "finished generating full bitmap for export");
                String ext = Util.getFileExtension(file);
                BitmapUtil.writeBitmap(bitmap, ext, file, exportQuality);
                Debug.print(this, "finished writing bitmap");
                asyncExec(new Runnable() {
                    public void run() {
                        exportTask.handleSuccess(file);
                    }
                });
            }
            catch (IOException e) {
                // special handling of IOException during export
                errorMessage = e.getMessage();
                asyncExec(new Runnable() {
                    public void run() {
                        exportTask.handleError(file, errorMessage);
                    }
                });
            }
        }
    }
    
    private class ExifRequest extends Request {
        private ExifQueryTask exifTask;
        
        ExifRequest(Object owner, BlimpSession session, ExifQueryTask task) {
            super(owner, session, task);
            this.exifTask = task;
        }
        
        protected void execute() throws IOException {
            assert(exifTask != null);
            exifTask.data = session.getInterestingExifData();
            asyncExec(exifTask);
        }
    }
    
    private class QuitRequest extends Request {
        QuitRequest(Object owner) {
            super(owner, null, null);
        }
        
        protected void execute() {
            assert(false); // unreachable
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
    }

    public void run() {
        while (!isFinished()) {
            try {
                Debug.print(this, "waiting for queue...");
                Request req = requestQueue.take();
                Debug.print(this, "got request from queue: " + req.getClass().getSimpleName());
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
        requestQueue.clear();
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
            else
                count++;
        }
        Debug.print(this, "cancelled " + count + " request(s)");
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
        putRequest(new BitmapRequest(owner, session, runnable, width, height, quality));
    }

    public void asyncGenerateHistogram(Object owner, BlimpSession session,
            String layerName, HistogramGeneratedTask task) {
        putRequest(new HistogramRequest(owner, session, task, layerName));
    }

    public void asyncGenerateBitmapSize(Object owner, BlimpSession session,
            String layerName, BitmapSizeGeneratedTask task) {
        putRequest(new SizeRequest(owner, session, task, layerName));
    }

    public void asyncExportBitmap(Object owner, BlimpSession session,
            File filePath, double quality, FileExportTask task) {
        putRequest(new ExportBitmapRequest(owner, session, task, filePath, quality));
    }

    public void zoomIn(Object owner, BlimpSession session, Runnable runnable) {
        putRequest(new ZoomInRequest(owner, session, runnable));
    }

    public void zoomOut(Object owner, BlimpSession session, Runnable runnable) {
        putRequest(new ZoomOutRequest(owner, session, runnable));
    }

    public void getExifData(Object owner, BlimpSession session,
            ExifQueryTask task) {
        putRequest(new ExifRequest(owner, session, task));
    }
}
