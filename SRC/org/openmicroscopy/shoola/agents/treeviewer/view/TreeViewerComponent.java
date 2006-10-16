/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerComponent
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
import java.awt.Cursor;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierFactory;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PropertiesCmd;
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.agents.treeviewer.editors.EditorFactory;
import org.openmicroscopy.shoola.agents.treeviewer.finder.ClearVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.agents.treeviewer.util.AddExistingObjectsDialog;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;

/** 
 * Implements the {@link TreeViewer} interface to provide the functionality
 * required of the tree viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerModel
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerWin
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerControl
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
class TreeViewerComponent
    extends AbstractComponent
    implements TreeViewer
{
    
    /** The Model sub-component. */
    private TreeViewerModel     model;
    
    /** The Controller sub-component. */
    private TreeViewerControl   controller;
    
    /** The View sub-component. */
    private TreeViewerWin       view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    TreeViewerComponent(TreeViewerModel model)
    {
        if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new TreeViewerControl(this);
        view = new TreeViewerWin();
        Finder f = new Finder(this);
        model.setFinder(f);
        f.addPropertyChangeListener(controller);
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        ExperimenterData user = model.getUserDetails();
        //TMP
        Set sets = user.getGroups();
        Iterator i = sets.iterator();
        long id = -1;
        while (i.hasNext()) {
            id = ((GroupData) i.next()).getId();
        }
        //model.setHierarchyRoot(USER_ROOT, user.getDefaultGroup().getId());
        model.setHierarchyRoot(USER_ROOT, id);
        controller.initialize(view);
        view.initialize(controller, model);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#activate()
     */
    public void activate()
    {
        switch (model.getState()) {
	        case NEW:
                model.getSelectedBrowser().activate();
                view.setOnScreen();
                model.setState(READY);
	            break;
	        case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
        } 
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getBrowsers()
     */
    public Map getBrowsers() { return model.getBrowsers(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#discard()
     */
    public void discard()
    {
        Map browsers = getBrowsers();
        Iterator i = browsers.values().iterator();
        while (i.hasNext())
            ((Browser) i.next()).discard();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getSelectedBrowser()
     */
    public Browser getSelectedBrowser() { return model.getSelectedBrowser(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setSelectedBrowser(Browser)
     */
    public void setSelectedBrowser(Browser browser)
    {
        switch (model.getState()) {
            case DISCARDED:
            case SAVE:
                throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED or SAVE " +
                    "state.");
        }
        Browser oldBrowser = model.getSelectedBrowser();
        if (oldBrowser == null || !oldBrowser.equals(browser)) {
            model.setSelectedBrowser(browser);
            if (browser != null) browser.activate();
            removeEditor();
            firePropertyChange(SELECTED_BROWSER_PROPERTY, oldBrowser, browser);
        }
    }
    
    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#displayBrowser(int)
     */
    public void displayBrowser(int browserType)
    {
        switch (model.getState()) {
            case DISCARDED:
            case SAVE:
                throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED or SAVE " +
                    "state.");
        }
        Map browsers = model.getBrowsers();
        Browser browser = (Browser) browsers.get(new Integer(browserType));
        if (browser.isDisplayed()) {
            view.removeBrowser(browser);
        } else {
            model.setSelectedBrowser(browser);
            view.addBrowser(browser);

        }
        browser.setDisplayed(!browser.isDisplayed());
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#showProperties(DataObject, int)
     */
    public void showProperties(DataObject object, int editorType)
    {
        switch (model.getState()) {
            case DISCARDED:
            case SAVE:
                throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED or SAVE " +
                    "state.");
        }
        switch (editorType) {
            case PROPERTIES_EDITOR:
            case CREATE_EDITOR:  
                break;
            default:
                throw new IllegalArgumentException("This method only " +
                        "supports the PROPERTIES_EDITOR and CREATE_EDITOR.");
        }
        removeEditor();
        model.setEditorType(editorType);
        Editor editor = EditorFactory.getEditor(this, object, editorType);
        editor.addPropertyChangeListener(controller);
        editor.activate();
        if (editorType == CREATE_EDITOR) 
            onComponentStateChange(false);
        view.addComponent(editor.getUI());
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#cancel()
     */
    public void cancel()
    {
        if (model.getState() != DISCARDED) {
            model.cancel();
            fireStateChange(); 
        }
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#removeEditor()
     */
    public void removeEditor()
    {
        switch (model.getState()) {
            case DISCARDED:
            //case SAVE: 
                throw new IllegalStateException("This method cannot be " +
                        "invoked in the DISCARDED, SAVE state.");
        }
        model.setEditorType(NO_EDITOR);
        view.removeAllFromWorkingPane();
        firePropertyChange(REMOVE_EDITOR_PROPERTY, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getUserDetails()
     */
    public ExperimenterData getUserDetails() { return model.getUserDetails(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#showFinder(boolean)
     */
    public void showFinder(boolean b)
    {
        switch (model.getState()) {
            case READY:
            case NEW:  
                break;
            default:
                throw new IllegalStateException("This method should only be " +
                "invoked in the READY or NEW state.");
        }
        if (model.getSelectedBrowser() == null) return;
        Finder finder = model.getFinder();
        if (b == finder.isDisplay())  return;
        Boolean oldValue = 
            finder.isDisplay() ? Boolean.TRUE : Boolean.FALSE,
        newValue = b ? Boolean.TRUE : Boolean.FALSE;
        view.showFinder(b);
        firePropertyChange(FINDER_VISIBLE_PROPERTY, oldValue, newValue);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#closeWindow()
     */
    public void closeWindow()
    {
        cancel();
        EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
        bus.post(new ExitApplication());
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#removeObject(TreeImageDisplay)
     */
    public void removeObject(TreeImageDisplay node)
    {
        switch (model.getState()) {
            case READY:
            case NEW:  
                break;
            default:
                throw new IllegalStateException("This method should only be " +
                "invoked in the READY or NEW state.");
        }
        if (node == null)
            throw new IllegalArgumentException("No node to remove.");
        if (!(node.getUserObject() instanceof DataObject))
            throw new IllegalArgumentException("Can only remove DataObject.");
        model.fireDataObjectsDeletion(node);
        fireStateChange();
    }
    
    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#removeObjects(List)
     */
    public void removeObjects(List nodes)
    {
        switch (model.getState()) {
            case READY:
            case NEW:  
                break;
            default:
                throw new IllegalStateException("This method should only be " +
                "invoked in the READY or NEW state.");
        }
        model.fireDataObjectsDeletion(nodes);
        fireStateChange();
    }
    
    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#showClassifier(ImageData[], int)
     */
    public void showClassifier(ImageData[] images, int mode)
    {
        switch (model.getState()) {
            case READY:
            case NEW:  
                break;
            default:
                throw new IllegalStateException("This method should only be " +
                "invoked in the READY or NEW state.");
        }
        if (images == null) 
            throw new IllegalArgumentException("Object cannot be null.");
        if (images.length == 0)
            throw new IllegalArgumentException("No images to classify or " +
                    "declassify.");
        removeEditor();
        model.setEditorType(CLASSIFIER_EDITOR);
        Classifier classifier = ClassifierFactory.getClassifier(this, mode, 
                                                    images);
        classifier.addPropertyChangeListener(controller);
        classifier.activate();
        view.addComponent(classifier.getUI());
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getLoadingWindow()
     */
    public JDialog getLoadingWindow()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method should only be " +
                "invoked in the DISCARDED state.");
        return view.getLoadingWindow();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setThumbnail(BufferedImage)
     */
    public void setThumbnail(BufferedImage thumbnail)
    {
        if (model.getState() == LOADING_THUMBNAIL) {
            if (thumbnail == null)
                throw new IllegalArgumentException("No thumbnail.");
            model.setState(READY);
            firePropertyChange(THUMBNAIL_LOADED_PROPERTY, null, thumbnail);
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#retrieveThumbnail(ImageData)
     */
    public void retrieveThumbnail(ImageData image)
    {
        if (model.getState() != DISCARDED) {
            if (image == null)
                throw new IllegalArgumentException("No image.");
            model.fireThumbnailLoading(image);
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#onSelectedDisplay()
     */
    public void onSelectedDisplay()
    {
        switch (model.getState()) {
            case DISCARDED:
            case SAVE:  
                throw new IllegalStateException("This method cannot be " +
                        "invoked in the DISCARDED, SAVE or LOADING_THUMBNAIL " +
                        "state");
        }
        
        removeEditor(); //remove the currently selected editor.
        //int editor = model.getEditorType();
        if (model.getEditorType() == TreeViewer.PROPERTIES_EDITOR) {
            PropertiesCmd cmd = new PropertiesCmd(this);
            cmd.execute();
        }
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#onDataObjectSave(DataObject, int)
     */
    public void onDataObjectSave(DataObject data, int operation)
    {
        int state = model.getState();
        if (operation == REMOVE_OBJECT && state != SAVE)
            throw new IllegalStateException("This method can only be " +
                    "invoked in the SAVE state");
        switch (state) {
            case DISCARDED:
                throw new IllegalStateException("This method cannot be " +
                        "invoked in the DISCARDED state");
        }
        if (data == null) 
            throw new IllegalArgumentException("No data object. ");
        switch (operation) {
            case CREATE_OBJECT:
            case UPDATE_OBJECT: 
            case REMOVE_OBJECT:  
                break;
            default:
                throw new IllegalArgumentException("Save operation not " +
                        "supported.");
        }    
        //int editor = model.getEditorType();
        //removeEditor(); //remove the currently selected editor.
        if (operation == REMOVE_OBJECT) {
            model.setState(READY);
            fireStateChange();
        }
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Browser browser = model.getSelectedBrowser();
        browser.refreshEdition(data, operation);
        if (operation == UPDATE_OBJECT) {
            Map browsers = model.getBrowsers();
            Iterator i = browsers.keySet().iterator();
            while (i.hasNext()) {
                browser = (Browser) browsers.get(i.next());
                if (!(browser.equals(model.getSelectedBrowser())))
                    browser.refreshEdition(data, operation);
            }
        }
        //onComponentStateChange(true);
       // if (editor == CREATE_EDITOR) {
           onSelectedDisplay();
       // }
        setStatus(false, "", true);
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#onNodesRemoved()
     */
    public void onNodesRemoved()
    {
        if (model.getState()!= SAVE)
            throw new IllegalStateException("This method can only be " +
                    "invoked in the SAVE state");
        model.setState(READY);
        fireStateChange();
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Browser browser = model.getSelectedBrowser();
        browser.refreshTree();
        onSelectedDisplay();
        setStatus(false, "", true);
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#clearFoundResults()
     */
    public void clearFoundResults()
    {
        switch (model.getState()) {
            //case LOADING_THUMBNAIL:
            case DISCARDED:
            case SAVE:  
                throw new IllegalStateException("This method cannot be " +
                        "invoked in the DISCARDED, SAVE or LOADING_THUMBNAIL " +
                        "state");
        }
        removeEditor(); //remove the currently selected editor.
        Browser browser = model.getSelectedBrowser();
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (browser != null) {
            browser.accept(new ClearVisitor());
            browser.setFoundInBrowser(null); 
        }
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#onImageClassified(ImageData[], Set, int)
     */
    public void onImageClassified(ImageData[] images, Set categories, int mode)
    {
        switch (model.getState()) {
            case DISCARDED:
                throw new IllegalStateException("This method cannot be " +
                        "invoked in the DISCARDED, SAVE or LOADING_THUMBNAIL " +
                        "state");
        }
        if (categories == null)
            throw new IllegalArgumentException("Categories shouln't be null.");
        if (images == null)
            throw new IllegalArgumentException("No image.");
        if (images.length == 0)
            throw new IllegalArgumentException("No image.");
        if (mode != Classifier.CLASSIFY_MODE && 
            mode != Classifier.DECLASSIFY_MODE)
            throw new IllegalArgumentException("Classification mode not " +
                    "supported.");
        Map browsers = model.getBrowsers();
        Iterator b = browsers.keySet().iterator();
        Browser browser;
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        while (b.hasNext()) {
            browser = (Browser) browsers.get(b.next());
            browser.refreshClassification(images, categories, mode);
        }
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see TreeViewer#moveToBack()
     */
    public void moveToBack()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        //view.setFocusable(false);
        view.toBack();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see TreeViewer#moveToFront()
     */
    public void moveToFront()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        view.toFront();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see TreeViewer#getRootGroupID()
     */
    public long getRootGroupID()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return model.getRootGroupID();
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#setHierarchyRoot(int, int)
     */
    public void setHierarchyRoot(int rootLevel, long rootID)
    {
        int oldLevel = model.getRootLevel();
        model.setHierarchyRoot(rootLevel, rootID);
        System.out.println("state: "+model.getState());
        if (model.getState() == READY)
            firePropertyChange(HIERARCHY_ROOT_PROPERTY, new Integer(oldLevel), 
                               new Integer(rootLevel));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#getRootLevel()
     */
    public int getRootLevel()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
            "This method cannot be invoked in the DISCARDED state.");
        return model.getRootLevel();
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#isObjectWritable(DataObject)
     */
    public boolean isObjectWritable(DataObject ho)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
            "This method cannot be invoked in the DISCARDED state.");
        return TreeViewerTranslator.isWritable(ho, getUserDetails().getId(), 
                                                getRootGroupID()); 
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#addExistingObjects(DataObject)
     */
    public void addExistingObjects(DataObject ho)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
            "This method cannot be invoked in the DISCARDED state.");
        if (ho == null) 
            throw new IllegalArgumentException("No object.");
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        model.fireDataExistingObjectsLoader(ho);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#setExistingObjects(Set)
     */
    public void setExistingObjects(Set nodes)
    {
        if (model.getState() != LOADING_DATA)
            throw new IllegalStateException(
            "This method cannot be invoked in the LOADING_DATA state.");
        if (nodes == null)
            throw new IllegalArgumentException("Nodes cannot be null.");
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        Set n = TreeViewerTranslator.transformIntoCheckNodes(nodes, 
                getUserDetails().getId(), getRootGroupID());
        model.setState(DIALOG_SELECTION);
        AddExistingObjectsDialog 
             dialog = new AddExistingObjectsDialog(view, n);
        dialog.addPropertyChangeListener(controller);
        UIUtilities.centerAndShow(dialog);  
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#addExistingObjects(Set)
     */
    public void addExistingObjects(Set set)
    {
        if (model.getState() != DIALOG_SELECTION)
            throw new IllegalStateException(
            "This method cannot be invoked in the LOADING_DATA state.");
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (set == null || set.size() == 0) model.setState(READY);
        else model.fireAddExistingObjects(set);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#navigate()
     */
    public void navigate()
    {
        int state = model.getState();
        Browser b = model.getSelectedBrowser();
        if (b != null) b.navigate(false);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#showMenu(int, Component, Point)
     */
    public void showMenu(int menuID, Component c, Point p)
    {
        //TODO: check state
        switch (menuID) {
            case MANAGER_MENU:
            case CLASSIFIER_MENU:  
                break;
            default:
                throw new IllegalArgumentException("Menu not supported.");
        }
        view.showMenu(menuID, c, p);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#setStatus(boolean, String, boolean)
     */
    public void setStatus(boolean enable, String text, boolean hide)
    {
        view.setStatus(text, hide);
        view.setStatusIcon(enable);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#onComponentStateChange(boolean)
     */
    public void onComponentStateChange(boolean b)
    {
       if (model.getState() == DISCARDED)
               throw new IllegalStateException(
               "This method cannot be invoked in the DISCARDED state.");
       Browser browser = model.getSelectedBrowser();
       if (browser != null) browser.onComponentStateChange(b);
       Boolean oldValue = Boolean.TRUE;
       if (b) oldValue = Boolean.FALSE;
       view.onStateChanged(b);
       firePropertyChange(ON_COMPONENT_STATE_CHANGED_PROPERTY, oldValue, 
                               new Boolean(b));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#setNodesToCopy(TreeImageDisplay[], int)
     */
    public void setNodesToCopy(TreeImageDisplay[] nodes, int index)
    {
        //Add controls.
        if (nodes == null || nodes.length == 0) {
            UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Copy action", "You first need to select " +
                            "the nodes to copy."); 
            return;
        }
        switch (index) {
            case CUT_AND_PASTE:
            case COPY_AND_PASTE:    
                break;

            default:
                throw new IllegalArgumentException("Index not supported.");
        }
        model.setNodesToCopy(nodes, index);
        //controller.getAction(TreeViewerControl.PASTE_OBJECT).setEnabled(true);
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see TreeViewer#paste(TreeImageDisplay[])
     */
    public void paste(TreeImageDisplay[] parents)
    {
        UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
        if (parents == null || parents.length == 0) {
            un.notifyInfo("Paste action", "You first need to select " +
            "the nodes to copy into"); 
        }
        TreeImageDisplay[] nodes = model.getNodesToCopy();
        if (nodes == null || nodes.length == 0) return; //shouldn't happen
        boolean b = model.paste(parents);
        if (!b) {
            un.notifyInfo("Paste action", "The nodes to copy cannot " +
            "be added to the selected nodes."); 
        } else fireStateChange();
    }
    
}
