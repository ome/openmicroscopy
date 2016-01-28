/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import java.util.Map;

import org.hibernate.Session;

import ome.model.internal.Details;
import ome.security.basic.AllGroupsSecurityFilter;
import ome.security.basic.CurrentDetails;
import ome.security.basic.OneGroupSecurityFilter;
import ome.system.EventContext;

/**
 * Security dispatcher holding each currently active {@link SecurityFilter}
 * instance and allowing dispatching between them.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecurityFilter
 * @since 4.4
 */
public class SecurityFilterHolder implements SecurityFilter {

    final protected AllGroupsSecurityFilter allgroups;

    final protected OneGroupSecurityFilter onegroup;

    final protected CurrentDetails cd;

    protected ThreadLocal<SecurityFilter> current = new ThreadLocal<SecurityFilter>() {
        @Override
        protected SecurityFilter initialValue() {
            return onegroup;
        }
    };

    public SecurityFilterHolder(CurrentDetails cd,
            OneGroupSecurityFilter onegroup,
            AllGroupsSecurityFilter allgroups) {
        this.cd = cd;
        this.onegroup = onegroup;
        this.allgroups = allgroups;
    }

    public SecurityFilter choose() {
        Long groupId = cd.getCurrentEventContext().getCurrentGroupId();
        if (groupId < 0) {
            return allgroups;
        } else {
            return onegroup;
        }
    }

    // Delegation
    // =========================================================================

    public String getName() {
        return choose().getName();
    }

    public String getDefaultCondition() {
        return choose().getDefaultCondition();
    }

    public Map<String, String> getParameterTypes() {
        return choose().getParameterTypes();
    }

    public void enable(Session sess, EventContext ec) {
        choose().enable(sess, ec);
    }

    public void disable(Session sess) {
        choose().disable(sess);
    }

    public boolean passesFilter(Session s, Details d, EventContext c) {
        return choose().passesFilter(s, d, c);
    }

}
