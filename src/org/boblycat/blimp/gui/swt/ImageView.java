package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.*;

public class ImageView extends Composite {
    BlimpSession session;
    boolean dirty;
    Runnable redrawTask;
    Canvas canvas;
    Image currentImage;
    int paintCounter;
    CLabel zoomLabel;

    public ImageView(Composite parent, int style, BlimpSession aSession) {
        super(parent, style);

        Listener redrawListener = new Listener() {
            public void handleEvent(Event e) {
                canvas.redraw();
            }
        };

        // Create GUI components
        canvas = new Canvas(this, SWT.NO_BACKGROUND | SWT.H_SCROLL
                | SWT.V_SCROLL);
        canvas.getHorizontalBar().setEnabled(false);
        canvas.getVerticalBar().setEnabled(false);
        canvas.getHorizontalBar().addListener(SWT.Selection, redrawListener);
        canvas.getVerticalBar().addListener(SWT.Selection, redrawListener);

        canvas.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                // System.out.println("paint " + paintCounter);
                paintCounter++;
                updateImage();
                if (currentImage == null) {
                    e.gc.fillRectangle(canvas.getClientArea());
                    return;
                }
                Rectangle clientArea = canvas.getClientArea();
                Image bufferImage = new Image(canvas.getDisplay(),
                        clientArea.width, clientArea.height);
                GC imageGC = new GC(bufferImage);
                imageGC.setBackground(new Color(imageGC.getDevice(), 0, 0, 0));
                imageGC.fillRectangle(bufferImage.getBounds());
                // System.out.println("canvas size: " + canvasSize.x + ","
                // + canvasSize.y);
                Rectangle imageBounds = currentImage.getBounds();
                // System.out.println("image size: "
                // + imageBounds.width + "," + imageBounds.height);
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
                imageGC.dispose();
                e.gc.drawImage(bufferImage, 0, 0);
                bufferImage.dispose(); // important!
            }
        });

        canvas.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) {
                invalidateImage();
            }
        });

        zoomLabel = new CLabel(this, SWT.NONE);
        zoomLabel.setText("100%");

        ToolBar toolBar = new ToolBar(this, SWT.BORDER);
        ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);
        toolItem.setText("Zoom In");
        toolItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (session != null)
                    session.zoomIn();
            }
        });
        toolItem = new ToolItem(toolBar, SWT.NONE);
        toolItem.setText("Zoom Out");
        toolItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (session != null)
                    session.zoomOut();
            }
        });

        // Layout logic
        FormData data = new FormData();
        data.top = new FormAttachment(0);
        data.right = new FormAttachment(100);
        zoomLabel.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(0);
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(zoomLabel);
        toolBar.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(toolBar);
        data.bottom = new FormAttachment(100);
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(100);
        canvas.setLayoutData(data);

        setLayout(new FormLayout());

        // Create session
        if (aSession != null)
            session = aSession;
        else
            session = new BlimpSession();
        session.addChangeListener(new LayerChangeListener() {
            public void handleChange(LayerEvent event) {
                // invalidateImage();
                invalidateWithDelay(100);
            }
        });

        // Runnable used for delayed update
        redrawTask = new Runnable() {
            public void run() {
                if (!canvas.isDisposed())
                    canvas.redraw();
            }
        };
    }

    public BlimpSession getSession() {
        return session;
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
        // bar.setSelection(0);
    }

    private void updateImage() {
        if (!dirty)
            return;
        Rectangle destArea = canvas.getClientArea();
        Bitmap bitmap = session.getSizedBitmap(destArea.width, destArea.height);
        if (bitmap == null || bitmap.getImage() == null)
            return;
        prepareScrollBar(canvas.getHorizontalBar(), destArea.width, bitmap
                .getWidth());
        prepareScrollBar(canvas.getVerticalBar(), destArea.height, bitmap
                .getHeight());
        int zoomPercentage = (int) (session.getCurrentZoom() * 100.0);
        zoomLabel.setText(Integer.toString(zoomPercentage) + "%");
        layout();
        try {
            currentImage = ImageConverter
                    .bitmapToSwtImage(getDisplay(), bitmap);
            dirty = false;
        }
        catch (Exception e) {
            // status("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void invalidateWithDelay(int delay) {
        dirty = true;
        getDisplay().timerExec(delay, redrawTask);
    }

    public void invalidateImage() {
        dirty = true;
        canvas.redraw();
    }
}