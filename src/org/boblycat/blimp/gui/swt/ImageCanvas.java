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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * A canvas wrapper for displaying and scrolling an image.
 *
 * Zooming (resizing) is not supported directly by this class,
 * and should be done by passing on a resized image.
 *
 * @author Knut Arild Erstad
 */
public class ImageCanvas extends Composite {
    private static final int PROGRESS_REDRAW_DELAY = 500;

    private Canvas canvas;
    private Image currentImage;
    private String currentProgressMessage;
    private boolean dirty;
    private boolean delayedRedrawInProgress;
    private Runnable delayedRedrawTask;

    /**
     * Construct a new image canvas.
     * @param parent
     *      a parent
     * @param style
     *      an SWT style value
     */
    public ImageCanvas(Composite parent, int style) {
        super(parent, style);
        Listener redrawListener = new Listener() {
            public void handleEvent(Event e) {
                canvas.redraw();
            }
        };
        setLayout(new FillLayout());
        canvas = new Canvas(this,
                SWT.NO_BACKGROUND | SWT.V_SCROLL | SWT.H_SCROLL);
        canvas.getHorizontalBar().setEnabled(false);
        canvas.getVerticalBar().setEnabled(false);
        canvas.getHorizontalBar().addListener(SWT.Selection, redrawListener);
        canvas.getVerticalBar().addListener(SWT.Selection, redrawListener);
        canvas.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                paintCanvas(e.gc);
            }
        });
        canvas.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) {
                invalidate();
            }
        });

        addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                SwtUtil.dispose(currentImage);
            }
        });

        delayedRedrawTask = new Runnable() {
            public void run() {
                if (canvas.isDisposed())
                    return;
                delayedRedrawInProgress = false;
                canvas.redraw();
            }
        };
    }

    /**
     * Update the currently displayed image.
     *
     * @param data
     *      the new image data, or <code>null</code> to clear the image.
     */
    public void setImageData(ImageData data) {
        SwtUtil.dispose(currentImage);
        if (data == null)
            currentImage = null;
        else
            currentImage = new Image(getDisplay(), data);
        invalidate();
    }

    /**
     * Set or clear a progress message, which will be displayed after a
     * short delay.
     *
     * @param message
     *      a progress message, or <code>null</code> to clear the message.
     */
    public void setProgressMessage(String message) {
        currentProgressMessage = message;
        delayedRedraw(PROGRESS_REDRAW_DELAY);
    }

    /**
     * Returns the bounds of the canvas client area, which is the current
     * size available for displaying an image.
     *
     * Typical usage for this is to determine a default zoom/size.
     *
     * @return a size rectangle.
     */
    public Rectangle getCanvasClientArea() {
        return canvas.getClientArea();
    }

    private void delayedRedraw(int delayMillisecs) {
        if (delayedRedrawInProgress)
            return;
        delayedRedrawInProgress = true;
        getDisplay().timerExec(delayMillisecs, delayedRedrawTask);
    }

    private void paintCanvas(GC gc) {
        if (currentImage == null) {
            SwtUtil.fillBlackRect(gc, canvas.getClientArea());
            drawProgressMessage(gc);
            return;
        }
        updateImageParams();
        Rectangle clientArea = canvas.getClientArea();
        Image bufferImage = new Image(canvas.getDisplay(),
                clientArea.width, clientArea.height);
        GC imageGC = new GC(bufferImage);
        SwtUtil.fillBlackRect(imageGC, bufferImage.getBounds());
        Rectangle imageBounds = currentImage.getBounds();
        int x, y;
        if (canvas.getHorizontalBar().isEnabled())
            x = -canvas.getHorizontalBar().getSelection();
        else
            x = (clientArea.width - imageBounds.width) / 2;
        if (canvas.getVerticalBar().isEnabled())
            y = -canvas.getVerticalBar().getSelection();
        else
            y = (clientArea.height - imageBounds.height) / 2;
        imageGC.drawImage(currentImage, x, y);
        drawProgressMessage(imageGC);
        imageGC.dispose();
        gc.drawImage(bufferImage, 0, 0);
        bufferImage.dispose(); // important!
    }

    private void drawProgressMessage(GC gc) {
        if (currentProgressMessage == null)
            return;
        Color color = new Color(gc.getDevice(), 255, 255, 255);
        gc.setForeground(color);
        gc.drawText("Processing: " + currentProgressMessage, 10, 10);
        color.dispose();
    }

    private static void prepareScrollBar(ScrollBar bar, int canvasPixels,
            int bitmapPixels) {
        int range = bitmapPixels - canvasPixels;
        boolean enabled = range > 0;
        bar.setEnabled(enabled);
        assert (enabled == bar.isEnabled());
        if (!enabled)
            return;
        bar.setMinimum(0);
        bar.setMaximum(range);
    }

    private void updateImageParams() {
        if (!dirty || currentImage == null)
            return;
        Rectangle destArea = canvas.getClientArea();
        prepareScrollBar(canvas.getHorizontalBar(), destArea.width,
                currentImage.getBounds().width);
        prepareScrollBar(canvas.getVerticalBar(), destArea.height,
                currentImage.getBounds().height);
        //layout();
        dirty = false;
    }

    private void invalidate() {
        dirty = true;
        canvas.redraw();
    }
}
