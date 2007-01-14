package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.NaturalCubicSpline;
import org.boblycat.blimp.PointDouble;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.layers.CurvesLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CurvesEditor extends LayerEditor {
    Canvas canvas;

    CurvesLayer curvesLayer;

    Double currentPointX;

    private static boolean almostEqual(double d1, double d2) {
        final double EPSILON = 0.01;
        return Math.abs(d1 - d2) < EPSILON;
    }

    private PointDouble canvasToSplinePos(int x, int y) {
        Point size = canvas.getSize();
        return new PointDouble((double) x / (double) size.x,
                (double) (size.y - y) / (double) size.y);
    }

    public CurvesEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());
        canvas = new Canvas(this, SWT.NO_BACKGROUND) {
            public Point computeSize(int wHint, int hHint, boolean changed) {
                return new Point(200, 200);
            }
        };
        canvas.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                Rectangle rect = canvas.getClientArea();
                Image tmpImage = new Image(getDisplay(), rect.width,
                        rect.height);
                GC gc = new GC(tmpImage);
                // fill background
                gc.setBackground(new Color(gc.getDevice(), 255, 255, 255));
                gc.fillRectangle(canvas.getBounds());
                // draw rulers
                final int RULER_COUNT = 4;
                Point size = canvas.getSize();
                gc.setForeground(new Color(gc.getDevice(), 190, 190, 190));
                for (int i = 1; i < RULER_COUNT; i++) {
                    int ypos = size.y * i / RULER_COUNT;
                    gc.drawLine(0, ypos, size.x, ypos);
                    int xpos = size.x * i / RULER_COUNT;
                    gc.drawLine(xpos, 0, xpos, size.y);
                }
                // draw curves
                gc.setForeground(new Color(gc.getDevice(), 100, 100, 100));
                double prevY = 0;
                PointDouble[] points = curvesLayer.getPoints();
                NaturalCubicSpline spline = curvesLayer.getSpline();
                int iMaxY = size.y - 1;
                if (points.length > 0)
                    prevY = points[0].y;
                int iPrevY = Util.constrainedValue(size.y
                        - (int) (prevY * size.y), 0, iMaxY);
                double[] splineValues = spline
                        .getSplineValues(0.0, 1.0, size.x);
                for (int x = 1; x < size.x; x++) {
                    double nextY = splineValues[x];
                    int iNextY = Util.constrainedValue(size.y
                            - (int) (nextY * size.y), 0, iMaxY);
                    gc.drawLine(x - 1, iPrevY, x, iNextY);
                    prevY = nextY;
                    iPrevY = iNextY;
                }
                // draw points
                gc.setBackground(new Color(gc.getDevice(), 0, 0, 0));
                for (int i = 0; i < points.length; i++) {
                    double x = points[i].x;
                    double y = points[i].y;
                    int xpos = (int) (x * size.x);
                    int ypos = size.y - (int) (y * size.y);
                    gc.fillRectangle(xpos - 1, ypos - 1, 3, 3);
                }
                // copy image
                e.gc.drawImage(tmpImage, 0, 0);
                tmpImage.dispose();
            }
        });

        canvas.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event e) {
                // System.out.println("mouse down " + e.x + " " + e.y);
                if (curvesLayer == null)
                    return;
                NaturalCubicSpline spline = curvesLayer.getSpline();
                PointDouble p = canvasToSplinePos(e.x, e.y);
                double closest = spline.findClosestPoint(p.x);
                if (e.button == 1) {
                    // left mouse button: add or move point
                    if (almostEqual(closest, p.x)) {
                        // move existing point
                        currentPointX = closest;
                        spline.movePoint(closest, p.x, p.y);
                    }
                    else {
                        // add new point
                        currentPointX = p.x;
                        spline.addPoint(p.x, p.y);
                    }
                }
                else if (e.button == 3) {
                    // right mouse button: remove point
                    if (almostEqual(closest, p.x)) {
                        spline.removePoint(closest);
                    }
                }
                layer.invalidate();
                canvas.redraw();
            }
        });

        canvas.addListener(SWT.MouseMove, new Listener() {
            public void handleEvent(Event e) {
                if (currentPointX == null || curvesLayer == null)
                    return;
                // System.out.println("mouse move " + e.x + " " + e.y);
                NaturalCubicSpline spline = curvesLayer.getSpline();
                PointDouble p = canvasToSplinePos(e.x, e.y);
                spline.movePoint(currentPointX, p.x, p.y);
                currentPointX = p.x;
                layer.invalidate();
                canvas.redraw();
            }
        });

        canvas.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                // System.out.println("mouse up " + e.x + " " + e.y);
                currentPointX = null;
            }
        });
    }

    protected void layerChanged() {
        curvesLayer = (CurvesLayer) layer;
    }
}
