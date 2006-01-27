/*
 * org.openmicroscopy.shoola.env.data.views.calls.DataObjectEditor
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
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;

/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class DataObjectEditor
	extends BatchCallTree
{

    /** Indicates to create a <code>Annotation</code>. */
    public static final int CREATE = 0;
    
    /** Indicates to update the <code>Annotation</code>. */
    public static final int UPDATE = 1;
    
    /** Indicates to remove the <code>Annotation</code>. */
    public static final int REMOVE = 2;

    /** The batch call. */
    private BatchCall	call;
    
    /** The {@link DataObject} to update. */
    private DataObject	userObject;
    
    /** The result of the call. */
    private Object		result;
    
    private AnnotationData transformPojoAnnotationData(pojos.AnnotationData ad)
    {
        AnnotationData annotation = new AnnotationData(ad.getId(),
                                                    ad.getOwner().getId(),
                                                    ad.getLastModified());
        annotation.setAnnotation(ad.getText());
        return annotation;
    }
    
    private DatasetData daToDatasetData(pojos.DatasetData data)
    {
        DatasetData d = new DatasetData();
        d.setName(data.getName());
        d.setDescription(data.getDescription());
        return d;
    }
    
    private ImageData daToImageData(pojos.ImageData data)
    {
        ImageData i = new ImageData();
        i.setName(data.getName());
        i.setDescription(data.getDescription());
        return i;
    }
    
    /** 
     * Returns the ID of the {@link DataObject}. 
     * 
     * @return See above.
     */
    private int getObjectID()
    {
        if (userObject instanceof pojos.ImageData)
            return ((pojos.ImageData) userObject).getId();
        else if (userObject instanceof pojos.DatasetData)
            return ((pojos.DatasetData) userObject).getId();
        return -1;
    }
    
    /**
     * Creates a {@link BatchCall} to update the {@link #userObject}
     * and create an annotation.
     * 
     * @param data The annotation to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall create(final pojos.AnnotationData data)
    {
        return new BatchCall("Update DataObject and create annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                DataManagementService dms = context.getDataManagementService();
                final int id = getObjectID();
                if (userObject instanceof pojos.DatasetData) {
                    DatasetData d = 
                        daToDatasetData((pojos.DatasetData) userObject);
                    dms.updateDataset(d, null, null);
                    sts.createDatasetAnnotation(id, data.getText());
                } else if (userObject instanceof pojos.ImageData) {
                    ImageData i = daToImageData((pojos.ImageData) userObject);
                    dms.updateImage(i);
                    sts.createImageAnnotation(id, data.getText(), -1, -1);
                }
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the {@link #userObject}
     * and update the specified annotation.
     * 
     * @param data The annotation to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall update(final pojos.AnnotationData data)
    {
        return new BatchCall("Update DataObject and update annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                DataManagementService dms = context.getDataManagementService();
                final AnnotationData annotation = 
            		transformPojoAnnotationData(data);
                final int id = getObjectID();
                if (userObject instanceof pojos.DatasetData) {
                    DatasetData d = 
                        daToDatasetData((pojos.DatasetData) userObject);
                    dms.updateDataset(d, null, null);
                    sts.updateDatasetAnnotation(annotation, id);
                } else if (userObject instanceof pojos.ImageData) {
                    ImageData i = daToImageData((pojos.ImageData) userObject);
                    dms.updateImage(i);
                    sts.updateImageAnnotation(annotation, id);
                }
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the {@link #userObject}
     * and delete the specified annotation.
     * 
     * @param data The annotation to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall delete(final pojos.AnnotationData data)
    {
        return new BatchCall("Update DataObject and remove annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                DataManagementService dms = context.getDataManagementService();
                final AnnotationData annotation = 
                    		transformPojoAnnotationData(data);
                if (userObject instanceof pojos.DatasetData) {
                    DatasetData d = 
                        daToDatasetData((pojos.DatasetData) userObject);
                    dms.updateDataset(d, null, null);
                    sts.removeDatasetAnnotation(annotation);
                } else if (userObject instanceof pojos.ImageData) {
                    ImageData i = daToImageData((pojos.ImageData) userObject);
                    dms.updateImage(i);
                    sts.removeImageAnnotation(annotation);
                }
            }
        };
    }
    
    /**
     * Adds the {@link #call} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(call); }

    /**
     *  Returns the result of the call.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance. 
     * 
     * @param userObject	The {@link DataObject} to update. Must be an
     * 						instance of {@link DatasetData} or 
     * 						{@link ImageData}. Mustn't be <code>null</code>.	
     * @param data			The {@link AnnotationData} to handle. Mustn't be 
     * 			   			<code>null</code>.	
     * @param op 			The type of operation to perform. One of the
     * 						constants defined by this class.
     */
    public DataObjectEditor(DataObject userObject, pojos.AnnotationData data,
            				int op)
    {
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject.");
        if (!(userObject instanceof ImageData) && 
                (userObject instanceof DatasetData))
            throw new IllegalArgumentException("DataObject not supported.");
        result = userObject;
        if (data == null)
            throw new IllegalArgumentException("No Annotation.");
        switch (op) {
	        case CREATE:
	            call = create(data);
	            break;
	        case UPDATE:
	            call = update(data);
	            break;
	        case REMOVE:
	            call = delete(data);
	            break;
	        default:
	            throw new IllegalArgumentException("Operation not supported.");
        }
    }
    
}
