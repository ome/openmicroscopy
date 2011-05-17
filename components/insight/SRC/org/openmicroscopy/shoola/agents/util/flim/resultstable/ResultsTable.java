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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.openmicroscopy.shoola.agents.util.flim.resultstable.io.CSVReader;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;

//Third-party libraries

//Application-internal dependencies

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
	implements ActionListener
{
	/** The action to save the results. */
	protected final static String SAVEACTION = "SAVE";
	
	/** The action to load results from csv file. */
	protected final static String LOADACTION = "LOAD";
	
	/** The results wizard action. */
	protected final static String WIZARDACTION = "WIZARD";
	
	/** The clear table action. */
	protected final static String CLEARACTION = "CLEAR";

	/** The default size of the column in a table. */
	protected static final int	COLUMNWIDTH = 64;
	
	/** View of the table. */
	protected TableView tableView;

	/** 
	 * The table selection listener attached to the table displaying the 
	 * objects.
	 */
	protected ListSelectionListener			listener;
	
	/** Then names of the columns. */
	protected List<String> columnNames;
	
	/** The save button. */
	protected JButton saveButton;
	
	/** The save button. */
	protected JButton clearButton;
	
	/** The save button. */
	protected JButton removeButton;
	
	/** The wizard Button.*/
	protected JButton wizardButton;
	
	/** The load button. */
	protected JButton loadButton;
	
	/** The list of buttons and their visibility. */
	protected Map<JButton, Boolean> buttonVisibility;
	
	/**
	 * Create the resultsTable to display the requested results from the user.
	 */
	public ResultsTable(List<String> columnNames)
	{
		this.columnNames = columnNames;
		initComponents();
		buildUI();
	}
	
	protected void initComponents()
	{
		tableView = new TableView();
		tableView.getTableHeader().setReorderingAllowed(false);
		ResultsTableModel tableModel = new ResultsTableModel(columnNames);
		
		tableView.setModel(tableModel);
		tableView.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		tableView.setRowSelectionAllowed(true);
		tableView.setColumnSelectionAllowed(false);
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
		createButtons();
	}
	
	/**
	 * Create the buttons on the table.
	 */
	protected void createButtons()
	{
		buttonVisibility = new LinkedHashMap<JButton, Boolean>();
		saveButton = createButton("Save", "Save results to CSV file.",SAVEACTION, true);
		loadButton = createButton("Load", "Load results from CSV file.",LOADACTION, true);
		clearButton = createButton("Clear", "Clear the table",CLEARACTION, true);
		wizardButton = createButton("Wizard", "Display the results wizard.",WIZARDACTION, true);
	}
	
	/**
	 * Create a button with name, tooltip and actionCommand, add table as listener to button.
	 * @param name See above.
	 * @param tooltop See above.
	 * @param actionCommand See above.
	 * @return See above.
	 */
	protected JButton createButton(String name, String tooltop, String actionCommand, boolean isVisible)
	{
		JButton button = new JButton(name);
		button.setToolTipText(tooltop);
		button.setActionCommand(actionCommand);
		button.addActionListener(this);
		buttonVisibility.put(button, isVisible);
		return button;
	}
	
	/**
	 * Build the UI components into the table.
	 */
	protected void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JScrollPane(tableView));
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		Iterator<JButton> keyIterator = buttonVisibility.keySet().iterator();
		while(keyIterator.hasNext())
		{
			JButton button = keyIterator.next();
			if(buttonVisibility.get(button))
				buttonPanel.add(button);
		}
		add(buttonPanel);
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
	
	/**
	 * Clear the table of all values.
	 */
	public void clearTable()
	{
		ResultsTableModel tableModel = (ResultsTableModel)tableView.getModel();
		tableModel.clear();
	}
	
	/**
	 * Overrides {@link ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		
	}

	/** Basic inner class use to set the cell renderer. */
	class TableView
		extends JTable
	{
		
		/** Creates a new instance. */
		TableView()
		{
			super();
		}
		
		/**
		 * Overridden to return a customized cell renderer.
		 * @see JTable#getCellRenderer(int, int)
		 */
		public TableCellRenderer getCellRenderer(int row, int column) 
		{
			return null;
	    }
	}
	
}
