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

    static public final String member_of_groups = "member_of_groups";

    static public final String leader_of_groups = "leader_of_groups";

    static public final String filterName = "securityFilter";

    private static String myFilterCondition = String.format(
                  "\n( "
                + "\n  1 = :is_share OR "
                + "\n  1 = :is_admin OR "
                + "\n  (group_id in (:leader_of_groups)) OR "
                + "\n  (owner_id = :current_user AND %s) OR " // 1st arg U
                + "\n  (group_id in (:member_of_groups) AND %s) OR " // 2nd arg G
                + "\n  (%s) " // 3rd arg W
                + "\n)"
                + "\n", isGranted(USER, READ), isGranted(GROUP, READ),
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
        parameterTypes.put(member_of_groups, "long");
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
    public boolean passesFilter(Details d, EventContext c) {

        final Long currentUserId = c.getCurrentUserId();
        final boolean admin = c.isCurrentUserAdmin();
        final boolean share = isShare(c);
        final List<Long> memberOfGroups = c.getMemberOfGroupsList();
        final List<Long> leaderOfGroups = c.getLeaderOfGroupsList();

        if (d == null || d.getPermissions() == null) {
            throw new InternalException("Details/Permissions null! "
                    + "Security system failure -- refusing to continue. "
                    + "The Permissions should be set to a default value.");
        }

        Permissions p = d.getPermissions();
        if (p == Permissions.DUMMY) {
            throw new InternalException("Need real group permissions");
        }

        Long o = d.getOwner().getId();
        Long g = d.getGroup().getId();

        if (share || admin) {
            return true;
        }

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

        if (leaderOfGroups.contains(g)) {
            return true;
        }

        return false;
    }

    /***
     * Since we assume that the group is "-1" for this method, we have to pass
     * in lists of all groups as we did before group permissions (~4.2).
     */
    public void enable(Session sess, EventContext ec) {
        final Filter filter = sess.enableFilter(getName());

        final int share01 = isShare(ec) ? 1 : 0;
        final int admin01 = ec.isCurrentUserAdmin() ? 1 : 0;

        filter.setParameter(is_admin, admin01);
        filter.setParameter(SecurityFilter.is_share, share01); // ticket:2219, not checking -1 here.
        filter.setParameter(SecurityFilter.current_user, ec.getCurrentUserId());
        filter.setParameterList(member_of_groups,
                configGroup(ec, ec.getMemberOfGroupsList()));
        filter.setParameterList(leader_of_groups,
                configGroup(ec, ec.getLeaderOfGroupsList()));

    }

    // ~ Helpers
    // =========================================================================

    protected Collection<Long> configGroup(EventContext ec, List<Long> list) {
        Collection<Long> rv = null;

        if (ec.isCurrentUserAdmin()) {
            // Admin is considered to be in every group
            // which is handled by other clauses of the
            // filter
            rv = Collections.singletonList(-1L);
        } else {
            // Non-admin are only in their groups.
            rv = list;
            if (rv == null || rv.size() == 0) {
                // If this list is empty, we have to fake something
                // to prevent Hibernate from complaining.
                rv = Collections.singletonList(Long.MIN_VALUE);
            }
        }

        return rv;
    }

    protected static String isGranted(Role role, Right right) {
        String bit = "" + Permissions.bit(role, right);
        String isGranted = String
                .format(
                        "(select (__g.permissions & %s) = %s from " +
                        "experimentergroup __g where __g.id = group_id)",
                        bit, bit);
        return isGranted;
    }

}
