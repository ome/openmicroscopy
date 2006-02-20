/*
 * org.openmicroscopy.shoola.agents.treeviewer.ObjectAnnotationEditor
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
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Creates, updates or deletes the specified <code>DataObject</code>
 * and its related annotation. The <code>DataObject</code> is an instance of 
 * Dataset or Image.
 * This class calls the <code>updateObjectAndAnnotation</code> method in the
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
public class ObjectAnnotationEditor
	extends EditorLoader
{

    /** Identifies the <code>CREATE</code> annotation action. */
    public static final int         CREATE = DataManagerView.CREATE_ANNOTATION;
    
    /** Identifies the <code>UPDATE</code> annotation action. */
    public static final int         UPDATE = DataManagerView.UPDATE_ANNOTATION;
    
    /** Identifies the <code>DELETE</code> annotation action. */
    public static final int         DELETE = DataManagerView.DELETE_ANNOTATION;

    /** One of the constants defined by this class. */
    private int             op;
    
    /** The object to annotate. */
    private DataObject		annotatedObject;
    
    /** The annotation to create, update or delete. */
    private AnnotationData  data;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;
    
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
     * @param viewer            The Editor this data loader is for.
     *                          Mustn't be <code>null</code>.
     * @param annotatedObject   The {@link DataObject} to annotate.
     * @param data              The {@link AnnotationData}.
     * @param op                The type of operation to perform.
     *                          One of the constants defined by this class.
     */
    public ObjectAnnotationEditor(Editor viewer, DataObject annotatedObject, 
									AnnotationData data, int op)
    {
        super(viewer);
        if (annotatedObject == null)
            throw new IllegalArgumentException("No DataObject.");
        if (!(annotatedObject instanceof ImageData) &&
                !(annotatedObject instanceof DatasetData))
            throw new IllegalArgumentException("DataObject not supported.");
        if (data == null)
            throw new IllegalArgumentException("No Annotation.");
        checkOperation(op);
        this.annotatedObject = annotatedObject;
        this.data = data;
        this.op = op;
    }
    
    /**
     * Saves the data
     * @see EditorLoader#load()
     */
    public void load()
    {
        handle = dmView.updateObjectAndAnnotation(annotatedObject, data, op,
                									this);
    }

    /** 
     * Cancels the data loading
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Editor.DISCARDED) return;  //Async cancel.
        viewer.setSaveResult((DataObject) result, Editor.UPDATE_OBJECT);
    }

}
