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
package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.boblycat.blimp.io.ColorRGB;
import org.boblycat.blimp.io.PointDouble;
import org.boblycat.blimp.layers.Layer;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;

import static org.boblycat.blimp.io.Serializer.propertyValueToString;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LayerPropertyEditor extends Composite {
    private static final int VALUE_COLUMN = 1;

    Layer layer;
    Tree propertyTree;
    TreeEditor treeEditor;
    TreeItem editedItem;
    ArrayList<Layer.Property> layerProperties;
    Menu contextMenu;
    MenuItem menuAddValue;
    MenuItem menuRemoveValue;

    public LayerPropertyEditor(Composite parent) {
        super(parent, SWT.NONE);
        layerProperties = new ArrayList<Layer.Property>();
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
        Class<?> propClass = prop.getPropertyClass();
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

    private void setTreeEditor(Control editor) {
        treeEditor.setEditor(editor, editedItem, VALUE_COLUMN);
    }

    private void refreshCurrentTreeEditor() {
        Control currentEditor = treeEditor.getEditor();
        if (currentEditor != null)
            setTreeEditor(currentEditor);
    }

    void disposeOldEditor() {
        Control oldEditor = treeEditor.getEditor();
        if (oldEditor != null) {
            Debug.print(this, "disposing old editor");
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
            Debug.print(this, "combo editor...");
            Combo comboEditor = new Combo(propertyTree, SWT.DROP_DOWN);
            Listener comboListener = new Listener() {
                public void handleEvent(Event e) {
                    assert (e.widget instanceof Combo);
                    Combo c = (Combo) e.widget;
                    cellEdited(c.getText());
                }
            };
            comboEditor.addListener(SWT.Selection, comboListener);
            comboEditor.addListener(SWT.DefaultSelection, comboListener);
            for (String val : enumValues)
                comboEditor.add(val);
            comboEditor.setText(editedItem.getText(VALUE_COLUMN));
            comboEditor.setFocus();
            setTreeEditor(comboEditor);
        }
        else {
            Debug.print(this, "text editor...");
            Text textEditor = new Text(propertyTree, SWT.NONE);
            textEditor.addListener(SWT.DefaultSelection, new Listener() {
                public void handleEvent(Event e) {
                    Text text = (Text) e.widget;
                    cellEdited(text.getText());
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

    private void refreshVisibleProperties() {
        // Refresh the property tree without destroying the existing tree
        // items if possible.
        layerProperties.clear();
        if (layer == null)
            return;
        int treeIndex = 0;
        for (Layer.Property prop: layer) {
            if (prop.getName().equals("active"))
                // skip standard properties
                continue;
            assert(treeIndex <= propertyTree.getItemCount());
            boolean existsInTree;
            TreeItem item = null;
            if (treeIndex >= propertyTree.getItemCount())
                existsInTree = false;
            else {
                item = propertyTree.getItem(treeIndex);
                if (prop.getName().equals(item.getText()))
                    existsInTree = true;
                else
                    existsInTree = false;
            }
            if (layer.isVisibleProperty(prop.getDescriptor())) {
                if (prop.getValue() == null) {
                    Util.warn("Failed to read value for property "
                            + prop.getName());
                    continue;
                }
                layerProperties.add(prop);
                if (!existsInTree) {
                    // add to tree
                    item = new TreeItem(propertyTree, SWT.NONE, treeIndex);
                    configureTreeItem(item, prop.getName(), prop.getValue());
                }
                treeIndex++;
            }
            else {
                if (existsInTree) {
                    // remove from tree
                    assert(item != null);
                    item.dispose();
                }
            }
        }
    }

    private void configureTreeItem(TreeItem item, String name, Object value) {
        item.setText(name);
        if (value.getClass().isArray()) {
            for (int i=0; i<Array.getLength(value); i++) {
                Object sub = Array.get(value, i);
                TreeItem subItem = new TreeItem(item, SWT.NONE);
                subItem.setText(1, propertyValueToString(sub));
            }
            item.setExpanded(true);
        }
        else {
            item.setText(1, propertyValueToString(value));
        }
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
        disposeOldEditor();
        propertyTree.removeAll();
        layerProperties.clear();
        if (layer == null)
            return;
        refreshVisibleProperties();
    }

    void subTreeEdited(TreeItem parentItem, String newText) {
        Debug.print(this, "subitem " + parentItem.indexOf(editedItem));
        int index = propertyTree.indexOf(parentItem);
        if ((index < 0) || (index >= layerProperties.size()))
            return;
        Layer.Property prop = layerProperties.get(index);
        int count = parentItem.getItemCount();
        String[] strValues = new String[count];
        for (int i = 0; i < count; i++) {
            TreeItem childItem = parentItem.getItem(i);
            if (childItem == editedItem && newText != null)
                strValues[i] = newText;
            else
                strValues[i] = childItem.getText(1);
        }
        tryApplyArrayValue(prop, strValues);
        try {
            Object array = prop.getValue();
            assert(array.getClass().isArray());
            int length = Array.getLength(array);
            parentItem.setItemCount(length);
            for (int i = 0; i < length; i++) {
                TreeItem subItem = parentItem.getItem(i);
                subItem.setText(1, propertyValueToString(Array.get(array, i)));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void cellEdited(String newText) {
        Debug.print(this, "new text: " + newText);
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
        refreshVisibleProperties();
        refreshCurrentTreeEditor();
    }

    Object parsePropertyValue(Class<?> propertyClass, String strValue)
            throws IllegalArgumentException {
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
        else if (propertyClass == PointDouble.class)
            return PointDouble.valueOfCommaString(strValue);
        else if (propertyClass == ColorRGB.class)
            return ColorRGB.parseColor(strValue);
        else if (propertyClass.isEnum()) {
            for (Object enumConst : propertyClass.getEnumConstants()) {
                if (enumConst.toString().equals(strValue))
                    return enumConst;
            }
            Util.err("Unknown enum value " + strValue);
            return null;
        }
        Util.err("Unsupported property type "
                + propertyClass.getName());
        return null;
    }

    void tryApplyTextValue(Layer.Property prop, String strValue) {
        Class<?> propertyClass = prop.getPropertyClass();
        try {
            Object objValue = parsePropertyValue(propertyClass, strValue);
            prop.setValue(objValue);
            layer.invalidate();
        }
        catch (IllegalArgumentException e) {
            return;
        }
    }

    void tryApplyArrayValue(Layer.Property prop, String[] strValues) {
        Class<?> arrayClass = prop.getPropertyClass();
        Class<?> componentClass = arrayClass.getComponentType();
        try {
            Object objValues = Array.newInstance(componentClass,
                    strValues.length);
            for (int i = 0; i < strValues.length; i++) {
                Array.set(objValues, i, parsePropertyValue(componentClass,
                        strValues[i]));
            }
            prop.setValue(objValues);
            layer.invalidate();
        }
        catch (IllegalArgumentException e) {
            return;
        }
    }
}