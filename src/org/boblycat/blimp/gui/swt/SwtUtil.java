package org.boblycat.blimp.gui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * SWT-related utility functions.
 * @author Knut Arild Erstad
 */
public class SwtUtil {
	public static void errorDialog(Shell parentShell, String title, String message) {
		MessageBox box = new MessageBox(parentShell, SWT.OK | SWT.ICON_ERROR);
		box.setText(title);
		box.setMessage(message);
		box.open();		
	}
}
