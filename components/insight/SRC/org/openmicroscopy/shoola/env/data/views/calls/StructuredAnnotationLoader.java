/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import omero.gateway.SecurityContext;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;

import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.AnnotationType;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Retrieves the structures annotations related to a given object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class StructuredAnnotationLoader extends BatchCallTree {
    /** The types of annotations to load */
    private EnumSet<AnnotationType> types;

    /** The result of the call. */
    private Object result;

    /** Loads the specified experimenter groups. */
    private BatchCall loadCall;

    /** The security context. */
    private SecurityContext ctx;

    /**
     * Creates a {@link BatchCall} to load the specified annotation.
     * 
     * @param rootType
     *            The type of object the annotations are linked to e.g. Image.
     * @param rootIDs
     *            The collection of object's ids the annotations are linked to.
     * @param annotationType
     *            The type of annotation to load.
     * @param nsInclude
     *            The annotation's name space to include if any.
     * @param nsExlcude
     *            The annotation's name space to exclude if any.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadSpecifiedAnnotationLinkedTo(final Class<? extends DataObject> rootType,
            final List<Long> rootIDs,
            final EnumSet<AnnotationType> annotationTypes,
            final List<String> nsInclude, final List<String> nsExlcude) {
        return new BatchCall("Loading Specified Annotations") {
            public void doCall() throws Exception {
                
                for (AnnotationType annotationType : annotationTypes) {
                    if (annotationType.getPojoClass() != null) {
                        OmeroMetadataService os = context.getMetadataService();
                        Map<Long, Collection<AnnotationData>> tmpResult = os
                                .loadAnnotations(ctx, rootType, rootIDs,
                                        annotationType.getPojoClass(),
                                        nsInclude, nsExlcude);

                        if (result == null)
                            result = tmpResult;
                        else {
                            Map<Long, Collection<AnnotationData>> resultCasted = (Map<Long, Collection<AnnotationData>>) result;
                            for (Entry<Long, Collection<AnnotationData>> e : tmpResult
                                    .entrySet()) {
                                Collection<AnnotationData> annos = resultCasted
                                        .get(e.getKey());
                                if (annos == null) {
                                    annos = new ArrayList<AnnotationData>();
                                    resultCasted.put(e.getKey(), annos);
                                }
                                annos.addAll(e.getValue());
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the existing annotations of the
     * specified type related to the passed type of object.
     * 
     * @param ctx
     *            The security context.
     * @param annotationType
     *            The type of annotation to load.
     * @param userID
     *            The id of the user or <code>-1</code> if the id is not
     *            specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAnnotations(final List<SecurityContext> ctx,
            final AnnotationType annotationType, final long userID) {
        return new BatchCall("Loading Existing Annotations") {
            public void doCall() throws Exception {
                if (annotationType.getPojoClass() != null) {
                    OmeroMetadataService os = context.getMetadataService();
                    Iterator<SecurityContext> i = ctx.iterator();
                    List l = new ArrayList();
                    while (i.hasNext()) {
                        l.addAll(os.loadAnnotations(i.next(),
                                annotationType.getPojoClass(), null, userID));
                    }
                    result = l;
                }
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param object
     *            The type of the object.
     * @param userID
     *            The id of the user who tagged the object or <code>-1</code> if
     *            the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadStructuredData(final Object object, final long userID) {
        return new BatchCall("Loading Structured Data") {
            public void doCall() throws Exception {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadStructuredData(ctx, object, userID);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param data
     *            The objects.
     * @param userID
     *            The id of the user who tagged the object or <code>-1</code> if
     *            the user is not specified.
     * @param viewed
     *            Pass <code>true</code> to load the rendering settings related
     *            to the objects, <code>false<code> otherwise.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadStructuredData(final List<DataObject> data,
            final long userID) {
        return new BatchCall("Loading Structured Data") {
            public void doCall() throws Exception {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadStructuredData(ctx, data, userID);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the specified annotation.
     * 
     * @param annotationID
     *            The id of the annotation to load.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadAnnotation(final long annotationID) {
        return new BatchCall("Loading Annotation") {
            public void doCall() throws Exception {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadAnnotation(ctx, annotationID);
            }
        };
    }

    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() {
        add(loadCall);
    }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() {
        return result;
    }

    public StructuredAnnotationLoader(SecurityContext ctx,
            EnumSet<AnnotationType> types, List<DataObject> data, long userID) {
        this.ctx = ctx;
        if (types == null) {
            loadCall = loadStructuredData(data, userID);
        }
        else {
            Class objType = null;
            List<Long> ids = new ArrayList<Long>(data.size());
            for (DataObject obj : data) {
                if (objType == null)
                    objType = obj.getClass();
                else if (!objType.equals(obj.getClass()))
                    throw new IllegalArgumentException(
                            "The passed objects must be of the same type!");
                ids.add(obj.getId());
            }

            loadCall = loadSpecifiedAnnotationLinkedTo(objType, ids, types,
                    null, null);
        }
    }

    public StructuredAnnotationLoader(SecurityContext ctx,
            AnnotationType annotationType, long userID) {
        this.ctx = ctx;
        loadCall = loadAnnotations(Collections.singletonList(ctx),
                annotationType, userID);
    }

    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param ctx
     *            The security context.
     * @param annotationID
     *            The Id of the annotation to load.
     */
    public StructuredAnnotationLoader(SecurityContext ctx, long annotationID) {
        this.ctx = ctx;
        loadCall = loadAnnotation(annotationID);
    }

    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param ctx
     *            The security context.
     * @param annotationType
     *            The type of annotations to fetch.
     * @param userID
     *            The id of the user or <code>-1</code> if the id is not
     *            specified.
     */
    public StructuredAnnotationLoader(List<SecurityContext> ctx,
            AnnotationType annotationType, long userID) {
        loadCall = loadAnnotations(ctx, annotationType, userID);
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx
     *            The security context.
     * @param rootType
     *            The type of object the annotations are linked to e.g. Image.
     * @param rootIDs
     *            The collection of object's ids the annotations are linked to.
     * @param annotationTypes
     *            The type of annotations to load.
     * @param nsInclude
     *            The annotation's name space to include if any.
     * @param nsExlcude
     *            The annotation's name space to exclude if any.
     */
    public StructuredAnnotationLoader(SecurityContext ctx, Class<? extends DataObject> rootType,
            List<Long> rootIDs, EnumSet<AnnotationType> annotationTypes,
            List<String> nsInclude, List<String> nsExlcude) {
        this.ctx = ctx;
        loadCall = loadSpecifiedAnnotationLinkedTo(rootType, rootIDs,
                annotationTypes, nsInclude, nsExlcude);
    }
}
