/*
 * org.openmicroscopy.shoola.agents.datamng.ExplorerImagePane
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
import java.sql.Date;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
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
class ImagesPane
	extends JPanel
{

    /** Action id. */
    static final int                NAME = 0, DATE = 1;
    
    private static final int        MAX_ID = 1;
            
    protected static final String[] columnNames;
    
    static {
        columnNames  = new String[MAX_ID+1];
        columnNames[NAME] = "Name";
        columnNames[DATE] = "Date";
    }

    private static final String     MSG = "Select the images you want " +
                    "to retrieve in the list above, and press the button." +
        "Retrieving the data can take time.";
    
	/** This UI component's controller and model. */
	private ImagesPaneManager		manager;

    private DataManagerCtrl         agentCtrl;
    
	ImagesPaneBar					bar;
	
	JScrollPane						scrollPane;
	
    TableSorter                     sorter;
    
    ImagesTableModel                tableModel;

    JTable                          table;
    
	/** 
	 * Creates a new instance.
	 *
	 *@param    agentCtrl   The agent's control component.
	 */
	ImagesPane(DataManagerCtrl agentCtrl, Registry registry)
	{
        this.agentCtrl = agentCtrl;
		initComponents();
		bar = new ImagesPaneBar(registry);
		manager = new ImagesPaneManager(this, agentCtrl);
		buildGUI();
	}
	
    void displayImages(Object[] images) 
    {
        table = new JTable();
        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        manager.attachTableListener(table);
        tableModel = new ImagesTableModel(images);
        sorter = new TableSorter(tableModel);  
        table.setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(table);
        sorter.sortByColumn(NAME);
        setTableLayout(table);
        JViewport viewPort = scrollPane.getViewport();
        viewPort.removeAll();
        viewPort.add(table);
    }   
    
	/** Initializes the table and the scrollPane. */
	void initComponents()
	{
		JPanel p = new JPanel();
        p.add(new MultilineLabel(MSG), BorderLayout.CENTER);
		scrollPane = new JScrollPane(p);
	}
	
	/** Return the manager of the component. */
	ImagesPaneManager getManager() { return manager; }
	
	/** Builds and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(bar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}
	/** Set icons in the tableHeader. */
    private void setTableLayout(JTable table)
    {
        IconManager im = IconManager.getInstance(agentCtrl.getRegistry());
        TableIconRenderer iconHeaderRenderer = new TableIconRenderer();
        TableColumnModel tcm = table.getTableHeader().getColumnModel();
        TableColumn tc = tcm.getColumn(NAME);
        tc.setHeaderRenderer(iconHeaderRenderer);
        tc.setHeaderValue(buildTableHeader(columnNames[NAME], im));
        tc = tcm.getColumn(DATE);
        tc.setHeaderRenderer(iconHeaderRenderer); 
        tc.setHeaderValue(buildTableHeader(columnNames[DATE], im));
    }
    
    /** Build the corresponding tableHeader. */
    private TableHeaderTextAndIcon buildTableHeader(String columnName, 
                                                    IconManager im)
    {
        return  new TableHeaderTextAndIcon(columnName, 
                        im.getIcon(IconManager.ORDER_BY_NAME_UP), 
                        im.getIcon(IconManager.ORDER_BY_NAME_DOWN), 
                        "Order by "+columnName);
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
            ImageSummary is;
            for (int i = 0; i < images.length; i++) {
                is = (ImageSummary) images[i];
                data[i][NAME] = is;
                data[i][DATE] = new Date(is.getDate().getTime()); 
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
            if (col == NAME) {
                ImageSummary is = (ImageSummary) sorter.getValueAt(row, col);
                is.setName(((ImageSummary) value).getName());
                agentCtrl.updateImage(is);
            }
        } 
        
        /** invoke when the view is updated. */
        public void setValueAt(ImageSummary is, int row)
        {
            ImageSummary summary = (ImageSummary) sorter.getValueAt(row, NAME);
            summary.setName(is.getName());
        } 
    }
    
}
