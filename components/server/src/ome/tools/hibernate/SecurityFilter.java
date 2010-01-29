/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.hibernate;

import java.util.Collection;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.hibernate3.FilterDefinitionFactoryBean;

import ome.conditions.InternalException;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;

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
public class SecurityFilter extends FilterDefinitionFactoryBean {

    static public final String is_share = "is_share";

    static public final String is_admin = "is_admin";

    static public final String current_group = "current_group";

    static public final String current_user = "current_user";

    static public final String current_groups = "current_groups";

    static public final String leader_of_groups = "leader_of_groups";

    static public final String filterName = "securityFilter";

    static private final Properties parameterTypes = new Properties();

    static private String defaultFilterCondition;
    static {
        parameterTypes.setProperty(is_share, "java.lang.Boolean");
        parameterTypes.setProperty(is_admin, "java.lang.Boolean");
        parameterTypes.setProperty(current_group, "long");
        parameterTypes.setProperty(current_user, "long");
        parameterTypes.setProperty(current_groups, "long");
        parameterTypes.setProperty(leader_of_groups, "long");
        // This can't be done statically because we need the securitySystem.
        defaultFilterCondition = String.format("(\n"
                + "\n  ( group_id = :current_group AND "
                + "\n     ( :is_admin OR "
                + "\n       (group_id in (:leader_of_groups)) OR "
                + "\n       (owner_id = :current_user AND %s) OR "
                + "\n       (group_id in (:current_groups) AND %s)"
                //        omitting world permissions
                + "\n     )"
                + "\n  )"
                + "\n OR :is_share"
                + "\n)\n", isGranted(USER, READ), isGranted(GROUP, READ),
                isGranted(WORLD, READ));
    }

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
    public SecurityFilter() {
        this.setFilterName(filterName);
        this.setParameterTypes(parameterTypes);
        this.setDefaultFilterCondition(defaultFilterCondition);
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
    public static boolean passesFilter(Details d,
            Long currentGroupId, Long currentUserId,
            Collection<Long> memberOfGroups, Collection<Long> leaderOfGroups,
            boolean admin, boolean share) {
        if (d == null || d.getPermissions() == null) {
            throw new InternalException("Details/Permissions null! "
                    + "Security system failure -- refusing to continue. "
                    + "The Permissions should be set to a default value.");
        }

        Permissions p = d.getPermissions();

        Long o = d.getOwner().getId();
        Long g = d.getGroup().getId();

        // ticket:1434 - Only loading current objects is permitted.
        // This method will not be called with system types.
        // See BasicACLVoter
        if (!currentGroupId.equals(g)) {
            return false;
        }

        if (currentUserId.equals(o) && p.isGranted(USER, READ)) {
            return true;
        }

        if (memberOfGroups.contains(g)
                && d.getPermissions().isGranted(GROUP, READ)) {
            return true;
        }

        if (admin) {
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
