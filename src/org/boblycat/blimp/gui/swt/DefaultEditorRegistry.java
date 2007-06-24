package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.layers.*;
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
    }
}
