package org.boblycat.blimp;

/**
 * Task used when asynchronously querying the size of a bitmap from the
 * image worker thread.
 * 
 * @see ImageWorkerThread
 * 
 * @author Knut Arild Erstad
 */
public abstract class BitmapSizeGeneratedTask implements Runnable {
    private int width;
    private int height;
    
    public void setSize(BitmapSize size) {
        if (size == null)
            return;
        width = size.width;
        height = size.height;
    }
    
    public void run() {
        handleSize(width, height);
    }

    protected abstract void handleSize(int width, int height);
}
