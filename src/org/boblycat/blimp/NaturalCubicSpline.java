package org.boblycat.blimp;

import java.util.Iterator;
import java.util.TreeMap;

public class NaturalCubicSpline {
	TreeMap<Double,Double> points;
	
	public NaturalCubicSpline() {
		points = new TreeMap<Double,Double>();
	}
	
	public void addPoint(double x, double y) {
		points.put(x, y);
	}
	
	public void movePoint(double oldX, double newX, double newY) {
		if (!points.containsKey(oldX))
			return;
		points.remove(oldX);
		addPoint(newX, newY);
	}
	
	// linear interpolation for now, fix!
	public double getSplineValue(double x) {
		//System.out.println("getSplineValue " + x);
		if (points.size() == 0)
			return 0;
		if (points.size() == 1)
			return points.values().iterator().next();
		Iterator<Double> xiter = points.keySet().iterator();
		double x1 = xiter.next();
		if (x <= x1)
			return points.get(x1);
		double x2 = x1;
		while (xiter.hasNext()) {
			x2 = xiter.next();
			if (x <= x2) {
				double h = x - x1;
				double y1 = points.get(x1);
				double y2 = points.get(x2);
				//System.out.println("h=" + h + " x=" + x + " x1=" + x1
				//		+ " x2=" + x2 + " y1=" + y1 + " y2=" + y2);
				return y1 + (y2-y1) * h / (x2-x1);
			}
			x1 = x2;
		}
		//System.out.println("fell through");
		return points.get(x2);
	}
}
