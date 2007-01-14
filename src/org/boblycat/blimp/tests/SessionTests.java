package org.boblycat.blimp.tests;

import org.boblycat.blimp.*;
import org.junit.*;
import static org.junit.Assert.*;

class TestBitmap extends Bitmap {
    String creator;
    int testValue;
}

class TestInput extends InputLayer {
    public Bitmap getBitmap() {
        TestBitmap bitmap = new TestBitmap();
        bitmap.creator = "TestSource";
        bitmap.testValue = 0;
        return bitmap;
    }
    
    public String getDescription() {
        return "TestDescription";
    }
}

class TestLayer extends AdjustmentLayer {
	int increment;
    TestLayer(int increment) {
        this.increment = increment;
    }
    
    public Bitmap applyLayer(Bitmap source) {
        assertSame(source.getClass(), TestBitmap.class);
        TestBitmap oldBitmap = (TestBitmap) source;
        TestBitmap bitmap = new TestBitmap();
        bitmap.creator = "TestLayer";
        bitmap.testValue = oldBitmap.testValue + increment;
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
        assertEquals(0, testBitmap.testValue);
    }
    
    @Test
    public void testSingleLayer() {
        for (int increment=0; increment<4; increment++) {
            BlimpSession session = createTestSession();
            assertEquals(0, session.layerCount());
            session.addLayer(0, new TestLayer(increment));
            assertEquals(1, session.layerCount());
            TestBitmap testBitmap = getTestBitmap(session);
            assertEquals("TestLayer", testBitmap.creator);
            assertEquals(increment, testBitmap.testValue);
        }
    }
    
    @Test
    public void testSingleInactiveLayer() {
        int increment = 5;
        BlimpSession session = createTestSession();
        assertEquals(0, session.layerCount());
        session.addLayer(new TestLayer(increment));
        assertEquals(1, session.layerCount());
        session.activateLayer(0, false);
        TestBitmap testBitmap = getTestBitmap(session);
        assertEquals("TestSource", testBitmap.creator);
        assertEquals(0, testBitmap.testValue);
    }
    
    @Test
    public void testMultipleLayers() {
        BlimpSession session = createTestSession();
        assertEquals(0, session.layerCount());
        session.addLayer(new TestLayer(1));
        session.addLayer(new TestLayer(2));
        session.addLayer(new TestLayer(4));
        assertEquals(3, session.layerCount());
        TestBitmap testBitmap = getTestBitmap(session);
        assertEquals("TestLayer", testBitmap.creator);
        assertEquals(1+2+4, testBitmap.testValue);
    }
    
    @Test
    public void testMultipleLayersInactive() {
        BlimpSession session = createTestSession();
        assertEquals(0, session.layerCount());
        session.addLayer(new TestLayer(1));
        session.addLayer(new TestLayer(2));
        session.addLayer(new TestLayer(4));
        assertEquals(3, session.layerCount());
        TestBitmap testBitmap;
        // all layers active
        testBitmap = getTestBitmap(session);
        assertEquals(1+2+4, testBitmap.testValue);
        // layer 0 inactive
        session.activateLayer(0, false);
        testBitmap = getTestBitmap(session);
        assertEquals(2+4, testBitmap.testValue);
        session.activateLayer(0, true);
        // layer 1 inactive
        session.activateLayer(1, false);
        testBitmap = getTestBitmap(session);
        assertEquals(1+4, testBitmap.testValue);
        session.activateLayer(1, true);
        // layer 0 inactive
        session.activateLayer(2, false);
        testBitmap = getTestBitmap(session);
        assertEquals(1+2, testBitmap.testValue);
        session.activateLayer(2, true);
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
        session.addLayer(new TestLayer(1));
        assertEquals(1, eventCount);
        session.addLayer(new TestLayer(2));
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
        Layer layer = new TestLayer(0);
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