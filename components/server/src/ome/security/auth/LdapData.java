/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.security.auth;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.auth.mappers.AbstractContextMapper;
import ome.security.auth.mappers.GroupContextMapper;
import ome.security.auth.mappers.PersonContextMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.HardcodedFilter;

/**
 * DAO for all Ldap access. Similar to {@link ome.util.SqlAction} this class
 * wraps access to a Spring {@link LdapTemplate} so as to guarantee that
 * OMERO-specific contracts are satisfied, including case-sensitivity. Logging
 * is performed at the DEBUG level on nearly every public method entry and on
 * exit regardless of return status.
 * 
 * @since 4.4.4
 */
public class LdapData
{

    private final static Log log = LogFactory.getLog(LdapData.class);

    protected final LdapTemplate ldap;

    protected final LdapConfig config;

    public LdapData(LdapTemplate ldap, LdapConfig config)
    {
        this.ldap = ldap;
        this.config = config;
    }

    //
    // Logging
    //

    protected void debug(String fmt, Object... args)
    {
        if (log.isDebugEnabled())
        {
            log.debug(String.format(fmt, args));
        }
    }

    //
    // Public USER methods
    //

    /**
     * Tries to acquire {@link #isAuthContext(String, String) an authorized
     * context} with the given username and password. If successful, true is
     * returned. Otherwise, false.
     * 
     * @param dn
     * @param password
     * @return
     */
    public boolean validatePassword(String dn, String password)
    {
        debug("validatePassword(%s, *****)", dn);
        try
        {
            isAuthContext(dn, password);
            debug("validatePassword => true");
            return true;
        }
        catch (SecurityViolation sv)
        {
            debug("validatePassword => false");
            return false;
        }
        catch (RuntimeException rt)
        {
            debug("validatePassword: Exception", rt);
            throw rt;
        }
    }

    /**
     * Run a query using the user filter and return the number of items returned
     * from the LDAP server.
     * 
     * @return
     */
    public int getUserCount()
    {
        debug("getUserCount()");
        try
        {
            int count = ldap.search(DistinguishedName.EMPTY_PATH,
                config.getUserFilter().encode(), new ContextMapper()
                {

                    public Object mapFromContext(Object arg0)
                    {
                        return arg0.toString();
                    }
                }).size();
            debug("getUserCount => %s", count);
            return count;
        }
        catch (RuntimeException rt)
        {
            debug("getUserCount: Exception", rt);
            throw rt;
        }
    }

    /**
     * Lookup a single user by his/her dn.
     * 
     * @param dns
     * @return Never null.
     */
    public Experimenter getUserByDn(String dns)
    {
        debug("getUsersByDn(%s)", dns);
        try
        {
            DistinguishedName dn = new DistinguishedName(dns);
            Experimenter rv = (Experimenter) ldap
                .lookup(dn, getContextMapper());
            debug("getUserByDn: count=%s", rv);
            return rv;
        }
        catch (RuntimeException rt)
        {
            debug("getUsersByDn: Exception", rt);
            throw rt;
        }
    }

    /**
     * 
     * @param username
     * @return
     */
    public Experimenter getUserByName(String username)
    {
        debug("getUserByName(%s)", username);
        try
        {
            PersonContextMapper mapper = getContextMapper();
            Experimenter exp = mapUserName(username, mapper);
            debug("getUserByName => %s", exp);
            return exp;
        }
        catch (RuntimeException rt)
        {
            debug("getUserByName: Exception", rt);
            throw rt;
        }
    }

    public String getUserDnByName(String username)
    {
        debug("getUserDnByName(%s)", username);
        try
        {
            PersonContextMapper mapper = getContextMapper();
            Experimenter exp = mapUserName(username, mapper);
            String dn = mapper.getDn(exp);
            debug("getUserDnByName => %s", dn);
            return dn;
        }
        catch (RuntimeException rt)
        {
            debug("getUserDnByName: Exception", rt);
            throw rt;
        }
    }

    /**
     * Look up users by AND'ing a {@link EqualsFilter} with the
     * {@link LdapConfig#usernameFilter(String)}.
     * 
     * @param dns
     *            If null, will be parsed as the
     *            {@link DistinguishedName#EMPTY_PATH}
     * @param attributes
     *            Not null. Size must match values size.
     * @param values
     *            Not null. Size must match attributes size.
     * @return A list of experimenters. Never null.
     */
    @SuppressWarnings("unchecked")
    public List<Experimenter> getUserByAttributes(String dns,
        String[] attributes, String[] values)
    {
        debug("getUserByAttributes(%s, %s, %s)", dns,
            Arrays.toString(attributes), Arrays.toString(values));

        try
        {
            AndFilter filter = new AndFilter();
            for (int i = 0; i < attributes.length; i++)
            {
                filter.and(new EqualsFilter(attributes[i], values[i]));
            }

            List<Experimenter> rv = ldap.search(parseDnString(dns),
                filter.encode(), getContextMapper());
            debug("getUserByAttributess => count=%s", rv.size());
            return rv;
        }
        catch (RuntimeException rt)
        {
            debug("getUserByAttributes: Exception", rt);
            throw rt;
        }
    }

    public List<Experimenter> getAllUsers()
    {
        debug("getAllUsers()");
        try
        {
            @SuppressWarnings("unchecked")
            List<Experimenter> rv = ldap.search(DistinguishedName.EMPTY_PATH,
                config.getUserFilter().encode(), getContextMapper());
            debug("getAllUsers => count=%s", rv.size());
            return rv;
        }
        catch (RuntimeException rt)
        {
            debug("getAllUsers: Exception", rt);
            throw rt;
        }
    }

    //
    // Public GROUP methods
    //

    public int getGroupCount()
    {
        debug("getGroupCount()");
        try
        {
            int count = ldap.search(DistinguishedName.EMPTY_PATH,
                config.getGroupFilter().encode(), new ContextMapper()
                {

                    public Object mapFromContext(Object arg0)
                    {
                        return arg0.toString();
                    }
                }).size();
            debug("getGroupCount => %s", count);
            return count;
        }
        catch (RuntimeException rt)
        {
            debug("getGroupCount: Exception", rt);
            throw rt;
        }
    }

    public String getGroupDnByName(String username)
    {
        debug("getGroupDnByName(%s)", username);
        try
        {
            GroupContextMapper mapper = getGroupContextMapper();
            ExperimenterGroup grp = mapGroupName(username, mapper);
            String dn = mapper.getDn(grp);
            debug("getGroupDnByName => %s", dn);
            return dn;
        }
        catch (RuntimeException rt)
        {
            debug("getGroupDnByName: Exception", rt);
            throw rt;
        }
    }

    /**
     * Look up group names by AND'ing a {@link EqualsFilter} with the
     * {@link LdapConfig#groupnameFilter(String)}.
     * 
     * @param dns
     *            If null, will be parsed as the
     *            {@link DistinguishedName#EMPTY_PATH}
     * @param attr
     *            Not null.
     * @param value
     *            Not null.
     * @return A list of groups. Never null.
     */
    @SuppressWarnings("unchecked")
    public List<String> getGroupNamesByAttribute(String attr, String value)
    {
        debug("getGroupByAttribute(%s, %s)", attr, value);
        try
        {
            AndFilter filter = new AndFilter();
            filter.and(config.getGroupFilter());
            filter.and(new EqualsFilter(attr, value));

            List<String> rv = ldap.search("", filter.encode(),
                new GroupAttributeMapper(config));
            debug("getGroupByAttribute => count=%s", rv.size());
            return rv;
        }
        catch (RuntimeException rt)
        {
            debug("getGroupByAttribute: Exception", rt);
            throw rt;
        }
    }

    /**
     * @param query
     *            Possibly null, in which case only the group filter will be
     *            applied.
     * @return
     */
    public List<String> getAllGroupNames(String query)
    {
        debug("getAllGroupNames(%s)", query);
        try
        {
            final GroupAttributeMapper mapper = new GroupAttributeMapper(config);
            String filter = null;
            if (query == null)
            {
                filter = config.getGroupFilter().encode();
            }
            else
            {
                AndFilter and = new AndFilter();
                and.and(config.getGroupFilter());
                and.and(new HardcodedFilter(query));
                filter = and.encode();
            }

            @SuppressWarnings("unchecked")
            final List<String> rv = (List<String>) ldap.search("", filter,
                mapper);
            debug("getAllGroupNames: count=%s", rv.size());
            return rv;
        }
        catch (RuntimeException rt)
        {
            debug("getAllGroupNames: Exception", rt);
            throw rt;
        }
    }

    //
    // Helpers
    //

    /**
     * Perform modifications on an LDAP entity.
     *
     * @param string Not null.
     * @param mods Not null with no null items.
     */
    public void modifyAttributes(String string, ModificationItem[] mods)
    {
        debug("modifyAttributes(%s)", string);
        try {
            ldap.modifyAttributes(string, mods);
            debug("modifyAttributes: OK");
        } catch (RuntimeException rt) {
            debug("modifyAttributes: Exception", rt);
            throw rt;
        }
    }

    /**
     * Lookup the {@link AbstractContextMapper#LDAP_DN} field from
     * {@link IObject#retrieve(String)}.
     * 
     * @param obj
     *            If null, null will be returned.
     * @return Possibly null value.
     */
    public String extractDN(IObject obj)
    {
        if (obj == null)
        {
            return null;
        }
        return getContextMapper().getDn(obj);
    }

    protected PersonContextMapper getContextMapper()
    {
        return new PersonContextMapper(config, config.getBase());
    }

    protected PersonContextMapper getContextMapper(String attr)
    {
        return new PersonContextMapper(config, config.getBase(), attr);
    }

    protected GroupContextMapper getGroupContextMapper()
    {
        return new GroupContextMapper(config, config.getBase());
    }

    /**
     * Creates an {@link AttributeSet} based on the username and the optional
     * attribute.
     * 
     * @param username
     *            Not null.
     * @param attribute
     *            Possibly null, in which case the {@link PersonContextMapper}
     *            will not have an attribute enforced.
     * @return
     */
    public AttributeSet getAttributeSet(String username, String attribute)
    {
        PersonContextMapper mapper = null;
        if (attribute == null)
        {
            mapper = getContextMapper();
        }
        else
        {
            mapper = getContextMapper(attribute);
        }
        Experimenter exp = mapUserName(username, mapper);
        String dn = mapper.getDn(exp);
        AttributeSet attrSet = mapper.getAttributeSet(exp);
        attrSet.put("dn", dn); // For queries
        return attrSet;
    }

    /**
     * Mapping a username to an {@link Experimenter}. This handles checking the
     * username for case exactness. This should be done at the LDAP level, but
     * Apache DS (the testing framework used) does not yet support
     * :caseExactMatch:.
     * 
     * When it does, the check here can be removed.
     * 
     * @param username
     * @param mapper
     * @return a non null Experimenter.
     * @see ticket:2557
     */
    protected Experimenter mapUserName(String username,
        PersonContextMapper mapper)
    {
        Filter filter = config.usernameFilter(username);
        @SuppressWarnings("unchecked")
        List<Experimenter> p = ldap.search("", filter.encode(),
            mapper.getControls(), mapper);

        if (p.size() == 1 && p.get(0) != null)
        {
            Experimenter e = p.get(0);
            if (e.getOmeName().equals(username))
            {
                return p.get(0);
            }
        }
        throw new ApiUsageException(String.format(
            "Cannot find unique DistinguishedName '%s': found=%s", username,
            p.size()));

    }

    @SuppressWarnings("unchecked")
    protected ExperimenterGroup mapGroupName(String groupname,
        GroupContextMapper mapper)
    {
        Filter filter = config.groupnameFilter(groupname);
        List<ExperimenterGroup> p = ldap.search("", filter.encode(), mapper);

        if (p.size() == 1 && p.get(0) != null)
        {
            ExperimenterGroup g = p.get(0);
            if (g.getName().equals(groupname))
            {
                return p.get(0);
            }
        }
        throw new ApiUsageException(String.format(
            "Cannot find unique DistinguishedName '%s': found=%s", groupname,
            p.size()));

    }

    /**
     * Creates the initial context with no connection request controls in order
     * to check authentication. If authentication fails, this method throws a
     * {@link SecurityViolation}.
     * 
     * @return {@link javax.naming.ldap.LdapContext}
     */
    @SuppressWarnings("unchecked")
    protected void isAuthContext(String username, String password)
    {

        Hashtable<String, String> env = new Hashtable<String, String>(5, 0.75f);
        try
        {
            env = (Hashtable<String, String>) ldap.getContextSource()
                .getReadOnlyContext().getEnvironment();

            if (username != null && !username.equals(""))
            {
                env.put(Context.SECURITY_PRINCIPAL, username);
                if (password != null)
                {
                    env.put(Context.SECURITY_CREDENTIALS, password);
                }
            }
            new InitialLdapContext(env, null);
        }
        catch (AuthenticationException authEx)
        {
            throw new SecurityViolation("Authentication falilure! "
                + authEx.toString());
        }
        catch (NamingException e)
        {
            throw new SecurityViolation("Naming exception! " + e.toString());
        }
    }

    /**
     * Create a new {@link DistinguishedName} instance from the argument. If the
     * value is null, then {@link DistinguishedName#EMPTY_PATH} will be
     * returned.
     * 
     * @param dns
     *            possibly null.
     * @return Never null.
     */
    protected DistinguishedName parseDnString(String dns)
    {
        DistinguishedName dn;
        if (dns == null)
        {
            dn = DistinguishedName.EMPTY_PATH;
        }
        else
        {
            dn = new DistinguishedName(dns);
        }
        return dn;
    }

}
