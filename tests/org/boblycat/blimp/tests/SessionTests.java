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
package org.boblycat.blimp.tests;

import java.io.IOException;

import org.boblycat.blimp.*;
import org.boblycat.blimp.BlimpSession.PreviewQuality;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.layers.ResizeLayer;
import org.boblycat.blimp.layers.ViewResizeLayer;
import org.junit.*;
import static org.junit.Assert.*;

public class SessionTests {
    int eventCount;

    LayerEvent lastLayerEvent;

    protected BlimpSession newSession() {
        return new BlimpSession();
    }

    BlimpSession createTestSession() {
        BlimpSession session = newSession();
        session.setInput(new TestInput());
        return session;
    }

    TestBitmap getTestBitmap(BlimpSession session) {
        try {
            Bitmap bitmap = session.getBitmap();
            assertNotNull(bitmap);
            assertSame(bitmap.getClass(), TestBitmap.class);
            return (TestBitmap) bitmap;
        }
        catch (IOException e) {
            assertTrue(false); // no exception expected
            return null;
        }
    }

    @Test
    public void testNoLayers() {
        BlimpSession session = createTestSession();
        TestBitmap testBitmap = getTestBitmap(session);
        assertEquals("TestInput", testBitmap.creator);
        assertEquals("", testBitmap.testValue);
    }

    @Test
    public void testSingleLayer() {
        for (int i = 0; i < 4; i++) {
            BlimpSession session = createTestSession();
            assertEquals(1, session.layerCount());
            session.addLayer(new TestLayer(Integer.toString(i)));
            assertEquals(2, session.layerCount());
            TestBitmap testBitmap = getTestBitmap(session);
            assertEquals("TestLayer", testBitmap.creator);
            assertEquals(Integer.toString(i), testBitmap.testValue);
        }
    }

    @Test
    public void testSingleInactiveLayer() {
        BlimpSession session = createTestSession();
        assertEquals(1, session.layerCount());
        session.addLayer(new TestLayer("TEST"));
        assertEquals(2, session.layerCount());
        session.activateLayer(1, false);
        TestBitmap testBitmap = getTestBitmap(session);
        assertEquals("TestInput", testBitmap.creator);
        assertEquals("", testBitmap.testValue);
    }

    @Test
    public void testMultipleLayers() {
        BlimpSession session = createTestSession();
        assertEquals(1, session.layerCount());
        session.addLayer(new TestLayer("A"));
        session.addLayer(new TestLayer("B"));
        session.addLayer(new TestLayer("C"));
        assertEquals(4, session.layerCount());
        TestBitmap testBitmap = getTestBitmap(session);
        assertEquals("TestLayer", testBitmap.creator);
        assertEquals("ABC", testBitmap.testValue);
    }

    @Test
    public void testMultipleLayersInactive() {
        BlimpSession session = createTestSession();
        assertEquals(1, session.layerCount());
        session.addLayer(new TestLayer("a"));
        session.addLayer(new TestLayer("b"));
        session.addLayer(new TestLayer("c"));
        assertEquals(4, session.layerCount());
        TestBitmap testBitmap;
        // all layers active
        testBitmap = getTestBitmap(session);
        assertEquals("abc", testBitmap.testValue);
        // layer a inactive
        session.activateLayer(1, false);
        testBitmap = getTestBitmap(session);
        assertEquals("bc", testBitmap.testValue);
        session.activateLayer(1, true);
        // layer b inactive
        session.activateLayer(2, false);
        testBitmap = getTestBitmap(session);
        assertEquals("ac", testBitmap.testValue);
        session.activateLayer(2, true);
        // layer c inactive
        session.activateLayer(3, false);
        testBitmap = getTestBitmap(session);
        assertEquals("ab", testBitmap.testValue);
        session.activateLayer(3, true);
    }

    @Test
    public void testMoveLayer() {
        BlimpSession session = createTestSession();
        session.addLayer(new TestLayer("a"));
        session.addLayer(new TestLayer("b"));
        session.addLayer(new TestLayer("c"));
        assertEquals("abc", getTestBitmap(session).testValue);

        session.moveLayer(1, 3);
        assertEquals("bca", getTestBitmap(session).testValue);

        session.moveLayer(3, 2);
        assertEquals("bac", getTestBitmap(session).testValue);

        session.moveLayer(2, 1);
        assertEquals("abc", getTestBitmap(session).testValue);
    }

    @Test
    public void testChangeEvents() {
        eventCount = 0;
        BlimpSession session = createTestSession();
        session.addChangeListener(new LayerChangeListener() {
            public void handleChange(LayerEvent e) {
                eventCount++;
            }
        });

        assertEquals(0, eventCount);
        session.addLayer(new TestLayer("a"));
        assertEquals(1, eventCount);
        session.addLayer(new TestLayer("b"));
        assertEquals(2, eventCount);
        session.activateLayer(0, false);
        assertEquals(3, eventCount);
        session.activateLayer(1, true); // no change
        assertEquals(3, eventCount);
        session.activateLayer(1, false);
        assertEquals(4, eventCount);
        session.activateLayer(1, true);
        assertEquals(5, eventCount);
        session.removeLayer(1);
        assertEquals(6, eventCount);
        session.removeLayer(0);
        assertEquals(7, eventCount);

        // explicitly trigger event
        session.triggerChangeEvent();
        assertEquals(8, eventCount);
    }

    @Test
    public void testChangeEventsOnInvalidate() {
        eventCount = 0;
        BlimpSession session = createTestSession();
        session.addChangeListener(new LayerChangeListener() {
            public void handleChange(LayerEvent e) {
                eventCount++;
            }
        });

        TestInput input = (TestInput) session.getInput();
        TestLayer layer = new TestLayer("a");
        assertEquals(0, eventCount);
        session.addLayer(layer);
        assertEquals(1, eventCount);

        input.invalidate();
        assertEquals(2, eventCount);
        layer.invalidate();
        assertEquals(3, eventCount);
    }

    @Test
    public void testLayerChangeEvent() {
        eventCount = 0;
        lastLayerEvent = null;
        Layer layer = new TestLayer("TEST");
        layer.invalidate();
        LayerChangeListener listener = new LayerChangeListener() {
            public void handleChange(LayerEvent event) {
                eventCount++;
                lastLayerEvent = event;
            }
        };
        layer.addChangeListener(listener);

        assertEquals(0, eventCount);
        assertEquals(null, lastLayerEvent);
        layer.invalidate();
        assertEquals(1, eventCount);
        assertEquals(layer, lastLayerEvent.getLayer());

        layer.removeChangeListener(listener);
        layer.invalidate();
        assertEquals(1, eventCount);
    }

    @Test
    public void testSynchronizeSessionData() {
        BlimpSession session1 = newSession();
        BlimpSession session2 = newSession();
        TestInput input1 = new TestInput();
        input1.setFilePath("input1 path");
        input1.setName("input1 name");
        session1.setInput(input1);
        TestLayer layer1 = new TestLayer();
        layer1.setStringValue("layer1 value");
        layer1.setName("layer1 name");
        session1.addLayer(layer1);
        session1.setProjectFilePath("/path/to/session1.blimp");

        session2.synchronizeSessionData(session1, false);
        TestInput input2 = (TestInput) session2.getInput();
        assertNotNull(input2);
        assertTrue(input1 != input2); // objects must be different
        assertEquals("input1 path", input2.getFilePath());
        assertEquals("input1 name", input2.getName());
        TestLayer layer2 = (TestLayer) session2.getLayer(1);
        assertNotNull(layer2);
        assertTrue(layer1 != layer2);
        assertEquals("layer1 value", layer2.getStringValue());
        assertEquals("layer1 name", layer2.getName());
        assertEquals("/path/to/session1.blimp", session2.getProjectFilePath());

        input2.setFilePath("input2 path");
        layer2.setStringValue("layer2 value");
        session1.synchronizeSessionData(session2, false);
        assertTrue(session1.getInput() == input1); // layer must be re-used
        assertEquals("input2 path", input1.getFilePath());
        assertTrue(layer1 == session1.getLayer(1));
        assertEquals("layer2 value", layer1.getStringValue());
    }

    @Test
    public void testPixelScaleFactor() throws IOException {
        BlimpSession session = createTestSession();
        Bitmap output = session.getBitmap();
        assertEquals(1.0, output.getPixelScaleFactor());

        int origWidth = output.getWidth();
        int origHeight = output.getHeight();

        session.addLayer(new TestLayer());
        output = session.getBitmap();
        assertEquals(1.0, output.getPixelScaleFactor());

        ViewResizeLayer viewResize = new ViewResizeLayer();
        viewResize.setViewWidth(origWidth / 2);
        viewResize.setViewHeight(origHeight / 2);
        session.addLayer(viewResize);
        output = session.getBitmap();
        assertEquals(2.0, output.getPixelScaleFactor());
        session.removeLayer(viewResize);

        ResizeLayer resize = new ResizeLayer();
        resize.setMaxSize(origWidth / 4);
        session.addLayer(resize);
        output = session.getBitmap();
        assertEquals(4.0, output.getPixelScaleFactor());
    }

    @Test
    public void testGeneratedNames() {
        BlimpSession session = createTestSession();
        assertEquals("TestInput1", session.getInput().getName());
        TestLayer layer1 = new TestLayer();
        TestLayer layer2 = new TestLayer();
        session.addLayer(layer1);
        session.addLayer(layer2);
        assertEquals("Test1", layer1.getName());
        assertEquals("Test2", layer2.getName());
    }

    private static TestLayer addTestLayer(BlimpSession session, String layerName) {
        TestLayer layer = new TestLayer();
        layer.setName(layerName);
        session.addLayer(layer);
        return layer;
    }

    @Test
    public void testGetHistogramsBeforeLayer() throws IOException {
        BlimpSession session = createTestSession();
        session.getInput().setName("input");
        TestLayer layer1 = addTestLayer(session, "layer1");
        TestLayer layer2 = addTestLayer(session, "layer2");

        for (int i = 0; i < 2; i++) {
            boolean useViewport = (i == 0);

            assertNull(session.getHistogramsBeforeLayer("", useViewport));
            assertNull(session.getHistogramsBeforeLayer("dummy", useViewport));
            assertNull(session.getHistogramsBeforeLayer("input", useViewport));

            assertNotNull(session.getHistogramsBeforeLayer("layer1", useViewport));
            assertNotNull(session.getHistogramsBeforeLayer("layer2", useViewport));
            assertNotNull(session.getHistogramsBeforeLayer(null, useViewport));

            layer1.setActive(false);
            layer2.setActive(false);
            assertNotNull(session.getHistogramsBeforeLayer("layer1", useViewport));
            assertNotNull(session.getHistogramsBeforeLayer("layer2", useViewport));
        }
    }

    private void assertSizeEquals(int width, int height, BitmapSize size) {
        assertNotNull(size);
        assertEquals(width, size.width);
        assertEquals(height, size.height);
    }

    @Test
    public void testGetSizeBeforeLayer() throws IOException {
        BlimpSession session = createTestSession();
        TestInput input = (TestInput) session.getInput();
        input.setName("input");
        input.setInputSize(100, 50);
        addTestLayer(session, "layer1");

        assertNull(session.getBitmapSizeBeforeLayer(""));
        assertNull(session.getBitmapSizeBeforeLayer("dummy"));
        assertNull(session.getBitmapSizeBeforeLayer("input"));

        assertSizeEquals(100, 50, session.getBitmapSizeBeforeLayer("layer1"));

        ResizeLayer resize = new ResizeLayer();
        resize.setName("resize");
        resize.setMaxSize(20);
        resize.setActive(false);
        session.addLayer(resize);
        assertSizeEquals(100, 50, session.getBitmapSizeBeforeLayer("resize"));
        assertSizeEquals(100, 50, session.getBitmapSizeBeforeLayer("layer1"));

        resize.setActive(true);
        assertSizeEquals(100, 50, session.getBitmapSizeBeforeLayer("resize"));
        assertSizeEquals(100, 50, session.getBitmapSizeBeforeLayer("layer1"));

        addTestLayer(session, "layer2");
        assertSizeEquals(20, 10, session.getBitmapSizeBeforeLayer("layer2"));
    }
    
    @Test
    public void testGetSizedBitmapNoResize() throws Exception {
        BlimpSession session = createTestSession();
        TestInput input = (TestInput) session.getInput();
        input.setInputSize(150, 100);
        Bitmap bitmap = session.getSizedBitmap(200, 200, PreviewQuality.Fast);
        assertEquals(150, bitmap.getWidth());
        assertEquals(100, bitmap.getHeight());
        assertEquals(1.0, session.getCurrentZoom());
    }

    @Test
    public void testGetSizedBitmapHalfSize() throws Exception {
        // Use a bitmap large enough that it has to be halved in size
        BlimpSession session = createTestSession();
        TestInput input = (TestInput) session.getInput();
        input.setInputSize(190, 100);
        Bitmap bitmap = session.getSizedBitmap(100, 100, PreviewQuality.Fast);
        assertEquals(95, bitmap.getWidth());
        assertEquals(50, bitmap.getHeight());
        assertEquals(0.5, session.getCurrentZoom());
    }

    @Test
    public void testGetSizedBitmapWithResizeLayer() throws Exception {
        // Use a large bitmap, but with a resize layer so there should be no zooming
        BlimpSession session = createTestSession();
        TestInput input = (TestInput) session.getInput();
        input.setInputSize(1000, 500);
        ResizeLayer resize = new ResizeLayer();
        resize.setMaxSize(90);
        session.addLayer(resize);
        Bitmap bitmap = session.getSizedBitmap(100, 100, PreviewQuality.Fast);
        assertEquals(90, bitmap.getWidth());
        assertEquals(45, bitmap.getHeight());
        assertEquals(1.0, session.getCurrentZoom());
    }
    
    @Test
    public void testGetBitmapNoZoomSideEffect() throws Exception {
        BlimpSession session = createTestSession();
        TestInput input = (TestInput) session.getInput();
        input.setInputSize(1000, 500);
        assertEquals(1.0, session.getCurrentZoom());
        session.getBitmap(); // just for side effects
        assertEquals(1.0, session.getCurrentZoom());
    }
    
    @Test
    public void testInputFilePath() {
        BlimpSession session = newSession();
        assertNull(session.inputFilePath());

        TestInput input = new TestInput();
        input.setFilePath(null);
        session.setInput(input);
        assertNull(session.inputFilePath());
        
        input.setFilePath("path 1");
        assertEquals("path 1", session.inputFilePath());
    }
}