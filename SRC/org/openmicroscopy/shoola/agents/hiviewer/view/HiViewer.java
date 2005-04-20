/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerUIF;
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.actions.BrowserAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class HiViewer
    extends TopWindow
{

    /** Reference to the statusBar. */
    private StatusBar   statusBar;
    
    private PopupMenu   popupMenu;
    
    private Action[]    actions;
    
    public HiViewer(Action[] actions, Registry reg)
    {
        super("Hierarchy Viewer", reg.getTaskBar());
        this.actions = actions;
        IconManager iconMng = IconManager.getInstance();
        statusBar = new StatusBar(iconMng.getIcon(IconManager.STATUS_INFO));
        popupMenu = new PopupMenu(actions);
        setJMenuBar(createMenuBar());
        buildUI();
    }
    
    /** Build and lay out the GUI. */
    private void buildUI()
    {
        JPanel p = new JPanel();
        p.setBackground(HiViewerUIF.BACKGROUND);
        Container container = getContentPane();
        container.setLayout(new BorderLayout(0, 0));
        container.add(p, BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.SOUTH);
    }
    
    /** Create the menu bar. */
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
        menu.add(new JMenuItem(actions[HiViewerUIF.VIEW_PDI]));
        menu.add(new JMenuItem(actions[HiViewerUIF.VIEW_CGCI]));
        menu.addSeparator();
        menu.add(new JMenuItem(actions[HiViewerUIF.EXIT]));
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
        menu.add(new JMenuItem(actions[HiViewerUIF.FIND_ANNOTATED]));
        menu.add(new JMenuItem(actions[HiViewerUIF.FIND_W_TITLE]));
        menu.add(new JMenuItem(actions[HiViewerUIF.FIND_W_ANNOTATION]));
        menu.add(new JMenuItem(actions[HiViewerUIF.FIND_W_ST]));
        menu.addSeparator();
        menu.add(new JMenuItem(actions[HiViewerUIF.CLEAR]));
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
        menu.add(new JMenuItem(actions[HiViewerUIF.SQUARY]));
        menu.add(new JMenuItem(actions[HiViewerUIF.TREE]));
        menu.addSeparator();
        menu.add(new JMenuItem(actions[HiViewerUIF.SAVE]));
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
        menu.add(new JMenuItem(actions[HiViewerUIF.PROPERTIES]));
        menu.add(new JMenuItem(actions[HiViewerUIF.ANNOTATE]));
        menu.add(new JMenuItem(actions[HiViewerUIF.CLASSIFY]));
        menu.addSeparator();
        menu.add(new JMenuItem(actions[HiViewerUIF.VIEW]));
        menu.add(new JMenuItem(actions[HiViewerUIF.ZOOM_IN]));
        menu.add(new JMenuItem(actions[HiViewerUIF.ZOOM_OUT]));
        return menu;
    }

    /** Closes the widget. */
    public void closeViewer()
    {
        setVisible(false);
        dispose();
    }
    
    /** 
     * Link each action to the specified browser.
     * 
     * @param browser   the specified browser.
     */
    public void linkActionsTo(Browser browser)
    {
        for (int i = 0; i < actions.length; i++)
           ((BrowserAction) actions[i]).setBrowser(browser);
    }
    
    /** Set the browser view. */
    public void setBrowserView(JComponent browserView)
    {
        Container container = getContentPane();
        container.removeAll();
        container.add(browserView, BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.SOUTH); 
    }
    
    /** Overrides the {@link #setOnScreen()} method. */
    public void setOnScreen()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width= 8*(screenSize.width/10);
        int height = 8*(screenSize.height/10);
        setSize(width, height); 
        UIUtilities.centerAndShow(this);
    }
    
    public void setStatus(String status, boolean hideProgressBar, 
                            int progressPerc)
    {
        statusBar.setStatus(status);
        statusBar.setProgress(hideProgressBar, progressPerc);
    }
    
}
