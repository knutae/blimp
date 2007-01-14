package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;

import net.sourceforge.jiu.color.adjustment.GammaCorrection;

public class GammaLayer extends AdjustmentLayer {
    double gamma;

    public GammaLayer() {
        gamma = 1.0;
    }

    public void setGamma(double value) {
        if (value <= 0.0 || value > GammaCorrection.MAX_GAMMA) {
            System.err.println("Ignored invalid gamma value " + value);
            return;
        }
        gamma = value;
    }

    public double getGamma() {
        return gamma;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        GammaCorrection op = new GammaCorrection();
        op.setGamma(gamma);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Gamma Correction";
    }

}
