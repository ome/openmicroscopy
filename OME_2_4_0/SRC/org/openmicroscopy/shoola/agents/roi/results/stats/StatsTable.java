/*
 * org.openmicroscopy.shoola.agents.roi.results.pane.StatsTable
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

package org.openmicroscopy.shoola.agents.roi.results.stats;

//Java imports
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.table.NumberFormatCellRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.table.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableSorter;

/** 
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
class StatsTable
    extends JTable
{
    
    int                     height;
    
    /**Icon displayed in the table header. */
    private Icon            up, down;
    
    private TableSorter     sorter;
    
    StatsTable(Icon up, Icon down)
    {
        this.up = up;
        this.down = down;
    }
    
    void setTableData(Number[][] data)
    {
        StatsTableModel model = new StatsTableModel(data);
        sorter.setModel(model);
        repaint();
    }
    
    /** Initializes the table. */
    void initTable(Number[][] data)
    {
        StatsTableModel model = new StatsTableModel(data);
        sorter = new TableSorter(model); 
        setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(this);
        sorter.sortByColumn(0);     // default
        setTableLayout(StatsResultsPane.zAndtFieldNames);
    }
    
    /** Set the table layout. */
    private void setTableLayout(String[] fieldNames)
    {
        TableIconRenderer iconHeaderRenderer = new TableIconRenderer();
        NumberFormatCellRenderer cr = new NumberFormatCellRenderer();
        TableColumnModel tcm = getTableHeader().getColumnModel();
        TableColumn tc;
        TableHeaderTextAndIcon txt;
        for (int i = 0; i < fieldNames.length; i++) {
            tc = tcm.getColumn(i);
            tc.setHeaderRenderer(iconHeaderRenderer);
            tc.setCellRenderer(cr);
            txt = new TableHeaderTextAndIcon(fieldNames[i], up, down, 
                                "Order by "+fieldNames[i]+".");
            tc.setHeaderValue(txt);
        }
    }
    
    private final class StatsTableModel
        extends AbstractTableModel
    {
        
        /** Columns' header. */
        private String[]    fieldNames = StatsResultsPane.zAndtFieldNames;
        
        /** Data to be displayed. */
        private Number[][]  data;
        
        StatsTableModel(Number[][] data)
        { 
            this.data = data;
            height = data.length*getRowHeight();
        }
        
        public String getColumnName(int col) { return fieldNames[col]; }
        
        public int getColumnCount() { return fieldNames.length; }
        
        public int getRowCount() { return data.length; }
        
        public Object getValueAt(int row, int col) {  return data[row][col]; }
        
        public boolean isCellEditable(int row, int col) { return false; }
        
    }
        
}
