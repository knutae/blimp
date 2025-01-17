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
package org.boblycat.blimp.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.boblycat.blimp.data.ColorDepth;
import org.boblycat.blimp.data.ColorRGB;
import org.boblycat.blimp.data.PointDouble;
import org.boblycat.blimp.event.LayerChangeListener;
import org.boblycat.blimp.event.LayerEvent;
import org.boblycat.blimp.io.BlimpBean;
import org.boblycat.blimp.io.DOMNodeIterator;
import org.boblycat.blimp.io.Serializer;
import org.boblycat.blimp.layers.*;
import org.boblycat.blimp.layers.RawFileInputLayer.WhiteBalance;
import org.boblycat.blimp.session.BlimpSession;
import org.boblycat.blimp.session.HistoryBlimpSession;
import org.junit.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;
import static org.boblycat.blimp.tests.Assert.*;

public class SerializationTests {
    int eventCount;

    private static DocumentBuilder createDocBuilder() {
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

    private static Element parseXml(String xml)
    throws IOException, SAXException {
        DocumentBuilder builder = createDocBuilder();
        StringReader reader = new StringReader(xml);
        Document doc = builder.parse(new InputSource(reader));
        assertNotNull(doc);
        assertNotNull(doc.getFirstChild());
        return (Element) doc.getFirstChild();
    }

    private static Element parseLayerXml(String xml)
    throws IOException, SAXException {
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

    private static String getChildValue(Element element) {
        assertNotNull(element);
        assertNotNull(element.getFirstChild());
        assertTrue(element.getFirstChild() instanceof Text);
        Text textNode = (Text) element.getFirstChild();
        assertNull(textNode.getNextSibling());
        return textNode.getNodeValue();
    }

    private static void assertChildValueEquals(Element element, String expected) {
        assertEquals(expected, getChildValue(element));
    }

    @Test
    public void testToXml() throws Exception {
        TestLayer layer = new TestLayer();
        layer.setIntValue(42);
        layer.setDoubleValue(-3.13);
        layer.setStringValue("abcDEF");
        layer.setEnumValue(TestLayer.Enum.TWO);
        layer.setDoubleArrayValue(new double[] { -0.123, 4.567 });
        layer.setColorValue(new ColorRGB(0, 100, 255));
        String xml = Serializer.layerToXml(layer);
        assertTrue(xml.length() > 0);

        Element root = parseLayerXml(xml);
        assertEquals("org.boblycat.blimp.layers.TestLayer", root
                .getAttribute("class"));

        assertTrue(root.getChildNodes().getLength() >= 5);

        Element activeElement = null;
        Element doubleValueElement = null;
        Element enumValueElement = null;
        Element intValueElement = null;
        Element stringValueElement = null;
        Element doubleArrayValueElement = null;
        Element colorValueElement = null;

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
            else if (nameAttr.equals("doubleArrayValue"))
                doubleArrayValueElement = child;
            else if (nameAttr.equals("colorValue"))
                colorValueElement = child;
        }

        assertChildValueEquals(activeElement, "true");
        assertChildValueEquals(doubleValueElement, "-3.13");
        assertChildValueEquals(enumValueElement, "TWO");
        assertChildValueEquals(intValueElement, "42");
        assertChildValueEquals(stringValueElement, "abcDEF");
        assertChildValueEquals(colorValueElement, "#0064FF");

        // array value
        assertNotNull(doubleArrayValueElement);
        Node child = doubleArrayValueElement.getFirstChild();
        assertEquals("value", child.getNodeName());
        assertChildValueEquals((Element) child, "-0.123");
        child = child.getNextSibling();
        assertEquals("value", child.getNodeName());
        assertChildValueEquals((Element) child, "4.567");
        assertNull(child.getNextSibling());
    }

    @Test
    public void testFromXml() throws Exception {
        String xml =
            "<layer class=\"org.boblycat.blimp.layers.TestLayer\">" +
            "  <property name=\"active\">false</property>" +
            "  <property name=\"intValue\">-33</property>" +
            "  <property name=\"stringValue\">Ouagadougou</property>" +
            "  <property name=\"enumValue\">THREE</property>" +
            "  <property name=\"doubleValue\">42.43</property>" +
            "  <property name=\"doubleArrayValue\">" +
            "    <value>43.2</value>" +
            "    <value>-23.4</value>" +
            "  </property>" +
            "  <property name=\"colorValue\">#FF0580</property>" +
            "</layer>";
        Layer layer = Serializer.layerFromXml(xml);
        assertNotNull(layer);
        assertTrue(layer instanceof TestLayer);
        TestLayer dummyLayer = (TestLayer) layer;
        assertEquals(false, dummyLayer.isActive());
        assertEquals(-33, dummyLayer.getIntValue());
        assertEquals("Ouagadougou", dummyLayer.getStringValue());
        assertEquals(TestLayer.Enum.THREE, dummyLayer.getEnumValue());
        assertEqualsD(42.43, dummyLayer.getDoubleValue());
        double[] array = dummyLayer.getDoubleArrayValue();
        assertNotNull(array);
        assertEquals(2, array.length);
        assertEqualsD(43.2, array[0]);
        assertEqualsD(-23.4, array[1]);
        ColorRGB color = dummyLayer.getColorValue();
        assertNotNull(color);
        assertEquals(255, color.getRed());
        assertEquals(5, color.getGreen());
        assertEquals(128, color.getBlue());
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
        String xml =
            "<layer class=\"org.boblycat.blimp.layers.TestLayer\">" +
            "  <property name=\"stringValue\">    </property>" +
            "</layer>";
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
        Element root = parseLayerXml(xml);
        assertEquals("Curves", root.getAttribute("type"));
        //assertEquals("", root.getAttribute("class"));
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
        String xml =
            "<layer type=\"Curves\">" +
            "  <property name=\"points\">" +
            "    <value>0.1,1.0</value>" +
            "    <value>0.5,0.98</value>" +
            "    <value>0.7,0.5</value>" +
            "    <value>1.0,0.34</value>" +
            "  </property>" +
            "</layer>";
        Layer layer = Serializer.layerFromXml(xml);
        assertNotNull(layer);
        assertTrue(layer instanceof CurvesLayer);
        CurvesLayer curves = (CurvesLayer) layer;
        PointDouble[] points = curves.getPoints();
        assertEquals(4, points.length);

        assertEqualsD(0.1, points[0].x);
        assertEqualsD(1.0, points[0].y);

        assertEqualsD(0.5, points[1].x);
        assertEqualsD(0.98, points[1].y);

        assertEqualsD(0.7, points[2].x);
        assertEqualsD(0.5, points[2].y);

        assertEqualsD(1.0, points[3].x);
        assertEqualsD(0.34, points[3].y);
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
        input.setFilePath("dummy/path");
        session.setInput(input);
        TestLayer layer = new TestLayer();
        layer.setIntValue(54);
        session.addLayer(layer);
        String xml = Serializer.layerToXml(session);
        Element root = parseXml(xml);
        assertEquals("session", root.getNodeName());
        assertEquals("", root.getAttribute("class"));

        Element child = (Element) root.getFirstChild();

        // skip all properties
        while (child != null && child.getNodeName().equals("property"))
            child = (Element) child.getNextSibling();

        assertNotNull(child);
        assertEquals("layer", child.getNodeName());
        assertEquals("org.boblycat.blimp.layers.TestInput", child
                .getAttribute("class"));
        assertHasPropertyChild(child, "filePath", "dummy/path");

        child = (Element) child.getNextSibling();
        assertNotNull(child);
        assertEquals("layer", child.getNodeName());
        assertEquals("org.boblycat.blimp.layers.TestLayer", child
                .getAttribute("class"));
        assertHasPropertyChild(child, "intValue", "54");
    }

    @Test
    public void testSessionFromXml() throws Exception {
        String xml =
            "<session>" +
            "  <layer class=\"org.boblycat.blimp.layers.TestInput\">" +
            "    <property name=\"filePath\">some.path</property>" +
            "  </layer>" +
            "  <layer class=\"org.boblycat.blimp.layers.TestLayer\">" +
            "    <property name=\"stringValue\">Some string value</property>" +
            "  </layer>" +
            "</session>";
        BlimpSession session = (BlimpSession) Serializer.layerFromXml(xml);
        assertNotNull(session);
        assertEquals(2, session.layerCount());

        TestInput input = (TestInput) session.getLayer(0);
        assertNotNull(input);
        assertEquals("some.path", input.getFilePath());
        assertTrue(input == session.getInput());

        TestLayer layer = (TestLayer) session.getLayer(1);
        assertNotNull(layer);
        assertEquals("Some string value", layer.getStringValue());
    }

    @Test
    public void testCloneSession() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setFilePath("a path");
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
        assertEquals("a path", inputClone.getFilePath());

        TestLayer layerClone = (TestLayer) sessionClone.getLayer(1);
        assertNotNull(layerClone);
        assertTrue(layer != layerClone);
        assertEqualsD(3.45, layerClone.getDoubleValue());
    }

    private static void checkTypeIdXml(Class <? extends Layer> layerClass,
            String typeId) throws Exception {
        // from xml
        String xml = "<layer type=\"" + typeId + "\"/>";
        Layer layer = Serializer.layerFromXml(xml);
        assertNotNull(layer);
        assertSame(layer.getClass(), layerClass);
        // to xml
        Element root = parseLayerXml(Serializer.layerToXml(layer));
        assertEquals(typeId, root.getAttribute("type"));
        assertEquals("", root.getAttribute("class"));
    }

    @Test
    public void testLayerTypeIds() throws Exception {
        checkTypeIdXml(InvertLayer.class, "Invert");
        checkTypeIdXml(BrightnessContrastLayer.class, "BrightnessContrast");
        checkTypeIdXml(CurvesLayer.class, "Curves");
        checkTypeIdXml(SaturationLayer.class, "HueSaturationLightness");
        checkTypeIdXml(GammaLayer.class, "Gamma");
        checkTypeIdXml(GrayscaleMixerLayer.class, "GrayscaleMixer");
        checkTypeIdXml(ResizeLayer.class, "Resize");
        checkTypeIdXml(UnsharpMaskLayer.class, "UnsharpMask");
        checkTypeIdXml(LocalContrastLayer.class, "LocalContrast");
        checkTypeIdXml(CropLayer.class, "Crop");
        checkTypeIdXml(LevelsLayer.class, "Levels");
        checkTypeIdXml(OrientationLayer.class, "Orientation");
        checkTypeIdXml(SolidColorBorderLayer.class, "Border");
        checkTypeIdXml(Color16BitLayer.class, "Promote16Bit");
        checkTypeIdXml(RawFileInputLayer.class, "RawInput");
        checkTypeIdXml(SimpleFileInputLayer.class, "FileInput");
    }

    private static Element findPropertyElement(Element root,
            String propertyName) {
        Element foundChild = null;
        int foundCount = 0;
        for (Node node: new DOMNodeIterator(root)) {
            assertTrue(node instanceof Element);
            Element child = (Element) node;
            assertEquals("property", child.getNodeName());
            String nameAttr = child.getAttribute("name");
            if (nameAttr.equals(propertyName)) {
                foundChild = child;
                foundCount++;
            }
        }
        assertTrue(foundCount <= 1);
        return foundChild;
    }

    private static void checkSinglePropertyElement(Element root,
            String propertyName, String expectedValue) {
        Element propertyChild = findPropertyElement(root, propertyName);
        assertChildValueEquals(propertyChild, expectedValue);
    }

    @Test
    public void testRawFileInputLayerToXmlWithCustomWB() throws Exception {
        RawFileInputLayer layer = new RawFileInputLayer();
        layer.setFilePath("/foo/bar.raw");
        layer.setColorDepth(ColorDepth.Depth16Bit);
        layer.setWhiteBalance(WhiteBalance.CustomRaw);
        layer.setRawWhiteBalance(new double[] {1.23, 0.5, 0.78, 0.0});

        String xml = Serializer.layerToXml(layer);
        Element root = parseLayerXml(xml);
        checkSinglePropertyElement(root, "filePath", "/foo/bar.raw");
        checkSinglePropertyElement(root, "colorDepth", "Depth16Bit");
        checkSinglePropertyElement(root, "whiteBalance", "CustomRaw");

        Element rawWhiteBalanceElement = findPropertyElement(
                root, "rawWhiteBalance");
        assertNotNull(rawWhiteBalanceElement);
        List<String> childValues = new ArrayList<String>();
        for (Node node: new DOMNodeIterator(rawWhiteBalanceElement)) {
            assertTrue(node instanceof Element);
            Element child = (Element) node;
            assertEquals("value", child.getNodeName());
            childValues.add(getChildValue(child));
        }
        assertEquals(4, childValues.size());
        assertEquals("1.23", childValues.get(0));
        assertEquals("0.5", childValues.get(1));
        assertEquals("0.78", childValues.get(2));
        assertEquals("0.0", childValues.get(3));
    }

    @Test
    public void testRawFileInputLayerToXmlWithCameraWB() throws Exception {
        RawFileInputLayer layer = new RawFileInputLayer();
        layer.setFilePath("c:\\photos\\img_1234.raw");
        layer.setColorDepth(ColorDepth.Depth8Bit);
        layer.setWhiteBalance(WhiteBalance.Camera);

        String xml = Serializer.layerToXml(layer);
        Element root = parseLayerXml(xml);
        checkSinglePropertyElement(root, "filePath", "c:\\photos\\img_1234.raw");
        checkSinglePropertyElement(root, "colorDepth", "Depth8Bit");
        checkSinglePropertyElement(root, "whiteBalance", "Camera");

        Element rawWhiteBalanceElement = findPropertyElement(
                root, "rawWhiteBalance");
        assertNull(rawWhiteBalanceElement);
    }

    @Test
    public void testProjectFilePathAfterLoadFromFile() throws Exception {
        File temp = File.createTempFile("projectTest", ".blimp");
        temp.deleteOnExit();
        FileWriter out = new FileWriter(temp);
        out.write("<session/>");
        out.close();
        BlimpSession session =
            (BlimpSession) Serializer.loadBeanFromFile(temp.getAbsolutePath());
        assertNotNull(session);
        assertNotNull(session.getProjectFilePath());
        assertEquals(temp.getAbsolutePath(), session.getProjectFilePath());
    }

    @Test
    public void testProjectFilePathAfterLoadHistorySessionFromFile()
    throws Exception {
        File temp = File.createTempFile("projectTest", ".blimp");
        temp.deleteOnExit();
        FileWriter out = new FileWriter(temp);
        out.write("<session/>");
        out.close();
        HistoryBlimpSession session =
            Serializer.loadHistorySessionFromFile(temp.getAbsolutePath());
        assertNotNull(session);
        assertNotNull(session.getProjectFilePath());
        assertEquals(temp.getAbsolutePath(), session.getProjectFilePath());
    }

    @Test
    public void testProjectFilePathAfterSaveToFile() throws Exception {
        File temp = File.createTempFile("projectTest", ".blimp");
        temp.deleteOnExit();
        BlimpSession session = new BlimpSession();
        assertNull(session.getProjectFilePath());
        Serializer.saveBeanToFile(session, temp.getAbsolutePath());
        assertNotNull(session.getProjectFilePath());
        assertEquals(temp.getAbsolutePath(), session.getProjectFilePath());
    }

    @Test
    public void testLoadHistorySessionFromFile() throws Exception {
        File temp = File.createTempFile("projectTest", ".blimp");
        temp.deleteOnExit();
        String xml =
            "<session>" +
            "  <layer class=\"org.boblycat.blimp.layers.TestInput\">" +
            "    <property name=\"filePath\">initial path</property>" +
            "  </layer>" +
            "</session>";
        FileWriter out = new FileWriter(temp);
        out.write(xml);
        out.close();

        HistoryBlimpSession session = Serializer.loadHistorySessionFromFile(
                temp.getAbsolutePath());
        assertNotNull(session);
        assertFalse(session.isDirty());
        TestInput input = (TestInput) session.getInput();
        assertNotNull(input);
        assertEquals("initial path", input.getFilePath());

        eventCount = 0;
        session.addChangeListener(new LayerChangeListener () {
            public void handleChange(LayerEvent e) {
                eventCount++;
            }
        });

        input.setFilePath("new path");
        input.invalidate();
        assertEquals(1, eventCount);
        assertNotNull(session.getHistory());
        assertTrue(session.getHistory().canUndo());

        session.undo();
        assertEquals("initial path", input.getFilePath());
        assertFalse(session.getHistory().canUndo());
    }

    @Test
    public void testNoUndoAfterLoadFromFile() throws Exception {
        File temp = File.createTempFile("projectTest", ".blimp");
        temp.deleteOnExit();
        String xml =
            "<session>" +
            "    <property name='active'>true</property>" +
            "    <property name='name'>test_6530</property>" +
            "    <property name='previewQuality'>Accurate</property>" +
            "    <layer type='RawInput'>" +
            "        <property name='active'>true</property>" +
            "        <property name='colorDepth'>Depth16Bit</property>" +
            "        <property name='colorSpace'>sRGB</property>" +
            "        <property name='filePath'>C:\\Users\\knute\\Pictures\\IMG_6530.CR2</property>" +
            "        <property name='name'>RawFileInput1</property>" +
            "        <property name='quality'>HalfSize</property>" +
            "        <property name='whiteBalance'>Auto</property>" +
            "    </layer>" +
            "    <layer type='Gamma'>" +
            "        <property name='active'>true</property>" +
            "        <property name='gamma'>2.2</property>" +
            "        <property name='name'>Gamma1</property>" +
            "    </layer>" +
            "    <layer type='LocalContrast'>" +
            "        <property name='active'>true</property>" +
            "        <property name='adaptive'>70</property>" +
            "        <property name='amount'>100</property>" +
            "        <property name='name'>LocalContrast1</property>" +
            "        <property name='radius'>100</property>" +
            "    </layer>" +
            "</session>";
        FileWriter out = new FileWriter(temp);
        out.write(xml);
        out.close();

        HistoryBlimpSession session = Serializer.loadHistorySessionFromFile(
                temp.getAbsolutePath());
        assertNotNull(session);
        assertEquals(3, session.layerCount());
        assertFalse(session.isDirty());
        assertFalse(session.getHistory().canUndo());
        assertFalse(session.getHistory().canRedo());

        session.getHistory().undo();
        assertEquals(3, session.layerCount());
        assertFalse(session.isDirty());
        assertFalse(session.getHistory().canUndo());
        assertFalse(session.getHistory().canRedo());
    }

    @Test
    public void testProjectFilePathAfterLoadFromFileAndUndo() throws Exception {
        File temp = File.createTempFile("projectTest", ".blimp");
        temp.deleteOnExit();
        String xml =
            "<session>" +
            "  <layer class=\"org.boblycat.blimp.layers.TestInput\">" +
            "    <property name=\"filePath\">initial path</property>" +
            "  </layer>" +
            "</session>";
        FileWriter out = new FileWriter(temp);
        out.write(xml);
        out.close();

        HistoryBlimpSession session = Serializer.loadHistorySessionFromFile(
                temp.getAbsolutePath());
        assertEquals(temp.getAbsolutePath(), session.getProjectFilePath());

        // change a value and record history
        TestInput input = (TestInput) session.getInput();
        input.invalidate();
        assertEquals("initial path", input.getFilePath());
        input.setFilePath("new path");
        input.invalidate();
        assertEquals(temp.getAbsolutePath(), session.getProjectFilePath());

        // undo
        session.undo();
        assertEquals("initial path", input.getFilePath());
        assertEquals(temp.getAbsolutePath(), session.getProjectFilePath());
    }
    
    @Test
    public void copyBeanDataProperties() {
        TestLayer layer1 = new TestLayer();
        layer1.setActive(false);
        layer1.setColorValue(ColorRGB.Black);
        layer1.setDoubleValue(4.2);
        layer1.setEnumValue(TestLayer.Enum.TWO);
        layer1.setIntValue(-43);
        layer1.setName("layer1");
        layer1.setStringValue("Text Value");
        layer1.setDoubleArrayValue(new double[] { 1, 2, 3 });
        
        TestLayer layer2 = new TestLayer();
        Serializer.copyBeanData(layer1, layer2);
        
        assertEquals(false, layer2.isActive());
        assertEquals(ColorRGB.Black, layer2.getColorValue());
        assertEqualsD(4.2, layer2.getDoubleValue());
        assertEquals(TestLayer.Enum.TWO, layer2.getEnumValue());
        assertEquals(-43, layer2.getIntValue());
        assertEquals("layer1", layer2.getName());
        assertEquals("Text Value", layer2.getStringValue());
        double[] arr = layer2.getDoubleArrayValue();
        assertEquals(3, arr.length);
        assertEqualsD(1.0, arr[0]);
        assertEqualsD(2.0, arr[1]);
        assertEqualsD(3.0, arr[2]);
    }
    
    private static void addTestLayer(BlimpSession session, String name, String stringValue) {
        TestLayer layer = new TestLayer();
        layer.setName(name);
        layer.setStringValue(stringValue);
        session.addLayer(layer);
    }
    
    private static void checkCopiedSessionData(BlimpSession session) {
        assertEquals(3, session.layerCount());
        assertEquals("input1", session.getLayer(0).getName());
        assertEquals("layer1", session.getLayer(1).getName());
        assertEquals("layer2", session.getLayer(2).getName());
    }
    
    @Test
    public void copyBeanDataSession() {
        BlimpSession session1 = new BlimpSession();
        session1.setName("session1");
        TestInput input = new TestInput();
        input.setName("input1");
        input.setFilePath("input path");
        session1.setInput(input);
        addTestLayer(session1, "layer1", "value1");
        addTestLayer(session1, "layer2", "value2");
        // sanity checks before copying
        checkCopiedSessionData(session1);
        
        BlimpSession session2 = new BlimpSession();
        
        // first copy (from nothing)
        Serializer.copyBeanData(session1, session2);
        checkCopiedSessionData(session2);

        // second copy (no changes)
        Serializer.copyBeanData(session1, session2);
        checkCopiedSessionData(session2);
        
        // third copy (with some changed data)
        session2.getInput().setName("NEW INPUT VALUE");
        session2.moveLayer(1, 2);
        assertEquals("layer2", session2.getLayer(1).getName());
        assertEquals("layer1", session2.getLayer(2).getName());
        Serializer.copyBeanData(session1, session2);
        checkCopiedSessionData(session2);
    }
}
