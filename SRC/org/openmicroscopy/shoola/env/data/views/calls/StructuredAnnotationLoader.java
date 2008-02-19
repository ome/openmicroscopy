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

//Third-party libraries

//Application-internal dependencies
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.ExperimenterData;
import pojos.ImageData;

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

	/** Indicates to load the files attached to a given object. */
	public static final int ATTACHMENT = 0;
	
	/** Indicates to load the tags related to a given object. */
	public static final int TAG = 1;
	
	/** Indicates to load the url related to a given object. */
	public static final int URL = 2;
	
	/** Indicates to load the annotation related to a given object. */
	public static final int ANNOTATION = 3;
	
	/** Indicates to load the annotation related to a given object. */
	public static final int VIEWED_BY = 4;
	
    /** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to load the tags related to the object
     * identified by the class and the id.
     * 
     * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadTags(final Class type, final long id, 
    							final long userID)
    {
        return new BatchCall("Loading Tags") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.loadTags(type, id, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the files attached to the object
     * identified by the class and the id.
     * 
     * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAttachments(final Class type, final long id, 
    							final long userID)
    {
        return new BatchCall("Loading Tags") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.loadAttachments(type, id, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the urls linked to the object
     * identified by the class and the id.
     * 
     * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadUrls(final Class type, final long id, 
    							final long userID)
    {
        return new BatchCall("Loading Tags") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.loadAttachments(type, id, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the annotations related to the object
     * identified by the class and the id.
     * 
     * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAnnotations(final Class type, final long id, 
    							final long userID)
    {
        return new BatchCall("Loading Tags") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.loadTags(type, id, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the users who viewed 
     * the specified set of pixels and also retrieve the rating associated
     * to that set.
     * 
     * @param imageID	The id of the image.
     * @param pixelsID	The pixels set id.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadViewedBy(final long imageID, final long pixelsID)
    {
        return new BatchCall("Loading Tags") {
            public void doCall() throws Exception
            {
            	OmeroImageService rds = context.getImageService();
				Map settings = rds.getRenderingSettings(pixelsID);
				Map<ExperimenterData, Collection> 
					ratings = new HashMap<ExperimenterData, Collection>();
				if (settings != null) {
					 Iterator i = settings.keySet().iterator();
					 OmeroDataService os = context.getDataService();
					 ExperimenterData exp;
					 while (i.hasNext()) {
						 exp = (ExperimenterData) i.next();
						 ratings.put(exp, os.loadRatings(ImageData.class, 
								 				imageID, exp.getId()));
					}
				}
               result = ratings;
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
     * @param id		The id of the object.
     * @param userID	The id of the user or <code>-1</code> if the id 
     * 					is not specified.
     */
    public StructuredAnnotationLoader(int index, Class type, long id, 
    									long userID)
    {
    	switch (index) {
			case ATTACHMENT:
				loadCall = loadAttachments(type, id, userID);
				break;
			case TAG:
				loadCall = loadTags(type, id, userID);
				break;
			case URL:
				loadCall = loadUrls(type, id, userID);
				break;
			case ANNOTATION:
				loadCall = loadAnnotations(type, id, userID);
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
    	}
    }
    
    public StructuredAnnotationLoader(int index, long imageID, long pixelsID)
    {
    	if (index != VIEWED_BY)
    		throw new IllegalArgumentException("This constructor can " +
    				"only invoke with the VIEWED_BY index: "+index);
    	loadCall = loadViewedBy(imageID, pixelsID);
    }
    
}
