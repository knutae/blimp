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

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.color.adjustment.Brightness;

class MultiplicativeContrast extends LookupTableOperation {
    static double transform(double x, double contrast) {
        return Util.constrainedValue(x * contrast, -1, 1);
    }

    void setTablesFromContrast(double contrast, int bitdepth) {
        int size = 1 << bitdepth;
        double factor = (size - 1);
        int[] tableData = new int[size];
        for (int i = 0; i < size; i++) {
            double x = 2.0 * i / factor - 1.0;
            double y = transform(x, contrast);
            int iy = (int) ((0.5 * y + 0.5) * factor);
            tableData[i] = iy;
        }
        setTables(tableData);
    }
}

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
        this.brightness = Util.constrainedValue(brightness,
                MIN_BRIGHTNESS, MAX_BRIGHTNESS);
    }

    public void setContrast(int contrast) {
        this.contrast = Util.constrainedValue(contrast,
                MIN_CONTRAST, MAX_CONTRAST);
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
            int bitDepth;
            if (image instanceof RGB48Image)
                bitDepth = 16;
            else
                bitDepth = 8;
            op.setTablesFromContrast(contrast/100.0, bitDepth);
            image = applyJiuOperation(image, op);
        }
        return new Bitmap(image);
    }

    public String getDescription() {
        return "Brightness and Contrast";
    }
}