/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;
import pojos.WellData;
import pojos.WellSampleData;

/** 
 * Handles the well samples related to the well.
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
public class WellImageSet
	extends ImageSet
{

	/** The String indicating how to display the value of the row. */
	private String	 				rowDisplay;
	
	/** The String indicating how to display the value of the column. */
	private String					columnDisplay;
	
	/** The selected well sample data. */
	private ImageNode 				selectedWellSample;
	
	/** Collection of well samples. */
	private List<WellSampleNode> 	samples;
	
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
		Iterator i = samples.iterator();
		ImageNode n;
		String title = "Well: "+rowDisplay+"-"+columnDisplay;
		while (i.hasNext()) {
			n = (ImageNode) i.next();
			n.setToolTipText(txt);
			n.setCanvasToolTip(txt);
			n.setTitle(title+" "+n.getTitle());
		}
	}
	
	/**
	 * Creates a new leaf node.
	 * 
	 * @param well	The original object in the image hierarchy which
	 *              is visualized by this node.  Never pass <code>null</code>.
	 */
	public WellImageSet(WellData well)
	{
		super("", well);
		if (well == null) 
			throw new IllegalArgumentException("Well cannot be null.");
		samples = new ArrayList<WellSampleNode>();
		setDefault();
		rowDisplay = null;
		columnDisplay = null;
	}

	/**
	 * Adds the passed well samples.
	 * 
	 * @param node The value to add.
	 */
	public void addWellSample(WellSampleNode node)
	{
		if (node != null) samples.add(node);
		setSelectedWellSample(0);
	}
	
	/**
	 * Sets the selected well sample.
	 * 
	 * @param index The index of the well samples.
	 */
	public void setSelectedWellSample(int index)
	{
		WellSampleNode node;
		Iterator i = samples.iterator();
		while (i.hasNext()) {
			node = (WellSampleNode) i.next();
			if (node.getIndex() == index)
				selectedWellSample = node;
		}
	}
	
	/**
	 * Returns the selected well sample.
	 * 
	 * @return See above.
	 */
	public ImageNode getSelectedWellSample() { return selectedWellSample; }
	
	/**
	 * Returns the image corresponding to the currently selected wellSample.
	 * 
	 * @return See above.
	 */
	public ImageData getSelectedImage()
	{
		if (selectedWellSample == null) return null;
		WellSampleData 
			wsd = (WellSampleData) selectedWellSample.getHierarchyObject();
		if (wsd == null) return null;
		return wsd.getImage();
	}
	
	/** 
	 * Returns the number of well samples.
	 * 
	 * @return See above.
	 */
	public int getNumberOfSamples() { return samples.size(); }
	
	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getRow()
	{ 
		if (getHierarchyObject() == null) return -1;
		return ((WellData) getHierarchyObject()).getRow(); 
	}
	
	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getColumn()
	{
		if (getHierarchyObject() == null) return -1;
		return ((WellData) getHierarchyObject()).getColumn();
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
	 * Sets the value indicating how to display the cell.
	 * 
	 * @param columnDisplay The value to set.
	 * @param rowDisplay 	The value to set.
	 */
	public void setCellDisplay(String columnDisplay, String rowDisplay)
	{
		this.rowDisplay = rowDisplay;
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
