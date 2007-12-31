/*
 * Copyright (C) 2007 Knut Arild Erstad
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

import net.sourceforge.jiu.filters.ConvolutionKernelFilter;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.jiu.UnsharpMaskKernel;

public class UnsharpMaskLayer extends AdjustmentLayer {
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 50;

    int level;

    public UnsharpMaskLayer() {
        level = 1;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        if (level == 0)
            return source;
        ConvolutionKernelFilter op = new ConvolutionKernelFilter();
        op.setKernel(new UnsharpMaskKernel(level));
        //op.setKernel(ConvolutionKernelFilter.TYPE_SHARPEN);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Unsharp Mask";
    }

    public void setLevel(int level) {
        this.level = Util.constrainedValue(level, MIN_LEVEL, MAX_LEVEL);
    }

    public int getLevel() {
        return level;
    }

}
