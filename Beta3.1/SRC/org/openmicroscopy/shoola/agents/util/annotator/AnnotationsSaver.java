/*
 * org.openmicroscopy.shoola.agents.util.annotator.AnnotationsSaver 
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
package org.openmicroscopy.shoola.agents.util.annotator;

//Java imports
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.annotator.view.Annotator;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.AnnotationData;

/** 
* Creates or updates the specified annotation.
* This class calls the <code>createAnnotation</code>, 
* <code>updateAndCreateAnnotation</code> or <code>updateAnnotation</code>
* methods in the <code>DataHandlerView</code>.
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
public class AnnotationsSaver
	extends AnnotatorLoader
{
	
	/** Indicates to create an annotation for the <code>DataObject</code>s. */
	private static final int	CREATE = 0;
	
	/** Indicates to update an annotation for the <code>DataObject</code>s. */
	private static final int	UPDATE = 1;
	
	/** 
	 * Indicates to create and update an annotation for the 
	 * <code>DataObject</code>s. 
	 */
	private static final int	UPDATE_AND_CREATE = 2;
	
	/** 
	 * The annotation mode, one of the following constants:
	 * {@link Annotator#BULK_ANNOTATE_MODE} or {@link Annotator#ANNOTATE_MODE}.
	 */
	private int				mode;
	
	/** Collection of <code>DataObject</code>s. */
	private Map				toUpdate;
	
	/** Collection of <code>DataObject</code>s to annotate. */
	private Set				toAnnotate;
	
	/** The annotation data. */
	private AnnotationData annotation;
	
	/** One of the constants defined by this class. */
	private int				index;
	
	/** The time reference object if any. */
	private TimeRefObject	timeRef;
	
	/** Handle to the async call so that we can cancel it. */
	private CallHandle  	handle;
	
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
	 * @param viewer		The Annotator this loader is for. 
	 * 						Mustn't be <code>null</code>.
	 * @param toUpdate		Collection of <code>DataObject</code> to update
	 * 						the annotation for. Mustn't be <code>null</code>.
	 * @param mode			One of the following constants:
	 * 						{@link Annotator#BULK_ANNOTATE_MODE} or
	 * 						{@link Annotator#ANNOTATE_MODE}.
	 */
	public AnnotationsSaver(Annotator viewer, Map toUpdate, int  mode)
	{
		super(viewer);
		if (toUpdate == null && toUpdate.size() == 0)
				throw new IllegalArgumentException("No data to save.");
		checkMode(mode);
		this.mode = mode;
		index = UPDATE;
		this.toUpdate = toUpdate;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The Annotator this loader is for.
	 * 						Mustn't be <code>null</code>.
	 * @param toUpdate		Collection of <code>DataObject</code> to update
	 * 						the annotation for. Mustn't be <code>null</code>.
	 * @param toAnnotate	Collection of <code>DataObject</code>s to annotate.
	 * 						Mustn't be <code>null</code>.
	 * @param annotation	The annotation. Mustn't be <code>null</code>.
	 * @param mode			One of the following constants:
	 * 						{@link Annotator#BULK_ANNOTATE_MODE} or
	 * 						{@link Annotator#ANNOTATE_MODE}.
	 */
	public AnnotationsSaver(Annotator viewer, Map toUpdate,
							Set toAnnotate, AnnotationData annotation, int mode)
	{
		super(viewer);
		if (toUpdate == null && toUpdate.size() == 0)
				throw new IllegalArgumentException("No data to save.");
		if (annotation == null)
			throw new IllegalArgumentException("No annotation.");
		if (toAnnotate == null && toAnnotate.size() == 0)
			throw new IllegalArgumentException("No data to save.");
		checkMode(mode);
		this.mode = mode;
		
		index = UPDATE_AND_CREATE;
		this.toUpdate = toUpdate;
		this.annotation = annotation;
		this.toAnnotate = toAnnotate;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The Annotator this loader is for. 
	 * 						Mustn't be <code>null</code>.
	 * @param toAnnotate	Collection of <code>DataObject</code>s to annotate.
	 * 						Mustn't be <code>null</code>.
	 * @param annotation	The annotation. Mustn't be <code>null</code>.
	 * @param mode			One of the following constants:
	 * 						{@link Annotator#BULK_ANNOTATE_MODE} or
	 * 						{@link Annotator#ANNOTATE_MODE}. 
	 */
	public AnnotationsSaver(Annotator viewer, Set toAnnotate, 
							AnnotationData annotation, int mode)
	{
		super(viewer);
		if (annotation == null)
			throw new IllegalArgumentException("No annotation.");
		if (toAnnotate == null && toAnnotate.size() == 0)
			throw new IllegalArgumentException("No data to save.");
		checkMode(mode);
		this.mode = mode;
		index = CREATE;	
		this.annotation = annotation;
		this.toAnnotate = toAnnotate;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The Annotator this loader is for. 
	 * 						Mustn't be <code>null</code>.
	 * @param ref			The time reference.	
	 * 						Mustn't be <code>null</code>.
	 * @param annotation	The annotation. Mustn't be <code>null</code>.
	 */
	public AnnotationsSaver(Annotator viewer, TimeRefObject ref, 
							AnnotationData annotation)
	{
		super(viewer);
		if (annotation == null)
			throw new IllegalArgumentException("No annotation.");
		if (ref == null)
			throw new IllegalArgumentException("No period specified.");
		this.mode = Annotator.BULK_ANNOTATE_MODE;
		this.annotation = annotation;
		timeRef = ref;
	}
	
	/** 
	 * Updates or creates annotations.
	 * @see AnnotatorLoader#load()
	 */
	public void load()
	{
		if (mode == Annotator.ANNOTATE_MODE) {
			switch (index) {
				case CREATE:
					handle = dhView.createAnnotation(toAnnotate, annotation, 
													this);
					break;
				case UPDATE:
					handle = dhView.updateAnnotation(toUpdate, this);
					break;
				case UPDATE_AND_CREATE:
					handle = dhView.updateAndCreateAnnotation(toUpdate, 
											toAnnotate, annotation, this);
			}
		} else {
			if (timeRef == null)
				handle = dhView.annotateChildren(toAnnotate, annotation, this);
			else 
				handle = dhView.annotateChildren(timeRef, annotation, this);
		}
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see AnnotatorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
	
	/**
	 * Feeds the result back to the viewer.
	 * @see AnnotatorLoader#handleResult(Object)
	 */
	public void handleResult(Object result)
	{
	    if (viewer.getState() == DataHandler.DISCARDED) return; 
	    viewer.saveAnnotations((List) result);
	}

}
