package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import java.io.File;
import java.util.Vector;

class ImageTab {
    CTabItem item;
    ImageView imageView;
    ImageTab(CTabItem item, ImageView imageView) {
        this.item = item;
        this.imageView = imageView;
    }
}

public class MainWindow {
    Display display;
    Shell shell;
    Label statusLabel;
    Listener menuHideListener;
    Listener menuItemListener;
    MenuItem menuFileOpen;
    MenuItem menuFileExit;
    CTabFolder mainTabFolder;
    CTabFolder rightTabFolder;
    LayersView layers;
    Vector<ImageTab> imageTabs;
    ImageTab currentImageTab;
    LayerRegistry layerRegistry;
    
    class MenuArmListener implements Listener {
        String helpText;
        MenuArmListener(String description) {
            helpText = description;
        }
        public void handleEvent(Event event) {
            statusLabel.setText(helpText);
        }
    }
    
    class MenuItemListener implements Listener {
        public void handleEvent(Event event) {
            if (event.widget == menuFileOpen) {
                doMenuOpen();
            }
            else if (event.widget == menuFileExit) {
                doMenuExit();
            }
            else if (event.widget instanceof MenuItem) {
        		MenuItem item = (MenuItem) event.widget;
        		if (item.getData() instanceof LayerRegistry.LayerInfo) {
        			LayerRegistry.LayerInfo info =
        				(LayerRegistry.LayerInfo) item.getData();
        			try {
        				addLayer(info.layerClass.newInstance());
        			}
        			catch (IllegalAccessException e) {
        				e.printStackTrace();
        			}
        			catch (InstantiationException e) {
        				e.printStackTrace();
        			}
        		}
        	}
        }
    }
    
    public MenuItem addMenuItem(Menu menu, String text, String description) {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(text);
        item.addListener(SWT.Arm, new MenuArmListener(description));
        item.addListener(SWT.Selection, menuItemListener);
        return item;
    }
    
	public MainWindow() {
        imageTabs = new Vector<ImageTab>();
        display = new Display();
        shell = new Shell(display);
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);
        
        // Menus
        Menu bar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(bar);

        menuHideListener = new Listener() {
            public void handleEvent(Event event) {
                status("");
            }
        };
        menuItemListener = new MenuItemListener();
        
        MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
        fileItem.setText("&File");
        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        fileItem.setMenu(fileMenu);
        fileMenu.addListener(SWT.Hide, menuHideListener);
        menuFileOpen = addMenuItem(fileMenu, "&Open", "Open an image");
        menuFileExit = addMenuItem(fileMenu, "E&xit", "Exit the program");
        
        MenuItem layerItem = new MenuItem(bar, SWT.CASCADE);
        layerItem.setText("Add &Layer");
        Menu layerMenu = new Menu(shell, SWT.DROP_DOWN);
        layerItem.setMenu(layerMenu);
        layerMenu.addListener(SWT.Hide, menuHideListener);

        layerRegistry = LayerRegistry.createDefaultRegister();
        for (LayerRegistry.LayerInfo info: layerRegistry) {
        	MenuItem item = addMenuItem(layerMenu, info.label, info.description);
        	item.setData(info);
        }

        // Bottom status line
        statusLabel = new Label(shell, SWT.BORDER);
        FormData statusLabelData = new FormData();
        statusLabelData.left = new FormAttachment(0);
        statusLabelData.right = new FormAttachment(100);
        statusLabelData.bottom = new FormAttachment(100);
        statusLabel.setLayoutData(statusLabelData);
        
        // Main GUI with two notebooks
        //int bottomHeight = statusLabel.getSize().y;
        int bottomHeight = 20;
        SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);
        FormData formData = new FormData();
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.top = new FormAttachment(0);
        formData.bottom = new FormAttachment(100, -bottomHeight);
        sashForm.setLayoutData(formData);
        
        mainTabFolder = new CTabFolder(sashForm, SWT.TOP| SWT.BORDER | SWT.CLOSE);
        rightTabFolder = new CTabFolder(sashForm, SWT.TOP | SWT.BORDER);
        int[] weights = {4, 1};
        sashForm.setWeights(weights);
        mainTabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                for (int i=0; i<imageTabs.size(); i++)
                    if (imageTabs.get(i).item == e.item) {
                        currentImageTab = imageTabs.get(i);
                        layers.updateWithSession(currentImageTab.imageView.getSession(),
                        		null);
                        return;
                    }
            }
        });
        
        // Layers view
        layers = new LayersView(rightTabFolder);
        layers.setLayout(new FillLayout());
        CTabItem tmpItem = new CTabItem(rightTabFolder, SWT.NONE);
        tmpItem.setText("Layers");
        tmpItem.setControl(layers);
        rightTabFolder.setSelection(0);

        shell.open();
    }
    
    public void mainLoop() {
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
    
    ImageView addImageView(String filename) {
        ImageView imageView = new ImageView(mainTabFolder);
        imageView.getSession().openFile(filename);
        CTabItem item = new CTabItem(mainTabFolder, SWT.CLOSE);
        File file = new File(filename);
        item.setText(file.getName());
        item.setControl(imageView);
        mainTabFolder.setSelection(item);
        currentImageTab = new ImageTab(item, imageView);
        imageTabs.add(currentImageTab);
        return imageView;
    }

    void doMenuOpen() {
        //status("File->Open");
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterNames(new String[] {"Images (jpeg, tiff, png, gif, bmp)", "All Files"});
        // the following is MS windows-specific
        dialog.setFilterExtensions(new String[] {"*.jpg;*.jpeg;*.tiff;*.tif;*.png;*.gif;*.bmp", "*.*"});
        String filename = dialog.open();
        if (filename != null) {
            ImageView imageView = addImageView(filename);
            imageView.invalidateImage();
            layers.updateWithSession(imageView.getSession(), null);
        }
    }
    
    void doMenuExit() {
        shell.close();
    }
    
    void status(String msg) {
        statusLabel.setText(msg);
    }
    
    void addLayer(Layer layer) {
        //ImageTab tab = getCurrentTab();
        ImageTab tab = currentImageTab;
        if (tab == null)
            return;
        tab.imageView.getSession().addLayer(layer);
        tab.imageView.invalidateImage();
        layers.updateWithSession(tab.imageView.getSession(), layer);
    }
    
    public static void main(String[] args) {
        MainWindow window = new MainWindow();
        window.mainLoop();
    }
}