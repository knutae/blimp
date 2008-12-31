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

import java.util.Arrays;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.ZoomFactor;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.geometry.ScaleReplication;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

class SuperSamplingDownScaleOperation extends ImageToImageOperation {
    int downScaleFactor;
    
    public void process() throws MissingParameterException,
    WrongParameterException {
        PixelImage pInput = getInputImage();
        if (pInput == null)
            throw new MissingParameterException("missing input image");
        if (!(pInput instanceof IntegerImage))
            throw new WrongParameterException("unsupported image type: must be IntegerImage");
        if (downScaleFactor <= 0)
            throw new WrongParameterException("unsupported downscale factor, must be >= 1");
        IntegerImage input = (IntegerImage) pInput;
        // Note: can currently drop a few pixels at the edges, but that seems acceptable...?
        int inWidth = input.getWidth();
        int inHeight = input.getHeight(); 
        int outWidth = inWidth / downScaleFactor;
        int outHeight = inHeight / downScaleFactor;
        IntegerImage output = (IntegerImage) input.createCompatibleImage(outWidth, outHeight);
        int[] outLine = new int[outWidth];
        int[] inLine = new int[inWidth];
        int factor2 = downScaleFactor * downScaleFactor;
        for (int channel=0; channel<input.getNumChannels(); channel++) {
            for (int y=0; y<outHeight; y++) {
                Arrays.fill(outLine, 0);
                int startY = y*downScaleFactor;
                int endY = Math.min((y+1)*downScaleFactor, inHeight);
                for (int inY=startY; inY<endY; inY++) {
                    input.getSamples(channel, 0, inY, inWidth, 1, inLine, 0);
                    int endX = outWidth * downScaleFactor;
                    for (int inX=0; inX < endX; inX++) {
                        outLine[inX / downScaleFactor] += inLine[inX];
                    }
                }
                for (int x=0; x<outWidth; x++) {
                    outLine[x] /= factor2;
                }
                output.putSamples(channel, 0, y, outWidth, 1, outLine, 0);
                setProgress(y, outHeight);
            }
        }
        setOutputImage(output);
    }
}

/**
 * A fast resize layer for e.g. zooming in the image view.
 *
 * @author Knut Arild Erstad
 */
public class ViewResizeLayer extends DimensionAdjustmentLayer {
    int viewWidth;
    int viewHeight;
    int imageWidth;
    int imageHeight;
    ZoomFactor zoomFactor;

    public ViewResizeLayer() {
        zoomFactor = new ZoomFactor();
        viewWidth = 100;
        viewHeight = 100;
    }

    private void setImageSize(int w, int h) {
        if (w != imageWidth || h != imageHeight)
            // invalidate current zoom when size changes
            zoomFactor = getAutoZoomFactor(w, h);
        imageWidth = w;
        imageHeight = h;
    }

    private ZoomFactor getAutoZoomFactor(int imageWidth, int imageHeight) {
        ZoomFactor autoZoomFactor = new ZoomFactor();
        while (autoZoomFactor.scale(imageWidth) > viewWidth
                || autoZoomFactor.scale(imageHeight) > viewHeight)
            autoZoomFactor.zoomOut();
        return autoZoomFactor;
    }

    public Bitmap applyLayer(Bitmap source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        setImageSize(sourceWidth, sourceHeight);
        Bitmap ret = source;
        // FIXME: scaling to 2/3 is a memory hog due to upscaling the whole image first.
        if (zoomFactor.getMultiplier() > 1) {
            ScaleReplication upSizer = new ScaleReplication();
            upSizer.setInputImage(ret.getImage());
            upSizer.setSize(
                    sourceWidth * zoomFactor.getMultiplier(),
                    sourceHeight * zoomFactor.getMultiplier());
            ret = new Bitmap(applyJiuOperation(ret.getImage(), upSizer));
        }
        if (zoomFactor.getDivisor() > 1) {
            SuperSamplingDownScaleOperation downSizer = new SuperSamplingDownScaleOperation();
            downSizer.downScaleFactor = zoomFactor.getDivisor();
            ret = new Bitmap(applyJiuOperation(ret.getImage(), downSizer));
        }
        double scaleFactor = source.getWidth() / (double) ret.getWidth();
        ret.setPixelScaleFactor(source.getPixelScaleFactor() * scaleFactor);
        return ret;
    }

    public ZoomFactor zoom() {
        return zoomFactor;
    }
    
    public void setZoom(ZoomFactor zoom) {
        zoomFactor = zoom;
    }

    public String getDescription() {
        return "View Resize";
    }

    public double getZoomValue() {
        return zoomFactor.toDouble();
    }

    public void setZoomValue(double value) {
        // Currently this function is only here in order to get the bean
        // serialization to work, needed for the bitmap cache
        // TODO: either implement the function, or find a different strategy
    }

    public void setViewWidth(int viewWidth) {
        this.viewWidth = viewWidth;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public void setViewHeight(int viewHeight) {
        this.viewHeight = viewHeight;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        int w = zoomFactor.scale(inputSize.width);
        int h = zoomFactor.scale(inputSize.height);
        double scaleFactor = inputSize.width / (double) w;
        return new BitmapSize(w, h, scaleFactor * inputSize.pixelScaleFactor);
    }
}
