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

import java.util.List;

import org.boblycat.blimp.Util;
import org.boblycat.blimp.Version;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Class for showing an About dialog.
 *
 * @author Knut Arild Erstad
 */
public class AboutDialog {
    /**
     * Show an About dialog.
     * @param parentShell a parent shell, usually the main window.
     * @param appImages application images (icons) for the dialog title bar.
     */
    public static void show(Shell parentShell, List<Image> appImages) {
        final Shell dialog = new Shell(parentShell,
                SWT.APPLICATION_MODAL | SWT.CLOSE);
        SwtUtil.setImages(dialog, appImages);
        dialog.setText("About Blimp");
        GridLayout layout = new GridLayout();
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        layout.verticalSpacing = 20;
        dialog.setLayout(layout);

        final Image aboutImage = SwtUtil.loadResourceImage(dialog.getDisplay(),
                "blimp-about.png");
        if (aboutImage == null)
            Util.warn("Failed to load blimp-about.png");
        else {
            Label label = new Label(dialog, SWT.NONE);
            label.setImage(aboutImage);
            label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        }

        Label heading = new Label(dialog, SWT.NONE);
        heading.setText("Blimp, a layered photo editor.");
        final Font bigFont = SwtUtil.copyFontWithHeight(heading.getFont(), 18);
        heading.setFont(bigFont);

        Link linkText = new Link(dialog, SWT.NONE);
        linkText.setText(
                "Version " + Version.versionString + "\n\n"
                + "Copyright (C) 2007, 2008 Knut Arild Erstad\n"
                + "\n"
                + "Blimp is <a href=\"http://www.gnu.org/philosophy/free-sw.html\">free software</a>"
                + " distributed under the <a href=\"http://www.gnu.org/licenses/gpl-2.0.html\">GNU General Public License, version 2</a>.\n"
                + "\n"
                + "Credits:\n"
                + "<a href=\"http://schmidt.devlib.org/jiu/\">Java Imaging Utilities</a> by Marco Schmidt and others\n"
                + "<a href=\"http://cybercom.net/~dcoffin/dcraw/\">dcraw</a> (Raw input) by Dave Coffin\n"
                + "\n"
                + "Please visit the <a href=\"https://projects.boblycat.org/blimp/\">Blimp project page</a>"
                + " for more information and resources.\n");
        linkText.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                SwtUtil.openLinkInBrowser(e.text);
            }
        });
        Button button = new Button(dialog, SWT.PUSH);
        button.setText("Close");
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                dialog.close();
            }
        });
        button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

        dialog.addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                SwtUtil.dispose(aboutImage);
                SwtUtil.dispose(bigFont);
            }
        });

        dialog.pack();
        dialog.open();
    }
}
