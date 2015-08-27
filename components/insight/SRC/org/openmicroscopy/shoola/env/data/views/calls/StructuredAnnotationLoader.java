/*
 * org.openmicroscopy.shoola.env.data.views.calls.StructuredAnnotationLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import omero.gateway.SecurityContext;
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
    private Object result;

    /** Loads the specified experimenter groups. */
    private BatchCall loadCall;

    /** The security context.*/
    private SecurityContext ctx;

    /**
     * Creates a {@link BatchCall} to load the specified annotation.
     * 
     * @param rootType The type of object the annotations are linked to e.g.
     * Image.
     * @param rootIDs The collection of object's ids the annotations are linked
     * to.
     * @param annotationType The type of annotation to load.
     * @param nsInclude The annotation's name space to include if any.
     * @param nsExlcude The annotation's name space to exclude if any.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadSpeficiedAnnotationLinkedTo(final Class<?> rootType,
            final List<Long> rootIDs, final Class<?> annotationType,
            final List<String> nsInclude, final List<String> nsExlcude)
    {
        return new BatchCall("Loading Specified Annotation") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadAnnotations(ctx, rootType, rootIDs,
                        annotationType, nsInclude, nsExlcude);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the existing annotations of the 
     * specified type related to the passed type of object.
     * 
     * @param annotationType The type of annotation to load.
     * @param userID The id of the user or <code>-1</code> if the id 
     * is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAnnotations(final Class annotationType,
            final long userID)
    {
        return new BatchCall("Loading Existing Annotations") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadAnnotations(ctx, annotationType, null, userID);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the existing annotations of the 
     * specified type related to the passed type of object.
     * 
     * @param ctx The security context.
     * @param annotationType The type of annotation to load.
     * @param userID The id of the user or <code>-1</code> if the id 
     * is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAnnotations(final List<SecurityContext> ctx,
            final Class annotationType, final long userID)
    {
        return new BatchCall("Loading Existing Annotations") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                Iterator<SecurityContext> i = ctx.iterator();
                List l = new ArrayList();
                while (i.hasNext()) {
                    l.addAll(os.loadAnnotations(i.next(), annotationType, null,
                            userID));
                }
                result = l;
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param type The type of the object.
     * @param id The id of the object.
     * @param userID The id of the user who tagged the object or
     * <code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadRatings(final Class type, final long id,
            final long userID)
    {
        return new BatchCall("Loading Ratings") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadRatings(ctx, type, id, userID);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the measurement related to the object
     * identified by the class and the id.
     * 
     * @param type The type of the object.
     * @param id The id of the object.
     * @param userID The id of the user who tagged the object or
     * <code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadROIMeasurements(final Class type, final long id,
            final long userID)
    {
        return new BatchCall("Loading Measurements") {
            public void doCall() throws Exception
            {
                OmeroImageService os = context.getImageService();
                result = os.loadROIMeasurements(ctx, type, id, userID);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param object The type of the object.
     * @param userID The id of the user who tagged the object or 
     * <code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadStructuredData(final Object object, final long userID)
    {
        return new BatchCall("Loading Structured Data") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadStructuredData(ctx, object, userID, true);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param data The objects.
     * @param userID The id of the user who tagged the object or
     *               <code>-1</code> if the user is not specified.
     * @param viewed Pass <code>true</code> to load the rendering settings
     *               related to the objects, <code>false<code> otherwise.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadStructuredData(final List<DataObject> data,
            final long userID, final boolean viewed)
    {
        return new BatchCall("Loading Structured Data") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadStructuredData(ctx, data, userID, viewed);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the specified annotation.
     * 
     * @param annotationID The id of the annotation to load.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAnnotation(final long annotationID)
    {
        return new BatchCall("Loading Annotation") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadAnnotation(ctx, annotationID);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param type The type of the object.
     * @param ids The collection of id of the object.
     * @param userID The id of the user who tagged the object or
     *            <code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadRatings(final Class type, final List<Long> ids,
            final long userID)
    {
        return new BatchCall("Loading Ratings") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadRatings(ctx, type, ids, userID);
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
     * @param ctx The security context.
     * @param index The index identifying the call. One of the constants
     *              defined by this class.
     * @param type The type of node the annotations are related to.
     * @param ids Collection of the id of the object.
     * @param userID The id of the user or <code>-1</code> if the id 
     *               is not specified.
     */
    public StructuredAnnotationLoader(SecurityContext ctx, int index,
            Class type, List<Long> ids, long userID)
    {
        this.ctx = ctx;
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
     * @param ctx The security context.
     * @param index The index identifying the call. One of the constants
     * defined by this class.
     * @param userID The id of the user or <code>-1</code> if the id
     * is not specified.
     * @param data The collection of data objects to handle.
     * @param viewed Pass <code>true</code> to load the rendering settings
     *               related to the objects, <code>false<code> otherwise.
     */
    public StructuredAnnotationLoader(SecurityContext ctx, int index,
            List<DataObject> data, long userID, boolean viewed)
    {
        this.ctx = ctx;
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
     * @param ctx The security context.
     * @param index The index identifying the call. One of the constants
     *              defined by this class.
     * @param object The object to handle.
     * @param userID The id of the user or <code>-1</code> if the id is not
     *               specified.
     */
    public StructuredAnnotationLoader(SecurityContext ctx, int index,
            Object object, long userID)
    {
        if (object == null)
            throw new IllegalArgumentException("Object not defined.");
        this.ctx = ctx;
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
     * @param ctx The security context.
     * @param annotationType The type of annotations to fetch.
     * @param userID The id of the user or <code>-1</code> if the id 
     *               is not specified.
     */
    public StructuredAnnotationLoader(SecurityContext ctx, Class annotationType,
            long userID)
    {
        this.ctx = ctx;
        loadCall = loadAnnotations(annotationType, userID);
    }

    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param ctx The security context.
     * @param annotationID The Id of the annotation to load.
     */
    public StructuredAnnotationLoader(SecurityContext ctx, long annotationID)
    {
        this.ctx = ctx;
        loadCall = loadAnnotation(annotationID);
    }

    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param ctx The security context.
     * @param annotationType The type of annotations to fetch.
     * @param userID The id of the user or <code>-1</code> if the id 
     *               is not specified.
     */
    public StructuredAnnotationLoader(List<SecurityContext> ctx,
            Class annotationType, long userID)
    {
        loadCall = loadAnnotations(ctx, annotationType, userID);
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param rootType The type of object the annotations are linked to e.g.
     *                 Image.
     * @param rootIDs The collection of object's ids the annotations are linked
     *                to.
     * @param annotationType The type of annotation to load.
     * @param nsInclude The annotation's name space to include if any.
     * @param nsExlcude The annotation's name space to exclude if any.
     */
    public StructuredAnnotationLoader(SecurityContext ctx, Class<?> rootType,
            List<Long> rootIDs, Class<?> annotationType, List<String> nsInclude,
            List<String> nsExlcude)
    {
        this.ctx = ctx;
        loadCall = loadSpeficiedAnnotationLinkedTo(rootType, rootIDs,
                annotationType, nsInclude, nsExlcude);
    }
}
