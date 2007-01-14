package org.boblycat.blimp.gui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * SWT-related utility functions.
 * 
 * @author Knut Arild Erstad
 */
public class SwtUtil {
    public static void errorDialog(Shell parentShell, String title,
            String message) {
        MessageBox box = new MessageBox(parentShell, SWT.OK | SWT.ICON_ERROR);
        box.setText(title);
        box.setMessage(message);
        box.open();
    }
    
    /**
     * Dispose the given widget if it is not <code>null</code>.
     * @param widget A widget, or <code>null</code>.
     */
    public static void dispose(Widget widget) {
        if (widget == null || widget.isDisposed())
            return;
        widget.dispose();
    }

    /**
     * Dispose the given resource if it is not <code>null</code>.
     * @param resource A graphics resource, or <code>null</code>.
     */
    public static void dispose(Resource resource) {
        if (resource == null || resource.isDisposed())
            return;
        resource.dispose();
    }
}
