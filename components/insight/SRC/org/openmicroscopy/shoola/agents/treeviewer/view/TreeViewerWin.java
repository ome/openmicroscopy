/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerWin
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.MoveToAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.NewObjectAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.finder.AdvancedFinder;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.ui.ActivityComponent;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.JXTaskPaneContainerSingle;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

import com.google.common.collect.Lists;

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

	/** The text of the <code>View</code> menu.*/
	static final String			VIEW_MENU = "View";
	
	/** The text of the <code>Edit</code> menu.*/
	static final String EDIT_MENU = "Edit";
	
	/** The text of the <code>Edit</code> menu.*/
	static final String CREATE_NEW_MENU = "Create New";
	
	/** The text of the <code>Edit</code> menu.*/
	static final String RENDERING_SETTINGS_MENU = "Rendering Settings";

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
    
    /** The first selected pane. */
    private JXTaskPane 			searchPane;
    
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
		container = new JXTaskPaneContainerSingle();
		container.addPropertyChangeListener(controller);
		JXTaskPane pane;
        final List<String>  browserNames = Lists.newArrayList("project", "screen", "file", "tag");
        final List<Integer> browserOrder = Lists.newArrayList(
                Browser.PROJECTS_EXPLORER, Browser.SCREENS_EXPLORER, Browser.FILES_EXPLORER, Browser.TAGS_EXPLORER);
        int browserIndex = browserOrder.indexOf(TreeViewerAgent.getDefaultHierarchy());
        switch (Integer.signum(browserIndex)) {
        case 1:
            browserNames.add(0, browserNames.remove(browserIndex));
            browserOrder.add(0, browserOrder.remove(browserIndex));
            /* intentional fall-through */
        case 0:
            for (browserIndex = 0; browserIndex < browserOrder.size(); browserIndex++) {
                pane = new TaskPaneBrowser(browsers.get(browserOrder.get(browserIndex)), browserNames.get(browserIndex));
                if (browserIndex == 0) {
                    firstPane = pane;
                }
                container.add(pane);
            }
        }
        
        browser = browsers.get(Browser.IMAGES_EXPLORER);
        container.add(new TaskPaneBrowser(browser, "image"));
        if (model.isLeader() || model.isAdministrator()) {
            browser = browsers.get(Browser.ADMIN_EXPLORER);
            final TaskPaneBrowser tpb = new TaskPaneBrowser(browser, "administration");
            container.add(tpb);
        }
        AdvancedFinder finder = model.getAdvancedFinder();
		finder.addPropertyChangeListener(controller);
		searchPane = new TaskPaneBrowser(new JScrollPane(finder), "search");
		container.add(searchPane);
		browsersDisplay = container;
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
        JMenuBar bar = tb.getTaskBarMenuBar();
        bar.setName("menu bar");
        List<JMenu> existingMenus = new ArrayList<JMenu>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
        	if (i != TaskBar.FILE_MENU)
        		existingMenus.add(bar.getMenu(i));
        }
        bar.removeAll();

        Iterator<JMenu> k = menus.iterator();
        while (k.hasNext()) 
        	bar.add(k.next());

        k = existingMenus.iterator();
        while (k.hasNext()) 
        	bar.add(k.next());
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
     * Helper method to create the <code>File</code> menu.
     * 
     * @return See above.
     */
    private JMenu createFileMenu()
    {
    	TaskBar tb = TreeViewerAgent.getRegistry().getTaskBar();
    	JMenu menu = tb.getMenu(TaskBar.FILE_MENU);
    	Component[] comps = menu.getPopupMenu().getComponents();
    	menu.removeAll();
        menu.add(createNewMenu());
        if (comps != null) {
        	for (int i = 0; i < comps.length; i++) {
        		menu.add(comps[i]);
			}
        }
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        TreeViewerAction a = controller.getAction(TreeViewerControl.BROWSE);
        JMenuItem item = new JMenuItem(a);
        item.setText(a.getActionName());
        menuItems.add(item);
        menu.add(item);
        JMenu viewMenu;
        switch (TreeViewerAgent.runAsPlugin()) {
			case LookupNames.IMAGE_J:
			case LookupNames.IMAGE_J_IMPORT:
				a = controller.getAction(TreeViewerControl.VIEW);
		        item = new JMenuItem(a);
		        item.setText(a.getActionName());
				viewMenu = new JMenu(TreeViewerWin.VIEW_MENU);
				viewMenu.setIcon(item.getIcon());
				viewMenu.add(item);
				menuItems.add(item);
				a = controller.getAction(TreeViewerControl.VIEW_IN_IJ);
		        item = new JMenuItem(a);
		        item.setText(a.getActionName());
		        viewMenu.add(item);
				menuItems.add(item);
				menu.add(viewMenu);
				break;
			case LookupNames.KNIME:
				a = controller.getAction(TreeViewerControl.VIEW);
		        item = new JMenuItem(a);
		        item.setText(a.getActionName());
				viewMenu = new JMenu(TreeViewerWin.VIEW_MENU);
				viewMenu.setIcon(item.getIcon());
				viewMenu.add(item);
				menuItems.add(item);
				a = controller.getAction(TreeViewerControl.VIEW_IN_KNIME);
		        item = new JMenuItem(a);
		        item.setText(a.getActionName());
		        viewMenu.add(item);
				menuItems.add(item);
				menu.add(viewMenu);
				break;
			default:
				a = controller.getAction(TreeViewerControl.VIEW);
		        item = new JMenuItem(a);
		        item.setText(a.getActionName());
		        menuItems.add(item);
		        menu.add(item);
		}
        
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
        JMenu move = createMoveToMenu();
        if (move != null)
        	menu.add(move);
        return menu;
    }
    
	/**
	 * Creates a menu if the various groups the data can be moved to.
	 * 
	 * @return See above.
	 */
	private JMenu createMoveToMenu()
	{
		List<MoveToAction> actions = controller.getMoveAction();
		if (actions == null || actions.size() <= 1) return null;
		JMenu menu = new JMenu(MoveToAction.NAME);
		Iterator<MoveToAction> i = actions.iterator();
		while (i.hasNext()) {
			menu.add(new JMenuItem(i.next()));
		}
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
		
		addComponentListener(new ComponentListener() {
            @Override
            public void componentShown(ComponentEvent e) {}
            
            @Override
            public void componentResized(ComponentEvent e) {
                container.reAdjustSizes();
            }
            
            @Override
            public void componentMoved(ComponentEvent e) { }
            
            @Override
            public void componentHidden(ComponentEvent e) {}
        });
    }

    /** Handles the change of location of the divider of the split panes.*/
    private void handleDividerMoved()
    {
		DataBrowser db = model.getDataViewer();
		JViewport viewPort = workingPane.getViewport();
		JComponent component;
		if (db != null) {
			component = db.getBrowser().getUI();
			component.setPreferredSize(viewPort.getExtentSize());
			component.setSize(viewPort.getExtentSize());
			component.validate();
			component.repaint();
			db.layoutDisplay();
		}
		MetadataViewer mv = model.getMetadataViewer();
		if (mv != null && metadataVisible) {
			component = mv.getEditorUI();
			Dimension d = rightPane.getSize();
			Dimension dd = viewPort.getExtentSize();
			Dimension nd = new Dimension(Math.abs(d.width-dd.width), d.height);
			component.setSize(nd);
			component.validate();
			component.repaint();
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
    	setName("tree viewer window");
    }

    /** Creates and displays the title of the window. */
    void createTitle()
    {
    	String title = model.getExperimenterNames()+" connected to ";
    	title += model.getHostname();
    	setTitle(title);
    }
    
    /** Expands the first pane. */
    void selectFirstPane()
    { 
		if (firstPane != null) 
		    firstPane.setCollapsed(false);
    }
    
    /** Adds the metadata editor. */
    void initializeDisplay()
    {
    	if (rightComponent == null) {
			rightComponent = model.getMetadataViewer().getEditorUI();
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
    }
    
    /**
     * Shows the search panel
     */
    void selectSearchPane() {
        searchPane.setCollapsed(false);
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
			case TreeViewer.VIEW_MENU:
				PopupMenu popupMenu = new PopupMenu(controller, model, index);
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

    /** Clears the menus. */
    void clearMenus() { toolBar.clearMenus(); }

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
		if (displayMode == TreeViewer.SEARCH_MODE)
    		displayMode = TreeViewer.EXPLORER_MODE;
    	else if (displayMode == TreeViewer.EXPLORER_MODE)
    		displayMode = TreeViewer.SEARCH_MODE;
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
            case TreeViewer.VIEW_MENU:
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
            for (final Browser browser : browsers.values()) {
                browser.onComponentStateChange(b);
            }
        }
        if (container != null) 
            container.setExpandable(b);
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
        if (b) 
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        else 
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        statusBar.setStatusIcon(b); 
    }
    
    /** 
     * Updates the text of the menu items when a node is selected
     * or when a tab pane is selected.
     */
    void updateMenuItems()
    {
    	Iterator<JMenuItem> i = menuItems.iterator();
    	JMenuItem item;
    	TreeViewerAction a;
    	while (i.hasNext()) {
    		item = i.next();
    		a = (TreeViewerAction) item.getAction();
			item.setText(a.getActionName());
			item.setToolTipText(a.getActionDescription());
		}
    	toolBar.setPermissions();
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
			rightComponent = model.getMetadataViewer().getEditorUI();
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
	 * Resets the metadata viewer.
	 * 
	 * @return See above.
	 */
	MetadataViewer resetMetadataViewer()
	{
		MetadataViewer v = model.resetMetadataViewer();
		if (rightComponent != null) rightPane.remove(rightComponent);
		rightComponent = v.getEditorUI();
		if (metadataVisible) {
			rightPane.add(rightComponent);
			if (dividerRightLocation > 0) 
				rightPane.setDividerLocation(dividerRightLocation);
			else rightPane.setResizeWeight(WEIGHT);
		}
		return v;
	}
	
	/**
	 * Makes sure that the metadata view is visible
	 */
	void forceShowMetaDataView() {
	    metadataVisible = true;
	    resetMetadataViewer();
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
	
	/** Indicates the group context.*/
	void setPermissions() { toolBar.setPermissions(); }
	
	/** 
	 * Resets the layout and returns the newly selected browser or 
	 * <code>null</code> if no new browser selected.
	 * 
	 * @return See above
	 */
	Browser resetLayout()
	{
		layoutBrowsers();
		splitPane.setLeftComponent(browsersDisplay);
		Browser result = null;
		
		Browser browser = model.getSelectedBrowser();
		Browser b;
		List<JXTaskPane> list = container.getTaskPanes();
		TaskPaneBrowser tpb;
		container.removePropertyChangeListener(controller);
		if (browser != null) {
			if (browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
    			if (TreeViewerAgent.isAdministrator()) {
    				for (JXTaskPane pane: list) {
    					if (pane instanceof TaskPaneBrowser) {
    						tpb = (TaskPaneBrowser) pane;
    						b = tpb.getBrowser();
    						if (b == browser) {
    							tpb.setCollapsed(false);
    						}
    					}
    				}
    			} else {
    				if (firstPane != null) {
    					result = ((TaskPaneBrowser) firstPane).getBrowser();
    					firstPane.setCollapsed(false);
    				}
    			}
    		} else {
    			for (JXTaskPane pane: list) {
					if (pane instanceof TaskPaneBrowser) {
						tpb = (TaskPaneBrowser) pane;
						b = tpb.getBrowser();
						if (b == browser) {
							tpb.setCollapsed(false);
						}
					}
				}
    		}
		} else { //that's the search.
			if (searchPane != null) 
			    searchPane.setCollapsed(false);
    	}
    	
		container.addPropertyChangeListener(controller);
		
		validate();
		repaint();
		return result;
	}
	

	/** Invokes when import is going on or finished.*/
	void onImport() { toolBar.onImport(); }

	
    /** Overrides the {@link #setOnScreen() setOnScreen} method. */
    @Override
    public void setOnScreen()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(9*(screenSize.width/10), 8*(screenSize.height/10));
        UIUtilities.incrementRelativeToAndShow(invokerBounds, this);
        invokerBounds = null;
    }

}
