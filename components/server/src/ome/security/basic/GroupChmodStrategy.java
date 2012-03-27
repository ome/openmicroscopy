/*
 * Copyright 2012 Glencoe Software, Inc. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import ome.conditions.ApiUsageException;
import ome.conditions.GroupSecurityViolation;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.ExperimenterGroup;
import ome.security.ChmodStrategy;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.SessionFactory;
import ome.util.SqlAction;
import ome.util.Utils;

/**
 * {@link ChmodStrategy} which only permits modifying
 * the permissions on groups.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4
 */
public class GroupChmodStrategy implements ChmodStrategy {

    private static class Check {
        final String perms;
        final String className;
        final Class<?> k;
        final String[][] lockChecks;

        Check(String perms, String className, Class<?> k, String[][] lockChecks) {
            this.perms = perms;
            this.className = className;
            this.k = k;
            this.lockChecks = lockChecks;
        }
    }

    private final static Log log = LogFactory.getLog(GroupChmodStrategy.class);

    private final BasicACLVoter voter;

    private final SessionFactory osf;

    private final SqlAction sql;

    private final ExtendedMetadata em;

    public GroupChmodStrategy(BasicACLVoter voter, SessionFactory osf,
            SqlAction sql, ExtendedMetadata em) {
        this.voter = voter;
        this.osf = osf;
        this.sql = sql;
        this.em = em;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object[] getChecks(IObject obj, String permissions) {

        ExperimenterGroup trusted = load(obj);
        if (!voter.allowChmod(trusted)) {
            throw new SecurityViolation("chmod not permitted");
        }

        if (!isReducePermissions(trusted, permissions)) {
            return new Object[0]; // none needed.
        }

        List<Object> checks = new ArrayList<Object>();
        Collection<String> classeNames = em.getClasses();
        for (String className : classeNames) {
            Class k = em.getHibernateClass(className);
            String[][] lockChecks = em.getLockChecks(k);
            checks.add(new Check(permissions, className, k, lockChecks));
        }

        return checks.toArray(new Object[checks.size()]);

    }

    public void chmod(IObject obj, String permissions) {
        handleGroupChange(obj, Permissions.parseString(permissions));
    }

    public void check(IObject obj, Object check) {
        if (!(check instanceof Check)) {
            throw new InternalException("Bad check:" + check);
        }
        Check c = ((Check) check);
        Map<String, Long> counts = em.countLocks(osf.getSession(), obj.getId(),
                c.lockChecks, null);

        long total = counts.get("*");
        if (total > 0) {
            throw new SecurityViolation(String.format(
                    "Cannot change permissions on %s to %s due to locks:\n%s",
                    obj, c.perms, counts));
        }
    }

    // Helpers
    // =========================================================================

    private ExperimenterGroup load(IObject obj) {

        if (!(obj instanceof ExperimenterGroup)) {
            throw new SecurityViolation("Only groups allowed");
        }

        if (obj.getId() == null) {
            throw new ApiUsageException("ID cannot be null");
        }

        final Session s = osf.getSession();
        return (ExperimenterGroup) s.get(ExperimenterGroup.class, obj.getId());
    }

    private boolean isReducePermissions(ExperimenterGroup trusted,
            String permissions) {

        final Permissions oldPerms = trusted.getDetails().getPermissions();
        final Permissions newPerms = Permissions.parseString(permissions);

        final Role u = Role.USER;
        final Role g = Role.GROUP;
        final Role a = Role.WORLD;
        final Right r = Right.READ;

        if (!newPerms.isGranted(u, r)) {
            throw new GroupSecurityViolation("Cannot remove user read: "
                    + trusted);
        }
        else if (oldPerms.isGranted(g, r) && !newPerms.isGranted(g, r)) {
            return true;
        }
        else if (oldPerms.isGranted(a, r) && !newPerms.isGranted(a, r)) {
            return true;
        }
        else {
            return false;
        }
    }

    private void handleGroupChange(IObject obj, Permissions newPerms) {

        final ExperimenterGroup group = load(obj);
        if (newPerms == null) {
            throw new ApiUsageException("PERMS cannot be null");
        }

        final Permissions oldPerms = group.getDetails().getPermissions();

        if (oldPerms.sameRights(newPerms)) {
            log.debug(String.format("Ignoring unchanged permissions: %s",
                    newPerms));
            return;
        }

        final Long internal = (Long) Utils.internalForm(newPerms);

        sql.changeGroupPermissions(obj.getId(), internal);
        log.info(String.format("Changed permissions for %s to %s", obj.getId(),
                internal));

    }

}
