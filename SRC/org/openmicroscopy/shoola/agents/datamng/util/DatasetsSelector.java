/*
 * org.openmicroscopy.shoola.agents.datamng.util.DatasetsSelector
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

package org.openmicroscopy.shoola.agents.datamng.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.table.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableSorter;
import pojos.DatasetData;

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
public class DatasetsSelector
    extends JDialog
{
    
    private static final int                NAME = 0, SELECT = 1;
    
    protected static final String[]         columnNames;
    
    static {
        columnNames  = new String[2];
        columnNames[NAME] = "Name";
        columnNames[SELECT] = "Select";
    }
    
    JButton                                 selectButton, resetButton, 
                                            loadButton, cancelButton;

    private DatasetsTableModel              datasetsTM;
    
    private TableSorter                     sorter;

    private DatasetsSelectorMng             manager;
    
    private DataManagerCtrl                 agentCtrl;
    
    public DatasetsSelector(DataManagerCtrl agentCtrl, 
                    ISelector iSelector, Set datasets)
    {
        super(agentCtrl.getReferenceFrame(), "List of used datasets", true);
        this.agentCtrl = agentCtrl;
        initComponents();
        manager = new DatasetsSelectorMng(this, iSelector);
        buildGUI(datasets);
    }
    
    /** Reset the selection. */
    void setSelection(Boolean b)
    {
        int countCol = datasetsTM.getColumnCount()-1;
        for (int i = 0; i < datasetsTM.getRowCount(); i++)
            datasetsTM.setValueAt(b, i, countCol);
    }
    
    /** Initializes the GUI components. */
    private void initComponents()
    {
        //buttons
        selectButton = new JButton("Select All");
        selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        selectButton.setToolTipText(
            UIUtilities.formatToolTipText("Select all datasets."));
        resetButton = new JButton("Reset");
        resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetButton.setToolTipText(
            UIUtilities.formatToolTipText("Cancel selection."));
        //cancel button
        loadButton = new JButton("OK");
        loadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loadButton.setToolTipText(
            UIUtilities.formatToolTipText("Apply selection."));
        cancelButton = new JButton("Cancel");
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.setToolTipText(
            UIUtilities.formatToolTipText("Close dialog."));
    }
    
    /** Build and Lay out the GUI. */
    private void buildGUI(Set datasets)
    {
        IconManager im = IconManager.getInstance(agentCtrl.getRegistry());
        TitlePanel tp = new TitlePanel(" Dataset selection", 
                                " Retrieve images contained in the " +
                                "following datasets.", 
                            im.getIcon(IconManager.DATASET_BIG));
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(tp, BorderLayout.NORTH);
        getContentPane().add(buildDatasetsPanel(datasets), BorderLayout.CENTER);
        setSize(DataManagerUIF.ADD_WIN_WIDTH, DataManagerUIF.ADD_WIN_HEIGHT);
    }
    
    /** Initializes the table and display it in a panel. */
    private JPanel buildDatasetsPanel(Set datasets)
    {
        //Initializes the table
        datasetsTM = new DatasetsTableModel(datasets);
        sorter = new TableSorter(datasetsTM);
        JTable t = new JTable();
        t.setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(t);
        setTableLayout(t);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setPreferredScrollableViewportSize(DataManagerUIF.VP_DIM);
        
        //Build the panel
        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
        contents.add(new JScrollPane(t));
        contents.add(Box.createRigidArea(DataManagerUIF.VBOX));
        contents.add(buildControlPanel());
        contents.add(Box.createRigidArea(DataManagerUIF.VBOX));
        contents.setSize(DataManagerUIF.ADD_WIN_WIDTH, 
                        DataManagerUIF.ADD_WIN_HEIGHT);
        return contents;
    }
    
    /** Build the panel containing the controls. */
    private JPanel buildControlPanel()
    {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        controls.add(resetButton);
        controls.add(Box.createRigidArea(DataManagerUIF.HBOX));
        controls.add(selectButton);
        controls.add(Box.createRigidArea(DataManagerUIF.HBOX));
        controls.add(loadButton);
        controls.add(Box.createRigidArea(DataManagerUIF.HBOX));
        controls.add(cancelButton);
        controls.setOpaque(false); //make panel transparent
        return controls;
    }
    
    /** Set icons in the tableHeader. */
    private void setTableLayout(JTable table)
    {
        IconManager im = IconManager.getInstance(agentCtrl.getRegistry());
        TableIconRenderer iconHeaderRenderer = new TableIconRenderer();
        TableColumnModel tcm = table.getTableHeader().getColumnModel();
        TableColumn tc = tcm.getColumn(NAME);
        tc.setHeaderRenderer(iconHeaderRenderer);
        TableHeaderTextAndIcon 
        txt = new TableHeaderTextAndIcon(columnNames[NAME], 
                im.getIcon(IconManager.ORDER_BY_NAME_UP),
                im.getIcon(IconManager.ORDER_BY_NAME_DOWN),
                "Order datasets by name.");
        tc.setHeaderValue(txt);
        tc = tcm.getColumn(SELECT);
        tc.setHeaderRenderer(iconHeaderRenderer); 
        txt = new TableHeaderTextAndIcon(columnNames[SELECT], 
                im.getIcon(IconManager.ORDER_BY_SELECTED_UP), 
                im.getIcon(IconManager.ORDER_BY_SELECTED_DOWN),
                "Order by selected datasets.");
        tc.setHeaderValue(txt);
        table.setDefaultRenderer(DatasetData.class, new DatasetsTableRenderer());
    }
     
    /** 
     * A <code>3</code>-column table model to view the summary of 
     * datasets contained in the project.
     * The first column contains the datasets ID and the 
     * second column the names. Cells are not editable. 
     */
    private class DatasetsTableModel
        extends AbstractTableModel
    {
        
        private static final int    LENGTH = 2;
        
        private Object[]            datasets;
        
        private Object[][]          data;

        private DatasetsTableModel(Set d)
        {
            this.datasets = d.toArray();
            data = new Object[datasets.length][LENGTH];
            for (int i = 0; i < datasets.length; i++) {
                data[i][0] = (DatasetData) datasets[i];
                data[i][1] = Boolean.FALSE;
            }
        }
    
        public int getColumnCount() { return LENGTH; }
    
        public int getRowCount() { return datasets.length; }
    
        public String getColumnName(int col){ return columnNames[col]; }
        
        public Class getColumnClass(int c)
        {
            return getValueAt(0, c).getClass();
        }

        public Object getValueAt(int row, int col) { return data[row][col]; }

        public boolean isCellEditable(int row, int col) { return (col == 1); }
        
        public void setValueAt(Object value, int row, int col)
        {   
            data[row][col] = value;
            DatasetData ds = (DatasetData) sorter.getValueAt(row, NAME);
            fireTableCellUpdated(row, col);
            manager.addDataset(((Boolean) value).booleanValue(), ds);
        }
    }
    
    final class DatasetsTableRenderer
        extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);
            if (value instanceof DatasetData)
                setText(((DatasetData) value).getName());
            return this;
        }
    }
}
