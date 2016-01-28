/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.swing.JSeparator;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlateData;
import omero.gateway.model.WellSampleData;

/** 
 * Displays the various publishing options.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class PublishingDialog
	extends JPopupMenu
{

	/** The text associated to the movie action. */
	private static final String MOVIE_TOOLTIP = 
		"Creates a movie of the image and attaches it to " +
		"the originating image.";
	
	/** The text associated to the export as OME-TIFF action. */
	private static final String EXPORT_AS_OME_TIFF_TOOLTIP = 
		"Export the image as OME-TIFF.";

	/** The text associated to the SPLIT_VIEW_FIGURE action. */
	private static final String SPLIT_VIEW_FIGURE_TOOLTIP = "" +
			"Create a figure of split-view images.";
	
	/** The text associated to the SPLIT_VIEW_FIGURE action. */
	private static final String SPLIT_VIEW_ROI_FIGURE_TOOLTIP = "" +
			"Create a figure of an ROI region as separate zoomed " +
			"split-channel panels.";
	
	/** The text associated to the movie action. */
	private static final String MOVIE_TEXT = "Make Movie...";
	
	/** The text associated to the export as OME-TIFF action. */
	private static final String EXPORT_AS_OME_TIFF_TEXT = 
		"Export as OME-TIFF...";
	
	/** The text associated to the SPLIT_VIEW_FIGURE action. */
	private static final String SPLIT_VIEW_FIGURE_TEXT = "Split View Figure...";
	
	/** The text associated to the SPLIT_VIEW_FIGURE action. */
	private static final String SPLIT_VIEW_ROI_FIGURE_TEXT = 
		"ROI Split Figure...";
	
	/** The text associated to the Thumbnails action. */
	private static final String THUMBNAILS_FIGURE_TOOLTIP = "" +
			"Export a figure of thumbnails, optionally sorted by tag.";
	
	/** The text associated to the Thumbnails action. */
	private static final String THUMBNAILS_FIGURE_TEXT = "" +
			"Thumbnail Figure...";
	
	/** The text associated to the Movie figure action. */
	private static final String MOVIE_FIGURE_TOOLTIP = "" +
			"Export a figure of a movie.";
	
	/** The text associated to the Thumbnails action. */
	private static final String MOVIE_FIGURE_TEXT = "Movie Figure...";
	
	/** Reference to the control. */
	private EditorControl 	controller;
	
	/** Reference to the Model. */
	private EditorModel   	model;
	
	/** Button to make a movie. */
	private JButton 		movieButton;
	
	/** Button to export an image as OME-TIFF. */
	private JButton 		exportAsOmeTiffButton;

	/** Button to create a split view figure of a collection of images. */
	private JButton 		splitViewFigureButton;
	
	/** Component to make a movie. */
	private JMenuItem 		movieItem;
	
	/** Component to export an image as OME-TIFF. */
	private JMenuItem 		exportAsOmeTiffItem;
	
	/** Component to create a split view figure of a collection of images. */
	private JMenuItem 		splitViewFigureItem;
	
	/** Component to create a split view figure of a collection of images. */
	private JMenuItem 		splitViewROIFigureItem;
	
	/** Component to create thumbnails figure of a collection of images. */
	private JMenuItem 		thumbnailsFigureItem;
	
	/** Component to create movie figure of a collection of images. */
	private JMenuItem 		movieFigureItem;
	
	/** The menu hosting the various options. */
	private JPopupMenu	menu;
	
	/**
	 * Creates a button.
	 * 
	 * @param icon The icon associated to the button.
	 * @param text The text displayed in the tool tip.
	 * @param id   The id of the action.
	 * @return See above.
	 */
	private JButton createButton(Icon icon, String text, int id)
	{
		JButton b = new JButton(icon);
		b.setToolTipText(text);
		b.addActionListener(controller);
		b.setActionCommand(""+id);
		b.setEnabled(false);
		UIUtilities.unifiedButtonLookAndFeel(b);
		return b;
	}
	
	/**
	 * Creates a menu item.
	 * 
	 * @param icon 	  The icon associated to the item.
	 * @param tooltip The text displayed in the tool tip.
	 * @param text    The text associated to the item.
	 * @param id   The id of the action.
	 * @return See above.
	 */
	private JMenuItem createMenuItem(Icon icon, String tooltip, String text, 
			int id)
	{
		JMenuItem b = new JMenuItem(icon);
		b.setText(text);
		b.setToolTipText(tooltip);
		b.addActionListener(controller);
		b.setActionCommand(""+id);
		b.setEnabled(false);
		return b;
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		movieButton = createButton(icons.getIcon(IconManager.SCRIPT_WITH_UI), 
				MOVIE_TOOLTIP, EditorControl.CREATE_MOVIE);
		exportAsOmeTiffButton = createButton(
				icons.getIcon(IconManager.EXPORT_AS_OMETIFF), 
				EXPORT_AS_OME_TIFF_TOOLTIP, 
				EditorControl.EXPORT_AS_OMETIFF);
		splitViewFigureButton = createButton(
				icons.getIcon(IconManager.SCRIPT_WITH_UI), 
				SPLIT_VIEW_FIGURE_TEXT, EditorControl.SPLIT_VIEW_FIGURE);
		splitViewFigureButton.setEnabled(true);
		
		movieItem = createMenuItem(icons.getIcon(IconManager.SCRIPT_WITH_UI), 
				MOVIE_TOOLTIP, MOVIE_TEXT, EditorControl.CREATE_MOVIE);
		exportAsOmeTiffItem = createMenuItem(
				icons.getIcon(IconManager.EXPORT_AS_OMETIFF), 
				EXPORT_AS_OME_TIFF_TOOLTIP, EXPORT_AS_OME_TIFF_TEXT,
				EditorControl.EXPORT_AS_OMETIFF);
		splitViewFigureItem = createMenuItem(
				icons.getIcon(IconManager.SCRIPT_WITH_UI), 
				SPLIT_VIEW_FIGURE_TOOLTIP, SPLIT_VIEW_FIGURE_TEXT,
				EditorControl.SPLIT_VIEW_FIGURE);
		splitViewFigureItem.setEnabled(true);
		splitViewROIFigureItem = createMenuItem(
				icons.getIcon(IconManager.SCRIPT_WITH_UI), 
				SPLIT_VIEW_ROI_FIGURE_TOOLTIP, SPLIT_VIEW_ROI_FIGURE_TEXT,
				EditorControl.SPLIT_VIEW_ROI_FIGURE);
		splitViewFigureButton.setEnabled(true);
		thumbnailsFigureItem = createMenuItem(
				icons.getIcon(IconManager.SCRIPT_WITH_UI), 
				THUMBNAILS_FIGURE_TOOLTIP, THUMBNAILS_FIGURE_TEXT,
				EditorControl.THUMBNAILS_FIGURE);
		thumbnailsFigureItem.setEnabled(true);
		movieFigureItem = createMenuItem(
				icons.getIcon(IconManager.SCRIPT_WITH_UI), 
				MOVIE_FIGURE_TOOLTIP, MOVIE_FIGURE_TEXT,
				EditorControl.MOVIE_FIGURE);
		movieFigureItem.setEnabled(true);
		
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		add(splitViewFigureItem);
		add(splitViewROIFigureItem);
		add(thumbnailsFigureItem);
		add(movieFigureItem);
		add(new JSeparator());
		add(movieItem);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the controller.
	 */
	PublishingDialog(EditorControl controller, EditorModel model)
	{
		this.controller = controller;
		this.model = model;
		initComponents();
		setRootObject();
		buildGUI();
	}
	
	/** Sets the root object. */
	void setRootObject()
	{
		Object refObject = model.getRefObject();
    	ImageData img = null;
    	exportAsOmeTiffButton.setEnabled(false);
    	movieButton.setEnabled(false);
    	splitViewFigureButton.setEnabled(false);
    	
    	exportAsOmeTiffItem.setEnabled(false);
    	movieItem.setEnabled(false);
    	splitViewFigureItem.setEnabled(false);
    	splitViewROIFigureItem.setEnabled(false);
    	movieFigureItem.setEnabled(false);
    	thumbnailsFigureItem.setEnabled(true);
    	if (refObject instanceof ImageData) {
    		img = (ImageData) refObject;
    		thumbnailsFigureItem.setEnabled(true);
    	} else if (refObject instanceof WellSampleData) {
    		img = ((WellSampleData) refObject).getImage();
    		thumbnailsFigureItem.setEnabled(false);
    	}
    	if (img != null) {
    		PixelsData data = null;
    		try {
    			data = img.getDefaultPixels();
    			boolean b = !model.isLargeImage();
    			exportAsOmeTiffButton.setEnabled(b);
    			exportAsOmeTiffItem.setEnabled(b);
    			if (!model.isSingleMode()) {
    			    exportAsOmeTiffItem.setEnabled(false);
    			}
    			movieButton.setEnabled(data.getSizeT() > 1 ||
                        data.getSizeZ() > 1);
                splitViewFigureButton.setEnabled(b && data.getSizeC() > 1);
                movieItem.setEnabled(data.getSizeT() > 1 ||
                        data.getSizeZ() > 1);
                splitViewFigureItem.setEnabled(b);
                splitViewROIFigureItem.setEnabled(b);
                movieFigureItem.setEnabled(true);
			} catch (Exception e) {}
    	} else {
    		if (refObject instanceof DatasetData)
    			thumbnailsFigureItem.setEnabled(true);
    		else if (refObject instanceof PlateData) 
    			thumbnailsFigureItem.setEnabled(false);
    		else thumbnailsFigureItem.setEnabled(false);
    	}
	}

}
