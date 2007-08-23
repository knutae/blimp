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

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A GUI for searching for files by name.
 * 
 * @author Knut Arild Erstad
 */
public class FileSearchView extends Composite {
    Text baseDirectory;
    Text fileName;
    List foundFiles;
    Label progressLabel;
    String selectedFilePath;
    Button okButton;
    boolean searchCancelled;

    public FileSearchView(Composite parent, int style, String strFileName) {
        super(parent, style);
        setLayout(new GridLayout(3, false));
        // TODO: this label should have a warning icon
        Label label = new Label(this, SWT.WRAP | SWT.ICON_WARNING);
        label.setText(String.format(
                "The source image %s was not found.  Please specify a " +
                "folder to search for the file below, then click Start Search.",
                strFileName));
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        
        label = new Label(this, SWT.NONE);
        label.setText("Directory");
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        baseDirectory = new Text(this, SWT.BORDER);
        baseDirectory.setLayoutData(new GridData(
                SWT.FILL, SWT.FILL, true, false));
        Button button = new Button(this, SWT.PUSH);
        button.setText("...");
        button.setLayoutData(new GridData(
                SWT.FILL, SWT.FILL, false, false));
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                DirectoryDialog dlg = new DirectoryDialog(getShell(),
                        SWT.APPLICATION_MODAL);
                dlg.setText("Base Folder");
                dlg.setMessage("Select a base folder to search.");
                String newDir = dlg.open();
                if (newDir != null)
                    baseDirectory.setText(newDir);
            }
        });
        label = new Label(this, SWT.NONE);
        label.setText("File Name");
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fileName = new Text(this, SWT.BORDER);
        fileName.setText(strFileName);
        fileName.setLayoutData(new GridData(
                SWT.FILL, SWT.FILL, true, false, 2, 1));
        foundFiles = new List(this, SWT.BORDER | SWT.SINGLE);
        foundFiles.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                // selection (e.g single click)
                updateOkButton();
            }
        });
        foundFiles.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                // default selection (double click)
                useSelected();
            }
        });
        foundFiles.setLayoutData(new GridData(
                SWT.FILL, SWT.FILL,
                true, true,
                3, 1));
        progressLabel = new Label(this, SWT.NONE);
        progressLabel.setText(" ");
        progressLabel.setLayoutData(new GridData(
                SWT.FILL, SWT.FILL,
                true, false,
                3, 1));

        Composite buttonRow = new Composite(this, SWT.NONE);
        FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
        fillLayout.spacing = 10;
        buttonRow.setLayout(fillLayout);
        buttonRow.setLayoutData(new GridData(
                SWT.CENTER, SWT.FILL,
                false, false,
                3, 1));
        button = new Button(buttonRow, SWT.PUSH);
        button.setText("Start Search");
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                doSearch();
            }
        });
        button = new Button(buttonRow, SWT.PUSH);
        button.setText("Stop Search");
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                stopSearch();
            }
        });
        okButton = new Button(buttonRow, SWT.PUSH);
        okButton.setText("Use Selected");
        okButton.setEnabled(false);
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                useSelected();
            }
        });
        button = new Button(buttonRow, SWT.PUSH);
        button.setText("Cancel");
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                setSelectedFilePath(null);
                closeDialog();
            }
        });
    }
    
    private void fileFound(File file) {
        foundFiles.add(file.getAbsolutePath());
    }
    
    private boolean isCancelledOrDisposed() {
        return searchCancelled || isDisposed();
    }
    
    private boolean tryDispatchMessages() {
        do {
            if (isCancelledOrDisposed())
                return false;
        } while (getDisplay().readAndDispatch());
        return true;
    }
    
    private void message(String msg) {
        progressLabel.setText(msg);
    }
    
    private void doSearch() {
        Queue<File> dirs = new LinkedList<File>();
        File baseDir = new File(baseDirectory.getText());
        if (!baseDir.isDirectory()) {
            message("Directory not found: " + baseDir.getAbsolutePath());
            return;
        }
        foundFiles.removeAll();
        setSelectedFilePath(null);
        searchCancelled = false;
        String searchName = fileName.getText().toLowerCase();
        dirs.add(baseDir);
        while (!dirs.isEmpty()) {
            File dir = dirs.poll();
            message(dir.getAbsolutePath());
            if (!tryDispatchMessages())
                break;
            File[] files = dir.listFiles();
            if (!tryDispatchMessages())
                break;
            for (File file: files) {
                if (file.isDirectory())
                    dirs.add(file);
                else if (searchName.equals(file.getName().toLowerCase()))
                    fileFound(file);
            }
        }
        if (isDisposed())
            return;
        if (searchCancelled)
            message("Search stopped.");
        else
            message(String.format("Found %d matching file(s).",
                    foundFiles.getItemCount()));
    }
    
    private void stopSearch() {
        searchCancelled = true;
    }
    
    public String getSelectedFilePath() {
        return selectedFilePath;
    }
    
    private void setSelectedFilePath(String newPath) {
        selectedFilePath = newPath;
    }
    
    private void updateSelectedFilePath() {
        if (isDisposed())
            return;
        int index = foundFiles.getSelectionIndex();
        if (index < 0)
            setSelectedFilePath(null);
        else
            setSelectedFilePath(foundFiles.getItem(index));
    }
    
    private void updateOkButton() {
        okButton.setEnabled(foundFiles.getSelectionIndex() >= 0);
    }
    
    private void useSelected() {
        updateSelectedFilePath();
        closeDialog();
    }
    
    private void closeDialog() {
        // a bit ugly, but never mind
        getShell().dispose();
    }

    /**
     * Show a file search dialog and return a search result.
     * 
     * @param parent The parent shell.
     * @param fileName The (default) file name to search for.
     * @return A full path selected by the user, or <code>null</code>
     *   if no file was found or selected.
     */
    public static String showDialog(Shell parent, String fileName) {
        Shell shell = new Shell(parent,
                SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
        shell.setSize(600, 400);
        shell.setText("File Search");
        shell.setLayout(new FillLayout());
        FileSearchView fileSearch = new FileSearchView(shell, SWT.NONE, fileName);
        fileSearch.baseDirectory.setText(new File("").getAbsolutePath());
        SwtUtil.modalLoop(shell);
        return fileSearch.getSelectedFilePath();
    }
}
