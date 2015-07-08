/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ToolBar 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;


//Third-party libraries
import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.util.FilesetInfoDialog;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.ScriptSubMenu;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.filter.file.CppFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JavaFilter;
import org.openmicroscopy.shoola.util.filter.file.MatlabFilter;
import org.openmicroscopy.shoola.util.filter.file.PythonFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.WellSampleData;

/** 
 * The tool bar of the editor.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ToolBar 
	extends JPanel
{
	
	/** The text associated to the export as OME-TIFF action. */
	private static final String EXPORT_AS_OME_TIFF_TOOLTIP = 
		"Export the image as OME-TIFF.";
	
	/** Button to save the annotations. */
	private JButton			saveButton;

	/** Button to download the original image. */
	private JButton			downloadButton;

	/** Button to load the rendering control for the primary select. */
	private JButton			rndButton;
	
	/** Button to refresh the selected tab. */
	private JButton			refreshButton;

	/** Button to bring up the analysis list. */
	private JButton			analysisButton;
	
	/** Button to bring up the publishing list. */
	private JButton			publishingButton;
	
	/** Button to bring up the list of scripts. */
	private JButton			scriptsButton;
	
	/** Button to export an image as OME-TIFF. */
	private JButton 		exportAsOmeTiffButton;
	
	/** Button to upload the script. */
	private JButton			uploadScriptButton;
	
	/** Button to save files as JPEG, OME-TIFF, download, etc. */
	private JButton			saveAsButton;
	
	/** The menu displaying the saving option. */
	private JPopupMenu		saveAsMenu;
	
	/** Indicates the loading progress. */
	private JXBusyLabel		busyLabel;

	/** Reference to the Control. */
	private EditorControl	controller;
	
	/** Reference to the Model. */
	private EditorModel 	model;

	/** The location of the mouse clicked. */
	private Point			location;
	
	/** The option dialog. */
	private PublishingDialog  publishingDialog;
	
	/** The option dialog. */
	private AnalysisDialog  	analysisDialog;

	/** Component used to download the archived file.*/
	private JMenuItem downloadItem;
	
	/** Component used to download the archived file.*/
	private JMenuItem exportAsOmeTiffItem;
	
	/** View the image.*/
	private JButton viewButton;

	/** The Button displaying the path to the file on the server.*/
	private JButton pathButton;
	
	/** Menu for displaying the 'Located in' information */
	private JButton locationButton;
	
	/** The component where the mouse clicked occurred.*/
	private Component component;

    /** Turns off some controls if the binary data are not available. */
    private void checkBinaryAvailability()
    {
    	if (MetadataViewerAgent.isBinaryAvailable()) return;
    	downloadButton.setEnabled(false); 
    	rndButton.setEnabled(false);
    	publishingButton.setEnabled(false);
		analysisButton.setEnabled(false);
    }
    
    /** Creates or recycles the save as menu. */
    private JPopupMenu createSaveAsMenu()
    {
        saveAsMenu = new JPopupMenu();
        IconManager icons = IconManager.getInstance();
        downloadItem = new JMenuItem(icons.getIcon(IconManager.DOWNLOAD));
        downloadItem.setToolTipText("Download the Archived File(s).");
        downloadItem.setText("Download...");
        downloadItem.addActionListener(controller);
        downloadItem.setActionCommand(""+EditorControl.DOWNLOAD);
        downloadItem.setBackground(UIUtilities.BACKGROUND_COLOR);
        List<DataObject> nodes = model.getSelectedObjects();
        boolean b = false;
        if (!CollectionUtils.isEmpty(nodes)) {
            Iterator<DataObject> i = nodes.iterator();
            while (i.hasNext()) {
                if (model.isArchived(i.next())) {
                    b = true;
                    break;
                }
            }
        }
        downloadItem.setEnabled(b);
        saveAsMenu.add(downloadItem);

        exportAsOmeTiffItem = new JMenuItem(icons.getIcon(
                IconManager.EXPORT_AS_OMETIFF));
        exportAsOmeTiffItem.setText("Export as OME-TIFF...");
        exportAsOmeTiffItem.setToolTipText(EXPORT_AS_OME_TIFF_TOOLTIP);
        exportAsOmeTiffItem.addActionListener(controller);
        exportAsOmeTiffItem.setActionCommand(
                ""+EditorControl.EXPORT_AS_OMETIFF);
        if (model.isMultiSelection()) b = false;
        else {
            b = model.getRefObject() instanceof ImageData &&
                    !model.isLargeImage();
        }
        exportAsOmeTiffItem.setEnabled(b);
        saveAsMenu.add(exportAsOmeTiffItem);
        ActionListener l = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = Integer.parseInt(e.getActionCommand());
                controller.saveAs(index);
            }
        };
        Map<Integer, String> formats = FigureParam.FORMATS;
        Entry<Integer, String> e;
        Iterator<Entry<Integer, String>> i = formats.entrySet().iterator();
        JMenuItem item;
        Object ho = model.getRefObject();
        boolean enabled = (ho instanceof ImageData ||
                ho instanceof WellSampleData || ho instanceof DatasetData);
        while (i.hasNext()) {
            e = i.next();
            item = new JMenuItem(icons.getIcon(
                    IconManager.EXPORT_AS_OMETIFF));
            item.setText("Export as "+e.getValue()+"...");
            item.addActionListener(l);
            item.setActionCommand(""+e.getKey());
            item.setEnabled(enabled);
            saveAsMenu.add(item);
        }
        setRootObject();
    	return saveAsMenu;
    }
    
    /** 
     * Creates or recycles the view menu.
     * 
     * @param source The source of the mouse event.
     * @param p The location of the mouse pressed.
     */
    private void showViewMenu(Component source, Point p)
    {
    	JPopupMenu menu = new JPopupMenu();
    	IconManager icons = IconManager.getInstance();
    	JMenuItem item = new JMenuItem(icons.getIcon(IconManager.VIEWER));
    	item.setText("View...");
    	item.setActionCommand(""+EditorControl.VIEW_IMAGE);
    	item.addActionListener(controller);
    	menu.add(item);
    	item = new JMenuItem(icons.getIcon(IconManager.VIEWER_IJ));
    	item.setText("View in ImageJ...");
    	item.setActionCommand(""+EditorControl.VIEW_IMAGE_IN_IJ);
    	item.addActionListener(controller);
    	menu.add(item);
    	menu.show(source, p.x, p.y);
    }
    
	/** Initializes the components. */
	private void initComponents()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		IconManager icons = IconManager.getInstance();
		saveButton = new JButton(icons.getIcon(IconManager.SAVE));
		saveButton.setToolTipText("Save changes back to the server.");
		saveButton.addActionListener(controller);
		saveButton.setActionCommand(""+EditorControl.SAVE);
		saveButton.setEnabled(false);
		saveButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		downloadButton = new JButton(icons.getIcon(IconManager.DOWNLOAD));
		downloadButton.setToolTipText("Download the Archived File(s).");
		downloadButton.addActionListener(controller);
		downloadButton.setActionCommand(""+EditorControl.DOWNLOAD);
		//downloadButton.setEnabled(false);
		downloadButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		rndButton = new JButton(icons.getIcon(IconManager.RENDERER));
		rndButton.setToolTipText("Rendering control for the first selected " +
				"image.");
		rndButton.addActionListener(controller);
		rndButton.setActionCommand(""+EditorControl.RENDERER);
		rndButton.setEnabled(false);
		rndButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		refreshButton = new JButton(icons.getIcon(IconManager.REFRESH));
		refreshButton.setToolTipText("Refresh.");
		refreshButton.addActionListener(controller);
		refreshButton.setActionCommand(""+EditorControl.REFRESH);
		refreshButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		publishingButton = new JButton(icons.getIcon(IconManager.PUBLISHING));
		publishingButton.setToolTipText("Display the publishing options.");
		publishingButton.setEnabled(false);
		publishingButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		publishingButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Launches the dialog when the user releases the mouse.
			 * MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				launchOptions((Component) e.getSource(), e.getPoint(), 
						MetadataViewer.PUBLISHING_OPTION);
			}
		});
		analysisButton = new JButton(icons.getIcon(IconManager.ANALYSIS));
		analysisButton.setToolTipText("Display the analysis options.");
		analysisButton.setEnabled(false);
		analysisButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		analysisButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Launches the dialog when the user releases the mouse.
			 * MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				launchOptions((Component) e.getSource(), e.getPoint(), 
						MetadataViewer.ANALYSIS_OPTION);
			}
		});
		scriptsButton = new JButton(icons.getIcon(IconManager.ANALYSIS_RUN));
		scriptsButton.setToolTipText("Display the available scripts.");
		scriptsButton.setEnabled(false);
		scriptsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		scriptsButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Loads the scripts of displays them if already loaded.
			 * MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				if (model.getScripts() == null) {
					location = e.getPoint();
					scriptsButton.setEnabled(false);
					model.loadScripts();
					setStatus(true);
				} else {
					launchOptions((Component) e.getSource(), e.getPoint(), 
						MetadataViewer.SCRIPTS_OPTION);
				}
			}
		});
		exportAsOmeTiffButton = new JButton(icons.getIcon(
				IconManager.EXPORT_AS_OMETIFF));
		exportAsOmeTiffButton.setEnabled(false);
		exportAsOmeTiffButton.setToolTipText(EXPORT_AS_OME_TIFF_TOOLTIP);
		exportAsOmeTiffButton.addActionListener(controller);
		exportAsOmeTiffButton.setActionCommand(
				""+EditorControl.EXPORT_AS_OMETIFF);
		exportAsOmeTiffButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		uploadScriptButton = new JButton(icons.getIcon(
				IconManager.UPLOAD_SCRIPT));
		uploadScriptButton.setToolTipText("Upload a script to the server.");
		uploadScriptButton.addActionListener(controller);
		uploadScriptButton.setActionCommand(""+EditorControl.UPLOAD_SCRIPT);
		uploadScriptButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		saveAsButton = new JButton(icons.getIcon(
				IconManager.EXPORT_AS_OMETIFF));
		saveAsButton.setToolTipText("Display the saving options.");
		saveAsButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Displays the saving options.
			 * MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				launchOptions((Component) e.getSource(), e.getPoint(),
						MetadataViewer.SAVE_OPTION);
			}
		});
		saveAsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		viewButton = new JButton("Full Viewer");
		viewButton.setToolTipText("Open full viewer.");
		if (MetadataViewerAgent.runAsPlugin() == LookupNames.IMAGE_J) {
			viewButton.addMouseListener(new MouseAdapter() {
				
				/**
				 * Displays the <code>view</code> menu.
				 * @see MouseListener#mouseReleased(MouseEvent)
				 */
				public void mouseReleased(MouseEvent e) {
					showViewMenu((Component) e.getSource(), e.getPoint());
				}
			});
		} else {
			viewButton.setActionCommand(""+EditorControl.VIEW_IMAGE);
	    	viewButton.addActionListener(controller);
		}
		
        MouseListener pathLocML = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                location = e.getPoint();
                component = (Component) e.getSource();
            }
        };
		
		pathButton = new JButton(icons.getIcon(IconManager.FILE_PATH));
        pathButton.setToolTipText("Show file paths on server.");
        pathButton.addActionListener(controller);
        pathButton.setActionCommand(""+EditorControl.SHOW_FILE_PATHS);
        pathButton.setEnabled(model.isSingleMode() && model.getImage() != null);
        pathButton.addMouseListener(pathLocML);
        
        locationButton = new JButton(icons.getIcon(IconManager.DATASET));
        locationButton.setToolTipText("Show parent Projects & Datasets.");
        locationButton.addActionListener(controller);
        locationButton.setActionCommand(""+EditorControl.SHOW_LOCATION);
        locationButton.setEnabled(model.isSingleMode() && model.getImage() != null);
        locationButton.addMouseListener(pathLocML);
		
		UIUtilities.unifiedButtonLookAndFeel(pathButton);
		UIUtilities.unifiedButtonLookAndFeel(locationButton);
		UIUtilities.unifiedButtonLookAndFeel(saveAsButton);
		UIUtilities.unifiedButtonLookAndFeel(saveButton);
		UIUtilities.unifiedButtonLookAndFeel(downloadButton);
		UIUtilities.unifiedButtonLookAndFeel(rndButton);
		UIUtilities.unifiedButtonLookAndFeel(refreshButton);
		UIUtilities.unifiedButtonLookAndFeel(exportAsOmeTiffButton);
		UIUtilities.unifiedButtonLookAndFeel(publishingButton);
		UIUtilities.unifiedButtonLookAndFeel(uploadScriptButton);
		UIUtilities.unifiedButtonLookAndFeel(analysisButton);
		UIUtilities.unifiedButtonLookAndFeel(scriptsButton);
		
		Dimension d = new Dimension(UIUtilities.DEFAULT_ICON_WIDTH, 
				UIUtilities.DEFAULT_ICON_HEIGHT);
    	busyLabel = new JXBusyLabel(d);
    	busyLabel.setEnabled(true);
    	busyLabel.setVisible(false);
	}
	
    /** 
     * Builds the general bar.
     * 
     * @return See above.
     */
    private JComponent buildGeneralBar()
    {
    	JToolBar bar = new JToolBar();
    	bar.setBackground(UIUtilities.BACKGROUND_COLOR);
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
    	
        bar.add(viewButton);
        bar.add(Box.createHorizontalGlue());
    	bar.add(saveButton);
    	bar.add(Box.createHorizontalStrut(5));
        bar.add(publishingButton);
        bar.add(Box.createHorizontalStrut(5));
        bar.add(locationButton);
        bar.add(Box.createHorizontalStrut(5));
        bar.add(pathButton);
        bar.add(Box.createHorizontalStrut(5));
        bar.add(saveAsButton);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(busyLabel);
        
    	return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	setBackground(UIUtilities.BACKGROUND_COLOR);
    	add(buildGeneralBar());
    	add(new JSeparator());
    }
    
    /**
     * Sets the icon associated to the script.
     * 
     * @param so The script to handle.
     */
    private void setScriptIcon(ScriptObject so)
    {
    	if (so.getIcon() != null) return;
    	Icon icon = null, largeIcon = null;
    	Iterator<CustomizedFileFilter> i = EditorUtil.SCRIPTS_FILTERS.iterator();
    	CustomizedFileFilter filter;
    	IconManager icons = IconManager.getInstance();
    	while (i.hasNext()) {
    		filter = i.next();
			if (filter.accept(so.getName())) {
				if (filter instanceof CppFilter) {
					largeIcon = icons.getIcon(IconManager.CPP_48);
					icon = icons.getIcon(IconManager.CPP);
				} else if (filter instanceof MatlabFilter) {
					icon = icons.getIcon(IconManager.MATLAB);
					largeIcon = icons.getIcon(IconManager.MATLAB_48);
				} else if (filter instanceof JavaFilter) {
					icon = icons.getIcon(IconManager.JAVA);
					largeIcon = icons.getIcon(IconManager.JAVA_48);
				} else if (filter instanceof PythonFilter) {
					icon = icons.getIcon(IconManager.PYTHON);
					largeIcon = icons.getIcon(IconManager.PYTHON_48);
				}
				break;
			}
		}
    	if (icon == null)
    		icon = icons.getIcon(IconManager.ANALYSIS);
    	if (largeIcon == null)
    		largeIcon = icons.getIcon(IconManager.ANALYSIS_48);
    	so.setIcon(icon);
    	so.setIconLarge(largeIcon);
    }
    
    /** 
     * Builds the menu displaying the available scripts.
     * 
     * @return See above.
     */
    private JPopupMenu getScriptsMenu()
    {
    	JPopupMenu menu = new JPopupMenu();
    	
    	Collection<ScriptObject> scripts = model.getScripts();
    	//Scripts are sorted.
    	if (scripts == null || scripts.size() == 0) return menu;
    	IconManager icons = IconManager.getInstance();
    	JMenuItem refresh = new JMenuItem(icons.getIcon(
				IconManager.REFRESH));
    	refresh.setText("Reload Scripts");
    	refresh.setToolTipText("Reloads the existing scripts.");
    	refresh.addActionListener(controller);
    	refresh.setActionCommand(""+EditorControl.RELOAD_SCRIPT);
    	menu.add(refresh);
    	menu.add(new JSeparator());
    	
    	Iterator<ScriptObject> i = scripts.iterator();
    	ScriptObject so;
    	Map<String, ScriptSubMenu> menus = new HashMap<String, ScriptSubMenu>();
    	String path;
    	ScriptSubMenu subMenu;
    	List<ScriptSubMenu> others = new ArrayList<ScriptSubMenu>();
    	List<String> formattedName = new ArrayList<String>();
    	while (i.hasNext()) {
    		so = i.next();
    		setScriptIcon(so);
    		path = so.getPath();
    		subMenu = menus.get(path);
    		if (subMenu == null) {
    			subMenu = new ScriptSubMenu(path, formattedName);
    			//formattedName.add(subMenu.getUnformattedText());
    			menus.put(path, subMenu);
    			if (so.isOfficialScript()) menu.add(subMenu);
    			else others.add(subMenu);
    		}
    		//if (!ScriptMenuItem.isScriptWithUI(so.getScriptLabel()))
    			subMenu.addScript(so).addActionListener(controller);
    	}
    	if (others.size() > 0) {
    		menu.add(new JSeparator());
    		JMenu uploadedMenu = new JMenu("User Scripts");
    		menu.add(uploadedMenu);
    		Iterator<ScriptSubMenu> j = others.iterator();
        	while (j.hasNext()) 
        		uploadedMenu.add(j.next());
    	}
    	return menu;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model 		Reference to the model. 
     * 						Mustn't be <code>null</code>.
     * @param controller 	Reference to the view. Mustn't be <code>null</code>.
     */
    ToolBar(EditorModel model, EditorControl controller)
    {
    	if (model == null)
    		throw new IllegalArgumentException("No model.");
    	if (controller == null)
    		throw new IllegalArgumentException("No control.");
    	this.model = model;
    	this.controller = controller;
    	initComponents();
    	buildGUI();
    }

    /**
     * Enables the {@link #saveButton} depending on the passed value.
     * 
     * @param b Pass <code>true</code> to save the data,
     * 			<code>false</code> otherwise. 
     */
    void setDataToSave(boolean b) { saveButton.setEnabled(b); }
    
    /**
     * Sets to <code>true</code> if loading data, to <code>false</code>
     * otherwise.
     * 
     * @param busy 	Pass <code>true</code> while loading data, 
     * 				<code>false</code> otherwise.
     */
    void setStatus(boolean busy)
    {
    	busyLabel.setBusy(busy);
    	busyLabel.setVisible(busy);
    }
    
    /** Updates the UI when a new object is selected. */
    void buildUI()
    {
    	saveAsMenu = null;
    	Object refObject = model.getRefObject();
    	rndButton.setEnabled(false);
		downloadButton.setEnabled(false);
		if (pathButton != null) pathButton.setEnabled(false);
    	if ((refObject instanceof ImageData) || 
    			(refObject instanceof WellSampleData)) {
    		rndButton.setEnabled(!model.isRendererLoaded());
    		if (model.isNumerousChannel())
    			rndButton.setEnabled(false);
    		if (refObject instanceof ImageData) {
    			downloadButton.setEnabled(model.isArchived());
    		}
    		if (pathButton != null) pathButton.setEnabled(model.isSingleMode());
    	} else if (refObject instanceof FileAnnotationData) {
    		downloadButton.setEnabled(true);
    	}
    	setRootObject();
    	checkBinaryAvailability();
    	revalidate();
    	repaint();
    }

    /** Sets the root object. */
	private void setRootObject()
	{ 
        Object ref = model.getRefObject();
        if (ref instanceof ExperimenterData || ref instanceof GroupData) {
            publishingButton.setEnabled(false);
            analysisButton.setEnabled(false);
            scriptsButton.setEnabled(false);
            return;
        }
        
        pathButton.setEnabled(false);
        locationButton.setEnabled(false);
        viewButton.setEnabled(false);
        exportAsOmeTiffButton.setEnabled(false);
        
        if (model.isSingleMode()) {
            exportAsOmeTiffButton.setEnabled(model.getImage() != null
                    && !model.isLargeImage());
            viewButton.setEnabled(model.getImage() != null);
            pathButton.setEnabled(model.getImage() != null);
            locationButton.setEnabled((model.getImage() != null || model
                    .getRefObject() instanceof DatasetData));
        }

        publishingButton.setEnabled(true);
        analysisButton.setEnabled(true);
        scriptsButton.setEnabled(true);
        if (publishingDialog != null)
            publishingDialog.setRootObject();
        if (analysisDialog != null)
            analysisDialog.setRootObject();
	}

	/**
	 * Launches the Options.
	 * 
	 * @param source The location of the mouse pressed.
	 * @param p 	 The location of the mouse pressed.
	 * @param index  Identifies the menu to pop up.
	 */
	void launchOptions(Component source, Point p, int index)
	{
		if (p == null) p = new Point(0, 0);
		switch (index) {
			case MetadataViewer.PUBLISHING_OPTION:
				if (publishingDialog == null)
					publishingDialog = new PublishingDialog(controller, model);
				publishingDialog.show(source, p.x, p.y);
				break;

			case MetadataViewer.ANALYSIS_OPTION:
				if (analysisDialog == null)
					analysisDialog = new AnalysisDialog(controller, model);
				analysisDialog.show(source, p.x, p.y);
				break;
			case MetadataViewer.SCRIPTS_OPTION:
				getScriptsMenu().show(source, p.x, p.y);
				break;
			case MetadataViewer.SAVE_OPTION:
				createSaveAsMenu().show(source, p.x, p.y);
		}
	}
	
	/** Sets the scripts. */
	void setScripts()
	{
		scriptsButton.setEnabled(true);
		setStatus(false);
		launchOptions(scriptsButton, location, MetadataViewer.SCRIPTS_OPTION);
		location = null;
	}
	
	/** Invokes when the size is loaded.*/
	void onSizeLoaded()
	{
		if (exportAsOmeTiffItem != null) {
			boolean b = model.getRefObject() instanceof ImageData && 
					!model.isLargeImage();
			exportAsOmeTiffButton.setEnabled(b);
		}
	}
	
        /** Displays the file set associated to the image. */
        void displayFileset() {
            SwingUtilities.convertPointToScreen(location, component);
            FilesetInfoDialog d = new FilesetInfoDialog();
            d.setData(model.getFileset(), model.getImportType());
            d.pack();
            if (location != null) {
                location = new Point(location.x - d.getSize().width,
                        location.y + 10);
            }
            d.open(location);
        }
        
        /**
         * Shows the location dialog
         */
        void displayLocation() {
            JComponent comp = model.getBrowser().getUI();
            model.loadParents();      
            SwingUtilities.convertPointToScreen(location, component);
            TinyDialog d = new TinyDialog(null, new JScrollPane(comp), TinyDialog.CLOSE_ONLY);
            d.getContentPane().setBackground(comp.getBackground());
            if (location != null) {
                location = new Point(location.x - 400,
                        location.y + 10);
            }
            d.setLocation(location);
            d.setSize(new Dimension(400,130));
            d.setResizable(true);
            d.setVisible(true);
        }
}

