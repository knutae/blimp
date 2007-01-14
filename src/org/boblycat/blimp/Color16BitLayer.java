package org.boblycat.blimp;

import net.sourceforge.jiu.color.promotion.PromotionRGB48;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB48Image;

public class Color16BitLayer extends Layer {

	@Override
	public Bitmap applyLayer(Bitmap source) {
		PixelImage image = source.getImage();
		if (!(image instanceof RGB48Image))
			image = applyJiuOperation(image, new PromotionRGB48());
		return new Bitmap(image);
	}

	@Override
	public String getName() {
		return "16-bit Color Depth";
	}

}
