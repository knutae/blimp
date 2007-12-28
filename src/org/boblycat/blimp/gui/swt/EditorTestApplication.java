/*
 * Copyright (C) 2007 Knut Arild Erstad
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

import java.util.logging.Level;

import org.boblycat.blimp.Debug;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.layers.Layer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A small test application for a single layer editor.
 *
 * Currently does not work for editors that requires access to the image worker
 * thread, for instance the levels editor.
 *
 * Usage: EditorTestApplication <LayerClass>
 *
 * @author Knut Arild Erstad
 */
public class EditorTestApplication {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Please supply a layer class name.");
            return;
        }

        Util.logger.setLevel(Level.ALL);

        String layerClassName = args[0];
        if (!layerClassName.contains("."))
            layerClassName = "org.boblycat.blimp.layers." + layerClassName;
        Class<?> layerClass = Class.forName(layerClassName);
        Debug.register(layerClass);

        Layer layer = (Layer) layerClass.newInstance();


        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setSize(500, 500);
        shell.setLayout(new FillLayout());

        LayerEditorRegistry registry = new DefaultEditorRegistry(shell);

        LayerEditor editor = registry.createEdior(layer, shell, SWT.NONE);
        if (editor == null) {
            System.err.println("No editor registered for layer "
                    + layer.getClass().getName());
            return;
        }
        Debug.register(editor.getClass());
        editor.setLayer(layer);
        shell.setText(editor.getClass().getSimpleName());
        shell.open();


        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

}
