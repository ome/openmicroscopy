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
import org.openmicroscopy.shoola.agents.treeviewer.actions.AnnotateAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClassifyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClearAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CloseTreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CopyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.DeclassifyAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.DeleteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ExitApplicationAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.FinderAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PasteAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PropertiesAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RefreshAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RootLevelAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.GroupData;
import pojos.ImageData;


/** 
 * The {@link TreeViewer}'s controller. 
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

    /** Identifies the <code>Properties action</code> in the Edit menu. */
    static final Integer	PROPERTIES = new Integer(0);
    
    /** Identifies the <code>View action</code> in the Edit menu. */
    static final Integer	VIEW = new Integer(1);
    
    /** Identifies the <code>Refresh action</code> in the File menu. */
    static final Integer	REFRESH = new Integer(2);
    
    /** Identifies the <code>Create object action</code> in the Edit menu. */
    static final Integer	CREATE_OBJECT = new Integer(3);
    
    /** Identifies the <code>Copy object action</code> in the Edit menu. */
    static final Integer	COPY_OBJECT = new Integer(4);
    
    /** Identifies the <code>Paste object action</code> in the Edit menu. */
    static final Integer	PASTE_OBJECT = new Integer(5);
    
    /** Identifies the <code>Delete object action</code> in the Edit menu. */
    static final Integer	DELETE_OBJECT = new Integer(6);
    
    /** 
     * Identifies the <code>Hierarchy Explorer</code> action in the View menu. 
     */
    static final Integer	HIERARCHY_EXPLORER = new Integer(7);
    
    /** 
     * Identifies the <code>Category Explorer</code> action in the View menu.
     */
    static final Integer	CATEGORY_EXPLORER = new Integer(8);
    
    /** Identifies the <code>Images Explorer</code> action in the View menu. */
    static final Integer	IMAGES_EXPLORER = new Integer(9);
    
    /** Identifies the <code>User root level</code> action in the File menu. */
    static final Integer	USER_ROOT_LEVEL = new Integer(10);
    
    /** Identifies the <code>Find action </code>in the Edit menu. */
    static final Integer	FIND = new Integer(11);
    
    /** Identifies the <code>Classify action</code> in the Edit menu. */
    static final Integer    CLASSIFY = new Integer(12);
    
    /** Identifies the <code>Declassify action</code> in the Edit menu. */
    static final Integer    DECLASSIFY = new Integer(13);
    
    /** Identifies the <code>Annotate action</code> in the Edit menu. */
    static final Integer    ANNOTATE = new Integer(14);
    
    /** Identifies the <code>Exit action</code> in the File menu. */
    static final Integer    EXIT = new Integer(15);
    
    /** Identifies the <code>Close action</code> in the File menu. */
    static final Integer    CLOSE = new Integer(16);
    
    /** Identifies the <code>Clear action</code> in the Edit menu. */
    static final Integer    CLEAR = new Integer(17);
    
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
        actionsMap.put(USER_ROOT_LEVEL, new RootLevelAction(model));
        actionsMap.put(FIND,  new FinderAction(model));
        actionsMap.put(CLASSIFY,  new ClassifyAction(model));
        actionsMap.put(DECLASSIFY,  new DeclassifyAction(model));
        actionsMap.put(ANNOTATE,  new AnnotateAction(model));
        actionsMap.put(CLOSE,  new CloseTreeViewerAction(model));
        actionsMap.put(CLEAR,  new ClearAction(model));
        actionsMap.put(EXIT,  new ExitApplicationAction());
    }
    
    /** Helper method to create the actions for the group level hierarchy. */
    private void createGroupLevelActions()
    {
        Set groups = model.getUserDetails().getGroups();
        Iterator i = groups.iterator();
        GroupData group;
        while (i.hasNext()) {
            group = (GroupData) i.next();
            groupLevelActionsMap.put(new Long(group.getId()), 
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
            public void windowClosing(WindowEvent e) { model.closeWindow(); }
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
        groupLevelActionsMap = new HashMap();
        createActions();
        createGroupLevelActions();
        model.addChangeListener(this);
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
                model.clearFoundResults();
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
    Action getGroupLevelAction(Long id) 
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
        if (name.equals(TreeViewer.CANCEL_LOADING_PROPERTY)) {
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
        } else if (name.equals(Editor.CLOSE_EDITOR_PROPERTY) ||
                name.equals(Classifier.CLOSE_CLASSIFIER_PROPERTY)) {
            model.removeEditor();
        } else if (name.equals(TreeViewer.FINDER_VISIBLE_PROPERTY)) {
            Boolean b = (Boolean) pce.getNewValue();
            if (!b.booleanValue()) model.clearFoundResults();
        } else if (name.equals(TreeViewer.SELECTED_BROWSER_PROPERTY)) {
            Browser  b = model.getSelectedBrowser();
            Iterator i = model.getBrowsers().values().iterator();
            Browser browser;
            while (i.hasNext()) {
                browser = (Browser) i.next();
                browser.setSelected(browser.equals(b));
            }
        } else if (name.equals(TreeViewer.THUMBNAIL_LOADING_PROPERTY)) {
            model.retrieveThumbnail((ImageData) pce.getNewValue());
        } else if (name.equals(Browser.SELECTED_DISPLAY_PROPERTY)) {
            model.onSelectedDisplay();
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
            case TreeViewer.SAVE:
                LoadingWindow window = view.getLoadingWindow();
                window.setTitle(TreeViewer.SAVING_TITLE);
                UIUtilities.centerAndShow(window);
                break;
            case TreeViewer.READY:
                view.getLoadingWindow().setVisible(false); 
                break;  
        }
    }
    
}
