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

import static omero.gateway.util.GatewayUtils.convertPojos;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import omero.api.IContainerPrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.sys.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.DataObject;
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

}
