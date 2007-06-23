package org.boblycat.blimp.layers;

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
    // TODO: add this later
    //public abstract BitmapSize calculateSize(int inputWidth, int inputHeight);
}
