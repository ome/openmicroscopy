/*
 * org.openmicroscopy.shoola.agents.datamng.ImagesSplitPane
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

package org.openmicroscopy.shoola.agents.datamng;




//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.table.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.table.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableSorter;

import pojos.ImageData;

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
class ImagesSplitPane
    extends JSplitPane
    implements ISplitPane
{

    private static final String     MSG = "Select the images you want " +
                                    "to retrieve in the list above, and " +
                                    "press the button Retrieving the data " +
                                    "can take time.";
    
    ImagesPane                      view;
    
    TableSorter                     sorter;
    
    ImagesTableModel                tableModel;

    JTable                          table;
    
    JScrollPane                     leftPane, rightPane;
    
    private Registry                registry;
    
    private static final String[]   columnNames;
    
    static {
        columnNames  = new String[ImagesPane.MAX_ID+1];
        columnNames[ImagesPane.NAME] = "Name";
        columnNames[ImagesPane.DATE] = "Date";
    }
    
    ImagesSplitPane(ImagesPane  view, Registry registry)
    {
        this.view = view;
        this.registry = registry;
        initComponents();
        buildGUI();
    }
    
    /** Display the JComponent in the right JScrollPane. */
    public void addToRightComponent(JComponent c)
    {
        JViewport port = rightPane.getViewport();
        port.removeAll();
        port.add(c);
    }
    
    /** Remove all components from the main JScrollPane. */
    public void removeFromRightComponent()
    {
        rightPane.getViewport().removeAll();
    }
    
    /** Diplays the list of images in the left scrollpane. */
    void displayImages(Object[] images) 
    {
        table = new JTable();
        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        view.manager.attachTableListener(table);
        tableModel = new ImagesTableModel(images);
        sorter = new TableSorter(tableModel);  
        table.setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(table);
        sorter.sortByColumn(ImagesPane.NAME);
        setTableLayout(table);
        JViewport viewPort = leftPane.getViewport();
        viewPort.removeAll();
        viewPort.add(table);
        removeFromRightComponent();
    }   
    
    /** initialize the ScrollPane. */
    private void initComponents()
    {
        JPanel pRight = new JPanel(), pLeft = new JPanel();
        pRight.add(new MultilineLabel(MSG), BorderLayout.CENTER);
        pRight.setMinimumSize(DataManagerUIF.COMPONENT_MIN_DIM);
        pLeft.setMinimumSize(DataManagerUIF.COMPONENT_MIN_DIM);
        rightPane = new JScrollPane(pRight);
        leftPane = new JScrollPane(pLeft);
    }
    
    /** Builds and lay out the GUI. */
    private void buildGUI()
    {
        setLeftComponent(leftPane);
        setRightComponent(rightPane);
        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setContinuousLayout(true); 
        setOneTouchExpandable(true); 
        setDividerLocation(DataManagerUIF.DIVIDER_LOC); 
    }
    
    /** Set icons in the tableHeader. */
    private void setTableLayout(JTable table)
    {
        IconManager im = IconManager.getInstance(registry);
        TableIconRenderer iconHeaderRenderer = new TableIconRenderer();
        TableColumnModel tcm = table.getTableHeader().getColumnModel();
        TableColumn tc = tcm.getColumn(ImagesPane.NAME);
        tc.setHeaderRenderer(iconHeaderRenderer);
        TableHeaderTextAndIcon header =  new TableHeaderTextAndIcon(
                columnNames[ImagesPane.NAME], 
                im.getIcon(IconManager.ORDER_BY_NAME_UP), 
                im.getIcon(IconManager.ORDER_BY_NAME_DOWN), "Order by name");
        tc.setHeaderValue(header);
        tc = tcm.getColumn(ImagesPane.DATE);
        tc.setHeaderRenderer(iconHeaderRenderer); 
        header =  new TableHeaderTextAndIcon(
                columnNames[ImagesPane.DATE], 
                im.getIcon(IconManager.ORDER_BY_DATE_UP), 
                im.getIcon(IconManager.ORDER_BY_DATE_DOWN), "Order by date");
        tc.setHeaderValue(header);
        table.setDefaultRenderer(ImageData.class, new ImagesTableRenderer());
    }
    
    /** 
     * A <code>2</code>-column table model to view the summary of 
     * the user's image.
     * The first column contains the image names, the second column 
     * the <code>created date</code>.
     */
    final class ImagesTableModel 
        extends AbstractTableModel
    {
        private Object[]      images;
        private Object[][]    data;
        
        private ImagesTableModel(Object[] images) 
        {
            this.images = images;
            data = new Object[images.length][2]; 
            ImageData is;
            for (int i = 0; i < images.length; i++) {
                is = (ImageData) images[i];
                data[i][ImagesPane.NAME] = is;
                data[i][ImagesPane.DATE] = new Date(is.getInserted().getTime()); 
            }
        }

        public int getColumnCount() { return 2; }
    
        public int getRowCount() { return images.length; }
    
        public String getColumnName(int col) { return columnNames[col]; }

        public Class getColumnClass(int col)
        {
            return getValueAt(0, col).getClass();
        }
        
        public Object getValueAt(int row, int col) { return data[row][col]; }
        
        public boolean isCellEditable(int row, int col) { return (col == 0); }

        //only name column is editable
        public void setValueAt(Object value, int row, int col)
        {
            if (col == ImagesPane.NAME) {
                ImageData is = (ImageData) sorter.getValueAt(row, col);
                is.setName(((ImageData) value).getName());
                view.updateImage(is);
            }
        } 
        
        /** invoke when the view is updated. */
        public void setValueAt(ImageData is, int row)
        {
            ImageData summary = (ImageData) sorter.getValueAt(row, 
                    ImagesPane.NAME);
            summary.setName(is.getName());
        } 
    }
    
    final class ImagesTableRenderer
        extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);
            if (value instanceof ImageData)
                setText(((ImageData) value).getName());
            return this;
        }
    }
    
}
