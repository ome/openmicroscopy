/*
 * org.openmicroscopy.shoola.agents.dataBrowser.visitor.SelectionVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.visitor;


//Java imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;

/** 
 * Highlights and selects the nodes.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class SelectionVisitor 
	implements ImageDisplayVisitor
{

	/**
	 * Returns <code>true</code> if one of the corners of the passed rectangle
	 * is contained in the selection, <code>false</code> otherwise
	 * 
	 * @param bounds The bounds of the node.
	 * @return See above.
	 */
	private boolean containsInSelection(Rectangle bounds)
	{
		return (bounds.intersects(selection));
	}
	
	/** The selection rectangle.*/
	private Rectangle selection;
	
	/** The collection of selected nodes.*/
	private List<ImageDisplay> selected;
	
	/** The colors to set when nodes are selected or not.*/
	private Colors colors;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param selection The selection rectangle.
	 * @param collect Pass <code>true</code> to collect the selected node, 
	 *                <code>false</code> otherwise.
	 */
	public SelectionVisitor(Rectangle selection, boolean collect)
	{
		this.selection = selection;
		colors = Colors.getInstance();
		if (collect) selected = new ArrayList<ImageDisplay>();
	}
	
	/**
	 * Returns the collection of selected nodes or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public List<ImageDisplay> getSelected() { return selected; }
	
	/**
	 * Highlights the selected nodes.
	 * @see ImageDisplayVisitor#visit(ImageNode)
	 */
	public void visit(ImageNode node)
	{
		if (selection == null) { //select all.
			node.setHighlight(colors.getSelectedHighLight(node, false));
			if (selected != null) selected.add(node);
		} else {
			if (containsInSelection(node.getBounds())) {
				node.setHighlight(colors.getSelectedHighLight(node, false));
				if (selected != null) selected.add(node);
			} else node.setHighlight(colors.getDeselectedHighLight(node));
		}
	}

	/**
	 * Required by {@link ImageDisplayVisitor} I/F no-operation in our case
	 * @see ImageDisplayVisitor#visit(ImageSet)
	 */
	public void visit(ImageSet node) {}

}
