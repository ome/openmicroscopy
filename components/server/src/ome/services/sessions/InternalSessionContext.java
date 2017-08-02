/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.Arrays;

import com.google.common.collect.ImmutableSet;

import ome.model.enums.AdminPrivilege;
import ome.model.internal.Permissions;
import ome.model.meta.Session;
import ome.services.sessions.stats.NullSessionStats;
import ome.system.Roles;

/**
 * Essentially dummy {@link SessionContext} implementation which uses the values
 * in {@link Role} to define a root-based admin instance.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
class InternalSessionContext extends SessionContextImpl {

    Roles roles;

    InternalSessionContext(Session s, ImmutableSet<AdminPrivilege> adminPrivileges, Roles roles) {
        super(s, adminPrivileges, Arrays.asList(roles.getSystemGroupId()),
                Arrays.asList(roles.getSystemGroupId()),
                Arrays.asList(roles.getSystemGroupName()),
                new NullSessionStats(), roles, null);
        this.roles = roles;
    }

    @Override
    public String getCurrentEventType() {
        return "Internal"; // TODO This should be in Roles
    }

    @Override
    public Long getCurrentGroupId() {
        return roles.getSystemGroupId();
    }

    @Override
    public String getCurrentGroupName() {
        return roles.getSystemGroupName();
    }

    @Override
    public Long getCurrentUserId() {
        return roles.getRootId();
    }

    @Override
    public String getCurrentUserName() {
        return roles.getRootName();
    }

    @Override
    public boolean isCurrentUserAdmin() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Overrides the base definition so that the call to force an early NPE
     * passes in the case of the internal session.
     */
    @Override
    public Permissions getCurrentGroupPermissions() {
        return Permissions.USER_PRIVATE;
    }
}
