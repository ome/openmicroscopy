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

package org.openmicroscopy.shoola.agents.datamng.editors.project;


//Java imports
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
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
class CreateProjectPane
	extends JPanel
{

	private CreateProjectEditorManager 	manager;
	private Registry 					registry;
	
	private MultilineLabel				nameField, descriptionArea;
	
	/**
	 * Create a new instance.
	 * 
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

	/** Returns the TextArea with the project's description. */
	MultilineLabel getDescriptionArea() { return descriptionArea; }

	/** Returns the textfield with project's name. */
	MultilineLabel getNameField() { return nameField; }
	
	/** Build and lay out the GUI. */
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
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildTable());
		//make panel transparent
		p.setOpaque(false);
		
		return p;
	}

	/** 
 	 * A <code>2x2</code> table model to view project summary.
	 * The first column contains the property names (name, description)
	 * and the second column holds the corresponding values. 
	 * <code>name</code> and <code>description</code> values
	 * are marked as editable.
	 */
	private TableComponent buildTable()
	{
		TableComponent table = new TableComponent(2, 2);
		setTableLayout(table);
		// Labels
		table.setValueAt(new JLabel(" Name"), 0, 0);
		table.setValueAt(new JLabel(" Description"), 1, 0);

		ProjectData pd = manager.getProjectData();
		//textfields
		nameField = new MultilineLabel(pd.getName());
		nameField.setForeground(DataManager.STEELBLUE);
		nameField.setEditable(true);
		JScrollPane scrollPaneName  = new JScrollPane(nameField);
		scrollPaneName.setPreferredSize(DataManager.DIM_SCROLL_NAME);
		table.setValueAt(scrollPaneName, 0, 1); 

		descriptionArea = new MultilineLabel(pd.getDescription());
		descriptionArea.setForeground(DataManager.STEELBLUE);
		descriptionArea.setEditable(true);
		JScrollPane scrollPane  = new JScrollPane(descriptionArea);
		scrollPane.setPreferredSize(DataManager.DIM_SCROLL_TABLE);

		// Scrollbar
		table.setValueAt(scrollPane, 1, 1);
		return table;
	}

	/** Set the tablelayout. */
	private void setTableLayout(TableComponent table)
	{
		table.setTableHeader(null);
		table.setRowHeight(1, DataManager.ROW_TABLE_HEIGHT);
		table.setRowHeight(0, DataManager.ROW_NAME_FIELD);
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
								
	}
	
}
