package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.LayerRearranger;
import org.boblycat.blimp.ProgressEvent;
import org.boblycat.blimp.Util;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.ProgressListener;

/**
 * Abstract base class for adjustment layers.
 */
public abstract class AdjustmentLayer extends Layer {
    public abstract Bitmap applyLayer(Bitmap source);
    
    class JiuProgressListener implements ProgressListener {
        AdjustmentLayer layer;
        ProgressEvent event;
        
        JiuProgressListener(AdjustmentLayer layer) {
            this.layer = layer;
            event = new ProgressEvent(layer);
        }
        
        public void setProgress(float progress) {
            //ProgressEvent event = new ProgressEvent(layer); 
            event.progress = progress;
            layer.progressEventSource.triggerChangeWithEvent(event);
        }
        
        public void setProgress(int index, int size) {
            setProgress((float) index / (float) size);
        }
    }

    /** Helper function for applying JIU operations */
    protected PixelImage applyJiuOperation(PixelImage input,
            ImageToImageOperation op) {
        PixelImage image = input;
        op.setInputImage(image);
        ProgressListener listener = null;
        if (progressEventSource.size() > 0) {
            listener = new JiuProgressListener(this);
            op.addProgressListener(listener);
        }
        try {
            op.process();
            image = op.getOutputImage();
        }
        catch (Exception e) {
            Util.err(op.getClass().getName() + ": " + e.getMessage());
        }
        if (listener != null)
            op.removeProgressListener(listener);
        return image;
    }
    
    /**
     * All layers that change the dimensions of an image must override this
     * function and return <code>true</code>.  This is used when deciding the
     * layer reordering for optimization purpuses when previewing,
     * see {@link LayerRearranger} for details.
     * 
     * @return
     *  <code>true</code> if the layer can change the dimensions,
     *  <code>false</code> otherwise. 
     */
    public boolean canChangeDimensions() {
        return false;
    }
    
    /**
     * All layers that change the color or color intensity of an image must
     * return <code>true</code>.  The default implementation returns the
     * oppsosite of <code>canChangeDimensions()</code>, since most layers
     * will only change either change one or the other.
     * 
     * @return
     *  <code>true</code> if the layer can change the colors of the input image
     *  in some way, <code>false</code> otherwise.
     */
    public boolean canChangeColors() {
        return !canChangeDimensions();
    }
}
