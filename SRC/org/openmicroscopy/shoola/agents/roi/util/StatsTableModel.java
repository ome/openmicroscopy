/*
 * org.openmicroscopy.shoola.agents.roi.util.StatsTableModel
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.roi.util;

import javax.swing.table.AbstractTableModel;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 *
 * A <code>7</code>-column table model to display the result of the 
 * ROI analysis. Columns are as follow: 
 * ROILable, Channel, section, timepoint, Minimum pixel value, 
 * Maximum pixel value, mean, standard deviation.
 * Cells aren't editable. 
 * 
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class StatsTableModel
    extends AbstractTableModel 
{
    
    private final String[]    fieldNames = {"Label", "Channel", "Z", "T", "Min",
                                            "Max", "Mean", "Sigma"};
    
    private Object[][] data;
    
    StatsTableModel(Object[][] data)
    {
        this.data = data;
    }
    
    public String getColumnName(int col) { return fieldNames[col]; }

    public int getColumnCount() { return fieldNames.length; }

    public int getRowCount() { return data.length; }

    public Object getValueAt(int row, int col) 
    {
        return data[row][col];
    }

    public boolean isCellEditable(int row, int col) { return false; }
}
