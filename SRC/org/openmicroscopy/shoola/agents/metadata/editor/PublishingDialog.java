/*
 * org.openmicroscopy.shoola.agents.metadata.editor.OptionsDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.WellSampleData;

/** 
 * Displays the various publishing options.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class PublishingDialog
	extends JDialog
{

	/** Horizontal gap between components. */
	private static final int	HORIZONTAL_STRUT = 5;
	
	/** The text associated to the movie action. */
	private static final String MOVIE_TOOLTIP = "Make a movie of the " +
			"selected image.";
	
	/** The text associated to the export as OME-TIFF action. */
	private static final String EXPORT_AS_OME_TIFF_TOOLTIP = 
		"Export the image as OME-TIFF.";

	/** The text associated to the SPLIT_VIEW_FIGURE action. */
	private static final String SPLIT_VIEW_FIGURE_TOOLTIP = "" +
			"Create a split view figure.";
	
	/** The text associated to the SPLIT_VIEW_FIGURE action. */
	private static final String SPLIT_VIEW_ROI_FIGURE_TOOLTIP = "" +
			"Create a split view figure.";
	
	/** The text associated to the movie action. */
	private static final String MOVIE_TEXT = "Make Movie...";
	
	/** The text associated to the export as OME-TIFF action. */
	private static final String EXPORT_AS_OME_TIFF_TEXT = 
		"Export As OME-TIFF...";
	
	/** The text associated to the SPLIT_VIEW_FIGURE action. */
	private static final String SPLIT_VIEW_FIGURE_TEXT = "Split Figure...";
	
	/** The text associated to the SPLIT_VIEW_FIGURE action. */
	private static final String SPLIT_VIEW_ROI_FIGURE_TEXT = 
		"Split ROI Figure...";
	
	/** Reference to the control. */
	private EditorControl controller;
	
	/** Reference to the Model. */
	private EditorModel   model;
	
	/** Button to make a movie. */
	private JButton movieButton;
	
	/** Button to export an image as OME-TIFF. */
	private JButton exportAsOmeTiffButton;

	/** Button to create a split view figure of a collection of images. */
	private JButton splitViewFigureButton;
	
	/** Component to make a movie. */
	private JMenuItem movieItem;
	
	/** Component to export an image as OME-TIFF. */
	private JMenuItem exportAsOmeTiffItem;
	
	/** Component to create a split view figure of a collection of images. */
	private JMenuItem splitViewFigureItem;
	
	/** Component to create a split view figure of a collection of images. */
	private JMenuItem splitViewROIFigureItem;
	
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
		UIUtilities.unifiedButtonLookAndFeel(b);
		return b;
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		movieButton = createButton(icons.getIcon(IconManager.MOVIE), 
				MOVIE_TOOLTIP, EditorControl.CREATE_MOVIE);
		exportAsOmeTiffButton = createButton(
				icons.getIcon(IconManager.EXPORT_AS_OMETIFF), 
				EXPORT_AS_OME_TIFF_TOOLTIP, 
				EditorControl.EXPORT_AS_OMETIFF);
		splitViewFigureButton = createButton(
				icons.getIcon(IconManager.SPLIT_VIEW), 
				SPLIT_VIEW_FIGURE_TEXT, EditorControl.SPLIT_VIEW_FIGURE);
		splitViewFigureButton.setEnabled(true);
		
		movieItem = createMenuItem(icons.getIcon(IconManager.MOVIE), 
				MOVIE_TOOLTIP, MOVIE_TEXT, EditorControl.CREATE_MOVIE);
		exportAsOmeTiffItem = createMenuItem(
				icons.getIcon(IconManager.EXPORT_AS_OMETIFF), 
				EXPORT_AS_OME_TIFF_TOOLTIP, EXPORT_AS_OME_TIFF_TEXT,
				EditorControl.EXPORT_AS_OMETIFF);
		splitViewFigureItem = createMenuItem(
				icons.getIcon(IconManager.SPLIT_VIEW), 
				SPLIT_VIEW_FIGURE_TOOLTIP, SPLIT_VIEW_FIGURE_TEXT,
				EditorControl.SPLIT_VIEW_FIGURE);
		splitViewFigureItem.setEnabled(true);
		splitViewROIFigureItem = createMenuItem(
				icons.getIcon(IconManager.SPLIT_VIEW), 
				SPLIT_VIEW_ROI_FIGURE_TOOLTIP, SPLIT_VIEW_ROI_FIGURE_TEXT,
				EditorControl.SPLIT_VIEW_FIGURE);
		splitViewFigureButton.setEnabled(true);
	}
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		//setResizable(false);
	}
	
	/** 
	 * Creates the component displaying the publishing controls.
	 * 
	 * @return See above.
	 */
	private JComponent createPublishingControls()
	{
		JToolBar bar = new JToolBar();
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(movieButton);
    	bar.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
        bar.add(exportAsOmeTiffButton);
        bar.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
        bar.add(splitViewFigureButton);
        return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		double[] columns = {TableLayout.FILL};
		TableLayout layout = new TableLayout();
		layout.setColumn(columns);
		p.setLayout(layout);
		int index = 0;
		layout.insertRow(index, TableLayout.PREFERRED);
		p.add(createPublishingControls(), "0, "+index);
		getContentPane().add(p, BorderLayout.NORTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the controller.
	 */
	PublishingDialog(EditorControl controller, EditorModel model)
	{
		super(MetadataViewerAgent.getRegistry().getTaskBar().getFrame());
		this.controller = controller;
		this.model = model;
		setProperties();
		initComponents();
		setRootObject();
		buildGUI();
		pack();
	}

	/**
	 * Displays the menu 
	 * @return
	 */
	JPopupMenu displayAsMenu()
	{
		if (menu != null) return menu;
		menu = new JPopupMenu();
		menu.add(movieItem);
		menu.add(exportAsOmeTiffItem);
		menu.add(splitViewFigureItem);
		menu.add(splitViewROIFigureItem);
		return menu;
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
    	if (refObject instanceof ImageData) {
    		img = (ImageData) refObject;
    	} else if (refObject instanceof WellSampleData) {
    		img = ((WellSampleData) refObject).getImage();
    	}
    	if (img != null) {
    		PixelsData data = null;
    		try {
    			data = img.getDefaultPixels();
    			exportAsOmeTiffButton.setEnabled(true);
    			movieButton.setEnabled(data.getSizeT() > 1 || 
    					data.getSizeZ() > 1);
    			splitViewFigureButton.setEnabled(data.getSizeC() > 1);
    			exportAsOmeTiffItem.setEnabled(true);
    			movieItem.setEnabled(data.getSizeT() > 1 || 
    					data.getSizeZ() > 1);
    			splitViewFigureItem.setEnabled(data.getSizeC() > 1);
    			splitViewROIFigureItem.setEnabled(data.getSizeC() > 1);
			} catch (Exception e) {}
    	}
	}

}
