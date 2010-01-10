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

import net.sourceforge.jiu.color.adjustment.Curves;
import net.sourceforge.jiu.util.NaturalCubicSpline;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.util.MathUtil;

class LevelsOperation extends Curves {
    void setValues(double blackLevel, double center, double whiteLevel) {
        NaturalCubicSpline spline = new NaturalCubicSpline();
        // point 1: (black, 0)
        // point 2: (center, 0.5)
        // point 3: (white, 1)
        spline.addPoint(blackLevel, 0);
        // ignore the center value if it is not between black and white
        if (center > blackLevel && center < whiteLevel)
            spline.addPoint(center, 0.5);
        spline.addPoint(whiteLevel, 1);
        setSpline(spline);
    }
}

public class LevelsLayer extends AdjustmentLayer {
    private double blackLevel;
    private double center;
    private double whiteLevel;

    public LevelsLayer() {
        blackLevel = 0;
        center = 0.5;
        whiteLevel = 1;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        LevelsOperation op = new LevelsOperation();
        op.setValues(blackLevel, center, whiteLevel);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Levels";
    }

    public void setBlackLevel(double blackLevel) {
        this.blackLevel = MathUtil.clamp(blackLevel, 0, 1);
    }

    public double getBlackLevel() {
        return blackLevel;
    }

    public void setCenter(double center) {
        if (center <= 0 || center >= 1)
            return;
        this.center = center;
    }

    public double getCenter() {
        return center;
    }

    public void setWhiteLevel(double whiteLevel) {
        this.whiteLevel = MathUtil.clamp(whiteLevel, 0, 1);
    }

    public double getWhiteLevel() {
        return whiteLevel;
    }

}
