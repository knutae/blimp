package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;

public class LayersView extends SashForm {
    Table layerTable;
    BlimpSession session;
    Menu contextMenu;
    MenuItem menuRemove;
    MenuItem menuEdit;
    int selectedLayerIndex; // layer index, not table item index
    LayerPropertyEditor propertyEditor;
    LayerEditorRegistry editorRegistry;
    
    public LayersView(Composite parent) {
        super(parent, SWT.VERTICAL);
        layerTable = new Table(this, SWT.MULTI | SWT.CHECK);
        //layerTable.setHeaderVisible(true);
        //layerTable.setLayout(new FillLayout());
        
        TableColumn col = new TableColumn(layerTable, SWT.LEFT);
        col.setText("Layers");
        col.setWidth(150);
        
        selectedLayerIndex = -1;
        
        layerTable.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                TableItem item = (TableItem) e.item;
                selectedLayerIndex = layerTable.getItemCount() - 1 - layerTable.indexOf(item);
                //System.out.println("index " + index);
                if (selectedLayerIndex >= 0) {
                    session.activateLayer(selectedLayerIndex, item.getChecked());
                    propertyEditor.setLayer(session.getLayer(selectedLayerIndex));
                }
                else {
                    propertyEditor.setLayer(null);
                }
            }
        });
        
        contextMenu = new Menu(layerTable);
        menuRemove = new MenuItem(contextMenu, SWT.PUSH);
        menuRemove.setText("&Remove");
        menuRemove.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (selectedLayerIndex >= 0) {
                    session.removeLayer(selectedLayerIndex);
                    updateWithSession(session, null, null);
                }
            }
        });
        menuEdit = new MenuItem(contextMenu, SWT.PUSH);
        menuEdit.setText("&Edit");
        menuEdit.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event e) {
        		if (selectedLayerIndex < 0)
        			return;
        		Layer layer = session.getLayer(selectedLayerIndex);
        		openLayerEditor(layer, new LayerEditorCallback() {
        			public void editingFinished(Layer layer, boolean cancelled) {
        				if (cancelled)
        					layer.triggerChangeEvent();
        			}
        		});
        	}
        });
        
        layerTable.setMenu(contextMenu);
        
        propertyEditor = new LayerPropertyEditor(this);
        
        createEditorRegistry();
    }
    
    private void createEditorRegistry() {
        editorRegistry = new LayerEditorRegistry(getShell());
        editorRegistry.register(BrightnessContrastLayer.class,
        		BrightnessContrastEditor.class);
        editorRegistry.register(CurvesLayer.class, CurvesEditor.class);
        editorRegistry.register(RawFileInputLayer.class, RawInputEditor.class);
        editorRegistry.register(GammaLayer.class, GammaEditor.class);
    }
    
    public void updateWithSession(BlimpSession session, Layer currentLayer,
    		LayerEditorCallback callback) {
        this.session = session;
        layerTable.removeAll();
        if (session == null)
            return;
        for (int i=session.layerCount()-1; i>=0; i--) {
            Layer layer = session.getLayer(i);
            TableItem item = new TableItem(layerTable, SWT.NONE);
            item.setChecked(layer.isActive());
            item.setText(layer.getDescription());
        }
        propertyEditor.setLayer(null);
        if (currentLayer != null) {
        	// TODO: also select the layer
        	openLayerEditor(currentLayer, callback);
        }
    }
    
    public void refresh() {
    	updateWithSession(session, null, null);
    }
    
    void openLayerEditor(Layer layer, LayerEditorCallback callback) {
    	editorRegistry.showEditorDialog(layer, callback);
    }
}