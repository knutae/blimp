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
package org.boblycat.blimp.layers;

import java.util.TreeMap;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.PointDouble;
import org.boblycat.blimp.RGBChannel;
import org.boblycat.blimp.jiuops.SplineOperation;
import org.boblycat.blimp.util.NaturalCubicSpline;

import net.sourceforge.jiu.data.PixelImage;

public class CurvesLayer extends AdjustmentLayer {
    NaturalCubicSpline spline;
    RGBChannel channel;

    public CurvesLayer() {
        spline = new NaturalCubicSpline();
        spline.addPoint(0.0, 0.0);
        spline.addPoint(1.0, 1.0);
        channel = RGBChannel.All;
    }

    public Bitmap applyLayer(Bitmap source) {
        SplineOperation curvesOp = new SplineOperation();
        curvesOp.setSpline(spline);
        curvesOp.setChannel(channel.toJiuIndex());
        PixelImage image = source.getImage();
        image = applyJiuOperation(image, curvesOp);
        return new Bitmap(image);
    }

    public String getDescription() {
        switch (channel) {
        case All:
            return "Curves";
        default:
            return "Curves (" + channel + ")";
        }
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

    /**
     * @param channel the color channel to set
     */
    public void setChannel(RGBChannel channel) {
        if (channel != null)
            this.channel = channel;
    }

    /**
     * @return the color channel
     */
    public RGBChannel getChannel() {
        return channel;
    }
}
