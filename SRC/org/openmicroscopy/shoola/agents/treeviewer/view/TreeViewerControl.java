/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerControl
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
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Action;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PropertiesAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.editors.CreateDataObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeViewerControl
    implements ChangeListener, PropertyChangeListener
{

    /** 
     * Reference to the {@link TreeViewer} component, which, in this context,
     * is regarded as the Model.
     */
    private TreeViewer      model;
    
    /** Reference to the View. */
    private TreeViewerWin   view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map             actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(TreeViewer.PROPERTIES, new PropertiesAction(model));
        actionsMap.put(TreeViewer.VIEW, new ViewAction(model));
        actionsMap.put(TreeViewer.REFRESH, new RefreshAction(model));
        actionsMap.put(TreeViewer.NEW_OBJECT, new CreateAction(model));
        actionsMap.put(TreeViewer.HIERARCHY_EXPLORER, 
                 new BrowserSelectionAction(model, Browser.HIERARCHY_EXPLORER));
        actionsMap.put(TreeViewer.CATEGORY_EXPLORER, 
                new BrowserSelectionAction(model, Browser.CATEGORY_EXPLORER));
        actionsMap.put(TreeViewer.IMAGES_EXPLORER, 
                new BrowserSelectionAction(model, Browser.IMAGES_EXPLORER));
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window. 
     */
    private void attachListeners()
    {
        Map browsers = model.getBrowsers();
        Iterator i = browsers.values().iterator();
        Browser browser;
        while (i.hasNext()) {
            browser = (Browser) i.next();
            browser.addPropertyChangeListener(this);
            browser.addChangeListener(this);
        }
            
        view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { model.discard(); }
        });
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(TreeViewerWin) initialize} method 
     * should be called straight 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link TreeViewer} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    TreeViewerControl(TreeViewer model)
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
    void initialize(TreeViewerWin view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        attachListeners();
    }

    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return (Action) actionsMap.get(id); }
    
    /** Reacts to property changed. */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
        System.out.println(name);
        if (name.equals(Browser.CANCEL_PROPERTY)) {
            Browser browser = model.getSelectedBrowser();
            if (browser != null) browser.cancel();
        } else if (name.equals(Browser.POPUP_MENU_PROPERTY)) {
            Component c = (Component) pce.getNewValue();
            Browser browser = model.getSelectedBrowser();
            if (browser != null && c != null)
                view.showPopup(c, browser.getClickPoint());
        } else if (name.equals(Browser.CLOSE_PROPERTY)) {
            Browser browser = (Browser) pce.getNewValue();
            if (browser != null) view.removeBrowser(browser);
        } else if (name.equals(CreateDataObject.CANCEL_CREATION_PROPERTY)) {
            view.cancelDataObjectCreation();
        } else if (name.equals(Browser.SELECTED_DISPLAY_PROPERTY)) {
            view.cancelDataObjectCreation();
        } else if (name.equals(CreateDataObject.FINISH_PROPERTY)) {
            DataObject object = (DataObject) pce.getNewValue();
            model.createObject(object);
        }
            
            
    }

    /**
     * Reacts to state changes in the {@link TreeViewer} and in the
     * {@link Browser}.
     */
    public void stateChanged(ChangeEvent ce)
    {
        switch (model.getState()) {
            case TreeViewer.DISCARDED:
                view.closeViewer();
                break;
            default:
                break;
        }
        
        Browser browser = model.getSelectedBrowser();
        if (browser != null) {
            switch (browser.getState()) {
                case Browser.LOADING_DATA:
                case Browser.LOADING_LEAVES:    
                    UIUtilities.centerAndShow(view.getLoadingWindow());
                    break;
                case Browser.READY:
                    if (view.getLoadingWindow().isVisible())
                        view.getLoadingWindow().setVisible(false);
                    break;
            }
        }
    }
    
}
