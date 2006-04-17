/*
 * org.openmicroscopy.shoola.agents.hiviewer.AnnotationEditor
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

package org.openmicroscopy.shoola.agents.hiviewer;




//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Creates, deletes or updates the annotations linked to the specified Dataset 
 * or Image.
 * This class calls the <code>createAnnotation</code>, 
 * <code>updateAnnotation</code> or <code>deleteAnnotation</code> methods in the
 * <code>HierarchyBrowsingView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AnnotationEditor
    extends CBDataLoader
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
    private DataObject      annotatedObject;
    
    /** The annotation data object to update or delete. */
    private AnnotationData  data;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle      handle;
    
    /**
     * Controls if the action index is valid.
     * 
     * @param i The index to control.
     */
    private void checkActionIndex(int i)
    {
        switch (i) {
            case UPDATE:
            case DELETE:    
                return;
            default:
                throw new IllegalArgumentException("Action not supported: "+i);
        }
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param clipBoard         The viewer this data loader is for.
     *                          Mustn't be <code>null</code>.
     * @param annotatedObject   The {@link DataObject} to annotate.
     * @param data              The annotation to handle.
     * @param operation         The action index. One of the constants
     *                          defined by this class.
     */
    public AnnotationEditor(ClipBoard clipBoard, DataObject annotatedObject, 
                            AnnotationData data, int operation)
    {
        super(clipBoard);
        if (data == null) throw new IllegalArgumentException("No Annotation.");
        if (annotatedObject == null) 
            throw new IllegalArgumentException("No DataObject to annotate.");
        if (!(annotatedObject instanceof DatasetData) && 
                !(annotatedObject instanceof ImageData))
            throw new IllegalArgumentException("DataObject to annotate not " +
                    "supported.");
        checkActionIndex(operation);
        this.operation = operation;
        this.annotatedObject = annotatedObject;
        this.data = data;
    }
    
    /**
     * Creates, deletes or updates the annotation.
     * @see CBDataLoader#load()
     */
    public void load()
    {
        switch (operation) {
            case CREATE:
                handle = hiBrwView.createAnnotation(annotatedObject, data,
                                                    this);
                break;
            case UPDATE:
                handle = hiBrwView.updateAnnotation(annotatedObject, data, 
                                                    this);
                break;
            case DELETE:
                handle = hiBrwView.deleteAnnotation(annotatedObject, data, 
                                                    this);
        }
    }

    /** 
     * Cancels the data saving. 
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see CBDataLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        if (clipBoard.getState() == ClipBoard.DISCARDED_ANNOTATIONS) return;
        //Review that code.
        //clipBoard.manageAnnotationEditing(((Boolean) result).booleanValue());
    }

}
