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
package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.layers.BrightnessContrastLayer;
import org.boblycat.blimp.layers.ColorBalanceLayer;
import org.boblycat.blimp.layers.ColorizeLayer;
import org.boblycat.blimp.layers.CropLayer;
import org.boblycat.blimp.layers.CurvesLayer;
import org.boblycat.blimp.layers.GammaLayer;
import org.boblycat.blimp.layers.GrayscaleMixerLayer;
import org.boblycat.blimp.layers.LevelsLayer;
import org.boblycat.blimp.layers.LocalContrastLayer;
import org.boblycat.blimp.layers.OrientationLayer;
import org.boblycat.blimp.layers.PrintLayer;
import org.boblycat.blimp.layers.RawFileInputLayer;
import org.boblycat.blimp.layers.ResizeLayer;
import org.boblycat.blimp.layers.RotateLayer;
import org.boblycat.blimp.layers.SaturationLayer;
import org.boblycat.blimp.layers.SolidColorBorderLayer;
import org.boblycat.blimp.layers.UnsharpMaskLayer;
import org.eclipse.swt.widgets.Shell;

/**
 * A subclass of LayerEditorRegistry which automatically registers all
 * the built-in editors.
 *
 * @author Knut Arild Erstad
 */
public class DefaultEditorRegistry extends LayerEditorRegistry {

    public DefaultEditorRegistry(Shell parent) {
        super(parent);
        register(BrightnessContrastLayer.class, BrightnessContrastEditor.class);
        register(CurvesLayer.class, CurvesEditor.class);
        register(RawFileInputLayer.class, RawInputEditor.class);
        register(GammaLayer.class, GammaEditor.class);
        register(GrayscaleMixerLayer.class, GrayscaleMixerEditor.class);
        register(LocalContrastLayer.class, LocalContrastEditor.class);
        register(LevelsLayer.class, LevelsEditor.class);
        register(OrientationLayer.class, OrientationEditor.class);
        register(ResizeLayer.class, ResizeEditor.class);
        register(CropLayer.class, CropEditor.class);
        register(SolidColorBorderLayer.class, SolidColorBorderEditor.class);
        register(SaturationLayer.class, SaturationEditor.class);
        register(RotateLayer.class, RotateEditor.class);
        register(ColorBalanceLayer.class, ColorBalanceEditor.class);
        register(ColorizeLayer.class, ColorizeEditor.class);
        register(UnsharpMaskLayer.class, UnsharpMaskEditor.class);
        register(PrintLayer.class, PrintEditor.class);
    }
}
