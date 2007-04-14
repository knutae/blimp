package org.boblycat.blimp.gui.swt;

import net.sourceforge.jiu.color.analysis.Histogram1DCreator;
import net.sourceforge.jiu.color.data.ArrayHistogram1D;
import net.sourceforge.jiu.color.data.Histogram1D;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.OperationFailedException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
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
    
    public HistogramView(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());
        canvas = new Canvas(this, SWT.NO_BACKGROUND);
        canvas.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                Rectangle rect = canvas.getClientArea();
                if (currentBitmap == null) {
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
    
    private void invalidateHistogramImage() {
        SwtUtil.dispose(histogramImage);
        histogramImage = null;
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
        gc.dispose();
        black.dispose();
        return histogramImage;
    }
    
    private Histogram1D getFullHistogram() {
        if (allChannelsHistogram != null)
            return allChannelsHistogram;
        IntegerImage image = (IntegerImage) currentBitmap.getImage();
        allChannelsHistogram = new ArrayHistogram1D(256);
        for (int channel=0; channel<image.getNumChannels(); channel++) {
            creator.setImage((IntegerImage) currentBitmap.getImage());
            try {
                creator.process();
            }
            catch (OperationFailedException e) {
                Util.err("Failed to create histogram for channel" + channel);
                return null;
            }
            Histogram1D channelHistogram = creator.getHistogram();
            if (channelHistogram.getMaxValue() != allChannelsHistogram
                    .getMaxValue()) {
                Util.err("Size mismatch while creating histogram.");
                return null;
            }
            for (int i=0; i<allChannelsHistogram.getMaxValue(); i++) {
                allChannelsHistogram.setEntry(i, allChannelsHistogram.getEntry(i)
                        + channelHistogram.getEntry(i));
            }
        }
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
