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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.LdapOperations;

/**
 * Handles the ":filtered_attribute:" specifier from etc/omero.properties.
 *
 * The values of the attribute equal to the string following ":attribute:" are
 * taken to be the names of {@link ExperimenterGroup} instances and created if
 * necessary if and only if they pass the assigned group filter. If no such
 * group filter should be assigned, see {@link AttributeNewUserGroupBean}
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @since Beta4.2
 */
public class FilteredAttributeNewUserGroupBean implements NewUserGroupBean {

    private final static Log log = LogFactory.getLog(FilteredAttributeNewUserGroupBean.class);

    private final String grpAttribute;

    public FilteredAttributeNewUserGroupBean(String grpAttribute) {
        this.grpAttribute = grpAttribute;
    }

    public List<Long> groups(String username, LdapConfig config,
            LdapOperations ldap, RoleProvider provider, AttributeSet attrSet) {

        Set<String> groupNames = attrSet.getAll(grpAttribute);
        if (groupNames == null) {
            throw new ValidationException(username + " has no attributes "
                    + grpAttribute);
        }

        String filter = config.getGroupFilter().encode();
        @SuppressWarnings("unchecked")
        List<String> filtered = (List<String>) ldap.search("", filter,
            new GroupAttributeMapper(config));

        List<Long> groups = new ArrayList<Long>();
        for (String grpName : groupNames) {
            if (!filtered.contains(grpName)) {
                log.debug("Group not found by filter: " + grpName);
            } else {
                groups.add(provider.createGroup(grpName, null, false));
            }
        }
        return groups;

    }

}
