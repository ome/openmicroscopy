/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.ArrayList;
import java.util.List;

import ome.conditions.ValidationException;
import ome.security.SecuritySystem;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * Handles the ":query:" specifier from etc/omero.properties.
 *
 * The string following ":query:" is interpreted as an LDAP query to be run in
 * combination with the "omero.ldap.group_filter" value. Properties of the form
 * "${}" will be replaced with found user properties.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @since Beta4.2
 */
public class QueryNewUserGroupBean implements NewUserGroupBean {

    private final String grpQuery;

    public QueryNewUserGroupBean(String grpQuery) {
        this.grpQuery = grpQuery;
    }

    @SuppressWarnings("unchecked")
    public List<Long> groups(String username, LdapConfig config,
            LdapOperations ldap, RoleProvider provider,
            final AttributeSet attrSet) {

        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("@{",
                "}", null, false);
        String query = helper.replacePlaceholders(grpQuery,
                new PlaceholderResolver() {
                    public String resolvePlaceholder(String arg0) {
                        if (attrSet.size(arg0) > 1) {
                            throw new ValidationException(
                                    "Multivalued property used in @{} format:"
                                            + grpQuery + "="
                                            + attrSet.getAll(arg0).toString());
                        }
                        return attrSet.getFirst(arg0);
                    }
                });
        AndFilter and = new AndFilter();
        and.and(config.getGroupFilter());

        and.and(new HardcodedFilter(query));
        List<String> groupNames = (List<String>) ldap.search("", and.encode(),
            new GroupAttributeMapper(config));

        List<Long> groups = new ArrayList<Long>(groupNames.size());
        for (String groupName : groupNames) {
            groups.add(provider.createGroup(groupName, null, false, true));
        }
        return groups;

    }

}
