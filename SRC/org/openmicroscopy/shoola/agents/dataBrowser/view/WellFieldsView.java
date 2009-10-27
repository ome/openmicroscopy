/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.WellFieldsView 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;



//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.util.ui.PlateGrid;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays all the fields of a given well.
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
class WellFieldsView
	extends JPanel
{

	/** The grid representing the plate. */
	private PlateGrid 			grid;
	
	/** Reference to the model. */
	private WellsModel 			model;
	
	/** Reference to the controller. */
	private DataBrowserControl 	controller;
	
	/** Component displaying the thumbnails. */
	private WellFieldsCanvas	canvas;
	
	/** The collection of nodes to display. */
	private List<WellSampleNode> nodes;
	
	/** Initializes the components. */
	private void initComponents()
	{
		grid = new PlateGrid(model.getRowSequenceIndex(), 
				model.getColumnSequenceIndex(), model.getValidWells());
		grid.addPropertyChangeListener(controller);
		WellImageSet node = model.getSelectedWell();
		if (node != null)
			grid.selectCell(node.getRow(), node.getColumn());
		canvas = new WellFieldsCanvas(this);
		nodes = null;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(new LineBorder(new Color(99, 130, 191)));
		setLayout(new BorderLayout(0, 0));
		add(canvas, BorderLayout.CENTER);
		add(UIUtilities.buildComponentPanel(grid), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	 Reference to the model.
	 * @param controller Reference to the control.
	 */
	WellFieldsView(WellsModel model, DataBrowserControl controller)
	{
		this.model = model;
		this.controller = controller;
		initComponents();
		buildGUI();
	}
	
	/** 
	 * Returns the fields to display if any.
	 * 
	 * @return See above.
	 */
	List<WellSampleNode> getNodes() { return nodes; }
	
	/**
	 * Sets the selected cell.
	 * 
	 * @param row 	 The row identifying the cell.
	 * @param column The column identifying the cell.
	 */
	void selectCell(int row, int column) { grid.selectCell(row, column); }

	/**
	 * Displays the passed fields.
	 * 
	 * @param nodes The nodes hosting the fields.
	 */
	void displayFields(List<WellSampleNode> nodes)
	{
		this.nodes = nodes;
		canvas.repaint();
	}
	
}
