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
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The {@link HiViewer}'s View.
 * Embeds the <code>Browser</code>'s UI to display the various visualization
 * trees.  Also provides a menu bar and a status bar.  After creation this
 * window will display an empty panel as a placeholder for the <code>Browser
 * </code>'s UI.  When said UI is ready, the Controller calls the
 * {@link #setBrowserView(JComponent) setBrowserView} method to have the View
 * display it. 
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
    
    
    /** The status bar. */
    private StatusBar       statusBar;
    
    /** The popup menu. */
    private PopupMenu       popupMenu;
    
    /** The Controller. */
    private HiViewerControl controller;
    
    
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
        menuBar.add(createHierarchyMenu());
        menuBar.add(createFindMenu());
        menuBar.add(createLayoutMenu());
        menuBar.add(createActionsMenu());
        return menuBar;
    }
    
    /**
     * Helper method to create the Hierarchy menu.
     * 
     * @return  The Hierarchy menu.
     */
    private JMenu createHierarchyMenu()
    {
        JMenu menu = new JMenu("Hierarchy");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.add(new JMenuItem(
                controller.getAction(HiViewerControl.VIEW_PDI)));
        menu.add(new JMenuItem(
                controller.getAction(HiViewerControl.VIEW_CGCI)));
        menu.addSeparator();
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.EXIT)));
        return menu;
    }
    
    /**
     * Helper method to create the Find menu.
     * 
     * @return  The Find menu.
     */
    private JMenu createFindMenu()
    {
        JMenu menu = new JMenu("Find");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(new JMenuItem(
                controller.getAction(HiViewerControl.FIND_ANNOTATED)));
        menu.add(new JMenuItem(
                controller.getAction(HiViewerControl.FIND_W_TITLE)));
        menu.add(new JMenuItem(
                controller.getAction(HiViewerControl.FIND_W_ANNOTATION)));
        menu.add(new JMenuItem(
                controller.getAction(HiViewerControl.FIND_W_ST)));
        menu.addSeparator();
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.CLEAR)));
        return menu;
    }
    
    /**
     * Helper method to create the Layout menu.
     * 
     * @return  The Layout menu.
     */
    private JMenu createLayoutMenu()
    {
        JMenu menu = new JMenu("Layout");
        menu.setMnemonic(KeyEvent.VK_L);
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.SQUARY)));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.TREE)));
        menu.addSeparator();
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.SAVE)));
        return menu;
    }
    
    /**
     * Helper method to create the Actions menu.
     * 
     * @return  The Actions menu.
     */
    private JMenu createActionsMenu()
    {
        JMenu menu = new JMenu("Actions");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.add(new JMenuItem(
                controller.getAction(HiViewerControl.PROPERTIES)));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.ANNOTATE)));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.CLASSIFY)));
        menu.addSeparator();
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.VIEW)));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.ZOOM_IN)));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.ZOOM_OUT)));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.ZOOM_FIT)));
        return menu;
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(HiViewerControl) initialize} method should be
     * called straigh after to link this View to the Controller.
     */
    HiViewerWin() 
    {
        super("Hierarchy Viewer", HiViewerAgent.getRegistry().getTaskBar());
        IconManager iconMng = IconManager.getInstance();
        statusBar = new StatusBar(iconMng.getIcon(IconManager.STATUS_INFO));
    }
    
    /**
     * Links this View to its Controller.
     * 
     * @param controller The Controller.
     */
    void initialize(HiViewerControl controller)
    {
        this.controller = controller;
        popupMenu = new PopupMenu(controller);
        setJMenuBar(createMenuBar());
        buildUI();
    }

    /** 
     * Sets the <code>Browser</code>'s UI into the display panel.
     * 
     * @param browserView  The <code>Browser</code>'s UI.  
     */
    void setBrowserView(JComponent browserView)
    {
        Container container = getContentPane();
        container.removeAll();
        container.add(browserView, BorderLayout.CENTER);
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
