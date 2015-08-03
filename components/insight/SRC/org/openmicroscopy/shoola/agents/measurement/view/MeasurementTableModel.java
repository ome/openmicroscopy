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

    /** Flag indicating to display the units.*/
    private boolean showUnits;

    /** Keep track of the columns to show the units for.*/
    private List<Boolean> unitsDisplay;

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
        unitsDisplay = new ArrayList<Boolean>();
    }

    /**
     * Sets the flag indicating to show the units.
     *
     * @param showUnits See above.
     */
    void setShowUnits(boolean showUnits) { this.showUnits = showUnits; }

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
        if (value instanceof List && !showUnits) {
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
        }
        if (showUnits) {
            if (value instanceof Length) {
                Length n = (Length) value;
                return convertLength(n, col);
            } else if (value instanceof List) {
                List l = (List) value;
                Iterator<Object> i = l.iterator();
                Object v;
                StringBuilder buffer = new StringBuilder();
                int size = l.size();
                Object s;
                while (i.hasNext()) {
                    v = i.next();
                    if (v instanceof Length) {
                        Length n = (Length) v;
                        s = convertLength(n, col);
                        if (size == 1) return s;
                        if (s != null) {
                            buffer.append(s);
                            buffer.append(" ");
                        }
                    } else if (v instanceof Number) {
                        double d = ((Number) v).doubleValue();
                        if (size == 1) {
                            return UIUtilities.twoDecimalPlacesAsNumber(d);
                        }
                        s = UIUtilities.twoDecimalPlaces(d);
                        if (s != null) {
                            buffer.append(s);
                            buffer.append(" ");
                        }
                    }
                }
                return buffer.toString();
            } else if (value instanceof Number) {
                double d = ((Number) value).doubleValue();
                return UIUtilities.twoDecimalPlacesAsNumber(d);
            }
        }
        return value;
    }

    /**
     * Converts the length object either as a string or a numerical value.
     *
     * @param n The value to convert.
     * @param col The column hosting the value.
     * @return See above
     */
    private Object convertLength(Length n, int col) {
        KeyDescription key = getColumnNames().get(col);
        MeasurementUnits units = getUnitsType();
        String k = key.getKey();
        String s = null;
        if (!units.getUnit().equals(UnitsLength.PIXEL)) {
            if (unitsDisplay.size() > col && unitsDisplay.get(col)) {
                s = UIUtilities.formatValue(n,
                        AnnotationKeys.AREA.getKey().equals(k));
            } else {
                return UIUtilities.formatValueNoUnitAsNumber(n,
                        AnnotationKeys.AREA.getKey().equals(k));
            }
            if (CommonsLangUtils.isNotBlank(s))
               return s;
        }
        Number value = UIUtilities.twoDecimalPlacesAsNumber(n.getValue());
        if (value.doubleValue() == 0) return null;
        return value;
    }

    /**
     * Sets the specified value.
     * @see AbstractTableModel#setValueAt(Object, int, int)
     */
    public void setValueAt(Object value, int row, int col) {}

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

    /**
     * Creates a copy of the model.
     *
     * @return See above.
     */
    MeasurementTableModel copy()
    {
        //check column name
        KeyDescription key;
        List<KeyDescription> list = new ArrayList<KeyDescription>(
                this.columnNames.size());
        Iterator<KeyDescription> kk = this.columnNames.iterator();
        while (kk.hasNext()) {
            key = kk.next();
            list.add(new KeyDescription(key.getKey(), key.getDescription()));
        }
        MeasurementTableModel model = new MeasurementTableModel(
                list, this.unitsType);
        model.values.addAll(this.values);
        //Add the units to the column names.
        for (int i = 0; i < model.getColumnCount(); i++) {
            List<String> symbols = new ArrayList<String>();
            key = model.getColumnNames().get(i);
            String k = key.getKey();
            for (int j = 0; j < getRowCount(); j++) {
                Object v = getValueAt(j, i);
                if (v instanceof Length) {
                    Length l = (Length) v;
                    if (AnnotationKeys.AREA.getKey().equals(k)) {
                        l = UIUtilities.transformSquareSize(l);
                    } else {
                        l = UIUtilities.transformSize(l);
                    }
                    
                    String s = l.getSymbol();
                    if (!symbols.contains(s) &&
                            !l.getUnit().equals(UnitsLength.PIXEL)) {
                        symbols.add(s);
                    }
                } else if (v instanceof Number) {
                    if (k.equals(AnnotationKeys.ANGLE.getKey())) {
                        String s = UIUtilities.DEGREE_SYMBOL;
                        if (!symbols.contains(s)) {
                            symbols.add(s);
                        }
                    }
                }
            }
            if (symbols.size() == 1) {
                String value = key.getDescription()+" ("+symbols.get(0);
                if (AnnotationKeys.AREA.getKey().equals(k)) {
                    value += UIUtilities.SQUARED_SYMBOL;
                }
                value += ")";
                key.setDescription(value);
                model.unitsDisplay.add(false);
            } else {
                model.unitsDisplay.add(true);
            }
        }
        return model;
    }
}
