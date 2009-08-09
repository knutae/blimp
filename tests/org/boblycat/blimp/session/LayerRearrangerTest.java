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
package org.boblycat.blimp.session;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.CropLayer;
import org.boblycat.blimp.layers.CurvesLayer;
import org.boblycat.blimp.layers.GammaLayer;
import org.boblycat.blimp.layers.GrayscaleMixerLayer;
import org.boblycat.blimp.layers.ResizeLayer;
import org.boblycat.blimp.layers.SolidColorBorderLayer;
import org.boblycat.blimp.layers.TestLayer;
import org.boblycat.blimp.layers.ViewResizeLayer;
import org.boblycat.blimp.session.LayerRearranger;
import org.junit.Test;

public class LayerRearrangerTest {
    List<AdjustmentLayer> layers;
    List<AdjustmentLayer> newLayers;

    public LayerRearrangerTest() {
        layers = new ArrayList<AdjustmentLayer>();
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

    @Test
    public void testGammaAndViewResize() {
        GammaLayer gamma = new GammaLayer();
        ViewResizeLayer viewResize = new ViewResizeLayer();
        layers.add(gamma);
        layers.add(viewResize);
        execute();
        assertSame(viewResize, newLayers.get(0));
        assertSame(gamma, newLayers.get(1));
    }
}
