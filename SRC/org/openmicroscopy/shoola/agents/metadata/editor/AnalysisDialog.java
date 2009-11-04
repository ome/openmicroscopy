/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AnalysisDialog 
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

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ImageData;
import pojos.PixelsData;
import pojos.WellSampleData;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class AnalysisDialog 
	extends JDialog
	implements PropertyChangeListener
{

	/** Horizontal gap between components. */
	private static final int	HORIZONTAL_STRUT = 5;
	
	
	/** The text associated to the FLIM action. */
	private static final String FLIM_TEXT = "";
	
	/** Reference to the control. */
	private EditorControl controller;
	
	/** Reference to the Model. */
	private EditorModel   model;
	
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
	}
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		//setResizable(false);
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
		p.add(createAnalysingControls(), "0, "+index);
		getContentPane().add(p, BorderLayout.NORTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the controller.
	 */
	AnalysisDialog(EditorControl controller, EditorModel model)
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
