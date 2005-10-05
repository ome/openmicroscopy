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
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.AnnotationEditor;
import org.openmicroscopy.shoola.agents.hiviewer.CBDataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DatasetAnnotationLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ImageAnnotationLoader;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ImgDisplayAnnotationVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ImgNodeAnnotationVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ImgSetAnnotationVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

/** 
 * 
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
    
    /** The index of the search panel. */
    static final int                SEARCH_PANEL = 0;
    
    /** The index of the annotation panel. */
    static final int                ANNOTATION_PANEL = 1;
    
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
    
    /** Map of retrieved annotations */
    private Map                     annotations;
    
    /** 
     * Will either be a hierarchy loader, a thumbnail loader, or 
     * <code>null</code> depending on the current state. 
     */
    private CBDataLoader            currentLoader;
    
    /** Reference to the component that embeds this model. */
    protected ClipBoardComponent    component;
    

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
        annotatedObjectID = -1;
        annotatedObjectIndex = -1;
        annotationStatus = INITIAL;
    }
    
    /**
     * @param component
     */
    void initialize(ClipBoardComponent component)
    {
        if (component == null) throw new NullPointerException("No component");
        this.component = component;
    }
    
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
    void setPaneIndex(int i) 
    {
        if (i != SEARCH_PANEL && i != ANNOTATION_PANEL)
            throw new IllegalArgumentException("Pane index not valid.");
        if (paneIndex == i) return;
        paneIndex = i;
        if (paneIndex != ANNOTATION_PANEL) discardAnnotation();
    }
    
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
     * {@link HierarchyBrowsingView#DATASET_ANNOTATION}, 
     * {@link HierarchyBrowsingView#IMAGE_ANNOTATION}.
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
     * @param annotations The map with the annotations.
     */
    void setAnnotations(Map annotations) 
    {
        if (annotations == null)
            throw new NullPointerException("No annotations");
        this.annotations = annotations;
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
        ImgDisplayAnnotationVisitor visitor = null;
        switch (annotatedObjectIndex) {
            case ClipBoard.DATASET_ANNOTATION:
                visitor = new ImgSetAnnotationVisitor(getParentModel(), data,
                                                        annotatedObjectID);
                break;
            case ClipBoard.IMAGE_ANNOTATION:
                visitor = new ImgNodeAnnotationVisitor(getParentModel(), data,
                        annotatedObjectID);
                break;
        }
        if (visitor != null) getParentModel().getBrowser().accept(visitor);
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
            case ClipBoard.DATASET_ANNOTATION:
                currentLoader = new DatasetAnnotationLoader(component, nodeID);
                break;
            case ClipBoard.IMAGE_ANNOTATION:
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
        int index = -1;
        switch (annotatedObjectIndex) {
            case ClipBoard.DATASET_ANNOTATION:
                index = HierarchyBrowsingView.DATASET_ANNOTATION;
                break;
            case ClipBoard.IMAGE_ANNOTATION:
                index = HierarchyBrowsingView.IMAGE_ANNOTATION;
        } 
        currentLoader = new AnnotationEditor(component, 
                AnnotationEditor.CREATE, index, annotatedObjectID, txt);
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
        int index = -1;
        switch (annotatedObjectIndex) {
            case ClipBoard.DATASET_ANNOTATION:
                index = HierarchyBrowsingView.DATASET_ANNOTATION;
                break;
            case ClipBoard.IMAGE_ANNOTATION:
                index = HierarchyBrowsingView.IMAGE_ANNOTATION;
        }
        currentLoader = new AnnotationEditor(component, AnnotationEditor.UPDATE,
                index, annotatedObjectID, data);
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
        int index = -1;
        switch (annotatedObjectIndex) {
            case ClipBoard.DATASET_ANNOTATION:
                index = HierarchyBrowsingView.DATASET_ANNOTATION;
                break;
            case ClipBoard.IMAGE_ANNOTATION:
                index = HierarchyBrowsingView.IMAGE_ANNOTATION;
        }
        currentLoader = new AnnotationEditor(component, AnnotationEditor.DELETE,
                                        index, data);
        currentLoader.load();
        state = ClipBoard.EDIT_ANNOTATIONS;
    }
    
}
