/*
 * org.openmicroscopy.shoola.agents.datamng.editors.image.ImportImageSelectionPane
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

package org.openmicroscopy.shoola.agents.datamng.editors.image;



//Java imports
import java.io.File;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;

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

class ImportImageSelectionPane
	extends JPanel
{
	
	private static final int		POS_ZERO = 0, POS_ONE = 1, POS_TWO = 2;
									
	private JComboBox				existingDatasets;
	private ImportImageChooserMng 	manager;
	private JPanel					titlePanel;
	
	ImportImageSelectionPane(List datasets, ImportImageChooserMng manager)
	{
		this.manager = manager;
		initComboBox(datasets);
		buildGUI();
	}
	
	JComboBox getExistingDatasets() { return existingDatasets; }
	
	private void initComboBox(List datasets)
	{
		existingDatasets = new JComboBox(datasets.toArray());
	}

	/** Re-build the component. */
	void rebuildComponent()
	{
		removeAll();
		add(titlePanel, POS_ZERO);
		add(Box.createRigidArea(DataManager.VBOX), POS_ONE);
		if (manager.getFilesToImport().size() != 0)
			add(buildTableToAddPanel(), POS_TWO);
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		buildTitlePanel();
		add(titlePanel, POS_ZERO);
	}
	
	/** Build panel with combobox. */
	private void buildTitlePanel()
	{
		titlePanel = new JPanel();
		JLabel label = new JLabel("Import selected images into ");
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
		titlePanel.add(label);
		titlePanel.add(existingDatasets);
	}
	
	/** Build panel with table containing the images to add. */
	
	private JPanel buildTableToAddPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		ImagesToAddTableModel tm = new ImagesToAddTableModel();
		JTable table = new JTable(tm);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(DataManager.VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane spAdd = new JScrollPane(table);
		p.add(spAdd);
		return p;
	}
	
	/** 
	 * A <code>2</code>-column table model to view the list of selected files.
	 * The first column contains the datasets ID and the 
	 * second column the names. Cells are not editable. 
	 */
	private class ImagesToAddTableModel
		extends AbstractTableModel
	{
		private final String[]	columnNames = {"Name", "Remove"};
		private Object[] files = manager.getFilesToImport().toArray();
		private List filesToRemove = manager.getFilesToRemove();
		private Object[][] 		data = new Object[files.length][2];
		
		private ImagesToAddTableModel()
		{
			File f;
			Boolean b;
			for (int i = 0; i < files.length; i++) {
				f = (File) files[i];
				data[i][0] = f.getName();
				b = new Boolean(filesToRemove.contains(f));
				data[i][1] = b;
			}
		}

		public int getColumnCount() { return 2; }

		public int getRowCount() { return files.length; }

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
			manager.setFilesToRemove(((Boolean) value).booleanValue(), 
									(File) files[row]);
		}
	}
}
