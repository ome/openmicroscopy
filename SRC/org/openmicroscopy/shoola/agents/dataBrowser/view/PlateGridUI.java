/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.PlateGridUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;
import org.openmicroscopy.shoola.util.ui.PlateGrid;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays a grid representing a plate.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class PlateGridUI 
	extends JPanel
{

	/** The text for the selected well. */
	private static final String	DEFAULT_WELL_TEXT = "Well: ";
	
	/** The text for the selected field. */
	private static final String	DEFAULT_FIELD_TEXT = "Field #";
	
	/** Reference to the model. */
	private WellsModel 			model;
	
	/** The grid representing the plate. */
	private PlateGrid 			grid;
	
	/** The currently selected well. */
	private JLabel				 selectedNode;
	
	/** The currently selected field. */
	private JLabel				 selectedField;
	
	/** Reference to the controller. */
	private DataBrowserControl 	controller;
	
	/** Initializes the components. */
	private void initComponents()
	{
		selectedField = new JLabel();
		grid = new PlateGrid(model.getRowSequenceIndex(), 
				model.getColumnSequenceIndex(), model.getValidWells(), 
				model.getRows(), model.getColumns());
		grid.addPropertyChangeListener(controller);
		WellImageSet node = model.getSelectedWell();
		selectedNode = new JLabel();
		if (node != null) {
			selectedNode.setText(DEFAULT_WELL_TEXT+node.getWellLocation());
			grid.selectCell(node.getRow(), node.getColumn());
		}
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		double[][] size = {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
				TableLayout.FILL}};
		setLayout(new TableLayout(size));
		add(grid, "0, 0, 0, 2");
		add(selectedNode, "2, 0, LEFT, TOP");
		add(selectedField, "2, 1, LEFT, TOP");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	 	Reference to the model.
	 * @param controller 	Reference to the control.
	 */
	PlateGridUI(WellsModel model, DataBrowserControl controller)
	{
		this.model = model;
		this.controller = controller;
		initComponents();
		buildGUI();
	}
	
	/** Invokes when a well is selected. */
	void onSelectedWell()
	{
		WellImageSet node = model.getSelectedWell();
		if (node != null) {
			selectedNode.setText(DEFAULT_WELL_TEXT+node.getWellLocation());
			grid.selectCell(node.getRow(), node.getColumn());
		}
	}
	
}
