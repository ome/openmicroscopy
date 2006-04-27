/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorModel
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;



//Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.AnnotationEditor;
import org.openmicroscopy.shoola.agents.treeviewer.AnnotationLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ClassificationPathsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectCreator;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectEditor;
import org.openmicroscopy.shoola.agents.treeviewer.EditorLoader;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * The Model component in the <code>Editor</code> MVC triad.
 * This class tracks the <code>Editor</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. The {@link EditorComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class EditorModel
{
    
    /** Holds one of the state flags defined by {@link Editor}. */
    private int                 state;
    
    /** The currently edited {@link DataObject}. */
    private DataObject          hierarchyObject;
    
    /** 
     * Identifies the type of editor, either {@link Editor#CREATE_EDITOR}
     * or {@link Editor#PROPERTIES_EDITOR}.
     */
    private int                 editorType;
    
    /** Back pointer to the {@link TreeViewer}.*/
    private TreeViewer          parentModel;
    
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
    private EditorLoader        currentLoader;
    
    
    /** The annotations related to the currently edited {@link DataObject}. */ 
    private Map                 annotations;
    
    /** The images' annotations. */
    private Map                 leavesAnnotations;
    
    /** The set of retrieved classifications */
    private Set                 classifications;
    
    /** Flag to indicate if the object is annotated. */
    private boolean             annotated;
    
    /** Reference to the component that embeds this model. */
    protected Editor            component;
    
    /** 
     * Returns the last annotation.
     * 
     * @param annotations   Collection of {@link AnnotationData} linked to 
     *                      the currently edited <code>Dataset</code> or
     *                      <code>Image</code>.
     * @return See above.
     */
    private AnnotationData getLastAnnotation(List annotations)
    {
        if (annotations == null || annotations.size() == 0) return null;
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Timestamp t1 = ((AnnotationData) o1).getLastModified(),
                          t2 = ((AnnotationData) o2).getLastModified();
                long n1 = t1.getTime();
                long n2 = t2.getTime();
                int v = 0;
                if (n1 < n2) v = -1;
                else if (n1 > n2) v = 1;
                return v;
            }
        };
        Collections.sort(annotations, c);
        return (AnnotationData) annotations.get(annotations.size()-1);
    }
    
    /**
     * Creates a new instance and sets the state to {@link Editor#NEW}.
     * 
     * @param parentModel       Back pointer to the {@link TreeViewer} parent 
     *                          model. Mustn't be <code>null</code>. 
     * @param editorType        The type of editor this model is for.
     * @param hierarchyObject   The {@link DataObject} to edit.
     *                          Mustn't be <code>null</code>.
     */
    protected EditorModel(TreeViewer parentModel, int editorType,
                        DataObject hierarchyObject)
    {
        if (parentModel == null) 
            throw new NullPointerException("No parent model.");
        if (editorType != Editor.CREATE_EDITOR && 
                editorType != Editor.PROPERTIES_EDITOR)
            throw new IllegalArgumentException("editor not supported.");
        if (hierarchyObject == null)
            throw new IllegalArgumentException("No Data object.");
        state = Editor.NEW;
        this.editorType = editorType;
        this.parentModel = parentModel;
        this.hierarchyObject = hierarchyObject;
        annotated = false;
    }
    
    /**
     * Called by the <code>Editor</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Editor component) { this.component = component; }
    
    /**
     * Returns the {@link TreeViewer} parent model. This method should
     * only be invoked to register the control with property change.
     * 
     * @return See above.
     */
    TreeViewer getParentModel() { return parentModel; }
    
    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link Editor} interface.  
     */
    int getState() { return state; }   
    
    /**
     * Sets the current state.
     * 
     * @param state The state to set.
     */
    void setState(int state) { this.state = state; }
    
    /**
     * Returns the currently edited <code>DataObject</code>.
     * 
     * @return See above.
     */
    DataObject getHierarchyObject() { return hierarchyObject; }
    
    /** 
     * Returns the type of editor.
     * 
     * @return See above.
     */
    int getEditorType() { return editorType; }
    
    /**
     * Returns <code>true</code> if the DataObject is an Image,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasThumbnail()
    {
        if (hierarchyObject == null) return false;
        else if (hierarchyObject instanceof ImageData)
            return true;
        return false;
    }

    /**
     * Returns <code>true</code> if it's possible to annotate 
     * the currenlty edited <code>DataObject</code>, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotable()
    { 
        if (hierarchyObject == null) return false;
        else if ((hierarchyObject instanceof DatasetData) ||
                (hierarchyObject instanceof ImageData)) return true; 
        return false;
    }
    
    /**
     * Returns <code>true</code> if the <code>DataObject</code> has been 
     * classified, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isClassified()
    {
        if (hierarchyObject == null || !(hierarchyObject instanceof ImageData))
                return false;
        Integer i = ((ImageData) hierarchyObject).getClassificationCount();
        if (i == null || i.intValue() == 0) return false;
        return true;
    }
    
    /**
     * Returns <code>true</code> if the current user can modify the 
     * currently edited object, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isEditable()
    {
        ExperimenterData owner = getExperimenterData();
        if (owner == null) return false;
        return (owner.getId() == parentModel.getUserDetails().getId());
    }
    
    /**
     * Returns the information on the owner of the {@link DataObject}. 
     * 
     * @return See above.
     */
    ExperimenterData getExperimenterData()
    {
        if (hierarchyObject == null) return null;
        else if (hierarchyObject instanceof DatasetData)
            return ((DatasetData) hierarchyObject).getOwner();
        else if (hierarchyObject instanceof ProjectData)
            return ((ProjectData) hierarchyObject).getOwner();
        else if (hierarchyObject instanceof CategoryData)
            return ((CategoryData) hierarchyObject).getOwner();
        else if (hierarchyObject instanceof CategoryGroupData)
            return ((CategoryGroupData) hierarchyObject).getOwner();
        else if (hierarchyObject instanceof ImageData)
            return ((ImageData) hierarchyObject).getOwner();
        return null;
    }
    
    /** 
     * Returns the name of the currenlty edited <code>DataObject</code>.
     * 
     * @return See above.
     */
    String getDataObjectName()
    { 
        if (hierarchyObject == null) return null;
        else if (hierarchyObject instanceof DatasetData)
            return ((DatasetData) hierarchyObject).getName();
        else if (hierarchyObject instanceof ProjectData)
            return ((ProjectData) hierarchyObject).getName();
        else if (hierarchyObject instanceof CategoryData)
            return ((CategoryData) hierarchyObject).getName();
        else if (hierarchyObject instanceof CategoryGroupData)
            return ((CategoryGroupData) hierarchyObject).getName();
        else if (hierarchyObject instanceof ImageData)
            return ((ImageData) hierarchyObject).getName();
        return null;
    }
    
    /** 
     * Returns the description of the currenlty edited <code>DataObject</code>.
     * 
     * @return See above.
     */
    String getDataObjectDescription()
    { 
        if (hierarchyObject == null) return null;
        else if (hierarchyObject instanceof DatasetData)
            return ((DatasetData) hierarchyObject).getDescription();
        else if (hierarchyObject instanceof ProjectData)
            return ((ProjectData) hierarchyObject).getDescription();
        else if (hierarchyObject instanceof CategoryData)
            return ((CategoryData) hierarchyObject).getDescription();
        else if (hierarchyObject instanceof CategoryGroupData)
            return ((CategoryGroupData) hierarchyObject).getDescription();
        else if (hierarchyObject instanceof ImageData)
            return ((ImageData) hierarchyObject).getDescription();
        return null;
    }
    
    /**
     * Returns the annotation of the currently edited <code>DataObject</code>.
     *  
     * @return See above.
     */
    AnnotationData getAnnotationData()
    {
        long id = getUserDetails().getId();
        if (hierarchyObject == null) return null;
        else if ((hierarchyObject instanceof ImageData) || 
                (hierarchyObject instanceof DatasetData))
            return getLastAnnotation(getAnnotations(id));
        return null;
    }
    
    /**
     * Sets the object in the {@link Editor#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
        cancel();
        state = Editor.DISCARDED;
    }
    
    /** Cancels any ongoing data and sets the state to {@link Editor#READY}. */
    void cancel()
    {
        if (currentLoader != null) {
            currentLoader.cancel();
            currentLoader = null;
        }
        state = Editor.READY;
    }
    
    /**
     * Returns the sorted annotations.
     * 
     * @return See above.
     */
    Map getAnnotations() { return annotations; }
    
    /**
     * Returns the annotations made by the specified owner.
     * 
     * @param ownerID   The id of the owner.
     * @return See above.
     */
    List getAnnotations(long ownerID)
    {
        return (List) annotations.get(new Long(ownerID));
    }
    
    /**
     * Sorts and sets the retrieved annotations.
     * 
     * @param map The annotations to set.
     */
    void setAnnotations(Map map)
    {
        ViewerSorter sorter = new ViewerSorter();
        sorter.setAscending(false);
        HashMap sortedAnnotations = new HashMap();
        Set set;
        Long index;
        Iterator i = map.keySet().iterator();
        Iterator j;
        AnnotationData annotation;
        Long ownerID;
        List userAnnos;
        System.out.println("index: "+map.size());
        while (i.hasNext()) {
            index = (Long) i.next();
            set = (Set) map.get(index);
            j = set.iterator();
            System.out.println("index: "+index);
            while (j.hasNext()) {
                annotation = (AnnotationData) j.next();;
                ownerID = new Long(annotation.getOwner().getId());
                userAnnos = (List) sortedAnnotations.get(ownerID);
                if (userAnnos == null) {
                    userAnnos = new ArrayList();
                    sortedAnnotations.put(ownerID, userAnnos);
                }
                userAnnos.add(annotation);
            }
        }
        i = sortedAnnotations.keySet().iterator();
        List timestamps, annotations, results, list;
        HashMap m;
        Iterator k, l;
        AnnotationData data;
        while (i.hasNext()) {
            ownerID = (Long) i.next();
            annotations = (List) sortedAnnotations.get(ownerID);
            k = annotations.iterator();
            m = new HashMap(annotations.size());
            timestamps = new ArrayList(annotations.size());
            while (k.hasNext()) {
                data = (AnnotationData) k.next();
                m.put(data.getLastModified(), data);
                timestamps.add(data.getLastModified());
            }
            results = sorter.sort(timestamps);
            l = results.iterator();
            list = new ArrayList(results.size());
            while (l.hasNext())
                list.add(m.get(l.next()));
            sortedAnnotations.put(ownerID, list);
        }
        this.annotations = sortedAnnotations;
    }
    
    void setLeavesAnnotations(Map map)
    {
        
    }
    
    /** 
     * Browses or view the specified <code>DataObject</code>.
     * 
     * @param object The object to browse or view.
     */
    void browse(DataObject object)
    {
        if (object != null) {
            ViewCmd cmd = new ViewCmd(parentModel, object);
            cmd.execute();
        }
    }
    
    /**
     * Returns the user's details.
     * 
     * @return See above.
     */
    ExperimenterData getUserDetails() { return parentModel.getUserDetails(); }

    /**
     * Indicates if the classification has already been loaded.
     * @return  <code>true</code> if the data has been loaded,
     *          <code>false</code> otherwise.
     */
    boolean isClassificationLoaded() { return (classifications != null); }
    
    /**
     * Sets the retrieved classifications.
     * 
     * @param classifications The collection to set.
     */
    void setClassifications(Set classifications)
    { 
        state = Editor.READY;
        this.classifications = classifications;
    }
    
    /**
     * Returns the retrieved classifications, <code>null</code> if not
     * retrieved yet.
     * 
     * @return See above.
     */
    Set getClassifications() { return classifications; }

    /**
     * Returns the loading window.
     * 
     * @return See above.
     */
    JDialog getLoadingWindow() { return parentModel.getLoadingWindow(); }
    
    /**
     * Fires an asynchronous annotation retrieval for the currently edited 
     * <code>DataObject</code>.
     */
    void fireAnnotationsLoading()
    {
        state = Editor.LOADING_ANNOTATION;
        currentLoader = new AnnotationLoader(component, hierarchyObject);
        currentLoader.load();
    }
    
    /**
     * Fires an asynchronous retrieval of the CategoryGroup/Category paths 
     * containing the currently edited image.
     */
    void fireClassificationLoading()
    {
        state = Editor.LOADING_CLASSIFICATION;
        long imageID = ((ImageData) hierarchyObject).getId();
        currentLoader = new ClassificationPathsLoader(component, imageID);
        currentLoader.load();
    }

    /**
     * Starts the asynchronous creation of the specified object.
     * 
     * @param object The object to create.
     */
    void fireDataObjectCreation(DataObject object)
    {
        Browser b = parentModel.getSelectedBrowser();
        if (b == null) return;
        state = Editor.SAVE_EDITION;
        Object p =  b.getSelectedDisplay().getUserObject();
        if (p instanceof String) //root
            currentLoader = new DataObjectCreator(component, object, null);
        else currentLoader = new DataObjectCreator(component, object,
                                                    (DataObject) p);
        currentLoader.load();
    }
    
    /**
     * Starts the asynchronous update of the specified object.
     * 
     * @param object The object to update.
     */
    void fireDataObjectUpdate(DataObject object)
    {
        state = Editor.SAVE_EDITION;
        currentLoader = new DataObjectEditor(component, object);
        currentLoader.load();
    }
    
    /**
     * Starts the asynchronous update of the specifed object and the creation 
     * of the annotation.
     * 
     * @param object    The object to update.
     * @param data      The annotation to create. 
     */
    void fireAnnotationCreate(DataObject object, AnnotationData data)
    {
        state = Editor.SAVE_EDITION;
        currentLoader = new AnnotationEditor(component, object, data, 
                                        AnnotationEditor.CREATE);
        currentLoader.load();
    }
    
    /**
     * Starts the asynchronous update of the specifed object and the deletion 
     * of the annotation.
     * 
     * @param object    The object to update.
     * @param data      The annotation to delete. 
     */
    void fireAnnotationDelete(DataObject object, AnnotationData data)
    {
        state = Editor.SAVE_EDITION;
        currentLoader = new AnnotationEditor(component, object, data, 
                                            AnnotationEditor.DELETE);
        currentLoader.load();
    }
    
    /**
     * Starts the asynchronous update of the specifed object and the update 
     * of the annotation.
     * 
     * @param object    The object to update.
     * @param data      The annotation to update. 
     */
    void fireAnnotationUpdate(DataObject object, AnnotationData data)
    {
        state = Editor.SAVE_EDITION;
        currentLoader = new AnnotationEditor(component, object, data, 
                                            AnnotationEditor.UPDATE);
        currentLoader.load();
    }

    /**
     * Notifies the parent model that the {@link DataObject object} has been 
     * saved. 
     * @param object    The saved object.
     * @param operation The type of operation.
     */
    void setSaveResult(DataObject object, int operation)
    {
        state = Editor.READY;
        parentModel.onDataObjectSave(object, operation);
    }
    
    /**
     * Sets to <code>true</code> if the object is annotated, <code>false</code>
     * otherwise;
     * 
     * @param annotated Passed <code>true</code> if the object is annotated,
     *                  <code>false</code> otherwise.
     */
    void setAnnotated(boolean annotated) { this.annotated = annotated; }
    
    /**
     * Returns <code>true</code> if the object is annotated, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotated() { return annotated; }
    
}
