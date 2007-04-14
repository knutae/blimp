package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
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
    
    /**
     * Get a platform-dependent list of extensions useful for SWT file dialogs.
     * @param names An array of extension names not including the dot, or "*" for all files.
     * @return A platform-dependent string of wildcards, separated by semicolons.
     */
    public static String getFilterExtensionList(String[] extensions) {
        StringBuilder builder = new StringBuilder();
    	if (Util.isWindowsOS()) {
            for (String ext : extensions) {
                builder.append(";*." + ext);
            }
        }
        else {
            // not windows: assume unix
            for (String ext : extensions) {
                builder.append(';');
                if (ext.equals("*"))
                    builder.append("*");
                else if (ext.toLowerCase().equals(ext.toUpperCase())) {
                    // no case
                    builder.append("*." + ext);
                }
                else {
                    // allow both cases
                    builder.append("*." + ext.toLowerCase());
                    builder.append(";*." + ext.toUpperCase());
                }
            }
        }
        if (builder.length() == 0)
            return "";
        // strip first separator
        return builder.substring(1);
    }
    
    public static void fillColorRect(GC gc, Rectangle rect,
            int red, int green, int blue) {
        Color color = new Color(gc.getDevice(), red, green, blue);
        gc.setBackground(color);
        gc.fillRectangle(rect);
        color.dispose();
    }
    
    public static void fillBlackRect(GC gc, Rectangle rect) {
        fillColorRect(gc, rect, 0, 0, 0);
    }
    
    public static void fillWhiteRect(GC gc, Rectangle rect) {
        fillColorRect(gc, rect, 255, 255, 255);
    }
}
