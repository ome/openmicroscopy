/*
 * org.openmicroscopy.shoola.agents.datamng.editors.ImageGeneralPane
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import org.openmicroscopy.shoola.env.data.model.ImageData;

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
class ImageGeneralPane
	extends JPanel
{

	private ImageEditorManager manager;
	private Registry				registry;
	private JButton					saveButton, reloadButton;
	
	ImageGeneralPane(ImageEditorManager manager, Registry registry)
	{
		this.manager = manager;
		this.registry = registry;
		buildGUI();
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
		saveButton.setContentAreaFilled( false ); 
		saveButton.setToolTipText("Save data to the DB");
		saveButton.setEnabled(false);
		
		//reload button
		reloadButton = new JButton(IM.getIcon(IconManager.RELOAD_DB));
		//get rid of surrounding border
		reloadButton.setBorder(null);
		reloadButton.setMargin(null);
		reloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//make panel transparent
		reloadButton.setOpaque(false);
		//suppress button press decoration
		reloadButton.setContentAreaFilled( false ); 
		reloadButton.setToolTipText("Reload data from the DB");
		
		JPanel controls = new JPanel(), all = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		all.setLayout(gridbag);  
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(reloadButton);
		controls.add(Box.createRigidArea(new Dimension(10, 0)));
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
		ImageTableModel imageTM = new ImageTableModel();
		JTable  t = new JTable(imageTM);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setTableHeader(null);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(t);
		p.add(all);
		//make panel transparent
		p.setOpaque(false);
		return p;
	}
	
	/**Returns the save button. */
	public JButton getSaveButton()
	{ 
		return saveButton;
	}
	
	/**Returns the reload button. */
	public JButton getReloadButton()
	{ 
		return reloadButton;
	}
	
	/** A <code>3x2</code> table model to view image summary.
	 * The first column contains the property names (id, name, description)
	 * and the second column holds the corresponding values. 
	 * <code>name</code> and <code>description</code> values
	 * are marked as editable. 
	 */
	private class ImageTableModel 
		extends AbstractTableModel 
	{

		private final String[]    fieldNames = {"ID", "Name", "Description"};
		ImageData id = manager.getImageData();
		private final Object[]	  
		data = {""+id.getID(), id.getName(), id.getDescription()};
		private ImageTableModel() {}

		public int getColumnCount() { return 2; }

		public int getRowCount() { return 3; }

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
			if (row == 0 && col == 1) isEditable = false;
			return isEditable;
		}
		
		public void setValueAt(Object value, int row, int col)
		{
			if (col != 0) {
				data[row]= value;
				fireTableCellUpdated(row, col);
				manager.setImageFields(value, row);
			}
		}
	}	
	
}
