/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.conditions.ValidationException;
import ome.security.SecuritySystem;

import org.springframework.ldap.core.LdapOperations;

/**
 * Handles the ":attribute:" specifier from etc/omero.properties.
 *
 * The values of the attribute equal to the string following ":attribute:" are
 * taken to be the names of {@link ExperimenterGroup} instances and created if
 * necessary.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @since Beta4.2
 */
public class AttributeNewUserGroupBean implements NewUserGroupBean {

    private final String grpSpec;

    public AttributeNewUserGroupBean(String grpSpec) {
        this.grpSpec = grpSpec;
    }

    public List<Long> groups(String username, LdapConfig config,
            LdapOperations ldap, RoleProvider provider, AttributeSet attrSet) {

        final String grpAttribute = grpSpec.substring(11);
        Set<String> groupNames = attrSet.getAll(grpAttribute);
        if (groupNames == null) {
            throw new ValidationException(username + " has no attributes "
                    + grpAttribute);
        }

        List<Long> groups = new ArrayList<Long>();
        for (String grpName : groupNames) {
            groups.add(provider.createGroup(grpName, null, false));
        }
        return groups;

    }

}
