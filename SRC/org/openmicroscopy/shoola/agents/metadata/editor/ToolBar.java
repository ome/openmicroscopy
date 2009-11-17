/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ToolBar 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;


//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PixelsData;
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
	
	/** Button to save the annotations. */
	private JButton			saveButton;

	/** Button to create a movie from the original image. */
	private JButton			createMovieButton;
	
	/** Button to download the original image. */
	private JButton			downloadButton;

	/** Button to load the rendering control for the primary select. */
	private JButton			rndButton;
	
	/** Button to refresh the selected tab. */
	private JButton			refreshButton;
	
	/** Button to analyze the image. */
	private JButton			flimButton;
	
	/** Button to export the image. */
	private JButton			exportButton;
	
	/** Indicates the loading progress. */
	private JXBusyLabel		busyLabel;
	
	/** Indicates the movie creation. */
	private JXBusyLabel		busyMovieLabel;

	/** Indicates an on-going FLIM analysis. */
	private JXBusyLabel		busyFLimLabel;
	
	/** 
	 * The component hosting the control only used when an <code>Image</code>
	 * is selected.
	 */
	private JComponent		imageBar;

	/** Reference to the Control. */
	private EditorControl	controller;
	
	/** Reference to the Model. */
	private EditorModel 	model;

	/** Initializes the components. */
	private void initComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		IconManager icons = IconManager.getInstance();
		int h = UIUtilities.DEFAULT_ICON_HEIGHT;
		int w = UIUtilities.DEFAULT_ICON_WIDTH;
		Icon icon = icons.getIcon(IconManager.SAVE);
		if (icon != null) {
			if (icon.getIconHeight() > h) h = icon.getIconHeight();
			if (icon.getIconWidth() > w) w = icon.getIconWidth();
		}
		saveButton = new JButton(icon);
		saveButton.setToolTipText("Save changes back to the server.");
		saveButton.addActionListener(controller);
		saveButton.setActionCommand(""+EditorControl.SAVE);
		saveButton.setEnabled(false);
		
		icon = icons.getIcon(IconManager.DOWNLOAD);
		if (icon != null) {
			if (icon.getIconHeight() > h) h = icon.getIconHeight();
			if (icon.getIconWidth() > w) w = icon.getIconWidth();
		}
		downloadButton = new JButton(icon);
		downloadButton.setToolTipText("Download the Archived File(s).");
		downloadButton.addActionListener(controller);
		downloadButton.setActionCommand(""+EditorControl.DOWNLOAD);
		downloadButton.setEnabled(false);
		
		icon = icons.getIcon(IconManager.RENDERER);
		if (icon != null) {
			if (icon.getIconHeight() > h) h = icon.getIconHeight();
			if (icon.getIconWidth() > w) w = icon.getIconWidth();
		}
		rndButton = new JButton(icon);
		rndButton.setToolTipText("Rendering control for the primary selected " +
				"image.");
		rndButton.addActionListener(controller);
		rndButton.setActionCommand(""+EditorControl.RENDERER);
		rndButton.setEnabled(false);
		icon = icons.getIcon(IconManager.MOVIE);
		if (icon != null) {
			if (icon.getIconHeight() > h) h = icon.getIconHeight();
			if (icon.getIconWidth() > w) w = icon.getIconWidth();
		}
		createMovieButton = new JButton(icon);
		createMovieButton.setToolTipText("Create a movie from the " +
				"selected image.");
		createMovieButton.addActionListener(controller);
		createMovieButton.setActionCommand(""+EditorControl.CREATE_MOVIE);
		createMovieButton.setEnabled(false);
		
		icon = icons.getIcon(IconManager.ANALYSE);
		flimButton = new JButton(icon);
		flimButton.setToolTipText("Analyse the image.");
		flimButton.addActionListener(controller);
		flimButton.setActionCommand(""+EditorControl.ANALYSE_FLIM);
		flimButton.setEnabled(false);
		
		icon = icons.getIcon(IconManager.REFRESH);
		refreshButton = new JButton(icon);
		refreshButton.setToolTipText("Refresh the selected tab.");
		refreshButton.addActionListener(controller);
		refreshButton.setActionCommand(""+EditorControl.REFRESH);
		
		icon = icons.getIcon(IconManager.EXPORT_AS_OMETIFF);
		exportButton = new JButton(icon);
		exportButton.setToolTipText("Export the image.");
		exportButton.addActionListener(controller);
		exportButton.setActionCommand(""+EditorControl.EXPORT);
		
		UIUtilities.unifiedButtonLookAndFeel(saveButton);
		UIUtilities.unifiedButtonLookAndFeel(downloadButton);
		UIUtilities.unifiedButtonLookAndFeel(createMovieButton);
		UIUtilities.unifiedButtonLookAndFeel(rndButton);
		UIUtilities.unifiedButtonLookAndFeel(flimButton);
		UIUtilities.unifiedButtonLookAndFeel(refreshButton);
		UIUtilities.unifiedButtonLookAndFeel(exportButton);
		
		
		Dimension d = new Dimension(w, h);
    	busyLabel = new JXBusyLabel(d);
    	busyLabel.setEnabled(true);
    	busyLabel.setVisible(false);
    	
    	busyMovieLabel = new JXBusyLabel(d);
    	busyMovieLabel.setEnabled(true);
    	busyMovieLabel.setToolTipText("Creating movie. Please wait.");
    	
    	busyFLimLabel = new JXBusyLabel(d);
    	busyFLimLabel.setEnabled(true);
    	busyFLimLabel.setToolTipText("Analyzin. Please wait.");
	}
    
    /** 
     * Builds the tool bar displaying the controls related to 
     * an image.
     * 
     * @return See above.
     */
    private JComponent buildImageToolBar()
    {
    	JToolBar bar = new JToolBar();
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(createMovieButton);
    	//if (model.isLifetime()) {
    	bar.add(Box.createHorizontalStrut(5));
        bar.add(exportButton);
    	//bar.add(Box.createHorizontalStrut(5));
        //bar.add(flimButton);
    	return bar;
    }
    
    /** 
     * Builds the general bar.
     * 
     * @return See above.
     */
    private JComponent buildGeneralBar()
    {
    	JToolBar bar = new JToolBar();
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(saveButton);
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(refreshButton);
    	/*
    	if (model.getRndIndex() == MetadataViewer.RND_GENERAL) {
    		bar.add(Box.createHorizontalStrut(5));
        	bar.add(rndButton);
    	}
    	*/
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(downloadButton);
    	return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	JPanel bars = new JPanel();
    	bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
    	bars.add(buildGeneralBar());
    	bars.add(Box.createHorizontalStrut(2));
    	imageBar = buildImageToolBar();
    	imageBar.setVisible(false);
    	bars.add(imageBar);
    	JPanel p = new JPanel();
    	p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    	p.add(UIUtilities.buildComponentPanel(bars));
    	p.add(UIUtilities.buildComponentPanelRight(busyLabel));
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	add(p);
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
    
    /** Enables the various controls. */
    void setControls()
    { 
    	Object refObject = model.getRefObject();
    	ImageData img = null;
    	
    	if (refObject instanceof ImageData) {
    		img = (ImageData) refObject;
    	} else if (refObject instanceof WellSampleData) {
    		img = ((WellSampleData) refObject).getImage();
    	}
    	if (img != null) {
    		PixelsData data = null;
    		try {
    			data = img.getDefaultPixels();
    			createMovieButton.setEnabled(data.getSizeT() > 1 || 
    					data.getSizeZ() > 1);
			} catch (Exception e) {}
    	}
    	if (refObject instanceof FileAnnotationData) {
    		downloadButton.setEnabled(true); 
    	} else 
    		downloadButton.setEnabled(model.isArchived()); 
    }
    
    /**
     * Enables the {@link #saveButton} depending on the passed value.
     * 
     * @param b Pass <code>true</code> to save the data,
     * 			<code>false</code> otherwise. 
     */
    void setDataToSave(boolean b) { saveButton.setEnabled(b); }
    
    /**
     * Replaces the {@link #createMovieButton} (resp.  {@link #busyMovieLabel})
     * by the {@link #busyMovieLabel} (resp. {@link #createMovieButton})
     * if the passed value is <code>false</code> (resp. <code>true</code>). 
     * 
     * @param b Pass <code>true</code> if movie creation,
     * 			<code>false</code> when it is done.
     */
    void createMovie(boolean b)
    { 
    	if (imageBar != null) {
    		busyMovieLabel.setBusy(b);
    		if (!b) {
    			imageBar.remove(busyMovieLabel);
    			imageBar.add(createMovieButton, 0);
        	} else {
        		imageBar.remove(createMovieButton);
    			imageBar.add(busyMovieLabel, 0);
        	}
    		imageBar.revalidate();
    		imageBar.repaint();
    	}
    	createMovieButton.setEnabled(!b);  
    }
    
    /**
     * Replaces the {@link #flimButton} (resp.  {@link #busyFLimLabel})
     * by the {@link #busyFLimLabel} (resp. {@link #flimButton})
     * if the passed value is <code>false</code> (resp. <code>true</code>). 
     * 
     * @param b Pass <code>true</code> if movie creation,
     * 			<code>false</code> when it is done.
     */
    void analyse(boolean b)
    { 
    	if (imageBar != null) {
    		busyFLimLabel.setBusy(b);
    		if (!b) {
    			imageBar.remove(busyFLimLabel);
    			imageBar.add(flimButton, 0);
        	} else {
        		imageBar.remove(flimButton);
    			imageBar.add(busyFLimLabel, 0);
        	}
    		imageBar.revalidate();
    		imageBar.repaint();
    	}
    	flimButton.setEnabled(!b);  
    }
    
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
    	Object refObject = model.getRefObject();
    	rndButton.setEnabled(false);
		imageBar.setVisible(false);
		downloadButton.setEnabled(false);
    	if ((refObject instanceof ImageData) || 
    			(refObject instanceof WellSampleData)) {
    		rndButton.setEnabled(!model.isRendererLoaded());
    		
    		if (model.isNumerousChannel()) {
    			rndButton.setEnabled(false);
    			flimButton.setEnabled(true);
    		} else {
    			flimButton.setEnabled(false);
    		}
    		if (refObject instanceof ImageData) 
    			downloadButton.setEnabled(model.isArchived());
    		imageBar.setVisible(!model.isMultiSelection());
    	} else if (refObject instanceof FileAnnotationData) {
    		downloadButton.setEnabled(true);
    	}
    	revalidate();
    	repaint();
    }
	
}
