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
package org.boblycat.blimp.jiuops;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Simple contrast operation based upon multiplication around a middle-grey center.
 * 
 * @author Knut Arild Erstad
 */
public class MultiplicativeContrastOperation extends LookupTableOperation {
    private int contrast;
    
    public MultiplicativeContrastOperation() {
        contrast = 100;
    }
    
    private static double transform(double x, double contrast) {
        return MathUtil.clamp(x * contrast, -1, 1);
    }

    /**
     * Set the contrast.
     * @param contrast a contrast value where 100 means no change
     */
    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    @Override
    public void process() throws MissingParameterException,
    WrongParameterException {
        PixelImage input = getInputImage();
        if (input == null)
            throw new MissingParameterException("no input image");
        // Create and set tables based on the bit depth of the input image
        int bitDepth = input.getBitsPerPixel() / input.getNumChannels();
        int size = 1 << bitDepth;
        double factor = (size - 1);
        double fContrast = contrast / 100.0;
        int[] tableData = new int[size];
        for (int i = 0; i < size; i++) {
            double x = 2.0 * i / factor - 1.0;
            double y = transform(x, fContrast);
            int iy = (int) ((0.5 * y + 0.5) * factor);
            tableData[i] = iy;
        }
        setTables(tableData);
        super.process();
    }
}
