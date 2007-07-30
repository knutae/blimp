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

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;

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