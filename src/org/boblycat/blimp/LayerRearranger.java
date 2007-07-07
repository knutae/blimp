package org.boblycat.blimp;

import java.util.Vector;

import org.boblycat.blimp.layers.AdjustmentLayer;

/**
 * This helper class for rearranging layers for optimization purposes.
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
    public static <T extends AdjustmentLayer> Vector<T> optimizeLayerOrder(
            Vector<T> layers) {
        Vector<T> newLayers = new Vector<T>();
        Vector<T> buffer = new Vector<T>();
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
