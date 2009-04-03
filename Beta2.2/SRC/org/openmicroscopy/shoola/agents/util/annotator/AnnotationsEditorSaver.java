/*
 * org.openmicroscopy.shoola.agents.util.annotator.AnnotationsEditorSaver 
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
package org.openmicroscopy.shoola.agents.util.annotator;


//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.AnnotationData;
import pojos.DataObject;

/** 
 * Creates, updates or deletes the specified annotation.
 * This class calls the <code>createAnnotation</code>, 
 * <code>updateAnnotation</code> or <code>deleteAnnotation</code>
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
public class AnnotationsEditorSaver 
	extends AnnotatorEditorLoader
{

    /** Identifies the <code>CREATE</code> annotation action. */
    public static final int	CREATE = 0;
    
    /** Identifies the <code>UPDATE</code> annotation action. */
    public static final int	UPDATE = 1;
    
    /** Identifies the <code>DELETE</code> annotation action. */
    public static final int	DELETE = 2;
	
	
	/** One of the constants defined by this class. */
	private int				index;
	
	/** Handle to the async call so that we can cancel it. */
	private CallHandle  	handle;
	
	/** The object to annotate. */
    private Set				toAnnotate;
    
    /** The annotation to create. */
    private AnnotationData  data;
    
    /** Collection of annotation to remove. */
    private List			toRemove;
    
    /** The annotated object. */
    private DataObject		annotatedObject;
    
    /**
     * Controls if the specified operation is supported.
     * 
     * @param op The type of operation.
     */
    private void checkOperation(int op)
    {
        switch (op) {
	        case CREATE:
	        case UPDATE:    
	        case DELETE:
	            return;
	        default:
	            throw new IllegalArgumentException("Operation not supported. ");
        }
    }
    
    /**
	 * Creates a new instance.
	 * 
	 * @param viewer	The Annotator this loader is for. 
	 * 					Mustn't be <code>null</code>.
	 * @param objects	The <code>DataObject</code>s to annotate.
	 * 					Mustn't be <code>null</code>.
	 * @param type		The type of <code>DataObject</code> 
	 * 					that can be annotated.
	 * @param data		The annotation for. Mustn't be <code>null</code>.
	 * @param index		One of the constants defined by this class.
	 */
	public AnnotationsEditorSaver(AnnotatorEditor viewer, Set objects, 
								Class type, AnnotationData data, int  index)
	{
		super(viewer);
		if (objects == null)
			throw new IllegalArgumentException("Object to annotate cannot be" +
					"null.");
		if (data == null)
			throw new IllegalArgumentException("Annotation cannot be null.");
		checkOperation(index);
		if (!checkAnnotationType(type))
			throw new IllegalArgumentException("Data object not supported.");
		this.index = index;
		this.data = data;
		toAnnotate = objects;
		if (index == DELETE) {
			toRemove = new ArrayList(1);
			toRemove.add(data);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The Annotator this loader is for. 
	 * 					Mustn't be <code>null</code>.
	 * @param object	The <code>DataObject</code> to annotate.
	 * 					Mustn't be <code>null</code>.
	 * @param data		The annotation for. Mustn't be <code>null</code>.
	 * @param index		One of the constants defined by this class.
	 */
	public AnnotationsEditorSaver(AnnotatorEditor viewer, DataObject object, 
								AnnotationData data, int  index)
	{
		super(viewer);
		if (object == null)
			throw new IllegalArgumentException("Object to annotate cannot be" +
					"null.");
		if (data == null)
			throw new IllegalArgumentException("Annotation cannot be null.");
		checkOperation(index);
		if (!checkAnnotationType(object.getClass()))
			throw new IllegalArgumentException("Data object not supported.");
		this.index = index;
		this.data = data;
		toAnnotate = new HashSet(1);
		toAnnotate.add(object);
		if (index == DELETE) {
			toRemove = new ArrayList(1);
			toRemove.add(data);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The Annotator this loader is for. 
	 * 					Mustn't be <code>null</code>.
	 * @param object	The <code>DataObject</code> to annotate.
	 * 					Mustn't be <code>null</code>.
	 * @param data		Collection of annotations to remove. 
	 * 					Mustn't be <code>null</code>.
	 * @param index		One of the constants defined by this class.
	 */
	public AnnotationsEditorSaver(AnnotatorEditor viewer, DataObject object, 
								List data)
	{
		super(viewer);
		if (object == null)
			throw new IllegalArgumentException("Object to annotate cannot be" +
					"null.");
		if (data == null)
			throw new IllegalArgumentException("Annotation cannot be null.");
		checkOperation(index);
		if (!checkAnnotationType(object.getClass()))
			throw new IllegalArgumentException("Data object not supported.");
		this.index = DELETE;
		toRemove = data;
		annotatedObject = object;
	}
	
	/** 
	 * Updates, creates or deletes the annotation depending on the
	 * value of the index.
	 * @see AnnotatorEditorLoader#load()
	 */
	public void load()
	{
		switch (index) {
			case UPDATE:
	        case CREATE:
	            handle = dhView.createAnnotation(toAnnotate, data, this);
	            break;
	        //case UPDATE:
	         //   handle = dhView.updateAnnotation(annotatedObject, data, this);
	         //   break;
	        case DELETE:
	            handle = dhView.deleteAnnotation(annotatedObject, toRemove, 
	            								this);
		}
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see AnnotatorEditorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
	
	/**
	 * Feeds the result back to the viewer.
	 * @see AnnotatorEditorLoader#handleResult(Object)
	 */
	public void handleResult(Object result)
	{
	    if (viewer.getState() == DataHandler.DISCARDED) return; 
	    //DataObject object = (DataObject) ((List) result).get(0);
	    viewer.setAnnotationSaved((List) result);
	}

}
