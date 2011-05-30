/*
 * org.openmicroscopy.shoola.agents.util.flim.resultstable.ResultsTable 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.flim.resultstable;


//Java imports
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.flim.resultstable.io.CSVReader;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;

/** 
 * Displays the results stored in the passed file.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ResultsTable 
	extends JPanel
{
	
	/** The default size of the column in a table. */
	protected static final int	COLUMNWIDTH = 64;
	
	/** The default column groupings. */
	protected static final int DEFAULT_MOD = 2;
	
	/** View of the table. */
	protected ResultsTableView tableView;

	/** 
	 * The table selection listener attached to the table displaying the 
	 * objects.
	 */
	protected ListSelectionListener			listener;
	
	/** Then names of the columns. */
	protected List<String> columnNames;
	

	/** The model for the table. */
	protected ResultsTableModel tableModel;
	
	/**
	 * Create the resultsTable to display the requested results from the user.
	 */
	public ResultsTable(List<String> columnNames)
	{
		this.columnNames = columnNames;
		initComponents();
		buildUI();
	}
	
	/**
	 * Initialise the components in the table.
	 */
	protected void initComponents()
	{
		tableView = new ResultsTableView();
		tableView.getTableHeader().setReorderingAllowed(false);
		tableModel = new ResultsTableModel(columnNames);
		
		tableView.setModel(tableModel);
		tableView.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		tableView.setRowSelectionAllowed(true);
		tableView.setColumnSelectionAllowed(false);
		tableView.setRowHighlightMod(DEFAULT_MOD);
		listener = new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) return;

		        ListSelectionModel lsm =
		            (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) {
		        } else {
		        	int index = lsm.getMinSelectionIndex();
		        	if (index < 0) return;
		        	ResultsTableModel m = 
	        			(ResultsTableModel) tableView.getModel();
		        	
		        }
			}
		
		};
		tableView.getSelectionModel().addListSelectionListener(listener);
	}
	
	
	
	/**
	 * Build the UI components into the table.
	 */
	protected void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JScrollPane(tableView));
	}
	
	/** 
	 * Resize the columns so that they fit the column names better, the 
	 * column will be a minimum size of COLUMNWIDTH or the length of the 
	 * text whichever is greater.
	 *
	 */
	protected void resizeTableColumns()
	{	
		int columnWidth = 0;
		Font font = getFont();
		FontMetrics metrics = getFontMetrics( font );
		ResultsTableModel tableModel = (ResultsTableModel)tableView.getModel();
		for(int i = 0 ; i < tableView.getColumnCount(); i++)
		{
			TableColumn col = tableView.getColumnModel().getColumn(i);
			int w  =  metrics.stringWidth(tableModel.getColumnName(i));
			columnWidth = Math.max(w, COLUMNWIDTH);
			col.setMinWidth(columnWidth);
			col.setPreferredWidth(columnWidth);
		}
	}
	
	/**
	 * Load the results from a file.
	 * @param fileName
	 * @return
	 */
	public boolean loadResults(String fileName)
	{
		File file = new File(fileName);
		CSVReader reader = new CSVReader();
		return false;
	}
	
	/** 
	 * Save the results.
	 * 
	 * @throws IOException Thrown if the data cannot be written.
	 * @return true if results saved, false if users cancels save.
	 */
	public boolean saveResults(String fileNameChoice)
		throws IOException
	{
		File file = new File(fileNameChoice);
		if (!file.getAbsolutePath().endsWith(ExcelFilter.EXCEL))
		{
			String newFileName = file.getAbsolutePath()+"."+ExcelFilter.EXCEL;
			file = new File(newFileName);
		}
		String filename = file.getAbsolutePath();
		ExcelWriter writer = new ExcelWriter(filename);
		writer.openFile();
		writer.createSheet("Measurement Results");
		writer.writeTableToSheet(0, 0, tableView.getModel());
		writer.close();
		return true;
	}
	
	public void insertData(Map<String, Object> data)
	{
		ResultsObject row = new ResultsObject();
		Iterator<String> keyIterator = data.keySet().iterator();
		while(keyIterator.hasNext())
		{
			String key = keyIterator.next();
			row.addElement(key, data.get(key));
		}
		tableModel.addRow(row);
	}
	
	/**
	 * Clear the table of all values.
	 */
	public void clearTable()
	{
		ResultsTableModel tableModel = (ResultsTableModel)tableView.getModel();
		tableModel.clear();
	}
	
	/**
	 * Set the hightlighting mod of the table.
	 * @param mod The mod.
	 */
	public void setRowHighlightMod(int mod)
	{
		tableView.setRowHighlightMod(mod);
	}

}
