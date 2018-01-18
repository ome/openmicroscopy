/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2017 University of Dundee. All rights reserved.
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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.AdminPrivilege;
import omero.model.AdminPrivilegeI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.sys.Roles;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.util.PojoMapper;
import omero.gateway.util.Pojos;
import omero.gateway.util.Utils;

/**
 * {@link Facility} for handling admin issues, e.g. creating users, groups,
 * etc.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class AdminFacility extends Facility {

    /** Reference to the roles.*/
    private Roles roles;

    /** All available admin privileges */
    private Collection<String> adminPrivileges;

    /**
     * Creates a new instance.
     * @param gateway Reference to the gateway.
     */
    AdminFacility(Gateway gateway) {
        super(gateway);
    }

    /**
     * Creates a group and returns it.
     *
     * @param ctx The security context.
     * @param groupData Host information about the group to create.
     * @param owner The owner of the group.
     * @param permissions The group's permissions.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public GroupData createGroup(SecurityContext ctx, GroupData groupData,
            ExperimenterData owner, int permissions)
            throws DSOutOfServiceException, DSAccessException {

        try {
            IAdminPrx svc = gateway.getAdminService(ctx);

            if (lookupGroup(ctx, groupData.getName()) != null)
                return null;

            ExperimenterGroup g = new ExperimenterGroupI();
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
     * Creates an experimenter and returns it.
     *
     * @param ctx
     *            The security context.
     * @param exp
     *            The experimenter to create.
     * @param username
     *            The user name to use.
     * @param password
     *            The password to use.
     * @param groups
     *            The groups to add the user to.
     * @param isAdmin
     *            Pass <code>true</code> if the user is an administrator,
     *            <code>false</code> otherwise.
     * @param isGroupOwner
     *            Pass <code>true</code> if the user is a group owner,
     *            <code>false</code> otherwise.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public ExperimenterData createExperimenter(SecurityContext ctx,
            ExperimenterData exp, String username, String password,
            List<GroupData> groups, boolean isAdmin, boolean isGroupOwner)
            throws DSOutOfServiceException, DSAccessException {
        return createExperimenter(ctx, exp, username, password, groups,
                isAdmin, isGroupOwner, null);
    }

    /**
     * Creates an experimenter and returns it.
     *
     * @param ctx
     *            The security context.
     * @param exp
     *            The experimenter to create.
     * @param username
     *            The user name to use.
     * @param password
     *            The password to use.
     * @param groups
     *            The groups to add the user to.
     * @param isAdmin
     *            Pass <code>true</code> if the user is an administrator,
     *            <code>false</code> otherwise.
     * @param isGroupOwner
     *            Pass <code>true</code> if the user is a group owner,
     *            <code>false</code> otherwise.
     * @param privileges
     *            Only grant these admin privileges (only applies if isAdmin ==
     *            <code>true</code>); pass an empty list to create a user in
     *            system group but no admin privileges (unspecified or
     *            <code>null</code> creates full admin with all privileges)
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public ExperimenterData createExperimenter(SecurityContext ctx,
            ExperimenterData exp, String username, String password,
            List<GroupData> groups, boolean isAdmin, boolean isGroupOwner, List<String> privileges)
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
            ExperimenterData value;
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

                if (privileges != null)
                    svc.setAdminPrivileges(exp.asExperimenter(), Utils.toEnum(
                            AdminPrivilege.class, AdminPrivilegeI.class,
                            privileges));

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
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public GroupData lookupGroup(SecurityContext ctx, String name)
            throws DSOutOfServiceException, DSAccessException {
        if(StringUtils.isBlank(name))
            return null;

        try {
            IAdminPrx svc = gateway.getAdminService(ctx);
            ExperimenterGroup g =  svc.lookupGroup(name);
            return (GroupData) (g == null ? null : PojoMapper.asDataObject(g));
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
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public ExperimenterData lookupExperimenter(SecurityContext ctx, String name)
            throws DSOutOfServiceException, DSAccessException {
        if(StringUtils.isBlank(name))
            return null;

        try {
            IAdminPrx svc = gateway.getAdminService(ctx);
            Experimenter exp = svc.lookupExperimenter(name);
            return exp == null ? null : (ExperimenterData) PojoMapper.asDataObject(exp);
        } catch (Exception e) {
            if (e instanceof ApiUsageException)
                return null;
            handleException(this, e, "Cannot load the required group.");
        }
        return null;
    }

    /**
     * Get the logged in user's admin privileges
     * (see omero.model.enums)
     * @param ctx
     *            The security context.
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Collection<String> getAdminPrivileges(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
        return getAdminPrivileges(ctx, gateway.getLoggedInUser());
    }

    /**
     * Get the admin privileges of a certain user
     * (see omero.model.enums)
     *
     * @param ctx
     *            The security context.
     * @param user
     *            The user.
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Collection<String> getAdminPrivileges(SecurityContext ctx,
            ExperimenterData user) throws DSOutOfServiceException,
            DSAccessException {
        if (!Pojos.hasID(user))
            return null;
        try {
            IAdminPrx adm = gateway.getAdminService(ctx);
            return Utils
                    .fromEnum(adm.getAdminPrivileges(user.asExperimenter()));
        } catch (Exception e) {
            handleException(this, e, "Cannot get admin privileges.");
        }
        return Collections.emptyList();
    }

    /**
     * Set the admin privileges of a certain user
     * (see omero.model.enums)
     *
     * @param ctx
     *            The security context.
     * @param user
     *            The user.
     * @param privileges
     *            The admin privileges.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void setAdminPrivileges(SecurityContext ctx, ExperimenterData user,
            Collection<String> privileges) throws DSOutOfServiceException,
            DSAccessException {
        if (!Pojos.hasID(user))
            return;

        try {
            IAdminPrx adm = gateway.getAdminService(ctx);
            adm.setAdminPrivileges(user.asExperimenter(), Utils.toEnum(
                    AdminPrivilege.class, AdminPrivilegeI.class, privileges));
        } catch (Exception e) {
            handleException(this, e, "Cannot set admin privileges.");
        }
    }

    /**
     * Grant an user additional admin privileges.
     *
     * @param ctx
     *            The security context.
     * @param user
     *            The user.
     * @param privileges
     *            The admin privileges to grant.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void addAdminPrivileges(SecurityContext ctx, ExperimenterData user,
            Collection<String> privileges) throws DSOutOfServiceException,
            DSAccessException {

        if (!Pojos.hasID(user) || CollectionUtils.isEmpty(privileges)) {
            return;
        }

        try {
            Collection<String> privs = getAdminPrivileges(ctx, user);
            for (String priv : privileges)
                if (!privs.contains(priv))
                    privs.add(priv);
            setAdminPrivileges(ctx, user, privs);
        } catch (Exception e) {
            handleException(this, e, "Cannot add admin privileges.");
        }
    }

    /**
     * Revoke admin privileges for a user
     *
     * @param ctx
     *            The security context.
     * @param user
     *            The user.
     * @param privileges
     *            The admin privileges to revoke.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void removeAdminPrivileges(SecurityContext ctx,
            ExperimenterData user, Collection<String> privileges)
            throws DSOutOfServiceException, DSAccessException {

        if (!Pojos.hasID(user) || CollectionUtils.isEmpty(privileges)) {
            return;
        }

        try {
            Collection<String> privs = getAdminPrivileges(ctx, user);
            privs.removeAll(privileges);
            setAdminPrivileges(ctx, user, privs);
        } catch (Exception e) {
            handleException(this, e, "Cannot remove admin privileges.");
        }
    }

    /**
     * Get all available admin privileges
     *
     * @param ctx
     *            The security context.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Collection<String> getAvailableAdminPrivileges(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
        if (adminPrivileges == null) {
            try {
                adminPrivileges = Collections.unmodifiableList(Utils
                        .fromEnum(gateway.getTypesService(ctx).allEnumerations(
                                "AdminPrivilege")));
            } catch (Exception e) {
                handleException(this, e, "Cannot get admin privileges.");
            }
        }
        return adminPrivileges;
    }

    /**
     * Checks if the currently logged in user has full admin privileges
     *
     * @param ctx
     *            The security context.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public boolean isFullAdmin(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
        return isFullAdmin(ctx, gateway.getLoggedInUser());
    }

    /**
     * Checks if a user has full admin privileges
     *
     * @param ctx
     *            The security context.
     * @param user
     *            The user.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public boolean isFullAdmin(SecurityContext ctx, ExperimenterData user)
            throws DSOutOfServiceException, DSAccessException {
        if (!Pojos.hasID(user))
            return false;
        try {
            return getAdminPrivileges(ctx, user).size() == getAvailableAdminPrivileges(
                    ctx).size();
        } catch (Exception e) {
            handleException(this, e, "Cannot get admin privileges.");
        }
        return false;
    }

    /**
     * Creates the permissions corresponding to the specified level.
     *
     * @param level
     *            The level to handle.
     * @return The {@link Permissions}.
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

    /**
     * Returns the security roles for the given context.
     *
     * @param ctx The security context.
     * @return See above.
     */
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
