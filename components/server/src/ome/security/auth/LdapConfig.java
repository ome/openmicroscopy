/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.logic.LdapImpl.GroupAttributMapper;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecuritySystem;
import ome.system.Roles;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

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

    private final List<String> groups = new ArrayList<String>();

    private final List<String> attributes = new ArrayList<String>();

    private final List<String> values = new ArrayList<String>();

    private final String newUserGroup;

    private final boolean enabled;

    public LdapConfig(boolean enabled, String newUserGroup, String[] groups, String[] attributes, String[] values) {
        this.enabled = enabled;
        this.newUserGroup = newUserGroup;
        this.groups.addAll(Arrays.asList(groups));
        this.attributes.addAll(Arrays.asList(attributes));
        this.values.addAll(Arrays.asList(values));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getNewUserGroup() {
        return newUserGroup;
    }

    public String[] getGroups() {
        return (String[]) groups.toArray(new String[groups.size()]);
    }

    public String[] getAttributes() {
        return (String[]) attributes.toArray(new String[attributes.size()]);
    }

    public String[] getValues() {
        return (String[]) values.toArray(new String[values.size()]);
    }

}
