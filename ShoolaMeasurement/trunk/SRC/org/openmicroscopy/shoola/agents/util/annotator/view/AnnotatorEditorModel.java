/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.annotator.view;



//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.annotator.AnnotationsEditorLoader;
import org.openmicroscopy.shoola.agents.util.annotator.AnnotationsEditorSaver;
import org.openmicroscopy.shoola.agents.util.annotator.AnnotatorEditorLoader;
import org.openmicroscopy.shoola.env.LookupNames;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * The Model component in the <code>AnnotatorEditor</code> MVC triad.
 * This class tracks the <code>AnnotatorEditor</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link AnnotatorEditorComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AnnotatorEditorModel
{

	/** Holds one of the state flags defined by {@link Annotator}. */
	private int						state;

	
    /** The annotations related to the currently edited {@link DataObject}. */ 
    private Map             		annotations;
    
    /** The DataObject to annotate. */
    private DataObject				dataObject;
    
    /** Flag to indicate if the object is annotated. */
    private boolean             	annotated;
    
    /** Collection of nodes that have to be annotated with the dataObject. */
    private Set<DataObject>			siblings;
    
	/** 
	 * Will either be a data loader or
	 * <code>null</code> depending on the current state. 
	 */
	private AnnotatorEditorLoader	currentLoader;
	
	/** Reference to the component that embeds this model. */
	protected AnnotatorEditor		component;
    
    /**
     * Creates a new instance.
     * 
     * @param dataObject The object to annotate.
     */
	AnnotatorEditorModel(DataObject dataObject)
	{
		this.dataObject = dataObject;
		state = DataHandler.NEW;
	}

	/**
	 * Called by the <code>Annotator</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(AnnotatorEditor component) { this.component = component; }
	
	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link Annotator} interface.  
	 */
	int getState() { return state; }    
 
	/**
	 * Sets the object in the {@link DataHandler#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		cancel();
		state = DataHandler.DISCARDED;
	}
 
	/**
	 * Sets the object in the {@link DataHandler#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
		if (currentLoader != null) {
			currentLoader.cancel();
			currentLoader = null;
		}
		state = DataHandler.READY;
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
    	if (annotations == null) return null;
        return (List) annotations.get(new Long(ownerID));
    }
    
    /**
     * Sorts and sets the retrieved annotations.
     * 
     * @param map The annotations to set.
     */
    void setAnnotations(Map map)
    {
    	HashMap<Long, List> sortedAnnotations = new HashMap<Long, List>();
        Set set;
        Long index;
        Iterator i = map.keySet().iterator();
        Iterator j;
        AnnotationData annotation;
        Long ownerID;
        List<AnnotationData> userAnnos;
        while (i.hasNext()) {
            index = (Long) i.next();
            set = (Set) map.get(index);
            j = set.iterator();
            while (j.hasNext()) {
                annotation = (AnnotationData) j.next();;
                ownerID = new Long(annotation.getOwner().getId());
                userAnnos = (List) sortedAnnotations.get(ownerID);
                if (userAnnos == null) {
                    userAnnos = new ArrayList<AnnotationData>();
                    sortedAnnotations.put(ownerID, userAnnos);
                }
                userAnnos.add(annotation);
            }
        }
        i = sortedAnnotations.keySet().iterator();
        List annotations;
        while (i.hasNext()) {
            ownerID = (Long) i.next();
            annotations = sortedAnnotations.get(ownerID);
            AnnotatorUtil.sortAnnotationByDate(annotations);
        }
        this.annotations = sortedAnnotations;
        state = DataHandler.READY;
    }
    
    /**
	 * Loads asynchronously the annotations for the annotated 
	 * <code>DataObject</code>s.
	 * 
	 * @param object The object to retrieve the annotation for.
	 */
	void fireAnnotationsRetrieval(DataObject object)
	{
		currentLoader = new AnnotationsEditorLoader(component, object);
		currentLoader.load();
		state = DataHandler.LOADING;
	}
	
	/**
	 * Loads asynchronously the annotations for the annotated 
	 * <code>DataObject</code>s.
	 */
	void fireAnnotationsRetrieval()
	{
		fireAnnotationsRetrieval(dataObject);
	}
	
	/**
     * Starts the asynchronous update of the specifed object and the creation 
     * of the annotation.
     * 
     * @param data      The annotation to create. 
     */
    void fireAnnotationCreate(AnnotationData data, int index)
    {
        state = DataHandler.SAVING;
        switch (index) {
			case AnnotatorEditor.SELECT_ONE:
			default:
				currentLoader = new AnnotationsEditorSaver(component, 
								dataObject, data, 
								AnnotationsEditorSaver.CREATE);
				break;
	
			case AnnotatorEditor.SELECT_ALL:
				if (siblings != null)
					currentLoader = new AnnotationsEditorSaver(component, 
							siblings, dataObject.getClass(), data, 
							AnnotationsEditorSaver.CREATE);
				else 
					currentLoader = new AnnotationsEditorSaver(component, 
							dataObject, data, 
							AnnotationsEditorSaver.CREATE);
				break;
		}
        
        currentLoader.load();
    }
    
    /**
     * Starts the asynchronous deletion of the annotation.
     * 
     * @param data Collection of annotations to delete.
     */
    void fireAnnotationDelete(List data)
    {
        state = DataHandler.SAVING;
        currentLoader = new AnnotationsEditorSaver(component, dataObject, data);
        currentLoader.load();
    }
    
    /**
     * Starts the asynchronous update of the specifed object and the update 
     * of the annotation.
     * 
     * @param data      The annotation to update. 
     */
    void fireAnnotationUpdate(AnnotationData data)
    {
    	state = DataHandler.SAVING;
        currentLoader = new AnnotationsEditorSaver(component, dataObject, data, 
        									AnnotationsEditorSaver.UPDATE);
        currentLoader.load();
    }
	
	 /**
     * Returns the most recent annotation of the currently edited 
     * <code>DataObject</code>.
     *  
     * @param index The index of the annotation.
     * @return See above.
     */
    AnnotationData getAnnotationData(int index)
    {
        long id = getUserDetails().getId();
        if (dataObject == null) return null;
        else if ((dataObject instanceof ImageData) || 
                (dataObject instanceof DatasetData)) {
        	List l = getAnnotations(id);
        	if (l == null || l.size() == 0) return null;
        	return (AnnotationData) l.get(index);
        }
        return null;
    }
    
    /**
     * Returns the most recent annotation done by the passed user.
     * 
     * @param ownerID The id of the experimenter.
     * @return See above.
     */
    AnnotationData getLastAnnotationFor(long ownerID)
    {
    	List l = getAnnotations(ownerID);
    	if (l == null || l.size() == 0) return null;
    	return (AnnotationData) l.get(0);
    }
    
    /**
     * Returns the current user's details.
     * 
     * @return See above.
     */
	ExperimenterData getUserDetails() 
	{
		return (ExperimenterData) AnnotatorFactory.getRegistry().lookup(
		        LookupNames.CURRENT_USER_DETAILS);
	}

	/**
	 * Sets the annotated value.
	 * 
	 * @param b The value to set.
	 */
	void setAnnotated(boolean b) { annotated = b; }

	/**
	 * Returns <code>true</code> if the annotation has been modified,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isAnnotated() { return annotated; }
	
	/**
	 * Returns the object to annotate.
	 * 
	 * @return See above.
	 */
	DataObject getDataObject() { return dataObject; }

	/**
	 * Returns the name of the data object.
	 * 
	 * @return See above.
	 */
	String getDataObjectName()
	{
		if (dataObject instanceof ImageData)
			return AnnotatorUtil.getPartialName(
						((ImageData) dataObject).getName());
		else if (dataObject instanceof ImageData)
			return ((DatasetData) dataObject).getName();
		return "";
	}
	
	/**
	 * Sets the state to the specified value.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state)  { this.state = state; }

	/**
	 * Sets the data object.
	 * 
	 * @param object The object to set.
	 */
	void setDataObject(DataObject object) { dataObject = object; }

	/**
	 * Sets the <code>DataObject</code>s that can be annotated with
	 * the edited one.
	 * 
	 * @param objects The value to set.
	 */
	void setSiblings(Set<DataObject> objects) { siblings = objects; }

	/**
	 * Returns the <code>DataObject</code>s that can be annotated with
	 * the edited one or <code>null</code> if none.
	 * 
	 * @return See above.
	 */
	Set getSiblings() { return siblings; }
	
}
