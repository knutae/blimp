package org.boblycat.blimp.gui.swt;

import net.sourceforge.jiu.color.analysis.Histogram1DCreator;
import net.sourceforge.jiu.color.data.Histogram1D;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Histogram;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class HistogramView extends Composite {
    Canvas canvas;
    Bitmap currentBitmap;
    Histogram1DCreator creator;
    Histogram1D allChannelsHistogram;
    Image histogramImage;
    Double blackLevel;
    Double centerLevel;
    Double whiteLevel;
    
    public HistogramView(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());
        canvas = new Canvas(this, SWT.NO_BACKGROUND);
        canvas.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                Rectangle rect = canvas.getClientArea();
                if (currentBitmap == null && allChannelsHistogram == null) {
                    SwtUtil.fillWhiteRect(e.gc, rect);
                    return;
                }
                // generate image, or get the cached version
                Image image = getHistogramImage(rect.width, rect.height);
                // copy image (flicker free)
                e.gc.drawImage(image, 0, 0);
            }
        });
        creator = new Histogram1DCreator();
        
        addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                SwtUtil.dispose(histogramImage);
            }
        });
    }
    
    public void setBitmap(Bitmap bitmap) {
        currentBitmap = bitmap;
        allChannelsHistogram = null;
        invalidateHistogramImage();
        canvas.redraw();
    }
    
    public void setAllchannelsHistogram(Histogram1D histogram) {
        allChannelsHistogram = histogram;
        invalidateHistogramImage();
        canvas.redraw();
    }
    
    public void setLevels(double black, double center, double white) {
        blackLevel = black;
        centerLevel = center;
        whiteLevel = white;
        invalidateHistogramImage();
        canvas.redraw();
    }
    
    private void invalidateHistogramImage() {
        SwtUtil.dispose(histogramImage);
        histogramImage = null;
    }
    
    private static void fillGrayRectange(GC gc, int width, int height,
            double startPercentage, double endPercentage) {
        int x1 = (int) (startPercentage * width);
        int x2 = (int) (endPercentage * width);
        int intensity = 128;
        SwtUtil.fillColorRect(gc, new Rectangle(x1, 0, x2, height),
                intensity, intensity, intensity);
    }
    
    private Image getHistogramImage(int width, int height) {
        if (histogramImage != null) {
            // invalidate if the size has changed
            Rectangle bounds = histogramImage.getBounds();
            if (bounds.width != width || bounds.height != height)
                invalidateHistogramImage();
        }
        if (histogramImage != null)
            // return cached image
            return histogramImage;
        histogramImage = new Image(getDisplay(), width, height);
        GC gc = new GC(histogramImage);
        SwtUtil.fillWhiteRect(gc, new Rectangle(0, 0, width, height));
        if (blackLevel != null)
            fillGrayRectange(gc, width, height, 0.0, blackLevel);
        if (whiteLevel != null)
            fillGrayRectange(gc, width, height, whiteLevel, 1.0);
        Color black = new Color(gc.getDevice(), 0, 0, 0);
        gc.setForeground(black);
        Histogram1D histogram = getFullHistogram();
        if (histogram != null) {
            Path path = new Path(gc.getDevice());
            generateHistogramPath(histogram, path, width, height);
            gc.setBackground(black);
            gc.fillPath(path);
            path.dispose();
        }
        if (centerLevel != null) {
            int x = (int) (centerLevel * width);
            SwtUtil.drawColorLine(gc, x, 0, x, height-1, 100, 100, 255);
        }
        gc.dispose();
        black.dispose();
        return histogramImage;
    }
    
    private Histogram1D getFullHistogram() {
        if (allChannelsHistogram != null)
            return allChannelsHistogram;
        allChannelsHistogram = new Histogram(currentBitmap);
        return allChannelsHistogram;
    }
    
    private static void generateHistogramPath(Histogram1D histogram, Path path,
            int width, int height) {
        float sourceWidth = histogram.getMaxValue();
        int ymax = 0;
        for (int i=0; i<=histogram.getMaxValue(); i++)
            ymax = Math.max(histogram.getEntry(i), ymax);
        float sourceHeight = ymax;
        
        path.moveTo(0, height);
        for (int x=0; x<histogram.getMaxValue(); x++) {
            float destx = (x * width) / sourceWidth;
            float desty = (histogram.getEntry(x) * height) / sourceHeight;
            path.lineTo(destx, height - desty);
        }
        path.lineTo(width, height);
        path.close();
    }
}
