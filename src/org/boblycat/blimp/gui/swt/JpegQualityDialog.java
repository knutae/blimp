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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple utility class for showing a JPEG quality dialog.
 *
 * @author Knut Arild Erstad
 */
public class JpegQualityDialog {
    public static double queryJpegQuality(Shell parent) {
        final Shell dialog = new Shell(parent,
                SWT.APPLICATION_MODAL | SWT.TITLE | SWT.BORDER);
        dialog.setText("JPEG Save Settings");
        dialog.setLayout(new GridLayout());
        ValueSlider slider = new ValueSlider(dialog, SWT.NONE,
                "JPEG Quality", 0, 100, 0);
        slider.setSelection(90);
        Button okButton = new Button(dialog, SWT.NONE);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                dialog.close();
            }
        });
        dialog.pack();
        dialog.open();
        SwtUtil.modalLoop(dialog);
        double quality = slider.getSelection() / 100.0;
        return quality;
    }
}
