package org.boblycat.blimp.layers;

import net.sourceforge.jiu.filters.ConvolutionKernelFilter;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.jiu.UnsharpMaskKernel;

public class UnsharpMaskLayer extends AdjustmentLayer {
    int level;
    
    public UnsharpMaskLayer() {
        level = 1;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        ConvolutionKernelFilter op = new ConvolutionKernelFilter();
        op.setKernel(new UnsharpMaskKernel(level));
        //op.setKernel(ConvolutionKernelFilter.TYPE_SHARPEN);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Unsharp Mask";
    }

    public void setLevel(int level) {
        this.level = Util.constrainedValue(level, 1, 50);
    }

    public int getLevel() {
        return level;
    }

}
