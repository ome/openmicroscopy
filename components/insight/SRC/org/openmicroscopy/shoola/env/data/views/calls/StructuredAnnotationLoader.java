/*
 * org.openmicroscopy.shoola.env.data.views.calls.StructuredAnnotationLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;

/** 
 * Retrieves the structures annotations related to a given object.
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
public class StructuredAnnotationLoader
	extends BatchCallTree
{

	/** Indicates to load the annotation related to a given object. */
	public static final int RATING = 10;
	
	/** Indicates to load the annotation related to a given object. */
	public static final int ROI_MEASUREMENT = 11;
	
	/** Indicates to load structured data */
	public static final int ALL = 1;
	
	/** Indicates to load the annotation identified by an Id. */
	public static final int SINGLE = 2;
	
    /** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to load the existing annotations of the 
     * specified type related to the passed type of object.
     * 
     * @param annotationType 	The type of annotation to load.
     * @param objectType		The type of object or <code>null</code>.
     * @param userID			The id of the user or <code>-1</code> if the id 
     * 							is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAnnotations(final Class annotationType,
    		final long userID)
    {
        return new BatchCall("Loading Existing annotations") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadAnnotations(annotationType, null, userID);
            }
        };
    }
 
    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadRatings(final Class type, final long id, 
    							final long userID)
    {
        return new BatchCall("Loading Ratings") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.loadRatings(type, id, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the measurement related to the object
     * identified by the class and the id.
     * 
     * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadROIMeasurements(final Class type, final long id, 
    							final long userID)
    {
        return new BatchCall("Loading Measurements") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.loadROIMeasurements(type, id, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param object	The type of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadStructuredData(final Object object, 
    									final long userID)
    {
        return new BatchCall("Loading Ratings") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.loadStructuredData(object, userID, true);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param data		The objects.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @param viewed	Pass <code>true</code> to load the rendering settings 
	 * 					related to the objects, <code>false<code>
	 * 					otherwise.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadStructuredData(final List<DataObject> data, 
    									final long userID, final boolean viewed)
    {
        return new BatchCall("Loading Ratings") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.loadStructuredData(data, userID, viewed);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the specified annotation.
     * 
     * @param annotationID	The id of the annotation to load.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAnnotation(final long annotationID)
    {
        return new BatchCall("Loading Ratings") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.loadAnnotation(annotationID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param type 		The type of the object.
     * @param ids		The collection of id of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadRatings(final Class type, final List<Long> ids, 
    							final long userID)
    {
        return new BatchCall("Loading Ratings") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.loadRatings(type, ids, userID);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param index		The index identifying the call. One of the constants
     * 					defined by this class.
     * @param type		The type of node the annotations are related to.
     * @param ids		Collection of the id of the object.
     * @param userID	The id of the user or <code>-1</code> if the id 
     * 					is not specified.
     */
    public StructuredAnnotationLoader(int index, Class type, List<Long> ids, 
    									long userID)
    {
    	switch (index) {
			case RATING:
				loadCall = loadRatings(type, ids, userID);
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param index The index identifying the call. One of the constants
     * 					defined by this class.
     * @param userID	The id of the user or <code>-1</code> if the id 
     * 					is not specified.
     * @param data		The collection of data objects to handle.
     * @param viewed
    
     */
    public StructuredAnnotationLoader(int index, List<DataObject> data, long 
    		userID, boolean viewed)
    {
    	switch (index) {
			case ALL:
				loadCall = loadStructuredData(data, userID, viewed);
		}
    }
    
    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param index		The index identifying the call. One of the constants
     * 					defined by this class.
     * @param object	The object to handle.
     * @param userID	The id of the user or <code>-1</code> if the id 
     * 					is not specified.
     */
    public StructuredAnnotationLoader(int index, Object object, 
    									long userID)
    {
    	if (object == null)
    		throw new IllegalArgumentException("Object not defined.");
    	switch (index) {
    		case ALL:
    			loadCall = loadStructuredData(object, userID);
    			break;
			case RATING:
				if (object instanceof DataObject) {
					DataObject ho = (DataObject) object;
					loadCall = loadRatings(object.getClass(), ho.getId(), 
							userID);
				}
				break;
			case ROI_MEASUREMENT:
				DataObject ho = (DataObject) object;
				loadCall = loadROIMeasurements(object.getClass(), ho.getId(), 
						userID);
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
    	}
    }
   
    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param annotationType	The type of annotations to fetch.
     * @param objectType		The type of object the annotations is related
     * 							to, or <code>null</code>.	
     * @param userID			The id of the user or <code>-1</code> if the id 
     * 							is not specified.
     */
    public StructuredAnnotationLoader(Class annotationType, long userID)
    {
    	loadCall = loadAnnotations(annotationType, userID);
    }
    
    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param annotationID The Id of the annotation to load.
     */
    public StructuredAnnotationLoader(long annotationID)
    {
    	loadCall = loadAnnotation(annotationID);
    }
    
}
