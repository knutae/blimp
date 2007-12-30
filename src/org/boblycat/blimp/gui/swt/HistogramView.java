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

import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Histogram;
import org.boblycat.blimp.RGBChannel;
import org.boblycat.blimp.RGBHistograms;
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
    RGBHistograms histograms;
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
                if (currentBitmap == null && histograms == null) {
                    SwtUtil.fillWhiteRect(e.gc, rect);
                    return;
                }
                // generate image, or get the cached version
                Image image = getHistogramImage(rect.width, rect.height);
                // copy image (flicker free)
                e.gc.drawImage(image, 0, 0);
            }
        });

        addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                SwtUtil.dispose(histogramImage);
            }
        });
    }

    public void setBitmap(Bitmap bitmap) {
        currentBitmap = bitmap;
        histograms = null;
        invalidateHistogramImage();
        canvas.redraw();
    }

    public void setHistograms(RGBHistograms h) {
        histograms = h;
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

    private static boolean isBlackPixel(RGB24Image image, int x, int y) {
        return ((image.getSample(RGBIndex.INDEX_RED, x, y) == 0) &&
                (image.getSample(RGBIndex.INDEX_GREEN, x, y) == 0) &&
                (image.getSample(RGBIndex.INDEX_BLUE, x, y) == 0));
    }

    private static void putGrayPixel(RGB24Image image, int x, int y,
            int intensity) {
        image.putSample(RGBIndex.INDEX_RED, x, y, intensity);
        image.putSample(RGBIndex.INDEX_GREEN, x, y, intensity);
        image.putSample(RGBIndex.INDEX_BLUE, x, y, intensity);
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

        // A JIU image is somewhat easier to work with than an SWT image,
        // but it does involve an extra conversion step...
        RGB24Image image = new MemoryRGB24Image(width, height);
        if (currentBitmap == null && histograms == null) {
            histogramImage = ImageConverter.jiuToSwtImageViaPixels(getDisplay(), image);
            return histogramImage;
        }

        if (histograms == null) {
            assert(currentBitmap != null);
            histograms = new RGBHistograms(currentBitmap);
        }

        int maxEntry = 0;
        for (RGBChannel channel: RGBChannel.COLORS) {
            Histogram histogram = histograms.getHistogram(channel);
            for (int i=0; i<histogram.getMaxValue(); i++) {
                int entry = histogram.getEntry(i);
                if (entry > maxEntry)
                    maxEntry = entry;
            }
        }

        for (RGBChannel channel: RGBChannel.COLORS) {
            Histogram histogram = histograms.getHistogram(channel);
            int jiuChannel = channel.toJiuIndex();
            int[] entries = histogram.scaledEntries(width);
            for (int x=0; x<width; x++) {
                int entry = entries[x];
                for (int y=0; y<height; y++) {
                    if (entry * height >= (height - y) * maxEntry)
                        image.putSample(jiuChannel, x, y, 255);
                }
            }
        }

        // replace black samples with gray
        int blackX = 0;
        int whiteX = width;
        if (blackLevel != null)
            blackX = (int) Math.round(blackLevel * width);
        if (whiteLevel != null)
            whiteX = (int) Math.round(whiteLevel * width);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (isBlackPixel(image, x, y)) {
                    int intensity = 128;
                    if (x < blackX || x >= whiteX)
                        intensity = 64;
                    putGrayPixel(image, x, y, intensity);
                }
            }
        }

        if (centerLevel != null) {
            int centerX = (int) Math.round(centerLevel * width);
            if (centerX >= 0 && centerX < width) {
                for (int y = 0; y < height; y++) {
                    putGrayPixel(image, centerX, y, 64);
                }
            }
        }

        histogramImage = ImageConverter.jiuToSwtImageViaPixels(getDisplay(), image);
        return histogramImage;
    }
}
