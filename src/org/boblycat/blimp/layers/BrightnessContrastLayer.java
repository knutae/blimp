/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
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

import org.boblycat.blimp.data.Bitmap;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.util.MathUtil;
import net.sourceforge.jiu.color.adjustment.Brightness;
import net.sourceforge.jiu.color.adjustment.MultiplicativeContrast;

public class BrightnessContrastLayer extends AdjustmentLayer {
    public static final int MIN_BRIGHTNESS = -100;
    public static final int MAX_BRIGHTNESS = 100;
    public static final int MIN_CONTRAST = 0;
    public static final int MAX_CONTRAST = 400;

    int brightness = 0;
    int contrast = 100;

    public BrightnessContrastLayer(int brightness, int contrast) {
        setBrightness(brightness);
        setContrast(contrast);
    }

    public BrightnessContrastLayer() {
    }

    public int getBrightness() {
        return brightness;
    }

    public int getContrast() {
        return contrast;
    }

    public void setBrightness(int brightness) {
        this.brightness = MathUtil.clamp(brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS);
    }

    public void setContrast(int contrast) {
        this.contrast = MathUtil.clamp(contrast, MIN_CONTRAST, MAX_CONTRAST);
    }

    public Bitmap applyLayer(Bitmap source) {
        PixelImage image = source.getImage();
        if (brightness != 0) {
            Brightness bOp = new Brightness();
            bOp.setBrightness(brightness);
            image = applyJiuOperation(image, bOp);
        }
        if (contrast != 100) {
            MultiplicativeContrast op = new MultiplicativeContrast();
            op.setContrast(contrast);
            image = applyJiuOperation(image, op);
        }
        return new Bitmap(image);
    }

    public String getDescription() {
        return "Brightness and Contrast";
    }
}