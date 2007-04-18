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
        messageDialog(parentShell, title, message, SWT.ICON_ERROR);
    }
    
    /**
     * Show a message dialog with an OK button.
     * @param parentShell The parent window.
     * @param title The window title.
     * @param message The message text.
     * @param style The style, which should include one of the
     *              <code>SWT.ICON_</code> constants.
     */
    public static void messageDialog(Shell parentShell, String title,
            String message, int style) {
        MessageBox box = new MessageBox(parentShell, SWT.OK | style);
        box.setText(title);
        box.setMessage(message);
        box.open();
    }
    
    public static int confirmationDialog(Shell parentShell, String title,
            String message, int style) {
        MessageBox box = new MessageBox(parentShell, style);
        box.setText(title);
        box.setMessage(message);
        return box.open();
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
    
    /**
     * Show a confirmation dialog asking the if it is okay to overwrite
     * the given file.
     * @param parentShell The parent window.
     * @param filename The file to be overwritten.
     * @return <code>true</code> if the user confirmed the overwrite.
     */
    public static boolean confirmOverwrite(Shell parentShell,
            String filename) {
        MessageBox box = new MessageBox(parentShell,
                SWT.YES | SWT.NO | SWT.ICON_QUESTION);
        box.setText("Overwrite File");
        box.setMessage("Do you want to overwrite the existing file?\n"
                + filename);
        int result = box.open();
        return (result == SWT.YES);
    }
}
