package org.boblycat.blimp;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.color.Invert;

public class InvertLayer extends AdjustmentLayer {
    public Bitmap applyLayer(Bitmap source) {
        PixelImage image = source.getImage();
        image = applyJiuOperation(image, new Invert());
        return new Bitmap(image);
    }

    public String getDescription() {
        return "Invert";
    }
}
