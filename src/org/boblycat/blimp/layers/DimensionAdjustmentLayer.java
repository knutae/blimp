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

import org.boblycat.blimp.BitmapSize;

/**
 * Base class for adjustment layers that can change the dimensions of a bitmap.
 *
 * All layers that can change the size of an input bitmap <i>must</i> inherit
 * from this class.  This includes operations such as rescaling, cropping and
 * adding borders.
 *
 * @author Knut Arild Erstad
 */
public abstract class DimensionAdjustmentLayer extends AdjustmentLayer {
    public abstract BitmapSize calculateSize(BitmapSize inputSize);

    @Override
    public boolean canChangeDimensions() {
        return true;
    }
}
