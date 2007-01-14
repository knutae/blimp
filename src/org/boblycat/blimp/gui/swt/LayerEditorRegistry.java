package org.boblycat.blimp.gui.swt;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import org.boblycat.blimp.*;
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
		Layer editedLayer;
		Layer layerClone;
		Shell dialog;
		LayerEditorCallback callback;
		Entry(Class<? extends LayerEditor> editorClass) {
			this.editorClass = editorClass;
			editorConstructor = getConstructor(editorClass);
		}
		
		void showDialog(Layer layer, LayerEditorCallback cb) {
			callback = cb;
			dialog = new Shell(parentShell,
					SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
			dialog.setLayout(new GridLayout());
			dialog.setText(layer.getDescription());
			Object args[] = { dialog, new Integer(SWT.NONE) };
			LayerEditor editor = null;
			try {
				editor =  editorConstructor.newInstance(args);
			}
			catch (Exception e) {
				System.err.println("Failed to construct editor: " + e.getMessage());
				return;
			}
			editedLayer = layer;
			layerClone = (Layer) layer.clone();
			editor.setLayer(layer);
			
			Composite buttonRow = new Composite(dialog, SWT.NONE);
			buttonRow.setLayout(new FillLayout());
			Button button = new Button(buttonRow, SWT.NONE);
			button.setText("Ok");
			button.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					editedLayer.setActive(true); // quick hack for raw input
					editedLayer.triggerChangeEvent();
					closeDialog();
					if (callback != null)
						callback.editingFinished(editedLayer, false);
				}
			});
			button = new Button(buttonRow, SWT.NONE);
			button.setText("Cancel");
			button.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					// revert layer changes
					Serializer.copyBeanData(layerClone, editedLayer);
					closeDialog();
					if (callback != null)
						callback.editingFinished(editedLayer, true);
				}
			});
			
			dialog.pack();
			dialog.open();
		}
		
		void closeDialog() {
			dialog.close();
			dialog = null;
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
			System.err.println("Editor class does not have the required constructor (Composite, int): "
					+ editorClass.getName());
			return;
		}
		registry.put(layerClass.getName(), new Entry(editorClass));
	}
	
	public boolean showEditorDialog(Layer layer, LayerEditorCallback callback) {
		Entry entry = registry.get(layer.getClass().getName());
		if (entry == null)
			return false;
		entry.showDialog(layer, callback);
		return true;
	}
}
