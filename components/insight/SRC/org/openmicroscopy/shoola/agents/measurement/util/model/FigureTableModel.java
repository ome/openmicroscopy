/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.collections.CollectionUtils;

import org.jhotdraw.draw.AttributeKey;

import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;

import omero.gateway.model.TagAnnotationData;

/** 
 * The model associated to the table displaying the figures.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class FigureTableModel 
	extends AbstractTableModel
{
	
	/** Identifies the <code>N/A</code> string. */
	private static final String		NA	="N/A";
	
	/** The figure this model is for. */
	private ROIFigure				figure;
	
	/** The collection of column's names. */
	private List<String>			columnNames;
	
	/** Collection of supported keys. */
	private List<AttributeKey>		keys;
	
	/** Collection of values handled by this model. */
	private List					values;
	
	/** Collection of fields. */
	private List<AttributeField>	fieldList;

	/**
	 * Creates a new instance.
	 * 
	 * @param fieldList The collection of fields. Mustn't be <code>null</code>.
	 * @param columnNames The collection of column's names.
	 *                    Mustn't be <code>null</code>.
	 */
	public FigureTableModel(List<AttributeField> fieldList,
			List<String> columnNames)
	{
		if (fieldList == null) 
			throw new IllegalArgumentException("No fields specified.");
		if (columnNames == null) 
			throw new IllegalArgumentException("No column's names specified.");
		this.fieldList = fieldList;
		this.columnNames = columnNames;
		keys = new ArrayList<AttributeKey>();
		values = new ArrayList<Object>();
	}
	
	/** Clears data. */
	public void clearData()
	{
		keys.clear();
		values.clear();
		fireTableDataChanged();
	}
	
	/**
	 * Sets the figure handled by this model.
	 * 
	 * @param figure The figure data.
	 */
	public void setData(ROIFigure figure)
	{
		if (figure == null) throw new IllegalArgumentException("No figure.");
		this.figure = figure;
		keys.clear();
		values.clear();
		boolean found;
		Iterator i;
		AttributeKey key;
		for (AttributeField fieldName : fieldList)
		{
			found = false;
			i = figure.getAttributes().keySet().iterator();
			while (i.hasNext())
			{
				key = (AttributeKey) i.next();
				if (key.equals(fieldName.getKey()))
				{
				    Object value = figure.getAttribute(key);
					if (MeasurementAttributes.TEXT.equals(key) ||
							MeasurementAttributes.WIDTH.equals(key) ||
							MeasurementAttributes.HEIGHT.equals(key)) {
						if (figure.isReadOnly())
							fieldName.setEditable(false);
						else fieldName.setEditable(figure.canEdit());
					} else if (AnnotationKeys.TAG.equals(key)) {
	                    
	                    StructuredDataResults sd = (StructuredDataResults) figure.getAttribute(key);
	                    if (sd != null) {
	                        Collection<TagAnnotationData> tags = sd.getTags();
	                        if (CollectionUtils.isNotEmpty(tags)) {
	                            StringBuffer buffer = new StringBuffer();
	                            Iterator<TagAnnotationData> k = tags.iterator();
	                            TagAnnotationData tag;
	                            int index = 0;
	                            int size = tags.size()-1;
	                            while (k.hasNext()) {
	                                tag = k.next();
	                                buffer.append(tag.getTagValue());
	                                if (index < size) {
	                                    buffer.append(", ");
	                                }
	                                index++;
	                            }
	                            value = buffer.toString();
	                        }
	                    }
					}
					keys.add(key);
					values.add(value);
					found = true;
					break;
				}
			}
			if (!found)
			{
				key = fieldName.getKey();
				keys.add(key);
				if (key instanceof AnnotationKey)
                    values.add(figure.getROI().getAnnotation(
                        (AnnotationKey) key));
                else values.add(NA);
			}
		}
		fireTableDataChanged();
	}
	
	/**
	 * Returns the figure handle by the model.
	 * 
	 * @return See above.
	 */
	public ROIFigure getFigure() { return figure; }
	
	/**
	 * Overridden to return the name of the specified column.
	 * 
	 * @see AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int col)
	{
		return columnNames.get(col);
	}
	
	/**
	 * Returns the number of columns.
	 * 
	 * @see AbstractTableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return columnNames.size();
	}
	
	/**
	 * Returns the number of rows.
	 * 
	 * @see AbstractTableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return keys.size();
	}
	
	/**
	 * Returns the value of the specified cell.
	 * 
	 * @see AbstractTableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		int n;
		if (rowIndex < 0) return null;
		if (columnIndex == 0) {
			n = fieldList.size();
			if (rowIndex < n)
				return fieldList.get(rowIndex).getName();
			return null;
		}
		n = values.size();
		if (rowIndex < n) return values.get(rowIndex);
		return null;
	}
	
	/**
	 * Returns the attributeGField of the specified cell.
	 * 
	 * @param rowIndex The index of the attributeField.
	 * @return see above.
	 */
	public AttributeField getFieldAt(int rowIndex)
	{
		return fieldList.get(rowIndex);
	}
	
	/**
	 * Sets the value depending on the <code>Attribute Key</code>.
	 * 
	 * @see AbstractTableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt(Object value, int row, int col)
	{
		if (col == 0 || keys.size() <= row) return;
		AttributeKey key = keys.get(row);
		if (figure.getAttribute(key) instanceof Double) 
		{
			if (value instanceof Double)
				figure.setAttribute(key, (Double) value);
			if (value instanceof String)
			{
				try
				{
					figure.setAttribute(keys.get(row), 
							new Double((String) value));
				}
				catch(Exception e)
				{
					MeasurementAgent.getRegistry().getUserNotifier().
					notifyInfo("Value for field invalid", "The value of "+
							value + " is invalid for " + key.toString());
					return;
				}
			}
		}
		else  
			figure.setAttribute(key, value);
		values.set(row, value);
		fireTableCellUpdated(row, col);
	}
	
	/**
	 * Depending on the selected cell, allows the user to edit.
	 * 
	 * @see AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col)
	{
		if (col == 0) return false;
		if (values.get(row) instanceof String) 
			if (NA.equals(values.get(row))) return false;
		return fieldList.get(row).isEditable();
	}
	
}
