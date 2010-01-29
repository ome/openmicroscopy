/*
 *   $Id$
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;

import ome.annotations.PermitAll;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.annotations.RolesAllowed;
import ome.api.IAdmin;
import ome.api.ILdap;
import ome.api.ServiceInterface;
import ome.api.local.LocalLdap;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.LdapUtil;
import ome.security.SecuritySystem;
import ome.security.auth.RoleProvider;
import ome.system.OmeroContext;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides methods for administering user accounts, passwords, as well as
 * methods which require special privileges.
 * 
 * Developer note: As can be expected, to perform these privileged the Admin
 * service has access to several resources that should not be generally used
 * while developing services. Misuse could circumvent security or auditing.
 * 
 * @author Aleksandra Tarkowska, A.Tarkowska@dundee.ac.uk
 * @version $Revision: 1552 $, $Date: 2007-05-23 09:43:33 +0100 (Wed, 23 May
 *          2007) $
 * @see SecuritySystem
 * @see Permissions
 * @since 3.0-M3
 */
@Transactional(readOnly = true)
@RevisionDate("$Date: 2007-05-23 09:43:33 +0100 (Wed, 23 May 2007) $")
@RevisionNumber("$Revision: 1552 $")
public class LdapImpl extends AbstractLevel2Service implements LocalLdap {

    protected final LdapOperations ldapOperations;

    protected final SimpleJdbcOperations jdbc;

    protected final String newUserGroup;

    protected final String groups;

    protected final String attributes;

    protected final String values;

    protected final boolean config;

    protected final RoleProvider roleProvider;

    public LdapImpl(RoleProvider roleProvider, LdapOperations ldapOperations,
            SimpleJdbcOperations jdbc, String newUserGroup, 
            String groups, String attributes, String values, boolean config) {
        this.roleProvider = roleProvider;
        this.ldapOperations = ldapOperations;
        this.jdbc = jdbc;
        this.newUserGroup = newUserGroup;
        this.groups = groups;
        this.attributes = attributes;
        this.values = values;
        this.config = config;
    }

    // ~ System-only interface methods
    // =========================================================================

    @RolesAllowed("system")
    public List<Experimenter> searchAll() {
        EqualsFilter filter = new EqualsFilter("objectClass", "person");
        return ldapOperations.search(DistinguishedName.EMPTY_PATH, filter
                .encode(), new PersonContextMapper());
    }

    @RolesAllowed("system")
    public List<Experimenter> searchByAttribute(String dns, String attr,
            String value) {
        DistinguishedName dn;
        if (dns == null) {
            dn = DistinguishedName.EMPTY_PATH;
        } else {
            dn = new DistinguishedName(dns);
        }

        if (attr != null && !attr.equals("") && value != null
                && !value.equals("")) {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectClass", "person"));
            filter.and(new EqualsFilter(attr, value));

            return ldapOperations.search(dn, filter.encode(),
                    new PersonContextMapper());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @RolesAllowed("system")
    public Experimenter searchByDN(String dns) {
        DistinguishedName dn = new DistinguishedName(dns);
        return (Experimenter) ldapOperations.lookup(dn,
                new PersonContextMapper());
    }

    @RolesAllowed("system")
    public String findDN(String username) {
        DistinguishedName dn;
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("cn", username));
        List<Experimenter> p = ldapOperations.search("", filter.encode(),
                new PersonContextMapper());
        if (p.size() == 1) {
            Experimenter exp = p.get(0);
            dn = new DistinguishedName(exp.retrieve("LDAP_DN").toString());
        } else {
            throw new ApiUsageException(
                    "Cannot find DistinguishedName or more then one 'cn' under the specified base");
        }
        return dn.toString();
    }

    @RolesAllowed("system")
    public Experimenter findExperimenter(String username) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("cn", username));
        List<Experimenter> p = ldapOperations.search("", filter.encode(),
                new PersonContextMapper());
        Experimenter exp = null;
        if (p.size() == 1) {
            exp = p.get(0);
        } else {
            throw new ApiUsageException(
                    "Cannot find DistinguishedName. More then one 'cn' under the specified base");
        }
        return exp;
    }

    @RolesAllowed("system")
    public List<String> searchDnInGroups(String attr, String value) {
        if (attr != null && !attr.equals("") && value != null
                && !value.equals("")) {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectClass", "groupOfNames"));
            filter.and(new EqualsFilter(attr, value));
            return ldapOperations.search("", filter.encode(),
                    new GroupAttributMapper());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @RolesAllowed("system")
    public List<Experimenter> searchByAttributes(String dn,
            String[] attributes, String[] values) {
        if (attributes.length != values.length) {
            return Collections.EMPTY_LIST;
        }
        AndFilter filter = new AndFilter();
        for (int i = 0; i < attributes.length; i++) {
            filter.and(new EqualsFilter(attributes[i], values[i]));
        }
        return ldapOperations.search(new DistinguishedName(dn),
                filter.encode(), new PersonContextMapper());
    }

    @RolesAllowed("system")
    public List<ExperimenterGroup> searchGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @RolesAllowed("system")
    @Transactional(readOnly = false)
    public void setDN(Long experimenterID, String dn) {
        LdapUtil.setDNById(jdbc, experimenterID, dn);
    }

    // Getters and Setters for requiroments
    // =========================================================================

    @RolesAllowed("system")
    public boolean getSetting() {
        return this.config;
    }

    @RolesAllowed("system")
    public List<String> getReqGroups() {
        if (this.groups.equals("")) {
            return Collections.EMPTY_LIST;
        }
        return Arrays.asList(this.groups.split(","));
    }

    @RolesAllowed("system")
    public String[] getReqAttributes() {
        if (this.attributes.equals("")) {
            return new String[] {};
        }
        return this.attributes.split(",");
    }

    @RolesAllowed("system")
    public String[] getReqValues() {
        if (this.values.equals("")) {
            return new String[] {};
        }
        return this.values.split(",");
    }

    @RolesAllowed("system")
    public void setReqAttributes(String[] arg0) {
        // TODO Auto-generated method stub

    }

    @RolesAllowed("system")
    public void setReqGroups(List<String> arg0) {
        // TODO Auto-generated method stub

    }

    @RolesAllowed("system")
    public void setReqValues(String[] arg0) {
        // TODO Auto-generated method stub

    }

    // ~ LOCAL PUBLIC METHODS
    // ========================================================================

    // ~ AttributesMapper
    // =========================================================================

    public static class UidAttributMapper implements AttributesMapper {

        @RolesAllowed("system")
        public Object mapFromAttributes(Attributes attributes)
                throws NamingException {
            ArrayList l = new ArrayList();
            for (NamingEnumeration ae = attributes.getAll(); ae
                    .hasMoreElements();) {
                Attribute attr = (Attribute) ae.next();
                String attrId = attr.getID();
                for (Enumeration vals = attr.getAll(); vals.hasMoreElements();) {
                    DistinguishedName dn = new DistinguishedName((String) vals
                            .nextElement());
                    if (attrId.equals("memberUid")) {
                        l.add(dn);
                    }
                }
            }
            return l;
        }

    }

    public static class GroupAttributMapper implements AttributesMapper {

        @RolesAllowed("system")
        public Object mapFromAttributes(Attributes attributes)
                throws NamingException {
            String groupName = null;
            if (attributes.get("cn") != null) {
                groupName = (String) attributes.get("cn").get();
            }
            return groupName;
        }

    }

    // ~ ContextMapper
    // =========================================================================

    public class PersonContextMapper implements ContextMapper {

        private DistinguishedName dn = new DistinguishedName();

        public DistinguishedName getDn() {
            return dn;
        }

        public void setDn(DistinguishedName dn) {
            this.dn = dn;
        }

        @RolesAllowed("system")
        public Object mapFromContext(Object ctx) {
            DirContextAdapter context = (DirContextAdapter) ctx;
            DistinguishedName dn = new DistinguishedName(context.getDn());
            try {
                dn.addAll(0, new DistinguishedName(getBase()));
            } catch (InvalidNameException e) {
                return null;
            }
            setDn(dn);

            Experimenter person = new Experimenter();
            if (context.getStringAttribute("cn") != null) {
                person.setOmeName(context.getStringAttribute("cn"));
            }
            person.setLastName("");
            if (context.getStringAttribute("sn") != null) {
                person.setLastName(context.getStringAttribute("sn"));
            }
            person.setFirstName("");
            if (context.getStringAttribute("givenName") != null) {
                person.setFirstName(context.getStringAttribute("givenName"));
            }
            if (context.getStringAttribute("mail") != null) {
                person.setEmail(context.getStringAttribute("mail"));
            }
            person.putAt("LDAP_DN", dn.toString());
            return person;
        }

    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return ILdap.class;
    }

    /**
     * Gets base from the OmeroContext -> Bean: contextSource
     * 
     * @return String
     */
    public String getBase() {
        String base = null;
        LdapContextSource ctx = (LdapContextSource) OmeroContext
                .getManagedServerContext().getBean("contextSource");
        try {
            base = ctx.getReadOnlyContext().getNameInNamespace();
        } catch (NamingException e) {
            throw new ApiUsageException(
                    "Cannot get BASE from ContextSource. Naming exception! "
                            + e.toString());
        }
        return base;

    }

    // ~ LocalLdap - Authentication
    // =========================================================================

    /**
     * Creates the initial context with no connection request controls in order
     * to check authentication. If authentication fails, this method throws
     * a {@link SecurityViolation}.
     * 
     * @return {@link javax.naming.ldap.LdapContext}
     */
    protected void isAuthContext(String username, String password) {
        // Set up environment for creating initial context
        LdapContextSource ctx = (LdapContextSource) OmeroContext
                .getManagedServerContext().getBean("contextSource");
        Hashtable<String, String> env = new Hashtable<String, String>(5, 0.75f);
        try {
            env = (Hashtable<String, String>) ctx.getReadOnlyContext()
                    .getEnvironment();

            if (username != null && !username.equals("")) {
                env.put(Context.SECURITY_PRINCIPAL, username);
                if (password != null) {
                    env.put(Context.SECURITY_CREDENTIALS, password);
                }
            }
            new InitialLdapContext(env, null);
        } catch (AuthenticationException authEx) {
            throw new SecurityViolation("Authentication falilure! "
                    + authEx.toString());
        } catch (NamingException e) {
            throw new SecurityViolation("Naming exception! " + e.toString());
        }
    }

    /**
     * Validates password for base. Base is user's DN. When context was created
     * successful specified requirements are valid.
     * 
     * @return boolean
     */
    @RolesAllowed("system")
    public boolean validatePassword(String base, String password) {
        try {
            isAuthContext(base, password);
        } catch (SecurityViolation sv) {
            return false;
        }
        // Check requiroments
        return validateRequiroments(base);
    }

    /**
     * Gets user from LDAP for checking him by requirements and setting his
     * details on DB
     * 
     * @return {@link ome.system.ServiceFactory}
     */
    @Transactional(readOnly = false)
    @RolesAllowed("system")
    public boolean createUserFromLdap(String username, String password) {
        // Find user by DN
        Experimenter exp = findExperimenter(username);
        DistinguishedName dn = new DistinguishedName(exp.retrieve("LDAP_DN")
                .toString());

        // DistinguishedName converted toString includes spaces
        if (!validateRequiroments(dn.toString())) {
            return false;
        }

        // Valid user's password
        boolean access = validatePassword(dn.toString(), password);

        if (access) {
            // If validation is successful create new user in DB
            long gid = roleProvider.createGroup(newUserGroup, null, false);
            long uid = roleProvider.createExperimenter(exp,
                    new ExperimenterGroup(gid, false),
                    new ExperimenterGroup(
                            this.sec.getSecurityRoles().getUserGroupId(), false));
            // Set user's DN in PASSWORD table (add suffix on the beginning)
            setDN(uid, dn.toString());
        }
        return access;
    }

    /**
     * Valids specyfied requirements for base (groups, attributes)
     * 
     * @return boolean
     */
    @RolesAllowed("system")
    public boolean validateRequiroments(String base) {
        boolean result = false;

        // list of groups
        List<String> groups = getReqGroups();
        // List of attributes
        String[] attrs = getReqAttributes();
        // List of attributes
        String[] vals = getReqValues();

        if (attrs.length != vals.length) {
            throw new ApiUsageException(
                    "Configuration exception. Attributes should have value on the omero.properties.");
        }

        // if groups
        if (groups.size() > 0) {
            List usergroups = searchDnInGroups("member", base);
            result = isInGroups(groups, usergroups);
        } else {
            result = true;
        }

        // if attributes
        if (result) {

            if (attrs.length > 0) {
                // cut DN
                DistinguishedName dn = new DistinguishedName(base);
                DistinguishedName baseDn = new DistinguishedName(getBase());
                for (int i = 0; i < baseDn.size(); i++) {
                    dn.removeFirst();
                }

                List<Experimenter> l = searchByAttributes(dn.toString(), attrs,
                        vals);
                if (l.size() <= 0) {
                    result = false;
                } else {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Checks that user's group list contains require groups. If one of user's
     * groups is on require groups' list will return true.
     * 
     * @return boolean
     */
    @RolesAllowed("system")
    public boolean isInGroups(List groups, List usergroups) {
        // user is not in groups
        if (usergroups.size() <= 0) {
            return false;
        }
        boolean flag = false;
        // checks containing
        for (int i = 0; i < usergroups.size(); i++) {
            if (groups.contains(usergroups.get(i))) {
                flag = true;
            }
        }
        return flag;
    }

}
