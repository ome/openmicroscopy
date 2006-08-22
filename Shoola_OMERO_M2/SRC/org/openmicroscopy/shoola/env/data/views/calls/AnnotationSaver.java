/*
 * org.openmicroscopy.shoola.env.data.views.calls.AnnotationSaver
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

package org.openmicroscopy.shoola.env.data.views.calls;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Command to save the annotation.
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
public class AnnotationSaver
    extends BatchCallTree
{
    
    /** Identifies the <code>CREATE</code> annotation action. */
    public static final int CREATE = 0;
    
    /** Identifies the <code>UPDATE</code> annotation action. */
    public static final int UPDATE = 1;
    
    /** Identifies the <code>DELETE</code> annotation action. */
    public static final int DELETE = 2;
    
    /** The save call. */
    private BatchCall       saveCall;

    /** The result of the query. */
    private DataObject      result;
    
    /**
     * Creates a {@link BatchCall} to remove the specified annotation.
     * 
     * @param object    The annotated <code>DataObject</code>.
     * @param data      The annotation to remove.
     * @return The {@link BatchCall}.
     */
    private BatchCall removeAnnotation(final DataObject object,
                                        final AnnotationData data)
    {
        return new BatchCall("Remove dataset annotation.") {
            public void doCall() throws Exception
            {
                OmeroService os = context.getOmeroService();
                result = os.removeAnnotationFrom(object, data);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified annotation.
     * 
     * @param object    The annotated <code>DataObject</code>.
     * @param data      The annotation to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateAnnotation(final DataObject object,
                                        final AnnotationData data)
    {
        return new BatchCall("Update image annotation.") {
            public void doCall() throws Exception
            {
                OmeroService os = context.getOmeroService();
                result = os.updateAnnotationFor(object, data);
            }     
        };
    }
    
    /**
     * Creates a {@link BatchCall} to create an annotation.
     * 
     * @param object    The annotated <code>DataObject</code>.
     * @param data      The annotation to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall createAnnotation(final DataObject object,
                                       final AnnotationData data)
    {
        return new BatchCall("Create dataset annotation.") {
            public void doCall() throws Exception
            {
                OmeroService os = context.getOmeroService();
                result = os.createAnnotationFor(object, data);
            }
        };
    }
    
    /**
     * Adds the {@link #saveCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(saveCall); }

    /**
     * Returns the result of the query.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }

    /**
     * Creates a new instance.
     * 
     * @param annotatedObject   The annotated <code>DataObject</code>.
     *                          Mustn't be <code>null</code>.
     * @param data              The Annotation object.
     *                          Mustn't be <code>null</code>.
     * @param algorithm         One of the constants defined by this class.
     */
    public AnnotationSaver(DataObject annotatedObject, AnnotationData data, 
                          int algorithm)
    {
        if (data == null) throw new IllegalArgumentException("No annotation.");
        if (annotatedObject == null) 
            throw new IllegalArgumentException("No DataObject to annotate.");
        if (!(annotatedObject instanceof DatasetData) &&
            !(annotatedObject instanceof ImageData))
            throw new IllegalArgumentException("DataObject cannot be " +
                                                "annotated.");
        switch (algorithm) {
            case DELETE:
                saveCall = removeAnnotation(annotatedObject, data);
                break;
            case UPDATE: 
                saveCall = updateAnnotation(annotatedObject, data);
                break;
            case CREATE:
                saveCall = createAnnotation(annotatedObject, data);
                break;
            default: 
                throw new IllegalArgumentException("Constructor should only" +
                        "be invoked to update or delete annotation.");
        }
    }
    
}
