 /*
 * org.openmicroscopy.shoola.agents.editor.view.EditorPreviewUI 
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
package org.openmicroscopy.shoola.agents.editor.preview;


//Java imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.ScrollablePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * This UI contains a {@link JXTaskPane} showing the metadata preview for an 
 * OMERO.editor file.
 * Based on code from 
 * {@link org.openmicroscopy.shoola.agents.metadata.editor.AcquisitionDataUI}
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class EditorPreviewUI 
	extends ScrollablePanel
	implements PropertyChangeListener
{
	
	/** Reference to the parent EditorPreview that acts as a controller */
	private EditorPreview						controller;
	
	/** The component hosting the image acquisition data. */
	private JPanel								previewPanel;
	
	/** The component hosting the image info. */
	private JXTaskPane 							imagePane;
	
	/** The UI component hosting the <code>JXTaskPane</code>s. */
	private JPanel								container;
	
	/** The constraints used to lay out the components in the container. */
	private GridBagConstraints					constraints;
	
	/** Default text if no data entered. */
	static final String DEFAULT_TEXT = "None";
	
	/** Initializes the UI components. */
	private void initComponents()
	{  
		container = new JPanel();
		
		previewPanel.addPropertyChangeListener(this);
		imagePane = EditorUtil.createTaskPane("OMERO.editor");
		// imagePane.setCollapsed(false);
		imagePane.add(previewPanel);
		imagePane.addPropertyChangeListener(
				UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		container.setBackground(UIUtilities.BACKGROUND_COLOR);
		container.setLayout(new GridBagLayout());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BorderLayout(0, 0));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 2, 2, 0);
		constraints.weightx = 1.0;
	    container.add(imagePane, constraints);
	    
	    add(container, BorderLayout.NORTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller	Reference to the Control. 
	 * 						Mustn't be <code>null</code>.
	 * @param previewPanel	Reference to the preview.
	 */
	EditorPreviewUI(EditorPreview controller, JPanel previewPanel)
	{	
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		this.controller = controller;
		this.previewPanel = previewPanel;
		initComponents();
		buildGUI();
	}
	
	
	/**
	 * Refreshes the title of the {@link #imagePane} from the model. 
	 * Should be called when the model changes. 
	 * 
	 * @param title The value to set.
	 */
	void setTitle(String title)
	{
		imagePane.setTitle(title);
	}
	
	/**
	 * Loads the acquisition metadata.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (!UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE.equals(name)) return;
		
		// load data, if not already loaded. 
		controller.loadPreviewData();
	}
	
}