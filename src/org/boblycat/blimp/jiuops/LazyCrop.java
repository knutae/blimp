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
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.geometry.Crop;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Crop implemented as a "lazy" operation for some image types, namely
 * {@link RGB24Image} and {@link RGB48Image}.
 * 
 * For the supported image types, no image data will be copied, the output
 * image will be a read-only wrapper that delegates sample operations to the
 * input image.
 * 
 * For other image types, this works like a normal {@link Crop} operation.
 * 
 * @author Knut Arild Erstad
 */
public class LazyCrop extends Crop {
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    
    public void setBounds(int x1, int y1, int x2, int y2) {
        super.setBounds(x1, y1, x2, y2);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    public void process() throws MissingParameterException, WrongParameterException {
        PixelImage input = getInputImage();
        if (input == null)
            throw new MissingParameterException("missing input image");
        if (input instanceof RGB48Image) {
            setOutputImage(new CroppedRGB48Image((RGB48Image) input, x1, y1, x2, y2));
        }
        else if (input instanceof RGB24Image) {
            setOutputImage(new CroppedRGB24Image((RGB24Image) input, x1, y1, x2, y2));
        }
        else {
            // fall back to normal (non-lazy) crop
            super.process();
        }
    }
}
