package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.boblycat.blimp.layers.Layer;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Point;

public class LayersView extends SashForm {
    class LayerEditorEnvironmentWrapper implements LayerEditorCallback {
        LayerEditorEnvironment original;
        LayerEditorEnvironment copy;
        LayerEditorEnvironmentWrapper(LayerEditorEnvironment env) {
            original = env;
            copy = env.clone();
            copy.editorCallback = this;
            copy.session.beginDisableAutoRecord();
        }
        
        public void editingFinished(Layer layer, boolean cancelled) {
            copy.session.endDisableAutoRecord();
            if (original.editorCallback != null)
                original.editorCallback.editingFinished(layer, cancelled);
        }
        
        void openLayerEditor() {
            if (!editorRegistry.showEditorDialog(copy)) {
                // no editor shown: re-enable autoRecord at once
                copy.session.endDisableAutoRecord();
                refresh(); // setActive() may have been called 
            }
        }
    }

    Table layerTable;
    Menu contextMenu;
    MenuItem menuRemove;
    MenuItem menuEdit;
    int selectedLayerIndex; // layer index, not table item index
    LayerPropertyEditor propertyEditor;
    LayerEditorRegistry editorRegistry;
    int dragIndex;
    LayerEditorEnvironment editorEnvironment;

    public LayersView(Composite parent) {
        super(parent, SWT.VERTICAL);
        layerTable = new Table(this, SWT.CHECK);
        // layerTable.setHeaderVisible(true);
        // layerTable.setLayout(new FillLayout());

        TableColumn col = new TableColumn(layerTable, SWT.LEFT);
        col.setText("Layers");
        col.setWidth(150);

        selectedLayerIndex = -1;
        dragIndex = -1;

        layerTable.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                TableItem item = (TableItem) e.item;
                selectedLayerIndex = layerIndexOfItem(item);
                if (selectedLayerIndex >= 0) {
                    getSession().activateLayer(selectedLayerIndex, item.getChecked());
                    propertyEditor.setLayer(getSession().getLayer(
                            selectedLayerIndex));
                }
                else {
                    propertyEditor.setLayer(null);
                }
            }
        });
        
        DragSource dragSource = new DragSource(layerTable, DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        dragSource.addDragListener(new DragSourceListener() {
            public void dragStart(DragSourceEvent e) {
                TableItem item = layerTable.getItem(new Point(e.x, e.y));
                dragIndex = layerIndexOfItem(item);
                if (dragIndex <= 0) {
                    e.doit = false;
                    dragIndex = -1;
                }
            }
            
            public void dragSetData(DragSourceEvent e) {
                // not used for anything, but looks like it needs to be there...?
                e.data = Integer.toString(dragIndex);
            }
            
            public void dragFinished(DragSourceEvent e) {
                dragIndex = -1;
            }
        });
        DropTarget dropTarget = new DropTarget(layerTable, DND.DROP_MOVE);
        dropTarget.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        dropTarget.addDropListener(new DropTargetAdapter() {
            public void dragOver(DropTargetEvent e) {
                Point p = layerTable.toControl(e.x, e.y);
                TableItem item = layerTable.getItem(p);
                int index = layerIndexOfItem(item);
                if (index <= 0 || index == dragIndex) {
                    e.detail = DND.DROP_NONE;
                    return;
                }
                e.detail = DND.DROP_MOVE;
            }
            
            public void drop(DropTargetEvent e) {
                e.detail = DND.DROP_NONE;
                try {
                    if (dragIndex < 0)
                        return;
                    Point p = layerTable.toControl(e.x, e.y);
                    TableItem item = layerTable.getItem(p);
                    int dropIndex = layerIndexOfItem(item);
                    if (dropIndex <= 0 || dropIndex == dragIndex)
                        return;
                    e.detail = DND.DROP_MOVE;
                    getSession().moveLayer(dragIndex, dropIndex);
                    refresh();
                }
                finally {
                    dragIndex = -1;
                }
            }
        });

        contextMenu = new Menu(layerTable);
        menuRemove = new MenuItem(contextMenu, SWT.PUSH);
        menuRemove.setText("&Remove");
        menuRemove.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (selectedLayerIndex >= 0) {
                    getSession().removeLayer(selectedLayerIndex);
                    refresh();
                }
            }
        });
        menuEdit = new MenuItem(contextMenu, SWT.PUSH);
        menuEdit.setText("&Edit");
        menuEdit.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (selectedLayerIndex < 0)
                    return;
                editorEnvironment.layerWasJustAdded = false;
                editorEnvironment.layer = getSession().getLayer(selectedLayerIndex);
                openLayerEditor(editorEnvironment, new LayerEditorCallback() {
                    public void editingFinished(Layer layer, boolean cancelled) {
                        if (cancelled)
                            layer.triggerChangeEvent();
                    }
                });
            }
        });

        layerTable.setMenu(contextMenu);

        propertyEditor = new LayerPropertyEditor(this);

        editorRegistry = new DefaultEditorRegistry(getShell());
    }
    
    private HistoryBlimpSession getSession() {
        if (editorEnvironment == null)
            return null;
        return editorEnvironment.session;
    }
    
    private int layerIndexOfItem(TableItem item) {
        if (item == null)
            return -1;
        int index = layerTable.indexOf(item);
        if (index < 0)
            return index;
        return layerTable.getItemCount() - index - 1;
    }

    public void updateWithEnvironment(LayerEditorEnvironment env) {
        editorEnvironment = env;
        layerTable.removeAll();
        HistoryBlimpSession session = getSession();
        if (session == null)
            return;
        for (int i = session.layerCount() - 1; i >= 0; i--) {
            Layer layer = session.getLayer(i);
            TableItem item = new TableItem(layerTable, SWT.NONE);
            item.setChecked(layer.isActive());
            item.setText(layer.getDescription());
        }
        propertyEditor.setLayer(null);
        openLayerEditor(env);
    }

    public void refresh() {
        editorEnvironment.layer = null; // don't open editor
        updateWithEnvironment(editorEnvironment);
    }
    
    void openLayerEditor(LayerEditorEnvironment env,
            LayerEditorCallback callback) {
        LayerEditorEnvironment tmpEnv = env.clone();
        tmpEnv.editorCallback = callback;
        openLayerEditor(tmpEnv);
    }

    void openLayerEditor(LayerEditorEnvironment env) {
        if (env.layer == null)
            return;
        LayerEditorEnvironmentWrapper wrapper =
            new LayerEditorEnvironmentWrapper(env);
        wrapper.openLayerEditor();
    }
}