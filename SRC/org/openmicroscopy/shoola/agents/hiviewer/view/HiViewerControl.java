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
import org.openmicroscopy.shoola.agents.hiviewer.actions.FindwSTAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.HideTitleBarAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.PropertiesAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.RefreshAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SaveLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SaveThumbnailsAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ShowTitleBarAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SquaryLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.TreeLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewCGCIAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewPDIAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomFitAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomInAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomOutAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;

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
        actionsMap.put(HiViewer.EXIT, new ExitAction(model));
        actionsMap.put(HiViewer.VIEW_CGCI, new ViewCGCIAction(model));
        actionsMap.put(HiViewer.VIEW_PDI, new ViewPDIAction(model));
        actionsMap.put(HiViewer.REFRESH, new RefreshAction(model));
        actionsMap.put(HiViewer.SQUARY, new SquaryLayoutAction(model));
        actionsMap.put(HiViewer.TREE, new TreeLayoutAction(model));
        actionsMap.put(HiViewer.SHOW_TITLEBAR, new ShowTitleBarAction(model));
        actionsMap.put(HiViewer.HIDE_TITLEBAR, new HideTitleBarAction(model));
        actionsMap.put(HiViewer.SAVE, new SaveLayoutAction(model));
        actionsMap.put(HiViewer.PROPERTIES, new PropertiesAction(model));
        actionsMap.put(HiViewer.ANNOTATE, new AnnotateAction(model));
        actionsMap.put(HiViewer.CLASSIFY, new ClassifyAction(model));
        actionsMap.put(HiViewer.DECLASSIFY, new DeclassifyAction(model));
        actionsMap.put(HiViewer.VIEW, new ViewAction(model));
        actionsMap.put(HiViewer.ZOOM_IN, new ZoomInAction(model));
        actionsMap.put(HiViewer.ZOOM_OUT, new ZoomOutAction(model));
        actionsMap.put(HiViewer.ZOOM_FIT, new ZoomFitAction(model));
        actionsMap.put(HiViewer.FIND_W_ST, new FindwSTAction(model));
        actionsMap.put(HiViewer.SAVE_THUMB, new SaveThumbnailsAction(model));
    }
  
    /** Creates the windowsMenuItems. */
    private void createWindowsMenuItems()
    {
        Set viewers = HiViewerFactory.getViewers();
        Iterator i = viewers.iterator();
        JMenu menu = view.getWindowsMenu();
        menu.removeAll();
        HiViewer viewer;
        while (i.hasNext()) {
            viewer = (HiViewer) i.next();
            if (!(viewer == model)) {
                menu.add(new JMenuItem(new ActivationAction(viewer)));
            }
        }
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window. Attaches a menu listener to the window menu.
     */
    private void attachListeners()
    {
        JMenu menu = view.getWindowsMenu();
        menu.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) { createWindowsMenuItems(); }
            
            /** 
             * Required but not actually needed in our case, 
             * no op implementation.
             */ 
            public void menuCanceled(MenuEvent e) {}

            /** 
             * Required but not actually needed in our case, 
             * no op implementation.
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
             * Required but not actually needed in our case, 
             * no op implementation.
             */
            public void menuKeyPressed(MenuKeyEvent e) {}
  
            /** 
             * Required but not actually needed in our case, 
             * no op implementation.
             */ 
            public void menuKeyTyped(MenuKeyEvent e) {}
        });
        
        view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { model.discard(); }
        });
        
    }
    
    /** Sets the browser UI, the clipboard UI and attach listeners. */
    private void setViews()
    {
        Browser browser = model.getBrowser();
        browser.addPropertyChangeListener(Browser.POPUP_POINT_PROPERTY, 
                                      this);
        browser.addPropertyChangeListener(
                Browser.THUMB_SELECTED_PROPERTY, this); 
        view.setViews(browser.getUI(), model.getClipBoard().getUI());
        view.setViewTitle();
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
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
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
    
    /** Reacts to property changes in the {@link Browser}. */
    public void propertyChange(PropertyChangeEvent pce)
    {
        Browser browser = model.getBrowser();
        ImageDisplay d = browser.getSelectedDisplay();
        String propName = pce.getPropertyName();
        if (Browser.POPUP_POINT_PROPERTY.equals(propName)) {
            Point p = browser.getPopupPoint();
            if (browser.getSelectedLayout() == LayoutFactory.TREE_LAYOUT) {
                JComponent c = browser.getTreeDisplay();
                if (c != null && p != null) view.showPopup(c, p);
            } else {
                if (d != null && p != null) view.showPopup(d, p);
            }
        } else if (Browser.THUMB_SELECTED_PROPERTY.equals(propName)) {  
            ThumbWinManager.display((ImageNode) d, model);
        }
    }
    
}
