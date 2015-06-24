/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

//Third-party libraries

//Application-internal dependencies
import pojos.WellSampleData;
import ome.model.units.BigResult;
import omero.model.enums.UnitsLength;

/** 
 * Display the primary image associated to the wellSample.
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
public class WellSampleNode 
	extends ImageNode
{

	/** Reference to the parent of the well. */
	private WellImageSet	parent;
	
	/** The index of the sample. */
	private int 			index;
	
	/** The height of the title to add to the location.*/
	private int				titleHeight;
	
	/**
     * Creates a new leaf node.
     * 
     * @param title	 The frame's title.
     * @param ho   	 The original object in the image hierarchy which
     *               is visualized by this node. It has to be an image object in 
     *               this case. Never pass <code>null</code>.
     * @param t      The thumbnail this node is going to display. 
     *               This is obviously a thumbnail for the image
     *               object this node represents.
     * @param index  The index of the sample.
     * @param parent Reference to the parent of the well.
     */
	public WellSampleNode(String title, Object ho, Thumbnail t, int index, 
							WellImageSet parent)
	{
		super(title, ho, t);
		if (parent == null)
			throw new IllegalArgumentException("No parent.");
		setTitleBarType(ImageNode.SMALL_TITLE_BAR);
		titleHeight = getTitleBar().getPreferredSize().height;
		setTitleBarType(ImageNode.NO_BAR);
		this.index = index;
		this.parent = parent;
	}

	/** 
	 * Returns the height of the title.
	 * 
	 * @return See above.
	 */
	public int getTitleHeight() { return titleHeight; }
	
	/**
	 * Returns the x-coordinate of the top-left corner that field on the grid.
	 * 
	 * @return See above.
	 */
	public double getPositionX()
	{
		WellSampleData data = (WellSampleData) getHierarchyObject();
		try {
            return data.getPositionX(UnitsLength.REFERENCEFRAME).getValue();
        } catch (BigResult e) {
            // can't do anything sensible at this point
            throw new RuntimeException(e);
        }
	}
	
	/**
	 * Returns the y-coordinate of the top-left corner that field on the grid.
	 * 
	 * @return See above.
	 */
	public double getPositionY()
	{
		WellSampleData data = (WellSampleData) getHierarchyObject();
		try {
            return data.getPositionY(UnitsLength.REFERENCEFRAME).getValue();
        } catch (BigResult e) {
            // can't do anything sensible at this point
            throw new RuntimeException(e);
        }
	}
	
	/**
	 * Returns the index of the well sample.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getRow() { return parent.getRow(); }
	
	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getColumn() { return parent.getColumn(); }
	
	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getLayedoutRow()
	{
		return parent.getRow()-parent.getIndentRow();
	}
	
	/**
	 * Returns the position of the well within the plate.
	 * 
	 * @return See above.
	 */
	public int getLayedoutColumn()
	{
		return parent.getColumn()-parent.getIndentColumn();
	}
	
	/**
	 * Returns the well related to that wellSample.
	 * 
	 * @return See above.
	 */
	public Object getParentObject() { return parent.getHierarchyObject(); }
	
	/**
	 * Returns the well this sample is part of.
	 * 
	 * @return See above.
	 */
	public WellImageSet getParentWell() { return parent; }
	
}
