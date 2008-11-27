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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.DataObject;
import pojos.ImageData;
import pojos.WellData;
import pojos.WellSampleData;

/** 
 * A concrete model for a plate.
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
	
	/** The number of rows. */
	private int			  		rows;
	
	/** The number of columns. */
	private int           		columns;
	
	/** The dimension of a well. */
	private Dimension 			wellDimension;
	
	/** The collection of nodes hosting the wells. */
	private Set					wellNodes;
	
	/** The collection of nodes used to display cells e.g. A-1. */
	private Set<CellDisplay> 	cells;
	
	/** The number of fields per well. */
	private int					fieldsNumber;
	
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
		wellDimension = null;
		this.parent = parent;
		long userID = DataBrowserAgent.getUserDetails().getId();
		wellNodes = DataBrowserTranslator.transformHierarchy(wells, 
							userID, 0);
		Set<ImageDisplay> samples = new HashSet<ImageDisplay>();
		cells = new HashSet<CellDisplay>();
        rows = -1;
        columns = -1;
        int row, column;
		Iterator j = wellNodes.iterator();
		WellImageSet node;
		ImageNode selected;
		int f = -1;
		while (j.hasNext()) {
			node = (WellImageSet) j.next();
			row = node.getRow();
			column = node.getColumn();
			if (row > rows) rows = row;
			if (column > columns) columns = column;
			//TODO: modify when info available from plate.
			node.setCellDisplay(""+(column+1), EditorUtil.LETTERS.get(row+1));
			f = node.getNumberOfSamples();
			if (fieldsNumber < f) fieldsNumber = f;
			selected = node.getSelectedWellSample();
			samples.add(selected);
			if (((DataObject) selected.getHierarchyObject()).getId() >= 0 &&
					wellDimension == null) {
				wellDimension = selected.getThumbnail().getOriginalSize();
			}
		}
		
		columns++;
		rows++;
		
		//info should come from the plate.
		CellDisplay cell;
		for (int k = 1; k <= columns; k++) {
			cell = new CellDisplay(k-1, ""+k);
			samples.add(cell);
			cells.add(cell);
		}
		for (int k = 1; k <= rows; k++) {
			cell = new CellDisplay(k-1, EditorUtil.LETTERS.get(k), 
					CellDisplay.TYPE_VERTICAL);
			samples.add(cell);
			cells.add(cell);
		}
        browser = BrowserFactory.createBrowser(samples);
		layoutBrowser(LayoutFactory.PLATE_LAYOUT);
	}
	
	/**
	 * Returns the number of fields per well
	 * 
	 * @return See above.
	 */
	int getFieldsNumber() { return fieldsNumber; }
	
	/**
	 * Views the selected field.
	 * 
	 * @param index The index of the field to view.
	 */
	void viewField(int index)
	{
		if (index < 0 || index >= fieldsNumber) return;
		Set<ImageDisplay> samples = new HashSet<ImageDisplay>();
		Set l = getNodes();
		Iterator i = l.iterator();
		WellImageSet well;
		while (i.hasNext()) {
			well = (WellImageSet) i.next();
			well.setSelectedWellSample(index);
			samples.add(well.getSelectedWellSample());
		}
		samples.addAll(cells);
		browser.refresh(samples);
		layoutBrowser(LayoutFactory.PLATE_LAYOUT);
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected DataBrowserLoader createDataLoader(boolean refresh, 
			Collection ids)
	{
		Set l = getNodes();
		Iterator i = l.iterator();
		ImageSet node;
		List<ImageData> images = new ArrayList<ImageData>();
		ImageNode selected;
		WellSampleData data;
		while (i.hasNext()) {
			node = (ImageSet) i.next();
			if (node instanceof WellImageSet) {
				selected = ((WellImageSet) node).getSelectedWellSample();
				//img = (ImageData) selected.getHierarchyObject();
				data = (WellSampleData) selected.getHierarchyObject();
				if (data.getId() < 0)
					selected.getThumbnail().setFullScaleThumb(
							Factory.createDefaultThumbnail(wellDimension.width, 
									wellDimension.height, "N/A"));
				else 
					images.add(data.getImage());
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
	
	/**
	 * No-op implementation in our case.
	 * @see DataBrowserModel#getNodes()
	 */
	protected Set<ImageDisplay> getNodes() { return wellNodes; }
	
	
}
