/*
 * org.openmicroscopy.shoola.agents.roi.pane.ColoredCellTable
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

package org.openmicroscopy.shoola.agents.roi.pane;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.image.roi.ROI4D;
import org.openmicroscopy.shoola.util.ui.ColoredLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellRenderer;
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
public class ColoredCellTable
    extends TableComponent
{
    
    /** MAX portable view size. */
    private static final int    MAX_WIDTH = 300, MAX_HEIGHT = 200;

    private int                 numRows, numColumns;
    
    private Object[][]          data;
    
    public ColoredCellTable(int numRows, int numColumns, ROI4D logicalROI, 
                            Color alphaColor, Color color, int z, int t)
    {
        super(numRows, numColumns);
        this.numRows = numRows;
        this.numColumns = numColumns;
        data = new Object[numRows][numColumns];
        buildTableData(logicalROI, alphaColor, color, z, t);
        setModel(new ColoredLabelTableModel(data, numColumns));
        setTableLayout();
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF); //Big table
    }
    
    public void buildTableData(ROI4D logicalROI, Color alphaColor, Color color, 
                            int z, int curT)
    {
        ColoredLabel label;
        String text;
        int t;
        Color c;
        for (int i = 0; i < numRows; i++) { //z
            for (int j = 0; j < numColumns; j++) {  //t
                label = new ColoredLabel();
                t = numRows-1-i;
                text = "z = "+t+", t = "+j;
                label.setToolTipText(UIUtilities.formatToolTipText(text));
                //Check plane
                c = AnalysisControls.DEFAULT_COLOR;
                if (logicalROI.getPlaneArea(t, j) != null) {
                    if (t == z && curT == j) c = color;
                    else c = alphaColor;
                }
                label.setBackground(c);
                data[i][j] = label;
            }
        }
    }
    
    /** Set the layout. */
    private void setTableLayout()
    {
        int width = AnalysisControls.WIDTH_MIN;
        if (numColumns > AnalysisControls.MAX) 
            width = AnalysisControls.WIDTH_MAX; 
        setRowHeight(width);
        TableColumn col;
        for (int i = 0; i < numColumns; i++) {
          col = getColumnModel().getColumn(i);
          col.setMinWidth(width);
          col.setMaxWidth(width);
          col.setPreferredWidth(width);
        }
        int w = width*numColumns, h = width*numRows;
        if (w > MAX_WIDTH) w = MAX_WIDTH;
        if (h > MAX_HEIGHT) h = MAX_HEIGHT;
        setPreferredScrollableViewportSize(new Dimension(w, h)); 
        setDefaultRenderer(JComponent.class, new TableComponentCellRenderer());
    }
       
    /** Build a tableModel, needed for mouseListener. */
    private final class ColoredLabelTableModel 
        extends AbstractTableModel
    {
        
        private Object[][]      data;
    
        private int             numbColumns;
        
        private String[]        fieldNames;
        
        private ColoredLabelTableModel(Object[][] data, int numbColumns) 
        {
            this.data = data;
            this.numbColumns = numbColumns;
            fieldNames = new String[numbColumns];
            for (int i = 0; i < numbColumns; i++) 
                fieldNames[i] = ""+i;
        }
    
        public String getColumnName(int col) { return fieldNames[col]; }
        
        public int getColumnCount() { return numbColumns; }
    
        public int getRowCount() { return data.length; }
    
        public Class getColumnClass(int col)
        {
            return getValueAt(0, col).getClass();
        }
    
        public Object getValueAt(int row, int col)
        { 
            return data[row][col];
       }
    
        public boolean isCellEditable(int row, int col) { return false; }
    
    }
    
}
