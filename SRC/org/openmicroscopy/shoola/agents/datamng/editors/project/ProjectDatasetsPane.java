/*
 * org.openmicroscopy.shoola.agents.datamng.editors.ProjectDatasetsTab
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

package org.openmicroscopy.shoola.agents.datamng.editors.project;

//Java imports
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.util.ui.TableComponent;
import org.openmicroscopy.shoola.util.ui.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.TableComponentCellRenderer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class ProjectDatasetsPane
	extends JPanel
{
	
	/** ID to position the components. */
	private static final int		POS_ONE = 0, POS_TWO = 1, POS_THREE = 2,
									POS_FOUR = 3;
	
	/** Reference to the manager. */
	private ProjectEditorManager	manager;

	private JButton					removeButton, resetButton;
	
	private JPanel					tableToAddPanel, buttonsPanel, tablePanel;
	
	private DatasetsTableModel 		datasetsTM;
	
	private List					datasetsToAdd;
	
	private List					listDatasets;
	
	ProjectDatasetsPane(ProjectEditorManager manager)
	{
		this.manager = manager;
		datasetsToAdd = new ArrayList();
		buildGUI();
	}

	/** Return the remove button. */
	JButton getRemoveButton() { return removeButton; }
	
	/** Return the reset button. */
	JButton getResetButton() { return resetButton; }
	
	/** Select or not all datasets. */
	void setSelection(Object val)
	{
		int countCol = datasetsTM.getColumnCount()-1;
		for (int i = 0; i < datasetsTM.getRowCount(); i++)
			datasetsTM.setValueAt(val, i, countCol);
	}
	
	/** Rebuild the component if some datasets are marked to be added. */ 
	void buildComponent(List l)
	{
		datasetsToAdd = l;
		removeAll();
		tableToAddPanel = buildTableToAddPanel();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(tablePanel, POS_ONE);
		add(tableToAddPanel, POS_TWO);
		add(Box.createRigidArea(DataManager.VBOX), POS_THREE);
		add(buttonsPanel, POS_FOUR);
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		listDatasets = manager.getProjectData().getDatasets();
		tablePanel = buildTablePanel();
		buttonsPanel = buildButtonsPanel();
		tableToAddPanel = buildTableToAddPanel();
		JPanel p = new JPanel();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(tablePanel, POS_ONE);
		add(tableToAddPanel, POS_TWO);
		add(Box.createRigidArea(DataManager.VBOX), POS_THREE);
		add(buttonsPanel, POS_FOUR);
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}

	/** Initializes and build panel containing the buttons. */
	private JPanel buildButtonsPanel()
	{
		JPanel controls = new JPanel();
		//remove button
		removeButton = new JButton("Remove All");
		removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		removeButton.setToolTipText(
			UIUtilities.formatToolTipText("Remove all datasets."));
		
		//cancel button
		resetButton = new JButton("Reset");
		resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		resetButton.setToolTipText(
			UIUtilities.formatToolTipText("Cancel selection."));

		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(resetButton);
		controls.add(Box.createRigidArea(DataManager.HBOX));
		controls.add(removeButton);
		controls.setOpaque(false); //make panel transparent
		
		if (listDatasets == null || listDatasets.size() == 0) {
			removeButton.setEnabled(false);
			resetButton.setEnabled(false);
		}
		return controls;
	}
	
	/** Build panel with table containing the datasets to add. */
	private JPanel buildTableToAddPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		if (datasetsToAdd.size() != 0) {
			p.add(buildLabelTable());
			DatasetsAddTableModel datasetsAddTM = new DatasetsAddTableModel();
			JTable table = new JTable(datasetsAddTM);
			table.setBackground(DataManager.STEELBLUE);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setPreferredScrollableViewportSize(DataManager.VP_DIM);
			//wrap table in a scroll pane and add it to the panel
			JScrollPane spAdd = new JScrollPane(table);
			p.add(spAdd);
		}
		return p;
	}
	
	/** Build panel with table containing existing datasets. */
	private JPanel buildTablePanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		//datasets table
		datasetsTM = new DatasetsTableModel();
		JTable t = new JTable(datasetsTM);
		//Set the columns' width.
		TableColumnModel columns = t.getColumnModel();
		TableColumn column = columns.getColumn(1);
		column.setPreferredWidth(DataManager.SELECT_COLUMN_WIDTH);
		column.setWidth(DataManager.SELECT_COLUMN_WIDTH);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setPreferredScrollableViewportSize(DataManager.VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane sp = new JScrollPane(t);
		p.add(sp);
		return p;
	}
	
	private TableComponent buildLabelTable()
	{
		TableComponent table = new TableComponent(1, 3);
		setTableLayout(table);
		//First row.
		JLabel label = new JLabel(" Datasets to add");
		table.setValueAt(label, 0, 0);
		label = new JLabel("");
		table.setValueAt(label, 0, 1);
		table.setValueAt(label, 0, 2);
		return table;
	}
	
	/** Set the layout of the table. */
	private void setTableLayout(TableComponent table)
	{
		table.setTableHeader(null);
		table.setOpaque(false);
		table.setShowGrid(false);
		table.setRowHeight(DataManager.ROW_NAME_FIELD);
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
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
		private final String[]	columnNames = {"Name", "Remove"};
		private final Object[]	datasets = listDatasets.toArray();
		private Object[][] 		data = new Object[datasets.length][2];
		private Map datasetSummaries;

		private DatasetsTableModel()
		{
			datasetSummaries = new HashMap();
			DatasetSummary ds;
			for (int i = 0; i < datasets.length; i++) {
				ds = (DatasetSummary) datasets[i];
				String sID = ""+ ds.getID();
				data[i][0] = ds.getName();
				data[i][1] = new Boolean(false);
				datasetSummaries.put(new Integer(i), ds);
			}
		}
	
		public int getColumnCount() { return 2; }
	
		public int getRowCount() { return datasets.length; }
	
		public String getColumnName(int col) { return columnNames[col]; }
		
		public Class getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}

		public Object getValueAt(int row, int col) { return data[row][col]; }

		public boolean isCellEditable(int row, int col)
		{ 
			boolean isEditable = false;
			if (col == 1) isEditable = true;
			return isEditable;
		}
		
		public void setValueAt(Object value, int row, int col)
		{
			data[row][col]= value;
			fireTableCellUpdated(row, col);
			DatasetSummary ds = (DatasetSummary) 
								datasetSummaries.get(new Integer(row));
			manager.selectDataset(((Boolean) value).booleanValue(), ds);
		}
	}
	
	/** 
	 * A <code>3</code>-column table model to view the summary of 
	 * datasets to be added to the project.
	 * The first column contains the datasets ID and the 
	 * second column the names. Cells are not editable. 
	 */
	private class DatasetsAddTableModel
		extends AbstractTableModel
	{
		private final String[]	columnNames = {"Name", "Remove"};
		private final Object[]	datasets = datasetsToAdd.toArray();
		private Object[][]		data = new Object[datasets.length][2];
		private Map 			datasetSummaries;

		private DatasetsAddTableModel()
		{
			datasetSummaries = new HashMap();
			DatasetSummary ds;
			for (int i = 0; i < datasets.length; i++) {
				ds = (DatasetSummary) datasets[i];
				data[i][0] = ds.getName();
				data[i][1] = new Boolean(false);
				datasetSummaries.put(new Integer(i), ds);
			}
		}

		public int getColumnCount() { return 2; }

		public int getRowCount() { return datasets.length; }

		public String getColumnName(int col) { return columnNames[col]; }
	
		public Class getColumnClass(int c)
		{ 
			return getValueAt(0, c).getClass();
		}

		public Object getValueAt(int row, int col) { return data[row][col]; }

		public boolean isCellEditable(int row, int col)
		{ 
			boolean isEditable = false;
			if (col == 1) isEditable = true;
			return isEditable;
		}
	
		public void setValueAt(Object value, int row, int col)
		{
			data[row][col]= value;
			fireTableCellUpdated(row, col);
			DatasetSummary ds = (DatasetSummary) 
								datasetSummaries.get(new Integer(row));
			manager.updateAddSelection(((Boolean) value).booleanValue(), ds);
		}
	}

}
