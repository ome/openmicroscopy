/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerWin
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.NewObjectAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.finder.AdvancedFinder;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.ui.ActivityComponent;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.JXTaskPaneContainerSingle;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

/**
 * The {@link TreeViewer}'s View. Embeds the different <code>Browser</code>'s UI
 * to display the various visualization trees. Also provides a menu bar
 * and a working pane. After creation this window will display an empty panel as
 * a place holder for the working pane UI.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk </a>
 * @version 2.2 <small>( <b>Internal version: </b> $Revision$ $Date:
 *          2006/01/05 16:05:46 $) </small>
 * @since OME2.2
 */
class TreeViewerWin
	extends TopWindow
{

	/** Identifies the <code>JXTaskPane</code> layout. */
	static final String			JXTASKPANE_TYPE = "JXTaskPane";

	/** Indicates how much to give to the Metadata View. */
	private static final double WEIGHT = 0.8;
	
    /** The default title of the window. */
    private static final String TITLE = "Data Manager";
    
    /** The Controller. */
    private TreeViewerControl	controller;

    /** The Model. */
    private TreeViewerModel 	model;

    /**
     * The split pane hosting the {@link #workingPane} and the
     * {@link #tabs}.
     */
    private JSplitPane			splitPane;
    
    /** The component hosting the working pane. */
    private JScrollPane 		workingPane;
    
    /** The component hosting the working pane. */
    private JSplitPane 			rightPane;

    /** The component displaying the browser. */
    private JComponent			browsersDisplay;
    
    /** 
     * Collections of menu items to update when a node is selected
     * or when a tab pane is selected.
     */
    private List<JMenuItem>		menuItems;
    
    /** The tool bar hosting the controls displaying by the pop-up menu. */
    private ToolBar             toolBar;
    
    /** The status bar. */
    private StatusBar           statusBar;
    
    /** The bounds of the component invoking the component. */
    private Rectangle			invokerBounds;
    
    /** Constants indicating the display mode. */
    private int					displayMode;
    
	/** The scrollPane hosting the advanced finder. */
	private JScrollPane			finderScrollPane;
	
	/** The component that has been removed. */
	private Component			leftComponent;
	
	/** The component that has been removed. */
	private Component			rightComponent;
	
	/** The location of the divider. */
	private int					dividerLocation;
	
	/** Flag indicating that the tree is visible or hidden. */
	private boolean				treeVisible;
	
	/** Flag indicating that the metadata view is visible or hidden. */
	private boolean				metadataVisible;
	
	/** The location of the right split pane. */
	private int					dividerRightLocation;

	/** The first selected pane. */
    private JXTaskPane 			firstPane;
    
    /** The component hosting the task panes. */
    private JXTaskPaneContainerSingle 	container;

    /** The pane displaying the viewer. */
    private JSplitPane			viewerPane;
    
    /** The listener to the split panes.*/
    private PropertyChangeListener listener;
    
    /**
     * Checks if the specified {@link Browser} is already visible.
     * 
     * @param browser The specified {@link Browser}.
     * @return <code>true</code> if visible, <code>false</code> otherwise.
     */
    private boolean isBrowserVisible(Browser browser)
    {
        Component[] comps = browsersDisplay.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i].equals(browser.getUI())) return true;
        }
        return false;
    }
    
    /** Creates the components hosting the browsers. */
    private void layoutBrowsers()
    {
    	Map<Integer, Browser> browsers = model.getBrowsers();
    	Browser browser;
    	if (getLayoutType().equals(JXTASKPANE_TYPE)) {
    		container = new JXTaskPaneContainerSingle();
    		container.addPropertyChangeListener(controller);
    		JXTaskPane pane;
    		switch (TreeViewerAgent.getDefaultHierarchy()) {
				case Browser.PROJECTS_EXPLORER:
					browser = browsers.get(Browser.PROJECTS_EXPLORER);
					pane = new TaskPaneBrowser(browser);
					firstPane = pane;
					container.add(pane);
					browser = browsers.get(Browser.SCREENS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));

					browser = browsers.get(Browser.FILES_EXPLORER);
					container.add(new TaskPaneBrowser(browser));

					browser = browsers.get(Browser.TAGS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));
					break;
				case Browser.SCREENS_EXPLORER:
					browser = browsers.get(Browser.SCREENS_EXPLORER);
					pane = new TaskPaneBrowser(browser);
					firstPane = pane;
					container.add(pane);
					browser = browsers.get(Browser.PROJECTS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));

					browser = browsers.get(Browser.FILES_EXPLORER);
					container.add(new TaskPaneBrowser(browser));

					browser = browsers.get(Browser.TAGS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));
					break;
				case Browser.TAGS_EXPLORER:
					browser = browsers.get(Browser.TAGS_EXPLORER);
					pane = new TaskPaneBrowser(browser);
					firstPane = pane;
					container.add(pane);
					browser = browsers.get(Browser.PROJECTS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));

					browser = browsers.get(Browser.SCREENS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));

					browser = browsers.get(Browser.FILES_EXPLORER);
					container.add(new TaskPaneBrowser(browser));
					break;
				case Browser.FILES_EXPLORER:
					browser = browsers.get(Browser.FILES_EXPLORER);
					pane = new TaskPaneBrowser(browser);
					firstPane = pane;
					container.add(pane);
					browser = browsers.get(Browser.PROJECTS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));

					browser = browsers.get(Browser.SCREENS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));
					browser = browsers.get(Browser.TAGS_EXPLORER);
					container.add(new TaskPaneBrowser(browser));
			}
    		
    		//browser = (Browser) browsers.get(Browser.FILE_SYSTEM_EXPLORER);
    		//container.add(new TaskPaneBrowser(browser));
             
            
            
            browser = browsers.get(Browser.IMAGES_EXPLORER);
            container.add(new TaskPaneBrowser(browser));
            if (model.isLeader() || model.isAdministrator()) {
            	browser = browsers.get(Browser.ADMIN_EXPLORER);
                container.add(new TaskPaneBrowser(browser));
            }
            AdvancedFinder finder = model.getAdvancedFinder();
    		finder.addPropertyChangeListener(controller);
    		container.add(new TaskPaneBrowser(new JScrollPane(finder)));
    		JScrollPane s = new JScrollPane(container);
    		s.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            browsersDisplay = s;
    		
    	} else {
    		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
    				JTabbedPane.WRAP_TAB_LAYOUT);
            tabs.setAlignmentX(LEFT_ALIGNMENT);
            Font font = (Font) TreeViewerAgent.getRegistry().lookup(
                    "/resources/fonts/Titles");
            tabs.setFont(font);
            tabs.setForeground(UIUtilities.STEELBLUE);
            
            browser = (Browser) browsers.get(Browser.PROJECTS_EXPLORER);
            if (browser.isDisplayed())
                tabs.addTab(browser.getTitle(), browser.getIcon(), 
                		browser.getUI());

            browser = (Browser) browsers.get(Browser.FILES_EXPLORER);
            if (browser.isDisplayed())
                tabs.addTab(browser.getTitle(), browser.getIcon(), 
                		browser.getUI());
            browser = (Browser) browsers.get(Browser.TAGS_EXPLORER);
            if (browser.isDisplayed())
                tabs.addTab(browser.getTitle(), browser.getIcon(), 
                		browser.getUI());
            browser = (Browser) browsers.get(Browser.IMAGES_EXPLORER);
            if (browser.isDisplayed())
                tabs.addTab(browser.getTitle(), browser.getIcon(), 
                		browser.getUI());
            browsersDisplay = tabs;
    	}
    }

    /**
     * Creates the menu bar.
     * 
     * @return The menu bar.
     */
    private JMenuBar createMenuBar()
    {
        TaskBar tb = TreeViewerAgent.getRegistry().getTaskBar();
        List<JMenu> menus = new ArrayList<JMenu>();
        menus.add(createFileMenu());
        menus.add(createEditMenu());
        if (!getLayoutType().equals(JXTASKPANE_TYPE)) 
        	menus.add(createViewMenu());
       
        JMenuBar bar = tb.getTaskBarMenuBar();
        JMenu[] existingMenus = new JMenu[bar.getMenuCount()];
        
		for (int i = 0; i < existingMenus.length; i++) {
			existingMenus[i] = bar.getMenu(i);
		}
		bar.removeAll();
		
		Iterator<JMenu> k = menus.iterator();
		while (k.hasNext()) 
			bar.add(k.next());
			
		for (int i = 0; i < existingMenus.length; i++) {
			bar.add(existingMenus[i]);
		}
        return bar;
    }
    
    /**
     * Creates the <code>New</code> menu.
     * 
     * @return See above.
     */
    private JMenu createNewMenu()
    {
    	JMenu menu = new JMenu(NewObjectAction.NAME);
    	TreeViewerAction a = controller.getAction(
    			TreeViewerControl.CREATE_TOP_PROJECT);
    	JMenuItem item = new JMenuItem(a);
    	menu.add(item);
    	item.setText(a.getActionName());

    	a = controller.getAction(TreeViewerControl.CREATE_TOP_DATASET);
    	item = new JMenuItem(a);
    	item.setText(a.getActionName());
    	menu.add(item);
    	menu.add(new JSeparator());
    	a = controller.getAction(TreeViewerControl.CREATE_TOP_SCREEN);
    	item = new JMenuItem(a);
    	item.setText(a.getActionName());
    	menu.add(item);
    	menu.add(new JSeparator());
    	a = controller.getAction(TreeViewerControl.CREATE_TOP_TAG_SET);
    	item = new JMenuItem(a);
    	item.setText(a.getActionName());
    	menu.add(item);
    	a = controller.getAction(TreeViewerControl.CREATE_TOP_TAG);
    	item = new JMenuItem(a);
    	item.setText(a.getActionName());
    	menu.add(item);
    	return menu;
    }
    
    /**
     * Helper method to create the Views menu.
     * 
     * @return The Views menu.
     */
    private JMenu createViewMenu()
    {
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        JCheckBoxMenuItem item = new JCheckBoxMenuItem();
        Map<Integer, Browser> browsers = model.getBrowsers();
        Browser browser = browsers.get(Browser.PROJECTS_EXPLORER);
        item.setSelected(browser.isDisplayed());
        item.setAction(
                controller.getAction(TreeViewerControl.HIERARCHY_EXPLORER));
        menu.add(item);
        item = new JCheckBoxMenuItem();
        browser = browsers.get(Browser.FILES_EXPLORER);
        item.setSelected(browser.isDisplayed());
        item.setAction(controller.getAction(TreeViewerControl.FILES_EXPLORER));
        menu.add(item);
        item = new JCheckBoxMenuItem();
        browser = browsers.get(Browser.TAGS_EXPLORER);
        item.setSelected(browser.isDisplayed());
        item.setAction(controller.getAction(TreeViewerControl.TAGS_EXPLORER));
        menu.add(item);
        item = new JCheckBoxMenuItem();
        browser = browsers.get(Browser.IMAGES_EXPLORER);
        item.setSelected(browser.isDisplayed());
        item.setAction(controller.getAction(TreeViewerControl.IMAGES_EXPLORER));
        menu.add(item);
        return menu;
    }
    
    /**
     * Helper method to create the <code>File</code> menu.
     * 
     * @return See above.
     */
    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(createNewMenu());
        TreeViewerAction a = controller.getAction(
        		TreeViewerControl.SWITCH_USER);
        JMenuItem item = new JMenuItem(a);
        //menu.add(item);
        item.setText(a.getActionName());
        //menu.add(createRootMenu());
        a = controller.getAction(TreeViewerControl.EDITOR_NO_SELECTION);
        item = new JMenuItem(a);
        menu.add(item);
        item.setText(a.getActionName());
        a = controller.getAction(TreeViewerControl.IMPORT_NO_SELECTION);
        item = new JMenuItem(a);
        menu.add(item);
        item.setText(a.getActionName());
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        a = controller.getAction(TreeViewerControl.BROWSE);
        item = new JMenuItem(a);
        item.setText(a.getActionName());
        menuItems.add(item);
        menu.add(item);
        a = controller.getAction(TreeViewerControl.VIEW);
        item = new JMenuItem(a);
        item.setText(a.getActionName());
        menuItems.add(item);
        menu.add(item);
        a = controller.getAction(TreeViewerControl.REFRESH_TREE);
        item = new JMenuItem(a);
        item.setText(a.getActionName());
        menu.add(item);
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.LOG_OFF)));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.EXIT)));
        return menu;
    }
    
    /**
     * Helper method to create the <code>Edit</code> menu.
     * 
     * @return See above.
     */
    private JMenu createEditMenu()
    {
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CUT_OBJECT)));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.COPY_OBJECT)));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.PASTE_OBJECT)));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.DELETE_OBJECT)));
        return menu;
    }

    /** Initializes the UI components. */
    private void initComponents()
    {
        layoutBrowsers();
        workingPane = new JScrollPane();
        workingPane.setBackground(UIUtilities.BACKGROUND_COLOR);
        workingPane.getViewport().setBackground(UIUtilities.BACKGROUND_COLOR);
        viewerPane = new JSplitPane();
        viewerPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        viewerPane.setOneTouchExpandable(true);
        viewerPane.setContinuousLayout(true);
        viewerPane.setBackground(UIUtilities.BACKGROUND_COLOR);
        //viewerPane.setResizeWeight(1.0);
        listener = new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(name)) {
					if (evt.getSource() == rightPane && metadataVisible)
						dividerRightLocation = rightPane.getDividerLocation();
					handleDividerMoved();
				}
			}
		};
    }

    /** Handles the change of location of the divider of the split panes.*/
    private void handleDividerMoved()
    {
		DataBrowser db = model.getDataViewer();
		if (db != null) {
			JViewport viewPort = workingPane.getViewport();
			JComponent component = db.getBrowser().getUI();
			component.setPreferredSize(viewPort.getExtentSize());
			component.setSize(viewPort.getExtentSize());
			component.validate();
			component.repaint();
			db.layoutDisplay();
		}
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	treeVisible = true;
    	metadataVisible = true;
    	rightPane = new JSplitPane();
    	rightPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    	rightPane.setOneTouchExpandable(true);
    	rightPane.setContinuousLayout(true);
    	rightPane.setBackground(UIUtilities.BACKGROUND_COLOR);
    	rightPane.setLeftComponent(workingPane);
    	rightPane.setRightComponent(null);
    	rightPane.setResizeWeight(WEIGHT);
    	
    	splitPane = new JSplitPane();
        //splitPane.setResizeWeight(1);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setLeftComponent(browsersDisplay);
        //splitPane.setRightComponent(workingPane);
        splitPane.setRightComponent(rightPane);
        splitPane.setResizeWeight(0);
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(toolBar, BorderLayout.NORTH);
        c.add(splitPane, BorderLayout.CENTER);
        c.add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance. The
     * {@link #initialize(TreeViewerControl, TreeViewerModel) initialize}
     * method should be called straight after to link this View to the
     * Controller.
     */
    TreeViewerWin()
    {
        super(TITLE);
        menuItems = new ArrayList<JMenuItem>();
    }

    /**
     * Links this View to its Controller.
     * 
     * @param controller	The Controller.
     * @param model 		The Model.
     * @param bounds		The bounds of the component invoking a new 
     * 						{@link TreeViewer}.
     */
    void initialize(TreeViewerControl controller, TreeViewerModel model, 
    						Rectangle bounds)
    {
    	this.controller = controller;
    	invokerBounds = bounds;
    	this.model = model;
    	displayMode = TreeViewer.EXPLORER_MODE;
    	statusBar = new StatusBar(controller);
    	statusBar.addPropertyChangeListener(controller);
    	toolBar = new ToolBar(controller, model, this);
    	initComponents();
    	setJMenuBar(createMenuBar());
    	buildGUI();
    	controller.attachUIListeners(browsersDisplay);
    	createTitle();
    }

    /** Creates and displays the title of the window. */
    void createTitle()
    {
    	 String title = model.getExperimenterNames()+"'s ";
         setTitle(title+TITLE);
    }
    
    /** Expands the first pane. */
    void selectFirstPane()
    { 
    	if (TreeViewerWin.JXTASKPANE_TYPE.equals(getLayoutType())) {
    		if (firstPane != null) firstPane.setCollapsed(false);
        	if (!UIUtilities.isLinuxOS()) {
        		List<JXTaskPane> list = container.getTaskPanes();
        		for (JXTaskPane pane: list) 
            		pane.setAnimated(true);
        	}
    	}
    }
    
    /** Adds the metadata editor. */
    void initializeDisplay()
    {
    	if (rightComponent == null) {
			rightComponent = new JScrollPane(
					model.getMetadataViewer().getEditorUI());
    	}
    	if (metadataVisible) {
    		Component[] components = rightPane.getComponents();
    		boolean b = false;
    		for (int i = 0; i < components.length; i++) {
				if (components[i] == rightComponent) {
					b = true;
				}
			}
    		if (!b) rightPane.setRightComponent(rightComponent);
    	}
    }
    
    /**
     * Selects the pane corresponding to the passed index.
     * 
     * @param browserType The type of browser hosted by the pane.
     */
    void selectPane(int browserType)
    {
    	if (TreeViewerWin.JXTASKPANE_TYPE.equals(getLayoutType())) {
    		List<JXTaskPane> list = container.getTaskPanes();
    		TaskPaneBrowser p;
    		Browser b;
    		container.removePropertyChangeListener(controller);
    		for (JXTaskPane pane: list)  {
    			if (pane instanceof TaskPaneBrowser) {
    				p = (TaskPaneBrowser) pane;
    				b = p.getBrowser();
    				if (b != null && b.getBrowserType() == browserType) {
    					p.setCollapsed(false);
    				}
    			}
    		}
    		container.addPropertyChangeListener(controller);
    	} else {
    		
    	}
    }
    
    /** 
     * Returns the type of layout of the browser.
     * 
     * @return See above.
     */
    String getLayoutType()
    {
    	String type = (String) 
		TreeViewerAgent.getRegistry().lookup("BrowserLayout");
    	if (type == null) type = "";
		return type;
    }
    
    /** Closes and disposes of the window. */
    void closeViewer()
    {
        setVisible(false);
        dispose();
    }

    /**
     * Adds the {@link Browser} corresponding the specified type to the display.
     * If the {@link Browser} is already visible, nothing happened.
     * 
     * @param browser The browser to add.
     */
    void addBrowser(Browser browser)
    {
    	if (browsersDisplay instanceof JTabbedPane) {
    		JTabbedPane tabs = (JTabbedPane) browsersDisplay;
    		 if (!(isBrowserVisible(browser)))
    	            tabs.addTab(browser.getTitle(), browser.getIcon(), 
    	            		browser.getUI());
    	        tabs.removeChangeListener(controller.getTabbedListener());
    	        tabs.setSelectedComponent(browser.getUI());
    	        tabs.addChangeListener(controller.getTabbedListener());
    	}
    }

    /**
     * Removes the specified {@link Browser} from the display.
     * 
     * @param browser The {@link Browser} to remove.
     */
    void removeBrowser(Browser browser)
    {
    	if (browsersDisplay instanceof JTabbedPane) {
    		JTabbedPane tabs = (JTabbedPane) browsersDisplay;
    		if (isBrowserVisible(browser)) {
                tabs.remove(browser.getUI());
                Component c = tabs.getSelectedComponent();
                if (c == null) {
                    model.setSelectedBrowser(null);
                    return;
                }
                Map<Integer, Browser> browsers = model.getBrowsers();
                Iterator<Browser> i = browsers.values().iterator();
                boolean selected = false;
                while (i.hasNext()) {
                    browser = i.next();
                    if (c.equals(browser.getUI())) {
                        model.setSelectedBrowser(browser);
                        selected = true;
                        break;
                    }
                }
                if (!selected)  model.setSelectedBrowser(null);
            }
    	}
    }
    
    /**
     * Brings up the pop-up menu on top of the specified component at the
     * specified point.
     * 
     * @param index The index of the menu. One of the following constants:
     * 				{@link TreeViewer#FULL_POP_UP_MENU} or 
     * 				{@link TreeViewer#PARTIAL_POP_UP_MENU}
     * @param c 	The component that requested the pop-up menu.
     * @param p 	The point at which to display the menu, relative to the
     *            	<code>component</code>'s coordinates.
     *  
     */
    void showPopup(int index, Component c, Point p)
    { 
    	if (c == null) return;
    	if (p == null) p = new Point(0, 0);
    	switch (index) {
			case TreeViewer.FULL_POP_UP_MENU:
			case TreeViewer.PARTIAL_POP_UP_MENU:
			case TreeViewer.ADMIN_MENU:
				PopupMenu popupMenu = new PopupMenu(controller, index);
		        popupMenu.show(c, p.x, p.y);	
		}
    }

    /**
     * Adds the specified component to the {@link #workingPane}.
     * 
     * @param component The component to add.
     */
    void addComponent(JComponent component)
    {
    	if (component == null) return;
    	rightPane.removePropertyChangeListener(listener);
    	splitPane.removePropertyChangeListener(listener);
        JViewport viewPort = workingPane.getViewport();
        component.setPreferredSize(viewPort.getExtentSize());
        viewPort.removeAll();
        viewPort.add(component);
        viewPort.validate();
        rightPane.addPropertyChangeListener(listener);
        splitPane.addPropertyChangeListener(listener);
    }

	/**
	 * Displays the passed viewer in the working area.
	 * 
	 * @param viewer The viewer to display.
	 * @param controls Reference to the controls.
	 * @param toAdd  Pass <code>true</code> to add the component, 
	 * 				 <code>false</code> otherwise. 
	 * @param toDetach 	Pass <code>true</code> to detach the viewer, 
	 * 					<code>false</code> otherwise.
	 */
    void displayViewer(JComponent viewer, JComponent controls, boolean toAdd,
    		boolean toDetach)
	{
    	JViewport viewPort = workingPane.getViewport();
    	viewPort.removeAll();
    	viewerPane.removeAll();
    	if (toAdd) {
    		if (model.isFullScreen()) return;
    		addComponent(viewer);
        	DataBrowser db = model.getDataViewer();
        	int location  = splitPane.getDividerLocation();
        	splitPane.removeAll();
        	splitPane.setLeftComponent(browsersDisplay);
        	if (db != null) {
        		viewerPane.setTopComponent(viewer);
            	viewerPane.setBottomComponent(db.getUI(false));
            	viewerPane.setResizeWeight(WEIGHT);
            	splitPane.setRightComponent(viewerPane);
        	} else splitPane.setRightComponent(rightPane);
        	splitPane.setDividerLocation(location);
    	} else {
    		/*
    		if (toDetach) {
    			//open the viewer in a separate view.
    			DataBrowser db = model.getDataViewer();
    			if (db != null)
    				viewerPane.setBottomComponent(db.getUI(false));
            	viewerPane.setResizeWeight(WEIGHT);
    		} else {
    			model.setFullScreen(false);
    			toolBar.setFullScreenSelected(true);
    			int location  = splitPane.getDividerLocation();
            	splitPane.removeAll();
            	splitPane.setLeftComponent(browsersDisplay);
            	splitPane.setRightComponent(rightPane);
            	splitPane.setDividerLocation(location);
    		}
    		*/
    	}
	}
    
	/** 
	 * Displays the data browser.
	 * 
	 * @param db The data browser.
	 */
	void displayBrowser(DataBrowser db)
	{
		if (db == null) return;
		if (model.isFullScreen()) {
			addComponent(db.getUI(model.isFullScreen()));
		} else {
			viewerPane.removeAll();
			viewerPane.setBottomComponent(db.getUI(false));
		}
	}
	
    /** Removes all the components from the {@link #workingPane}. */
    void removeAllFromWorkingPane()
    {
        JViewport viewPort = workingPane.getViewport();
        viewPort.removeAll();
        viewPort.validate();
        viewPort.repaint();
    }
    
    /**
     * Shows or hides the component depending on the stated of the frame.
     * 
     * @param b Passed <code>true</code> to show the component, 
     * 			<code>false</code> otherwise.
     */
    void showFinder(boolean b)
    {
        JSplitPane pane = null;
        if (b) { //finder visible.
            pane = new JSplitPane();
            pane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            pane.setResizeWeight(1);
            pane.setOneTouchExpandable(true);
            pane.setContinuousLayout(true);
            pane.setTopComponent(splitPane);
            pane.setBottomComponent(model.getFinder());
        } else pane = splitPane;
        Container c = getContentPane();
        c.removeAll();
        c.add(toolBar, BorderLayout.NORTH);
        c.add(pane, BorderLayout.CENTER);
        c.add(statusBar, BorderLayout.SOUTH);
        c.validate();
    }
  
    /** Displays or hides the search component. */
    void showAdvancedFinder()
    {
    	if (!getLayoutType().equals(JXTASKPANE_TYPE)) {
    		if (displayMode == TreeViewer.SEARCH_MODE)
        		displayMode = TreeViewer.EXPLORER_MODE;
        	else if (displayMode == TreeViewer.EXPLORER_MODE)
        		displayMode = TreeViewer.SEARCH_MODE;
        	splitPane.setDividerLocation(splitPane.getDividerLocation());
        	if (finderScrollPane == null) {
        		AdvancedFinder finder = model.getAdvancedFinder();
        		finder.addPropertyChangeListener(controller);
        		finderScrollPane = new JScrollPane(finder);
        	}
        	switch (displayMode) {
    			case TreeViewer.SEARCH_MODE:
    				splitPane.remove(browsersDisplay);
    	    		splitPane.setLeftComponent(finderScrollPane);
    				break;
    			case TreeViewer.EXPLORER_MODE:
    				splitPane.remove(finderScrollPane);
    	    		splitPane.setLeftComponent(browsersDisplay);
    				break;
    		}
    	} else {
    		if (displayMode == TreeViewer.SEARCH_MODE)
        		displayMode = TreeViewer.EXPLORER_MODE;
        	else if (displayMode == TreeViewer.EXPLORER_MODE)
        		displayMode = TreeViewer.SEARCH_MODE;
    	}
    }
    
    /**
     * Returns the {@link #displayMode}.
     * 
     * @return See above.
     */
    int getDisplayMode() { return displayMode; }
    
    /**
     * Brings up the menu on top of the specified component at 
     * the specified location.
     * 
     * @param menuID    The id of the menu.
     * @param c         The component that requested the pop-up menu.
     * @param p         The point at which to display the menu, relative to the
     *                  <code>component</code>'s coordinates.
     */
    void showMenu(int menuID, Component c, Point p)
    {
        switch (menuID) {
            case TreeViewer.MANAGER_MENU:
                toolBar.showManagementMenu(c, p);
                break;
            case TreeViewer.CREATE_MENU_CONTAINERS:
            case TreeViewer.CREATE_MENU_SCREENS:
            case TreeViewer.CREATE_MENU_TAGS:
            case TreeViewer.CREATE_MENU_ADMIN:
                toolBar.showCreateMenu(c, p, menuID);
                break;
            case TreeViewer.PERSONAL_MENU:
            	toolBar.showPersonalMenu(c, p);
            	break;
            case TreeViewer.AVAILABLE_SCRIPTS_MENU:
            	toolBar.showAvailableScriptsMenu(c, p);
        }  
    }
    
    /**
     * Enables the tab pane depending on the specified parameter.
     * 
     * @param b Pass <code>true</code> to enable the tab pane,
     *          <code>false</code> otherwise.
     */
    void onStateChanged(boolean b)
    { 
    	Map<Integer, Browser> browsers = model.getBrowsers();
        if (browsers != null) {
        	Entry entry;
            Iterator i = browsers.entrySet().iterator();
            while (i.hasNext()) {
            	entry = (Entry) i.next();
            	((Browser) entry.getValue()).onComponentStateChange(b);
            }    

        }
        //if (browser != null) browser.onComponentStateChange(b);
        if (container != null) container.setExpandable(b);
        browsersDisplay.setEnabled(b);
    }
    
    /** 
     * Sets the status message.
     * 
     * @param text  The message to display.
     * @param hide  Pass <code>true</code> to hide the progress bar, 
     *              <code>false</otherwise>.
     */
    void setStatus(String text, boolean hide)
    {
        statusBar.setStatus(text);
        statusBar.setProgress(hide);
    }
    
    /**
     * Sets the icon depending on the passed flag.
     * 
     * @param b Pass <code>true</code> when loading data, <code>false</code>
     *          otherwise.
     */
    void setStatusIcon(boolean b)
    {
        if (b) setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        else setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        statusBar.setStatusIcon(b); 
    }
    
    /** 
     * Updates the text of the menu items when a node is selected
     * or when a tab pane is selected.
     */
    void updateMenuItems()
    {
    	Iterator i = menuItems.iterator();
    	JMenuItem item;
    	TreeViewerAction a;
    	while (i.hasNext()) {
    		item = (JMenuItem) i.next();
    		a = (TreeViewerAction) item.getAction();
			item.setText(a.getActionName());
			item.setToolTipText(a.getActionDescription());
		}
    }
    
    /** Shows or hides the Tree Viewer. */
	void setInspectorVisibility()
	{
		if (treeVisible) {
			if (leftComponent == null)
				leftComponent = splitPane.getLeftComponent();
			dividerLocation = splitPane.getDividerLocation();
			Component[] components = splitPane.getComponents();
			if (components != null) {
				boolean b = false;
				for (int i = 0; i < components.length; i++) {
					if (components[i] == leftComponent)
						b = true;
				}
				if (b && leftComponent != null)
					splitPane.remove(leftComponent);
			}
		} else {
			if (leftComponent != null) {
				splitPane.add(leftComponent);
				splitPane.setDividerLocation(dividerLocation);
			}
		}
		treeVisible = !treeVisible;
	}
	
    /** Shows or hides the Tree Viewer. */
	void setMetadataVisibility()
	{
		if (rightComponent == null) {
			rightComponent = new JScrollPane(
					model.getMetadataViewer().getEditorUI());
    	}
			
		if (metadataVisible) {
			Component[] components = rightPane.getComponents();
			if (components != null && components.length > 0) {
				boolean b = false;
				for (int i = 0; i < components.length; i++) {
					if (components[i] == rightComponent)
						b = true;
				}
				if (b) {
					rightPane.remove(rightComponent);
				}
			}
		} else {
			if (rightComponent != null) {
				rightPane.add(rightComponent);
				if (dividerRightLocation > 0) 
					rightPane.setDividerLocation(dividerRightLocation);
				else rightPane.setResizeWeight(WEIGHT);
			}
		}
		metadataVisible = !metadataVisible;
	}
	
	/**
	 * Reloads the specified thumbnails.
	 * 
	 * @param ids The collection of images' ids to reload.
	 */
	void reloadThumbnails(List<Long> ids)
	{
		model.reloadThumbnails(ids);
	}
	
	/**
	 * Indicates that an activity has just terminated.
	 * 
	 * @param activity The activity to handle.
	 */
	void onActivityTerminated(ActivityComponent activity)
	{
		TinyDialog d = new TinyDialog(this, activity.getActivityType(), 
				TinyDialog.CLOSE_ONLY);
		d.getContentPane().setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		d.addPropertyChangeListener(TinyDialog.CLOSED_PROPERTY, 
				controller);
		d.pack();
		Point p = new Point(0, 2*statusBar.getPreferredSize().height);
		setCloseAfter(true);
		showJDialogAsSheet(d, p, UP_RIGHT);
	}
	
	/** Refreshes the renderer. */
	void refreshRenderer() { model.refreshRenderer(); }
	
	/** 
	 * Returns the MIME type 
	 * 
	 * @return See above.
	 */
	String getObjectMimeType() { return model.getObjectMimeType(); }
	
	/**
	 * Returns the script corresponding to the specified name.
	 * 
	 * @param value The name of the script.
	 * @return See above
	 */
	ScriptObject getScriptFromName(String name)
	{
		return model.getScriptFromName(name);
	}
	
	/** 
	 * Invokes when loadings scripts.
	 * 
	 * @param loading Passes <code>true</code> if there is an on-going loading.
	 *                <code>false</code> otherwise.
	 */
	void setScriptsLoadingStatus(boolean loading)
	{
		toolBar.setScriptsLoadingStatus(loading);
	}
	
    /** Overrides the {@link #setOnScreen() setOnScreen} method. */
    public void setOnScreen()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(9*(screenSize.width/10), 8*(screenSize.height/10));
        UIUtilities.incrementRelativeToAndShow(invokerBounds, this);
        invokerBounds = null;
    }



}
