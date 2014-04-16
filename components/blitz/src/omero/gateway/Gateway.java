/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.gateway;

import static omero.gateway.util.GatewayUtils.convertAnnotation;
import static omero.gateway.util.GatewayUtils.convertPojos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import omero.api.IContainerPrx;
import omero.api.IMetadataPrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.sys.Parameters;
import pojos.DataObject;
import pojos.util.PojoMapper;

@SuppressWarnings("unchecked")
public class Gateway extends ConnectionManager {

    /**
     * Retrieves hierarchy trees rooted by a given node. i.e. the requested node
     * as root and all of its descendants. The annotation for the current user
     * is also linked to the object. Annotations are currently possible only for
     * Image and Dataset. Wraps the call to the
     * {@link IPojos#loadContainerHierarchy(Class, List, Map)} and maps the
     * result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param ctx
     *            The security context, necessary to determine the service.
     * @param rootType
     *            The top-most type which will be searched for Can be
     *            <code>Project</code>. Mustn't be <code>null</code>.
     * @param rootIDs
     *            A set of the IDs of top-most containers. Passed
     *            <code>null</code> to retrieve all container of the type
     *            specified by the rootNodetype parameter.
     * @param options
     *            The Options to retrieve the data.
     * @return A set of hierarchy trees.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#loadContainerHierarchy(Class, List, Map)
     */
    public Set loadContainerHierarchy(SecurityContext ctx, Class rootType,
            List rootIDs, Parameters options) throws DSOutOfServiceException,
            DSAccessException {
        Connector c = getConnector(ctx, true, false);
        try {
            IContainerPrx service = c.getPojosService();
            return PojoMapper.asDataObjects(service.loadContainerHierarchy(
                    convertPojos(rootType).getName(), rootIDs, options));
        } catch (Throwable t) {
            handleException(t, "Cannot load hierarchy for " + rootType + ".");
        }
        return Collections.emptySet();
    }

    /**
     * Retrieves hierarchy trees in various hierarchies that contain the
     * specified Images. The annotation for the current user is also linked to
     * the object. Annotations are currently possible only for Image and
     * Dataset. Wraps the call to the
     * {@link IPojos#findContainerHierarchies(Class, List, Map)} and maps the
     * result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param ctx
     *            The security context, necessary to determine the service.
     * @param rootNodeType
     *            top-most type which will be searched for Can be
     *            <code>Project</code> Mustn't be <code>null</code>.
     * @param leavesIDs
     *            Set of identifiers of the Images that sit at the bottom of the
     *            trees. Mustn't be <code>null</code>.
     * @param options
     *            Options to retrieve the data.
     * @return A <code>Set</code> with all root nodes that were found.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#findContainerHierarchies(Class, List, Map)
     */
    public Set findContainerHierarchy(SecurityContext ctx, Class rootNodeType,
            List leavesIDs, Parameters options) throws DSOutOfServiceException,
            DSAccessException {
        Connector c = getConnector(ctx, true, false);
        try {
            IContainerPrx service = c.getPojosService();
            return PojoMapper.asDataObjects(service.findContainerHierarchies(
                    convertPojos(rootNodeType).getName(), leavesIDs, options));
        } catch (Throwable t) {
            handleException(t, "Cannot find hierarchy for " + rootNodeType
                    + ".");
        }
        return Collections.emptySet();
    }

    /**
     * Loads all the annotations that have been attached to the specified
     * <code>rootNodes</code>. This method looks for all the <i>valid</i>
     * annotations that have been attached to each of the specified objects. It
     * then maps each <code>rootNodeID</code> onto the set of all annotations
     * that were found for that node. If no annotations were found for that
     * node, then the entry will be <code>null</code>. Otherwise it will be a
     * <code>Set</code> containing <code>Annotation</code> objects. Wraps the
     * call to the
     * {@link IMetadataPrx#loadAnnotations(String, List, List, List)} and maps
     * the result calling {@link PojoMapper#asDataObjects(Parameters)}.
     * 
     * @param ctx
     *            The security context.
     * @param nodeType
     *            The type of the rootNodes. Mustn't be <code>null</code>.
     * @param nodeIDs
     *            TheIds of the objects of type <code>rootNodeType</code>.
     *            Mustn't be <code>null</code>.
     * @param annotationTypes
     *            The collection of annotations to retrieve or passed an empty
     *            list if we retrieve all the annotations.
     * @param annotatorIDs
     *            The identifiers of the users for whom annotations should be
     *            retrieved. If <code>null</code>, all annotations are returned.
     * @param options
     *            Options to retrieve the data.
     * @return A map whose key is rootNodeID and value the <code>Set</code> of
     *         all annotations for that node or <code>null</code>.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#findAnnotations(Class, List, List, Map)
     */
    public Map loadAnnotations(SecurityContext ctx, Class nodeType,
            List nodeIDs, List<Class> annotationTypes, List annotatorIDs,
            Parameters options) throws DSOutOfServiceException,
            DSAccessException {
        List<String> types = new ArrayList<String>();
        if (annotationTypes != null && annotationTypes.size() > 0) {
            types = new ArrayList<String>(annotationTypes.size());
            Iterator<Class> i = annotationTypes.iterator();
            String k;
            while (i.hasNext()) {
                k = convertAnnotation(i.next());
                if (k != null)
                    types.add(k);
            }
        }
        Connector c = getConnector(ctx, true, false);
        try {
            IMetadataPrx service = c.getMetadataService();
            return PojoMapper.asDataObjects(service.loadAnnotations(
                    convertPojos(nodeType).getName(), nodeIDs, types,
                    annotatorIDs, options));
        } catch (Throwable t) {
            handleException(t, "Cannot find annotations for " + nodeType + ".");
        }
        return Collections.emptyMap();
    }

    /**
     * Loads the specified annotations.
     * 
     * @param ctx
     *            The security context.
     * @param annotationIds
     *            The annotation to load.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.s
     */
    public Set<DataObject> loadAnnotation(SecurityContext ctx,
            List<Long> annotationIds) throws DSOutOfServiceException,
            DSAccessException {
        if (annotationIds == null || annotationIds.size() == 0)
            return new HashSet<DataObject>();

        Connector c = getConnector(ctx, true, false);
        try {
            IMetadataPrx service = c.getMetadataService();
            return PojoMapper.asDataObjects(service
                    .loadAnnotation(annotationIds));
        } catch (Throwable t) {
            handleException(t, "Cannot find the annotations.");
        }
        return Collections.emptySet();
    }
}
