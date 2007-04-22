package org.boblycat.blimp;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A worker thread for doing image (layer) processing.
 * Useful for GUI applications. 
 * @author Knut Arild Erstad
 */
public abstract class ImageWorkerThread extends Thread {
    static final boolean debug = false;
    
    enum RequestType {
        GENERATE_BITMAP,
        GENERATE_HISTOGRAM,
        ZOOM_IN,
        ZOOM_OUT,
        QUIT,
    }

    class Request {
        RequestType type;
        Runnable runnable;
        BlimpSession sessionCopy;
        String layerName;
        HistogramGeneratedTask histogramTask;
        int viewWidth;
        int viewHeight;

        Request(RequestType type) {
            this.type = type;
        }

        Request(RequestType type, Runnable runnable) {
            this.type = type;
            this.runnable = runnable;
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
    
    private static void debugPrint(String msg) {
        if (debug)
            System.out.println("worker: " + msg);
    }
    
    /**
     * This function is called *on the worker thread* when a bitmap has been
     * generated.  It is up to subclasses how to handle this, but in general,
     * the bitmap should be converted (if needed), and then the runnable object
     * should be executed on the main/GUI thread.
     * 
     * @param runnable A runnable object.
     * @param bitmap A generated bitmap, which could be <code>null</code>.
     */
    protected abstract void bitmapGenerated(Runnable runnable, Bitmap bitmap);
    
    protected abstract void histogramGenerated(HistogramGeneratedTask task);
    
    protected abstract void progressReported(ProgressEvent event);
    
    protected abstract boolean isFinished();
    
    protected abstract void handleError(Runnable runnable, String errorMessage);
    
    private void processRequest(Request req) {
        assert(Thread.currentThread() == this);

        if (req.sessionCopy != null) {
            session.synchronizeSessionData(req.sessionCopy);
        }
        try {
            switch (req.type) {
            case GENERATE_BITMAP:
                assert(req.runnable != null);
                // Generate the bitmap on this thread.  It should not be transferred
                // to other threads.
                debugPrint("generating bitmap");
                Bitmap bitmap;
                if (req.viewWidth > 0 && req.viewHeight > 0)
                    bitmap = session.getSizedBitmap(req.viewWidth, req.viewHeight);
                else
                    bitmap = session.getBitmap();
                debugPrint("finished generating bitmap");
                bitmapGenerated(req.runnable, bitmap);
                break;
            case GENERATE_HISTOGRAM:
                assert(req.histogramTask != null && req.layerName != null);
                debugPrint("generating histogram for layer " + req.layerName);
                Histogram histogram = session.getHistogramBeforeLayer(req.layerName, true);
                debugPrint("finished generating histogram");
                // Note: the following should work without synchronization problems,
                // because the histogram task is only used by one thread at a time.
                req.histogramTask.setHistogram(histogram);
                histogramGenerated(req.histogramTask);
                break;
            case ZOOM_IN:
                session.zoomIn();
                bitmapGenerated(req.runnable, session.getBitmap());
                break;
            case ZOOM_OUT:
                session.zoomOut();
                bitmapGenerated(req.runnable, session.getBitmap());
                break;
            }
        }
        catch (IOException e) {
            handleError(req.runnable, e.getMessage());
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
                debugPrint("waiting for queue...");
                Request req = requestQueue.take();
                debugPrint("got request from queue: " + req.type.toString());
                if (req.type == RequestType.QUIT)
                    break;
                processRequest(req);
            }
            catch (InterruptedException e) {
                System.err.println("Image worker thread interrupted");
                e.printStackTrace();
                break;
            }
        }
    }
    
    public void cancelRequests() {
        requestQueue.clear();
        // TODO: cancel ongoing operation (if possible)
    }
    
    public void quit() {
        cancelRequests();
        putRequest(new Request(RequestType.QUIT));
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

    public void asyncGenerateSizedBitmap(BlimpSession session, Runnable runnable,
            int width, int height) {
        // the following can happen on any thread
        Request req = new Request(RequestType.GENERATE_BITMAP);
        req.runnable = runnable;
        req.sessionCopy = BlimpSession.createCopy(session);
        req.viewWidth = width;
        req.viewHeight = height;
        putRequest(req);
    }
    
    public void asyncGenerateHistogram(BlimpSession session, String layerName,
            HistogramGeneratedTask task) {
        Request req = new Request(RequestType.GENERATE_HISTOGRAM);
        req.histogramTask = task;
        req.layerName = layerName;
        req.sessionCopy = BlimpSession.createCopy(session);
        putRequest(req);
    }
    
    public void zoomIn(Runnable runnable) {
        putRequest(new Request(RequestType.ZOOM_IN, runnable));
    }

    public void zoomOut(Runnable runnable) {
        putRequest(new Request(RequestType.ZOOM_OUT, runnable));
    }
}
