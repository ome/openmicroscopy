/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerWin
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

package org.openmicroscopy.shoola.agents.treeviewer.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.GroupData;

/**
 * The {@link TreeViewer}'s View. Embeds the different <code>Browser</code>'s UI
 * to display the various visualization trees. Also provides a menu bar
 * and a working pane. After creation this window will display an empty panel as
 * a placeholder for the working pane UI.
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
    
    /** The default title of the window. */
    private static final String TITLE = "Data Manager";
    
    /** The location of the vertical split pane location. */
    private static final int 	DIVIDER_LOCATION = 300;

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

    /** The tabbed pane hosting the {@link Browser}s. */
    private JTabbedPane 		tabs;

    /** The popup menu. */
    //private PopupMenu 			popupMenu;

    /** The loading window. */
    private LoadingWindow 		loadingWin;

    /** The menu hosting the root levels. */
    private JMenu 				rootLevelMenu;
    
    /** The tool bar hosting the controls displaying by the popup menu. */
    private ToolBar             toolBar;
    
    /** The status bar. */
    private StatusBar           statusBar;
    
    /**
     * Checks if the specified {@link Browser} is already visible.
     * 
     * @param browser The specified {@link Browser}.
     * @return <code>true</code> if visible, <code>false</code> otherwise.
     */
    private boolean isBrowserVisible(Browser browser)
    {
        Component[] comps = tabs.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i].equals(browser.getUI())) return true;
        }
        return false;
    }

    /** Creates the {@link JTabbedPane} hosting the browsers. */
    private void createTabbedPane()
    {
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        Font font = (Font) TreeViewerAgent.getRegistry().lookup(
                "/resources/fonts/Titles");
        tabs.setFont(font);
        tabs.setForeground(UIUtilities.STEELBLUE);

        Map browsers = model.getBrowsers();
        Browser browser = (Browser) browsers.get(new Integer(
                                            Browser.PROJECT_EXPLORER));
        if (browser.isDisplayed())
            tabs.addTab(browser.getTitle(), browser.getIcon(), browser.getUI());
        browser = (Browser) browsers.get(new Integer(
                                            Browser.CATEGORY_EXPLORER));
        if (browser.isDisplayed())
            tabs.addTab(browser.getTitle(), browser.getIcon(), browser.getUI());
        browser = (Browser) browsers.get(new Integer(Browser.IMAGES_EXPLORER));
        if (browser.isDisplayed())
            tabs.addTab(browser.getTitle(), browser.getIcon(), browser.getUI());
    }

    /**
     * Creates the menu bar.
     * 
     * @return The menu bar.
     */
    private JMenuBar createMenuBar()
    {
        TaskBar tb = TreeViewerAgent.getRegistry().getTaskBar();
        JMenu[] menus = new JMenu[3];
        menus[0] = createFileMenu();
        menus[1] = createEditMenu();
        menus[2] = createViewMenu();
        tb.addToMenuBar(menus, true);
        return tb.getTaskBarMenu();
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
        Map browsers = model.getBrowsers();
        Browser browser = (Browser) browsers.get(new Integer(
                                            Browser.PROJECT_EXPLORER));
        item.setSelected(browser.isDisplayed());
        item.setAction(
                controller.getAction(TreeViewerControl.HIERARCHY_EXPLORER));
        menu.add(item);
        item = new JCheckBoxMenuItem();
        browser = (Browser) browsers.get(new Integer(
                                    Browser.CATEGORY_EXPLORER));
        item.setSelected(browser.isDisplayed());
        item.setAction(
                controller.getAction(TreeViewerControl.CATEGORY_EXPLORER));
        menu.add(item);
        item = new JCheckBoxMenuItem();
        browser = (Browser) browsers.get(new Integer(
                            Browser.IMAGES_EXPLORER));
        item.setSelected(browser.isDisplayed());
        item.setAction(
                controller.getAction(TreeViewerControl.IMAGES_EXPLORER));
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
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CREATE_TOP_CONTAINER)));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CREATE_OBJECT)));
        menu.add(createRootMenu());
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        TreeViewerAction a = controller.getAction(TreeViewerControl.VIEW);
        JMenuItem item = new JMenuItem(a);
        item.setText(a.getActionName());
        menu.add(item);
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.REFRESH_TREE)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
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
                controller.getAction(TreeViewerControl.COPY_OBJECT)));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.PASTE_OBJECT)));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.DELETE_OBJECT)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.FIND)));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CLEAR)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CLASSIFY)));
        TreeViewerAction a = controller.getAction(TreeViewerControl.ANNOTATE);
        JMenuItem item = new JMenuItem(a);
        item.setText(a.getActionName());
        menu.add(item);
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        a = controller.getAction(TreeViewerControl.PROPERTIES);
        item = new JMenuItem(a);
        item.setText(a.getActionName());
        menu.add(item);
        return menu;
    }
    
    /**
     * Helper method to create the Classify submenu.
     * 
     * @return  The Classify submenu.
     */
    /*
    private JMenu createClassifySubMenu()
    {
        IconManager im = IconManager.getInstance();
        JMenu menu = new JMenu("Classify");
        menu.setIcon(im.getIcon(IconManager.CLASSIFY));
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CLASSIFY))); 
        menu.add(new JMenuItem(
                controller.getAction(TreeViewerControl.DECLASSIFY))); 
        return menu;
    }
    */
    /**
     * Helper method to create the Hierarchy root menu.
     * 
     * @return See above.
     */
    private JMenu createRootMenu()
    {
        rootLevelMenu = new JMenu("Select Hierarchy root");
        IconManager im = IconManager.getInstance();
        rootLevelMenu.setIcon(im.getIcon(IconManager.TRANSPARENT));
        ButtonGroup bGroup = new ButtonGroup();
        JRadioButtonMenuItem item;
        ExperimenterData details = model.getUserDetails();
        Set groups = details.getGroups();
        Iterator i = groups.iterator();
        GroupData group;
        JMenu groupMenu = new JMenu("Group");
        while (i.hasNext()) {
            group = (GroupData) i.next();
            item = new JRadioButtonMenuItem(
                    controller.getGroupLevelAction(new Long(group.getId())));
            bGroup.add(item);
            groupMenu.add(item);
        }
        rootLevelMenu.add(groupMenu);
        item = new JRadioButtonMenuItem(
                controller.getAction(TreeViewerControl.USER_ROOT_LEVEL));
        bGroup.add(item);
        rootLevelMenu.add(item);
        item.setSelected(true);
        return rootLevelMenu;
    }

    /** Initializes the UI components. */
    private void initComponents()
    {
        createTabbedPane();
        workingPane = new JScrollPane();
        //JPanel p = new JPanel();
        //p.setBackground(Color.RED);
        //workingPane.add(p);
    }

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        splitPane = new JSplitPane();
        splitPane.setResizeWeight(1);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setLeftComponent(tabs);
        splitPane.setRightComponent(workingPane);
        splitPane.setDividerLocation(DIVIDER_LOCATION);
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(toolBar, BorderLayout.NORTH);
        c.add(splitPane, BorderLayout.CENTER);
        c.add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance. The
     * {@link #initialize(TreeViewerControl, TreeViewerModel) initialize}method
     * should be called straigh after to link this View to the Controller.
     */
    TreeViewerWin()
    {
        super(TITLE);
    }

    /**
     * Links this View to its Controller.
     * 
     * @param controller The Controller.
     * @param model The Model.
     */
    void initialize(TreeViewerControl controller, TreeViewerModel model)
    {
        this.controller = controller;
        this.model = model;
        statusBar = new StatusBar(controller);
        statusBar.addPropertyChangeListener(controller);
        toolBar = new ToolBar(controller);
        //popupMenu = new PopupMenu(controller);
        loadingWin = new LoadingWindow(this);
        loadingWin.addPropertyChangeListener(controller);
        initComponents();
        setJMenuBar(createMenuBar());
        buildGUI();
        controller.attachUIListeners(tabs);
    }

    /**
     * Returns the loading window.
     * 
     * @return See above.
     */
    LoadingWindow getLoadingWindow() { return loadingWin; }

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
        if (!(isBrowserVisible(browser)))
            tabs.addTab(browser.getTitle(), browser.getIcon(), browser.getUI());
        tabs.removeChangeListener(controller.getTabbedListener());
        tabs.setSelectedComponent(browser.getUI());
        tabs.addChangeListener(controller.getTabbedListener());
    }

    /**
     * Removes the specified {@link Browser} from the display.
     * 
     * @param browser The {@link Browser} to remove.
     */
    void removeBrowser(Browser browser)
    {
        if (isBrowserVisible(browser)) {
            tabs.remove(browser.getUI());
            Component c = tabs.getSelectedComponent();
            if (c == null) {
                model.setSelectedBrowser(null);
                return;
            }
            Map browsers = model.getBrowsers();
            Iterator i = browsers.values().iterator();
            boolean selected = false;
            while (i.hasNext()) {
                browser = (Browser) i.next();
                if (c.equals(browser.getUI())) {
                    model.setSelectedBrowser(browser);
                    selected = true;
                    break;
                }
            }
            if (!selected)  model.setSelectedBrowser(null);
        }
    }

    /**
     * Brings up the popup menu on top of the specified component at the
     * specified point.
     * 
     * @param c The component that requested the popup menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     *  
     */
    void showPopup(Component c, Point p)
    { 
        PopupMenu popupMenu = new PopupMenu(controller);
        popupMenu.show(c, p.x, p.y);
    }

    /**
     * Adds the specified component to the {@link #workingPane}.
     * 
     * @param component The component to add.
     */
    void addComponent(JComponent component)
    {
        JViewport viewPort = workingPane.getViewport();
        component.setPreferredSize(viewPort.getExtentSize());
        viewPort.removeAll();
        viewPort.add(component);
        viewPort.validate();
    }

    /** Removes all the components from the {@link #workingPane}. */
    void removeAllFromWorkingPane()
    {
        JViewport viewPort = workingPane.getViewport();
        if (viewPort.getComponents().length != 0) {
            viewPort.removeAll();
            viewPort.repaint();
        }
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
  
    /**
     * Brings up the menu on top of the specified component at 
     * the specified location.
     * 
     * @param menuID    The id of the menu.
     * @param c         The component that requested the popup menu.
     * @param p         The point at which to display the menu, relative to the
     *                  <code>component</code>'s coordinates.
     */
    void showMenu(int menuID, Component c, Point p)
    {
        switch (menuID) {
            case TreeViewer.MANAGER_MENU:
                toolBar.showManagementMenu(c, p);
                break;
            case TreeViewer.CLASSIFIER_MENU:
                toolBar.showClassifyMenu(c, p);
        }  
    }
    
    /**
     * Enables the tabbed pane depending on the specified parameter.
     * 
     * @param b Pass <code>true</code> to enable the tabbed pane,
     *          <code>false</code> otherwise.
     */
    void onStateChanged(boolean b)
    { 
        Map browsers = model.getBrowsers();
        if (browsers != null) {
            Iterator i = browsers.keySet().iterator();
            while (i.hasNext()) {
                ((Browser) browsers.get(i.next())).onComponentStateChange(b);
            }
            
        }
        //if (browser != null) browser.onComponentStateChange(b);
        tabs.setEnabled(b);
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
        statusBar.setProgress(hide, -1);
    }
    
    /**
     * Sets the icon depending on the passed flag.
     * 
     * @param b Pass <code>true</code> when loading data, <code>false</code>
     *          otherwise.
     */
    void setStatusIcon(boolean b)
    {
        IconManager icons = IconManager.getInstance();
        if (b) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            statusBar.setStatusIcon(icons.getIcon(IconManager.CANCEL), b); 
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            statusBar.setStatusIcon(icons.getIcon(IconManager.TRANSPARENT), b); 
        }
    }

    /** Overrides the {@link #setOnScreen() setOnScreen} method. */
    public void setOnScreen()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 8*(screenSize.width/10);
        int height = 8*(screenSize.height/10);
        setSize(width, height);
        UIUtilities.centerAndShow(this);
    }

}
