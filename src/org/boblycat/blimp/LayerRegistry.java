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
package org.boblycat.blimp;

import java.util.Iterator;
import java.util.Vector;

import org.boblycat.blimp.layers.*;

/**
 * Registry for adjustment layers (not input layers).
 * This is used for generating menus in the GUI.
 * 
 * @author Knut Arild Erstad
 */
public class LayerRegistry implements Iterable<LayerRegistry.LayerInfo> {
    public class LayerInfo {
        public Class<? extends AdjustmentLayer> layerClass;
        public String label;
        public String description;
        // String category;
    }

    private Vector<LayerInfo> layerInfoList;

    public LayerRegistry() {
        layerInfoList = new Vector<LayerInfo>();
    }

    public void registerLayer(Class<? extends AdjustmentLayer> layerClass,
            String label, String description) {
        LayerInfo info = new LayerInfo();
        info.layerClass = layerClass;
        info.label = label;
        info.description = description;
        layerInfoList.add(info);
    }

    public Iterator<LayerInfo> iterator() {
        return layerInfoList.iterator();
    }

    /** Returns a layer register with the built-in layers. */
    public static LayerRegistry createDefaultRegister() {
        LayerRegistry reg = new LayerRegistry();
        reg.registerLayer(InvertLayer.class, "&Invert", "Invert (negative)");
        reg.registerLayer(BrightnessContrastLayer.class,
                "&Brightness/Contrast", "Brightness and Contrast");
        reg.registerLayer(CurvesLayer.class, "&Curves", "Curves");
        reg.registerLayer(SaturationLayer.class, "&Hue/Saturation/Lightness",
                "Adjust Hue, Saturation and Lightness");
        reg.registerLayer(Color16BitLayer.class, "Promote to &16-Bit",
                "Promote the image to 16-bit per color channel");
        reg.registerLayer(GammaLayer.class, "Ga&mma", "Adjust gamma");
        reg.registerLayer(GrayscaleMixerLayer.class, "&Grayscale Mixer",
                "Convert to grayscale using an RGB mixer");
        reg.registerLayer(ResizeLayer.class, "&Resize", "Resize Image");
        reg.registerLayer(UnsharpMaskLayer.class, "&Unsharp Mask",
                "Image sharpening");
        reg.registerLayer(LocalContrastLayer.class, "Local Contrast &Enhancement",
                "Increase contrast locally in adjacent areas of the image");
        reg.registerLayer(CropLayer.class, "Cro&p", "Crop Image");
        reg.registerLayer(LevelsLayer.class, "&Levels", "Levels");
        reg.registerLayer(OrientationLayer.class, "&Orientation",
                "Change the Image Orientation");
        reg.registerLayer(SolidColorBorderLayer.class, "Bor&der",
                "Add a solid color border around the image.");
        reg.registerLayer(RotateLayer.class, "Ro&tate (EXPERIMENTAL)",
                "Rotate the image by an arbitrary angle.");
        return reg;
    }
}
