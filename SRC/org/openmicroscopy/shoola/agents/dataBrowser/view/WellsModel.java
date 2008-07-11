/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.WellsModel 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.CellDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ImageData;
import pojos.WellData;

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
 * @since 3.0-Beta3
 */
class WellsModel
	extends DataBrowserModel
{

	/** The collection of objects this model is for. */
	private Set<WellData>	wells;

	/** The number of rows. */
	private int			  	rows;
	
	/** The number of columns. */
	private int           	columns;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	The parent of the wells.
	 * @param wells 	The collection to wells the model is for.
	 */
	WellsModel(Object parent, Set<WellData> wells)
	{
		super();
		if (wells  == null) 
			throw new IllegalArgumentException("No wells.");
		this.wells = wells;
		this.parent = parent;
		long userID = DataBrowserAgent.getUserDetails().getId();
		Set wellImageNodes = DataBrowserTranslator.transformHierarchy(wells, 
							userID, 0);
		
		
        Iterator<WellData> i = wells.iterator();
        rows = -1;
        columns = -1;
        WellData well;
        int row, column;
        while (i.hasNext()) {
        	well = i.next();
			row = well.getRow();
			column = well.getColumn();
			if (row > rows) rows = row;
			if (column > columns) columns = column;
		}
		columns++;
		rows++;
		
		Iterator j = wellImageNodes.iterator();
		WellImageNode node;
		while (j.hasNext()) {
			node = (WellImageNode) j.next();
			node.setRowDisplay(EditorUtil.LETTERS.get(node.getRow()+1));
			node.setColumnDisplay(""+(node.getColumn()+1));
		}
		//info should come from the plate.
		for (int k = 1; k <= columns; k++) 
			wellImageNodes.add(new CellDisplay(k-1, ""+k));
		for (int k = 1; k <= rows; k++) 
			wellImageNodes.add(new CellDisplay(k-1, EditorUtil.LETTERS.get(k), 
					CellDisplay.TYPE_VERTICAL));
		
        browser = BrowserFactory.createBrowser(wellImageNodes);
		layoutBrowser(LayoutFactory.PLATE_LAYOUT);
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected DataBrowserLoader createDataLoader(boolean refresh, 
			Collection ids)
	{
		Set l = browser.getImageNodes();
		Iterator i = l.iterator();
		ImageNode node;
		ImageData img;
		List<ImageData> images = new ArrayList<ImageData>();
		while (i.hasNext()) {
			node = (ImageNode) i.next();
			if (node instanceof WellImageNode) {
				img = (ImageData) node.getHierarchyObject();
				if (img.getId() < 0) 
					node.getThumbnail().setFullScaleThumb(
							Factory.createDefaultThumbnail("N/A"));
				else 
					images.add(img);
			}
		}

		if (images.size() == 0) return null;
		return new ThumbnailLoader(component, images);
	}
	
	/**
	 * Returns the type of this model.
	 * @see DataBrowserModel#getType()
	 */
	protected int getType() { return DataBrowserModel.WELLS; }
	
}
