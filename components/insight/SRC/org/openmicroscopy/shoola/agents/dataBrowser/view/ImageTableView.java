/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.RollOverNode;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;

/** 
 * Embed the {@link ImageTable}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class ImageTableView
	extends JPanel
{

	/**
	 * Bound property indicating that the nodes in the table have been selected.
	 */
	static final String	TABLE_NODES_SELECTION_PROPERTY = "tableNodesSelection";
	
	/** Bound property indicating to display a pop-up menu. */
	static final String TABLE_SELECTION_MENU_PROPERTY = "tableSelectionMenu";
	
	/** 
	 * Bound property indicating to view the last selected node if 
	 * it is an image.
	 */
	static final String TABLE_SELECTION_VIEW_PROPERTY = "tableSelectionView";
	
	/** 
	 * Bound property indicating to roll over the node.
	 */
	static final String TABLE_SELECTION_ROLL_OVER_PROPERTY =
		"tableSelectionRollOver";
	
	/** Reference to the table displaying the nodes. */
	private ImageTable 			table;
	
	/** Reference to the model. */
	private DataBrowserModel 	model;
	
	/** The magnification factor of thumbnail.*/
	private double magnification;
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param root The root node of the tree.
	 */
	private void initComponents(ImageDisplay root)
	{
		table = new ImageTable(root, this, model);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		JScrollPane pane = new JScrollPane(table);
		pane.setBackground(UIUtilities.BACKGROUND_COLOR);
		add(pane, BorderLayout.CENTER);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param model	Reference to the Model. Mustn't be <code>null</code>.
	 * @param root 	The root of the tree.
	 */
	ImageTableView(DataBrowserModel model, ImageDisplay root)
	{
		if (model == null) throw new IllegalArgumentException("No model.");
		this.model = model;
		initComponents(root);
		buildGUI();
	}
	
	/**
     * Brings up the popup menu on top of the specified component at the
     * specified point.
     * 
     * @param location 	The point at which to display the menu, relative to the 
     *          		<code>component</code>'s coordinates.         
     */
	void showMenu(Point location)
	{
		firePropertyChange(TABLE_SELECTION_MENU_PROPERTY, null, location);
	}
	
	/**
     * Returns the {@link ViewerSorter}.
     * 
     * @return See above.
     */
    ViewerSorter getSorter() { return model.getSorter(); }
    
	/** Refreshes the table when filtering data. */
	void refreshTable() { table.refreshTable(); }
	
	/** 
	 * Sets the collection of selected nodes.
	 * 
	 * @param nodes The nodes to set.
	 */
	void selectNodes(List<ImageDisplay> nodes) 
	{
		table.setHighlightedNodes(nodes);
		firePropertyChange(TABLE_NODES_SELECTION_PROPERTY, null, nodes);
	}

	/**
	 * Selects the nodes in the table.
	 * The nodes has been selected via other views.
	 * 
	 * @param objects The selected data objects.
	 */
	void setSelectedNodes(List<DataObject> objects)
	{
		table.setSelectedNodes(objects);
	}

	/** 
	 * Fires a property change to view the last selected node if it is an image.
	 */
	void viewSelectedNode()
	{
		firePropertyChange(TABLE_SELECTION_VIEW_PROPERTY,
				Boolean.valueOf(false), Boolean.valueOf(true));
	}

	/**
	 * Fires a property indicating to show or hide the the node.
	 * 
	 * @param node The node to handle.
	 */
	void rollOverNode(RollOverNode node)
	{
		firePropertyChange(TABLE_SELECTION_ROLL_OVER_PROPERTY,
				Boolean.valueOf(false), node);
	}
	
	/**
	 * Marks the nodes on which a given operation could not be performed
	 * e.g. paste rendering settings.
	 * 
	 * @param type The type of data objects.
	 * @param ids  Collection of object's ids.
	 */
	void markUnmodifiedNodes(Class type, Collection<Long> ids)
	{
		table.markUnmodifiedNodes(type, ids);
	}
	
	/**
	 * Sets the magnification factor.
	 * 
	 * @param magnification The value to set.
	 */
	void setMagnification(double magnification)
	{
		this.magnification = magnification;
	}
	
	/**
	 * Returns the magnification factor.
	 * 
	 * @return See above.
	 */
	double getMagnification() { return magnification; }
	
}
