/*
 * org.openmicroscopy.shoola.agents.datamng.ps.PropertySheet
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
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
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
public class ProjectEditor
	extends JDialog
{	
	
	/** ID to position the components. */
	static final int		POS_MAIN = 0, POS_DATASET = 1, POS_OWNER = 2;
									
	/** Reference to the manager. */
	private ProjectEditorManager 	manager;
	
	/** Reference to the registry. */
	private Registry				registry;
	
	private ProjectGeneralPane		generalPane;
	private ProjectDatasetsPane		datasetsPane;
	private ProjectOwnerPane		ownerPane;
	
	private ProjectEditorBar		bar;
	private JTabbedPane				tabs;
	
	public ProjectEditor(Registry registry, DataManagerCtrl control,
						 ProjectData model)
	{
		super((JFrame) registry.getTopFrame().getFrame(), true);
		this.registry = registry;
		manager = new ProjectEditorManager(this, control, model);
		generalPane = new ProjectGeneralPane(manager, registry);
		datasetsPane = new ProjectDatasetsPane(manager);
		ownerPane = new ProjectOwnerPane(manager);
		bar = new ProjectEditorBar(manager);
		buildGUI();
		manager.initListeners();
		setSize(DataManager.EDITOR_WIDTH, DataManager.EDITOR_HEIGHT);
	}
	
	ProjectDatasetsPane getDatasetsPane() { return datasetsPane; }
	
	/** Returns the cancel button displayed in {@link ProjectEditorBar}. */
	JButton getAddButton() { return bar.getAddButton(); }
	
	/**  Returns the save button displayed {@link ProjectEditorBar}. */
	JButton getSaveButton() { return bar.getSaveButton(); }
	
	/** Returns the cancel button displayed in {@link ProjectEditorBar}. */
	JButton getCancelButton() { return bar.getCancelButton(); }
	
	/** Returns the remove button displayed in {@link ProjectDatasetsPane}. */
	JButton getRemoveButton() { return datasetsPane.getRemoveButton(); }
	
	/** Returns the cancel button displayed in {@link ProjectDatasetsPane}. */
	JButton getResetButton() { return datasetsPane.getResetButton(); }

	/** Return the remove button. */
	JButton getRemoveToAddButton()
	{ 
		return datasetsPane.getRemoveToAddButton();
	}
	
	/** Return the reset button. */
	JButton getResetToAddButton() {return datasetsPane.getResetToAddButton(); }
	
	/** Returns the TextArea displayed in {@link ProjectGeneralPane}. */
	JTextArea getDescriptionArea() { return generalPane.getDescriptionArea(); }

	/** Returns the textfield displayed in {@link ProjectGeneralPane}. */
	JTextArea getNameField() { return generalPane.getNameField(); }
	
	/** 
	 * Set the selected tab.
	 * 
	 * @param index	index is one of the following cst 
	 * 				<code>POS_DATASET</code>, <code>POS_MAIN</code>, 
	 * 				<code>POS_OWNER</code>.
	 */
	void setSelectedPane(int index) { tabs.setSelectedIndex(index); }
	
	/** Re-build the datasetsPane component. */
	void rebuildComponent()
	{
		tabs.remove(POS_DATASET);
		datasetsPane.rebuildComponent();
		IconManager im = IconManager.getInstance(registry);
		tabs.insertTab("Datasets", im.getIcon(IconManager.DATASET), 
						datasetsPane, null, POS_DATASET);
		tabs.setSelectedIndex(POS_DATASET);	
	}
	
	/** Build and layout the GUI. */
	void buildGUI()
	{
		//create and initialize the tabs
		tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
  		tabs.setAlignmentX(LEFT_ALIGNMENT);
  		IconManager im = IconManager.getInstance(registry);
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		tabs.insertTab("General", im.getIcon(IconManager.PROJECT), generalPane,
						null, POS_MAIN);
		tabs.insertTab("Datasets", im.getIcon(IconManager.DATASET), 
						datasetsPane, null, POS_DATASET);
		tabs.insertTab("Owner", IconManager.getOMEIcon(), ownerPane, null, 
						POS_OWNER);
  		tabs.setSelectedComponent(generalPane);
		tabs.setFont(font);
		tabs.setForeground(DataManager.STEELBLUE);
  		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);
		getContentPane().add(bar, BorderLayout.SOUTH);	
	}

}
