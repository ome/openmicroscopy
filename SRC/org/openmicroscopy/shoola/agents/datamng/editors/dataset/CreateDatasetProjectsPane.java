/*
 * org.openmicroscopy.shoola.agents.datamng.editors.CreateDatasetProjectsPane
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

package org.openmicroscopy.shoola.agents.datamng.editors.dataset;

//Java imports
import java.awt.Cursor;
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
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
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
class CreateDatasetProjectsPane
	extends JPanel
{
								
	/** Reference to the manager. */
	private CreateDatasetEditorManager	manager;

	private JButton						selectButton;
	private JButton						resetButton;
	private ProjectsTableModel 			projectsTM;
	
	CreateDatasetProjectsPane(CreateDatasetEditorManager manager)
	{
		this.manager = manager;
		buildGUI();
	}
	
	/** Returns the select button. */
	JButton getSelectButton() { return selectButton; }
	
	/** Returns the cancel button. */
	JButton getResetButton() { return resetButton; }

	/** Select or not all projects. */
	void setSelection(Object val)
	{
		int countCol = projectsTM.getColumnCount()-1;
		for (int i = 0; i < projectsTM.getRowCount(); i++)
			projectsTM.setValueAt(val, i, countCol); 
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildDatasetsPanel());
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}
	
	private JPanel buildDatasetsPanel()
	{
		//select button
		selectButton = new JButton("Select All");
		selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		selectButton.setToolTipText(
			UIUtilities.formatToolTipText("Select all projects."));
		//cancel button
		resetButton = new JButton("Reset");
		resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		resetButton.setToolTipText(
			UIUtilities.formatToolTipText("Cancel selection."));
		JPanel controls = new JPanel(), p = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(resetButton);
		controls.add(Box.createRigidArea(DataManager.HBOX));
		controls.add(selectButton);
		controls.setOpaque(false); //make panel transparent
	  	
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	  
		//datasets table
		projectsTM = new ProjectsTableModel();
		JTable t = new JTable(projectsTM);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setPreferredScrollableViewportSize(DataManager.VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane sp = new JScrollPane(t);
		p.add(sp);
		p.add(Box.createRigidArea(DataManager.VBOX));
		p.add(controls);
		
		return p;
	}
	
	/** 
	 * A <code>3</code>-column table model to view the summary of 
	 * project to add to a new dataset.
	 * The first column contains the projects ID and the 
	 * second column the names, the third one a check box.
	 * The first two cells are not editable, the third one is. 
	 */
	private class ProjectsTableModel
		extends AbstractTableModel
	{
		private final String[] columnNames = {"ID", "Name", "Select"};
		private final Object[] projects = manager.getProjects().toArray();
		private Object[][] data = new Object[projects.length][3];
		
		private Map projectSummaries;
		private ProjectsTableModel()
		{
			projectSummaries = new HashMap();
			for (int i = 0; i < projects.length; i++) {
				String sID = ""+((ProjectSummary) projects[i]).getID();
				data[i][0] = sID;
				data[i][1] = ((ProjectSummary) projects[i]).getName();
				data[i][2] = new Boolean(false);
				projectSummaries.put(sID, projects[i]);
			}
		}

		public int getColumnCount() { return 3; }

		public int getRowCount() { return projects.length; }

		public String getColumnName(int col) { return columnNames[col]; }

		public Class getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}

		public Object getValueAt(int row, int col) { return data[row][col]; }
	
		//cells may not be edited
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
			ProjectSummary is = (ProjectSummary) 
								projectSummaries.get((String) data[row][0]);
			manager.addProject(((Boolean) value).booleanValue(), is);
		}
	}
	
}
