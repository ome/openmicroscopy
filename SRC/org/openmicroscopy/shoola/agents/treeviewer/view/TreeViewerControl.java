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
import java.util.Set;
import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClassifyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CopyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.DeclassifyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.DeleteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.FinderAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PasteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PropertiesAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RootLevelAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.treeviewer.editors.EditorUI;
import org.openmicroscopy.shoola.agents.treeviewer.finder.ClearVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.GroupData;


/** 
 * Thre {@link TreeViewer}'s controller. 
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

    /** Identifies the Properties action in the Actions menu. */
    static final Integer	PROPERTIES = new Integer(0);
    
    /** Identifies the View action in the Actions menu. */
    static final Integer	VIEW = new Integer(1);
    
    /** Identifies the Refresh action in the Actions menu. */
    static final Integer	REFRESH = new Integer(2);
    
    /** Identifies the Create object action in the Actions menu. */
    static final Integer	CREATE_OBJECT = new Integer(3);
    
    /** Identifies the Copy object action in the Actions menu. */
    static final Integer	COPY_OBJECT = new Integer(4);
    
    /** Identifies the Paste object action in the Actions menu. */
    static final Integer	PASTE_OBJECT = new Integer(5);
    
    /** Identifies the Delete object action in the Actions menu. */
    static final Integer	DELETE_OBJECT = new Integer(6);
    
    /** Identifies the Hierarchy Explorer action in the Views menu. */
    static final Integer	HIERARCHY_EXPLORER = new Integer(7);
    
    /** Identifies the Category Explorer action in the Views menu. */
    static final Integer	CATEGORY_EXPLORER = new Integer(8);
    
    /** Identifies the Images Explorer action in the Views menu. */
    static final Integer	IMAGES_EXPLORER = new Integer(9);
    
    /** Identifies the World root level action in the Hierarchy menu. */
    static final Integer	WORLD_ROOT_LEVEL = new Integer(10);
    
    /** Identifies the User root level action in the Hierarchy menu. */
    static final Integer	USER_ROOT_LEVEL = new Integer(11);
    
    /** Identifies the Find action in the Edit menu. */
    static final Integer	FIND = new Integer(12);
    
    /** Identifies the Classify action in the Edit menu. */
    static final Integer    CLASSIFY = new Integer(13);
    
    /** Identifies the Find action in the Edit menu. */
    static final Integer    DECLASSIFY = new Integer(14);
    
    /** 
     * Reference to the {@link TreeViewer} component, which, in this context,
     * is regarded as the Model.
     */
    private TreeViewer      model;
    
    /** Reference to the View. */
    private TreeViewerWin   view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map             actionsMap;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map				groupLevelActionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(PROPERTIES, new PropertiesAction(model));
        actionsMap.put(VIEW, new ViewAction(model));
        actionsMap.put(REFRESH, new RefreshAction(model));
        actionsMap.put(CREATE_OBJECT, new CreateAction(model));
        actionsMap.put(COPY_OBJECT, new CopyAction(model));
        actionsMap.put(DELETE_OBJECT, new DeleteAction(model));
        actionsMap.put(PASTE_OBJECT, new PasteAction(model));
        actionsMap.put(HIERARCHY_EXPLORER, 
                 new BrowserSelectionAction(model, Browser.HIERARCHY_EXPLORER));
        actionsMap.put(CATEGORY_EXPLORER, 
                new BrowserSelectionAction(model, Browser.CATEGORY_EXPLORER));
        actionsMap.put(IMAGES_EXPLORER, 
                new BrowserSelectionAction(model, Browser.IMAGES_EXPLORER));
        actionsMap.put(WORLD_ROOT_LEVEL, 
                new RootLevelAction(model, TreeViewer.WORLD_ROOT));
        actionsMap.put(USER_ROOT_LEVEL, 
                new RootLevelAction(model, TreeViewer.USER_ROOT));
        actionsMap.put(FIND,  new FinderAction(model));
        actionsMap.put(CLASSIFY,  new ClassifyAction(model));
        actionsMap.put(DECLASSIFY,  new DeclassifyAction(model));
    }
    
    /** Helper method to create the actions for the group level hierarchy. */
    private void createGroupLevelActions()
    {
        Set groups = model.getUserDetails().getGroups();
        Iterator i = groups.iterator();
        GroupData group;
        while (i.hasNext()) {
            group = (GroupData) i.next();
            groupLevelActionsMap.put(new Integer(group.getId()), 
                    new RootLevelAction(model, TreeViewer.GROUP_ROOT, 
                            group.getId(), group.getName()));
        }
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
        model.addPropertyChangeListener(this);
        view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { model.closing(); }
        });
    }
    
    /** Clears the results of a previous search action. */
    private void clearFind()
    {
        Browser browser = model.getSelectedBrowser();
        if (browser != null) {
            browser.accept(new ClearVisitor());
            browser.setFoundInBrowser(null); 
        }
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
        groupLevelActionsMap = new HashMap();
        createActions();
        createGroupLevelActions();
    }
    
    /**
     * Adds listeners to UI components.
     *
     * @param tabs
     */
    void attachUIListeners(JTabbedPane tabs)
    {
        //Register listener
        tabs.addChangeListener(new ChangeListener() {
            // This method is called whenever the selected tab changes
            public void stateChanged(ChangeEvent ce) {
                JTabbedPane pane = (JTabbedPane) ce.getSource();
                clearFind();
                Component c = pane.getSelectedComponent();
                if (c == null) {
                    model.setSelectedBrowser(null);
                    return;
                }
                Map browsers = model.getBrowsers();
                Iterator i = browsers.values().iterator();
                boolean selected = false;
                Browser browser;
                while (i.hasNext()) {
                    browser = (Browser) i.next();
                    if (c.equals(browser.getUI())) {
                        model.setSelectedBrowser(browser);
                        selected = true;
                        break;
                    }
                }
                if (!selected) model.setSelectedBrowser(null);
            }
        });
    }
    
    /**
     * Links this Controller to its View.
     * 
     * @param view   Reference to the View. Mustn't be <code>null</code>.
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
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the group id.
     * @return The specified action.
     */
    Action getGroupLevelAction(Integer id) 
    { 
        return (Action) groupLevelActionsMap.get(id);
    }
      
    /**
     * Reacts to property changed. 
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
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
        } else if (name.equals(EditorUI.CANCEL_EDITION_PROPERTY) ||
                name.equals(Classifier.CANCEL_CLASSIFICATION_PROPERTY) ||
                name.equals(Browser.SELECTED_DISPLAY_PROPERTY)) {
            model.removeEditor();
        } else if (name.equals(Finder.CLOSE_FINDER_PROPERTY)) {
            clearFind();
            model.showFinder(false);
        } else if (name.equals(TreeViewer.SELECTED_BROWSER_PROPERTY)) {
            Browser  b = model.getSelectedBrowser();
            Iterator i = model.getBrowsers().values().iterator();
            Browser browser;
            while (i.hasNext()) {
                browser = (Browser) i.next();
                browser.setSelected(browser.equals(b));
            }
        } else if (name.equals(TreeViewer.REMOVE_EDITOR_PROPERTY)) {
            model.cancel();
        } else if (name.equals(Classifier.CLASSIFY_PROPERTY)) {
            model.classifyImage((Map) pce.getNewValue());
        } else if (name.equals(Classifier.DECLASSIFY_PROPERTY)) {
            model.declassifyImage((Map) pce.getNewValue());
        } else if (name.equals(Classifier.BROWSE_PROPERTY)) {
            model.browse((DataObject) pce.getNewValue());
        }
    }

    /**
     * Reacts to state changes in the {@link TreeViewer} and in the
     * {@link Browser}.
     * @see ChangeListener#stateChanged(ChangeEvent)
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
                case Browser.COUNTING_ITEMS:
                case Browser.READY:
                    if (view.getLoadingWindow().isVisible())
                        view.getLoadingWindow().setVisible(false);
                    break;
            }
        }
    }
    
}
