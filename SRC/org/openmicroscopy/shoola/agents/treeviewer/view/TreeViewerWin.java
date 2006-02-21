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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.editors.EditorUI;
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

    /** The description of the window. */
    private static final String DESCRIPTION = "Brings up the Data Manager";

    /** The location of the verical slipt pane location. */
    private static final int 	DIVIDER_LOCATION = 200;

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
    private PopupMenu 			popupMenu;

    /** The loading window. */
    private LoadingWindow 		loadingWin;

    /** The menu hosting the root levels. */
    private JMenu 				rootLevelMenu;
    
    /**
     * Checks if the specified {@link Browser} is already visible.
     * 
     * @param browser The specified {@link Browser}.
     * @return <code>true</code> if visible, <code>false</code> otherwise.
     */
    private boolean isBrowserVisible(Browser browser)
    {
        Component[] comps = tabs.getComponents();
        for (int i = 0; i < comps.length; i++) 
            return (comps[i].equals(browser.getUI()));
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
                                            Browser.HIERARCHY_EXPLORER));
        tabs.addTab(browser.getTitle(), browser.getIcon(), browser.getUI());
        browser = (Browser) browsers.get(new Integer(
                                            Browser.CATEGORY_EXPLORER));
        tabs.addTab(browser.getTitle(), browser.getIcon(), browser.getUI());
    }

    /**
     * Creates the menu bar.
     * 
     * @return The menu bar.
     */
    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    /**
     * Helper method to create the <code>Help</code> menu.
     * 
     * @return See above.
     */
    private JMenu createHelpMenu()
    {
        JMenu file = new JMenu("Help");
        return file;
    }
    
    /**
     * Helper method to create the <code>File</code> menu.
     * 
     * @return See above.
     */
    private JMenu createFileMenu()
    {
        JMenu file = new JMenu("File");
        file.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CREATE_OBJECT)));
        file.add(createRootMenu());
        file.add(new JSeparator(JSeparator.HORIZONTAL));
        file.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CLOSE)));
        file.add(new JSeparator(JSeparator.HORIZONTAL));
        file.add(new JMenuItem(
                controller.getAction(TreeViewerControl.REFRESH)));
        file.add(new JSeparator(JSeparator.HORIZONTAL));
        file.add(new JMenuItem(
                controller.getAction(TreeViewerControl.EXIT)));
        return file;
    }
    
    /**
     * Helper method to create the <code>Edit</code> menu.
     * 
     * @return See above.
     */
    private JMenu createEditMenu()
    {
        JMenu edit = new JMenu("Edit");
        edit.add(new JMenuItem(
                controller.getAction(TreeViewerControl.COPY_OBJECT)));
        edit.add(new JMenuItem(
                controller.getAction(TreeViewerControl.PASTE_OBJECT)));
        edit.add(new JMenuItem(
                controller.getAction(TreeViewerControl.DELETE_OBJECT)));
        edit.add(new JSeparator(JSeparator.HORIZONTAL));
        edit.add(new JMenuItem(
                controller.getAction(TreeViewerControl.FIND)));
        edit.add(new JMenuItem(
                controller.getAction(TreeViewerControl.CLEAR)));
        edit.add(new JSeparator(JSeparator.HORIZONTAL));
        edit.add(new JMenuItem(
                controller.getAction(TreeViewerControl.VIEW)));
        edit.add(new JSeparator(JSeparator.HORIZONTAL));
        edit.add(createClassifySubMenu());
        edit.add( new JMenuItem(
                controller.getAction(TreeViewerControl.ANNOTATE)));
        edit.add(new JSeparator(JSeparator.HORIZONTAL));
        edit.add(new JMenuItem(
                controller.getAction(TreeViewerControl.PROPERTIES)));
        return edit;
    }
    
    /**
     * Helper method to create the Classify submenu.
     * 
     * @return  The Classify submenu.
     */
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
    
    /**
     * Helper method to create the Hierarchy root menu.
     * 
     * @return See above.
     */
    private JMenu createRootMenu()
    {
        rootLevelMenu = new JMenu("Hierarchy root");
        IconManager im = IconManager.getInstance();
        rootLevelMenu.setIcon(im.getIcon(IconManager.TRANSPARENT));
        ButtonGroup bGroup = new ButtonGroup();
        JRadioButtonMenuItem item;
        /*
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                controller.getAction(TreeViewerControl.WORLD_ROOT_LEVEL));
        rootLevel.add(item);
        bGroup.add(item);
        */
        ExperimenterData details = model.getUserDetails();
        Set groups = details.getGroups();
        Iterator i = groups.iterator();
        GroupData group;
        while (i.hasNext()) {
            group = (GroupData) i.next();
            item = new JRadioButtonMenuItem(
                    controller.getGroupLevelAction(new Integer(group.getId())));
            bGroup.add(item);
            rootLevelMenu.add(item);
        }
        item = new JRadioButtonMenuItem(
                controller.getAction(TreeViewerControl.USER_ROOT_LEVEL));
        bGroup.add(item);
        rootLevelMenu.add(item);
        item.setSelected(true);
        return rootLevelMenu;
    }

    /**
     * Helper method to create the Views menu.
     * 
     * @return The Views menu.
     */
    private JMenu createViewMenu()
    {
        JMenu views = new JMenu("View");
        JMenuItem item = new JMenuItem(
                controller.getAction(TreeViewerControl.HIERARCHY_EXPLORER));
        views.add(item);
        item = new JMenuItem(
                controller.getAction(TreeViewerControl.CATEGORY_EXPLORER));
        views.add(item);
        item = new JMenuItem(
                controller.getAction(TreeViewerControl.IMAGES_EXPLORER));
        views.add(item);
        return views;
    }

    /** Initializes the UI components. */
    private void initComponents()
    {
        createTabbedPane();
        workingPane = new JScrollPane();
        JPanel p = new JPanel();
        p.setBackground(Color.RED);
        workingPane.add(p);
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
        c.add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Specifies icons, text, and tooltips for the display buttons in the
     * TaskBar. Those buttons are managed by the superclass, we only have to
     * specify what they should look like.
     */
    private void configureDisplayButtons()
    {
        IconManager im = IconManager.getInstance();
        configureQuickLaunchBtn(im.getIcon(IconManager.MANAGER), DESCRIPTION);
        configureWinMenuEntry(TITLE, im.getIcon(IconManager.MANAGER));
    }

    /**
     * Creates a new instance. The
     * {@link #initialize(TreeViewerControl, TreeViewerModel) initialize}method
     * should be called straigh after to link this View to the Controller.
     */
    TreeViewerWin()
    {
        super(TITLE, TreeViewerAgent.getRegistry().getTaskBar());
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
        popupMenu = new PopupMenu(controller);
        loadingWin = new LoadingWindow(this);
        loadingWin.addPropertyChangeListener(controller);
        initComponents();
        configureDisplayButtons();
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
        tabs.setSelectedComponent(browser.getUI());
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
            if (!selected)
                model.setSelectedBrowser(null);
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
    void showPopup(Component c, Point p) { popupMenu.show(c, p.x, p.y); }

    /**
     * Adds the specified component to the {@link #workingPane}.
     * 
     * @param component The component to add.
     */
    void addComponent(JComponent component)
    {
        if (component instanceof EditorUI)
            ((EditorUI) component)
                    .setComponentsSize(workingPane.getBounds().width);
        JViewport viewPort = workingPane.getViewport();
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
        /*
        if (b) { //finder visible.
            JSplitPane pane = new JSplitPane();
            pane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            pane.setResizeWeight(1);
            pane.setOneTouchExpandable(true);
            pane.setContinuousLayout(true);
            pane.setTopComponent(workingPane);
            pane.setBottomComponent(model.getFinder());
            splitPane.setRightComponent(pane);
        } else {
            splitPane.remove(splitPane.getRightComponent());
            splitPane.setRightComponent(workingPane);
        }
        splitPane.setDividerLocation(splitPane.getDividerLocation());
        splitPane.repaint();
        */
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
        c.add(pane, BorderLayout.CENTER);
        c.validate();
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
