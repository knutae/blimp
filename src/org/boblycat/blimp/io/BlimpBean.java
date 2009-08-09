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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.boblycat.blimp.Util;

/**
 * Base class for serializable Blimp objects.
 *
 * @author Knut Arild Erstad
 */
public abstract class BlimpBean implements Iterable<BlimpBean.Property> {
    /**
     * Wraps a BlimpBean property using JavaBeans functionality. BlimpBean
     * properties are accessible through the Iterable interface.
     */
    public class Property {
        PropertyDescriptor descriptor;
        BlimpBean bean;

        public String getName() {
            return descriptor.getName();
        }

        public Object getValue() {
            String errorMessage = null;
            try {
                Method reader = descriptor.getReadMethod();
                assert (reader != null);
                return reader.invoke(bean);
            }
            catch (InvocationTargetException e) {
                errorMessage = e.getMessage();
            }
            catch (IllegalAccessException e) {
                errorMessage = e.getMessage();
            }
            Util.err(errorMessage);
            Util.err("Failed to get value for property " + getName()
                    + ", class " + bean.getClass().getName());
            return null;
        }

        public void setValue(Object value) {
            String errorMessage = null;
            try {
                Method writer = descriptor.getWriteMethod();
                assert (writer != null);
                writer.invoke(bean, value);
                return;
            }
            catch (InvocationTargetException e) {
                errorMessage = e.getMessage();
            }
            catch (IllegalAccessException e) {
                errorMessage = e.getMessage();
            }
            Util.err(errorMessage);
            Util.err("Failed to set value for property " + getName()
                    + ", class " + bean.getClass().getName());
        }

        public Class<?> getPropertyClass() {
            Method writer = descriptor.getWriteMethod();
            return writer.getParameterTypes()[0];
        }

        private Property(BlimpBean bean, PropertyDescriptor descriptor) {
            this.bean = bean;
            this.descriptor = descriptor;
        }

        public PropertyDescriptor getDescriptor() {
            return descriptor;
        }
    }

    @SuppressWarnings("serial")
    public class NotImplementedException extends Exception {
    }

    /**
     * This function can be overridden to filter out properties that should not
     * be included when serializing the bean.  By default all read-write
     * properties are included, so the default implementation always returns
     * <code>true</code>.
     *
     * This function must always return the same result for the same property,
     * regardless of any internal state.  To filter out properties dynamically,
     * use <code>isVisibleProperty</code> instead.
     *
     * @param pd
     *            A property descriptor.
     * @return <code>true</code> by default.
     */
    protected boolean isSerializableProperty(PropertyDescriptor pd) {
        return true;
    }

    /**
     * This function can be overridden to dynamically hide some properties
     * for property editors or in the XML representation.  For instance,
     * setting a certain boolean property could cause another property to
     * become hidden.
     *
     * The default implementation always returns <code>true</code>, which
     * means that no properties are hidden.
     *
     * @param pd
     *            A property descriptor.
     * @return <code>true</code> by default.
     */
    public boolean isVisibleProperty(PropertyDescriptor pd) {
        return true;
    }

    /**
     * Iterator for BlimpBean properties. This includes all JavaBean properties
     * defined in subclasses of BlimpBean which are both readable and writable.
     *
     * @return An iterator for this bean's properties.
     */
    public Iterator<Property> iterator() {
        List<Property> props = new ArrayList<Property>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass(),
                    BlimpBean.class);
            PropertyDescriptor[] propertyDesc = beanInfo
                    .getPropertyDescriptors();
            for (int i = 0; i < propertyDesc.length; i++) {
                PropertyDescriptor pd = propertyDesc[i];
                if (pd.getReadMethod() == null || pd.getWriteMethod() == null)
                    // skip properties without both read and write access
                    continue;
                if (!isSerializableProperty(pd))
                    continue;
                // TODO: skip unsupported types?
                props.add(new Property(this, pd));
            }
        }
        catch (IntrospectionException e) {
            Util.err("Failed to get property descriptors for class "
                    + this.getClass().getName());
            e.printStackTrace();
            return null;
        }
        return props.iterator();
    }

    /**
     * Find a BlimpBean property by name. This function currently has linear
     * time complexity.
     *
     * @param name
     *            The name of the (JavaBeans) property to search for.
     * @return A property with the given name, or null.
     */
    public Property findProperty(String name) {
        for (Property p : this) {
            if (name.equals(p.getName())) {
                return p;
            }
        }
        return null;
    }

    /**
     * The element name used for XML/DOM representations of beans. Override in
     * subclasses.
     *
     * @return The string "bean", unless the function is overridden.
     */
    public String elementName() {
        return "bean";
    }

    /**
     * Retrieves a list of child beans. Override this for beans with children.
     *
     * @return A list of child beans, or <code>null</code>.
     */
    public List<? extends BlimpBean> getChildren() {
        return null;
    }

    /**
     * Add a child bean. Override this for beans with children.
     *
     * @param child
     *            The new child to add.
     * @throws NotImplementedException
     *             If this bean cannot have children, or the child type is not
     *             supported.
     */
    public void addChild(BlimpBean child) throws NotImplementedException {
        throw new NotImplementedException();
    }
    
    /**
     * Removes all children.  Override this for beans with children.
     * The default implementation does nothing.
     */
    public void removeAllChildren() {
    }

    /**
     * Create a deep copy of this bean. Internally, this uses Serializer
     * functionality and is meant to work automatically for subclasses.
     */
    public BlimpBean clone() {
        try {
            return Serializer.beanFromDOM(Serializer.beanToDOM(this));
        }
        catch (ClassNotFoundException e) {
            // should never happen
            e.printStackTrace();
            assert (false);
            return null;
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BlimpBean))
            return false;
        return equals((BlimpBean) obj);
    }

    public int hashCode() {
        return Serializer.beanToXml(this).hashCode();
    }

    /**
     * Test if this bean is equals to the given bean.
     * @param bean A bean to test equality against.
     * @return <code>true</code> if the bean's XML representation is equal.
     */
    public boolean equals(BlimpBean bean) {
        if (bean == null)
            return false;
        // TODO: optimize this
        return Serializer.beanToXml(bean).equals(Serializer.beanToXml(this));
    }

    /**
     * Called after this bean has been loaded from a file.
     * The default implementation does nothing, but can be overridden.
     * @param filename The filename the bean was loaded from.
     */
    protected void beanLoaded(String filename) {

    }

    /**
     * Called after this bean has been saved to a file.
     * The default implementation does nothing, but can be overridden.
     * @param filename The filename the bean was saved to.
     */
    protected void beanSaved(String filename) {

    }
}
