package org.boblycat.blimp.tests;

import static org.junit.Assert.*;

import java.util.Vector;

import org.boblycat.blimp.LayerRearranger;
import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.CropLayer;
import org.boblycat.blimp.layers.CurvesLayer;
import org.boblycat.blimp.layers.GammaLayer;
import org.boblycat.blimp.layers.GrayscaleMixerLayer;
import org.boblycat.blimp.layers.ResizeLayer;
import org.boblycat.blimp.layers.SolidColorBorderLayer;
import org.junit.Test;

public class LayerRearrangerTest {
    Vector<AdjustmentLayer> layers;
    Vector<AdjustmentLayer> newLayers;
    
    public LayerRearrangerTest() {
        layers = new Vector<AdjustmentLayer>();
        newLayers = null;
    }
    
    private void execute() {
        newLayers = LayerRearranger.optimizeLayerOrder(layers);
        assertNotNull(newLayers);
        assertEquals(layers.size(), newLayers.size());
    }

    @Test
    public void testEmpty() {
        newLayers = LayerRearranger.optimizeLayerOrder(layers);
        execute();
    }
    
    @Test
    public void testSingle() {
        layers.add(new TestLayer());
        newLayers = LayerRearranger.optimizeLayerOrder(layers);
        execute();
        assertSame(layers.get(0), newLayers.get(0));
    }
    
    @Test
    public void testResizeOptimization() {
        ResizeLayer resize = new ResizeLayer();
        GammaLayer gamma = new GammaLayer();
        GrayscaleMixerLayer mixer = new GrayscaleMixerLayer();
        layers.add(gamma);
        layers.add(mixer);
        layers.add(resize);
        execute();
        assertSame(resize, newLayers.get(0));
        assertSame(gamma, newLayers.get(1));
        assertSame(mixer, newLayers.get(2));
    }
    
    @Test
    public void testCropAndResizeOptimization() {
        CropLayer crop = new CropLayer();
        ResizeLayer resize = new ResizeLayer();
        GammaLayer gamma = new GammaLayer();
        layers.add(crop);
        layers.add(gamma);
        layers.add(resize);
        execute();
        assertSame(crop, newLayers.get(0));
        assertSame(resize, newLayers.get(1));
        assertSame(gamma, newLayers.get(2));
    }
    
    @Test
    public void testGammaAndBorderLayers() {
        GammaLayer gamma = new GammaLayer();
        SolidColorBorderLayer border = new SolidColorBorderLayer();
        layers.add(gamma);
        layers.add(border);
        execute();
        assertSame(gamma, newLayers.get(0));
        assertSame(border, newLayers.get(1));
    }
    
    @Test
    public void testBorderAndResizeLayers() {
        SolidColorBorderLayer border = new SolidColorBorderLayer();
        ResizeLayer resize = new ResizeLayer();
        layers.add(border);
        layers.add(resize);
        execute();
        assertSame(border, newLayers.get(0));
        assertSame(resize, newLayers.get(1));
    }
    
    @Test
    public void testGammaCurvesResizeAndBorder() {
        GammaLayer gamma = new GammaLayer();
        CurvesLayer curves = new CurvesLayer();
        ResizeLayer resize = new ResizeLayer();
        SolidColorBorderLayer border = new SolidColorBorderLayer();
        layers.add(gamma);
        layers.add(curves);
        layers.add(resize);
        layers.add(border);
        execute();
        assertSame(resize, newLayers.get(0));
        assertSame(gamma, newLayers.get(1));
        assertSame(curves, newLayers.get(2));
        assertSame(border, newLayers.get(3));
    }

    @Test
    public void testGammaCurvesBorderAndResize() {
        GammaLayer gamma = new GammaLayer();
        CurvesLayer curves = new CurvesLayer();
        SolidColorBorderLayer border = new SolidColorBorderLayer();
        ResizeLayer resize = new ResizeLayer();
        layers.add(gamma);
        layers.add(curves);
        layers.add(border);
        layers.add(resize);
        execute();
        assertSame(gamma, newLayers.get(0));
        assertSame(curves, newLayers.get(1));
        assertSame(border, newLayers.get(2));
        assertSame(resize, newLayers.get(3));
    }
}
