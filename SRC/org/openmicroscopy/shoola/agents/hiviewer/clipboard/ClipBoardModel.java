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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.annotator.AnnotationPane;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindPane;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * The Model component in the <code>ClipBoard</code> MVC triad.
 * This class tracks the <code>ClipBoard</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. The {@link ClipBoardComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk 
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClipBoardModel
{
    
    /** Holds one of the state flags defined by {@link ClipBoard}. */
    private int                     state;
    
    /** Reference to the {@link HiViewer}. */
    private HiViewer                parentModel;
    
    /** The index of the selected pane. */
    private int                     paneIndex;
    
    /** Retrieved annotations for a specified image or dataset.*/
    private Map                     annotations;
    
    /** The {@link ViewerSorter} used to sort the annotations. */
    private ViewerSorter            sorter;
    
    /** 
     * Will either be a hierarchy loader, a thumbnail loader, or 
     * <code>null</code> depending on the current state. 
     */
    private CBDataLoader            currentLoader;
    
    /** The map holding the {@link ClipBoardPane}s. */
    private HashMap                 cbPanes;
    
    /** Reference to the component that embeds this model. */
    protected ClipBoardComponent    component;
    

    /** Initializes the default values. */
    private void init()
    {
        setPaneIndex(ClipBoard.FIND_PANE);
        cbPanes = new HashMap();
        sorter = new ViewerSorter();
        sorter.setAscending(false);
    }
    
    /** Initializes the components composing the clipBoard. */
    private void createClipBoardPanes()
    {
        cbPanes.put(new Integer(ClipBoard.FIND_PANE), new FindPane(component));
        cbPanes.put(new Integer(ClipBoard.ANNOTATION_PANE), 
                        new AnnotationPane(component));
    }
    
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
        createClipBoardPanes();
    }
    
    /**
     * Returns the {@link ClipBoardPane} corresponding to the specified index,
     * <code>null</code> if there is no component corresponding to the index.
     * 
     * @param index The <code>ClipBoardPane</code> index.
     * @return See above.
     */
    ClipBoardPane getClipboardPane(int index) 
    {
        return (ClipBoardPane) cbPanes.get(new Integer(index));
    }
    
    /**
     * Returns the {@link ClipBoardPane}s composing the clip board.
     *  
     * @return See above.
     */
    Map getClipBoardPanes() { return cbPanes; }
    
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
     * Sets the state.
     *
     * @param state The state to set.
     */
    void setState(int state) { this.state = state; }

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
        Long index;
        Iterator i = map.keySet().iterator();
        Iterator j;
        AnnotationData annotation;
        Long ownerID;
        List userAnnos;
        while (i.hasNext()) {
            index = (Long) i.next();
            set = (Set) map.get(index);
            j = set.iterator();
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
     * Returns the current user's details. Helper method
     * 
     * @return See above.
     */
    ExperimenterData getUserDetails()
    {
        return parentModel.getUserDetails();
    }
    
    /**
     * Starts the asynchronous retrieval of the annotations 
     * and sets the state to {@link ClipBoard#LOADING_ANNOTATIONS}.
     * 
     * @param ho The <code>DataObject</code> to retrieve the annotation for.
     */
    void fireAnnotationsLoading(DataObject ho)
    {
        if (ho instanceof ImageData)
            currentLoader = new ImageAnnotationLoader(component, ho.getId());
        else if (ho instanceof DatasetData)
            currentLoader = new DatasetAnnotationLoader(component, ho.getId());    
        currentLoader.load();
        state = ClipBoard.LOADING_ANNOTATIONS;
    }
    
    /**
     * Starts the asynchronous creation of an annotation.
     * 
     * @param data The annotation.
     */
    void fireCreateAnnotation(AnnotationData data)
    {
        Object ho = component.getHierarchyObject();
        currentLoader = new AnnotationEditor(component, (DataObject) ho, data,
                                        AnnotationEditor.CREATE);
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
        Object ho = component.getHierarchyObject();
        currentLoader = new AnnotationEditor(component, (DataObject) ho, data,
                                        AnnotationEditor.UPDATE);
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
        Object ho = component.getHierarchyObject();
        currentLoader = new AnnotationEditor(component, (DataObject) ho, data,
                                        AnnotationEditor.DELETE);
        currentLoader.load();
        state = ClipBoard.EDIT_ANNOTATIONS;
    }

    /**
     * Returns the annotation of the currently edited <code>DataObject</code>.
     *  
     * @return See above.
     */
    AnnotationData getUserAnnotationData()
    {
        long id = getUserDetails().getId();
        Object ho = component.getHierarchyObject();
        if (ho == null) return null;
        else if ((ho instanceof ImageData) || 
                (ho instanceof DatasetData)) {
            List l = (List) annotations.get(new Long(id));
            return getLastAnnotation(l);
        }
        return null;
    }
    
}
