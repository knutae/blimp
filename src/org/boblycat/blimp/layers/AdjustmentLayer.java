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

import java.util.logging.Level;

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
            Util.logger.log(Level.SEVERE,
                    op.getClass().getName() + " failed with a "
                    + e.getClass().getName() + ": " + e.getMessage(),
                    e);
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
