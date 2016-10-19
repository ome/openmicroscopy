/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.PlateSaver;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailFieldsLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailProvider;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.CellDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.DecoratorVisitor;

import omero.gateway.model.TableResult;
import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.PlateGrid;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.WellGridElement;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourObject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;

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
	private List<ImageDisplay> wellNodes;
	
	/** The collection of nodes used to display cells e.g. A-1. */
	private Set<CellDisplay> 	cells;
	
	/** The number of fields per well. */
	private int					fieldsNumber;
	
    /**
     * The selected field. (default field index which is selected in the
     * toolbar)
     */
    private int defaultFieldIndex;
	
	/** Indicates how to display a row. */
	private int					rowSequenceIndex;
	
	/** Indicates how to display a column. */
	private int					columnSequenceIndex;
	
	/** Value indicating if the wells are valid or not. */
	private List<WellGridElement>	validWells;
	
	/** Flag indicating to load or not the thumbnails. */
	private boolean				withThumbnails;
	
	/** The selected nodes. Note: Can contain wells and fields! */
	private List<WellSampleNode> selectedNodes;
	
	/** 
	 * Sorts the passed nodes by row.
	 * 
	 * @param nodes The nodes to sort.
	 * @return See above.
	 */
	private List<ImageDisplay> sortByRow(Set nodes)
	{
		List<ImageDisplay> l = new ArrayList<ImageDisplay>();
		if (nodes == null) return l;
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			l.add((ImageDisplay) i.next());
		}
		Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                WellData w1 = (WellData) 
                		((WellImageSet) o1).getHierarchyObject(),
                         w2 = (WellData) 
                         ((WellImageSet) o2).getHierarchyObject();
                int n1 = w1.getRow();
                int n2 = w2.getRow();
                int v = 0;
                if (n1 < n2) v = -1;
                else if (n1 > n2) v = 1;
                else if (n1 == n2) {
                	int c1 = w1.getColumn();
                	int c2 = w2.getColumn();
                	 if (c1 < c2) v = -1;
                     else if (c1 > c2) v = 1;
                }
                return v;
            }
        };
        Collections.sort(l, c);
		return l;
	}
	
	/**
	 * Creates the color related to the passed Well.
	 * 
	 * @param data The well to handle.
	 * @return See above.
	 */
	private Color createColor(WellData data)
	{
		int red = data.getRed();
		int green = data.getGreen();
		int blue = data.getBlue();
		int alpha = data.getAlpha();
		if (red < 0 || green < 0 || blue < 0 || alpha < 0) return null;
		if (red > 255 || green > 255 || blue > 255 || alpha > 255) return null;
		return new Color(red, green, blue, alpha);
	}
	
	/**
	 * Returns <code>true</code> if the passed description are the same, 
	 * <code>false</code> otherwise.
	 * 
	 * @param d1 The color to handle.
	 * @param d2 The color to handle.
	 * @return See above.
	 */
	private boolean isSameDescription(String d1, String d2)
	{
		if (d1 == null && d2 == null) return true;
		if (d1 == null && d2 != null) return false;
		if (d1 != null && d2 == null) return false;
		if (d1 != null && d2 != null) {
			String t1 = d1.trim();
			String t2 = d2.trim();
			if (t1 != null && t2 != null)
				return t1.equals(t2);
			return false;
		}
		return false;
	}
	
	/**
	 * Handles the selection of a cell
	 * 
	 * @param cell The selected cell.
	 * @param well The well to handle.
	 * @param results The collection of objects to update.
	 */
	private void handleCellSelection(CellDisplay cell, WellImageSet well,
			List<DataObject> results)
	{
		String description = cell.getDescription();
		Color c = cell.getHighlight();
		WellData data = (WellData) well.getHierarchyObject();
		data.setWellType(description);
		well.setDescription(description);
		results.add(data);
		if (c == null || !cell.isSpecified()) {
			data.setRed(null);
		} else {
			data.setRed(c.getRed());
			data.setGreen(c.getGreen());
			data.setBlue(c.getBlue());
			data.setAlpha(c.getAlpha());
		}
		well.setHighlight(c);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param parent	The parent of the wells.
	 * @param wells 	The collection to wells the model is for.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
	 */
	WellsModel(SecurityContext ctx, Object parent, Collection<WellData> wells, 
			boolean withThumbnails)
	{
		super(ctx);
		if (wells == null)
			throw new IllegalArgumentException("No wells.");
		this.withThumbnails = withThumbnails;
		selectedNodes = new ArrayList<WellSampleNode>();
		wellDimension = null;
		this.parent = parent;
		wellNodes = sortByRow(DataBrowserTranslator.transformHierarchy(wells));

		PlateData plate = (PlateData) parent;
		columnSequenceIndex = plate.getColumnSequenceIndex();
		rowSequenceIndex = plate.getRowSequenceIndex();
		defaultFieldIndex = plate.getDefaultSample();
		if (defaultFieldIndex < 0) 
		    defaultFieldIndex = 0;
		Set<ImageDisplay> samples = new HashSet<ImageDisplay>();
		cells = new HashSet<CellDisplay>();
        rows = -1;
        columns = -1;
        int row, column;
		Iterator<ImageDisplay> j = wellNodes.iterator();
		WellImageSet node;
		ImageNode selected;
		int f;
		String columnSequence;
		String rowSequence;
		Map<Integer, ColourObject> cMap = new HashMap<Integer, ColourObject>();
		Map<Integer, ColourObject> rMap = new HashMap<Integer, ColourObject>();
		WellData data;
		String type;
		ColourObject co;
		Color color;
		boolean b;
		validWells = new ArrayList<WellGridElement>();
		int minRow = -1;
		int minColumn = -1;
		while (j.hasNext()) {
			node = (WellImageSet) j.next();
			row = node.getRow();
			column = node.getColumn();
			data = (WellData) node.getHierarchyObject();
			type = data.getWellType();
			if (cMap.containsKey(column)) {
				co = cMap.get(column);
				color = createColor(data);
				if (!UIUtilities.isSameColors(co.getColor(), color, true) ||
						!isSameDescription(co.getDescription(), type)) {
					co.setColor(null);
					co.setDescription(null);
					cMap.put(column, co);
				}
			} else {
				cMap.put(column, new ColourObject(createColor(data), type));
			}
			
			if (rMap.containsKey(row)) {
				co = rMap.get(row);
				color = createColor(data);
				if (!UIUtilities.isSameColors(co.getColor(), color, true) ||
						!isSameDescription(co.getDescription(), type)) {
					co.setColor(null);
					co.setDescription(null);
					rMap.put(row, co);
				}
			} else {
				rMap.put(row, new ColourObject(createColor(data), type));
			}
			if (row > rows) rows = row;
			if (column > columns) columns = column;
			
			if (minRow < 0 || minRow > row) {
				minRow = row;
			}
			
			if (minColumn < 0 || minColumn > column) {
				minColumn = column;
			}
			columnSequence = "";
			if (columnSequenceIndex == PlateData.ASCENDING_LETTER)
				columnSequence = UIUtilities.LETTERS.get(column+1);
			else if (columnSequenceIndex == PlateData.ASCENDING_NUMBER)
				columnSequence = ""+(column+1);
			rowSequence = "";
			if (rowSequenceIndex == PlateData.ASCENDING_LETTER)
				rowSequence = UIUtilities.LETTERS.get(row+1);
			else if (rowSequenceIndex == PlateData.ASCENDING_NUMBER)
				rowSequence = ""+(row+1);
			node.setCellDisplay(columnSequence, rowSequence);
			f = node.getNumberOfSamples();
			if (fieldsNumber < f) 
			    fieldsNumber = f;
			node.setSelectedWellSample(defaultFieldIndex);
			selected = node.getSelectedWellSample();
			//set the title to Row/Column
			node.formatWellSampleTitle();
			samples.add(selected);
			b = false;
			if (node.isSampleValid()) {
				wellDimension = selected.getThumbnail().getOriginalSize();
				b = true;
			}
			validWells.add(new WellGridElement(row, column, b));
		}
		//
		if (minRow >= 0 || minColumn >= 0) {
			j = wellNodes.iterator();
			while (j.hasNext()) {
				node = (WellImageSet) j.next();
				if (minRow > 0)
				    node.setIndentRow(minRow);
				if (minColumn > 0)
				    node.setIndentColumn(minColumn);
				if (node.getRow() == minRow || node.getColumn() == minColumn)
				    node.formatWellSampleTitle();
			}
		}
		
		columns++;
		rows++;
		CellDisplay cell;
		for (int k = 1; k <= columns; k++) {
			columnSequence = "";
			if (columnSequenceIndex == PlateData.ASCENDING_LETTER)
				columnSequence = UIUtilities.LETTERS.get(k+1);
			else if (columnSequenceIndex == PlateData.ASCENDING_NUMBER)
				columnSequence = ""+k;
			cell = new CellDisplay(k-1, columnSequence);
			co = cMap.get(k-1);
			if (co != null) {
				cell.setHighlight(co.getColor());
				cell.setDescription(co.getDescription());
			}
			//if (!isMac)
			//samples.add(cell);
			//cells.add(cell);
		}
		for (int k = 1; k <= rows; k++) {
			rowSequence = "";
			if (rowSequenceIndex == PlateData.ASCENDING_LETTER)
				rowSequence = UIUtilities.LETTERS.get(k);
			else if (rowSequenceIndex == PlateData.ASCENDING_NUMBER)
				rowSequence = ""+k;
			
			cell = new CellDisplay(k-1, rowSequence, CellDisplay.TYPE_VERTICAL);
			co = rMap.get(k-1);
			if (co != null) {
				cell.setHighlight(co.getColor());
				cell.setDescription(co.getDescription());
			}
			//if (!isMac)
			//samples.add(cell);
			//cells.add(cell);
		}
		browser = BrowserFactory.createBrowser(samples);
        browser.accept(new DecoratorVisitor(getCurrentUser().getId()));
        
		layoutBrowser(LayoutFactory.PLATE_LAYOUT);
		if (wellDimension == null)
			wellDimension = new Dimension(ThumbnailProvider.THUMB_MAX_WIDTH,
					ThumbnailProvider.THUMB_MAX_HEIGHT);
	}
	
	/**
	 * Indicates how to display a column. 
	 * 
	 * @return See above.
	 */
	int getColumnSequenceIndex()
	{
		if (columnSequenceIndex == PlateData.ASCENDING_LETTER)
			return PlateGrid.ASCENDING_LETTER;
		return PlateGrid.ASCENDING_NUMBER;
	}
	
	/**
	 * Indicates how to display a row. 
	 * 
	 * @return See above.
	 */
	int getRowSequenceIndex()
	{
		if (rowSequenceIndex == PlateData.ASCENDING_LETTER)
			return PlateGrid.ASCENDING_LETTER;
		return PlateGrid.ASCENDING_NUMBER;
	}
	
	/** 
	 * Returns an array indicating the valid wells.
	 * 
	 * @return See above.
	 */
	List<WellGridElement> getValidWells() { return validWells; }
	
	/**
	 * Returns the number of fields per well.
	 * 
	 * @return See above.
	 */
	int getFieldsNumber() { return fieldsNumber; }
	
	/**
	 * Returns the selected field, the default value is <code>0</code>.
	 * 
	 * @return See above.
	 */
	int getDefaultFieldIndex() { return defaultFieldIndex; }

    /**
     * Sets the selected well.
     * 
     * @param node
     *            The selected node.
     */
    void setSelectedWell(WellSampleNode node) {
        selectedNodes.clear();
        selectedNodes.add(node);
    }
    
	/**
	 * Sets the selected wells. 
	 * 
	 * @param nodes The selected nodes.
	 */
	void setSelectedWells(List<WellSampleNode> nodes)
	{
        selectedNodes.clear();
        if (nodes != null) {
            selectedNodes = nodes;
        }
	}
	
    /**
     * Returns the selected well. Note: Can be a well or a field (@see
     * {@link WellSampleNode#isWell()} )!
     * 
     * @return See above.
     */
    WellSampleNode getSelectedWell() {
        if (CollectionUtils.isEmpty(selectedNodes))
            return null;

        return selectedNodes.get(0);
    }
	
	/**
	 * Returns the collection of selected wells.
	 * Note: Can contain wells and fields (@see {@link WellSampleNode#isWell()} )!
	 * @return See above.
	 */
	List<WellSampleNode> getSelectedWells() { return selectedNodes; }
	
	/**
	 * Views the selected field.
	 * 
	 * @param index The index of the field to view.
	 */
	void viewField(int index)
	{
		if (index < 0 || index >= fieldsNumber) return;
		defaultFieldIndex = index;
		Set<ImageDisplay> samples = new HashSet<ImageDisplay>();
		List<ImageDisplay> l = getNodes();
		Iterator<ImageDisplay> i = l.iterator();
		WellImageSet well;
		int row = -1;
		int col = -1;
		Collection<ImageDisplay> c = browser.getSelectedDisplays();
		Map<Integer, Integer> location = new HashMap<Integer, Integer>();
		WellSampleNode selected;
		if (c != null && c.size() > 0) {
			Iterator<ImageDisplay> j = c.iterator();
			Object object;
			while (j.hasNext()) {
				object = j.next();
				if (object instanceof WellSampleNode) {
					selected = (WellSampleNode) object;
					location.put(selected.getRow(), selected.getColumn());
				}
			}
		}
		List<ImageDisplay> nodes = new ArrayList<ImageDisplay>();
		while (i.hasNext()) {
			well = (WellImageSet) i.next();
			well.setSelectedWellSample(index);
			selected = (WellSampleNode) well.getSelectedWellSample();
			row = selected.getRow();
			if (location.containsKey(row)) {
				col = location.get(row);
				if (selected.getColumn() == col) nodes.add(selected);
			}
			samples.add(selected);
		}
		samples.addAll(cells);
		browser.refresh(samples, nodes);
		layoutBrowser(LayoutFactory.PLATE_LAYOUT);
		//quietly save the field.

		PlateData plate = (PlateData) parent;
		long userID = DataBrowserAgent.getUserDetails().getId();
		if (plate.getOwner().getId() == userID) {
			plate.setDefaultSample(defaultFieldIndex);
			List<DataObject> list = new ArrayList<DataObject>();
			list.add(plate);
			DataBrowserLoader loader = new PlateSaver(component, ctx, list);
			loader.load();
		}
	}
	
	/**
	 * Sets the tabular data.
	 * 
	 * @param data The value to set.
	 */
	void setTabularData(List<TableResult> data)
	{
		List<ImageDisplay> nodes = getNodes();
		if (nodes == null || nodes.size() == 0) return;
		Iterator<ImageDisplay> i = nodes.iterator();
		WellImageSet well;
		while (i.hasNext()) {
			well = (WellImageSet) i.next();
			well.setTabularData(data);
		}
	}
	
	/**
	 * Sets the values for the row or the column.
	 * Returns the collection of wells to update.
	 * 
	 * @param cell The selected cell.
	 */
	void setSelectedCell(CellDisplay cell)
	{
		if (cell == null) return;
		List<DataObject> results = new ArrayList<DataObject >();
		List<ImageDisplay> l = getNodes();
		Iterator<ImageDisplay> i = l.iterator();
		WellImageSet well;
		int index = cell.getIndex();
		if (cell.getType() == CellDisplay.TYPE_HORIZONTAL) {
			while (i.hasNext()) {
				well = (WellImageSet) i.next();
				if (well.getColumn() == index) {
					handleCellSelection(cell, well, results);
				}
			}
		} else {
			while (i.hasNext()) {
				well = (WellImageSet) i.next();
				if (well.getRow() == index) {
					handleCellSelection(cell, well, results);
				}
			}
		}
		if (results.size() > 0) {
			DataBrowserLoader loader = new PlateSaver(component, ctx, results);
			loader.load();
		}
	}
	
	/**
	 * Returns the number of rows.
	 * 
	 * @return See above.
	 */
	int getRows() { return rows; }
	
	/**
	 * Returns the number of columns.
	 * 
	 * @return See above.
	 */
	int getColumns() { return columns; }
	
	/**
	 * Returns the well corresponding to the passed location.
	 * 
	 * @param row 	 The row identifying the well.
	 * @param column The column identifying the well.
	 * @return See above.
	 */
	WellImageSet getWell(int row, int column)
	{
		List<ImageDisplay> l = getNodes();
		Iterator<ImageDisplay> i = l.iterator();
		WellImageSet well;
		while (i.hasNext()) {
			well = (WellImageSet) i.next();
			if (well.getColumn() == column && well.getRow() == row) 
				return well;
		}
		return null;
	}
	
    /**
     * Creates a concrete loader.
     * 
     * @param fields
     *            The rows/columns identifying the well.
     * @return See above.
     */
    DataBrowserLoader createFieldsLoader(List<Point> fields) {
        List<ImageDisplay> l = getNodes();
        Iterator<ImageDisplay> i = l.iterator();
        ImageSet node;

        Multimap<Point, ImageData> images = HashMultimap.create();

        WellSampleData data;
        Thumbnail thumb;
        WellImageSet wis;
        List<WellSampleNode> nodes;
        Iterator<WellSampleNode> j;
        WellSampleNode n;
        while (i.hasNext()) {
            node = (ImageSet) i.next();
            if (node instanceof WellImageSet) {
                wis = (WellImageSet) node;
                Point targetField = null;
                for (Point p : fields) {
                    if (wis.getRow() == p.getX() && wis.getColumn() == p.getY()) {
                        targetField = p;
                        break;
                    }
                }
                if (targetField != null) {
                    nodes = wis.getWellSamples();
                    j = nodes.iterator();
                    while (j.hasNext()) {
                        n = j.next();
                        data = (WellSampleData) n.getHierarchyObject();

                        if (data.getId() < 0) {
                            thumb = n.getThumbnail();
                            thumb.setValid(false);
                            thumb.setFullScaleThumb(Factory
                                    .createDefaultImageThumbnail(
                                            wellDimension.width,
                                            wellDimension.height));
                        } else {
                            images.put(targetField, data.getImage());
                        }
                    }
                }
            }
        }

        if (images.size() == 0)
            return null;

        return new ThumbnailFieldsLoader(component, ctx, images);
    }
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected  List<DataBrowserLoader> createDataLoader(boolean refresh, 
			Collection ids)
	{
		if (!withThumbnails) 
			return null;
		
		List<ImageDisplay> l = getNodes();
		Iterator<ImageDisplay> i = l.iterator();
		ImageSet node;
		List<DataObject> images = new ArrayList<DataObject>();
		ImageNode selected;
		WellSampleData data;
		Thumbnail thumb;
		while (i.hasNext()) {
			node = (ImageSet) i.next();
			if (node instanceof WellImageSet) {
				selected = ((WellImageSet) node).getSelectedWellSample();
				data = (WellSampleData) selected.getHierarchyObject();
				if (data.getId() < 0) {
					thumb = selected.getThumbnail();
					thumb.setValid(false);
					thumb.setFullScaleThumb(
							Factory.createDefaultImageThumbnail(
									wellDimension.width, wellDimension.height));
				}
				else {
					ImageData img = data.getImage();
					if(CollectionUtils.isEmpty(ids) || ids.contains(img.getId()))
						images.add(data.getImage());
				}
			}
		}

		if (images.size() == 0) 
			return null;
		return createThumbnailsLoader(sorter.sort(images));
	}
	
	/**
	 * Returns the type of this model.
	 * @see DataBrowserModel#getType()
	 */
	protected int getType() { return DataBrowserModel.WELLS; }
	
	/**
	 * No-operation implementation in our case.
	 * @see DataBrowserModel#getNodes()
	 */
	protected List<ImageDisplay> getNodes() { return wellNodes; }

}
