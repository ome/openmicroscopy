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
import java.awt.Rectangle;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.editors.CreateDatasetEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.CreateProjectEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.DatasetEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.ImageEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.ProjectEditor;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
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
	/** Width of the widget. */
	private static final int 				WIN_WIDTH = 350;
	
	/** Height of the widget. */
	private static final int 				WIN_HEIGHT = 350;
	
	/** Location x-coordinate. */
	private static final int				X_LOCATION = 0;
	
	/** Location y-coordinate. */
	private static final int				Y_LOCATION = 0;
	
	/** 
	 * UI component to view a summary of the user's data
	 * and to mark the currently viewed image. 
	 */
	private ExplorerPane					explPane;
	
	/** Reference to the regisry. */
	private Registry						registry;
	
	/** Reference to the control component. */
	private DataManagerCtrl					control;
	
	private JMenu							newMenu;
	
	/** On-request menu displayed for nodes in the tree. */
	private TreePopupMenu					popupMenu;
	
			
	DataManagerUIF(DataManagerCtrl control, Registry registry)
	{
		//name, resizable, closable, maximizable, iconifiable.
		super("DataManager", true, true, true, true);
		this.registry = registry;
		this.control = control;
		explPane = new ExplorerPane(control, registry);
		popupMenu = new TreePopupMenu(control, registry);
		setJMenuBar(createMenuBar());
		buildGUI();
		//set the size and position the window.
		setBounds(X_LOCATION, Y_LOCATION, WIN_WIDTH, WIN_HEIGHT);	
	}

	/**
	 * Forward event to {@link ExplorerPaneManager}.
	 */
	void updateProjectInTree()
	{
		explPane.getManager().updateProjectInTree();
	}

	/**
	 * Forward event to {@link ExplorerPaneManager}.
	 */
	void updateDatasetInTree()
	{
		explPane.getManager().updateDatasetInTree();
	}
	
	/**
	 * Forward event to {@link ExplorerPaneManager}.
	 */
	void updateImageInTree(ImageData id)
	{
		explPane.getManager().updateImageInTree(id);
	}
	
	/**
	 * Forward event to {@link ExplorerPaneManager}.
	 */
	void addNewProjectToTree(ProjectSummary ps)
	{
		explPane.getManager().addNewProjectToTree(ps);
	}
	
	/**
	 * Forward event to {@link ExplorerPaneManager}.
	 */
	void addNewDatasetToTree(List projects)
	{
		explPane.getManager().addNewDatasetToTree(projects);
	}
	
	/**
	 * Returns the menu displayed for nodes in the tree.
	 * 
	 * @return See above.
	 */
	TreePopupMenu getPopupMenu()
	{
		return popupMenu;
	}
    
	/** 
	 * Brings up the property sheet dialog for the specified project.
	 *
	 * @param   p   The project whose properties will be displayed by the 
	 * 				property sheet dialog. Mustn't be <code>null</code>.
	 */
	void showProjectPS(ProjectData p)
	{
		ProjectEditor   ps = new ProjectEditor(registry, control, p);
		showPS((JDialog) ps);
	}
    
	/** 
	 * Brings up the property sheet dialog for the specified dataset.
	 *
	 * @param   d   The dataset whose properties will be displayed by the
	 * 				property sheet dialog. Mustn't be <code>null</code>.
	 */
	void showDatasetPS(DatasetData d)
	{
		DatasetEditor   ps = new DatasetEditor(registry, control, d);
		showPS((JDialog) ps);
	}
    
	/** 
	 * Brings up the property sheet dialog for the specified image.
	 *
	 * @param   i   The image whose properties will be displayed by the 
	 * 				property sheet dialog. Mustn't be <code>null</code>.
	 */
	void showImagePS(ImageData i) 
	{
		ImageEditor   ps = new ImageEditor(registry, control, i);
		showPS((JDialog) ps);
	}	
	
	/** 
	 * Brings up the createProject dialog for the specified image.
	 *
	 * @param p   	The project to create.
	 * 				Mustn't be <code>null</code>.
	 * @param d		List of available datasets.
	 */
	void showCreateProject(ProjectData p, List d)
	{
		CreateProjectEditor cpe = new CreateProjectEditor(registry, control,
														  p, d);
		showPS((JDialog) cpe);
	}
	
	/** 
	 * Brings up the createDataset dialog for the specified image.
	 *
	 * @param   d   The image whose properties will be displayed by the 
	 * 				property sheet dialog. Mustn't be <code>null</code>.
	 */
	void showCreateDataset(DatasetData d, List p, List i)
	{
		CreateDatasetEditor cde = new CreateDatasetEditor(registry, control,
														 d, p, i);
		showPS((JDialog) cde);
	}
	
	/** 
	 * Brings up the createImage dialog for the specified image.
	 *
	 * @param   i   The image whose properties will be displayed by the 
	 * 				property sheet dialog. Mustn't be <code>null</code>.
	 */
	void showCreateImage(ImageData i)
	{
		//CreateDatasetEditor cde = new CreateDatasetEditor(registry, d);
		//showPS((JDialog) cpe);
	}
	
	/** 
	 * Sizes, centers and brings up the specified editor dialog.
	 *
	 * @param   editor	The editor dialog.
	 */
	private void showPS(JDialog editor)
	{
		//editor.pack();
		JFrame topFrame = (JFrame) registry.getTopFrame().getFrame();
		Rectangle tfB = topFrame.getBounds(), psB = editor.getBounds();
		int offsetX = (tfB.width-psB.width)/2, 
			offsetY = (tfB.height-psB.height)/2;
		if (offsetX < 0)	offsetX = 0;
		if (offsetY < 0)	offsetY = 0;
		editor.setLocation(tfB.x+offsetX, tfB.y+offsetY);
		editor.setVisible(true);
	}

	/** 
	 * Build and lay out the GUI.
	 */
	private void buildGUI()
	{
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM, 
											JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		//TODO: specify lookup name.
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		IconManager im = IconManager.getInstance(registry);
		Icon icon = im.getIcon(IconManager.OME);
		
		//TODO: image not loaded						
		tabs.addTab("Explorer", icon, explPane);
		tabs.setFont(font);
		tabs.setForeground(DataManager.STEELBLUE);
		tabs.setSelectedComponent(explPane);
		
		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);
		
		setFrameIcon(icon);	
	} 	
	
	/** Creates an internal menu. */
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar(); 
		createNewMenu();
		menuBar.add(newMenu);
		
		return menuBar;
	}
	
	/** Creates the <code>newMenu</code>. */
	private void createNewMenu()
	{
		newMenu = new JMenu("New...");
		JMenuItem menuItem = new JMenuItem("Project");
		control.setMenuItemListener(menuItem, DataManagerCtrl.PROJECT_ITEM);
		newMenu.add(menuItem);
		menuItem = new JMenuItem("Dataset");
		control.setMenuItemListener(menuItem, DataManagerCtrl.DATASET_ITEM);
		newMenu.add(menuItem);
		menuItem = new JMenuItem("Image");
		control.setMenuItemListener(menuItem, DataManagerCtrl.IMAGE_ITEM);
		newMenu.add(menuItem);
	}
	
}
