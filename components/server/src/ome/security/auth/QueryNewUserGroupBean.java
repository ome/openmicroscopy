/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ome.conditions.ValidationException;
import ome.security.SecuritySystem;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class QueryNewUserGroupBean implements NewUserGroupBean, NewUserGroupOwnerBean {

    private final static Logger log = LoggerFactory.getLogger(QueryNewUserGroupBean.class);

    private final String grpQuery;

    public QueryNewUserGroupBean(String grpQuery) {
        this.grpQuery = grpQuery;
    }

    private String parseQuery(final AttributeSet attrSet, final String query) {
        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("@{",
                "}", null, false);
        return helper.replacePlaceholders(query,
                new PlaceholderResolver() {
                    public String resolvePlaceholder(String arg0) {
                        if (attrSet.size(arg0) > 1) {
                            throw new ValidationException(
                                    "Multivalued property used in @{} format:"
                                            + query + "="
                                            + attrSet.getAll(arg0).toString());
                        }
                        return attrSet.getFirst(arg0);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private List<Long> _groups(boolean owner, String username, LdapConfig config,
            LdapOperations ldap, RoleProvider provider, final AttributeSet attrSet) {

        String query = parseQuery(attrSet, grpQuery);
        String ownerQuery = null;
        if (owner) {
            ownerQuery = config.getNewUserGroupOwner();
            if (StringUtils.isBlank(ownerQuery)) {
                log.debug("Owner query disabled");
                return Collections.emptyList(); // EARLY EXIT
            }
            ownerQuery = parseQuery(attrSet, ownerQuery);
        }

        AndFilter and = new AndFilter();
        and.and(config.getGroupFilter());
        and.and(new HardcodedFilter(query));
        if (owner) {
            and.and(new HardcodedFilter(ownerQuery));
        }

        log.debug("Running query: {}", and.encode());
        List<String> groupNames = (List<String>) ldap.search("", and.encode(),
            new GroupAttributeMapper(config));

        List<Long> groups = new ArrayList<Long>(groupNames.size());
        for (String groupName : groupNames) {
            groups.add(provider.createGroup(groupName, null, false, true));
        }
        return groups;

    }

    @Override
    public List<Long> groups(String username, LdapConfig config,
            LdapOperations ldap, RoleProvider provider,
            AttributeSet attrSet) {
        return _groups(false, username, config, ldap, provider, attrSet);
    }

    @Override
    public List<Long> ownerOfGroups(String username, LdapConfig config,
            LdapOperations ldap, RoleProvider provider, AttributeSet attrSet) {
        return _groups(true, username, config, ldap, provider, attrSet);
    }
}
