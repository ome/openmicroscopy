/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerWin
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The {@link HiViewer}'s View.
 * Embeds the <code>Browser</code>'s UI and the <code>ClipBoard</code>'s UI
 * to display the various visualization trees. Also provides a menu bar and a
 * status bar. After creation this window will display an empty panel as a 
 * placeholder for the <code>Browser</code>'s UI. When said UI is ready, the
 * Controller calls the {@link #setViews(JComponent, JComponent) setViews}
 * method to have the View display it. 
 *
 * @see org.openmicroscopy.shoola.agents.hiviewer.browser.Browser
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
class HiViewerWin
    extends TopWindow
{

    /** Default background color. */
    public static final Color   BACKGROUND = new Color(250, 253, 255);
    
    /** The maximum length of the title. */
    private static final int    TITLE_MAX_LENGTH = 50;
    
    /** The default title of the window. */
    private static final String DEFAULT_TITLE = "Hierarchy Viewer";
    
    /** The status bar. */
    private StatusBar           statusBar;
    
    /** The popup menu. */
    private PopupMenu           popupMenu;
    
    /** The windows menu. */
    private JMenu               windowsMenu;
    
    /** 
     * The main pane hosting the <code>Browser</code> and 
     * the <code>ClipBoard</code>
     */
    private JSplitPane			mainPane;
    
    /** The Controller. */
    private HiViewerControl     controller;
    
    /** The Model. */
    private HiViewerModel       model;
    
    /**
     * Sets the <code>Browser</code>'s UI and <code>ClipBoard</code>'s UI into
     * a horizontal splitPane.
     * 
     * @param browserUI The <code>Browser</code>'s UI
     * @param clipBoardUI The <code>ClipBoard</code>'s UI
     * @return See above.
     */
    private JSplitPane createSplitPane(JComponent browserUI,
                                        JComponent clipBoardUI)
    {
        mainPane = new JSplitPane();
        mainPane.setResizeWeight(1);
        mainPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        mainPane.setOneTouchExpandable(true);
        mainPane.setContinuousLayout(true);
        mainPane.setTopComponent(browserUI);
        mainPane.setBottomComponent(clipBoardUI);
        return mainPane;
    }
    
    /** Builds and lays out the GUI. */
    private void buildUI()
    {
        JPanel p = new JPanel();
        p.setBackground(BACKGROUND);
        Container container = getContentPane();
        container.setLayout(new BorderLayout(0, 0));
        container.add(p, BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.SOUTH);
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
        menuBar.add(windowsMenu);
        menuBar.add(createHelpMenu());
        return menuBar;
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
        menu.setMnemonic(KeyEvent.VK_C);
        menu.setIcon(im.getIcon(IconManager.CLASSIFY));
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.CLASSIFY)));
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.DECLASSIFY)));
        return menu;
    }
    
    /**
     * Helper method to create the File menu.
     * 
     * @return  The File menu.
     */
    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.SAVE_THUMB)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.EXIT)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(controller.getAction(HiViewer.REFRESH)));
        //menu.add(new JSeparator(JSeparator.HORIZONTAL));
        //menu.add(new JMenuItem(controller.getAction(
        //                        HiViewer.EXIT_APPLICATION)));
        return menu;
    }
    
    /**
     * Helper method to create the Edit menu.
     * 
     * @return  The Edit menu.
     */
    private JMenu createEditMenu()
    {
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.add(new JMenuItem(controller.getAction(HiViewer.FIND)));
        //menu.add(new JMenuItem(controller.getAction(HiViewer.CLEAR)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.VIEW)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(createClassifySubMenu());
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.ANNOTATE)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.PROPERTIES)));
        return menu;
    }
    
    /**
     * Helper method to create the View menu.
     * 
     * @return  The Layout menu.
     */
    private JMenu createViewMenu()
    {
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.add(new JMenuItem(controller.getAction(HiViewer.VIEW_PDI)));
        menu.add(new JMenuItem(controller.getAction(HiViewer.VIEW_CGCI)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(controller.getAction(HiViewer.SQUARY)));
        menu.add(new JMenuItem(controller.getAction(HiViewer.TREE_VIEW)));
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.SHOW_TITLEBAR)));
        menu.add(new JMenuItem(
                controller.getAction(HiViewer.HIDE_TITLEBAR)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(controller.getAction(HiViewer.ZOOM_IN)));
        menu.add(new JMenuItem(controller.getAction(HiViewer.ZOOM_OUT)));
        menu.add(new JMenuItem(controller.getAction(HiViewer.ZOOM_FIT)));
        return menu;
    }

    /** Helper method to create the Windows menu. */
    private void createWindowsMenu()
    {
        windowsMenu = new JMenu("Window");
        windowsMenu.setMnemonic(KeyEvent.VK_W);
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
     * Creates a new instance.
     * The {@link #initialize(HiViewerControl, HiViewerModel) initialize} method
     * should be called straigh after to link this View to the Controller
     * and the Model.
     */
    HiViewerWin() 
    {
        super(DEFAULT_TITLE);
        IconManager iconMng = IconManager.getInstance();
        statusBar = new StatusBar(iconMng.getIcon(IconManager.STATUS_INFO));
        createWindowsMenu();
    }

    /**
     * Links this View to its Controller and Model.
     * 
     * @param controller The Controller.
     * @param model The Model.
     */
    void initialize(HiViewerControl controller, HiViewerModel model)
    {
        this.controller = controller;
        this.model = model;
        popupMenu = new PopupMenu(controller);
        setJMenuBar(createMenuBar());
        buildUI();
    }

    /** 
     * Returns the <code>windows</code> menu. 
     * 
     * @return See above.
     */
    JMenu getWindowsMenu() { return windowsMenu; }
    
    /** 
     * Sets the <code>Browser</code>'s UI and the <code>ClipBoard</code>'s UI
     * into the display panel.
     * 
     * @param browserView  The <code>Browser</code>'s UI.  
     * @param clipBoardView The <code>ClipBoard</code>'s UI.
     */
    void setViews(JComponent browserView, JComponent clipBoardView)
    {
        Container container = getContentPane();
        container.removeAll();
        container.add(createSplitPane(browserView, clipBoardView),
                        BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.SOUTH); 
    }
    
    /**
     * Adjusts the status bar according to the specified arguments.
     * 
     * @param status Textual description to display.
     * @param hideProgressBar Whether or not to hide the progress bar.
     * @param progressPerc  The percentage value the progress bar should
     *                      display.  If negative, it is iterpreted as
     *                      not available and the progress bar will be
     *                      set to indeterminate mode.  This argument is
     *                      only taken into consideration if the progress
     *                      bar shouldn't be hidden.
     */
    void setStatus(String status, boolean hideProgressBar, int progressPerc)
    {
        statusBar.setStatus(status);
        statusBar.setProgress(hideProgressBar, progressPerc);
    }
    
    /**
     * Brings up the popup menu on top of the specified component at the
     * specified point.
     * 
     * @param c The component that requested the popup menu.
     * @param p The point at which to display the menu, relative to the 
     *          <code>component</code>'s coordinates.
     *          
     */
    void showPopup(Component c, Point p) { popupMenu.show(c, p.x, p.y); }
    
    /** Closes and disposes of the window. */
    void closeViewer()
    {
        setVisible(false);
        dispose();
    }
    
    /** Sets the title of the viewer. */
    void setViewTitle()
    {
        String title = DEFAULT_TITLE+": ";
        title += getViewTitle();
        setTitle(title);
    }
    
    /**
     * Shows or hides the component depending on the stated of the frame.
     * 
     * @param b Passed <code>true</code> to show the component, 
     * 			<code>false</code> otherwise.
     */
    void showTreeView(boolean b)
    {
        Container container = getContentPane();
        if (b) {
            JSplitPane pane = new JSplitPane();
            pane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            pane.setOneTouchExpandable(true);
            pane.setContinuousLayout(true);
            pane.setLeftComponent(model.getTreeView());
            pane.setRightComponent(mainPane);
            container.remove(mainPane);
            container.add(pane, BorderLayout.CENTER);
        } else {
            container.removeAll();
            container.add(mainPane, BorderLayout.CENTER);
            container.add(statusBar, BorderLayout.SOUTH); 
        }
        container.validate();
        container.repaint();
    }
    
    /** 
     * Returns the title of the HiViewer. 
     * 
     * @return See above.
     */
    public String getViewTitle()
    {
        Set roots = model.getBrowser().getRootNodes();
        Iterator i = roots.iterator();
        StringBuffer buf = new StringBuffer();
        String title = "";
        while (i.hasNext()) {
            title += ((ImageDisplay) i.next()).getTitle();
            if (title.length() > TITLE_MAX_LENGTH) {
                title.substring(0, 47);
                title += "...";
                break;
            }
        }
        buf.insert(0, title);
        return buf.toString();
    }
    
    /** Overrides the {@link #setOnScreen() setOnScreen} method. */
    public void setOnScreen()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width= 8*(screenSize.width/10);
        int height = 8*(screenSize.height/10);
        setSize(width, height); 
        UIUtilities.centerAndShow(this);
    }
    
}
