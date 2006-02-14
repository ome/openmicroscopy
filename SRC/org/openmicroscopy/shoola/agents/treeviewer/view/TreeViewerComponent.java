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
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierFactory;
import org.openmicroscopy.shoola.agents.treeviewer.editors.DOEditor;
import org.openmicroscopy.shoola.agents.treeviewer.editors.EditorFactory;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.env.data.OmeroPojoService;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
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
     * Converts the specified UI rootLevel into its corresponding 
     * constant defined by the {@link OmeroPojoService}.
     * 
     * @param level The level to convert.
     * @return See above.
     */
    private int convertRootLevel(int level)
    {
        switch (level) {
            case WORLD_ROOT:
                return OmeroPojoService.WORLD_HIERARCHY_ROOT;
            case USER_ROOT:
                return OmeroPojoService.USER_HIERARCHY_ROOT;
            case GROUP_ROOT:
                return OmeroPojoService.GROUP_HIERARCHY_ROOT;
            default:
                throw new IllegalArgumentException("Level not supported");
        }
    }
    
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
        //check state
        Browser oldBrowser = model.getSelectedBrowser();
        if (oldBrowser.equals(browser)) return;
        model.setSelectedBrowser(browser);
        removeEditor();
        firePropertyChange(SELECTED_BROWSER_PROPERTY, oldBrowser, browser);
    }
    
    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#addBrowser(int)
     */
    public void addBrowser(int browserType)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        Map browsers = model.getBrowsers();
        Browser browser = (Browser) browsers.get(new Integer(browserType));
        if (browser != null) {
            model.setSelectedBrowser(browser);
            view.addBrowser(browser);
        }
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#showProperties(DataObject, int)
     */
    public void showProperties(DataObject object, int editorType)
    {
        if (editorType == PROPERTIES_EDITOR || editorType == CREATE_EDITOR)
            model.setEditorType(editorType);
        else return;
        model.setDataObject(object);
        DOEditor panel = EditorFactory.getEditor(this, object, editorType);
        panel.addPropertyChangeListener(DOEditor.CANCEL_EDITION_PROPERTY, 
                                        controller);
        if ((object instanceof ImageData) || (object instanceof DatasetData)) {
            model.fireAnnotationLoading(object); 
            fireStateChange();
            UIUtilities.centerAndShow(view.getLoadingWindow());
        } else view.addComponent(panel); 
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#cancel()
     */
    public void cancel()
    {
        if (model.getState() != DISCARDED) {
            model.cancel();
            if (view.getLoadingWindow().isVisible())
                view.getLoadingWindow().setVisible(false); 
        }
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#cancel()
     */
    public void removeEditor()
    {
        //TODO: check state 
        model.setEditorType(NO_EDITOR);
        model.setDataObject(null);
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
        if (model.getSelectedBrowser() == null) return;
        Finder finder = model.getFinder();
        if (b == finder.isDisplay())  return;
        finder.setDisplay(b);
        view.showFinder(b);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#closing()
     */
    public void closing()
    {
        cancel();
		view.setVisible(false);	 
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#saveObject(DataObject, int)
     */
    public void saveObject(DataObject object, int algorithm)
    {
        int state = model.getState();
        if (state != NEW && state != READY)
            throw new IllegalStateException("This method should only be " +
                    "invoked in the NEW or READY state.");
        Browser browser = model.getSelectedBrowser();
        if (browser == null) return;
        switch (algorithm) {
	        case CREATE_OBJECT:
	            model.fireDataObjectCreation(object);
                break;
	        case UPDATE_OBJECT:
	        case DELETE_OBJECT:
	            model.fireDataObjectUpdate(object, algorithm);
	            break;
	        default:
	            throw new IllegalArgumentException("Save object: Algorithm " +
	            		"not supported.");
        }
        LoadingWindow window = view.getLoadingWindow();
        window.setTitleAndText(LoadingWindow.SAVING_TITLE,
                                LoadingWindow.SAVING_MSG);
        UIUtilities.centerAndShow(window);
        fireStateChange();
    }
    
    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#saveObject(DataObject, AnnotationData, int)
     */
    public void saveObject(DataObject o, AnnotationData annotation, int op)
    {
        int state = model.getState();
        if (state != NEW && state != READY)
            throw new IllegalStateException("This method should only be " +
                    "invoked in the NEW or READY state.");
        switch (op) {
	        case CREATE_ANNOTATION:
	        case UPDATE_ANNOTATION:
	        case DELETE_ANNOTATION:
	            break;
	        default:
	            throw new IllegalArgumentException("Save object: Annotation " +
	            		"algorithm not supported.");
        }
        if (annotation == null) 
            throw new IllegalArgumentException("No annotation to save.");
        if (o == null) model.fireAnnotationEdition(annotation, op);
        else {
            if ((o instanceof DatasetData) || (o instanceof ImageData)) {
                model.fireDataObjectAndAnnotationEdition(o, annotation, op);
                fireStateChange();   
            }
            else throw new IllegalArgumentException("DataObject not " +
            										"supported.");
        }									
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setSaveResult(DataObject, int)
     */
    public void setSaveResult(DataObject object, int op)
    {
        if (model.getState() != SAVE_EDITION)
            throw new IllegalStateException(
                    "This method can only be invoked in the SAVE state."); 
        if (op != DELETE_OBJECT && op != UPDATE_OBJECT && op != CREATE_OBJECT) 
            throw new IllegalArgumentException(
                "The operations supported are DELETE and UPDATE."); 
        Browser browser = model.getSelectedBrowser();
        browser.refreshEdit(object, op);
        if (view.getLoadingWindow().isVisible())
            view.getLoadingWindow().setVisible(false);
        view.removeAllFromWorkingPane();
        model.setState(READY);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setAnnotations(Map)
     */
    public void setAnnotations(Map map)
    {
        if (model.getState() != LOADING_ANNOTATION)
            throw new IllegalStateException("This method can only be invoked" +
                    " in the LOADING_ANNOTATION state.");
        if (map == null) throw new IllegalArgumentException("No annotations.");
        DOEditor editor = EditorFactory.getEditor();
        if (editor == null) return;
        editor.setAnnotations(map);
        view.getLoadingWindow().setVisible(false);
        if (editor.hasThumbnail()) 
            model.fireThumbnailLoading();
        else model.setState(READY);
        fireStateChange();
        view.addComponent(editor); 
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setDataObjectThumbnail(BufferedImage)
     */
    public void setDataObjectThumbnail(BufferedImage thumbnail)
    {
        if (model.getState() != LOADING_THUMBNAIL)
            throw new IllegalStateException("This method can only be invoked" +
                    " in the LOADING_THUMBNAIL state.");
        if (thumbnail == null)
            throw new IllegalArgumentException("No thumbnail.");
        if (model.getEditorType() == CLASSIFIER_EDITOR) {
            Classifier classifier = ClassifierFactory.getClassifier();
            if (classifier == null) return;
            classifier.setThumbnail(thumbnail);
        } else {
            DOEditor editor = EditorFactory.getEditor();
            if (editor == null) return;
            editor.setThumbnail(thumbnail);
        }
        model.setState(READY);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#retrieveClassification(int)
     */
    public void retrieveClassification(int imageID)
    {
        if (model.getEditorType() != PROPERTIES_EDITOR) 
            throw new IllegalStateException("This method should only be " +
                    "invoked in the editing state.");
        model.fireClassificationLoading(imageID);
        fireStateChange();
        UIUtilities.centerAndShow(view.getLoadingWindow());
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setRetrievedClassification(Set)
     */
    public void setRetrievedClassification(Set paths)
    {
        if (model.getState() != LOADING_CLASSIFICATION)
            throw new IllegalStateException("This method should only be " +
                    "invoked in the LOADING_CLASSIFICATION state.");
        if (paths == null)
            throw new IllegalArgumentException("No paths to set.");
        DOEditor editor = EditorFactory.getEditor();
        if (editor == null) return;
        Set set = TreeViewerTranslator.transformHierarchy(paths);
        editor.setClassifiedNodes(set);
        model.setState(READY);
        view.getLoadingWindow().setVisible(false);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#browse(DataObject)
     */
    public void browse(DataObject object)
    {
        if (object instanceof ImageData) {
            ImageData image = (ImageData) object;
            LoadImage evt = new LoadImage(image.getId(), 
                    image.getDefaultPixels().getId(), image.getName());
            TreeViewerAgent.getRegistry().getEventBus().post(evt);
            return;
        }
        int id = -1;
        int index = -1;
        if (object instanceof CategoryData) {
            id =  ((CategoryData) object).getId();
            index = Browse.CATEGORY;
        } else if (object instanceof CategoryGroupData) {
            id =  ((CategoryGroupData) object).getId();
            index = Browse.CATEGORY_GROUP;
        }
        if (id == -1) 
            throw new IllegalArgumentException("Can only browse category or " +
                    "category group.");
        int rootID = model.getSelectedBrowser().getRootID();
        int rootLevel = model.getSelectedBrowser().getRootLevel();
        Browse event = new Browse(id, index, convertRootLevel(rootLevel), 
                                rootID);
        TreeViewerAgent.getRegistry().getEventBus().post(event);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setClassificationPaths(int, Set)
     */
    public void setClassificationPaths(int m, Set paths)
    {
        if (model.getState() != LOADING_CLASSIFICATION_PATH)
            throw new IllegalStateException("This method should only be " +
                    "invoked in the LOADING_CLASSIFICATION_PATH state.");
        if (paths == null)
            throw new IllegalArgumentException("No paths to set.");
        Set nodes = TreeViewerTranslator.transformClassificationPaths(paths);
        view.getLoadingWindow().setVisible(false);
        model.setEditorType(CLASSIFIER_EDITOR);
        Classifier classifier = ClassifierFactory.getClassifier(this, m, nodes,
                                 (ImageData) model.getDataObject());
        classifier.addPropertyChangeListener(controller);
        view.addComponent(classifier); 
        model.fireThumbnailLoading();
        fireStateChange();
    }
    
    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#showClassifier(ImageData, int)
     */
    public void showClassifier(ImageData object, int mode)
    {
        if (model.getState() != READY)
            throw new IllegalStateException("This method should only be " +
                "invoked in the READY state.");
        if (object == null) 
            throw new IllegalArgumentException("Object cannot be null.");
        view.removeAllFromWorkingPane();
        firePropertyChange(REMOVE_EDITOR_PROPERTY, Boolean.FALSE, Boolean.TRUE);
        model.fireClassificationPathsLoading(object, mode);
        fireStateChange();
        UIUtilities.centerAndShow(view.getLoadingWindow());
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#classifyImage(Map)
     */
    public void classifyImage(Map categories)
    {
        if (model.getState() != READY)
            throw new IllegalStateException("This method should only be " +
                "invoked in the READY state.");
        if (categories == null)
            throw new IllegalArgumentException("No categories.");
        model.fireClassification(categories);
        fireStateChange();
        LoadingWindow window = view.getLoadingWindow();
        window.setTitleAndText(LoadingWindow.SAVING_TITLE,
                                LoadingWindow.SAVING_MSG);
        UIUtilities.centerAndShow(window);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#declassifyImage(Map)
     */
    public void declassifyImage(Map categories)
    {
        if (model.getState() != READY)
            throw new IllegalStateException("This method should only be " +
                "invoked in the READY state.");
        if (categories == null) 
            throw new IllegalArgumentException("No categories.");
        
        model.fireDeclassification(categories);
        fireStateChange(); 
        LoadingWindow window = view.getLoadingWindow();
        window.setTitleAndText(LoadingWindow.SAVING_TITLE,
                                LoadingWindow.SAVING_MSG);
        UIUtilities.centerAndShow(window);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#saveClassification(boolean)
     */
    public void saveClassification(boolean b)
    {
        if (model.getState() != SAVE_CLASSIFICATION)
            throw new IllegalStateException("This method should only be " +
                "invoked in the SAVE_CLASSIFICATION state.");
        if (!b) {
            UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Classification", "The classification wasn't" +
                    "successful in the databasse. Please try again.");
        }
        view.getLoadingWindow().setVisible(false);
        view.removeAllFromWorkingPane();
        firePropertyChange(REMOVE_EDITOR_PROPERTY, Boolean.FALSE, Boolean.TRUE);
        model.setState(READY);
        fireStateChange();
    }
    
}
