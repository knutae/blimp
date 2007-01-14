package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import static org.boblycat.blimp.Serializer.propertyValueToString;

import java.lang.reflect.Array;
import java.util.Vector;

public class LayerPropertyEditor extends Composite {
    final int VALUE_COLUMN = 1;
    Layer layer;
    Tree propertyTree;
    TreeEditor treeEditor;
    TreeItem editedItem;
    Vector <Layer.Property> layerProperties;
    Menu contextMenu;
    MenuItem menuAddValue;
    MenuItem menuRemoveValue;
    
    public LayerPropertyEditor(Composite parent) {
        super(parent, SWT.NONE);
        layerProperties = new Vector<Layer.Property>();
        propertyTree = new Tree(this, SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
        propertyTree.setHeaderVisible(true);
        TreeColumn col = new TreeColumn(propertyTree, SWT.LEFT);
        col.setText("Name");
        col.setWidth(80);
        col = new TreeColumn(propertyTree, SWT.LEFT);
        col.setText("Value");
        col.setWidth(100);
        setLayout(new FillLayout());
        
        treeEditor = new TreeEditor(propertyTree);
        treeEditor.horizontalAlignment = SWT.LEFT;
        treeEditor.grabHorizontal = true;
        treeEditor.minimumWidth = 50;
        
        propertyTree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editedItem = (TreeItem) e.item;
                startEditing();
            }
        });
        
        contextMenu = new Menu(propertyTree);
        menuAddValue = new MenuItem(contextMenu, SWT.PUSH);
        menuAddValue.setText("&Add value");
        menuAddValue.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event e) {
        		doMenuAddValue();
        	}
        });
        menuRemoveValue = new MenuItem(contextMenu, SWT.PUSH);
        menuRemoveValue.setText("&Remove value");
        menuRemoveValue.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event e) {
        		doMenuRemoveValue();
        	}
        });
        
        propertyTree.setMenu(contextMenu);
    }
    
    String[] getEnumValuesForItem(TreeItem item) {
    	if (item.getParentItem() != null)
    		// enums not supported for subitems yet
    		return null;
    	int index = propertyTree.indexOf(item);
    	if (index < 0 || index >= layerProperties.size())
    		return null;
    	Layer.Property prop = layerProperties.get(index);
    	Class propClass = prop.getPropertyClass();
    	if (propClass == Boolean.class || propClass == Boolean.TYPE) {
    		return new String[] { "true", "false" };
    	}
    	else if (propClass.isEnum()) {
    		Object[] enums = propClass.getEnumConstants();
    		String[] values = new String[enums.length];
    		for (int i = 0; i < enums.length; i++)
    			values[i] = enums[i].toString();
    		return values;
    	}
    	return null;
    }
    
    void setTreeEditor(Control editor) {
    	treeEditor.setEditor(editor, editedItem, VALUE_COLUMN);
    }
    
    void disposeOldEditor() {
        Control oldEditor = treeEditor.getEditor();
        if (oldEditor != null) {
        	//System.out.println("disposing old editor");
        	oldEditor.dispose();
        }    	
    }
    
    void startEditing() {
        disposeOldEditor();

        if (editedItem == null)
            return;
        if (editedItem.getItemCount() > 0)
        	return;        
        
        String[] enumValues = getEnumValuesForItem(editedItem);
        if (enumValues != null) {
        	//System.out.println("combo editor...");
        	Combo comboEditor = new Combo(propertyTree, SWT.DROP_DOWN);
        	Listener comboListener = new Listener() {
        		public void handleEvent(Event e) {
        			assert(e.widget instanceof Combo);
        			Combo c = (Combo) e.widget;
        			cellEdited(c.getText());
        		}
        	};
        	comboEditor.addListener(SWT.Selection, comboListener);
        	comboEditor.addListener(SWT.DefaultSelection, comboListener);
        	for (String val: enumValues)
        		comboEditor.add(val);
        	comboEditor.setText(editedItem.getText(VALUE_COLUMN));
        	comboEditor.setFocus();
        	setTreeEditor(comboEditor);
        }
        else {
        	//System.out.println("text editor...");
            Text textEditor = new Text(propertyTree, SWT.NONE);
            textEditor.addListener(SWT.DefaultSelection, new Listener() {
                public void handleEvent(Event e) {
                	cellEdited(e.text);
                }
            });
            textEditor.setText(editedItem.getText(VALUE_COLUMN));
            textEditor.selectAll();
            textEditor.setFocus();
            setTreeEditor(textEditor);        	
        }
    }
    
    void doMenuAddValue() {
    	if (editedItem == null)
    		return;
    	int addIndex;
    	TreeItem parent;
    	TreeItem newItem;
    	if (editedItem.getItemCount() > 0) {
    		parent = editedItem;
    		addIndex = 0;
    	}
    	else if (editedItem.getParentItem() != null) {
    		parent = editedItem.getParentItem();
    		addIndex = parent.indexOf(editedItem) + 1;
    	}
    	else {
    		return;
    	}
    	newItem = new TreeItem(parent, SWT.NONE, addIndex);
    	editedItem = newItem;
    	startEditing();
    }
    
    void doMenuRemoveValue() {
    	if (editedItem == null || editedItem.getParentItem() == null)
    		return;
    	TreeItem parentItem = editedItem.getParentItem();
    	editedItem.dispose();
    	editedItem = parentItem;
    	subTreeEdited(parentItem, null);
    }
    
    public void setLayer(Layer layer) {
        this.layer = layer;
        disposeOldEditor();
        propertyTree.removeAll();
        layerProperties.clear();
        if (layer == null)
            return;
        for (Layer.Property prop: layer) { 
        	if (prop.getName().equals("active"))
        		// skip standard properties
        		continue;
        	layerProperties.add(prop);
            String name = prop.getName();
            Object value = prop.getValue();
            if (value == null) {
                System.out.println("Error reading value for property " + name);
                continue;
            }
            TreeItem item = new TreeItem(propertyTree, SWT.NONE);
            item.setText(name);
            if (value instanceof Object[]) {
            	//System.out.println("yep, array it is");
            	//item.setText(1, "...");
            	Object[] array = (Object[]) value;
            	for (Object sub: array) {
            		TreeItem subItem = new TreeItem(item, SWT.NONE);
            		//subItem.setText("");
            		subItem.setText(1, propertyValueToString(sub));
            	}
            	item.setExpanded(true);
            }
            else {
                item.setText(1, propertyValueToString(value));
            }        	
        }
    }
    
    void subTreeEdited(TreeItem parentItem, String newText) {
		//System.out.println("subitem " + parentItem.indexOf(editedItem));
    	int index = propertyTree.indexOf(parentItem);
        if ((index < 0) || (index >= layerProperties.size()))
            return;
        Layer.Property prop = layerProperties.get(index);
        int count = parentItem.getItemCount();
        String[] strValues = new String[count];
        for (int i=0; i < count; i++) {
        	TreeItem childItem = parentItem.getItem(i);
        	if (childItem == editedItem && newText != null)
        		strValues[i] = newText;
        	else
        		strValues[i] = childItem.getText(1);
        }
        tryApplyArrayValue(prop, strValues);
        try {
        	Object[] objValues = (Object[]) prop.getValue();
    		parentItem.setItemCount(objValues.length);
    		for (int i=0; i<objValues.length; i++) {
    			TreeItem subItem = parentItem.getItem(i);
    			subItem.setText(1, propertyValueToString(objValues[i]));
    		}
        }
        catch (Exception e) {
        	e.printStackTrace();
        }    	
    }

    void cellEdited(String newText) {
        //System.out.println("new text: " + newText);
    	TreeItem parentItem = editedItem.getParentItem();
    	if (parentItem != null) {
    		subTreeEdited(parentItem, newText);
    	}
    	else {
        	int index = propertyTree.indexOf(editedItem);
	        if ((index < 0) || (index >= layerProperties.size()))
	            return;
	        Layer.Property prop = layerProperties.get(index);
	        tryApplyTextValue(prop, newText);
	        Object val = prop.getValue();
	            if (val != null)
	                editedItem.setText(1, propertyValueToString(val));
    	}
    }
    
    Object parsePropertyValue(Class propertyClass, String strValue)
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
    	else if (propertyClass.isEnum()) {
    		for (Object enumConst: propertyClass.getEnumConstants()) {
    			if (enumConst.toString().equals(strValue))
    				return enumConst;
    		}
    		System.err.println("Unknown enum value " + strValue);
    		return null;
    	}
    	System.err.println("Unsupported property type " + propertyClass.getName());
    	return null;
    }
    
    void tryApplyTextValue(Layer.Property prop, String strValue)
    {
        Class propertyClass = prop.getPropertyClass();
        try {
        	Object objValue = parsePropertyValue(propertyClass, strValue);
        	prop.setValue(objValue);
        	layer.invalidate();
        }
        catch (NumberFormatException e) {
        	return;
        }
    }
    
    void tryApplyArrayValue(Layer.Property prop, String[] strValues)
    {
        Class arrayClass = prop.getPropertyClass();
        Class componentClass = arrayClass.getComponentType();
    	try {
    		Object objValues = Array.newInstance(componentClass, strValues.length);
    		for (int i=0; i<strValues.length; i++) {
    			Array.set(objValues, i, parsePropertyValue(componentClass,
    					strValues[i]));
    		}
    		prop.setValue(objValues);
    		layer.invalidate();
    	}
        catch (NumberFormatException e) {
        	return;
        }
    }
}