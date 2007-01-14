package org.boblycat.blimp;

import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.data.PixelImage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * Abstract base class for adjustment layers in Blimp.
 * In Blimp, all image operations are based upon adjustment layers which
 * must extend this class.
 */
public abstract class Layer implements Iterable<Layer.Property> {
	/**
	 * Wraps a Layer property using JavaBeans functionality.
	 * Layer properties are accessible through the Iterable interface.
	 */
	public class Property {
		PropertyDescriptor descriptor;
		Layer layer;
		
		public String getName() {
			return descriptor.getName();
		}
		
		public Object getValue() {
			String errorMessage = null;
			try {
				Method reader = descriptor.getReadMethod();
				assert(reader != null);
				return reader.invoke(layer);
			}
			catch (InvocationTargetException e) {
				errorMessage = e.getMessage();
			}
			catch (IllegalAccessException e) {
				errorMessage = e.getMessage();
			}
			System.err.println(errorMessage);
			System.err.println("Failed to get value for property " + getName()
					+ ", class " + layer.getClass().getName());
			return null;
		}
		
		public void setValue(Object value) {
			String errorMessage = null;
			try {
				Method writer = descriptor.getWriteMethod();
				assert(writer != null);
				writer.invoke(layer, value);
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
					+ ", class " + layer.getClass().getName());
		}
		
		public Class getPropertyClass() {
			Method writer = descriptor.getWriteMethod();
			return writer.getParameterTypes()[0];
		}
		
		private Property(Layer layer, PropertyDescriptor descriptor) {
			this.layer = layer;
			this.descriptor = descriptor;
		}
	}
	
    boolean active;
    Vector<LayerChangeListener> changeListeners;
    
    public abstract Bitmap applyLayer(Bitmap source);
    
    public abstract String getName();

    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Layer() {
        active = true;
        changeListeners = new Vector<LayerChangeListener>();
    }
    
    public void addChangeListener(LayerChangeListener listener) {
        int i = changeListeners.indexOf(null);
        if (i >= 0)
            changeListeners.setElementAt(listener, i);
        else
            changeListeners.add(listener);
    }
    
    public void removeChangeListener(LayerChangeListener listener) {
        int i = changeListeners.indexOf(listener);
        if (i >= 0)
            changeListeners.setElementAt(null, i);
    }
    
    void triggerChangeEvent() {
        LayerEvent event = new LayerEvent(this);
        for (LayerChangeListener listener: changeListeners) {
            if (listener == null)
                continue;
            listener.handleChange(event);
        }
    }
    
    protected PixelImage applyJiuOperation(PixelImage input, ImageToImageOperation op) {
        PixelImage image = input;
        op.setInputImage(image);
        try {
            op.process();
            image = op.getOutputImage();
        }
        catch (Exception e) {
            System.out.println(op.getClass().getName() + ": " + e.getMessage());
        }
        return image;
    }
    
    public void invalidate() {
        triggerChangeEvent();
    }
    
    /** Iterator for property descriptors. */
    public Iterator<PropertyDescriptor> propertyDescIterator() {
    	Vector<PropertyDescriptor> props = new Vector<PropertyDescriptor>();
    	try {
    		BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass(), Object.class);
    		PropertyDescriptor[] propertyDesc = beanInfo.getPropertyDescriptors();
    		for (int i=0; i<propertyDesc.length; i++) {
    			PropertyDescriptor pd = propertyDesc[i];
    			if (pd.getName().equals("active"))
    				// skip standard properties
    				continue;
    			if (pd.getReadMethod() == null || pd.getWriteMethod() == null)
    				// skip properties without read/write access
    				continue;
    			// todo: skip unsupported types?
    			props.add(pd);
    		}
    	}
    	catch (IntrospectionException e) {
    		System.err.println("Failed to get property descriptors for class "
    				+ this.getClass().getName());
    		e.printStackTrace();
    	}
    	return props.iterator();
    }
    
    /**
     * Iterator for layer properties.  This includes all JavaBean properties
     * defined in Layer and subclasses which are both readable and writable.
     * @return An iterator for this layer's properties.
     */
    public Iterator<Property> iterator() {
    	Vector<Property> props = new Vector<Property>();
    	try {
    		BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass(), Object.class);
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
    	}
    	return props.iterator();
    }
    
    /**
     * Find a layer property by name.  This function currently has linear time
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
     * Create a deep copy of this layer.
     * Internally, this uses Serializer functionality and is meant to work
     * automatically for subclasses.
     */
    public Layer clone() {
    	try {
    		return Serializer.layerFromDOM(Serializer.layerToDOM(this));
    	}
    	catch (ClassNotFoundException e) {
    		// should never happen
    		e.printStackTrace();
    		assert(false);
    		return null;
    	}
    }
}
