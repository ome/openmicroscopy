/*
 * org.openmicroscopy.shoola.env.data.views.calls.AnnotationSaver
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

package org.openmicroscopy.shoola.env.data.views.calls;



//Java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
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
    private List      result;
    
    /**
     * Creates a {@link BatchCall} to create and update the specified 
     * annotation.
     * 
     * @param toUpdate  The annotated <code>DataObject</code>s.
     * @param toCreate  The a<code>DataObject</code>s to annotate.
     * @param data      The annotation.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateAndCreateAnnotation(final Map toUpdate, 
    							final Set toCreate, final AnnotationData data)
    {
        return new BatchCall("Remove dataset annotation.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = new ArrayList();
                result.add(os.updateAnnotationFor(toUpdate));
                result.add(os.createAnnotationFor(toCreate, data));
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to update the specified annotation.
     * 
     * @param objects   The annotated <code>DataObject</code>s.
     * @param data      The annotation to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateAnnotation(final Map objects)
    {
        return new BatchCall("Update image annotation.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.updateAnnotationFor(objects);
            }     
        };
    }
    
    /**
     * Creates a {@link BatchCall} to create an annotation.
     * 
     * @param nodeType	The type of the node. One out of the following types:
     *                  <code>DatasetData, ImageData</code>. 
     * @param objects   The annotated <code>DataObject</code>s.
     * @param data      The annotation to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall createAnnotation(final Set objects,
                                       final AnnotationData data)
    {
        return new BatchCall("Create dataset annotation.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.createAnnotationFor(objects, data);
            }
        };
    }
    
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
                OmeroDataService os = context.getDataService();
                result = new ArrayList(1);
                result.add(os.removeAnnotationFrom(object, data));
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
                OmeroDataService os = context.getDataService();
                result = new ArrayList(1);
                result.add(os.updateAnnotationFor(object, data));
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
                OmeroDataService os = context.getDataService();
                result = new ArrayList(1);
                result.add(os.createAnnotationFor(object, data));
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
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
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
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
     * 
     * @param toCreate	Collections of <code>DataObject</code>s to 
     * 					annotate. Mustn't be <code>null</code>.
     * @param data		The Annotation object. Mustn't be <code>null</code>.
     */
    public AnnotationSaver(Set toCreate, AnnotationData data)
    {
        if (data == null) throw new IllegalArgumentException("No annotation.");
        if (toCreate == null || toCreate.size() == 0) 
            throw new IllegalArgumentException("No DataObject to annotate.");
        saveCall = createAnnotation(toCreate, data);
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
     * @param toUpdate	The annotated <code>DataObject</code>s.
     * 					Mustn't be <code>null</code>.
     * @param toCreate	The <code>DataObject</code>s to annotate.
     * 					Mustn't be <code>null</code>.
     * @param data		The Annotation object.Mustn't be <code>null</code>.
     */
    public AnnotationSaver(Map toUpdate)
    {
        if (toUpdate == null || toUpdate.size() == 0) 
            throw new IllegalArgumentException("No DataObject to annotate.");
        saveCall = updateAnnotation(toUpdate);
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
     * @param toUpdate	The annotated <code>DataObject</code>s.
     * 					Mustn't be <code>null</code>.
     * @param toCreate	The <code>DataObject</code>s to annotate.
     * 					Mustn't be <code>null</code>.
     * @param data		The Annotation object.Mustn't be <code>null</code>.
     */
    public AnnotationSaver(Map toUpdate, Set toCreate, AnnotationData data)
    {
        if (toUpdate == null || toUpdate.size() == 0) 
            throw new IllegalArgumentException("No DataObject to annotate.");
        if (data == null) throw new IllegalArgumentException("No annotation.");
        if (toCreate == null || toCreate.size() == 0) 
            throw new IllegalArgumentException("No DataObject to annotate.");
        saveCall = updateAndCreateAnnotation(toUpdate, toCreate, data);
    }
    
}
