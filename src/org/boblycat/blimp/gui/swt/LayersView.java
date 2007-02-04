package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.boblycat.blimp.layers.BrightnessContrastLayer;
import org.boblycat.blimp.layers.CurvesLayer;
import org.boblycat.blimp.layers.GammaLayer;
import org.boblycat.blimp.layers.GrayscaleMixerLayer;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.layers.LocalContrastLayer;
import org.boblycat.blimp.layers.RawFileInputLayer;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Point;

public class LayersView extends SashForm {
    class LayerEditorCallbackWrapper implements LayerEditorCallback {
        LayerEditorCallback originalCallback;
        HistoryBlimpSession session;
        LayerEditorCallbackWrapper(LayerEditorCallback original,
                HistoryBlimpSession session) {
            originalCallback = original;
            this.session = session;
            session.beginDisableAutoRecord();
        }
        
        public void editingFinished(Layer layer, boolean cancelled) {
            session.endDisableAutoRecord();
            if (originalCallback != null)
                originalCallback.editingFinished(layer, cancelled);
        }
        
        void openLayerEditor(Layer layer) {
            if (!editorRegistry.showEditorDialog(layer, this))
                // no editor shown: re-enable autoRecord at once
                session.endDisableAutoRecord();
        }
    }

    Table layerTable;
    HistoryBlimpSession session;
    Menu contextMenu;
    MenuItem menuRemove;
    MenuItem menuEdit;
    int selectedLayerIndex; // layer index, not table item index
    LayerPropertyEditor propertyEditor;
    LayerEditorRegistry editorRegistry;
    int dragIndex;

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
                    session.activateLayer(selectedLayerIndex, item.getChecked());
                    propertyEditor.setLayer(session.getLayer(
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
                    session.moveLayer(dragIndex, dropIndex);
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
        editorRegistry.register(GrayscaleMixerLayer.class,
                GrayscaleMixerEditor.class);
        editorRegistry.register(LocalContrastLayer.class, LocalContrastEditor.class);
    }
    
    private int layerIndexOfItem(TableItem item) {
        if (item == null)
            return -1;
        int index = layerTable.indexOf(item);
        if (index < 0)
            return index;
        return layerTable.getItemCount() - index - 1;
    }

    public void updateWithSession(HistoryBlimpSession session, Layer currentLayer,
            LayerEditorCallback callback) {
        this.session = session;
        layerTable.removeAll();
        if (session == null)
            return;
        for (int i = session.layerCount() - 1; i >= 0; i--) {
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
        LayerEditorCallbackWrapper wrapper =
            new LayerEditorCallbackWrapper(callback, session);
        wrapper.openLayerEditor(layer);
    }
}