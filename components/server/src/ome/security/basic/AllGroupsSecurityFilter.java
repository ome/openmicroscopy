/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.hibernate3.FilterDefinitionFactoryBean;

import ome.conditions.InternalException;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.security.SecurityFilter;
import ome.system.EventContext;
import ome.system.Roles;

/**
 * overrides {@link FilterDefinitionFactoryBean} in order to construct our
 * security filter in code and not in XML. This allows us to make use of the
 * knowledge within {@link Permissions}
 *
 * With the addition of shares in 4.0, it is necessary to remove the security
 * filter if a share is active and allow loading to throw the necessary
 * exceptions.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0
 * @see <a
 *      href="https://trac.openmicroscopy.org.uk/omero/ticket/117">ticket117</a>
 * @see <a
 *      href="https://trac.openmicroscopy.org.uk/omero/ticket/1154">ticket1154</a>
 */
public class AllGroupsSecurityFilter extends AbstractSecurityFilter {

    private static String myFilterCondition = "(\n"
                // Should handle hidden groups at the top-level
                // ticket:1784 - Allowing system objects to be read.
                + "\n  ( group_id in (:current_groups) AND "
                + "\n     ( 1 = :is_nonprivate OR "
                + "\n       1 = :is_adminorpi OR "
                + "\n       owner_id = :current_user"
                + "\n     )"
                + "\n  ) OR"
                + "\n  group_id = %s OR " // ticket:1794
                // Will need to add something about world readable here.
                + "\n 1 = :is_share"
                + "\n)\n";

    /**
     * default constructor which calls all the necessary setters for this
     * {@link FactoryBean}. Also constructs the {@link #defaultFilterCondition }
     * This query clause must be kept in sync with
     * {@link #passesFilter(Details, Long, Collection, Collection, boolean)}
     *
     * @see #passesFilter(Details, Long, Collection, Collection, boolean)
     * @see FilterDefinitionFactoryBean#setFilterName(String)
     * @see FilterDefinitionFactoryBean#setParameterTypes(Properties)
     * @see FilterDefinitionFactoryBean#setDefaultFilterCondition(String)
     */
    public AllGroupsSecurityFilter() {
        super();
    }

    public AllGroupsSecurityFilter(Roles roles) {
        super(roles);
    }

    public String getDefaultCondition() {
        return String.format(myFilterCondition, roles.getUserGroupId());
    }

    public Map<String, String> getParameterTypes() {
        Map<String, String> parameterTypes = new HashMap<String, String>();
        parameterTypes.put(is_share, "int");
        parameterTypes.put(is_adminorpi, "int");
        parameterTypes.put(is_nonprivate, "int");
        parameterTypes.put(current_groups, "long");
        parameterTypes.put(current_user, "long");
        return parameterTypes;
    }

    /**
     * tests that the {@link Details} argument passes the security test that
     * this filter defines. The two must be kept in sync. This will be used
     * mostly by the
     * {@link OmeroInterceptor#onLoad(Object, java.io.Serializable, Object[], String[], org.hibernate.type.Type[])}
     * method.
     *
     * @param d
     *            Details instance. If null (or if its {@link Permissions} are
     *            null all {@link Right rights} will be assumed.
     * @return true if the object to which this
     */
    public boolean passesFilter(Details d,
            Long currentGroupId, Long currentUserId,
            boolean nonPrivate, boolean adminOrPi, boolean share,
            List<Long> memberOfGroups) {
        if (d == null || d.getPermissions() == null) {
            throw new InternalException("Details/Permissions null! "
                    + "Security system failure -- refusing to continue. "
                    + "The Permissions should be set to a default value.");
        }

        Long o = d.getOwner().getId();
        Long g = d.getGroup().getId();

        if (share) {
            return true;
        }

        // ticket:1434 - Only loading current objects is permitted.
        // This method will not be called with system types.
        // See BasicACLVoter
        // Also ticket:1784 allowing system objects to be read.
        // Also ticket:1791 allowing user objects to be read (also 1794)
        if (Long.valueOf(roles.getSystemGroupId()).equals(g) ||
                Long.valueOf(roles.getUserGroupId()).equals(g)) {
            return true;
        }

        // ticket:3529 - if we're querying for multi groups, then allow
        // admins to read anything, and prevent other non-group members
        // from doing anything.
        if (currentGroupId < 0) {
            if (adminOrPi) {
                return true;
            } else if (!memberOfGroups.contains(g)) {
                return false;
            }
        } else if (!currentGroupId.equals(g)) {
            return false;
        }

        if (nonPrivate) {
            return true;
        }

        if (adminOrPi) {
            return true;
        }

        if (currentUserId.equals(o)) {
            return true;
        }

        return false;
    }

    public void enable(Session sess, EventContext ec) {
        final Filter filter = sess.enableFilter(getName());

        final Long groupId = ec.getCurrentGroupId();
        final Long shareId = ec.getCurrentShareId();
        int share01 = shareId != null ? 1 : 0; // non-final; "ticket:3529" below

        final int admin01 = (ec.isCurrentUserAdmin() ||
                ec.getLeaderOfGroupsList().contains(ec.getCurrentGroupId()))
                ? 1 : 0;

        final int nonpriv01 = (ec.getCurrentGroupPermissions().isGranted(Role.GROUP, Right.READ)
                || ec.getCurrentGroupPermissions().isGranted(Role.WORLD, Right.READ))
                ? 1 : 0;

        // ticket:3529 - if the group id is less than zero, then we assume that
        // SELECTs should return more than a single group.
        Collection<Long> groups = null;
        if (groupId < 0) { // Special marker
            if (ec.isCurrentUserAdmin()) {
                // Admin is considered to be in every group
                share01 = 1;
                groups = Collections.singletonList(-1L);
            } else {
                // Non-admin are only in their groups.
                groups = ec.getMemberOfGroupsList();
            }
        } else {
            // Group is a real value, pass only one.
            groups = Collections.singletonList(groupId);
        }

        filter.setParameter(SecurityFilter.is_share, share01); // ticket:2219, not checking -1 here.
        filter.setParameter(SecurityFilter.is_adminorpi, admin01);
        filter.setParameter(SecurityFilter.is_nonprivate, nonpriv01);
        filter.setParameter(SecurityFilter.current_user, ec.getCurrentUserId());
        filter.setParameterList(SecurityFilter.current_groups, groups);

    }

}
