/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.annotator.AnnotationsLoader;
import org.openmicroscopy.shoola.agents.util.annotator.AnnotationsSaver;
import org.openmicroscopy.shoola.agents.util.annotator.AnnotatorLoader;
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
* The Model component in the <code>Annotator</code> MVC triad.
* This class tracks the <code>Annotator</code>'s state and knows how to
* initiate data retrievals. It also knows how to store and manipulate
* the results. This class  provide  a suitable data loader. 
* The {@link AnnotatorComponent} intercepts the 
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
class AnnotatorModel
{
	
	/** 
	 * The classification mode, either {@link Annotator#BULK_ANNOTATE_MODE} or
	 * {@link Annotator#ANNOTATE_MODE}.
	 */
	private int				mode;
	
	/** Holds one of the state flags defined by {@link Annotator}. */
	private int				state;

	/** Collection of <code>DataObject</code>s to annotate. */
	private Set<DataObject>	toAnnotate;
	
	/** Collection of <code>DataObject</code>s already annotated. */
	private Set<DataObject>	annotated;
	
	/** The type of <code>DataObject</code>s to annotate. */
	private Class			type;
	
	/** The annotations retrieved for the annotated <code>DataObject</code>s. */
	private Map				annotations;
	
	/** The time reference object if any. */
	private TimeRefObject	timeRef;
	
	/** 
	 * Will either be a data loader or
	 * <code>null</code> depending on the current state. 
	 */
	private AnnotatorLoader	currentLoader;
	
	/** Reference to the component that embeds this model. */
	protected Annotator		component;

	/**
	 * Returns <code>true</code> if the <code>DataObject</code> has been 
	 * annotated by the current user, <code>false</code> otherwise.
	 * 
	 * @param data The <code>DataObject</code> to handle.
	 * @return See above.
	 */
	private boolean isObjectAnnotated(DataObject data)
	{
		Long n = null;
		if (data instanceof ImageData) 
			n = ((ImageData) data).getAnnotationCount();
		else if (data instanceof DatasetData) 
			n = ((DatasetData) data).getAnnotationCount();
		if (n == null) return false;
		return (n.longValue() != 0);
	}

	/**
	 * Checks if the passed mode is supported.
	 * 
	 * @param m The value to check.
	 */
	private void checkMode(int m)
	{
		switch (m) {
			case Annotator.ANNOTATE_MODE:
			case Annotator.BULK_ANNOTATE_MODE:
				break;
			default:
				throw new IllegalArgumentException("Annotate mode not " +
						"supported.");
		}
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param objects	Collection of <code>DataObject</code>s to annotate.
	 * @param mode		One of the following contants:
	 * 					{@link Annotator#BULK_ANNOTATE_MODE} or
	 * 					{@link Annotator#ANNOTATE_MODE}.
	 * @param type		The type of node, either <code>ImageData</code> or
	 * 					either <code>DatasetData</code>
	 */
	AnnotatorModel(Set objects, int mode, Class type)
	{
		checkMode(mode);
		this.mode = mode;
		toAnnotate = objects;
		if (mode == Annotator.ANNOTATE_MODE) {
			annotated = new HashSet<DataObject>();
			Iterator i = objects.iterator();
			DataObject data;
			while (i.hasNext()) {
				data = (DataObject) i.next();
				if (isObjectAnnotated(data)) annotated.add(data);
			}
		}
		state = DataHandler.NEW;
		this.type = type;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ref The time object.
	 */
	AnnotatorModel(TimeRefObject ref)
	{
		mode = Annotator.BULK_ANNOTATE_MODE;
		timeRef = ref;
		state = DataHandler.NEW;
	}
	
	/**
	 * Called by the <code>Annotator</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(Annotator component) { this.component = component; }
	
	/**
	 * Returns <code>true</code> if some objects have been previously
	 * annotated, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAnnotatedData()
	{ 
		if (annotated == null) return false;
		return (annotated.size() != 0);
	}
	
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
	 * Loads asynchronously the annotations for the annotated 
	 * <code>DataObject</code>s.
	 */
	void fireAnnotationsRetrieval()
	{
		if (mode == Annotator.BULK_ANNOTATE_MODE || annotated.size() == 0) {
			state = DataHandler.READY;
			return;
		}
		currentLoader = new AnnotationsLoader(component, annotated, type);
		currentLoader.load();
		state = DataHandler.LOADING;
	}
	
	/** 
	 * Saves asynchronously the annotation. 
	 * 
	 * @param data 		The annotation.
	 * @param object	The object to annotate if single annotation,
	 * 					or <code>null</code> if all displayed objects
	 * 					are annotated. 
	 */
	void fireAnnotationSaving(AnnotationData data, DataObject object)
	{ 
		switch (mode) {
			case Annotator.ANNOTATE_MODE:
				//if (annotated.size() == maxObjects)
				//	currentLoader = new AnnotationsSaver(component,  
				//							getAnnotatedObjects(data), mode);
				//else if (toAnnotate.size() == maxObjects) 
				if (object == null)
					currentLoader = new AnnotationsSaver(component, toAnnotate, 
														data, mode);
				else {
					Set<DataObject> nodes = new HashSet<DataObject>(1);
					nodes.add(object);
					currentLoader = new AnnotationsSaver(component, nodes, 
														data, mode);
				}
				//else 
				//	currentLoader = new AnnotationsSaver(component, 
				//						 getAnnotatedObjects(data), toAnnotate, 
				//						 data, mode);
				break;
	
			case Annotator.BULK_ANNOTATE_MODE:
				if (timeRef == null)
					currentLoader = new AnnotationsSaver(component, toAnnotate, 
													data, mode);
				else 
					currentLoader = new AnnotationsSaver(component, timeRef, 
															data);
		}
		currentLoader.load();
		state = DataHandler.SAVING;
	}
	
	/**
	 * Sets the annotations found for the annotated <code>DataObject</code>s
	 * 
	 * @param map The value to set.
	 */
	void setAnnotations(Map map)
	{
		HashMap<Long, List> sortedAnnotations;
		HashMap<Long, Map>	objectAnnotations = new HashMap<Long, Map>();
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
          sortedAnnotations = new HashMap<Long, List>();
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
          j = sortedAnnotations.keySet().iterator();
          List annotations;
          while (j.hasNext()) {
              ownerID = (Long) j.next();
              annotations = sortedAnnotations.get(ownerID);
              AnnotatorUtil.sortAnnotationByDate(annotations);
          }
          objectAnnotations.put(index, sortedAnnotations);
      }
      
      
		this.annotations = objectAnnotations;
      state = DataHandler.READY;
	}
	
	/**
	 * Returns the list of annotations linked to the <code>DataObject</code>
	 * identified by the passed ID.
	 * 
	 * @param annotateID The id of the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Map getAnnotationsFor(long annotateID)
	{
		return (Map) annotations.get(new Long(annotateID));
	}
	
	/**
	 * Returns the type of annotations to handle.
	 * 
	 * @return See above.
	 */
	AnnotationData getAnnotationType()
	{ 
		if (mode == Annotator.BULK_ANNOTATE_MODE)
			return new AnnotationData(AnnotationData.IMAGE_ANNOTATION); 
		if (type.equals(DatasetData.class))
			return new AnnotationData(AnnotationData.DATASET_ANNOTATION); 
		if (type.equals(ImageData.class))
			return new AnnotationData(AnnotationData.IMAGE_ANNOTATION); 
		return null;
	}
	
	/**
	 * Returns the annotation mode. One of the following values:
	 * {@link Annotator#BULK_ANNOTATE_MODE} or
	 * {@link Annotator#ANNOTATE_MODE}.
	 * 
	 * @return See above.
	 */
	int getAnnotationMode() { return mode; }
	
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
	 * Returns the collection of <code>DataObject</code>s to annotate.
	 * 
	 * @return See above.
	 */
	Set getSelectedObjects() { return toAnnotate; }
	
}
