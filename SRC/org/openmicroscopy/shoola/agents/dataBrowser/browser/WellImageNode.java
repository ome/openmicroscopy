/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageNode 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;



//Java imports
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.WellData;

//Third-party libraries

//Application-internal dependencies

/** 
 * Display the primary image associated to the well.
 * This class will probably more responsabilities as the system 
 * evolves.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class WellImageNode
	extends ImageNode
{

	/** The well of reference. */
	private WellData well;
	
	/** The String indicating how to display the value of the row. */
	private String	 rowDisplay;
	
	/** The String indicating how to display the value of the column. */
	private String	 columnDisplay;
	
	/** 
	 * Sets the default value for the row and column display.
	 * Sets the tooltip.
	 */
	private void setDefault()
	{
		if (rowDisplay == null) setRowDisplay(""+getRow());
		if (columnDisplay == null) setColumnDisplay(""+getColumn());
		String txt = 
			UIUtilities.formatToolTipText(rowDisplay+"-"+columnDisplay);
		getTitleBar().setToolTipText(txt);
		setCanvasToolTip(txt);
		setToolTipText(txt);
	}
	
	/**
	 * Creates a new leaf node.
	 * 
	 * @param title             The frame's title.
	 * @param hierarchyObject   The original object in the image hierarchy which
	 *                          is visualized by this node. It has to be an 
	 *                          image object in this case. 
	 *                          Never pass <code>null</code>.
	 * @param t                 The thumbnail this node is going to display. 
	 *                          This is obviously a thumbnail for the image
	 *                          object this node represents.
	 */
	public WellImageNode(String title, Object hierarchyObject, Thumbnail t)
	{
		super(title, hierarchyObject, t);
		setTitleBarType(ImageNode.NO_BAR);
		rowDisplay = null;
		columnDisplay = null;
	}
	
	/**
	 * Sets the well of reference.
	 * 
	 * @param well The value to set. Mustn't be <code>null</code>.
	 */
	public void setWellData(WellData well)
	{
		if (well == null) 
			throw new IllegalArgumentException("Well cannot be null.");
		this.well = well;
		setDefault();
	}

	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getRow()
	{ 
		if (well == null) return -1;
		return well.getRow(); 
	}
	
	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getColumn()
	{
		if (well == null) return -1;
		return well.getColumn();
	}
	
	/**
	 * Sets the value indicating how to display the row.
	 * 
	 * @param rowDisplay The value to set.
	 */
	public void setRowDisplay(String rowDisplay)
	{ 
		this.rowDisplay = rowDisplay;
		setDefault();
	}
	
	/**
	 * Sets the value indicating how to display the column.
	 * 
	 * @param columnDisplay The value to set.
	 */
	public void setColumnDisplay(String columnDisplay)
	{ 
		this.columnDisplay = columnDisplay;
		setDefault();
	}
	
	/**
	 * Returns the UI representation of the row index.
	 * 
	 * @return See above
	 */
	public String getRowDisplay() { return rowDisplay; }
	
	/**
	 * Returns the UI representation of the column index.
	 * 
	 * @return See above
	 */
	public String getColumnDisplay() { return columnDisplay; }
	
}
