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

import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;


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

    static public final String is_admin = "is_admin";

    static public final String current_groups = "current_groups";

    static public final String leader_of_groups = "leader_of_groups";

    static public final String filterName = "securityFilter";

    private static String myFilterCondition = String.format("\n( "
                + "\n :is_share OR \n :is_admin OR "
                + "\n (group_id in (:leader_of_groups)) OR "
                + "\n (owner_id = :current_user AND %s) OR " + // 1st arg U
                "\n (group_id in (:current_groups) AND %s) OR " + // 2nd arg G
                "\n (%s) " + // 3rd arg W
                "\n)\n", isGranted(USER, READ), isGranted(GROUP, READ),
                isGranted(WORLD, READ));
    
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
        parameterTypes.put(is_admin, "int");
        parameterTypes.put(current_user, "long");
        parameterTypes.put(current_groups, "long");
        parameterTypes.put(leader_of_groups, "long");
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
    public boolean passesFilter(Details d, Long currentGroupId,
        Long currentUserId, boolean nonPrivate, boolean adminOrPi,
        boolean share, List<Long> memberOfGroups) {
        if (d == null || d.getPermissions() == null) {
            throw new InternalException("Details/Permissions null! "
                    + "Security system failure -- refusing to continue. "
                    + "The Permissions should be set to a default value.");
        }

        Permissions p = d.getPermissions();

        Long o = d.getOwner().getId();
        Long g = d.getGroup().getId();

        // most likely and fastest first
        if (p.isGranted(WORLD, READ)) {
            return true;
        }

        if (currentUserId.equals(o) && p.isGranted(USER, READ)) {
            return true;
        }

        if (memberOfGroups.contains(g)
                && d.getPermissions().isGranted(GROUP, READ)) {
            return true;
        }

        if (adminOrPi) {
            return true;
        }

        if (share) {
            return true;
        }

        if (leaderOfGroups.contains(g)) {
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

    // ~ Helpers
    // =========================================================================

    protected static String isGranted(Role role, Right right) {
        String bit = "" + Permissions.bit(role, right);
        String isGranted = String
                .format(
                        "(cast(permissions as bit(64)) & cast(%s as bit(64))) = cast(%s as bit(64))",
                        bit, bit);
        return isGranted;
    }

}
