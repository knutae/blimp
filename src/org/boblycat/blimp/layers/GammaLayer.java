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

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.util.Util;

import net.sourceforge.jiu.color.adjustment.GammaCorrection;

public class GammaLayer extends AdjustmentLayer {
    double gamma;

    public GammaLayer() {
        gamma = 1.0;
    }

    public void setGamma(double value) {
        if (value <= 0.0 || value > GammaCorrection.MAX_GAMMA) {
            Util.err("Ignored invalid gamma value " + value);
            return;
        }
        gamma = value;
    }

    public double getGamma() {
        return gamma;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        GammaCorrection op = new GammaCorrection();
        op.setGamma(gamma);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Gamma Correction";
    }

}
