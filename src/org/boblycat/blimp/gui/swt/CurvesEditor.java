package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.CurvesLayer;
import org.boblycat.blimp.NaturalCubicSpline;
import org.boblycat.blimp.PointDouble;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CurvesEditor extends LayerEditor {
	Canvas canvas;
	CurvesLayer curvesLayer;
	Double currentPointX;
	
	/*
	private Point splineToCanvasCoord(PointDouble splineCoord) {
		Point size = canvas.getSize();
		return new Point(
				(int) splineCoord.x * size.x,
				(int) splineCoord.y * size.y);
	}
	*/

    private static boolean almostEqual(double d1, double d2) {
    	final double EPSILON = 0.01;
    	return Math.abs(d1-d2) < EPSILON;
    }
    
	private PointDouble canvasToSplinePos(int x, int y) {
		Point size = canvas.getSize();
		return new PointDouble(
				(double) x / (double) size.x,
				(double) (size.y - y) / (double) size.y);
	}
	
	public CurvesEditor(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		canvas = new Canvas(this, SWT.NONE) {
			public Point computeSize(int wHint, int hHint, boolean changed) {
				return new Point(200, 200);
			}
		};
		canvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				GC gc = e.gc;
				// fill background
				gc.setBackground(new Color(gc.getDevice(), 255, 255, 255));
				gc.fillRectangle(canvas.getBounds());
				// draw rulers
				final int RULER_COUNT = 10;
				Point size = canvas.getSize();
				gc.setForeground(new Color(gc.getDevice(), 190, 190, 190));
				for (int i=0; i<RULER_COUNT; i++) {
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
				if (points.length > 0)
					prevY = points[0].y;
				/*
				for (int x=1; x<size.x; x++) {
					double nextX = ((double) x) / ((double) size.x);
					double nextY = spline.getSplineValue(nextX);
					int iPrevY = size.y - (int) (prevY * size.y);
					int iNextY = size.y - (int) (nextY * size.y);
					gc.drawLine(x-1, iPrevY, x, iNextY);
					prevY = nextY;
				}
				*/
				double[] splineValues = spline.getSplineValues(0.0, 1.0, size.x);
				for (int x=1; x<size.x; x++) {
					//double nextX = ((double) x) / ((double) size.x);
					double nextY = splineValues[x];
					int iPrevY = size.y - (int) (prevY * size.y);
					int iNextY = size.y - (int) (nextY * size.y);
					gc.drawLine(x-1, iPrevY, x, iNextY);
					prevY = nextY;
				}
				// draw points
				gc.setBackground(new Color(gc.getDevice(), 0, 0, 0));
				for (int i=0; i<points.length; i++) {
					double x = points[i].x;
					double y = points[i].y;
					int xpos = (int) (x * size.x);
					int ypos = size.y - (int) (y * size.y);
					gc.fillRectangle(xpos-1, ypos-1, 3, 3);
				}
			}
		});
		
		canvas.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				System.out.println("mouse down " + e.x + " " + e.y);
				if (curvesLayer == null)
					return;
				NaturalCubicSpline spline = curvesLayer.getSpline();
				PointDouble p = canvasToSplinePos(e.x, e.y);
				double closest = spline.findClosestPoint(p.x);
				if (e.button == 1) {
					// left mouse button: add or remove point
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
				System.out.println("mouse move " + e.x + " " + e.y);
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
				System.out.println("mouse up " + e.x + " " + e.y);
				currentPointX = null;
			}
		});
	}

	protected void layerChanged() {
		curvesLayer = (CurvesLayer) layer;
	}
}
