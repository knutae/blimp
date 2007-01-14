package org.boblycat.blimp;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.color.adjustment.Brightness;
import net.sourceforge.jiu.color.adjustment.Contrast;

public class BrightnessContrastLayer extends AdjustmentLayer {
    int brightness;
    int contrast;

    public BrightnessContrastLayer(int brightness, int contrast) {
        setBrightness(brightness);
        setContrast(contrast);
    }

    public BrightnessContrastLayer() {
        this(0, 0);
    }

    public int getBrightness() {
        return brightness;
    }

    public int getContrast() {
        return contrast;
    }

    public void setBrightness(int brightness) {
        this.brightness = Util.constrainedValue(brightness, -100, 100);
    }

    public void setContrast(int contrast) {
        this.contrast = Util.constrainedValue(contrast, -100, 100);
    }

    public Bitmap applyLayer(Bitmap source) {
        PixelImage image = source.getImage();
        if (brightness != 0) {
            Brightness bOp = new Brightness();
            bOp.setBrightness(brightness);
            image = applyJiuOperation(image, bOp);
        }
        if (contrast != 0) {
            Contrast cOp = new Contrast();
            cOp.setContrast(contrast);
            image = applyJiuOperation(image, cOp);
        }
        return new Bitmap(image);
    }

    public String getDescription() {
        return "Brightness and Contrast";
    }
}