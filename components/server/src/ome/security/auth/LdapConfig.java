/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.HashMap;
import java.util.Map;

import ome.security.SecuritySystem;

import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.HardcodedFilter;

/**
 * Static methods for dealing with LDAP (DN) and the "password" table. Used
 * primarily by {@link ome.security.JBossLoginModule}
 *
 * @author Aleksandra Tarkowska, A.Tarkowska at dundee.ac.uk
 * @see SecuritySystem
 * @see ome.logic.LdapImpl
 * @since 3.0-Beta3
 */
public class LdapConfig {

    private final Map<String, String> groupMapping;

    private final Map<String, String> userMapping;

    private final HardcodedFilter userFilter;

    private final HardcodedFilter groupFilter;

    private final String newUserGroup;

    private final boolean enabled;

    private final boolean syncOnLogin;

    /**
     * Passes all values to
     * {@link #LdapConfig(boolean, String, String, String, String, String, boolean)}
     * but sets {@link #syncOnLogin} to false.
     *
     * @param enabled
     * @param newUserGroup
     * @param userFilter
     * @param groupFilter
     * @param userMapping
     * @param groupMapping
     */
    public LdapConfig(boolean enabled, String newUserGroup,
            String userFilter, String groupFilter,
            String userMapping, String groupMapping) {
        this(enabled, newUserGroup, userFilter, groupFilter,
                userMapping, groupMapping, false);
    }

    /**
     * Base constructor which stores all {@link #parse(String)} and stores all
     * values for later lookup.
     *
     * @param enabled
     * @param newUserGroup
     * @param userFilter
     * @param groupFilter
     * @param userMapping
     * @param groupMapping
     * @param syncOnLogin
     */
    public LdapConfig(boolean enabled, String newUserGroup,
            String userFilter, String groupFilter,
            String userMapping, String groupMapping,
            boolean syncOnLogin) {
        this.enabled = enabled;
        this.newUserGroup = newUserGroup;
        this.userFilter = new HardcodedFilter(userFilter);
        this.groupFilter = new HardcodedFilter(groupFilter);
        this.userMapping = parse(userMapping);
        this.groupMapping = parse(groupMapping);
        this.syncOnLogin = syncOnLogin;
    }

    // Helpers

    public Filter usernameFilter(String username) {
        String attributeKey = getUserAttribute("omeName");
        AndFilter filter = new AndFilter();
        filter.and(getUserFilter());
        filter.and(new EqualsFilter(attributeKey, username));
        return filter;
    }

    public Filter groupnameFilter(String groupname) {
        String attributeKey = getGroupAttribute("name");
        AndFilter filter = new AndFilter();
        filter.and(getGroupFilter());
        filter.and(new EqualsFilter(attributeKey, groupname));
        return filter;
    }

    // Accessors

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSyncOnLogin() {
        return syncOnLogin;
    }

    public String getNewUserGroup() {
        return newUserGroup;
    }

    public Filter getUserFilter() {
        return this.userFilter;
    }

    public Filter getGroupFilter() {
        return this.groupFilter;
    }

    public String getUserAttribute(String key) {
        return userMapping.get(key);
    }

    public String getGroupAttribute(String key) {
        return groupMapping.get(key);
    }

    protected Map<String, String> parse(String mapping) {
        Map<String, String> rv = new HashMap<String, String>();
        String[] mappings = mapping.split("[\\n\\s;:,]+");
        for (int i = 0; i < mappings.length; i++) {
            String[] parts = mappings[i].split("=", 2);
            rv.put(parts[0], (parts.length < 2 ? null : parts[1]));
        }
        return rv;
    }
}
