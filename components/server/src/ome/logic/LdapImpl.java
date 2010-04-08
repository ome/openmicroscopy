/*
 *   $Id$
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;

import ome.annotations.NotNull;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.annotations.RolesAllowed;
import ome.api.ILdap;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecuritySystem;
import ome.security.auth.LdapConfig;
import ome.security.auth.PersonContextMapper;
import ome.security.auth.RoleProvider;
import ome.system.Roles;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
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
public class LdapImpl extends AbstractLevel2Service implements ILdap {

    private final SimpleJdbcOperations jdbc;

    private final RoleProvider provider;

    private final LdapContextSource ctx;

    private final LdapOperations ldap;

    private final LdapConfig config;

    private final Roles roles;

    public LdapImpl(
            LdapContextSource ctx,
            LdapOperations ldap, Roles roles,
            LdapConfig config,
            RoleProvider roleProvider,
            SimpleJdbcOperations jdbc) {
        this.ctx = ctx;
        this.jdbc = jdbc;
        this.ldap = ldap;
        this.roles = roles;
        this.config = config;
        this.provider = roleProvider;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return ILdap.class;
    }

    // ~ System-only interface methods
    // =========================================================================

    @SuppressWarnings("unchecked")
    @RolesAllowed("system")
    public List<Experimenter> searchAll() {
        EqualsFilter filter = new EqualsFilter("objectClass", "person");
        return ldap.search(DistinguishedName.EMPTY_PATH, filter
                .encode(), getContextMapper());
    }

    @SuppressWarnings("unchecked")
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

            return ldap.search(dn, filter.encode(),
                    getContextMapper());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @RolesAllowed("system")
    public Experimenter searchByDN(String dns) {
        DistinguishedName dn = new DistinguishedName(dns);
        return (Experimenter) ldap.lookup(dn, getContextMapper());
    }

    @RolesAllowed("system")
    @SuppressWarnings("unchecked")
    public String findDN(String username) {
        DistinguishedName dn;
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("cn", username));
        List<Experimenter> p = ldap.search("", filter.encode(),
                getContextMapper());
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
        List<Experimenter> p = ldap.search("", filter.encode(),
                new PersonContextMapper(getBase()));
        Experimenter exp = null;
        if (p.size() == 1) {
            exp = p.get(0);
        } else {
            throw new ApiUsageException(
                    "Cannot find unique DistinguishedName: found=" + p.size());
        }
        return exp;
    }

    @RolesAllowed("system")
    @SuppressWarnings("unchecked")
    public List<String> searchDnInGroups(String attr, String value) {
        if (attr != null && !attr.equals("") && value != null
                && !value.equals("")) {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectClass", "groupOfNames"));
            filter.and(new EqualsFilter(attr, value));
            return ldap.search("", filter.encode(),
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
        return ldap.search(new DistinguishedName(dn),
                filter.encode(), getContextMapper());
    }

    @RolesAllowed("system")
    public List<ExperimenterGroup> searchGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @RolesAllowed("system")
    @Transactional(readOnly = false)
    public void setDN(@NotNull Long experimenterID, String dn) {
        int results = jdbc
        .update(
                "update password set dn = ? where experimenter_id = ? ",
                dn, experimenterID);
        if (results < 1) {
                results = jdbc.update("insert into password values (?,?,?) ",
                        experimenterID, null, dn);
        }
    }

    @RolesAllowed("system")
    public boolean getSetting() {
        return config.isEnabled();
    }

    // ~ System-only interface methods
    // =========================================================================


    //
    // WRITES
    //

    /**
     * Gets user from LDAP for checking him by requirements and setting his
     * details on DB
     *
     * @return {@link ome.system.ServiceFactory}
     */
    public boolean createUserFromLdap(String username, String password) {

        // Find user by DN
        Experimenter exp = findExperimenter(username);
        DistinguishedName dn = new DistinguishedName(exp.retrieve("LDAP_DN")
                .toString());

        // DistinguishedName converted toString includes spaces
        if (!validateRequirements(dn.toString())) {
            return false;
        }

        // Valid user's password
        boolean access = validatePassword(dn.toString(), password);

        if (access) {
            // If validation is successful create new user in DB
            long gid = provider.createGroup(config.getNewUserGroup(), null, false);
            long uid = provider.createExperimenter(exp,
                    new ExperimenterGroup(gid, false),
                    new ExperimenterGroup(roles.getUserGroupId(), false));
            // Set user's DN in PASSWORD table (add suffix on the beginning)
            setDN(uid, dn.toString());
        }
        return access;
    }

    //
    // READS
    //

    /**
     * Validates password for base. Base is user's DN. When context was created
     * successful specified requirements are valid.
     *
     * @return boolean
     */
    public boolean validatePassword(String base, String password) {
        try {
            isAuthContext(base, password);
        } catch (SecurityViolation sv) {
            return false;
        }
        return validateRequirements(base);
    }

    public List<Map<String, Object>> lookupLdapAuthExperimenters() {
        return jdbc
                .queryForList(
                        "select dn, experimenter_id from password where dn is not null ");
    }

    public String lookupLdapAuthExperimenter(Long id) {
        String s;

        try {
            s = jdbc
                    .queryForObject(
                            "select dn from password where dn is not null and experimenter_id = ? ",
                            String.class, id);
        } catch (EmptyResultDataAccessException e) {
            s = null;
        }

        return s;
    }







    public ContextMapper getContextMapper() {
        return new PersonContextMapper(getBase());
    }

    // Helpers
    // =========================================================================

    /**
     * Valids specyfied requirements for base (groups, attributes)
     *
     * @return boolean
     */
    private boolean validateRequirements(String base) {
        boolean result = false;

        String[] groups = config.getGroups();
        String[] attrs = config.getAttributes();
        String[] vals = config.getValues();

        if (attrs.length != vals.length) {
            throw new ApiUsageException(
                    "Configuration exception. Attributes should have value on the omero.properties.");
        }

        // if groups
        if (groups.length > 0) {
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
     * Creates the initial context with no connection request controls in order
     * to check authentication. If authentication fails, this method throws
     * a {@link SecurityViolation}.
     *
     * @return {@link javax.naming.ldap.LdapContext}
     */
    @SuppressWarnings("unchecked")
    private void isAuthContext(String username, String password) {

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

    private String getBase() {
        String base = null;
        try {
            base = ctx.getReadOnlyContext().getNameInNamespace();
        } catch (NamingException e) {
            throw new ApiUsageException(
                    "Cannot get BASE from ContextSource. Naming exception! "
                            + e.toString());
        }
        return base;

    }

    /**
     * Checks that user's group list contains require groups. If one of user's
     * groups is on require groups' list will return true.
     *
     * @return boolean
     */
    private boolean isInGroups(String[] groups, List<String> usergroups) {
        // user is not in groups
        if (usergroups.size() == 0) {
            return false;
        }
        boolean flag = false;
        // checks containing
        for (int i = 0; i < usergroups.size(); i++) {
            if (Arrays.asList(groups).contains(usergroups.get(i))) {
                flag = true;
            }
        }
        return flag;
    }

    private static class GroupAttributMapper implements AttributesMapper {

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

}
