/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerControl
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.actions.ActivationAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.AnnotateAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ClassifyAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.DeclassifyAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ExitAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ExitApplicationAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.FindAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.FindwSTAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.HideTitleBarAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.PropertiesAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.RefreshAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SaveLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SaveThumbnailsAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ShowTitleBarAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SquaryLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.TreeLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.TreeViewAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewCGCIAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewPDIAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomFitAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomInAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomOutAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.treeview.SelectedNodeVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeView;

/** 
 * The HiViewer's Controller.
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
class HiViewerControl
    implements ChangeListener, PropertyChangeListener
{
    
    /** Identifies the <code>Exit action</code> in the File menu. */
    static final Integer     EXIT = new Integer(0);
    
    /** Identifies the <code>View P/D/I</code> action in the View menu. */
    static final Integer     VIEW_PDI = new Integer(1);
    
    /** Identifies the <code>View CG/C/I</code> action in the View menu. */
    static final Integer     VIEW_CGCI = new Integer(2);
    
    /** Identifies the Find With ST action in the Find menu. */
    static final Integer     FIND_W_ST = new Integer(3);
    
    /** Identifies the <code>Clear action</code> in the Edit menu. */
    static final Integer     CLEAR = new Integer(4);
    
    /** Identifies the Squary Layout action in the Layout menu. */
    static final Integer     SQUARY = new Integer(5);
    
    /** Identifies the Tree Layout action in the Layout menu. */
    static final Integer     TREE = new Integer(6);
    
    /** Identifies the <code>Show Title Bar</code> action in the View menu. */
    static final Integer     SHOW_TITLEBAR = new Integer(7);
    
    /** Identifies the Hide Title Bar action in the Layout menu. */
    static final Integer     HIDE_TITLEBAR = new Integer(8);
    
    /** Identifies the Save Layout action in the Layout menu. */
    static final Integer     SAVE = new Integer(9);
    
    /** Identifies the <code>Properties</code> action in the Edit menu. */
    static final Integer     PROPERTIES = new Integer(10);
    
    /** Identifies the <code>Annotate</code> action in the Edit menu. */
    static final Integer     ANNOTATE = new Integer(11);
    
    /** Identifies the <code>Classify</code> action in the Edit menu. */
    static final Integer     CLASSIFY = new Integer(12);
    
    /** Identifies the <code>Declassify</code> action in the Edit menu. */
    static final Integer     DECLASSIFY = new Integer(13);
    
    /** Identifies the <code>View</code> action in the Edit menu. */
    static final Integer     VIEW = new Integer(14);
    
    /** Identifies the <code>Zoom In</code> action in the View menu. */
    static final Integer     ZOOM_IN = new Integer(15);
    
    /** Identifies the <code>Zoom Out</code> action in the View menu. */
    static final Integer     ZOOM_OUT = new Integer(16);
    
    /** Identifies the <code>Zoom Fit</code> action in the View menu. */
    static final Integer     ZOOM_FIT = new Integer(17);
    
    /** Identifies the <code>Refresh</code> action in the File menu. */
    static final Integer     REFRESH = new Integer(18);
    
    /** Identifies the <code>Save thumbnails</code> action in the File menu. */
    static final Integer     SAVE_THUMB = new Integer(19);
      
    /** Identifies the <code>ree view</code>T action in the View menu. */
    static final Integer     TREE_VIEW = new Integer(20);
    
    /** Identifies the <code>Exit Application</code> action in the File menu. */
    static final Integer     EXIT_APPLICATION = new Integer(21);
    
    /** Identifies the <code>Find</code> action in the Edit menu. */
    static final Integer     FIND = new Integer(22);
    
    /** 
     * Reference to the {@link HiViewer} component, which, in this context,
     * is regarded as the Model.
     */
    private HiViewer        model;
    
    /** Reference to the View. */
    private HiViewerWin     view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map             actionsMap;
    
    /** Keep track of the old state.*/
    private int             historyState;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(EXIT, new ExitAction(model));
        actionsMap.put(VIEW_CGCI, new ViewCGCIAction(model));
        actionsMap.put(VIEW_PDI, new ViewPDIAction(model));
        actionsMap.put(REFRESH, new RefreshAction(model));
        actionsMap.put(SQUARY, new SquaryLayoutAction(model));
        actionsMap.put(TREE, new TreeLayoutAction(model));
        actionsMap.put(SHOW_TITLEBAR, new ShowTitleBarAction(model));
        actionsMap.put(HIDE_TITLEBAR, new HideTitleBarAction(model));
        actionsMap.put(SAVE, new SaveLayoutAction(model));
        actionsMap.put(PROPERTIES, new PropertiesAction(model));
        actionsMap.put(ANNOTATE, new AnnotateAction(model));
        actionsMap.put(CLASSIFY, new ClassifyAction(model));
        actionsMap.put(DECLASSIFY, new DeclassifyAction(model));
        actionsMap.put(VIEW, new ViewAction(model));
        actionsMap.put(ZOOM_IN, new ZoomInAction(model));
        actionsMap.put(ZOOM_OUT, new ZoomOutAction(model));
        actionsMap.put(ZOOM_FIT, new ZoomFitAction(model));
        actionsMap.put(FIND_W_ST, new FindwSTAction(model));
        actionsMap.put(SAVE_THUMB, new SaveThumbnailsAction(model));
        actionsMap.put(TREE_VIEW, new TreeViewAction(model));
        actionsMap.put(FIND, new FindAction(model));
        actionsMap.put(EXIT_APPLICATION, new ExitApplicationAction());
    }
  
    /** Creates the windowsMenuItems. */
    private void createWindowsMenuItems()
    {
        Set viewers = HiViewerFactory.getViewers();
        Iterator i = viewers.iterator();
        JMenu menu = HiViewerFactory.getWindowsMenu();
        menu.removeAll();
        HiViewer viewer;
        while (i.hasNext()) {
            viewer = (HiViewer) i.next();
            //if (!(viewer == model))
                menu.add(new JMenuItem(new ActivationAction(viewer)));
        }
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window. Attaches a menu listener to the window menu.
     */
    private void attachListeners()
    {
        JMenu menu = HiViewerFactory.getWindowsMenu();
        menu.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) { createWindowsMenuItems(); }
            
            /** 
             * Required by I/F but not actually needed in our case, 
             * no op implementation.
             * @see MenuListener#menuCanceled(MenuEvent)
             */ 
            public void menuCanceled(MenuEvent e) {}

            /** 
             * Required by I/F but not actually needed in our case, 
             * no op implementation.
             * @see MenuListener#menuDeselected(MenuEvent)
             */ 
            public void menuDeselected(MenuEvent e) {}
            
        });
        
        //Listen to keyboard selection
        menu.addMenuKeyListener(new MenuKeyListener() {

            
            public void menuKeyReleased(MenuKeyEvent e)
            {
                createWindowsMenuItems();
            }
            
            /** 
             * Required by I/F but not actually needed in our case, 
             * no op implementation.
             * @see MenuKeyListener#menuKeyPressed(MenuKeyEvent)
             */
            public void menuKeyPressed(MenuKeyEvent e) {}
  
            /** 
             * Required by I/F but not actually needed in our case, 
             * no op implementation.
             * @see MenuKeyListener#menuKeyTyped(MenuKeyEvent)
             */
            public void menuKeyTyped(MenuKeyEvent e) {}
            
        });
        
        view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { model.discard(); }
        }); 
    }
    
    /** Sets the browser UI, the clipboard UI and attaches listeners. */
    private void setViews()
    {
        Browser browser = model.getBrowser();
        browser.addPropertyChangeListener(Browser.POPUP_POINT_PROPERTY, this);
        browser.addPropertyChangeListener(Browser.THUMB_SELECTED_PROPERTY, 
                							this); 
        browser.addPropertyChangeListener(Browser.SELECTED_DISPLAY_PROPERTY, 
											this); 
        view.setViews(browser.getUI(), model.getClipBoard().getUI());
        view.setViewTitle();
    }
    
    /**
     * Brings up on screen the selected node. The nodes containing the child
     * are visited i.e. parent then grandparent all the way up to the root node.
     * 
     * @param childBounds   The bounds of the selected node.
     * @param parent        The node containing the child.
     * @param isRoot        <code>true</code> if its the root node, 
     *                      <code>false</code> otherwise.   
     */
    private void scrollToNode(Rectangle childBounds, ImageDisplay parent,
                                boolean isRoot)
    {
        JScrollPane dskDecorator = parent.getDeskDecorator();
        Rectangle viewRect = dskDecorator.getViewport().getViewRect();
        if (!viewRect.contains(childBounds)) {
            JScrollBar vBar = dskDecorator.getVerticalScrollBar();
            JScrollBar hBar = dskDecorator.getHorizontalScrollBar();
            vBar.setValue(childBounds.y);
            hBar.setValue(childBounds.x);
        }
        if (!isRoot) {
            ImageDisplay node = parent.getParentDisplay();
            scrollToNode(childBounds, node, (node.getParentDisplay() == null));       
        }      
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(HiViewerWin) initialize} method 
     * should be called straigh 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link HiViewer} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    HiViewerControl(HiViewer model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        actionsMap = new HashMap();
        createActions();
    }
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param view   Reference to the View.  Mustn't be <code>null</code>.
     */
    void initialize(HiViewerWin view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        historyState = -1;
        model.addChangeListener(this);   
        attachListeners();
    }
    
    /**
     * Returns a read-only map with HiViewer actions.
     * 
     * @return See above.
     */
    Map getActionMap() { return Collections.unmodifiableMap(actionsMap) ;}  
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return (Action) actionsMap.get(id); }

    /**
     * Brings up on screen the specified {@link ImageDisplay node} if not
     * visible. 
     * 
     * @param node The node to bring up on screen. Mustn't be <code>null</code>.
     */
    void scrollToNode(ImageDisplay node)
    {
        if (node == null) throw new IllegalArgumentException("No node.");
        ImageDisplay parent = node.getParentDisplay();
        if (parent != null)
            scrollToNode(node.getBounds(), parent,
                    (parent.getParentDisplay() == null)); 
    }
    
    /**
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent ce)
    {
        int state = model.getState();
        switch (state) {
            case HiViewer.LOADING_THUMBNAILS:
                setViews();
                break;
            case HiViewer.READY:
                if (historyState == HiViewer.LOADING_HIERARCHY)
                    setViews();
                break;
            case HiViewer.DISCARDED:
                view.setVisible(false);
                view.dispose();
                break;
        }
        historyState = state;
    }
    
    /**
     * Reacts to property changes in the {@link Browser}.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String propName = pce.getPropertyName();
        if (Browser.POPUP_POINT_PROPERTY.equals(propName)) {
            Browser browser = model.getBrowser();
            ImageDisplay d = browser.getLastSelectedDisplay();
            Point p = browser.getPopupPoint();
            if (d != null && p != null) view.showPopup(d, p);
        } else if (Browser.THUMB_SELECTED_PROPERTY.equals(propName)) {  
            ImageDisplay d = model.getBrowser().getLastSelectedDisplay();
            ThumbWinManager.display((ImageNode) d, model);
        } else if (Browser.SELECTED_DISPLAY_PROPERTY.equals(propName)) {
                TreeView treeView = model.getTreeView();
                if (treeView == null) return; 
                ImageDisplay d = model.getBrowser().getLastSelectedDisplay();
                treeView.accept(new SelectedNodeVisitor(treeView, d)); 
        } else if (TreeView.TREE_POPUP_POINT_PROPERTY.equals(propName))  {
            TreeView treeView = model.getTreeView();
            if (treeView == null) return; //tree shouldn't be null
            Point p = treeView.getPopupPoint();
            if (p != null) view.showPopup(((JComponent) pce.getNewValue()), p);
        } else if (TreeView.CLOSE_PROPERTY.equals(propName)) {
            model.showTreeView(false);
        } else if (TreeView.TREE_SELECTED_DISPLAY_PROPERTY.equals(propName)) {
            TreeView treeView = model.getTreeView();
            if (treeView == null) return; //tree shouldn't be null
            Browser browser = model.getBrowser();
            ImageDisplay img = (ImageDisplay) pce.getNewValue();
            if (img != null) {
                if (!(img.equals(browser.getLastSelectedDisplay())))
                    model.getBrowser().setSelectedDisplay(img);
            } else model.getBrowser().setSelectedDisplay(img);
            
        } else if (HiViewer.SCROLL_TO_NODE_PROPERTY.equals(propName)) {
            scrollToNode((ImageDisplay) pce.getNewValue());
        }
    }

}
