/*
 * org.openmicroscopy.shoola.agents.datamng.editors.ProjectDatasetsDiffPane
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
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.util.ui.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.TableSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
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
class ProjectDatasetsDiffPane
	extends JDialog
{

	/** Action id. */
	private static final int				NAME = 0, SELECT = 1;
			
	protected static final String[]			columnNames;
	
	static {
		columnNames  = new String[2];
		columnNames[NAME] = "Name";
		columnNames[SELECT] = "Select";
	}

	private JButton							selectButton, cancelButton, 
											saveButton;
											
	private DatasetsTableModel 				datasetsTM;
	
	private TableSorter 					sorter;
	
	/** Reference to the control of the main widget. */
	private ProjectEditorManager 			control;
	
	private ProjectDatasetsDiffPaneManager	manager;
	
	private JPanel							contents;
	
	private IconManager						im;
	
	ProjectDatasetsDiffPane(ProjectEditorManager control, List datasetsDiff)
	{
		super(control.getView(), "List of existing datasets", true);
		this.control = control;
		im = IconManager.getInstance(control.getView().getRegistry());
		initButtons(datasetsDiff);
		manager = new ProjectDatasetsDiffPaneManager(this, control, 
													datasetsDiff);
		buildGUI();
	}
	
	/**
	 * Return the {@link ProjectDatasetsDiffPaneManager manager} of the widget.
	 */
	ProjectDatasetsDiffPaneManager getManager() { return manager; }
	
	JPanel getContents() { return contents; }
	
	/** Return select button. */
	JButton getSelectButton() { return selectButton; }
	
	/** Return select button. */
	JButton getCancelButton() { return cancelButton; }
	
	/** Return select button. */
	JButton getSaveButton() { return saveButton; }
	
	/** Select or not all datasets. */
	void setSelection(Object val)
	{
		int countCol = datasetsTM.getColumnCount()-1;
		for (int i = 0; i < datasetsTM.getRowCount(); i++)
			datasetsTM.setValueAt(val, i, countCol);
	}
	
	/** 
	 * Initializes the controls. 
	 * 
	 * @param d List of datasets which don't belong to the project.
	 */
	private void initButtons(List d)
	{
		//remove button
		selectButton = new JButton("Select All");
		selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		selectButton.setToolTipText(
			UIUtilities.formatToolTipText("Select all datasets."));
		//cancel button
		cancelButton = new JButton("Reset");
		cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cancelButton.setToolTipText(
			UIUtilities.formatToolTipText("Cancel selection."));
		//cancel button
		saveButton = new JButton("OK");
		saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		saveButton.setToolTipText(
			UIUtilities.formatToolTipText("Add the selection."));
		
		if (d == null || d.size() == 0) {
			selectButton.setEnabled(false);
			cancelButton.setEnabled(false);
			saveButton.setEnabled(false);
		}	
	}
	
	/** Build and lay out the GUI. */
	void buildGUI()
	{
		TitlePanel tp = new TitlePanel(" Add datasets", 
								"  Select datasets to add to the project.", 
							im.getIcon(IconManager.DATASET_BIG));
		contents = buildDatasetsPanel();
		contents.setSize(DataManager.ADD_WIN_WIDTH, DataManager.ADD_WIN_HEIGHT);
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tp, BorderLayout.NORTH);
		getContentPane().add(contents, BorderLayout.CENTER);
		setSize(DataManager.ADD_WIN_WIDTH, DataManager.ADD_WIN_HEIGHT);
	}
	
	/** Build panel with table. */
	JPanel buildDatasetsPanel()
	{
		JPanel controls = new JPanel(), p = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(cancelButton);
		controls.add(Box.createRigidArea(DataManager.HBOX));
		controls.add(selectButton);
		controls.add(Box.createRigidArea(DataManager.HBOX));
		controls.add(saveButton);
		controls.setOpaque(false); //make panel transparent
		
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		//datasets table
		datasetsTM = new DatasetsTableModel();
		JTable t = new JTable();
		sorter = new TableSorter(datasetsTM);  
		t.setModel(sorter);
		sorter.addMouseListenerToHeaderInTable(t);
		setTableLayout(t);
		
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setPreferredScrollableViewportSize(DataManager.VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane sp = new JScrollPane(t);
		p.add(sp);
		p.add(Box.createRigidArea(DataManager.VBOX));
		p.add(controls);
		p.add(Box.createRigidArea(DataManager.VBOX));
		return p;
	}

	/** Set icons in the tableHeader. */
	private void setTableLayout(JTable table)
	{
		IconManager im = IconManager.getInstance(
							control.getView().getRegistry());
		TableIconRenderer iconHeaderRenderer = new TableIconRenderer();
		TableColumnModel tcm = table.getTableHeader().getColumnModel();
		TableColumn tc = tcm.getColumn(NAME);
		tc.setHeaderRenderer(iconHeaderRenderer);
		TableHeaderTextAndIcon 
		txt = new TableHeaderTextAndIcon(columnNames[NAME], 
				im.getIcon(IconManager.ORDER_BY_NAME), 
				"Order datasets by name.");
		tc.setHeaderValue(txt);
		tc = tcm.getColumn(SELECT);
		tc.setHeaderRenderer(iconHeaderRenderer); 
		txt = new TableHeaderTextAndIcon(columnNames[SELECT], 
				im.getIcon(IconManager.ORDER_BY_SELECTED), 
				"Order by selected datasets.");
		tc.setHeaderValue(txt);
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
		private final Object[]		datasets = 
									manager.getDatasetsDiff().toArray();
		private Object[][] 			data = new Object[datasets.length][2];
		
		private DatasetsTableModel()
		{
			for (int i = 0; i < datasets.length; i++) {
				data[i][0] = (DatasetSummary) datasets[i];
				data[i][1] = new Boolean(false);
			}
		}
	
		public int getColumnCount() { return 2; }
	
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
			DatasetSummary ds = (DatasetSummary) sorter.getValueAt(row, NAME);
			fireTableCellUpdated(row, col);
			manager.addDataset(((Boolean) value).booleanValue(), ds);
		}
	}
	
}
