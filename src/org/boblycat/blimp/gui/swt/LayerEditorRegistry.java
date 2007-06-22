package org.boblycat.blimp.gui.swt;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import org.boblycat.blimp.*;
import org.boblycat.blimp.layers.Layer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class LayerEditorRegistry {
    class Entry {
        Class<? extends LayerEditor> editorClass;
        Constructor<? extends LayerEditor> editorConstructor;
        Layer originalClone;
        Layer workingClone;
        Layer actualLayer;
        Shell dialog;
        LayerEditorEnvironment env;
        Button previewCheckButton;
        LayerEditor editor;

        Entry(Class<? extends LayerEditor> editorClass) {
            this.editorClass = editorClass;
            editorConstructor = getConstructor(editorClass);
        }
        
        Layer editedLayer() {
            if (previewEnabled())
                return actualLayer;
            else
                return workingClone;
        }
        
        boolean previewEnabled() {
            return previewCheckButton.getSelection();
        }

        void showDialog(LayerEditorEnvironment environment) {
            env = environment;
            dialog = new Shell(parentShell, SWT.APPLICATION_MODAL
                    | SWT.DIALOG_TRIM);
            dialog.setLayout(new GridLayout());
            dialog.setText(env.layer.getDescription());
            Object args[] = { dialog, new Integer(SWT.NONE) };
            editor = null;
            try {
                editor = editorConstructor.newInstance(args);
            }
            catch (Exception e) {
                System.err.println("Failed to construct editor: "
                        + e.getMessage());
                return;
            }
            actualLayer = env.layer;
            originalClone = (Layer) actualLayer.clone();
            workingClone = (Layer) actualLayer.clone();
            
            previewCheckButton = new Button(dialog, SWT.CHECK);
            previewCheckButton.setText("Preview");
            previewCheckButton.setSelection(editor.previewByDefault());
            previewCheckButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event e) {
                    if (previewEnabled()) {
                        // apply working changes to actual layer
                        Serializer.copyBeanData(workingClone, actualLayer);
                        if (env.layerWasJustAdded)
                            actualLayer.setActive(true);
                    }
                    else {
                        // update the working clone and revert the actual layer
                        Serializer.copyBeanData(actualLayer, workingClone);
                        Serializer.copyBeanData(originalClone, actualLayer);
                        if (env.layerWasJustAdded)
                            actualLayer.setActive(false);
                    }
                    editor.setLayer(editedLayer());
                    actualLayer.invalidate();
                }
            });

            editor.session = env.session;
            editor.workerThread = env.workerThread;
            editor.setLayer(editedLayer());
            if (env.layerWasJustAdded && previewEnabled()) {
                actualLayer.setActive(true);
                actualLayer.invalidate();
            }

            Composite buttonRow = new Composite(dialog, SWT.NONE);
            buttonRow.setLayout(new FillLayout());
            Button button = new Button(buttonRow, SWT.NONE);
            button.setText("Ok");
            button.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event e) {
                    if (editedLayer() != actualLayer)
                        Serializer.copyBeanData(editedLayer(), actualLayer);
                    if (env.layerWasJustAdded)
                        actualLayer.setActive(true);
                    actualLayer.invalidate();
                    editingFinished(false);
                }
            });
            button = new Button(buttonRow, SWT.NONE);
            button.setText("Cancel");
            button.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event e) {
                    // revert layer changes
                    Serializer.copyBeanData(originalClone, actualLayer);
                    editingFinished(true);
                }
            });

            dialog.pack();
            dialog.open();
        }
        
        void editingFinished(boolean cancelled) {
            dialog.close();
            dialog = null;
            env.layerWasJustAdded = false;
            if (env.editorCallback != null)
                env.editorCallback.editingFinished(actualLayer, cancelled);
        }
    }

    Hashtable<String, Entry> registry;

    Shell parentShell;

    static Constructor<? extends LayerEditor> getConstructor(
            Class<? extends LayerEditor> editorClass) {
        Class[] argTypes = { Composite.class, Integer.TYPE };
        try {
            return editorClass.getConstructor(argTypes);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    public LayerEditorRegistry(Shell parent) {
        parentShell = parent;
        registry = new Hashtable<String, Entry>();
    }

    public void register(Class<? extends Layer> layerClass,
            Class<? extends LayerEditor> editorClass) {
        if (getConstructor(editorClass) == null) {
            System.err
                    .println("Editor class does not have the required constructor (Composite, int): "
                            + editorClass.getName());
            return;
        }
        registry.put(layerClass.getName(), new Entry(editorClass));
    }

    public boolean showEditorDialog(LayerEditorEnvironment env) {
        assert(env.layer != null);
        Entry entry = registry.get(env.layer.getClass().getName());
        if (entry == null) {
            if (env.layerWasJustAdded) {
                env.layer.setActive(true);
                env.layer.invalidate();
            }
            return false;
        }
        entry.showDialog(env);
        return true;
    }
}
