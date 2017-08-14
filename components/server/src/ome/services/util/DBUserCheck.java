/*
 * Copyright (C) 2013-2017 University of Dundee & Open Microscopy Environment.
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

package ome.services.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import ome.system.PreferenceContext;
import ome.system.Roles;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hook run by the context. This hook examines the database for the well known
 * user info on creation and provides them to the Spring context via
 * {@link #getRoles()}.
 *
 * @author Josh Moore, josh at glencoesoftwarecom
 * @since 5.0.0-beta2
 */
public class DBUserCheck {

    public final static Logger log = LoggerFactory.getLogger(DBUserCheck.class);

    final private SqlAction sql;
    final private PreferenceContext prefs;
    final private Roles roles;

    public DBUserCheck(SqlAction sql, PreferenceContext prefs) throws Exception {
        this.sql = sql;
        this.prefs = prefs;
        this.roles = load();
        sql.setRoles(roles.getRootId(), roles.getGuestId(),
                roles.getSystemGroupId(), roles.getUserGroupId(), roles.getGuestGroupId());
    }

    private String getRoleName(String which, String defaultValue) {
        String rv;
        try {
            rv = prefs.getProperty("omero.roles." + which);
        } catch (Exception e) {
            rv = null;
        }

        if (rv == null) {
            return defaultValue;
        }
        return rv;
    }

    public Roles getRoles() {
        return roles;
    }

    public Roles load() throws Exception {
        String userGroup = getRoleName("group.user", "user");
        String sysGroup = getRoleName("group.system", "system");
        String guestGroup = getRoleName("group.guest", "guest");
        String guestUser = getRoleName("user.guest", "guest");
        String rootUser = getRoleName("user.root", "root");

        Long rootUserID = -1L;
        try {
            rootUserID = sql.getUserId(rootUser);
        } catch (Exception e) {
            log.debug("No root user found", e);
        }

        Long guestUserID = -1L;
        try {
            guestUserID = sql.getUserId(guestUser);
        } catch (Exception e) {
            log.debug("No guest user found", e);
        }

        Map<String, Long> groupIDs =
                sql.getGroupIds(new HashSet<String>(
                        Arrays.asList(userGroup, sysGroup, guestGroup)));

        Long userGroupID = groupIDs.get(userGroup);
        if (userGroupID == null) {
            userGroupID = -1L;
        }

        Long sysGroupID = groupIDs.get(sysGroup);
        if (sysGroupID == null) {
            sysGroupID = -1L;
        }

        Long guestGroupID = groupIDs.get(guestGroup);
        if (guestGroupID == null) {
            guestGroupID = -1L;
        }

        log.info("User {}.id = {}", rootUser, rootUserID);
        log.info("User {}.id = {}", guestUser, guestUserID);
        log.info("Group {}.id = {}", sysGroup, sysGroupID);
        log.info("Group {}.id = {}", userGroup, userGroupID);
        log.info("Group {}.id = {}", guestGroup, guestGroupID);
        return new Roles(rootUserID, rootUser,
                sysGroupID, sysGroup,
                userGroupID, userGroup,
                guestUserID, guestUser,
                guestGroupID, guestGroup);
    }

}
