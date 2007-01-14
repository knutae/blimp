package org.boblycat.blimp.tests;

import org.boblycat.blimp.*;
import org.junit.*;
import static org.junit.Assert.*;

class TestBitmap extends Bitmap {
    String creator;
    String testValue;
}

class TestInput extends InputLayer {
    public Bitmap getBitmap() {
        TestBitmap bitmap = new TestBitmap();
        bitmap.creator = "TestSource";
        bitmap.testValue = "";
        return bitmap;
    }
    
    public String getDescription() {
        return "TestDescription";
    }
}

class TestLayer extends AdjustmentLayer {
	String suffix;
    TestLayer(String suffix) {
        this.suffix = suffix;
    }
    
    public Bitmap applyLayer(Bitmap source) {
        assertSame(source.getClass(), TestBitmap.class);
        TestBitmap oldBitmap = (TestBitmap) source;
        TestBitmap bitmap = new TestBitmap();
        bitmap.creator = "TestLayer";
        bitmap.testValue = oldBitmap.testValue + suffix;
        //bitmap.setImage(oldBitmap.getImage());
        return bitmap;
    }
    
    public String getDescription() {
        return "Test";
    }
}

public class SessionTests {
    int eventCount;
    LayerEvent lastLayerEvent;
    
    BlimpSession createTestSession() {
        BlimpSession session = new BlimpSession();
        session.setInput(new TestInput());
        return session;
    }
    
    TestBitmap getTestBitmap(BlimpSession session) {
        Bitmap bitmap = session.getBitmap();
        assertNotNull(bitmap);
        assertSame(bitmap.getClass(), TestBitmap.class);
        return (TestBitmap) bitmap;
    }
    
    @Test
    public void testNoLayers() {
        BlimpSession session = createTestSession();
        TestBitmap testBitmap = getTestBitmap(session);
        assertEquals("TestSource", testBitmap.creator);
        assertEquals("", testBitmap.testValue);
    }
    
    @Test
    public void testSingleLayer() {
        for (int i=0; i<4; i++) {
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
        assertEquals("TestSource", testBitmap.creator);
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
}