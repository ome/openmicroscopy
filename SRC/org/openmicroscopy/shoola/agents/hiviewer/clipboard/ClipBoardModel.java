/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardModel
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.AnnotationEditor;
import org.openmicroscopy.shoola.agents.hiviewer.CBDataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DatasetAnnotationLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ImageAnnotationLoader;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ImgDisplayAnnotationVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;

import pojos.AnnotationData;

/** 
 * The Model component in the <code>ClipBoard</code> MVC triad.
 * This class tracks the <code>ClipBoard</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. The {@link ClipBoardComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 * 
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClipBoardModel
{

    /** Identifies a <code>CREATE</code> annotation action. */
    static final int                CREATE = 100;
    
    /** Identifies a <code>UPDATE</code> annotation action. */
    static final int                UPDATE = 101;
    
    /** Identifies a <code>DELETE</code> annotation action. */
    static final int                DELETE = 102;
    
    /** Indicates that no action has been done on the selected annotation */
    private static final int        INITIAL = 103;
    
    /** Holds one of the state flags defined by {@link ClipBoard}. */
    private int                     state;
    
    /**
     * One of the following constants: 
     * {@link #CREATE}, {@link #UPDATE}, {@link #DELETE}.
     */
    private int                     annotationStatus;
    
    /** Reference to the {@link HiViewer}. */
    private HiViewer                parentModel;
    
    /** The Index of the selected pane. */
    private int                     paneIndex;
    
    /** The index corresponding to the type of annotations. */
    private int                     annotatedObjectIndex;
    
    /** The ID of the currently selected data object. */
    private int                     annotatedObjectID;
    
    /** Retrieved annotations for a specified image or dataset.*/
    private Map                     annotations;
    
    /** The {@link ViewerSorter} used to sort the annotations. */
    private ViewerSorter            sorter;
    
    /** 
     * Will either be a hierarchy loader, a thumbnail loader, or 
     * <code>null</code> depending on the current state. 
     */
    private CBDataLoader            currentLoader;
    
    /** Reference to the component that embeds this model. */
    protected ClipBoardComponent    component;
    

    /** Initializes the default values. */
    private void init()
    {
        annotatedObjectID = -1;
        annotatedObjectIndex = -1;
        annotationStatus = INITIAL;
        sorter = new ViewerSorter();
        sorter.setAscending(false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parentModel A reference to the {@link HiViewer}, viewed as 
     * the parentModel. Mustn't be null.
     */
    ClipBoardModel(HiViewer parentModel)
    {
        if (parentModel == null)
            throw new NullPointerException("No parent model.");
        this.parentModel = parentModel;
        init();
    }
    
    /**
     * Called by the <code>ClipBoard</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(ClipBoardComponent component)
    {
        if (component == null) throw new NullPointerException("No component");
        this.component = component;
    }
    
    /**
     * Returns the {@link HiViewer} model.
     * 
     * @return See below.
     */
    HiViewer getParentModel() { return parentModel; }
    
    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link ClipBoard} interface.  
     */
    int getState() { return state; }
    
    /**
     * Returns the index of the selected pane.
     * 
     * @return One of the flags defined by the {@link ClipBoard} interface.
     */
    int getPaneIndex() { return paneIndex; }
    
    /**
     * Sets the index of the selected tabbedPane.
     * 
     * @param i The index of the tabbedPane.
     */
    void setPaneIndex(int i)  { paneIndex = i; }
    
    /**
     * Returns the annotation status.
     * One of the following constant {@link #CREATE}, {@link #UPDATE},
     * {@link #DELETE}.
     * 
     * @return See above.
     */
    int getAnnotationStatus() { return annotationStatus; }
    
    /**
     * Returns the annotation index.
     * One of the following constant
     * {@link AnnotationEditor#DATASET_ANNOTATION}, 
     * {@link AnnotationEditor#IMAGE_ANNOTATION}.
     * 
     * @return See above.
     */
    int getAnnotatedObjectIndex() { return annotatedObjectIndex; }
    
    /** 
     * Returns the ID of the data object currently selected.
     * 
     * @return See above.
     */
    int getAnnotatedObjectID() { return annotatedObjectID; }
    
    /**
     * Returns the retrieved annotations.
     * 
     * @return See above.
     */
    Map getAnnotations() { return annotations; }
    
    /**
     * Sets the retrieved annotations.
     * 
     * @param map The map with the annotations.
     */
    void setAnnotations(Map map) 
    {
        if (map == null) throw new NullPointerException("No annotations");
        HashMap sortedAnnotations = new HashMap();
        Set set;
        Integer index;
        Iterator i = map.keySet().iterator();
        Iterator j;
        AnnotationData annotation;
        Integer ownerID;
        List userAnnos;
        while (i.hasNext()) {
            index = (Integer) i.next();
            set = (Set) map.get(index);
            j = set.iterator();
            while (j.hasNext()) {
                annotation = (AnnotationData) j.next();;
                ownerID = new Integer(annotation.getOwner().getId());
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
            ownerID = (Integer) i.next();
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
        state = ClipBoard.ANNOTATIONS_READY;
    }
    
    
    /**
     * Sets the object in the {@link HiViewer#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discardAnnotation()
    {
        if (currentLoader != null) {
            currentLoader.cancel();
            currentLoader = null;
        }
        state = ClipBoard.DISCARDED_ANNOTATIONS;
    }
     
    /**
     * Creates a visitor and updates the data object.
     */
    void updateNodeAnnotation()
    {
        if (annotationStatus == INITIAL) return;
        //Visit the tree.
        UserDetails details = getParentModel().getUserDetails();
        Map annotations = getAnnotations();
        List l = (List) annotations.get(new Integer(details.getUserID()));
        AnnotationData data = null;
        if (l != null) data = (AnnotationData) l.get(0);
        int algoType = -1;
        switch (annotatedObjectIndex) {
            case AnnotationEditor.DATASET_ANNOTATION:
                algoType = ImageDisplayVisitor.IMAGE_SET_ONLY;
                break;
            case AnnotationEditor.IMAGE_ANNOTATION:
                algoType = ImageDisplayVisitor.IMAGE_NODE_ONLY;
                break;
        }
        if (algoType != -1) {
            ImgDisplayAnnotationVisitor visitor = 
                new ImgDisplayAnnotationVisitor(getParentModel(), data,
                    annotatedObjectID);
            getParentModel().getBrowser().accept(visitor, algoType);
        }
        annotationStatus = INITIAL;
    }
    
    /**
     * Starts the asynchronous retrieval of the annotations 
     * and sets the state to {@link ClipBoard#LOADING_ANNOTATIONS}.
     * 
     * @param nodeID The id of the annotated hierarchy object. Either a
     * dataset or an image.
     * @param objectIndex
     */
    void fireAnnotationsLoading(int nodeID, int objectIndex)
    {
        //annotationStatus = -1;
        annotatedObjectID = nodeID;
        annotatedObjectIndex = objectIndex;
        switch (objectIndex) {
            case AnnotationEditor.DATASET_ANNOTATION:
                currentLoader = new DatasetAnnotationLoader(component, nodeID);
                break;
            case AnnotationEditor.IMAGE_ANNOTATION:
                currentLoader = new ImageAnnotationLoader(component, nodeID);
        }
        if (currentLoader != null) currentLoader.load();            
        state = ClipBoard.LOADING_ANNOTATIONS;
    }
    
    /**
     * Starts the asynchronous creation of an annotation.
     * 
     * @param txt The annotation.
     */
    void fireCreateAnnotation(String txt)
    {
        annotationStatus = CREATE;
        currentLoader = new AnnotationEditor(component, 
                AnnotationEditor.CREATE, annotatedObjectIndex,
                annotatedObjectID, txt);
        currentLoader.load();
        state = ClipBoard.EDIT_ANNOTATIONS;
    }
    
    /**
     * Starts the asynchronous update of the currently selected annotation.
     * 
     * @param data The selected annotation.
     */
    void fireUpdateAnnotation(AnnotationData data)
    {
        annotationStatus = UPDATE;
        currentLoader = new AnnotationEditor(component, AnnotationEditor.UPDATE,
                annotatedObjectIndex, annotatedObjectID, data);
        currentLoader.load(); 
        state = ClipBoard.EDIT_ANNOTATIONS;
    }

    /**
     * Starts the asynchronous deletion of the currently selected annotation.
     * 
     * @param data The selected annotation.
     */
    void fireDeleteAnnotation(AnnotationData data)
    {
        annotationStatus = DELETE;
        currentLoader = new AnnotationEditor(component, AnnotationEditor.DELETE,
                                        annotatedObjectIndex, data);
        currentLoader.load();
        state = ClipBoard.EDIT_ANNOTATIONS;
    }
    
}
