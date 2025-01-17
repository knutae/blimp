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

import net.sourceforge.jiu.color.promotion.PromotionRGB48;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB48Image;

public class Color16BitLayer extends AdjustmentLayer {

    @Override
    public Bitmap applyLayer(Bitmap source) {
        PixelImage image = source.getImage();
        if (!(image instanceof RGB48Image))
            image = applyJiuOperation(image, new PromotionRGB48());
        return new Bitmap(image);
    }

    @Override
    public String getDescription() {
        return "16-bit Color Depth";
    }

}
