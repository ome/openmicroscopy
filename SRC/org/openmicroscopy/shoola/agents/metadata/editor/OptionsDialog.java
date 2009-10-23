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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolBar;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.WellSampleData;

/** 
 * Displays the various publishing options, and analysis options.
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
class OptionsDialog
	extends JDialog
	implements PropertyChangeListener
{

	/** Horizontal gap between components. */
	private static final int	HORIZONTAL_STRUT = 5;
	
	/** The text associated to the movie action. */
	private static final String MOVIE_TEXT = "Make a movie of the " +
			"selected image.";
	
	/** The text associated to the export as OME-TIFF action. */
	private static final String EXPORT_AS_OME_TIFF_TEXT = "Export the image" +
			" as OME-TIFF.";
	
	/** The text associated to the FLIM action. */
	private static final String FLIM_TEXT = "";
	
	/** Reference to the control. */
	private EditorControl controller;
	
	/** Reference to the Model. */
	private EditorModel   model;
	
	/** Button to make a movie. */
	private JButton movieButton;
	
	/** Button to export an image as OME-TIFF. */
	private JButton exportAsOmeTiffButton;
	
	/** Button to perform <code>FLIM</code> analysis. */
	private JButton flimButton;
	
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
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		movieButton = createButton(icons.getIcon(IconManager.MOVIE), 
				MOVIE_TEXT, EditorControl.CREATE_MOVIE);
		exportAsOmeTiffButton = createButton(
				icons.getIcon(IconManager.EXPORT_AS_OMETIFF), 
				EXPORT_AS_OME_TIFF_TEXT, EditorControl.EXPORT);
		flimButton = createButton(icons.getIcon(IconManager.ANALYSE), 
				FLIM_TEXT, EditorControl.ANALYSE_FLIM);
		
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
	private JXTaskPane createPublishingControls()
	{
		JToolBar bar = new JToolBar();
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(movieButton);
    	bar.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
        bar.add(exportAsOmeTiffButton);
        JXTaskPane pane = EditorUtil.createTaskPane("Publish");
		pane.setCollapsed(false);
    	pane.addPropertyChangeListener(this);
		pane.add(bar);
        return pane;
	}
	
	/** 
	 * Creates the component displaying the first level of routines.
	 * 
	 * @return See above.
	 */
	private JXTaskPane createAnalysingControls()
	{
		JToolBar bar = new JToolBar();
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	JXTaskPane pane = EditorUtil.createTaskPane("Analyse");
 		pane.setCollapsed(false);
    	pane.addPropertyChangeListener(this);
 		pane.add(bar);
        return pane;
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
		index++;
		layout.insertRow(index, TableLayout.PREFERRED);
		p.add(createAnalysingControls(), "0, "+index);
		getContentPane().add(p, BorderLayout.NORTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the controller.
	 */
	OptionsDialog(EditorControl controller, EditorModel model)
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

	/** Sets the root object. */
	void setRootObject()
	{
		Object refObject = model.getRefObject();
    	ImageData img = null;
    	exportAsOmeTiffButton.setEnabled(false);
    	movieButton.setEnabled(false);
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
			} catch (Exception e) {}
    	}
    	flimButton.setEnabled(!model.isNumerousChannel());
	}

	/**
	 * Listens to the property fired the taskPane.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent);
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		
		if (UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE.equals(name)) {
			//TODO:
		}
	}

}
