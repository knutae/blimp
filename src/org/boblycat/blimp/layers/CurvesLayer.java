package org.boblycat.blimp.layers;

import java.util.TreeMap;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.NaturalCubicSpline;
import org.boblycat.blimp.PointDouble;
import org.boblycat.blimp.SplineOperation;

import net.sourceforge.jiu.data.PixelImage;

public class CurvesLayer extends AdjustmentLayer {
    NaturalCubicSpline spline;

    public CurvesLayer() {
        spline = new NaturalCubicSpline();
        spline.addPoint(0.0, 0.0);
        spline.addPoint(1.0, 1.0);
    }

    public Bitmap applyLayer(Bitmap source) {
        SplineOperation curvesOp = new SplineOperation();
        curvesOp.setTablesFromSpline(spline, source.getChannelBitDepth());
        PixelImage image = source.getImage();
        image = applyJiuOperation(image, curvesOp);
        return new Bitmap(image);
    }

    public String getDescription() {
        return "Curves";
    }

    void normalizePoints() {
        TreeMap<Double, Double> oldPoints = spline.getPoints();
        TreeMap<Double, Double> newPoints = new TreeMap<Double, Double>();
        for (double x : oldPoints.keySet()) {
            double y = oldPoints.get(x);
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
        spline.setPoints(newPoints);
    }

    public void setPoints(PointDouble[] value) {
        TreeMap<Double, Double> points = spline.getPoints();
        points.clear();
        for (PointDouble p : value) {
            if (!points.containsKey(p.x)) {
                points.put(p.x, p.y);
            }
        }
        normalizePoints();
    }

    public PointDouble[] getPoints() {
        TreeMap<Double, Double> points = spline.getPoints();
        PointDouble[] ret = new PointDouble[points.size()];
        int i = 0;
        for (double x : points.keySet()) {
            ret[i] = new PointDouble(x, points.get(x));
            i++;
        }
        return ret;
    }

    public NaturalCubicSpline getSpline() {
        return spline;
    }
}
