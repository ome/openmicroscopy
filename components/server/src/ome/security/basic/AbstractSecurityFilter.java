/*
 *   $Id$
 *
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.util.Collection;
import java.util.Properties;

import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.security.SecurityFilter;
import ome.system.EventContext;
import ome.system.Roles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.hibernate3.FilterDefinitionFactoryBean;

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
 */
public abstract class AbstractSecurityFilter extends FilterDefinitionFactoryBean
    implements SecurityFilter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final Roles roles;

    /**
     * Default constructor which calls all the necessary setters for this
     * {@link FactoryBean}. Also calls {@link #setDefaultFilterCondition(String)}.
     * This query clause must be kept in sync with
     * {@link #passesFilter(Session, Details, EventContext)}.
     *
     * @see #passesFilter(Session, Details, EventContext)
     * @see FilterDefinitionFactoryBean#setFilterName(String)
     * @see FilterDefinitionFactoryBean#setParameterTypes(java.util.Map)
     * @see FilterDefinitionFactoryBean#setDefaultFilterCondition(String)
     */
    public AbstractSecurityFilter() {
        this(new Roles());
    }

    public AbstractSecurityFilter(Roles roles) {
        this.roles = roles;
        this.setFilterName(getName());
        this.setParameterTypes(getParameterTypes());
        this.setDefaultFilterCondition(getDefaultCondition());
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void disable(Session sess) {
	    sess.disableFilter(getName());
	    disableBaseFilters(sess);
    }

    public boolean isNonPrivate(EventContext c) {
        return c.getCurrentGroupPermissions().isGranted(Role.GROUP, Right.READ)
            || c.getCurrentGroupPermissions().isGranted(Role.WORLD, Right.READ);
    }

    public boolean isAdminOrPi(EventContext c) {
        return c.isCurrentUserAdmin() ||
            c.getLeaderOfGroupsList().contains(c.getCurrentGroupId());

    }

    public boolean isShare(EventContext c) {
        return c.getCurrentShareId() != null;
    }

    protected void enableBaseFilters(Session sess, int admin01, Long currentUserId) {
        final Filter sessionFilter = sess.enableFilter("owner_or_admin");
        sessionFilter.setParameter("is_admin", admin01);
        sessionFilter.setParameter("current_user", currentUserId);
	}

    protected void disableBaseFilters(Session sess) {
        sess.disableFilter("owner_or_admin");
    }
}
