/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.security.basic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.model.enums.AdminPrivilege;
import ome.model.internal.NamedValue;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.system.Roles;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Report the light administrator privileges associated with a given session.
 * Caches recent results.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.3.0
 */
public class LightAdminPrivileges {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightAdminPrivileges.class);

    // NOTE THE SET OF LIGHT ADMINISTRATOR PRIVILEGES AND THEIR ASSOCIATED VALUE STRINGS

    /* see trac ticket 10691 re. enum values */
    private enum Privilege {
        CHGRP("Chgrp"),
        CHOWN("Chown"),
        MODIFY_USER("ModifyUser"),
        READ_SESSION("ReadSession"),
        SUDO("Sudo"),
        WRITE_FILE("WriteFile"),
        WRITE_OWNED("WriteOwned");

        public final String value;

        Privilege(String value) {
            this.value = value;
        }
    };

    private static final ImmutableSet<AdminPrivilege> ADMIN_PRIVILEGES;
    private static final ImmutableMap<String, AdminPrivilege> ADMIN_PRIVILEGES_BY_VALUE;

    static {
        final ImmutableMap.Builder<String, AdminPrivilege> builder = ImmutableMap.builder();
        for (final Privilege privilege : Privilege.values()) {
            builder.put(privilege.value, new AdminPrivilege(privilege.value));
        }
        ADMIN_PRIVILEGES_BY_VALUE = builder.build();
        ADMIN_PRIVILEGES = ImmutableSet.copyOf(ADMIN_PRIVILEGES_BY_VALUE.values());
    }

    // PUBLIC QUERY METHODS

    /**
     * @return all the light administrator privileges
     */
    public ImmutableSet<AdminPrivilege> getAllPrivileges() {
        return ADMIN_PRIVILEGES;
    }

    /**
     * @param value the string value of a light administrator privilege
     * @return the corresponding privilege, or {@code null} if there is no privilege with that string value
     */
    public AdminPrivilege getPrivilege(String value) {
        final AdminPrivilege privilege = ADMIN_PRIVILEGES_BY_VALUE.get(value);
        if (privilege == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("checked for unknown privilege " + value);
            }
            return null;
        }
        return privilege;
    }

    /**
     * Determine the light administrator privileges associated with a session.
     * If the session originates via <q>sudo</q>, takes that into account.
     * Does <em>not</em> take account of if the relevant user is a member of <tt>system</tt>:
     * calculates assuming that the user is an administrator.
     * Caches newly fetched privileges for future lookups.
     * @param session an OMERO session
     * @return the light administrator privileges associated with the session
     */
    public ImmutableSet<AdminPrivilege> getSessionPrivileges(Session session) {
        return getSessionPrivileges(session, true);
    }

    /**
     * Determine the light administrator privileges associated with a session.
     * If the session originates via <q>sudo</q>, takes that into account.
     * Does <em>not</em> take account of if the relevant user is a member of <tt>system</tt>:
     * calculates assuming that the user is an administrator.
     * @param session an OMERO session
     * @param isCache if newly fetched privileges should be cached for future lookups
     * @return the light administrator privileges associated with the session
     */
    public ImmutableSet<AdminPrivilege> getSessionPrivileges(Session session, boolean isCache) {
        try {
            if (isCache) {
                return PRIVILEGE_CACHE.get(session);
            } else {
                final ImmutableSet<AdminPrivilege> privileges = PRIVILEGE_CACHE.getIfPresent(session);
                if (privileges != null) {
                    return privileges;
                } else {
                    return getPrivileges(session);
                }
            }
        } catch (ExecutionException ee) {
            LOGGER.warn("failed to check privileges for session " + session.getId(), ee.getCause());
            return ImmutableSet.of();
        }
    }

    // DETERMINE AND CACHE PRIVILEGES FOR SESSIONS

    private final long rootId;

    /**
     * @param roles the OMERO roles
     */
    public LightAdminPrivileges(Roles roles) {
        rootId = roles.getRootId();
    }

    private final LoadingCache<ome.model.meta.Session, ImmutableSet<AdminPrivilege>> PRIVILEGE_CACHE =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(
                    new CacheLoader<ome.model.meta.Session, ImmutableSet<AdminPrivilege>>() {
                        @Override
                        public ImmutableSet<AdminPrivilege> load(Session session) {
                            return getPrivileges(session);
                        }
                    });

    /**
     * Determine the light administrator privileges associated with a session.
     * If the session originates via <q>sudo</q>, takes that into account.
     * Does <em>not</em> take account of if the relevant user is a member of <tt>system</tt>:
     * calculates assuming that the user is an administrator.
     * Assumes that <tt>root</tt> has all light administrator privileges.
     * @param session an OMERO session
     * @return the light administrator privileges associated with the session
     */
    private ImmutableSet<AdminPrivilege> getPrivileges(ome.model.meta.Session session) {
        final Set<AdminPrivilege> privileges = new HashSet<>(getAllPrivileges());
        final Experimenter user;
        if (session.getSudoer() == null) {
            user = session.getOwner();
        } else {
            user = session.getSudoer();
        }
        if (user != null && user.getId() != rootId) {
            final List<NamedValue> config = user.getConfig();
            if (CollectionUtils.isNotEmpty(config)) {
                for (final NamedValue configProperty : config) {
                    if (!Boolean.parseBoolean(configProperty.getValue())) {
                        privileges.remove(ADMIN_PRIVILEGES_BY_VALUE.get(configProperty.getName()));
                    }
                }
            }
        }
        return ImmutableSet.copyOf(privileges);
    }
}
