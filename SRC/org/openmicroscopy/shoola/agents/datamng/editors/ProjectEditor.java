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
package org.openmicroscopy.shoola.agents.datamng.editors;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.IconFactory;
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
	private static final int		GENERAL_PANE = 0;
	private static final int		DATASETS_PANE = 1;
	private static final int		OWNER_PANE	= 2;
	private static final int 		WIN_WIDTH = 300;
	private static final int 		WIN_HEIGHT = 300;
	
	private static final Color   	STEELBLUE = new Color(0x4682B4);
	
	/** Reference to the manager. */
	private ProjectEditorManager 	manager;
	
	/** Reference to the registry. */
	private Registry				registry;
	
	private ProjectGeneralPane		generalPane;
	private ProjectDatasetsPane		datasetsPane;
	private ProjectOwnerPane		ownerPane;
	
	/** 
	* The image icons needed to build the GUI.
	* The first icon is the explorer.
	*/
	private Icon[]			images;
	
	public ProjectEditor(Registry registry, ProjectData model)
	{
		super((JFrame) registry.getTopFrame().getFrame(), true);
		this.registry = registry;
		manager = new ProjectEditorManager(this, model);
		generalPane = new ProjectGeneralPane(manager, registry);
		datasetsPane = new ProjectDatasetsPane(manager);
		ownerPane = new ProjectOwnerPane(manager);
		loadImages();
		buildGUI();
		manager.initListeners();
		setSize(WIN_WIDTH, WIN_HEIGHT);
	}
	
	void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
  		tabs.setAlignmentX(LEFT_ALIGNMENT);
		//TODO: specify lookup name.
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
  		tabs.addTab("General", images[GENERAL_PANE], generalPane);
  		tabs.addTab("Datasets", images[DATASETS_PANE], datasetsPane);
		tabs.addTab("Owner", images[OWNER_PANE], ownerPane);
  		tabs.setSelectedComponent(generalPane);
		tabs.setFont(font);
		tabs.setForeground(STEELBLUE);
  		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);	
	}
	
	/**
	 * Fills up the <code>images</code> array.
	 */
	private void loadImages()
	{
		images = new Icon[3];
		IconFactory factory = (IconFactory) 
							registry.lookup("/resources/icons/Factory");
		images[GENERAL_PANE] = factory.getIcon("project16.png");
		images[DATASETS_PANE] = factory.getIcon("dataset16.png");
		images[OWNER_PANE] = factory.getIcon("OME16.png"); //??
		//TODO: handle nulls (image not loaded).
	}
	
	/** 
	 * Returns the project table displayed in the generalPane.
	 * Forward event to generalPane.
	 */
	public JTable getProjectTable()
	{
		return generalPane.getProjectTable();
	}
	
	/** 
	 * Returns the save button displayed in the generalPane.
	 * Forward event to generalPane.
	 */
	public JButton getSaveButton()
	{
		return generalPane.getSaveButton();
	}
	
	/** 
	 * Returns the reload button displayed in the generalPane.
	 * Forward event to generalPane.
	 */
	public JButton getReloadButton()
	{
		return generalPane.getReloadButton();
	}
	
}
