/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.ArrayList;
import java.util.List;

import ome.conditions.ValidationException;
import ome.security.SecuritySystem;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapRdn;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * Handles the ":ou:" specifier from etc/omero.properties by adding the user
 * to a group named by the final organizational unit. For example, if a user
 * is in the group, "ou=HookeLab,ou=biology,ou=example", then the user will
 * be added to the "HookeLab" group.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @since Beta4.2
 */
public class OrgUnitNewUserGroupBean implements NewUserGroupBean {

    private final DistinguishedName dn;

    public OrgUnitNewUserGroupBean(DistinguishedName dn) {
        this.dn = dn;
    }

    @SuppressWarnings("unchecked")
    public List<Long> groups(String username, LdapConfig config,
            LdapOperations ldap, RoleProvider provider,
            final AttributeSet attrSet) {

        final List<Long> groups = new ArrayList<Long>();
        final List<LdapRdn> names = dn.getNames();
        for (int i = names.size(); i > 0; i--) {
            LdapRdn name = names.get(i-1);
            if ("ou".equals(name.getKey())) {
                final String grpName = name.getValue("ou");
                groups.add(provider.createGroup(grpName, null, false));
                break;
            }
        }
        return groups;
    }

}
