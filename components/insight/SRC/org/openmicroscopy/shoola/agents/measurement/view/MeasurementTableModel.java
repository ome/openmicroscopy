/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;



//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import omero.model.Length;
import omero.model.enums.UnitsLength;

import org.openmicroscopy.shoola.agents.measurement.util.model.MeasurementObject;
import org.openmicroscopy.shoola.agents.measurement.util.ui.KeyDescription;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
/**
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class MeasurementTableModel extends AbstractTableModel
{
    
    private MeasurementUnits                unitsType;
    
    /** The collection of column's names. */
    private List<KeyDescription>            columnNames;
    
    /** Collection of <code>Object</code>s hosted by this model. */
    private List<MeasurementObject> values;
    
    /**
     * Creates a new instance.
     * 
     * @param colNames  The collection of column's names.
     *                      Mustn't be <code>null</code>.
     * @param units The units of measurement.
     */
    MeasurementTableModel(List<KeyDescription> colNames,
            MeasurementUnits units)
    {
        if (colNames == null)
            throw new IllegalArgumentException("No column's names " +
                                                "specified.");
        this.columnNames = colNames;
        this.values = new ArrayList<MeasurementObject>();
        this.unitsType = units;
    }

    public List<KeyDescription> getColumnNames() { return  columnNames; }

    /**
     * Returns the units type.
     *
     * @return See above.
     */
    public MeasurementUnits getUnitsType() { return unitsType; }

    /** 
     * Adds a new row to the model.
     * 
     * @param row The value to add.
     */
    void addRow(MeasurementObject row)
    {
        values.add(row);
        fireTableStructureChanged();
    }
    
    /** 
     * Get a row from the model.
     * 
     * @param index The row to return
     * 
     * @return MeasurementObject the row.
     */
    MeasurementObject getRow(int index)
    {
        if (index < values.size())
            return values.get(index);
        return null;
    }
    
    /**
     * Returns the value of the specified cell.
     * @see AbstractTableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) 
    {
        if (row < 0 || row > values.size()) return null;
        MeasurementObject rowData = values.get(row);
        Object value = rowData.getElement(col);
        if (value instanceof List) {
            List<Object> l = (List<Object>) value;
            
            if (l.size() == 1) 
            	return l.get(0);
            StringBuilder buffer = new StringBuilder();
            Iterator<Object> i = l.iterator();
            Object v;
            double total = 0;
            while (i.hasNext()) {
                v = i.next();
                if (v instanceof Number) {
                    double d = ((Number) v).doubleValue();
                    total += d;
                    buffer.append(UIUtilities.formatToDecimal(d));
                    buffer.append(" ");
                }
            }
            if (total > 0) {
                buffer.append("= "+UIUtilities.formatToDecimal(total));
            }
            return buffer.toString();
        } else if (value instanceof Length) {
            MeasurementUnits units = getUnitsType();
            Length n = (Length) value;
            String s;
            if (!units.getUnit().equals(UnitsLength.PIXEL)) {
                KeyDescription key = getColumnNames().get(col);
                String k = key.getKey();
                s = UIUtilities.formatValue(n, AnnotationKeys.AREA.getKey().equals(k));
                if (CommonsLangUtils.isNotBlank(s))
                   return s;
            } else {
                s = UIUtilities.twoDecimalPlaces(n.getValue());
                if (CommonsLangUtils.isNotBlank(s)) {
                    return s;
                }
            }
        }
        return rowData.getElement(col);
    }
    
    /**
     * Sets the specified value.
     * @see AbstractTableModel#setValueAt(Object, int, int)
     */
    public void setValueAt(Object value, int row, int col) 
    {

    }
    
    /**
     * Overridden to return the name of the specified column.
     * @see AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int col) 
    {
    	return columnNames.get(col).getDescription();
     }
    
    /**
     * Overridden to return the number of columns.
     * @see AbstractTableModel#getColumnCount()
     */
    public int getColumnCount() { return columnNames.size();  }

    /**
     * Overridden to return the number of rows.
     * @see AbstractTableModel#getRowCount()
     */
    public int getRowCount() { 
    	return values.size();
    	}
    
    /**
     * Overridden so that the cell is not editable.
     * @see AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int row, int col) 
    { 
        return false;
    }
}
