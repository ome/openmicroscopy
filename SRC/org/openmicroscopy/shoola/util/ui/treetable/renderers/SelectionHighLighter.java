/*
 * org.openmicroscopy.shoola.util.ui.treetable.renderers.SelectionHighLighter 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.treetable.renderers;


//Java imports
import java.awt.Component;


//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;

//Application-internal dependencies

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Renderer used to highlight selection. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SelectionHighLighter
	extends ColorHighlighter
{	
	
	/** The table of reference. */
	private JXTreeTable table;
	
	/**
	 * Returns <code>true</code> if the row is in the selected rows field.
	 * 
	 * @param row 	The row to handle.
	 * @param rows 	The selected rows.
	 * @return See above.
	 */
	private boolean isSelected(int row, int [] rows)
	{
		for (int i = 0 ; i < rows.length ; i++)
			if (row == rows[i]) return true;
		return false;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param table Reference to the table. Mustn't be <code>null</code>.
	 */
	public SelectionHighLighter(JXTreeTable table)
	{
		if (table == null)
			throw new IllegalArgumentException("No table specified.");
		this.table = table;
	}

	/**
	 * Overridden to set the correct background
	 * @see ColorHighlighter#applyBackground(Component, ComponentAdapter)
	 */
	protected void applyBackground(Component renderer, ComponentAdapter adapter) 
	{
		if (isSelected(adapter.row, table.getSelectedRows()))
			renderer.setBackground(UIUtilities.SELECTED_BACKGROUND_COLOUR);
	}

}


