package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.ProgressEvent;

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
            System.err.println(op.getClass().getName() + ": " + e.getMessage());
        }
        if (listener != null)
            op.removeProgressListener(listener);
        return image;
    }
}
