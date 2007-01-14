package org.boblycat.blimp.tests;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.boblycat.blimp.*;
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
	
	private Element parseXml(String xml)
	throws IOException, SAXException {
		DocumentBuilder builder = createDocBuilder();
		StringReader reader = new StringReader(xml);
		Document doc = builder.parse(new InputSource(reader));
		assertNotNull(doc);
		assertNotNull(doc.getFirstChild());
		return (Element) doc.getFirstChild();
	}
	
	private Element parseLayerXml(String xml)
	throws IOException, SAXException {
		Element root = parseXml(xml);
		assertEquals("layer", root.getNodeName());
		return root;
	}
	
	private Element findNamedChild(Element parent, String nodeName,
			String nameAttribute) {
		for (Node child: new DOMNodeIterator(parent, true)) {
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
	
	@Test
	public void testToXml() throws Exception {
		DummyLayer layer = new DummyLayer();
		layer.setIntValue(42);
		layer.setDoubleValue(-3.13);
		layer.setStringValue("abcDEF");
		layer.setEnumValue(DummyLayer.Enum.TWO);
		String xml = Serializer.layerToXml(layer);
		//System.out.println(xml);
		assertTrue(xml.length() > 0);

		Element root = parseLayerXml(xml);
		assertEquals("org.boblycat.blimp.tests.DummyLayer",
				root.getAttribute("class"));
		
		// The following is very strict: check the exact number of layer
		// properties.  If more properties are added to Layer in the future,
		// the test will have to be extended. 
		assertEquals(5, root.getChildNodes().getLength());
		
		Element child = (Element) root.getFirstChild();
		assertEquals("property", child.getNodeName());
		assertEquals("active", child.getAttribute("name"));
		assertNotNull(child.getFirstChild());
		Text textNode = (Text) child.getFirstChild();
		assertEquals("true", textNode.getNodeValue());
		
		child = (Element) child.getNextSibling();
		assertEquals("property", child.getNodeName());
		assertEquals("doubleValue", child.getAttribute("name"));
		assertNotNull(child.getFirstChild());
		textNode = (Text) child.getFirstChild();
		assertEquals("-3.13", textNode.getNodeValue());

		child = (Element) child.getNextSibling();
		assertEquals("property", child.getNodeName());
		assertEquals("enumValue", child.getAttribute("name"));
		assertNotNull(child.getFirstChild());
		textNode = (Text) child.getFirstChild();
		assertEquals("TWO", textNode.getNodeValue());

		child = (Element) child.getNextSibling();
		assertEquals("property", child.getNodeName());
		assertEquals("intValue", child.getAttribute("name"));
		assertNotNull(child.getFirstChild());
		textNode = (Text) child.getFirstChild();
		assertEquals("42", textNode.getNodeValue());

		child = (Element) child.getNextSibling();
		assertEquals("property", child.getNodeName());
		assertEquals("stringValue", child.getAttribute("name"));
		assertNotNull(child.getFirstChild());
		textNode = (Text) child.getFirstChild();
		assertEquals("abcDEF", textNode.getNodeValue());
	}
	
	@Test
	public void testFromXml() throws Exception {
		String xml =
			"<layer class=\"org.boblycat.blimp.tests.DummyLayer\">" +
			"  <property name=\"active\">false</property>" +
			"  <property name=\"intValue\">-33</property>" +
			"  <property name=\"stringValue\">Ouagadougou</property>" +
			"  <property name=\"enumValue\">THREE</property>" +
			"  <property name=\"doubleValue\">42.43</property>" +
			"</layer>";
		Layer layer = Serializer.layerFromXml(xml);
		assertNotNull(layer);
		assertTrue(layer instanceof DummyLayer);
		DummyLayer dummyLayer = (DummyLayer) layer;
		assertEquals(false, dummyLayer.isActive());
		assertEquals(-33, dummyLayer.getIntValue());
		assertEquals("Ouagadougou", dummyLayer.getStringValue());
		assertEquals(DummyLayer.Enum.THREE, dummyLayer.getEnumValue());
		assertEquals(42.43, dummyLayer.getDoubleValue());
	}
	
	@Test
	public void testClone() {
		DummyLayer layer = new DummyLayer();
		layer.setIntValue(-4231);
		layer.setStringValue("A 'string' value!");
		BlimpBean copy = layer.clone();
		assertEquals(DummyLayer.class, copy.getClass());
		DummyLayer layerCopy = (DummyLayer) copy;
		assertEquals(-4231, layerCopy.getIntValue());
		assertEquals("A 'string' value!", layerCopy.getStringValue());
	}
	
	@Test
	public void testFromXmlWithWhiteSpace() throws Exception {
		String xml =
			"<layer class=\"org.boblycat.blimp.tests.DummyLayer\">" +
			"  <property name=\"stringValue\">    </property>" +
			"</layer>";
		Layer layer = Serializer.layerFromXml(xml);
		assertNotNull(layer);
		assertTrue(layer instanceof DummyLayer);
		DummyLayer dummyLayer = (DummyLayer) layer;
		assertEquals("    ", dummyLayer.getStringValue());
	}
	
	@Test
	public void testCurvesToXml() throws Exception {
		CurvesLayer layer = new CurvesLayer();
		PointDouble[] points = {
				new PointDouble(0.0, 0.1),
				new PointDouble(0.2, 0.3),
				new PointDouble(1.0, 0.5),
				};
		layer.setPoints(points);
		String xml = Serializer.layerToXml(layer);
		//System.out.println(xml);
		Element root = parseLayerXml(xml);
		assertEquals("org.boblycat.blimp.CurvesLayer", root.getAttribute("class"));
		Element property = findNamedChild(root, "property", "points");
		assertNotNull(property);
		int i = 0;
		for (Node child: new DOMNodeIterator(property, true)) {
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
			"<layer class=\"org.boblycat.blimp.CurvesLayer\">" +
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
		for (Node child: new DOMNodeIterator(layerElement)) {
			if (!(child instanceof Element))
				continue;
			Element element = (Element) child;
			if (!(element.getNodeName().equals("property") &&
					propertyName.equals(element.getAttribute("name"))))
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
		DummyInput input = new DummyInput();
		input.setPath("dummy/path");
		session.setInput(input);
		DummyLayer layer = new DummyLayer();
		layer.setIntValue(54);
		session.addLayer(layer);
		String xml = Serializer.layerToXml(session);
		//System.out.println(xml);
		Element root = parseXml(xml);
		assertEquals("session", root.getNodeName());
		
		Element child = (Element) root.getFirstChild();

		// skip all properties
		while (child != null && child.getNodeName().equals("property"))
			child = (Element) child.getNextSibling();
		
		assertNotNull(child);
		assertEquals("layer", child.getNodeName());
		assertEquals("org.boblycat.blimp.tests.DummyInput",
				child.getAttribute("class"));
		assertHasPropertyChild(child, "path", "dummy/path");
		
		child = (Element) child.getNextSibling();
		assertNotNull(child);
		assertEquals("layer", child.getNodeName());
		assertEquals("org.boblycat.blimp.tests.DummyLayer",
				child.getAttribute("class"));
		assertHasPropertyChild(child, "intValue", "54");
	}
	
	@Test
	public void testSessionFromXml() throws Exception {
		String xml =
			"<session class=\"org.boblycat.blimp.BlimpSession\">" +
			"  <layer class=\"org.boblycat.blimp.tests.DummyInput\">" +
			"    <property name=\"path\">some.path</property>" +
			"  </layer>" +
			"  <layer class=\"org.boblycat.blimp.tests.DummyLayer\">" +
			"    <property name=\"stringValue\">Some string value</property>" +
			"  </layer>" +
			"</session>";
		BlimpSession session = (BlimpSession) Serializer.layerFromXml(xml);
		assertNotNull(session);
		assertEquals(2, session.layerCount());
		
		DummyInput input = (DummyInput) session.getLayer(0);
		assertNotNull(input);
		assertEquals("some.path", input.getPath());
		assertTrue(input == session.getInput());
		
		DummyLayer layer = (DummyLayer) session.getLayer(1);
		assertNotNull(layer);
		assertEquals("Some string value", layer.getStringValue());
	}
	
	@Test
	public void testCloneSession() {
		BlimpSession session = new BlimpSession();
		DummyInput input = new DummyInput();
		input.setPath("a path");
		session.setInput(input);
		DummyLayer layer = new DummyLayer();
		layer.setDoubleValue(3.45);
		session.addLayer(layer);
		
		BlimpSession sessionClone = (BlimpSession) session.clone();
		assertNotNull(sessionClone);
		assertTrue(session != sessionClone);
		assertEquals(2, sessionClone.layerCount());
		
		DummyInput inputClone = (DummyInput) sessionClone.getLayer(0);
		assertNotNull(inputClone);
		assertTrue(input != inputClone);
		assertEquals("a path", inputClone.getPath());
		
		DummyLayer layerClone = (DummyLayer) sessionClone.getLayer(1);
		assertNotNull(layerClone);
		assertTrue(layer != layerClone);
		assertEquals(3.45, layerClone.getDoubleValue());
	}
}
