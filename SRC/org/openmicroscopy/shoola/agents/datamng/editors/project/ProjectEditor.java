/*
 * org.openmicroscopy.shoola.agents.datamng.editors.project.ProjectEditor
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
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

/** 
 * Project's property sheet.
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
	extends JPanel
{	
	
	/** ID to position the components. */
	static final int               POS_MAIN = 0, POS_DATASET = 1, POS_OWNER = 2;
									
	/** Reference to the manager. */
	private ProjectEditorManager   manager;
	
	/** Reference to the {@link DataManagerCtrl}. */
	private DataManagerCtrl agentCtrl;
	
	private ProjectGeneralPane		generalPane;
	private ProjectDatasetsPane		datasetsPane;
	private ProjectOwnerPane		ownerPane;
	
	private ProjectEditorBar		bar;
	private JTabbedPane				tabs;
	
	public ProjectEditor(DataManagerCtrl agentCtrl, ProjectData model)
	{
        this.agentCtrl = agentCtrl;
		manager = new ProjectEditorManager(this, agentCtrl, model);
		generalPane = new ProjectGeneralPane(manager);
		datasetsPane = new ProjectDatasetsPane(manager);
		ownerPane = new ProjectOwnerPane(manager);
		bar = new ProjectEditorBar();
		buildGUI();
		manager.initListeners();
	}
	
	Registry getRegistry() { return agentCtrl.getRegistry(); } 
	
	ProjectDatasetsPane getDatasetsPane() { return datasetsPane; }
	
    /** Returns the view button displayed in {@link ProjectEditorBar}. */
    JButton getViewButton() { return bar.viewButton; }
    
	/** Returns the add button displayed in {@link ProjectEditorBar}. */
	JButton getAddButton() { return bar.addButton; }
	
	/**  Returns the save button displayed {@link ProjectEditorBar}. */
	JButton getSaveButton() { return bar.saveButton; }
	
	/** Returns the remove button displayed in {@link ProjectDatasetsPane}. */
	JButton getRemoveButton() { return datasetsPane.removeButton; }
	
	/** Returns the cancel button displayed in {@link ProjectDatasetsPane}. */
	JButton getResetButton() { return datasetsPane.resetButton; }

	/** Return the remove button. */
	JButton getRemoveToAddButton() { return datasetsPane.removeToAddButton; }
	
	/** Return the reset button. */
	JButton getResetToAddButton() {return datasetsPane.resetToAddButton; }
	
	/** Returns the TextArea displayed in {@link ProjectGeneralPane}. */
	JTextArea getDescriptionArea() { return generalPane.descriptionArea; }

	/** Returns the textfield displayed in {@link ProjectGeneralPane}. */
	JTextArea getNameField() { return generalPane.nameField; }
	
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
		IconManager im = IconManager.getInstance(getRegistry());
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
        Registry registry = getRegistry();
  		IconManager im = IconManager.getInstance(registry);
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		tabs.insertTab("General", im.getIcon(IconManager.PROJECT), generalPane,
						null, POS_MAIN);
		tabs.insertTab("Datasets", im.getIcon(IconManager.DATASET), 
						datasetsPane, null, POS_DATASET);
		tabs.insertTab("Owner", im.getIcon(IconManager.OWNER), ownerPane, null, 
						POS_OWNER);
  		tabs.setSelectedComponent(generalPane);
		tabs.setFont(font);
		tabs.setForeground(DataManagerUIF.STEELBLUE);
		TitlePanel tp = new TitlePanel("Edit Project", 
                                        "Edit an existing project.", 
										im.getIcon(IconManager.PROJECT_BIG));
        setLayout(new BorderLayout(0, 0));
        add(tp, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH); 
	}

}
