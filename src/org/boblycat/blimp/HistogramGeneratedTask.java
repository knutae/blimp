package org.boblycat.blimp;

public abstract class HistogramGeneratedTask implements Runnable {
    Histogram histogram;
    
    public void setHistogram(Histogram histogram) {
        this.histogram = histogram;
    }
    
    public void run() {
        handleHistogram(histogram);
    }
    
    public abstract void handleHistogram(Histogram histogram);
}
