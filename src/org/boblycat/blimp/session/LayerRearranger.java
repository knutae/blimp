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
package org.boblycat.blimp.session;

import java.util.ArrayList;
import java.util.List;

import org.boblycat.blimp.layers.AdjustmentLayer;

/**
 * This helper class rearranges layers for optimization purposes.
 *
 * This is used by <code>BlimpSession</code> when previewing images.
 * The idea is to put layers that downscale the image data earlier in the
 * layer order so that other layers operate on smaller amounts of image
 * data, while still maintaining the most important dependencies between
 * layers.
 *
 * @author Knut Arild Erstad
 */
public class LayerRearranger {
    public static <T extends AdjustmentLayer> ArrayList<T> optimizeLayerOrder(
            List<T> layers) {
        ArrayList<T> newLayers = new ArrayList<T>();
        ArrayList<T> buffer = new ArrayList<T>();
        for (T layer: layers) {
            if (layer.canChangeDimensions()) {
                if (layer.canChangeColors()) {
                    newLayers.addAll(buffer);
                    buffer.clear();
                }
                newLayers.add(layer);
            }
            else {
                buffer.add(layer);
            }
        }
        newLayers.addAll(buffer);
        return newLayers;
    }
}
