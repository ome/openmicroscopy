/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.HashMap;
import java.util.Map;

import ome.conditions.InternalException;
import ome.security.SecuritySystem;

import org.springframework.ldap.core.DistinguishedName;
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

    private final String base;

    private final String newUserGroup;

    private final String newUserGroupOwner;

    private final boolean enabled;

    private final boolean syncOnLogin;


    /**
     * Sets {@link #syncOnLogin} to false and {@link #base} to null.
     */
    public LdapConfig(boolean enabled, String newUserGroup, String userFilter,
        String groupFilter, String userMapping, String groupMapping) {
        this(enabled, newUserGroup, userFilter, groupFilter, userMapping,
            groupMapping, false, null);
    }

    /**
     * Sets {@link #base} to null.
     */
    public LdapConfig(boolean enabled, String newUserGroup, String userFilter,
        String groupFilter, String userMapping, String groupMapping, boolean syncOnLogin) {
        this(enabled, newUserGroup, userFilter, groupFilter, userMapping,
            groupMapping, syncOnLogin, null);
    }

    /**
     * Sets {@link #newUserGroupOwner} to null.
     */
    public LdapConfig(boolean enabled, String newUserGroup, String userFilter,
        String groupFilter, String userMapping, String groupMapping, boolean syncOnLogin,
        String base) {
        this(enabled, newUserGroup, userFilter, groupFilter, userMapping,
            groupMapping, syncOnLogin, base, null);
    }

    /**
     * Base constructor which stores all {@link #parse(String)} and stores all
     * values for later lookup.
     */
    public LdapConfig(boolean enabled,
            String newUserGroup,
            String userFilter, String groupFilter,
            String userMapping, String groupMapping,
            boolean syncOnLogin, String base,
            String newUserGroupOwner) {
        this.enabled = enabled;
        this.newUserGroup = newUserGroup;
        this.userFilter = new HardcodedFilter(userFilter);
        this.groupFilter = new HardcodedFilter(groupFilter);
        this.userMapping = parse(userMapping);
        this.groupMapping = parse(groupMapping);
        this.syncOnLogin = syncOnLogin;
        this.base = base;
        this.newUserGroupOwner = newUserGroupOwner;
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

    /**
     * Calculate the relative DN based on the current base. For example,
     * if the base is "ou=example" and the fullDNString is
     * "cn=myuser,ou=example", then the returned DN will be "cn=myuser".
     *
     * Note: if {@link #base} is null, then this will throw an exception.
     *
     * @param full Not null.
     * @return Never null.
     */
    public DistinguishedName relativeDN(String fullDNString) {
        DistinguishedName full = new DistinguishedName(fullDNString);
        DistinguishedName base = new DistinguishedName(this.base);

        if (this.base.trim().length() == 0) {
            return full;
        } else if (base.equals(full)) {
            return new DistinguishedName("");
        } else if (!full.startsWith(base)) {
            throw new InternalException(String.format(
                "Full DN (%s) does not start with base DN (%s)",
                full, base));
        } else {
            full.removeFirst(base);
            return full;
        }
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

    public String getNewUserGroupOwner() {
        return newUserGroupOwner;
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
