/*
 * org.openmicroscopy.shoola.agents.treeviewer.AnnotationEditor
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

package org.openmicroscopy.shoola.agents.treeviewer;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Creates, updates or deletes the specified annotation.
 * This class calls the <code>createAnnotation</code>, 
 * <code>deleteAnnotation</code> or <code>updateAnnotation</code> methods in the
 * <code>DataManagerView</code>.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class AnnotationEditor
	extends DataTreeViewerLoader
{

    /** Identifies the <code>CREATE</code> annotation action. */
    public static final int         CREATE = 0;
    
    /** Identifies the <code>UPDATE</code> annotation action. */
    public static final int         UPDATE = 1;
    
    /** Identifies the <code>DELETE</code> annotation action. */
    public static final int         DELETE = 2;

    /** One of the constants defined by this class. */
    private int             operation;
    
    /** The object to annotate. */
    private DataObject		annotatedObject;
    
    /** The annotation to create, update or delete. */
    private AnnotationData  data;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Controls if the specified <code>DataObject</code> can be annotated.
     * 
     * @param object The object to control.
     */
    private void checkAnnotatedObject(DataObject object)
    {
        if ((object instanceof ImageData) || (object instanceof DatasetData))
            return;
        throw new IllegalArgumentException("Data object cannot be " +
        		"annotated.");
    }
    
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
     * Returns the ID of the object to annotate.
     * 
     * @return See above.
     */
    private int getAnnotatedObjectID()
    {
        if (annotatedObject instanceof ImageData) 
            return ((ImageData) annotatedObject).getId();
        else if (annotatedObject instanceof DatasetData) 
            return ((DatasetData) annotatedObject).getId();
        return -1;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer            The TreeViewer this data loader is for.
     *                          Mustn't be <code>null</code>.
     * @param annotatedObject   The {@link DataObject} to annotate.
     * @param data              The {@link AnnotationData} to handle.
     * @param operation         The type of operation to perform.
     *                          One of the constants defined by this class.
     */
    public AnnotationEditor(TreeViewer viewer, DataObject annotatedObject, 
            				AnnotationData data, int operation)
    {
        super(viewer);   
        if (data == null)
            throw new IllegalArgumentException("No annotation.");
        checkAnnotatedObject(annotatedObject);
        checkOperation(operation);
        this.data = data;
        this.annotatedObject = annotatedObject;
        this.operation = operation;
    }
    
    /**
     * Saves the data.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        switch (operation) {
	        case CREATE:
	            handle = dmView.createAnnotation(annotatedObject.getClass(),
                        				getAnnotatedObjectID(), data.getText(),
                        				this);
	            break;
	        case UPDATE:
	            handle = dmView.updateAnnotation(annotatedObject.getClass(),
        								getAnnotatedObjectID(), data, this);
	            break;
	        case DELETE:
	            handle = dmView.deleteAnnotation(annotatedObject.getClass(),
	                    					data, this);
	            break;
        }
    }

    /** 
     * Cancels the data loading
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer.
     * @see DataTreeViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        viewer.setSaveResult(annotatedObject, TreeViewer.UPDATE_OBJECT); 
    }
    
}
