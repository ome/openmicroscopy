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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.ui.TopWindow;

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
public class DataManagerUIF
	extends TopWindow
{
    
    public static final Color       STEELBLUE = new Color(0x4682B4);

    /** Width of the editor dialog window. */
    public static final int         EDITOR_WIDTH = 500;
    
    /** Height of the editor dialog window. */
    public static final int         EDITOR_HEIGHT = 500;
    
    /** Width of the "add" window. */
    public static final int         ADD_WIN_WIDTH = 400;
    
    /** Height of the "add" window. */
    public static final int         ADD_WIN_HEIGHT = 400;
        
    public static final Dimension   DIM_SCROLL_TABLE = new Dimension(40, 60);
    
    public static final Dimension   DIM_SCROLL_NAME = new Dimension(40, 25);
    
    public static final int         ROW_TABLE_HEIGHT = 60;
    
    public static final int         ROW_NAME_FIELD = 25;
    
    public static final int         SELECT_COLUMN_WIDTH = 15;
    
    public static final Dimension   HBOX = new Dimension(10, 0),
                                    VBOX = new Dimension(0, 10);
    public static final Dimension   VP_DIM = new Dimension(200, 70);
    
    private static final String     HIERARCHY = "Hierarchy", 
                                    CLASSIFIER = "Classifier";
    
    JMenu                                   hierarchyMenu, classifierMenu;
    
	/** 
	 * UI component to view a summary of the user's data
	 * and to mark the currently viewed image. 
	 */
	private ExplorerPane					explPane;
	
	/** List of image summary object. */
	private ImagesPane						imgPane;
	
    private ClassifierPane                  classifierPane;
    
	/** Reference to the regisry. */
	private Registry						registry;
	
	/** Reference to the control component. */
	private DataManagerCtrl					control;
	
	/** On-request menu displayed for nodes in the hierarchy tree. */
	private TreePopupMenu					popupMenu;
	
    /** On-request menu displayed for nodes in the classifier tree. */
    private ClassifierPopupMenu             classifierPopupMenu;
    
	private IconManager 					im;
	
	public DataManagerUIF(DataManagerCtrl control, Registry registry)
	{
		super("Data Manager", registry.getTaskBar());
		this.registry = registry;
		this.control = control;
		im = IconManager.getInstance(registry);
		explPane = new ExplorerPane(control, registry);
		popupMenu = new TreePopupMenu(control, registry);
        classifierPopupMenu = new ClassifierPopupMenu(control, registry);
		imgPane = new ImagesPane(control, registry);
        classifierPane = new ClassifierPane(control, registry);
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
	
    /** Forward event to {@link ExplorerPaneManager}. */
    void rebuildClassificationTree() { classifierPane.getManager().rebuildTree(); }
    
    /** Forward event to {@link ClassifierPaneManager}. */
    void addNewGroupToTree(CategoryGroupData data)
    {
        classifierPane.getManager().addNewGroupToTree(data);
    }
    
	/** Return the menu displayed for nodes in the tree. */
	TreePopupMenu getPopupMenu() { return popupMenu; }
    
    /** Return the menu displayed for nodes in the tree. */
    ClassifierPopupMenu getClassifierPopupMenu() { return classifierPopupMenu; }
	
	/**
	 * Specifies icons, text, and tooltips for the display buttons in the
	 * TaskBar.
	 * Those buttons are managed by the superclass, we only have to specify
	 * what they should look like.
	 */
	private void configureDisplayButtons()
	{
		configureQuickLaunchBtn(im.getIcon(IconManager.DMANAGER), 
												"Bring up the Data Manager.");
		configureWinMenuEntry("Data Manager", 
							im.getIcon(IconManager.DMANAGER));
	}

	/** Build and lay out the GUI. */
	private void buildGUI(ToolBar bar)
	{
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
											JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		//TODO: specify lookup name.
		Font font = (Font) registry.lookup("/resources/fonts/Titles");					
		tabs.addTab(HIERARCHY, im.getIcon(IconManager.EXPLORER), explPane);
		tabs.addTab("Images", im.getIcon(IconManager.IMAGE), imgPane);
        tabs.addTab(CLASSIFIER, im.getIcon(IconManager.EXPLORER), 
                        classifierPane);
		tabs.setFont(font);
		tabs.setForeground(STEELBLUE);
		tabs.setSelectedComponent(explPane);
		
		//Configure the display buttons in the TaskBar.
		configureDisplayButtons();
		
		//Set the menubar, the content pane's layout and add components.
		setJMenuBar(createMenuBar());

		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(bar, BorderLayout.NORTH);
		getContentPane().add(tabs, BorderLayout.CENTER);
	} 	
	
	/** Creates an internal menu. */
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar(); 
		menuBar.add(createHierarchyMenu());
        menuBar.add(createClassifierMenu());
		//menuBar.add(createImportMenu());
		return menuBar;
	}
	
	/** Creates the <code>newMenu</code>. */
	private JMenu createHierarchyMenu()
	{
		hierarchyMenu = new JMenu(HIERARCHY);
		JMenuItem menuItem = new JMenuItem("New Project", 
							im.getIcon(IconManager.CREATE_PROJECT));
		control.attachItemListener(menuItem, DataManagerCtrl.PROJECT_ITEM);
		hierarchyMenu.add(menuItem);
		menuItem = new JMenuItem("New Dataset", 
							im.getIcon(IconManager.CREATE_DATASET));
		control.attachItemListener(menuItem, DataManagerCtrl.DATASET_ITEM);
		hierarchyMenu.add(menuItem);
		return hierarchyMenu;
	}
	
    /** Creates the <code>newMenu</code>. */
    private JMenu createClassifierMenu()
    {
        classifierMenu = new JMenu(CLASSIFIER);
        JMenuItem menuItem = new JMenuItem("New Group and Category", 
                            im.getIcon(IconManager.CREATE_CG));
        control.attachItemListener(menuItem, DataManagerCtrl.CREATE_CG);
        classifierMenu.add(menuItem);
        return classifierMenu;
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
