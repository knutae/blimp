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
    private BitmapSize size;
    
    public void setSize(BitmapSize size) {
        this.size = size;
    }
    
    public void run() {
        handleSize(size);
    }

    protected abstract void handleSize(BitmapSize size);
}
