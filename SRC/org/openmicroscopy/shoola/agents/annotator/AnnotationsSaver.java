/*
 * org.openmicroscopy.shoola.agents.annotator.AnnotationsSaver 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.annotator;




//Java imports
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.annotator.view.Annotator;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.AnnotationData;

/** 
 * Creates or updates the specified annotation.
 * This class calls the <code>createAnnotation</code>, 
 * <code>updateAndCreateAnnotation</code> or <code>updateAnnotation</code>
 * methods in the <code>AnnotatorView</code>.
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
	
	/** Collection of <code>DataObject</code>s. */
	private Map				toUpdate;
	
	/** Collection of <code>DataObject</code>s to annotate. */
	private Set				toAnnotate;
	
	/** The annotation data. */
	private AnnotationData annotation;
	
	/** One of the constants defined by this class. */
	private int				index;
	
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The Annotator this loader is for.
	 * @param type			The type of annotation.
	 * @param toUpdate		Collection of <code>DataObject</code> to update
	 * 						the annotation for.
	 */
	public AnnotationsSaver(Annotator viewer, Map toUpdate)
	{
		super(viewer);
		if (toUpdate == null && toUpdate.size() == 0)
				throw new IllegalArgumentException("No data to save.");
		index = UPDATE;
		this.toUpdate = toUpdate;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The Annotator this loader is for.
	 * @param toUpdate		Collection of <code>DataObject</code> to update
	 * 						the annotation for.
	 * @param toAnnotate	Collection of <code>DataObject</code>s to annotate.
	 * @param annotation	The annotation. 
	 */
	public AnnotationsSaver(Annotator viewer, Map toUpdate,
							Set toAnnotate, AnnotationData annotation)
	{
		super(viewer);
		if (toUpdate == null && toUpdate.size() == 0)
				throw new IllegalArgumentException("No data to save.");
		if (annotation == null)
			throw new IllegalArgumentException("No annotation.");
		if (toAnnotate == null && toAnnotate.size() == 0)
			throw new IllegalArgumentException("No data to save.");
		index = UPDATE_AND_CREATE;
		this.toUpdate = toUpdate;
		this.annotation = annotation;
		this.toAnnotate = toAnnotate;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The Annotator this loader is for.
	 * @param toAnnotate	Collection of <code>DataObject</code>s to annotate.
	 * @param annotation	The annotation.
	 */
	public AnnotationsSaver(Annotator viewer, Set toAnnotate, 
							AnnotationData annotation)
	{
		super(viewer);
		if (annotation == null)
			throw new IllegalArgumentException("No annotation.");
		if (toAnnotate == null && toAnnotate.size() == 0)
			throw new IllegalArgumentException("No data to save.");
		index = CREATE;	
		this.annotation = annotation;
		this.toAnnotate = toAnnotate;
	}
	
	/** 
     * Cancels the data loading. 
     * @see AnnotatorLoader#laod()
     */
	public void load()
	{
		switch (index) {
			case CREATE:
				handle = aView.createAnnotation(toAnnotate, annotation, this);
				break;
			case UPDATE:
				handle = aView.updateAnnotation(toUpdate, this);
				break;
			case UPDATE_AND_CREATE:
				handle = aView.updateAndCreateAnnotation(toUpdate, 
										toAnnotate, annotation, this);
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
        if (viewer.getState() == Annotator.DISCARDED) return; 
        viewer.saveAnnotations((List) result);
    }

}
