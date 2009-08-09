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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.boblycat.blimp.Util;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.session.BlimpSession;
import org.boblycat.blimp.session.HistoryBlimpSession;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Static serialization and parsing utilities for Blimp objects.
 */
public class Serializer {
    private static DOMImplementation domImpl;
    private static DOMImplementationLS domImplLS;
    private static Document document;
    private static DocumentBuilder documentBuilder;
    private static SerializationRegistry registry;

    static {
        try {
            DOMImplementationRegistry reg = DOMImplementationRegistry
                    .newInstance();
            domImpl = reg.getDOMImplementation("XML 1.0 LS");
            domImplLS = (DOMImplementationLS) domImpl;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (domImplLS == null) {
            Util.err("FATAL: Failed to get DOMImplementationLS instance.");
            Util.err("Registry property: "
                    + DOMImplementationRegistry.PROPERTY);
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
            Util.err("FATAL: Failed to create DocumentBuilder instance.");
            System.exit(1);
        }
        registry = SerializationRegistry.createDefaultRegistry();
    }

    public static String propertyValueToString(Object value) {
        if (value == null)
            return "";
        Class<?> pClass = value.getClass();
        if (pClass == PointDouble.class)
            return ((PointDouble) value).toCommaString();
        return value.toString();
    }

    private static void appendPropertyValue(Element property, Object value) {
        if (value == null)
            return;
        if (value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); ++i) {
                Element subProperty = document.createElement("value");
                appendPropertyValue(subProperty, Array.get(value, i));
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
        return beanToDOM(layer);
    }

    public static Element beanToDOM(BlimpBean bean) {
        Element element = document.createElement(bean.elementName());
        String typeId = registry.getTypeId(bean.getClass());
        if (typeId != null)
            element.setAttribute("type", typeId);
        else if (!(bean instanceof BlimpSession))
            element.setAttribute("class", bean.getClass().getName());
        for (BlimpBean.Property p : bean) {
            if (!bean.isVisibleProperty(p.descriptor))
                continue;
            String name = p.getName();
            Element property = document.createElement("property");
            property.setAttribute("name", name);
            appendPropertyValue(property, p.getValue());
            element.appendChild(property);
        }
        if (bean.getChildren() != null)
            for (BlimpBean child : bean.getChildren())
                element.appendChild(beanToDOM(child));
        return element;
    }

    public static String layerToXml(Layer layer) {
        return beanToXml(layer);
    }

    public static String beanToXml(BlimpBean bean) {
        LSSerializer serializer = domImplLS.createLSSerializer();
        return serializer.writeToString(beanToDOM(bean));
    }

    public static String domToXml(Node node, boolean prettyPrint) {
        LSSerializer serializer = domImplLS.createLSSerializer();
        if (prettyPrint)
            serializer.getDomConfig().setParameter("format-pretty-print", true);
        return serializer.writeToString(node);
    }

    public static Layer layerFromXml(String xml) throws ClassNotFoundException,
            SAXException, IOException {
        // TODO: is it possible to get an IOException here?
        // If not, catch it inside this function.
        StringReader reader = new StringReader(xml);
        Document doc = documentBuilder.parse(new InputSource(reader));
        Node root = doc.getFirstChild();
        if ((root == null) || !(root instanceof Element))
            layerParseFailure("unknown XML parse error");
        return layerFromDOM((Element) root);
    }

    private static void beanParseFailure(String message) {
        // todo: create a new exception type
        throw new RuntimeException(message);
    }

    private static void layerParseFailure(String message) {
        // todo: create a new exception type
        throw new RuntimeException(message);
    }

    private static void beanParseWarning(String message) {
        Util.warn(message);
    }

    private static void layerParseWarning(String message) {
        beanParseWarning(message);
    }

    private static String getChildText(Element parent) {
        StringBuilder buf = new StringBuilder();
        for (Node child : new DOMNodeIterator(parent)) {
            if (child instanceof Text) {
                buf.append(child.getNodeValue());
            }
            else
                layerParseWarning("ignored unexpected non-text node");
        }
        return buf.toString();
    }

    public static Object parsePropertyValue(Class<?> propertyClass, String strValue)
            throws NumberFormatException, ColorRGB.SyntaxException {
        if (propertyClass == String.class)
            return strValue;
        else if (propertyClass == Integer.class
                || propertyClass == Integer.TYPE)
            return Integer.valueOf(strValue);
        else if (propertyClass == Boolean.class
                || propertyClass == Boolean.TYPE)
            return Boolean.valueOf(strValue);
        else if (propertyClass == Double.class || propertyClass == Double.TYPE)
            return Double.valueOf(strValue);
        else if (propertyClass.isEnum()) {
            if (strValue.length() == 0)
                return null;
            // return Enum.valueOf(propertyClass, strValue); // doesn't work
            // This works, but is potentially slow
            for (Object enumObj : propertyClass.getEnumConstants()) {
                if (strValue.equals(enumObj.toString()))
                    return enumObj;
            }
            layerParseWarning("Unknown enum value " + strValue);
            return null;
        }
        else if (propertyClass == PointDouble.class)
            return PointDouble.valueOfCommaString(strValue);
        else if (propertyClass == ColorRGB.class)
            return ColorRGB.parseColor(strValue);
        layerParseWarning("Unsupported property type "
                + propertyClass.getName());
        return null;
    }

    private static Object parsePropertyArrayValues(Element parent,
            Layer.Property prop) {
        Class<?> arrayClass = prop.getPropertyClass();
        Class<?> componentClass = arrayClass.getComponentType();
        // Collect array values in a list
        List<Object> values = new ArrayList<Object>();
        for (Node child : new DOMNodeIterator(parent, true)) {
            if (!(child instanceof Element)
                    || !child.getNodeName().equals("value")) {
                layerParseWarning("ignoring unrecognized child node under "
                        + prop.getName());
                continue;
            }
            String strValue = getChildText((Element) child);
            Object value = parsePropertyValue(componentClass, strValue);
            if (value != null)
                values.add(value);
        }
        // Convert the list to an array of the correct type
        Object arrayValues = Array.newInstance(componentClass, values.size());
        for (int i = 0; i < values.size(); i++) {
            Array.set(arrayValues, i, values.get(i));
        }
        return arrayValues;
    }

    private static Element getFirstElementChild(Element parent) {
        for (Node child : new DOMNodeIterator(parent)) {
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
            prop
                    .setValue(parsePropertyValue(prop.getPropertyClass(),
                            strValue));
        }
        else {
            layerParseWarning("unsupported child for property "
                    + prop.getName());
        }
    }

    static BlimpBean newBeanInstance(Class<? extends BlimpBean> beanClass) {
        try {
            return beanClass.newInstance();
        }
        catch (Exception e) {
            beanParseFailure(e.getMessage());
        }
        return null;
    }

    static BlimpBean newBeanInstance(String className,
            Class<? extends BlimpBean> baseClass) throws ClassNotFoundException {
        Class<?> beanClass = Class.forName(className);
        if (!baseClass.isAssignableFrom(beanClass))
            beanParseFailure("class not a subclass of " + baseClass.getName()
                    + ": " + className);
        try {
            return (BlimpBean) beanClass.newInstance();
        }
        catch (Exception e) {
            beanParseFailure(e.getMessage());
        }
        return null;

    }

    static Layer newLayerInstance(String className)
            throws ClassNotFoundException {
        return (Layer) newBeanInstance(className, Layer.class);
    }

    /**
     * Iterate through the children of the given node and copy the data, which
     * can include properties and child beans.
     *
     * @param beanNode
     *            The bean DOM node.
     * @param dest
     *            The destination bean.
     */
    private static void copyBeanChildrenFromDOM(Element beanNode, BlimpBean dest) {
        for (Node child : new DOMNodeIterator(beanNode, true)) {
            if (!(child instanceof Element)) {
                layerParseWarning("ignoring unrecognized child of type ("
                        + child.getClass() + ")");
                continue;
            }
            Element element = (Element) child;
            String nodeName = element.getNodeName();
            if (nodeName.equals("property")) {
                String propName = element.getAttribute("name");
                BlimpBean.Property prop = dest.findProperty(propName);
                if (prop == null)
                    beanParseWarning("property not found: " + propName);
                else
                    propertyValueFromDOM(element, prop);
            }
            else if (nodeName.equals("bean") || nodeName.equals("layer")) {
                try {
                    BlimpBean childBean = beanFromDOM(element);
                    dest.addChild(childBean);
                }
                catch (ClassNotFoundException e) {
                    beanParseWarning("failed to create child layer of class "
                            + element.getAttribute("class"));
                }
                catch (BlimpBean.NotImplementedException e) {
                    beanParseWarning("failed to add child layer of class "
                            + element.getAttribute("class"));
                }

            }
            else {
                beanParseWarning("ignoring unknown element " + nodeName);
            }
        }
    }

    public static BlimpBean beanFromDOM(Element beanNode)
            throws ClassNotFoundException {
        String typeId = beanNode.getAttribute("type");
        String className = beanNode.getAttribute("class");
        String nodeName = beanNode.getNodeName();
        BlimpBean bean = null;
        if (typeId.length() > 0) {
            Class<? extends BlimpBean> beanClass = registry.getBeanClass(typeId);
            if (beanClass == null)
                throw new ClassNotFoundException(
                        "Bean class with type id " + typeId + " not found.");
            bean = newBeanInstance(beanClass);
        }
        else if (className.length() > 0) {
            bean = newBeanInstance(className, BlimpBean.class);
        }
        else if (nodeName.equals("session")) {
            // special case for <session>, could make this more generic
            // in the future
            bean = new BlimpSession();
        }
        else {
            layerParseFailure("Failed to create bean " + nodeName);
        }
        if (bean == null)
            return null;
        if (!bean.elementName().equals(nodeName))
            beanParseWarning("element name '" + beanNode.getNodeName()
                    + "' differs from expected bean element '"
                    + bean.elementName() + "'");
        copyBeanChildrenFromDOM(beanNode, bean);
        return bean;
    }

    public static void copyBeanData(BlimpBean source, BlimpBean dest) {
        Element beanNode = beanToDOM(source);
        dest.removeAllChildren();
        copyBeanChildrenFromDOM(beanNode, dest);
    }

    public static Layer layerFromDOM(Element layerNode)
            throws ClassNotFoundException {
        return (Layer) beanFromDOM(layerNode);
    }

    /**
     * Save the bean in XML format to the given file.
     *
     * @param bean
     *            The bean to save.
     * @param filename
     *            File to save.
     */
    public static void saveBeanToFile(BlimpBean bean, String filename)
            throws IOException {
        FileWriter writer = new FileWriter(filename);
        try {
            LSOutput output = domImplLS.createLSOutput();
            output.setCharacterStream(writer);
            LSSerializer serializer = domImplLS.createLSSerializer();
            serializer.write(beanToDOM(bean), output);
            bean.beanSaved(filename);
        }
        finally {
            writer.close();
        }
    }

    /**
     * Load the bean from the given file, which must be in XML format.
     *
     * @param filename
     *            File to load.
     * @return A new bean object.
     * @throws FileNotFoundException
     *             If the file does not exist.
     * @throws SAXException
     *             If the XML parsing fails.
     * @throws IOException
     *             If a file exception occurs.
     * @throws ClassNotFoundException
     *             If the bean class is not known.
     */
    public static BlimpBean loadBeanFromFile(String filename)
            throws FileNotFoundException, SAXException, IOException,
            ClassNotFoundException {
        FileReader reader = new FileReader(filename);
        Document doc = documentBuilder.parse(new InputSource(reader));
        Node root = doc.getFirstChild();
        if ((root == null) || !(root instanceof Element))
            layerParseFailure("unknown XML parse error");
        BlimpBean bean = beanFromDOM((Element) root);
        if (bean != null)
            bean.beanLoaded(filename);
        return bean;
    }

    private static BlimpSession loadSessionFromFile(String filename,
            Class<? extends BlimpSession> sessionClass)
            throws FileNotFoundException, SAXException, IOException,
            ClassNotFoundException {
        BlimpSession session = (BlimpSession) newBeanInstance(sessionClass);
        if (session == null)
            return null;
        BlimpBean bean = loadBeanFromFile(filename);
        if (bean == null || !(bean instanceof BlimpSession))
            return null;
        copyBeanData(bean, session);
        return session;
    }

    /**
     * Load a history-enabled session from file.
     * @param filename
     *  File to load.
     * @return
     *  A new history session object.
     * @throws FileNotFoundException
     *  If the file does not exist.
     * @throws SAXException
     *  If the XML parsing fails.
     * @throws IOException
     *  If a file exception occurs.
     * @throws ClassNotFoundException
     *  If the file references an unknown class.
     */
    public static HistoryBlimpSession loadHistorySessionFromFile(String filename)
            throws FileNotFoundException, SAXException, IOException,
            ClassNotFoundException {
        HistoryBlimpSession session = (HistoryBlimpSession)
            loadSessionFromFile(filename, HistoryBlimpSession.class);
        if (session != null) {
            BlimpBean bean = session;
            bean.beanLoaded(filename);
        }
        return session;
    }
}
