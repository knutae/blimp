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
public class LayerRegistry implements Iterable<LayerRegistry.Category> {
    public class LayerInfo {
        public Class<? extends AdjustmentLayer> layerClass;
        public String label;
        public String description;
    }

    public class Category implements Iterable<LayerInfo> {
        public String label;
        private Vector<LayerInfo> layers;
        private Category() {
            layers = new Vector<LayerInfo>();
        }
        public Iterator<LayerInfo> iterator() {
            return layers.iterator();
        }
        private void registerLayer(Class<? extends AdjustmentLayer> layerClass,
                String label, String description) {
            LayerInfo info = new LayerInfo();
            info.layerClass = layerClass;
            info.label = label;
            info.description = description;
            layers.add(info);
        }
    }
    
    private Vector<Category> categoryList;

    public LayerRegistry() {
        categoryList = new Vector<Category>();
    }
    
    public Category addCategory(String label) {
        Category cat = new Category();
        cat.label = label;
        categoryList.add(cat);
        return cat;
    }
    
    public Iterator<Category> iterator() {
        return categoryList.iterator();
    }

    /** Returns a layer register with the built-in layers. */
    public static LayerRegistry createDefaultRegistry() {
        LayerRegistry reg = new LayerRegistry();
        Category cat;
        
        cat = reg.addCategory("&Image");
        cat.registerLayer(OrientationLayer.class, "&Orientation",
                "Flip or rotate the image in 90 degree increments.");
        cat.registerLayer(CropLayer.class, "&Crop",
                "Crop the image.");
        cat.registerLayer(RotateLayer.class, "&Rotate",
                "Rotate the image by an arbitrary angle.");
        cat.registerLayer(ResizeLayer.class, "Re&size",
                "Resize the image.");
        cat.registerLayer(SolidColorBorderLayer.class, "&Border",
                "Add a solid color border around the image.");
        
        cat = reg.addCategory("&Color");
        cat.registerLayer(BrightnessContrastLayer.class, "&Brightness/Contrast",
                "Brightness and contrast");
        cat.registerLayer(LevelsLayer.class, "&Levels",
                "Adjust brightness levels");
        cat.registerLayer(CurvesLayer.class, "&Curves",
                "Precise tonality adjustment");
        cat.registerLayer(ColorBalanceLayer.class, "Color &Balance",
                "Adjust the color tones");
        cat.registerLayer(SaturationLayer.class, "&Hue/Saturation/Lightness",
                "Adjust hue, saturation and lightness");
        cat.registerLayer(GrayscaleMixerLayer.class, "&Grayscale Mixer",
                "Convert to grayscale using an RGB mixer");
        cat.registerLayer(GammaLayer.class, "Ga&mma",
                "Gamma correction (lightness)");
        cat.registerLayer(InvertLayer.class, "&Invert",
                "Invert all colors, resulting in a negative image");
        cat.registerLayer(Color16BitLayer.class, "Promote to &16-Bit",
                "Promote the image from 8-bit to 16-bit per color channel");
        
        cat = reg.addCategory("&Enhance");
        cat.registerLayer(LocalContrastLayer.class, "&Local Contrast Enhancement",
                "Increase contrast locally in adjacent areas of the image");
        cat.registerLayer(UnsharpMaskLayer.class, "&Unsharp Mask",
                "Sharpen the image");
        return reg;
    }
}
