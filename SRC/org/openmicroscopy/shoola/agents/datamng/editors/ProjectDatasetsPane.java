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
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
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

	ProjectDatasetsPane(ProjectEditorManager manager)
	{
		this.manager = manager;
		buildGUI();
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildDatasetsPanel());
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}
	
	private JScrollPane buildDatasetsPanel()
	{
		//datasets table
		DatasetsTableModel datasetsTM = new DatasetsTableModel();
		JTable  t = new JTable(datasetsTM);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setPreferredScrollableViewportSize(VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane     sp = new JScrollPane(t);
		return sp;
	}
	
	/** A <code>2</code>-column table model to view the summary of 
	 * datasets contained in the project.
	 * The first column contains the datasets ID and the 
	 * second column the names. Cells are not editable. 
	 */
	private class DatasetsTableModel
		extends AbstractTableModel
	{
	
		private final String[]    columnNames = {"ID", "Name"};
		private final Object[]    
		datasets = manager.getProjectData().getDatasets().toArray();
	
		private DatasetsTableModel() {}
	
		public int getColumnCount() { return 2; }
	
		public int getRowCount() { return datasets.length; }
	
		public String getColumnName(int col)
		{
			return columnNames[col];
		}
	
		public Object getValueAt(int row, int col)
		{
			Object  val = null;
			if (col == 0) val = ""+((DatasetSummary) datasets[row]).getID();
			else val = ((DatasetSummary) datasets[row]).getName();
			return val;
		}
		
		//cells may not be edited
		public boolean isCellEditable(int row, int col) { return false; }
	}
	
}
