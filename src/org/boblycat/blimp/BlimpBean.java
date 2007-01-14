package org.boblycat.blimp;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;

/**
 * Base class for serializable Blimp objects.
 * @author Knut Arild Erstad
 */
public abstract class BlimpBean  implements Iterable<BlimpBean.Property> {
	/**
	 * Wraps a BlimpBean property using JavaBeans functionality.
	 * BlimpBean properties are accessible through the Iterable interface.
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
				assert(reader != null);
				return reader.invoke(bean);
			}
			catch (InvocationTargetException e) {
				errorMessage = e.getMessage();
			}
			catch (IllegalAccessException e) {
				errorMessage = e.getMessage();
			}
			System.err.println(errorMessage);
			System.err.println("Failed to get value for property " + getName()
					+ ", class " + bean.getClass().getName());
			return null;
		}
		
		public void setValue(Object value) {
			String errorMessage = null;
			try {
				Method writer = descriptor.getWriteMethod();
				assert(writer != null);
				writer.invoke(bean, value);
				return;
			}
			catch (InvocationTargetException e) {
				errorMessage = e.getMessage();
			}
			catch (IllegalAccessException e) {
				errorMessage = e.getMessage();
			}
			System.err.println(errorMessage);
			System.err.println("Failed to set value for property " + getName()
					+ ", class " + bean.getClass().getName());
		}
		
		public Class getPropertyClass() {
			Method writer = descriptor.getWriteMethod();
			return writer.getParameterTypes()[0];
		}
		
		private Property(BlimpBean bean, PropertyDescriptor descriptor) {
			this.bean = bean;
			this.descriptor = descriptor;
		}
	}
	
    /**
     * Iterator for BlimpBean properties.  This includes all JavaBean properties
     * defined in subclasses of BlimpBean which are both readable and writable.
     * @return An iterator for this bean's properties.
     */
    public Iterator<Property> iterator() {
    	Vector<Property> props = new Vector<Property>();
    	try {
    		BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass(), BlimpBean.class);
    		PropertyDescriptor[] propertyDesc = beanInfo.getPropertyDescriptors();
    		for (int i=0; i<propertyDesc.length; i++) {
    			PropertyDescriptor pd = propertyDesc[i];
    			if (pd.getReadMethod() == null || pd.getWriteMethod() == null)
    				// skip properties without both read and write access
    				continue;
    			// todo: skip unsupported types?
      			props.add(new Property(this, pd));
    		}
    	}
    	catch (IntrospectionException e) {
    		System.err.println("Failed to get property descriptors for class "
    				+ this.getClass().getName());
    		e.printStackTrace();
    		return null;
    	}
    	return props.iterator();
    }	

    /**
     * Find a BlimpBean property by name.  This function currently has linear time
  	 * complexity.
     * @param name The name of the (JavaBeans) property to search for.
     * @return A property with the given name, or null.
     */
    public Property findProperty(String name) {
    	for (Property p: this) {
    		if (name.equals(p.getName())) {
    			return p;
    		}
    	}
    	return null;
    }
    
    /**
     * The element name used for XML/DOM representations of beans.
     * Override in subclasses.
     * @return The string "bean", unless the function is overridden.
     */
    public String elementName() {
    	return "bean";
    }

    /**
     * Create a deep copy of this bean.
     * Internally, this uses Serializer functionality and is meant to work
     * automatically for subclasses.
     */
    public BlimpBean clone() {
    	try {
    		return Serializer.beanFromDOM(Serializer.beanToDOM(this));
    	}
    	catch (ClassNotFoundException e) {
    		// should never happen
    		e.printStackTrace();
    		assert(false);
    		return null;
    	}
    }
}
