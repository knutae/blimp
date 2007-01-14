package org.boblycat.blimp;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;

/**
 * Abstract base class for adjustment layers.
 */
public abstract class AdjustmentLayer extends Layer {
    public abstract Bitmap applyLayer(Bitmap source);

    /** Helper function for applying JIU operations */
    protected static PixelImage applyJiuOperation(PixelImage input,
            ImageToImageOperation op) {
        PixelImage image = input;
        op.setInputImage(image);
        try {
            op.process();
            image = op.getOutputImage();
        }
        catch (Exception e) {
            System.err.println(op.getClass().getName() + ": " + e.getMessage());
        }
        return image;
    }
}
