package org.boblycat.blimp.gui.swt;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import org.boblycat.blimp.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class LayerEditorRegistry {
	class Entry {
		Class<? extends LayerEditor> editorClass;
		Constructor<? extends LayerEditor> editorConstructor;
		Entry(Class<? extends LayerEditor> editorClass) {
			this.editorClass = editorClass;
			editorConstructor = getConstructor(editorClass);
		}
		
		void showDialog(Layer layer) {
			Shell dialog = new Shell(parentShell,
					SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
			dialog.setLayout(new FillLayout());
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
			editor.setLayer(layer);
			dialog.pack();
			dialog.open();
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
	
	public boolean showEditorDialog(Layer layer) {
		Entry entry = registry.get(layer.getClass().getName());
		if (entry == null)
			return false;
		entry.showDialog(layer);
		return true;
	}
}
