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
package org.boblycat.blimp.gui.swt.editors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.boblycat.blimp.*;
import org.boblycat.blimp.layers.Layer;
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

        void showDialog(LayerEditorEnvironment environment) {
            new EditorDialog(parentShell, editorConstructor, environment).show();
        }
    }

    HashMap<String, Entry> registry;

    Shell parentShell;

    public static Constructor<? extends LayerEditor> getConstructor(
            Class<? extends LayerEditor> editorClass) {
        Class<?>[] argTypes = { Composite.class, Integer.TYPE };
        try {
            return editorClass.getConstructor(argTypes);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    public LayerEditorRegistry(Shell parent) {
        parentShell = parent;
        registry = new HashMap<String, Entry>();
    }

    public void register(Class<? extends Layer> layerClass,
            Class<? extends LayerEditor> editorClass) {
        if (getConstructor(editorClass) == null) {
            Util.err("Editor class does not have the required constructor (Composite, int): "
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

    public LayerEditor createEdior(Layer layer, Composite parent, int flags)
    throws InvocationTargetException, IllegalAccessException,
    InstantiationException {
        Entry entry = registry.get(layer.getClass().getName());
        if (entry == null)
            return null;
        Object args[] = { parent, new Integer(flags) };
        return entry.editorConstructor.newInstance(args);
    }
}
