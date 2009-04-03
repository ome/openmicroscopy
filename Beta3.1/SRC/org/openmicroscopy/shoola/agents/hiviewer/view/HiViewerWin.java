/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerWin
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneUI;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ClipBoardViewAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.HiViewerAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.TreeViewAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomFitAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomInAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomOutAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.env.ui.TaskBar;
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
    private static final String DEFAULT_TITLE = "Browser";
    
    /** The status bar. */
    private StatusBar           statusBar;
    
    /** The popup menu. */
    private PopupMenu           popupMenu;
    
    /** The tool bar. */
    private HiViewerToolBar     toolBar;
    
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
     * The location of the divider before removing the 
     * <code>ClipBoard</code>. 
     */
    private int                 lastMove;
    
    /** 
     * Sets the divider's location of the {@link #mainPane} if a move
     * happened.
     */
    private void setLastMove()
    {
    	if (lastMove != -1)  {
        	mainPane.setDividerLocation(lastMove);
            mainPane.setResizeWeight(0);
        }	
    }
    
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
    	int orientation = JSplitPane.HORIZONTAL_SPLIT;
    	if (ClipBoard.HORIZONTAL_SPLIT)
    		orientation = JSplitPane.VERTICAL_SPLIT;
        mainPane = new JSplitPane(orientation, browserUI, clipBoardUI);
        mainPane.setOneTouchExpandable(true);
        mainPane.setContinuousLayout(true);
        mainPane.setResizeWeight(1); //before we remove items.
        setLastMove();
        return mainPane;
    }
    
    /** Builds and lays out the GUI. */
    private void buildUI()
    {
        JPanel p = new JPanel();
        p.setBackground(BACKGROUND);
        Container container = getContentPane();
        container.setLayout(new BorderLayout(0, 0));
        addToContainer(p);
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
        TaskBar tb = HiViewerAgent.getRegistry().getTaskBar();
        menuBar.add(tb.getWindowsMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    /**
     * Helper method to create the Layout submenu.
     * 
     * @return The Layout submenu.
     */
    private JMenu createLayoutMenu()
    {
        IconManager im = IconManager.getInstance();
        JMenu menu = new JMenu("Layout");
        menu.setIcon(im.getIcon(IconManager.TRANSPARENT));
        int index = LayoutFactory.getDefaultLayoutIndex();
        ButtonGroup group = new ButtonGroup();
        JCheckBoxMenuItem item = new JCheckBoxMenuItem();
        item.setSelected(index == LayoutFactory.SQUARY_LAYOUT);
        item.setAction(controller.getAction(HiViewerControl.SQUARY));
        group.add(item);
        menu.add(item);
        item = new JCheckBoxMenuItem();
        item.setSelected(index == LayoutFactory.FLAT_LAYOUT);
        item.setAction(controller.getAction(HiViewerControl.FLAT_LAYOUT));
        group.add(item);
        menu.add(item);
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
                controller.getAction(HiViewerControl.SAVE_THUMB)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        HiViewerAction a = (HiViewerAction) 
        		controller.getAction(HiViewerControl.REFRESH);
        JMenuItem item = new JMenuItem(a);
        item.setText(a.getName());
        menu.add(item);
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(
                controller.getAction(HiViewerControl.EXIT)));
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
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.FIND)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.VIEW)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.CLASSIFY)));
        menu.add(new JMenuItem(controller.getAction(
                                HiViewerControl.DECLASSIFY)));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.ANNOTATE)));
        menu.add(new JMenuItem(controller.getAction(HiViewerControl.REMOVE)));
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new JMenuItem(
                    controller.getAction(HiViewerControl.PROPERTIES)));
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
        //menu.add(new JMenuItem(controller.getAction(HiViewerControl.VIEW_PDI)));
        //menu.add(new JMenuItem(
        //                    controller.getAction(HiViewerControl.VIEW_CGCI)));
        //menu.add(new JSeparator(JSeparator.HORIZONTAL));
        JCheckBoxMenuItem item = new JCheckBoxMenuItem();
        item.setSelected(false);
        item.setText(TreeViewAction.NAME);
        item.setAction(controller.getAction(HiViewerControl.TREE_VIEW));
        //menu.add(item);
        item = new JCheckBoxMenuItem();
        item.setSelected(true);
        item.setAction(controller.getAction(HiViewerControl.CLIPBOARD_VIEW));
        item.setText(ClipBoardViewAction.NAME);
        //menu.add(item);
        item = new JCheckBoxMenuItem();
        item.setSelected(model.isTitleBarVisible());
        item.setAction(controller.getAction(HiViewerControl.SHOW_TITLEBAR));
        menu.add(item);
        //item = new JCheckBoxMenuItem();
        //item.setSelected(model.isRollOver());
        //item.setAction(controller.getAction(HiViewerControl.ROLL_OVER));
        //menu.add(item);
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        JMenuItem mi = new JMenuItem(
        			controller.getAction(HiViewerControl.ZOOM_IN));
        mi.setText(ZoomInAction.NAME);
        menu.add(mi);
        mi = new JMenuItem(controller.getAction(HiViewerControl.ZOOM_OUT));
        mi.setText(ZoomOutAction.NAME);
        menu.add(mi);
        mi = new JMenuItem(controller.getAction(HiViewerControl.ZOOM_FIT));
        mi.setText(ZoomFitAction.NAME);
        menu.add(mi);
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(createLayoutMenu());
        menu.add(new JMenuItem(
        		controller.getAction(HiViewerControl.RESET_LAYOUT)));
        return menu;
    }
    
    /**
     * Helper method to create the <code>Help</code> menu.
     * 
     * @return See above.
     */
    private JMenu createHelpMenu()
    {
        JMenu file = new JMenu("Help");
        TaskBar bar = TreeViewerAgent.getRegistry().getTaskBar();
        JMenuItem item = bar.getCopyMenuItem(TaskBar.COMMENT);
        if (item != null) file.add(item);
        return file;
    }
    
    /**
     * Adds the specified component to the container.
     * 
     * @param body The component to add.
     */
    private void addToContainer(JComponent body)
    {
        Container container = getContentPane();
        container.add(toolBar, BorderLayout.NORTH);
        container.add(body, BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.SOUTH);
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
        statusBar = new StatusBar(iconMng.getIcon(IconManager.INFO));
        lastMove = -1;
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
        toolBar = new HiViewerToolBar(controller, model);
        setJMenuBar(createMenuBar());
        buildUI();
    }
    
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
        //addToContainer(createSplitPane(browserView, clipBoardView));
        addToContainer(browserView);
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
     *          <code>false</code> otherwise.
     */
    void showClipBoard(boolean b)
    {
        JComponent cb = model.getClipBoard().getUI();
        if (cb == null) return;
        if (b) {
            mainPane.setRightComponent(cb);
            setLastMove();
        } else {
            lastMove = mainPane.getDividerLocation();
            mainPane.remove(cb);   
        }
        ((BasicSplitPaneUI) mainPane.getUI()).getDivider().setVisible(b);
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
            JSplitPane treeViewPane = new JSplitPane();
            treeViewPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            treeViewPane.setOneTouchExpandable(true);
            treeViewPane.setContinuousLayout(true);
            if (model.getTreeView() == null) model.createTreeView();
            treeViewPane.setLeftComponent(model.getTreeView());
            treeViewPane.setRightComponent(mainPane);
            container.remove(mainPane);
            container.add(treeViewPane, BorderLayout.CENTER);
        } else {
            container.removeAll();
            addToContainer(mainPane);
        }
        container.validate();
        container.repaint();
    }
    
    /** 
     * Returns the title of the HiViewer. 
     * 
     * @return See above.
     */
    String getViewTitle()
    {
        Set roots = model.getBrowser().getRootNodes();
        if (model.isSearchResult()) {
        	int n = roots.size();
        	String s = n+" occurence";
        	if (n > 1)
        		s += "s found";
        	else s +=" found";
        	s += model.getSearchResult();
        	return s;
        }
        List r = model.getSorter().sort(roots);
        Iterator i = r.iterator();
        StringBuffer buf = new StringBuffer();
        String title = "";
        while (i.hasNext()) {
            title += ((ImageDisplay) i.next()).getTitle();
            
            if (title.length() > TITLE_MAX_LENGTH) {
                title.substring(0, 47);
                title += "...";
                break;
            }
            title += ", ";
        }
        if (title.endsWith(", ")) title = title.substring(0, title.length()-2);
        buf.insert(0, title);
        return buf.toString();
    }

    /**
     * Returns the <code>DataHandler</code> or null if not initialized.
     * 
     * @return See above.
     */
    DataHandler getDataHandler() { return model.getDataHandler(); } 
    
    /** Discards the <code>DataHandler</code>. */
    void discardDataHandler() { model.discardDataHandler(); }
    
    /**
     * Sets the location of the window relative to the bounds of the components
     * invoking the {@link HiViewer}.
     * 
     * @param bounds the bounds of the component invoking the {@link HiViewer}.
     */
    void setComponentBounds(Rectangle bounds)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width= 8*(screenSize.width/10);
        int height = 8*(screenSize.height/10);
        setSize(width, height); 
        UIUtilities.incrementRelativeToAndShow(bounds, this);
    }
    
    /** Overrides the {@link #setOnScreen() setOnScreen} method. */
    public void setOnScreen() { setComponentBounds(null); }

}
