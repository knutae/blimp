package org.boblycat.blimp;

import java.util.Iterator;
import java.util.Vector;

import org.boblycat.blimp.layers.*;

/**
 * Registry for adjustment layers (not input layers).
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
        reg.registerLayer(Color16BitLayer.class, "&16-Bit",
                "Convert image to 16-bit per color channel");
        reg.registerLayer(GammaLayer.class, "&Gamma", "Adjust gamma");
        reg.registerLayer(GrayscaleMixerLayer.class, "&Grayscale Mixer",
                "Convert to grayscale using an RGB mixer");
        reg.registerLayer(ResizeLayer.class, "&Resize", "Resize Image");
        reg.registerLayer(UnsharpMaskLayer.class, "&Unsharp Mask",
                "Image sharpening");
        reg.registerLayer(LocalContrastLayer.class, "&Local Contrast Enhancement",
                "Increase contrast locally in adjacent areas of the image");
        return reg;
    }
}
