package org.boblycat.blimp;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;
import org.w3c.dom.ls.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Static serialization and parsing utilities for Blimp objects.
 */
public class Serializer {
	static private DOMImplementation domImpl;
	static private DOMImplementationLS domImplLS;
	static private Document document;
	static private DocumentBuilder documentBuilder;
	
	static {
		try {
			DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
			domImpl = reg.getDOMImplementation("XML 1.0 LS");
			domImplLS = (DOMImplementationLS) domImpl;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (domImplLS == null) {
			System.err.println("FATAL: Failed to get DOMImplementationLS instance.");
			System.err.println("Registry property: " + DOMImplementationRegistry.PROPERTY);
			System.exit(1);
		}
		document = domImpl.createDocument("", "BlimpLayer", null);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = factory.newDocumentBuilder();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (documentBuilder == null) {
			System.err.println("FATAL: Failed to create DocumentBuilder instance.");
			System.exit(1);
		}
	}
	
    public static String propertyValueToString(Object value) {
    	Class pClass = value.getClass();
    	if (pClass == PointDouble.class)
    		return ((PointDouble) value).toCommaString();
    	return value.toString();
    }
    
    private static void appendPropertyValue(Element property, Object value) {
    	if (value instanceof Object[]) {
    		Object[] array = (Object[]) value;
    		for (int i=0; i<array.length; ++i) {
    			Element subProperty = document.createElement("value");
    			appendPropertyValue(subProperty, array[i]);
    			property.appendChild(subProperty);
    		}
    	}
    	else {
    		String strValue = propertyValueToString(value);
    		Text textNode = document.createTextNode(strValue);
    		property.appendChild(textNode);
    	}
    }

    public static Element layerToDOM(Layer layer) {
		Element element = document.createElement("layer");
		//element.setAttribute("name", layer.getName());
		element.setAttribute("class", layer.getClass().getName());
		for (Layer.Property p: layer) {
			String name = p.getName();
			Element property = document.createElement("property");
			property.setAttribute("name", name);
			appendPropertyValue(property, p.getValue());
			element.appendChild(property);
		}
		return element;
	}
	
	public static String layerToXml(Layer layer) {
		LSSerializer serializer = domImplLS.createLSSerializer();
		return serializer.writeToString(layerToDOM(layer));
	}
	
	public static Layer layerFromXml(String xml)
	throws ClassNotFoundException, SAXException, IOException {
		// TODO: is it possible to get an IOException here?
		// If not, catch it inside this function.
		StringReader reader = new StringReader(xml);
		Document doc = documentBuilder.parse(new InputSource(reader));
		Node root = doc.getFirstChild();
		if ((root == null) || !(root instanceof Element))
			layerParseFailure("unknown XML parse error");
		return layerFromDOM((Element) root);
	}
	
	private static void layerParseFailure(String message) {
		// todo: create a new exception type
		throw new RuntimeException(message);
	}
	
	private static void layerParseWarning(String message) {
		System.err.println("Warning: " + message);
	}
	
	private static String getChildText(Element parent) {
		StringBuffer buf = new StringBuffer();
		for (Node child: new DOMNodeIterator(parent)) {
			if (child instanceof Text) {
				buf.append(child.getNodeValue());
			}
			else
				layerParseWarning("ignored unexpected non-text node");
		}
		return buf.toString();
	}
	
    public static Object parsePropertyValue(Class propertyClass, String strValue)
	throws NumberFormatException
	{
    	if (propertyClass == String.class)
			return strValue;
		else if (propertyClass == Integer.class || propertyClass == Integer.TYPE)
			return Integer.valueOf(strValue);
		else if (propertyClass == Boolean.class || propertyClass == Boolean.TYPE)
			return Boolean.valueOf(strValue);
		else if (propertyClass == PointDouble.class)
			return PointDouble.valueOfCommaString(strValue);
		layerParseWarning("Unsupported property type " + propertyClass.getName());
		return null;
	}

    private static Object parsePropertyArrayValues(Element parent,
    		Layer.Property prop) {
    	Class arrayClass = prop.getPropertyClass();
    	Class componentClass = arrayClass.getComponentType();
    	// Collect array values in a vector
    	Vector<Object> values = new Vector<Object>();
    	for (Node child: new DOMNodeIterator(parent, true)) {
    		if (!(child instanceof Element) ||
    				!child.getNodeName().equals("value")) {
    			layerParseWarning("ignoring unrecognized child node under "
    					+ prop.getName());
    			continue;
    		}
    		String strValue = getChildText((Element) child);
    		Object value = parsePropertyValue(componentClass, strValue);
    		if (value != null)
    			values.add(value);
    	}
    	// Convert the vector to an array of the correct type
    	Object arrayValues = Array.newInstance(componentClass, values.size());
    	for (int i=0; i<values.size(); i++) {
    		Array.set(arrayValues, i, values.get(i));
    	}
    	return arrayValues;
    }
    
    private static Element getFirstElementChild(Element parent) {
    	for (Node child: new DOMNodeIterator(parent)) {
    		if (child instanceof Element)
    			return (Element) child;
    	}
    	return null;
    }
	
	private static void propertyValueFromDOM(Element parent, Layer.Property prop) {
		if (getFirstElementChild(parent) != null) {
			// assume an array property
			Object value = parsePropertyArrayValues(parent, prop);
			prop.setValue(value);
			return;
		}
		Node child = parent.getFirstChild();
		if (child == null) {
			// just pass null
			prop.setValue(null);
		}
		else if (child instanceof Text) {
			// normal property
			String strValue = getChildText(parent);
			prop.setValue(parsePropertyValue(prop.getPropertyClass(), strValue));
		}
		else {
			layerParseWarning("unsupported child for property " + prop.getName());
		}
	}
	
	static Layer newLayerInstance(String className)
	throws ClassNotFoundException {
		Class layerClass = Class.forName(className);
		if (!Layer.class.isAssignableFrom(layerClass))
			layerParseFailure("layer class not a subclass of Layer: " + className);
		Layer newLayer = null;
		try {
			newLayer = (Layer) layerClass.newInstance();
		}
		catch (Exception e) {
			layerParseFailure(e.getMessage());
		}
		return newLayer;
	}
	
	public static Layer layerFromDOM(Element layerNode)
		throws ClassNotFoundException {
		if (!layerNode.getNodeName().equals("layer"))
			layerParseFailure("layer node must be named 'layer'");
		String className = layerNode.getAttribute("class");
		Layer layer = newLayerInstance(className);
		for (Node child: new DOMNodeIterator(layerNode, true)) {
			if (!(child instanceof Element)
					|| !child.getNodeName().equals("property")) {
				layerParseWarning("ignoring unrecognized child node ("
						+ child.getNodeName() + ")");
				continue;
			}
			Element propertyElement = (Element) child;
			String propName = propertyElement.getAttribute("name");
			Layer.Property prop = layer.findProperty(propName);
			if (prop == null)
				layerParseWarning("property not found: " + propName);
			else
				propertyValueFromDOM(propertyElement, prop);
			
		}
		return layer;
	}
}
