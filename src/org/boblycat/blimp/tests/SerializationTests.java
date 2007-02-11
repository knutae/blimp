package org.boblycat.blimp.tests;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.boblycat.blimp.*;
import org.boblycat.blimp.layers.CurvesLayer;
import org.boblycat.blimp.layers.Layer;
import org.junit.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

public class SerializationTests {
    private DocumentBuilder createDocBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(builder);
        return builder;
    }

    private Element parseXml(String xml) throws IOException, SAXException {
        DocumentBuilder builder = createDocBuilder();
        StringReader reader = new StringReader(xml);
        Document doc = builder.parse(new InputSource(reader));
        assertNotNull(doc);
        assertNotNull(doc.getFirstChild());
        return (Element) doc.getFirstChild();
    }

    private Element parseLayerXml(String xml) throws IOException, SAXException {
        Element root = parseXml(xml);
        assertEquals("layer", root.getNodeName());
        return root;
    }

    private Element findNamedChild(Element parent, String nodeName,
            String nameAttribute) {
        for (Node child : new DOMNodeIterator(parent, true)) {
            if (!(child instanceof Element))
                continue;
            Element el = (Element) child;
            if (!nodeName.equals(el.getNodeName()))
                continue;
            if (!nameAttribute.equals(el.getAttribute("name")))
                continue;
            return el;
        }
        return null;
    }
    
    private static void assertChildValueEquals(Element element, String expected) {
        assertNotNull(element);
        assertNotNull(element.getFirstChild());
        assertTrue(element.getFirstChild() instanceof Text);
        Text textNode = (Text) element.getFirstChild();
        assertEquals(expected, textNode.getNodeValue());
    }

    @Test
    public void testToXml() throws Exception {
        TestLayer layer = new TestLayer();
        layer.setIntValue(42);
        layer.setDoubleValue(-3.13);
        layer.setStringValue("abcDEF");
        layer.setEnumValue(TestLayer.Enum.TWO);
        String xml = Serializer.layerToXml(layer);
        // System.out.println(xml);
        assertTrue(xml.length() > 0);

        Element root = parseLayerXml(xml);
        assertEquals("org.boblycat.blimp.tests.TestLayer", root
                .getAttribute("class"));

        assertTrue(root.getChildNodes().getLength() >= 5);

        Element activeElement = null;
        Element doubleValueElement = null;
        Element enumValueElement = null;
        Element intValueElement = null;
        Element stringValueElement = null;
        
        for (Node node: new DOMNodeIterator(root)) {
            assertTrue(node instanceof Element);
            Element child = (Element) node;
            assertEquals("property", child.getNodeName());
            String nameAttr = child.getAttribute("name");
            
            if (nameAttr.equals("active"))
                activeElement = child;
            else if (nameAttr.equals("doubleValue"))
                doubleValueElement = child;
            else if (nameAttr.equals("enumValue"))
                enumValueElement = child;
            else if (nameAttr.equals("intValue"))
                intValueElement = child;
            else if (nameAttr.equals("stringValue"))
                stringValueElement = child;
        }

        assertChildValueEquals(activeElement, "true");
        assertChildValueEquals(doubleValueElement, "-3.13");
        assertChildValueEquals(enumValueElement, "TWO");
        assertChildValueEquals(intValueElement, "42");
        assertChildValueEquals(stringValueElement, "abcDEF");
    }

    @Test
    public void testFromXml() throws Exception {
        String xml = "<layer class=\"org.boblycat.blimp.tests.TestLayer\">"
                + "  <property name=\"active\">false</property>"
                + "  <property name=\"intValue\">-33</property>"
                + "  <property name=\"stringValue\">Ouagadougou</property>"
                + "  <property name=\"enumValue\">THREE</property>"
                + "  <property name=\"doubleValue\">42.43</property>"
                + "</layer>";
        Layer layer = Serializer.layerFromXml(xml);
        assertNotNull(layer);
        assertTrue(layer instanceof TestLayer);
        TestLayer dummyLayer = (TestLayer) layer;
        assertEquals(false, dummyLayer.isActive());
        assertEquals(-33, dummyLayer.getIntValue());
        assertEquals("Ouagadougou", dummyLayer.getStringValue());
        assertEquals(TestLayer.Enum.THREE, dummyLayer.getEnumValue());
        assertEquals(42.43, dummyLayer.getDoubleValue());
    }

    @Test
    public void testClone() {
        TestLayer layer = new TestLayer();
        layer.setIntValue(-4231);
        layer.setStringValue("A 'string' value!");
        BlimpBean copy = layer.clone();
        assertEquals(TestLayer.class, copy.getClass());
        TestLayer layerCopy = (TestLayer) copy;
        assertEquals(-4231, layerCopy.getIntValue());
        assertEquals("A 'string' value!", layerCopy.getStringValue());
    }

    @Test
    public void testFromXmlWithWhiteSpace() throws Exception {
        String xml = "<layer class=\"org.boblycat.blimp.tests.TestLayer\">"
                + "  <property name=\"stringValue\">    </property>"
                + "</layer>";
        Layer layer = Serializer.layerFromXml(xml);
        assertNotNull(layer);
        assertTrue(layer instanceof TestLayer);
        TestLayer dummyLayer = (TestLayer) layer;
        assertEquals("    ", dummyLayer.getStringValue());
    }

    @Test
    public void testCurvesToXml() throws Exception {
        CurvesLayer layer = new CurvesLayer();
        PointDouble[] points = { new PointDouble(0.0, 0.1),
                new PointDouble(0.2, 0.3), new PointDouble(1.0, 0.5), };
        layer.setPoints(points);
        String xml = Serializer.layerToXml(layer);
        // System.out.println(xml);
        Element root = parseLayerXml(xml);
        assertEquals("org.boblycat.blimp.layers.CurvesLayer", root
                .getAttribute("class"));
        Element property = findNamedChild(root, "property", "points");
        assertNotNull(property);
        int i = 0;
        for (Node child : new DOMNodeIterator(property, true)) {
            assertNotNull(child);
            assertEquals("value", child.getNodeName());
            Node subChild = child.getFirstChild();
            assertNotNull(subChild);
            assertTrue(subChild instanceof Text);
            if (i == 0)
                assertEquals("0.0,0.1", subChild.getNodeValue());
            else if (i == 1)
                assertEquals("0.2,0.3", subChild.getNodeValue());
            else if (i == 2)
                assertEquals("1.0,0.5", subChild.getNodeValue());
            i++;
        }
        assertEquals(3, i);
    }

    @Test
    public void testCurvesFromXml() throws Exception {
        String xml = "<layer class=\"org.boblycat.blimp.layers.CurvesLayer\">"
                + "  <property name=\"points\">" + "    <value>0.1,1.0</value>"
                + "    <value>0.5,0.98</value>" + "    <value>0.7,0.5</value>"
                + "    <value>1.0,0.34</value>" + "  </property>" + "</layer>";
        Layer layer = Serializer.layerFromXml(xml);
        assertNotNull(layer);
        assertTrue(layer instanceof CurvesLayer);
        CurvesLayer curves = (CurvesLayer) layer;
        PointDouble[] points = curves.getPoints();
        assertEquals(4, points.length);

        assertEquals(0.1, points[0].x);
        assertEquals(1.0, points[0].y);

        assertEquals(0.5, points[1].x);
        assertEquals(0.98, points[1].y);

        assertEquals(0.7, points[2].x);
        assertEquals(0.5, points[2].y);

        assertEquals(1.0, points[3].x);
        assertEquals(0.34, points[3].y);
    }

    static void assertHasPropertyChild(Element layerElement,
            String propertyName, String propertyValue) {
        int foundCount = 0;
        for (Node child : new DOMNodeIterator(layerElement)) {
            if (!(child instanceof Element))
                continue;
            Element element = (Element) child;
            if (!(element.getNodeName().equals("property") && propertyName
                    .equals(element.getAttribute("name"))))
                continue;
            // found the property
            foundCount++;
            assertTrue(element.getFirstChild() instanceof Text);
            Text text = (Text) element.getFirstChild();
            assertEquals(propertyValue, text.getNodeValue());
        }
        assertEquals(1, foundCount);
    }

    @Test
    public void testSessionToXml() throws Exception {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setPath("dummy/path");
        session.setInput(input);
        TestLayer layer = new TestLayer();
        layer.setIntValue(54);
        session.addLayer(layer);
        String xml = Serializer.layerToXml(session);
        // System.out.println(xml);
        Element root = parseXml(xml);
        assertEquals("session", root.getNodeName());

        Element child = (Element) root.getFirstChild();

        // skip all properties
        while (child != null && child.getNodeName().equals("property"))
            child = (Element) child.getNextSibling();

        assertNotNull(child);
        assertEquals("layer", child.getNodeName());
        assertEquals("org.boblycat.blimp.tests.TestInput", child
                .getAttribute("class"));
        assertHasPropertyChild(child, "path", "dummy/path");

        child = (Element) child.getNextSibling();
        assertNotNull(child);
        assertEquals("layer", child.getNodeName());
        assertEquals("org.boblycat.blimp.tests.TestLayer", child
                .getAttribute("class"));
        assertHasPropertyChild(child, "intValue", "54");
    }

    @Test
    public void testSessionFromXml() throws Exception {
        String xml = "<session class=\"org.boblycat.blimp.BlimpSession\">"
                + "  <layer class=\"org.boblycat.blimp.tests.TestInput\">"
                + "    <property name=\"path\">some.path</property>"
                + "  </layer>"
                + "  <layer class=\"org.boblycat.blimp.tests.TestLayer\">"
                + "    <property name=\"stringValue\">Some string value</property>"
                + "  </layer>" + "</session>";
        BlimpSession session = (BlimpSession) Serializer.layerFromXml(xml);
        assertNotNull(session);
        assertEquals(2, session.layerCount());

        TestInput input = (TestInput) session.getLayer(0);
        assertNotNull(input);
        assertEquals("some.path", input.getPath());
        assertTrue(input == session.getInput());

        TestLayer layer = (TestLayer) session.getLayer(1);
        assertNotNull(layer);
        assertEquals("Some string value", layer.getStringValue());
    }

    @Test
    public void testCloneSession() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setPath("a path");
        session.setInput(input);
        TestLayer layer = new TestLayer();
        layer.setDoubleValue(3.45);
        session.addLayer(layer);

        BlimpSession sessionClone = (BlimpSession) session.clone();
        assertNotNull(sessionClone);
        assertTrue(session != sessionClone);
        assertEquals(2, sessionClone.layerCount());

        TestInput inputClone = (TestInput) sessionClone.getLayer(0);
        assertNotNull(inputClone);
        assertTrue(input != inputClone);
        assertEquals("a path", inputClone.getPath());

        TestLayer layerClone = (TestLayer) sessionClone.getLayer(1);
        assertNotNull(layerClone);
        assertTrue(layer != layerClone);
        assertEquals(3.45, layerClone.getDoubleValue());
    }
}
