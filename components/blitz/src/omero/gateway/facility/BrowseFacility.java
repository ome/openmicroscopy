/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.facility;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.util.PojoMapper;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class BrowseFacility extends Facility {

    BrowseFacility(Gateway gateway) {
        super(gateway);
    }
    
    @SuppressWarnings("rawtypes")
    public Set<DataObject> loadHierarchy(SecurityContext ctx, Class rootType,
            long userId) throws DSOutOfServiceException {
        ParametersI param = new ParametersI();
        param.exp(omero.rtypes.rlong(userId));
        param.orphan();
        param.leaves();
        return loadHierarchy(ctx, rootType, null, param);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<DataObject> loadHierarchy(SecurityContext ctx, Class rootType,
            List<Long> rootIDs, Parameters options)
            throws DSOutOfServiceException {

        try {
            IContainerPrx service = gateway.getPojosService(ctx);
            return PojoMapper.asDataObjects(service.loadContainerHierarchy(
                    PojoMapper.getModelType(rootType).getName(), rootIDs,
                    options));
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptySet();
    }

    /**
     * Retrieves an updated version of the specified object.
     *
     * @param ctx
     *            The security context.
     * @param klass
     *            The type of object to retrieve.
     * @param id
     *            The object's id.
     * @return The last version of the object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    @SuppressWarnings("unchecked")
    public <T extends DataObject> T findObject(SecurityContext ctx, Class<T> klass, long id)
            throws DSOutOfServiceException, DSAccessException {
        String klassName = PojoMapper.getModelType(klass).getSimpleName();
        IObject obj = findIObject(ctx, klassName, id, false);
        return (T) PojoMapper.asDataObject(obj);
    }
    
    /**
     * Retrieves an updated version of the specified object.
     *
     * @param ctx
     *            The security context.
     * @param klassName
     *            The type of object to retrieve.
     * @param id
     *            The object's id.
     * @return The last version of the object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public IObject findIObject(SecurityContext ctx, String klassName, long id)
            throws DSOutOfServiceException, DSAccessException {
        return findIObject(ctx, klassName, id, false);
    }

    /**
     * Retrieves an updated version of the specified object.
     *
     * @param ctx
     *            The security context.
     * @param klassName
     *            The type of object to retrieve.
     * @param id
     *            The object's id.
     * @return The last version of the object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public IObject findIObject(SecurityContext ctx, String klassName, long id,
            boolean allGroups) throws DSOutOfServiceException, DSAccessException {
        try {
            Map<String, String> m = new HashMap<String, String>();
            if (allGroups) {
                m.put("omero.group", "-1");
            } else {
                m.put("omero.group", "" + ctx.getGroupID());
            }

            IQueryPrx service = gateway.getQueryService(ctx);
            return service.find(klassName, id, m);
        } catch (Throwable t) {
            handleException(this, t, "Cannot retrieve the requested object with "
                    + "object ID: " + id);
        }
        return null;
    }

    /**
     * Retrieves an updated version of the specified object.
     *
     * @param ctx
     *            The security context.
     * @param o
     *            The object to retrieve.
     * @return The last version of the object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public IObject findIObject(SecurityContext ctx, IObject o)
            throws DSOutOfServiceException, DSAccessException {
        if (o == null)
            return null;
        try {
            IQueryPrx service = gateway.getQueryService(ctx);
            return service.find(o.getClass().getName(), o.getId().getValue());
        } catch (Throwable t) {
            handleException(this, t, "Cannot retrieve the requested object with "
                    + "object ID: " + o.getId());
        }
        return null;
    }
    
    /**
     * Retrieves the groups visible by the current experimenter.
     *
     * @param ctx The security context.
     * @param loggedInUser The user currently logged in.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public Set<GroupData> getAvailableGroups(SecurityContext ctx,
            ExperimenterData user)
        throws DSOutOfServiceException, DSAccessException
    {
        Set<GroupData> pojos = new HashSet<GroupData>();
        try {
            IQueryPrx service = gateway.getQueryService(ctx);
            //Need method server side.
            ParametersI p = new ParametersI();
            p.addId(user.getId());
            List<IObject> groups = service.findAllByQuery(
                    "select distinct g from ExperimenterGroup as g "
                    + "join fetch g.groupExperimenterMap as map "
                    + "join fetch map.parent e "
                    + "left outer join fetch map.child u "
                    + "left outer join fetch u.groupExperimenterMap m2 "
                    + "left outer join fetch m2.parent p "
                    + "where g.id in "
                    + "  (select m.parent from GroupExperimenterMap m "
                    + "  where m.child.id = :id )", p);
            ExperimenterGroup group;
            //GroupData pojoGroup;
            Iterator<IObject> i = groups.iterator();
            while (i.hasNext()) {
                group = (ExperimenterGroup) i.next();
                pojos.add((GroupData) PojoMapper.asDataObject(group));
            }
            return pojos;
        } catch (Throwable t) {
            handleException(this, t, "Cannot retrieve the available groups ");
        }
        return pojos;
    }
}
