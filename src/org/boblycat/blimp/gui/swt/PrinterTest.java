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
package org.boblycat.blimp.gui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;

public class PrinterTest {
    public static void run(Shell parent) {
        PrintDialog dlg = new PrintDialog(parent);
        PrinterData data = dlg.open();
        //SwtUtil.messageDialog(parent, "Test", data.toString(), SWT.ICON_INFORMATION);
        Printer printer = new Printer(data);
        try {
            String info =
                "Bounds: " + printer.getBounds() +
                "\nClient area: " + printer.getClientArea();
            SwtUtil.messageDialog(parent, data.name, info, SWT.ICON_INFORMATION);
            
        }
        finally {
            printer.dispose();
        }
    }
}
