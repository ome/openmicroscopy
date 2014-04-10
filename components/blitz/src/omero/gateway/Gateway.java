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

import static omero.gateway.util.GatewayUtils.*;

import java.util.ArrayList;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.DataObject;
import pojos.TagAnnotationData;
import pojos.util.PojoMapper;

public class Gateway extends ConnectionManager {

    private Logger log = LoggerFactory.getLogger(Gateway.class.getName());

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
    public Set<DataObject> loadContainerHierarchy(SecurityContext ctx,
            Class rootType, List<Long> rootIDs, Parameters options)
            throws DSOutOfServiceException, DSAccessException {
        Connector c = getConnector(ctx, true, false);
        try {
            IContainerPrx service = c.getPojosService();
            return PojoMapper.asDataObjects(service.loadContainerHierarchy(
                    convertPojos(rootType).getName(), rootIDs, options));
        } catch (Throwable t) {
            handleException(t, "Cannot load hierarchy for " + rootType + ".");
        }
        return new HashSet<DataObject>();
    }
    
    /**
     * Counts the number of items in a collection for a given object.
     * Returns a map which key is the passed rootNodeID and the value is
     * the number of items contained in this object and
     * maps the result calling {@link PojoMapper#asDataObjects(Map)}.
     *
     * @param ctx The security context.
     * @param rootNodeType  The type of container.
     * @param property              One of the properties defined by this class.
     * @param ids           The identifiers of the objects.
     * @param options               Options to retrieve the data.
     * @param rootNodeIDs   Set of root node IDs.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     * @see IPojos#getCollectionCount(String, String, List, Map)
     */
    public Map getCollectionCount(SecurityContext ctx, Class rootNodeType,
                    String property, List ids, Parameters options)
            throws DSOutOfServiceException, DSAccessException
    {
    Connector c = getConnector(ctx, true, false);
            try {
                  IMetadataPrx service = c.getMetadataService();
                    IContainerPrx svc = c.getPojosService();
                    if (TagAnnotationData.class.equals(rootNodeType)) {
                            return service.getTaggedObjectsCount(ids, options);
                    }
                    String p = convertProperty(rootNodeType, property);
                    if (p == null) return null;
                    return PojoMapper.asDataObjects(svc.getCollectionCount(
                                    convertPojos(rootNodeType).getName(), p, ids, options));
            } catch (Throwable t) {
                    handleException(t, "Cannot count the collection.");
            }
            return new HashMap();
    }

    /**
     * Loads all the annotations that have been attached to the specified
     * <code>rootNodes</code>. This method looks for all the <i>valid</i>
     * annotations that have been attached to each of the specified objects. It
     * then maps each <code>rootNodeID</code> onto the set of all annotations
     * that were found for that node. If no annotations were found for that
     * node, then the entry will be <code>null</code>. Otherwise it will be a
     * <code>Set</code> containing <code>Annotation</code> objects.
     * Wraps the call to the
     * {@link IMetadataPrx#loadAnnotations(String, List, List, List)}
     * and maps the result calling {@link PojoMapper#asDataObjects(Parameters)}.
     *
     * @param ctx The security context.
     * @param nodeType      The type of the rootNodes.
     *                      Mustn't be <code>null</code>.
     * @param nodeIDs       TheIds of the objects of type
     *                      <code>rootNodeType</code>.
     *                      Mustn't be <code>null</code>.
     * @param annotationTypes The collection of annotations to retrieve or
     *                                                passed an empty list if we retrieve all the
     *                                                annotations.
     * @param annotatorIDs  The identifiers of the users for whom annotations
     *                                              should be retrieved. If <code>null</code>,
     *                                              all annotations are returned.
     * @param options       Options to retrieve the data.
     * @return A map whose key is rootNodeID and value the <code>Set</code> of
     *         all annotations for that node or <code>null</code>.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     * @see IPojos#findAnnotations(Class, List, List, Map)
     */
    public Map loadAnnotations(SecurityContext ctx, Class nodeType, List nodeIDs,
                    List<Class> annotationTypes, List annotatorIDs, Parameters options)
    throws DSOutOfServiceException, DSAccessException
    {
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
                    return PojoMapper.asDataObjects(
                                    service.loadAnnotations(convertPojos(nodeType).getName(),
                                                    nodeIDs, types, annotatorIDs, options));
            } catch (Throwable t) {
                    handleException(t, "Cannot find annotations for "+nodeType+".");
            }
            return new HashMap();
    }
}
