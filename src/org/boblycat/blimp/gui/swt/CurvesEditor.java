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
				if (points.length > 0)
					prevY = points[0].y;
				NaturalCubicSpline spline = curvesLayer.getSpline();
				for (int x=1; x<size.x; x++) {
					double nextX = ((double) x) / ((double) size.x);
					double nextY = spline.getSplineValue(nextX);
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
	}

	protected void layerChanged() {
		curvesLayer = (CurvesLayer) layer;
	}
}
