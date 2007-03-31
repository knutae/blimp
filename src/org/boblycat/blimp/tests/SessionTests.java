package org.boblycat.blimp.tests;

import java.io.IOException;

import org.boblycat.blimp.*;
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
        input1.setPath("input1 path");
        input1.setName("input1 name");
        session1.setInput(input1);
        TestLayer layer1 = new TestLayer();
        layer1.setStringValue("layer1 value");
        layer1.setName("layer1 name");
        session1.addLayer(layer1);
        
        session2.synchronizeSessionData(session1);
        TestInput input2 = (TestInput) session2.getInput();
        assertNotNull(input2);
        assertTrue(input1 != input2); // objects must be different
        assertEquals("input1 path", input2.getPath());
        assertEquals("input1 name", input2.getName());
        TestLayer layer2 = (TestLayer) session2.getLayer(1);
        assertNotNull(layer2);
        assertTrue(layer1 != layer2);
        assertEquals("layer1 value", layer2.getStringValue());
        assertEquals("layer1 name", layer2.getName());
        
        input2.setPath("input2 path");
        layer2.setStringValue("layer2 value");
        session1.synchronizeSessionData(session2);
        assertTrue(session1.getInput() == input1); // layer must be re-used
        assertEquals("input2 path", input1.getPath());
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
        
        ViewResizeLayer viewResize = new ViewResizeLayer(
                origWidth / 2, origHeight / 2, true);
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
}