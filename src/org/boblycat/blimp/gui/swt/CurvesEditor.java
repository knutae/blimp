/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
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

import org.boblycat.blimp.Debug;
import org.boblycat.blimp.jiuops.MathUtil;
import org.boblycat.blimp.jiuops.NaturalCubicSpline;
import org.boblycat.blimp.PointDouble;
import org.boblycat.blimp.RGBChannel;
import org.boblycat.blimp.layers.CurvesLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CurvesEditor extends GridBasedLayerEditor {
    Canvas canvas;
    Combo channelCombo;
    CurvesLayer curvesLayer;
    Double currentPointX;
    Double selectedPointX;
    int mouseX, mouseY;

    private static boolean almostEqual(double d1, double d2) {
        final double EPSILON = 0.03;
        return Math.abs(d1 - d2) < EPSILON;
    }

    private PointDouble canvasToSplinePos(int x, int y) {
        Point size = canvas.getSize();
        return new PointDouble((double) x / (double) size.x,
                (double) (size.y - y) / (double) size.y);
    }

    private void debug(String msg) {
        Debug.print(this, msg);
    }

    public CurvesEditor(Composite parent, int style) {
        super(parent, style);
        mouseX = -1;
        mouseY = -1;
        channelCombo = createEnumCombo("RGB Channel(s):", RGBChannel.class);
        canvas = new Canvas(this, SWT.NO_BACKGROUND) {
            public Point computeSize(int wHint, int hHint, boolean changed) {
                return new Point(200, 200);
            }
        };
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        canvas.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                Rectangle rect = canvas.getClientArea();
                Image tmpImage = new Image(getDisplay(), rect.width,
                        rect.height);
                GC gc = new GC(tmpImage);
                // fill background
                SwtUtil.fillWhiteRect(gc, canvas.getBounds());
                // draw rulers
                final int RULER_COUNT = 4;
                Point size = canvas.getSize();
                Color color = new Color(gc.getDevice(), 190, 190, 190);
                gc.setForeground(color);
                for (int i = 1; i < RULER_COUNT; i++) {
                    int ypos = size.y * i / RULER_COUNT;
                    gc.drawLine(0, ypos, size.x, ypos);
                    int xpos = size.x * i / RULER_COUNT;
                    gc.drawLine(xpos, 0, xpos, size.y);
                }
                color.dispose();
                // draw curves
                color = new Color(gc.getDevice(), 100, 100, 100);
                gc.setForeground(color);
                double prevY = 0;
                PointDouble[] points = curvesLayer.getPoints();
                NaturalCubicSpline spline = curvesLayer.getSpline();
                int iMaxY = size.y - 1;
                if (points.length > 0)
                    prevY = points[0].y;
                int iPrevY = MathUtil.clamp(size.y
                        - (int) (prevY * size.y), 0, iMaxY);
                double[] splineValues = spline
                        .getSplineValues(0.0, 1.0, size.x);
                for (int x = 1; x < size.x; x++) {
                    double nextY = splineValues[x];
                    int iNextY = MathUtil.clamp(size.y
                            - (int) (nextY * size.y), 0, iMaxY);
                    gc.drawLine(x - 1, iPrevY, x, iNextY);
                    prevY = nextY;
                    iPrevY = iNextY;
                }
                color.dispose();
                // draw points
                color = new Color(gc.getDevice(), 0, 0, 0);
                gc.setBackground(color);
                for (int i = 0; i < points.length; i++) {
                    double x = points[i].x;
                    double y = points[i].y;
                    int xpos = (int) (x * size.x);
                    int ypos = size.y - (int) (y * size.y);
                    gc.fillRectangle(xpos - 1, ypos - 1, 3, 3);
                }
                color.dispose();
                // mark the closest point
                if (selectedPointX != null) {
                    double x = selectedPointX;
                    double y = spline.getSplineValue(x);
                    //debug("selected point at " + x + "," + y);
                    int xpos = (int) (x * size.x);
                    int ypos = size.y - (int) (y * size.y);
                    color = new Color(gc.getDevice(), 200, 0, 0);
                    gc.setBackground(color);
                    gc.fillRectangle(xpos - 2, ypos - 2, 5, 5);
                    color.dispose();
                }
                // copy image
                e.gc.drawImage(tmpImage, 0, 0);
                tmpImage.dispose();
                gc.dispose();
            }
        });

        canvas.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event e) {
                //debug("mouse down " + e.x + " " + e.y);
                if (curvesLayer == null)
                    return;
                NaturalCubicSpline spline = curvesLayer.getSpline();
                PointDouble p = canvasToSplinePos(e.x, e.y);
                double closest = spline.findClosestPoint(p.x);
                if (e.button == 1) {
                    // left mouse button: add or move point
                    if (almostEqual(closest, p.x)) {
                        // move existing point
                        spline.movePoint(closest, p.x, p.y);
                        currentPointX = p.x;
                        debug("moved point " + closest + " -> " + p.x);
                    }
                    else {
                        // add new point
                        currentPointX = p.x;
                        spline.addPoint(p.x, p.y);
                        debug("added point " + p.x);
                    }
                    selectedPointX = currentPointX;
                }
                else if (e.button == 3) {
                    // right mouse button: remove point
                    if (almostEqual(closest, p.x)) {
                        spline.removePoint(closest);
                        selectedPointX = null;
                    }
                }
                layer.invalidate();
                canvas.redraw();
            }
        });

        canvas.addListener(SWT.MouseMove, new Listener() {
            public void handleEvent(Event e) {
                if (curvesLayer == null)
                    return;
                mouseX = e.x;
                mouseY = e.y;
                //canvas.redraw();
                NaturalCubicSpline spline = curvesLayer.getSpline();
                PointDouble p = canvasToSplinePos(e.x, e.y);
                if (currentPointX != null) {
                    //debug("mouse move " + e.x + " " + e.y);
                    spline.movePoint(currentPointX, p.x, p.y);
                    currentPointX = p.x;
                    selectedPointX = currentPointX;
                    layer.invalidate();
                    canvas.redraw();
                }
                else {
                    double closest = spline.findClosestPoint(p.x);
                    Double oldSelected = selectedPointX;
                    if (almostEqual(closest, p.x))
                        selectedPointX = closest;
                    else
                        selectedPointX = null;
                    //debug("old " + oldSelected + " new " + selectedPointX);
                    if (selectedPointX != oldSelected)
                        canvas.redraw();
                }
            }
        });

        canvas.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                //debug(this, "mouse up " + e.x + " " + e.y);
                currentPointX = null;
            }
        });
    }

    protected void updateLayer() {
        for (RGBChannel channel: RGBChannel.values()) {
            if (channel.toString().equals(channelCombo.getText())) {
                curvesLayer.setChannel(channel);
                break;
            }
        }
    }

    protected void layerChanged() {
        curvesLayer = (CurvesLayer) layer;
        setEnumComboValue(channelCombo, curvesLayer.getChannel());
    }
}
