/*
 * org.openmicroscopy.shoola.agents.datamng.editors.ProjectCreationPane
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ProjectData;

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
class CreateProjectPane
	extends JPanel
{

	private CreateProjectEditorManager 	manager;
	private Registry 					registry;
	
	private JButton						saveButton;
	/**
	 * @param manager
	 * @param registry
	 */
	CreateProjectPane(CreateProjectEditorManager manager, 
								Registry registry) 
	{
		this.manager = manager;
		this.registry = registry;
		buildGUI();
	}

	/** Returns the save button. */
	public JButton getSaveButton()
	{ 
		return saveButton;
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildSummaryPanel());
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}
	
	private JPanel buildSummaryPanel() 
	{	
		JPanel  p = new JPanel();
		IconManager IM = IconManager.getInstance(registry);
		//save button
		saveButton = new JButton(IM.getIcon(IconManager.SAVE_DB));
		//get rid of surrounding border
		saveButton.setBorder(null);
		saveButton.setMargin(null);
		saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//make panel transparent
		saveButton.setOpaque(false);
		//suppress button press decoration
		saveButton.setContentAreaFilled(false); 
		saveButton.setToolTipText("Save data to the DB");
		saveButton.setEnabled(false);

		JPanel controls = new JPanel(), all = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		all.setLayout(gridbag);  
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(saveButton);
		controls.setOpaque(false); //make panel transparent
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(controls,c); 
		all.add(controls);
		all.setOpaque(false); //make panel transparent
   	
		//summary table
		ProjectTableModel projectTM = new ProjectTableModel();
		JTable projectTable = new JTable(projectTM);
		projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		projectTable.setTableHeader(null);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(projectTable);
		p.add(all);
		//make panel transparent
		p.setOpaque(false);
		return p;
	}


	/** A <code>2x2</code> table model to view project summary.
	 * The first column contains the property names (name, description)
	 * and the second column holds the corresponding values. 
	 * <code>name</code> and <code>description</code> values
	 * are marked as editable. 
	 */
	private class ProjectTableModel 
		extends AbstractTableModel 
	{

		private final String[]    fieldNames = {"Name", "Description"};
		ProjectData pd = manager.getProjectData();
	
		private final Object[]	  
			data = {pd.getName(), pd.getDescription() };
			
		private ProjectTableModel() {}
	
		public int getColumnCount() { return 2; }
	
		public int getRowCount() { return 2; }
	
		public Object getValueAt(int row, int col) 
		{
			Object  val = null;
			if (col == 0)  val = fieldNames[row];
			else val = data[row];
			return val;
		}
	
		//entries in the value column can be edited
		public boolean isCellEditable(int row, int col)
		{
			boolean isEditable = true;
			if (col == 0) isEditable = false;
			return isEditable;
		}
	
		public void setValueAt(Object value, int row, int col)
		{
			if (col != 0) {
				data[row]= value;
				fireTableCellUpdated(row, col);
				manager.setProjectFields(value, row);
			}
		}
	}

}
