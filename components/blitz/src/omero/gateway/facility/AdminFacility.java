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
import java.util.Iterator;
import java.util.List;

import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.sys.Roles;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.util.PojoMapper;

/**
 * {@link Facility} for handling admin issues, e. g. creating users, groups,
 * etc.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class AdminFacility extends Facility {

    Roles roles;

    AdminFacility(Gateway gateway) {
        super(gateway);
    }

    /**
     * Creates a group.
     */
    public GroupData createGroup(SecurityContext ctx, GroupData groupData,
            ExperimenterData owner, int permissions)
            throws DSOutOfServiceException, DSAccessException {

        try {
            IAdminPrx svc = gateway.getAdminService(ctx);
            ExperimenterGroup g = lookupGroup(ctx, groupData.getName());

            if (g != null)
                return null;

            g = new ExperimenterGroupI();
            g.setName(omero.rtypes.rstring(groupData.getName()));
            g.setLdap(omero.rtypes.rbool(false));
            g.setDescription(omero.rtypes.rstring(groupData.getDescription()));
            g.getDetails().setPermissions(createPermissions(permissions));
            long groupID = svc.createGroup(g);
            g = svc.getGroup(groupID);
            List<ExperimenterGroup> list = new ArrayList<ExperimenterGroup>();
            list.add(g);

            if (owner != null)
                svc.setGroupOwner(g, owner.asExperimenter());

            return (GroupData) PojoMapper.asDataObject(g);
        } catch (Exception e) {
            handleException(this, e, "Cannot create group and owner.");
        }
        return null;
    }

    /**
     * Creates an experimenter.
     */
    public ExperimenterData createExperimenter(SecurityContext ctx,
            ExperimenterData exp, String username, String password,
            List<GroupData> groups, boolean isAdmin, boolean isGroupOwner)
            throws DSOutOfServiceException, DSAccessException {

        try {
            IAdminPrx svc = gateway.getAdminService(ctx);
            ExperimenterGroup g = null;
            List<ExperimenterGroup> l = new ArrayList<ExperimenterGroup>();
            if (groups != null && groups.size() >= 1) {
                g = groups.get(0).asGroup();
                Iterator<GroupData> j = groups.iterator();
                while (j.hasNext())
                    l.add(((GroupData) j.next()).asGroup());
            }
            long id;
            Experimenter value;
            boolean systemGroup = false;
            final ExperimenterGroup userGroup = new ExperimenterGroupI(
                    getRoles(ctx).userGroupId, false);
            ExperimenterGroup system = new ExperimenterGroupI(
                    getRoles(ctx).systemGroupId, false);

            value = lookupExperimenter(ctx, username);
            if (value == null) {
                if (isAdmin) {
                    l.add(userGroup);
                    l.add(system);
                } else
                    l.add(userGroup);
                if (g == null) {
                    g = l.get(0);
                    systemGroup = true;
                }
                exp.asExperimenter().setOmeName(omero.rtypes.rstring(username));
                exp.asExperimenter().setLdap(omero.rtypes.rbool(false));
                if (password != null && password.length() > 0) {
                    id = svc.createExperimenterWithPassword(
                            exp.asExperimenter(),
                            omero.rtypes.rstring(password), g, l);
                } else
                    id = svc.createExperimenter(exp.asExperimenter(), g, l);
                exp = (ExperimenterData) PojoMapper.asDataObject(svc
                        .getExperimenter(id));
                if (isGroupOwner && !systemGroup)
                    svc.setGroupOwner(g, exp.asExperimenter());
                return exp;
            }

        } catch (Exception e) {
            handleException(this, e, "Cannot create the experimenters.");
        }
        return null;
    }

    /**
     * Returns the group corresponding to the passed name or <code>null</code>.
     *
     * @param ctx
     *            The security context.
     * @param name
     *            The name of the group.
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public ExperimenterGroup lookupGroup(SecurityContext ctx, String name)
            throws DSOutOfServiceException, DSAccessException {

        try {
            IAdminPrx svc = gateway.getAdminService(ctx);
            return svc.lookupGroup(name);
        } catch (Exception e) {
            if (e instanceof ApiUsageException)
                return null;
            handleException(this, e, "Cannot load the group.");
        }
        return null;
    }

    /**
     * Returns the experimenter corresponding to the passed name or
     * <code>null</code>.
     *
     * @param ctx
     *            The security context.
     * @param name
     *            The name of the experimenter.
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public Experimenter lookupExperimenter(SecurityContext ctx, String name)
            throws DSOutOfServiceException, DSAccessException {

        try {
            IAdminPrx svc = gateway.getAdminService(ctx);
            return svc.lookupExperimenter(name);
        } catch (Exception e) {
            if (e instanceof ApiUsageException)
                return null;
            handleException(this, e, "Cannot load the required group.");
        }
        return null;
    }

    /**
     * Creates the permissions corresponding to the specified level.
     *
     * @param level
     *            The level to handle.
     * @return
     */
    private Permissions createPermissions(int level) {
        String perms = "rw----"; // private group
        switch (level) {
        case GroupData.PERMISSIONS_GROUP_READ:
            perms = "rwr---";
            break;
        case GroupData.PERMISSIONS_GROUP_READ_LINK:
            perms = "rwra--";
            break;
        case GroupData.PERMISSIONS_GROUP_READ_WRITE:
            perms = "rwrw--";
            break;
        case GroupData.PERMISSIONS_PUBLIC_READ:
            perms = "rwrwr-";
        }
        return new PermissionsI(perms);
    }

    private Roles getRoles(SecurityContext ctx) {
        try {
            if (roles == null)
                roles = gateway.getAdminService(ctx).getSecurityRoles();
            return roles;
        } catch (ServerError e) {
        } catch (DSOutOfServiceException e) {
        }
        return null;
    }

}
