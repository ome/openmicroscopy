/*
 * org.openmicroscopy.shoola.agents.datamng.DataManagerUIF
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

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
class DataManagerUIF
	extends JInternalFrame
{
	
	/** 
	 * UI component to view a summary of the user's data
	 * and to mark the currently viewed image. 
	 */
	private ExplorerPane					explPane;
	
	/** List of image summary object. */
	private ImagesPane						imgPane;
	
	/** Reference to the regisry. */
	private Registry						registry;
	
	/** Reference to the control component. */
	private DataManagerCtrl					control;
	
	/** On-request menu displayed for nodes in the tree. */
	private TreePopupMenu					popupMenu;
	
	private IconManager 					im;
	
	DataManagerUIF(DataManagerCtrl control, Registry registry)
	{
		//name, resizable, closable, maximizable, iconifiable.
		super("DataManager", true, true, true, true);
		this.registry = registry;
		this.control = control;
		im = IconManager.getInstance(registry);
		explPane = new ExplorerPane(control, registry);
		popupMenu = new TreePopupMenu(control, registry);
		imgPane = new ImagesPane(control, registry);
		setJMenuBar(createMenuBar());
		buildGUI(new ToolBar(control, registry));
		pack();	
	}

	/** Forward event to {@link ExplorerPaneManager}. */
	boolean isTreeLoaded() { return explPane.getManager().isTreeLoaded(); }
	
	/** Forward event to {@link ExplorerPaneManager}. */
	void rebuildTree() { explPane.getManager().rebuildTree(); }
	
	/** Forward event to {@link ExplorerPaneManager}. */
	void updateProjectInTree() { explPane.getManager().updateProjectInTree(); }

	/** Forward event to {@link ExplorerPaneManager}. */
	void updateDatasetInTree() { explPane.getManager().updateDatasetInTree(); }
	
	/** Forward event to {@link ExplorerPaneManager}. */
	void updateImageInTree(ImageSummary is)
	{
		explPane.getManager().updateImageInTree(is);
	}
	
	/** Forward event to {@link ImagesPaneManager}. */
	void updateImageInTable(ImageSummary is)
	{
		imgPane.getManager().updateImageInTable(is);
	}
	
	/** Forward event to {@link ExplorerPaneManager}. */
	void addNewProjectToTree(ProjectSummary ps)
	{
		explPane.getManager().addNewProjectToTree(ps);
	}
	
	/** Forward event to {@link ExplorerPaneManager}. */
	void addNewDatasetToTree(List projects)
	{
		explPane.getManager().addNewDatasetToTree(projects);
	}
	
	/** Return the menu displayed for nodes in the tree. */
	TreePopupMenu getPopupMenu() { return popupMenu; }
	
	/** Build and lay out the GUI. */
	private void buildGUI(ToolBar bar)
	{
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
											JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		//TODO: specify lookup name.
		Font font = (Font) registry.lookup("/resources/fonts/Titles");					
		tabs.addTab("Hierarchy", im.getIcon(IconManager.EXPLORER), explPane);
		tabs.addTab("Images", im.getIcon(IconManager.IMAGE), imgPane);
		tabs.setFont(font);
		tabs.setForeground(DataManager.STEELBLUE);
		tabs.setSelectedComponent(explPane);
		
		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(bar, BorderLayout.NORTH);
		getContentPane().add(tabs, BorderLayout.CENTER);
		setFrameIcon(im.getIcon(IconManager.DMANAGER));	
	} 	
	
	/** Creates an internal menu. */
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar(); 
		menuBar.add(createNewMenu());
		//menuBar.add(createImportMenu());
		return menuBar;
	}
	
	/** Creates the <code>newMenu</code>. */
	private JMenu createNewMenu()
	{
		JMenu newMenu = new JMenu("New...");
		JMenuItem menuItem = new JMenuItem("Project", 
							im.getIcon(IconManager.CREATE_PROJECT));
		control.attachItemListener(menuItem, DataManagerCtrl.PROJECT_ITEM);
		newMenu.add(menuItem);
		menuItem = new JMenuItem("Dataset", 
							im.getIcon(IconManager.CREATE_DATASET));
		control.attachItemListener(menuItem, DataManagerCtrl.DATASET_ITEM);
		newMenu.add(menuItem);
		return newMenu;
	}
	
	/** Creates the <code>importMenu</code>. */
	/*
	private JMenu createImportMenu()
	{
		JMenu menu = new JMenu("Import");
		JMenuItem menuItem = new JMenuItem("Image", 
								im.getIcon(IconManager.IMPORT_IMAGE));
		control.attachItemListener(menuItem, DataManagerCtrl.IMAGE_ITEM);
		menu.add(menuItem);
		return menu;
	}
	*/
}
