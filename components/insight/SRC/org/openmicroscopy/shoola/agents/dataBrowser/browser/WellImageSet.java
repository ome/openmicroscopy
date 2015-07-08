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
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import omero.gateway.model.TableResult;
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
	
	/** The description of the well. */
	private String description;
	
	/** The tabular data. */
	private Map<String[], Object[]> tabularData;
	
	/** The tooltip.*/
	private List<String> text;
	
	/** The value by which to shift the row by when laying out the cell.*/
	private int indentRow;
	
	/** The value by which to shift the column by when laying out the cell.*/
	private int indentColumn;
	
	/** 
	 * Sets the default value for the row and column display.
	 * Sets the text displayed in the tool tip.
	 */
	private void setDefault()
	{
		if (rowDisplay == null) setRowDisplay(""+getRow());
		if (columnDisplay == null) setColumnDisplay(""+getColumn());
	}
	
	/** Formats the tool tips. */
	private void formatDisplay()
	{
		Entry<String[], Object[]> entry;
		int size = tabularData.size()-1;
		int index = 0;
		Iterator<Entry<String[], Object[]>>
		i = tabularData.entrySet().iterator();
		String[] headers;
		Object[] values;
		text = new ArrayList<String>();
 		while (i.hasNext()) {
			entry = i.next();
			headers = entry.getKey();
			values = entry.getValue();
			for (int j = 0; j < headers.length; j++) {
				text.add(" "+headers[j]+":"+values[j]);
			}
			if (index < size) text.add("--------------");
			index++;
		}
 		String txt = UIUtilities.formatToolTipText(text);
		Iterator<WellSampleNode> j = samples.iterator();
		WellSampleNode n;
		while (j.hasNext()) {
			n = j.next();
			n.setToolTipText(txt);
			n.setCanvasToolTip(txt);
		}
	}
	
	/** Sets the color of the well. */
	private void setWellColor()
	{
		WellData well = (WellData) getHierarchyObject();
		int r = well.getRed();
		int g = well.getGreen();
		int b = well.getBlue();
		int a = well.getAlpha();
		if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255 &&
				a >= 0 && a <= 255)
			super.setHighlight(new Color(r, g, b, a));
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
		description = well.getWellType();
		setWellColor();
		samples = new ArrayList<WellSampleNode>();
		setDefault();
		rowDisplay = null;
		columnDisplay = null;
		indentRow = 0;
		indentColumn = 0;
	}

	
	/**
	 * Sets the value by which to shift the row by when laying out the cell.
	 * 
	 * @param indentRow The value to set.
	 */
	public void setIndentRow(int indentRow) { this.indentRow = indentRow; }
	
	/**
	 * Sets the value by which to shift the row by when laying out the cell.
	 *  
	 * @param indentColumn The value to set.
	 */
	public void setIndentColumn(int indentColumn)
	{
		this.indentColumn = indentColumn;
	}
	
	/**
	 * Returns the value by which to shift the row by when laying out the cell.
	 * 
	 * @return See above.
	 */
	public int getIndentRow() { return indentRow; }
	
	/**
	 * Returns the value by which to shift the row by when laying out the cell.
	 * 
	 * @return See above.
	 */
	public int getIndentColumn() { return indentColumn; }
	
	/**
	 * Returns the location of the well on the grid as a string.
	 * 
	 * @return See above.
	 */
	public String getWellLocation() 
	{
		StringBuffer buf = new StringBuffer();
		buf.append(rowDisplay+"-"+columnDisplay);
		return buf.toString();
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
		Iterator<WellSampleNode> i = samples.iterator();
		while (i.hasNext()) {
			node = i.next();
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
     * Returns all the well samples linked to that well.
     * 
     * @return A <i>read-only</i> set containing all the child nodes.
     */
	public List<WellSampleNode> getWellSamples()
	{
		return Collections.unmodifiableList(samples);
	}
	
	/** Formats the title of the wells.*/
	public void formatWellSampleTitle()
	{
		if (samples == null || samples.size() == 0) return;
		Iterator<WellSampleNode> i = samples.iterator();
		WellSampleNode wsn;
		while (i.hasNext()) {
			wsn = i.next();
			wsn.setTitle(getCellLabel());
			wsn.setTitleBarType(ImageNode.SMALL_TITLE_BAR);
		}
	}
	
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
		Integer i = ((WellData) getHierarchyObject()).getRow();
		if (i == null) return -1;
		return i; 
	}
	
	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getColumn()
	{
		if (getHierarchyObject() == null) return -1;
		Integer i = ((WellData) getHierarchyObject()).getColumn();
		if (i == null) return -1;
		return i;
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
	 * Returns the label associated to the well e.g. A 1.
	 * 
	 * @return See above.
	 */
	public String getCellLabel()
	{
		return rowDisplay+" "+columnDisplay;
	}
	
	/**
     * Sets the description.
     * 
     * @param description The value to set.
     */
    public void setDescription(String description) 
    { 
    	this.description = description;
    	setDefault();
    }
	
    /**
     * Sets the tabular data for the well.
     * 
     * @param tables The tables to handle.
     */
    public void setTabularData(List<TableResult> tables)
    {
		Iterator<TableResult> i = tables.iterator();
		TableResult table;
		int index;
		Object[][] data;
		long id;
		long wellID = ((WellData) getHierarchyObject()).getId();
		Object[] values;
		tabularData = new HashMap<String[], Object[]>();
		String[] headers;
		while (i.hasNext()) {
			table = i.next();
			index = table.getColumnIndex(TableResult.WELL_COLUMN_INDEX);
			if (index >= 0) {
				data = table.getData();
				headers = table.getHeaders();
				values = new Object[headers.length];
				for (int j = 0; j < data.length; j++) {
					id = (Long) data[j][index];
					if (id == wellID) {
						for (int k = 0; k < values.length; k++) {
							values[k] = data[j][k];
						}
					}
				}
				tabularData.put(headers, values);
			}
		}
		formatDisplay();
    }
    
    /**
     * Returns the text associated to the node.
     * 
     * @return See above.
     */
    public List<String> getText() { return text; }
    
    /**
     * Returns <code>true</code> if the selected well sample is valid, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isSampleValid()
    {
    	ImageData img = getSelectedImage();
    	if (img == null) return false;
    	return img.getId() >= 0;
    }
    
    /**
     * Overridden to make sure that the default color is set.
     * @see ImageSet#setHighlight(Color) 
     */
    public void setHighlight(Color highlight)
    {
    	super.setHighlight(highlight);
		Iterator<WellSampleNode> i = samples.iterator();
		while (i.hasNext()) {
			i.next().setHighlight(highlight);
		}
    }
    
    /**
     * Overridden to return the name of the selected sample.
     * @see ImageSet#getTitle()
     */
    public String getTitle()
    {
    	if (selectedWellSample == null) return "";
    	return selectedWellSample.getTitle();
    }
    
}
