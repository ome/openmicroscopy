/*
 * org.openmicroscopy.shoola.agents.datamng.editors.CreateDatasetPane
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.util.ui.TableComponent;
import org.openmicroscopy.shoola.util.ui.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.TableComponentCellRenderer;

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
class CreateDatasetPane
	extends JPanel
{
	
	private CreateDatasetEditorManager 	manager;
	private Registry 					registry;
	
	private JButton						saveButton;
	private JTextArea					nameField;
	
	private JTextArea					descriptionArea;
	
	/**
	 * @param manager
	 * @param registry
	 */
	CreateDatasetPane(CreateDatasetEditorManager manager, Registry registry) 
	{
		this.manager = manager;
		this.registry = registry;
		buildGUI();
	}
	
	/** Returns the TextArea with the project's description. */
	JTextArea getDescriptionArea()
	{
		return descriptionArea;
	}

	/** Returns the textfield with project's name. */
	JTextArea getNameField()
	{
		return nameField;
	}
	
	/** Returns the save button. */
	JButton getSaveButton()
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
	
	/** Build panel containing fields to fill in. */
	private JPanel buildSummaryPanel() 
	{	
		JPanel  p = new JPanel();
		//save button
		saveButton = new JButton("Save");
		//get rid of surrounding border
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
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildTable());
		p.add(all);
		//make panel transparent
		p.setOpaque(false);
		
		return p;
	}
	
	/** 
	 * A <code>2x2</code> table model to view dataset summary.
	 * The first column contains the property names (name, description)
	 * and the second column holds the corresponding values. 
	 * <code>name</code> and <code>description</code> values
	 * are marked as editable.
	 */
	private JTable buildTable()
	{
		JTable table = new TableComponent(2, 2);
		table.setTableHeader(null);
		table.setRowHeight(1, DataManager.ROW_TABLE_HEIGHT);
		table.setRowHeight(0, DataManager.ROW_NAME_FIELD);
		// Labels
		table.setValueAt(new JLabel(" Name"), 0, 0);
		table.setValueAt(new JLabel(" Description"), 1, 0);

		DatasetData pd = manager.getDatasetData();
	
		//textfields
		nameField = new JTextArea(pd.getName());
		nameField.setForeground(DataManager.STEELBLUE);
		nameField.setEditable(true);
		nameField.setLineWrap(true);
		nameField.setWrapStyleWord(true);
		JScrollPane scrollPaneName  = new JScrollPane(nameField);
		scrollPaneName.setPreferredSize(DataManager.DIM_SCROLL_NAME);
		table.setValueAt(scrollPaneName, 0, 1);  

		descriptionArea = new JTextArea(pd.getDescription());
		descriptionArea.setForeground(DataManager.STEELBLUE);
		descriptionArea.setEditable(true);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		JScrollPane scrollPane  = new JScrollPane(descriptionArea);
		scrollPane.setPreferredSize(DataManager.DIM_SCROLL_TABLE);

		// Scrollbar
		table.setValueAt(scrollPane,1, 1);

		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
		return table;
	}
	
}
