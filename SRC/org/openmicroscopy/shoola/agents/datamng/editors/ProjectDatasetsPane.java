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

package org.openmicroscopy.shoola.agents.datamng.editors;

//Java imports
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;

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
	private static final Dimension	VP_DIM = new Dimension(200, 70);
	
	/** Reference to the manager. */
	private ProjectEditorManager	manager;

	private JButton					removeButton;
	private JButton					cancelButton;
	private DatasetsTableModel 		datasetsTM;
	
	ProjectDatasetsPane(ProjectEditorManager manager)
	{
		this.manager = manager;
		buildGUI();
	}
	
	public JButton getRemoveButton()
	{
		return removeButton;
	}
	
	public JButton getCancelButton()
	{
		return cancelButton;
	}

	/** Select or not all datasets. */
	void setSelection(Object val)
	{
		int countCol = datasetsTM.getColumnCount()-1;
		for (int i = 0; i < datasetsTM.getRowCount(); i++)
			datasetsTM.setValueAt(val, i, countCol);
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildDatasetsPanel());
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}
	
	private JPanel buildDatasetsPanel()
	{
		//remove button
		removeButton = new JButton("Remove All");
		removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		removeButton.setToolTipText("Remove all datasets");
		
		//cancel button
		cancelButton = new JButton("Cancel");
		cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cancelButton.setToolTipText("Cancel selection");
		JPanel controls = new JPanel(), p = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(cancelButton);
		controls.add(Box.createRigidArea(new Dimension(10, 0)));
		controls.add(removeButton);
		controls.setOpaque(false); //make panel transparent
		
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		//datasets table
		datasetsTM = new DatasetsTableModel();
		JTable t = new JTable(datasetsTM);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setPreferredScrollableViewportSize(VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane sp = new JScrollPane(t);
		
		p.add(sp);
		p.add(Box.createRigidArea(new Dimension(0, 10)));
		p.add(controls);
		
		return p;
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
		private final String[]    columnNames = {"ID", "Name", "Remove"};
		private final Object[]    
		datasets = manager.getProjectData().getDatasets().toArray();
		private Object[][] data = new Object[datasets.length][3];
		private Map datasetSummaries;
		
		private DatasetsTableModel()
		{
			datasetSummaries = new HashMap();
			for (int i = 0; i < datasets.length; i++) {
				String sID = ""+ ((DatasetSummary) datasets[i]).getID();
				data[i][0] = sID;
				data[i][1] = ((DatasetSummary) datasets[i]).getName();
				data[i][2] = new Boolean(false);
				datasetSummaries.put(sID, datasets[i]);
			}
		}
	
		public int getColumnCount() { return 3; }
	
		public int getRowCount() { return datasets.length; }
	
		public String getColumnName(int col)
		{
			return columnNames[col];
		}
		
		public Class getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}

		public Object getValueAt(int row, int col)
		{
			return data[row][col];
		}

		public boolean isCellEditable(int row, int col)
		{ 
			boolean isEditable = false;
			if (col == 2) isEditable = true;
			return isEditable;
		}
		
		public void setValueAt(Object value, int row, int col)
		{
			data[row][col]= value;
			fireTableCellUpdated(row, col);
			boolean b = ((Boolean) value).booleanValue();
			DatasetSummary ds = (DatasetSummary) 
			datasetSummaries.get((String) data[row][0]);
			manager.addDataset(b, ds);
		}
	}
	
}
