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
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.editors.DOEditor;
import org.openmicroscopy.shoola.agents.treeviewer.util.UtilConstants;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * The {@link TreeViewer}'s View. Embeds the different <code>Browser</code>
 * 's UI to display the various visualization trees. Also provides a menu bar
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

    /** The component hosting the working pane. */
    private JScrollPane 		rightPane;

    /** The tabbed pane hosting the {@link Browser}s. */
    private JTabbedPane 		tabs;

    /** The popup menu. */
    private PopupMenu 			popupMenu;

    /** The loading window. */
    private LoadingWindow 		loadingWin;

    /** The menu hosting the root levels. */
    private JMenu 				rootLevel;

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

    /**
     * Creates the {@link JTabbedPane} hosting the browser.
     * 
     * @return See above.
     */
    private JTabbedPane createTabbedPane()
    {
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        Font font = (Font) TreeViewerAgent.getRegistry().lookup(
                "/resources/fonts/Titles");
        tabs.setFont(font);
        tabs.setForeground(UtilConstants.STEELBLUE);

        Map browsers = model.getBrowsers();
        Browser browser = (Browser) browsers.get(new Integer(
                Browser.HIERARCHY_EXPLORER));
        tabs.addTab(browser.getTitle(), browser.getIcon(), browser.getUI());

        return tabs;
    }

    /**
     * Creates the menu bar.
     * 
     * @return The menu bar.
     */
    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createRootMenu());
        menuBar.add(createViewsMenu());
        return menuBar;
    }

    /**
     * Helper method to create the Hierarchy root menu.
     * 
     * @return See above.
     */
    private JMenu createRootMenu()
    {
        rootLevel = new JMenu("Hierarchy root");
        rootLevel.setEnabled(false);
        return rootLevel;
    }

    /**
     * Helper method to create the Views menu.
     * 
     * @return The Views menu.
     */
    private JMenu createViewsMenu()
    {
        JMenu views = new JMenu("Views");
        JMenuItem item = new JMenuItem(
                controller.getAction(TreeViewer.HIERARCHY_EXPLORER));
        views.add(item);
        item = new JMenuItem(
                controller.getAction(TreeViewer.CATEGORY_EXPLORER));
        views.add(item);
        item = new JMenuItem(controller.getAction(TreeViewer.IMAGES_EXPLORER));
        views.add(item);
        return views;
    }

    /** Initializes the UI components. */
    private void initComponents()
    {
        rightPane = new JScrollPane();
        JPanel p = new JPanel();
        p.setBackground(Color.RED);
        rightPane.add(p);
    }

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        JSplitPane pane = new JSplitPane();
        pane.setResizeWeight(1);
        pane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        pane.setOneTouchExpandable(true);
        pane.setContinuousLayout(true);
        pane.setLeftComponent(createTabbedPane());
        pane.setRightComponent(rightPane);
        pane.setDividerLocation(DIVIDER_LOCATION);
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(pane, BorderLayout.CENTER);
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
     * Adds the specified component to the {@link #rightPane}.
     * 
     * @param component The component to add.
     */
    void addComponent(JComponent component)
    {
        if (component instanceof DOEditor)
            ((DOEditor) component)
                    .setComponentsSize(rightPane.getBounds().width);
        JViewport viewPort = rightPane.getViewport();
        viewPort.removeAll();
        viewPort.add(component);
        viewPort.validate();
    }

    /** Removes all the components from the {@link #rightPane}. */
    void removeAllFromRightPane()
    {
        JViewport viewPort = rightPane.getViewport();
        if (viewPort.getComponents().length != 0) {
            viewPort.removeAll();
            viewPort.repaint();
        }
    }

    /** Adds items to the {@link #rootLevel} menu. */
    void fillRootLevelMenu()
    {
        rootLevel.setEnabled(true);
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                			controller.getAction(TreeViewer.WORLD_ROOT_LEVEL));
        rootLevel.add(item);
        group.add(item);
        UserDetails details = model.getUserDetails();
        List ids = details.getGroupIDs();
        Iterator i = ids.iterator();
        while (i.hasNext()) {
            item = new JRadioButtonMenuItem(
                    controller.getGroupLevelAction((Integer) i.next()));
            group.add(item);
            rootLevel.add(item);
        }
        item = new JRadioButtonMenuItem(controller
                .getAction(TreeViewer.USER_ROOT_LEVEL));
        group.add(item);
        rootLevel.add(item);
        item.setSelected(true);
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
