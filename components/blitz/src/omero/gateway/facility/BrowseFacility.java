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

import java.util.ArrayList;
import java.util.Collection;
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
import omero.model.Image;
import omero.model.Well;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.WellData;
import omero.gateway.util.PojoMapper;

/**
 * A {@link Facility} for browsing the data hierarchy and retrieving
 * {@link ProjectData}, {@link DatasetData}, etc.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class BrowseFacility extends Facility {

    /**
     * Creates a new instance
     * 
     * @param gateway
     *            Reference to the {@link Gateway}
     */
    BrowseFacility(Gateway gateway) {
        super(gateway);
    }

    /**
     * Retrieves hierarchy trees rooted by a given node.
     * i.e. the requested node as root and all of its descendants.
     *
     * @param ctx The security context.
     * @param rootType The type of node to handle.
     * @param userId The user's to retrieve the data to handle.
     * @return See above.
     * @throws DSOutOfServiceException  If the connection is broken, or logged in.
     */
    public Set<DataObject> loadHierarchy(SecurityContext ctx, Class rootType,
            long userId) throws DSOutOfServiceException {
        ParametersI param = new ParametersI();
        if (userId >= 0) {
            param.exp(omero.rtypes.rlong(userId));
        }
        param.orphan();
        param.leaves();
        return loadHierarchy(ctx, rootType, null, param);
    }

    /**
     * Retrieves hierarchy trees rooted by a given node.
     * i.e. the requested node as root and all of its descendants.
     *
     * @param ctx The security context.
     * @param rootType The type of node to handle.
     * @param rootIDs The node's id.
     * @param options The retrieval options.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in.
     */
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
    public <T extends DataObject> T findObject(SecurityContext ctx,
            Class<T> klass, long id) throws DSOutOfServiceException,
            DSAccessException {
        return findObject(ctx, klass, id, false);
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
     * @param allGroups
     *            Pass <code>true</code> to take all groups into account,
     *            <code>false</code> to only use ctx's group
     * @return The last version of the object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public <T extends DataObject> T findObject(SecurityContext ctx,
            Class<T> klass, long id, boolean allGroups)
            throws DSOutOfServiceException, DSAccessException {
        String klassName = PojoMapper.getModelType(klass).getSimpleName();
        IObject obj = findIObject(ctx, klassName, id, allGroups);
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
     * @param allGroups Pass <code>true</code> to look for all groups
     * @return The last version of the object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public IObject findIObject(SecurityContext ctx, String klassName, long id,
            boolean allGroups) throws DSOutOfServiceException,
            DSAccessException {
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
            handleException(this, t,
                    "Cannot retrieve the requested object with "
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
            handleException(this, t,
                    "Cannot retrieve the requested object with "
                            + "object ID: " + o.getId());
        }
        return null;
    }

    /**
     * Retrieves the groups visible by the current experimenter.
     *
     * @param ctx
     *            The security context.
     * @param user
     *            The user currently logged in.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Set<GroupData> getAvailableGroups(SecurityContext ctx,
            ExperimenterData user) throws DSOutOfServiceException,
            DSAccessException {
        Set<GroupData> pojos = new HashSet<GroupData>();
        try {
            IQueryPrx service = gateway.getQueryService(ctx);
            // Need method server side.
            ParametersI p = new ParametersI();
            p.addId(user.getId());
            List<IObject> groups = service
                    .findAllByQuery(
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
            // GroupData pojoGroup;
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

    /** Load Projects */

    /**
     * Get all projects
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return A collection of {@link ProjectData}s
     */
    public Collection<ProjectData> getProjects(SecurityContext ctx) {
        try {
            ParametersI param = new ParametersI();

            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> projects = service.loadContainerHierarchy(PojoMapper
                    .getModelType(ProjectData.class).getName(), null, param);

            Collection<ProjectData> result = new ArrayList<ProjectData>(
                    projects.size());
            for (IObject proj : projects)
                result.add((ProjectData) PojoMapper.asDataObject(proj));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Get the projects for the given project ids
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ids
     *            The ids of the projects to fetch
     * @return A collection of {@link ProjectData}s
     */
    public Collection<ProjectData> getProjects(SecurityContext ctx,
            Collection<Long> ids) {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            List<IObject> projects = service.loadContainerHierarchy(PojoMapper
                    .getModelType(ProjectData.class).getName(), idsList, null);

            Collection<ProjectData> result = new ArrayList<ProjectData>(
                    projects.size());
            for (IObject proj : projects)
                result.add((ProjectData) PojoMapper.asDataObject(proj));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Get the projects of a certain user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the owner
     * @return A collection of {@link ProjectData}s
     */
    public Collection<ProjectData> getProjects(SecurityContext ctx, long ownerId) {
        try {
            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));

            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> projects = service.loadContainerHierarchy(PojoMapper
                    .getModelType(ProjectData.class).getName(), null, param);

            Collection<ProjectData> result = new ArrayList<ProjectData>(
                    projects.size());
            for (IObject proj : projects)
                result.add((ProjectData) PojoMapper.asDataObject(proj));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Get the projects for the given project ids which belong to a certain user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the owner
     * @param ids
     *            The ids of the projects to fetch
     * @return A collection of {@link ProjectData}s
     */
    public Collection<ProjectData> getProjects(SecurityContext ctx,
            long ownerId, Collection<Long> ids) {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));

            List<IObject> projects = service.loadContainerHierarchy(PojoMapper
                    .getModelType(ProjectData.class).getName(), idsList, param);

            Collection<ProjectData> result = new ArrayList<ProjectData>(
                    projects.size());
            for (IObject proj : projects)
                result.add((ProjectData) PojoMapper.asDataObject(proj));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /** Load Datasets */

    /**
     * Loads all datasets
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return A collection of {@link DatasetData}s
     */
    public Collection<DatasetData> getDatasets(SecurityContext ctx) {
        try {
            ParametersI param = new ParametersI();
            param.leaves();

            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> datasets = service.loadContainerHierarchy(PojoMapper
                    .getModelType(DatasetData.class).getName(), null, param);

            Collection<DatasetData> result = new ArrayList<DatasetData>(
                    datasets.size());
            for (IObject ds : datasets)
                result.add((DatasetData) PojoMapper.asDataObject(ds));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the datasets with the given ids
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ids
     *            The ids of the datasets to load
     * @return A collection of {@link DatasetData}s
     */
    public Collection<DatasetData> getDatasets(SecurityContext ctx,
            Collection<Long> ids) {
        try {
            ParametersI param = new ParametersI();
            param.leaves();

            IContainerPrx service = gateway.getPojosService(ctx);

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            List<IObject> datasets = service.loadContainerHierarchy(PojoMapper
                    .getModelType(DatasetData.class).getName(), idsList, param);

            Collection<DatasetData> result = new ArrayList<DatasetData>(
                    datasets.size());
            for (IObject ds : datasets)
                result.add((DatasetData) PojoMapper.asDataObject(ds));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the datasets for a particular user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the user
     * @return A collection of {@link DatasetData}s
     */
    public Collection<DatasetData> getDatasets(SecurityContext ctx, long ownerId) {
        try {
            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));
            param.leaves();

            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> datasets = service.loadContainerHierarchy(PojoMapper
                    .getModelType(DatasetData.class).getName(), null, param);

            Collection<DatasetData> result = new ArrayList<DatasetData>(
                    datasets.size());
            for (IObject ds : datasets)
                result.add((DatasetData) PojoMapper.asDataObject(ds));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the datasets with the given ids which belong to a particular user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the user
     * @param ids
     *            The ids of the datasets to load
     * @return A collection of {@link DatasetData}s
     */
    public Collection<DatasetData> getDatasets(SecurityContext ctx,
            long ownerId, Collection<Long> ids) {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));
            param.leaves();

            List<IObject> datasets = service.loadContainerHierarchy(PojoMapper
                    .getModelType(DatasetData.class).getName(), idsList, param);

            Collection<DatasetData> result = new ArrayList<DatasetData>(
                    datasets.size());
            for (IObject ds : datasets)
                result.add((DatasetData) PojoMapper.asDataObject(ds));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /** Load Screens */

    /**
     * Loads all screens
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return A collection of {@link ScreenData}s
     */
    public Collection<ScreenData> getScreens(SecurityContext ctx) {
        try {
            ParametersI param = new ParametersI();

            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> screens = service.loadContainerHierarchy(PojoMapper
                    .getModelType(ScreenData.class).getName(), null, param);

            Collection<ScreenData> result = new ArrayList<ScreenData>(
                    screens.size());
            for (IObject s : screens)
                result.add((ScreenData) PojoMapper.asDataObject(s));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the screens with the given ids
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ids
     *            The ids of the screens to load
     * @return A collection of {@link ScreenData}s
     */
    public Collection<ScreenData> getScreens(SecurityContext ctx,
            Collection<Long> ids) {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            List<IObject> screens = service.loadContainerHierarchy(PojoMapper
                    .getModelType(ScreenData.class).getName(), idsList, null);

            Collection<ScreenData> result = new ArrayList<ScreenData>(
                    screens.size());
            for (IObject s : screens)
                result.add((ScreenData) PojoMapper.asDataObject(s));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the screens for a particular user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the user
     * @return A collection of {@link ScreenData}s
     */
    public Collection<ScreenData> getScreens(SecurityContext ctx, long ownerId) {
        try {
            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));

            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> screens = service.loadContainerHierarchy(PojoMapper
                    .getModelType(ScreenData.class).getName(), null, param);

            Collection<ScreenData> result = new ArrayList<ScreenData>(
                    screens.size());
            for (IObject s : screens)
                result.add((ScreenData) PojoMapper.asDataObject(s));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the screens with the given ids which belong to a particular user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the user
     * @param ids
     *            The ids of the screens to load
     * @return A collection of {@link ScreenData}s
     */
    public Collection<ScreenData> getScreens(SecurityContext ctx, long ownerId,
            Collection<Long> ids) {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));

            List<IObject> screens = service.loadContainerHierarchy(PojoMapper
                    .getModelType(ScreenData.class).getName(), idsList, param);

            Collection<ScreenData> result = new ArrayList<ScreenData>(
                    screens.size());
            for (IObject s : screens)
                result.add((ScreenData) PojoMapper.asDataObject(s));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /** Load PLates */

    /**
     * Loads all plates
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return A collection of {@link PlateData}s
     */
    public Collection<PlateData> getPlates(SecurityContext ctx) {
        try {
            ParametersI param = new ParametersI();
            param.leaves();

            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> plates = service.loadContainerHierarchy(PojoMapper
                    .getModelType(PlateData.class).getName(), null, param);

            Collection<PlateData> result = new ArrayList<PlateData>(
                    plates.size());
            for (IObject p : plates)
                result.add((PlateData) PojoMapper.asDataObject(p));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the plates with the given ids
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ids
     *            The ids of the screens to load
     * @return A collection of {@link PlateData}s
     */
    public Collection<PlateData> getPlates(SecurityContext ctx,
            Collection<Long> ids) {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);

            ParametersI param = new ParametersI();
            param.leaves();

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            List<IObject> plates = service.loadContainerHierarchy(PojoMapper
                    .getModelType(PlateData.class).getName(), idsList, param);

            Collection<PlateData> result = new ArrayList<PlateData>(
                    plates.size());
            for (IObject p : plates)
                result.add((PlateData) PojoMapper.asDataObject(p));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the plates for a particular user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the user
     * @return A collection of {@link PlateData}s
     */
    public Collection<PlateData> getPlates(SecurityContext ctx, long ownerId) {
        try {
            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));
            param.leaves();

            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> plates = service.loadContainerHierarchy(PojoMapper
                    .getModelType(PlateData.class).getName(), null, param);

            Collection<PlateData> result = new ArrayList<PlateData>(
                    plates.size());
            for (IObject p : plates)
                result.add((PlateData) PojoMapper.asDataObject(p));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the plates with the given ids which belong to a particular user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the user
     * @param ids
     *            The ids of the plates to load
     * @return A collection of {@link PlateData}s
     */
    public Collection<PlateData> getPlates(SecurityContext ctx, long ownerId,
            Collection<Long> ids) {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));
            param.leaves();

            List<IObject> plates = service.loadContainerHierarchy(PojoMapper
                    .getModelType(PlateData.class).getName(), idsList, param);

            Collection<PlateData> result = new ArrayList<PlateData>(
                    plates.size());
            for (IObject p : plates)
                result.add((PlateData) PojoMapper.asDataObject(p));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the wells for a given plate 
     * @param ctx The {@link SecurityContext}
     * @param plateId The ID of the plate
     * @return A collection of {@link WellData}s
     */
    public Collection<WellData> getWells(SecurityContext ctx, long plateId) {
        Collection<WellData> result = new ArrayList<WellData>();
        try {
            IQueryPrx proxy = gateway.getQueryService(ctx);
            StringBuilder sb = new StringBuilder();
            ParametersI param = new ParametersI();
            param.addLong("plateID", plateId);
            sb.append("select well from Well as well ");
            sb.append("left outer join fetch well.plate as pt ");
            sb.append("left outer join fetch well.wellSamples as ws ");
            sb.append("left outer join fetch ws.plateAcquisition as pa ");
            sb.append("left outer join fetch ws.image as img ");
            sb.append("left outer join fetch img.pixels as pix ");
            sb.append("left outer join fetch pix.pixelsType as pt ");
            sb.append("where well.plate.id = :plateID");

            List<IObject> results = proxy.findAllByQuery(sb.toString(), param);
            Iterator<IObject> i = results.iterator();
            WellData well;
            while (i.hasNext()) {
                well = new WellData((Well) i.next());
                result.add(well);
            }
        } catch (Throwable t) {
            logError(this, "Could not load wells", t);
        }
        return result;
    }
    
    /** Load Images */

    /**
     * Loads all images of the logged in user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return A collection of {@link ImageData}s
     */
    public Collection<ImageData> getUserImages(SecurityContext ctx) {
        try {
            ParametersI param = new ParametersI();
            param.grp(omero.rtypes.rlong(ctx.getGroupID()));
            if (ctx.getExperimenter() >= 0) 
                param.exp(omero.rtypes.rlong(ctx.getExperimenter()));
            
            IContainerPrx service = gateway.getPojosService(ctx);
            List<Image> images = service.getUserImages(param);

            Collection<ImageData> result = new ArrayList<ImageData>(
                    images.size());
            for (Image img : images)
                result.add((ImageData) PojoMapper.asDataObject(img));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads a image
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param id
     *            The ids of the image to load
     * @return The {@link ImageData}
     */
    public ImageData getImage(SecurityContext ctx, long id) {
        return getImages(ctx, Collections.singleton(id)).iterator().next();
    }
    
    /**
     * Loads a image
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param id
     *            The ids of the image to load
     * @param params
     *            Custom parameters, can be <code>null</code>
     * @return The {@link ImageData}
     */
    public ImageData getImage(SecurityContext ctx, long id, ParametersI params) {
        return getImages(ctx, Collections.singleton(id), params).iterator()
                .next();
    }

    /**
     * Loads the images with the given ids
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ids
     *            The ids of the images to load
     * @return A collection of {@link ImageData}s
     */
    public Collection<ImageData> getImages(SecurityContext ctx,
            Collection<Long> ids) {
        return getImages(ctx, ids, null);
    }
    
    /**
     * Loads the images with the given ids
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ids
     *            The ids of the images to load
     * @param params
     *            Custom parameters, can be <code>null</code>
     * @return A collection of {@link ImageData}s
     */
    public Collection<ImageData> getImages(SecurityContext ctx,
            Collection<Long> ids, ParametersI params) {
        try {
            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            IContainerPrx service = gateway.getPojosService(ctx);
            List<Image> images = service.getImages(
                    PojoMapper.getModelType(ImageData.class).getName(),
                    idsList, params);

            Collection<ImageData> result = new ArrayList<ImageData>(
                    images.size());
            for (Image img : images)
                result.add((ImageData) PojoMapper.asDataObject(img));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Loads the images for a particular user
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param ownerId
     *            The id of the user
     * @param ids The image ids
     * @return A collection of {@link ImageData}s
     */
    public Collection<ImageData> getImages(SecurityContext ctx, long ownerId,
            Collection<Long> ids) {
        try {
            ParametersI param = new ParametersI();
            param.exp(omero.rtypes.rlong(ownerId));

            List<Long> idsList = new ArrayList<Long>(ids.size());
            for (long id : ids)
                idsList.add(id);

            IContainerPrx service = gateway.getPojosService(ctx);
            List<Image> images = service.getImages(
                    PojoMapper.getModelType(ImageData.class).getName(),
                    idsList, param);

            Collection<ImageData> result = new ArrayList<ImageData>(
                    images.size());
            for (Image img : images)
                result.add((ImageData) PojoMapper.asDataObject(img));

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Load all images belonging to particular datasets
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param datasetIds
     *            The ids of the datasets
     * @return A collection of {@link ImageData}s
     */
    public Collection<ImageData> getImagesForDatasets(SecurityContext ctx,
            Collection<Long> datasetIds) {
        try {
            Collection<ImageData> result = new ArrayList<ImageData>();

            Collection<DatasetData> datasets = getDatasets(ctx, datasetIds);
            for (DatasetData ds : datasets) {
                for (Object obj : ds.getImages()) {
                    if (obj instanceof ImageData)
                        result.add((ImageData) obj);
                }
            }

            return result;
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }

    /**
     * Load all images belonging to particular projects
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param projectIds
     *            The ids of the projects
     * @return A collection of {@link ImageData}s
     */
    public Collection<ImageData> getImagesForProjects(SecurityContext ctx,
            Collection<Long> projectIds) {
        try {
            Collection<ProjectData> projects = getProjects(ctx, projectIds);
            Collection<Long> dsIds = new ArrayList<Long>();
            for (ProjectData proj : projects) {
                for (DatasetData ds : proj.getDatasets())
                    dsIds.add(ds.getId());
            }
            return getImagesForDatasets(ctx, dsIds);
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptyList();
    }
}
