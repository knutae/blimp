package org.boblycat.blimp;

import java.util.TreeMap;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

class CurvesOperation extends LookupTableOperation {
	int[] table;
	
	void setSpline(NaturalCubicSpline spline, int bitDepth) {
		assert(bitDepth == 8 || bitDepth == 16);
		int size = 1 << bitDepth;
		table = new int[size];
		for (int i=0; i<size; i++) {
			double x = i / (double) (size-1);
			double y = spline.getSplineValue(x);
			int iy = (int) (y * (size-1));
			if (iy < 0)
				iy = 0;
			else if (iy >= size)
				iy = size-1;
			assert(iy >= 0 && iy < size);
			//System.out.println("" + i + " --> " + intY);
			table[i] = iy;
		}		
	}
	
	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		setTables(table);
		super.process();
	}
}

public class CurvesLayer extends Layer {
	TreeMap<Double,Double> points;
	NaturalCubicSpline spline;
	
	public CurvesLayer() {
		points = new TreeMap<Double, Double>();
		points.put(0.0, 0.0);
		points.put(1.0, 1.0);
	}

	public Bitmap applyLayer(Bitmap source) {
		// TODO: actually implement
		getSpline();
		PixelImage image = source.getImage();
		int bitDepth = image.getBitsPerPixel() / image.getNumChannels();
		CurvesOperation curvesOp = new CurvesOperation();
		curvesOp.setSpline(spline, bitDepth);
		image = applyJiuOperation(image, curvesOp);
		return new Bitmap(image);
	}

	public String getName() {
		return "Curves";
	}
	
	void normalizePoints() {
		spline = null; // invalidate spline
		TreeMap<Double,Double> newPoints = new TreeMap<Double, Double>();
		for (double x: points.keySet()) {
			double y = points.get(x);
			if (x <= 0.0) {
				// no X smaller than zero allowed, biggest value wins
				newPoints.put(0.0, y);
			}
			else if (x >= 1.0) {
				// no X larger than one allowed, smallest value wins
				newPoints.put(1.0, y);
				break;
			}
			else {
				newPoints.put(x, y);
			}
		}
		// need at least two points
		if (newPoints.size() == 0) {
			newPoints.put(0.0, 0.0);
			newPoints.put(1.0, 1.0);
		}
		else if (newPoints.size() == 1) {
			if (newPoints.containsKey(0.0))
				newPoints.put(1.0, 1.0);
			else
				newPoints.put(0.0, 0.0);
		}
		points = newPoints;
	}
	
	public void setPoints(PointDouble[] value) {
		points.clear();
		for (PointDouble p: value) {
			if (!points.containsKey(p.x)) {
				points.put(p.x, p.y);
				//System.out.println("set point " + p.x + "," + p.y);
			}
		}
		normalizePoints();
	}

	public PointDouble[] getPoints() {
		PointDouble[] ret = new PointDouble[points.size()];
		int i = 0;
		for (double x: points.keySet()) {
			ret[i] = new PointDouble(x, points.get(x));
			i++;
		}
		return ret;
	}
	
	public NaturalCubicSpline getSpline() {
		if (spline == null) {
			spline = new NaturalCubicSpline();
			for (double x: points.keySet()) {
				spline.addPoint(x, points.get(x));
			}			
		}
		return spline;
	}
}
