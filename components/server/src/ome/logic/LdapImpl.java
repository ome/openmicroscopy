/*
 *   $Id$
 *
 *   Copyright 2007 - 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import ome.annotations.RolesAllowed;
import ome.api.ILdap;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.parameters.Parameters;
import ome.security.SecuritySystem;
import ome.security.auth.AttributeNewUserGroupBean;
import ome.security.auth.AttributeSet;
import ome.security.auth.GroupAttributeMapper;
import ome.security.auth.GroupContextMapper;
import ome.security.auth.LdapConfig;
import ome.security.auth.NewUserGroupBean;
import ome.security.auth.NewUserGroupOwnerBean;
import ome.security.auth.OrgUnitNewUserGroupBean;
import ome.security.auth.PersonContextMapper;
import ome.security.auth.QueryNewUserGroupBean;
import ome.security.auth.RoleProvider;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.util.SqlAction;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Provides methods for administering user accounts, passwords, as well as
 * methods which require special privileges.
 *
 * Developer note: As can be expected, to perform these privileged the Admin
 * service has access to several resources that should not be generally used
 * while developing services. Misuse could circumvent security or auditing.
 *
 * @author Aleksandra Tarkowska, A.Tarkowska@dundee.ac.uk
 * @see SecuritySystem
 * @see Permissions
 * @since 3.0-M3
 */
@Transactional(readOnly = true)
public class LdapImpl extends AbstractLevel2Service implements ILdap,
    ApplicationContextAware {

    private final SqlAction sql;

    private final RoleProvider provider;

    private final ContextSource ctx;

    private final LdapOperations ldap;

    private final LdapConfig config;

    private final Roles roles;

    private OmeroContext appContext;

    public LdapImpl(ContextSource ctx, LdapOperations ldap, Roles roles,
            LdapConfig config, RoleProvider roleProvider, SqlAction sql) {
        this.ctx = ctx;
        this.sql = sql;
        this.ldap = ldap;
        this.roles = roles;
        this.config = config;
        this.provider = roleProvider;
    }

    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        appContext = (OmeroContext) arg0;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return ILdap.class;
    }

    // ~ System-only interface methods
    // =========================================================================

    @SuppressWarnings("unchecked")
    @RolesAllowed("system")
    public List<Experimenter> searchAll() {
        return ldap.search(DistinguishedName.EMPTY_PATH, config.getUserFilter()
                .encode(), getPersonContextMapper());
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
            filter.and(config.getUserFilter());
            filter.and(new EqualsFilter(attr, value));
            return ldap.search(dn, filter.encode(), getPersonContextMapper());
        } else {
            return Collections.emptyList();
        }
    }

    @RolesAllowed("system")
    public Experimenter searchByDN(String dns) {
        DistinguishedName dn = new DistinguishedName(dns);
        return (Experimenter) ldap.lookup(dn, getPersonContextMapper());
    }

    @RolesAllowed("system")
    public String findDN(String username) {
        PersonContextMapper mapper = getPersonContextMapper();
        return mapper.getDn(findExperimenter(username));
    }

    @RolesAllowed("system")
    public String findGroupDN(String groupname) {
        GroupContextMapper mapper = getGroupContextMapper();
        return mapper.getDn(findGroup(groupname));
    }

    @RolesAllowed("system")
    public Experimenter findExperimenter(String username) {
        PersonContextMapper mapper = getPersonContextMapper();
        return mapUserName(username, mapper);
    }

    @RolesAllowed("system")
    public ExperimenterGroup findGroup(String groupname) {
        GroupContextMapper mapper = getGroupContextMapper();
        return mapGroupName(groupname, mapper);
    }

    /**
     * Mapping a username to an {@link Experimenter}. This handles checking the
     * username for case exactness. This should be done at the LDAP level, but
     * Apache DS (the testing framework used) does not yet support
     * :caseExactMatch:. When it does, the check here can be removed.
     *
     * @param username
     * @param mapper
     * @return a non null Experimenter.
     * @see ticket:2557
     */
    @SuppressWarnings("unchecked")
    private Experimenter mapUserName(String username, PersonContextMapper mapper) {
        Filter filter = config.usernameFilter(username);
        List<Experimenter> p = ldap.search("", filter.encode(),
                mapper.getControls(), mapper);

        if (p.size() == 1 && p.get(0) != null) {
            Experimenter e = p.get(0);
            if (provider.isIgnoreCaseLookup()) {
                if (e.getOmeName().equalsIgnoreCase(username)) {
                    return p.get(0);
                }
            } else {
                if (e.getOmeName().equals(username)) {
                    return p.get(0);
                }
            }
        }
        throw new ApiUsageException(
                "Cannot find unique user DistinguishedName: found=" + p.size());
    }

    @SuppressWarnings("unchecked")
    private ExperimenterGroup mapGroupName(String groupname,
            GroupContextMapper mapper) {
        Filter filter = config.groupnameFilter(groupname);
        List<ExperimenterGroup> g = ldap.search("", filter.encode(),
                mapper.getControls(), mapper);

        if (g.size() == 1 && g.get(0) != null) {
            ExperimenterGroup grp = g.get(0);
            if (grp.getName().equals(groupname)) {
                return g.get(0);
            }
        }
        throw new ApiUsageException(
                "Cannot find unique group DistinguishedName: found=" + g.size());
    }

    @RolesAllowed("system")
    @SuppressWarnings("unchecked")
    public List<String> searchDnInGroups(String attr, String value) {
        if (attr != null && !attr.equals("") && value != null
                && !value.equals("")) {
            AndFilter filter = new AndFilter();
            filter.and(config.getGroupFilter());
            filter.and(new EqualsFilter(attr, value));
            return ldap.search("", filter.encode(), new GroupAttributeMapper(
                    config));
        } else {
            return Collections.emptyList();
        }
    }

    @RolesAllowed("system")
    @SuppressWarnings("unchecked")
    public List<Experimenter> searchByAttributes(String dn,
            String[] attributes, String[] values) {
        if (attributes.length != values.length) {
            return Collections.emptyList();
        }
        AndFilter filter = new AndFilter();
        for (int i = 0; i < attributes.length; i++) {
            filter.and(new EqualsFilter(attributes[i], values[i]));
        }
        return ldap.search(new DistinguishedName(dn), filter.encode(),
                getPersonContextMapper());
    }

    @RolesAllowed("system")
    @Transactional(readOnly = false)
    @Deprecated
    public void setDN(Long experimenterID, String dn) {
        Experimenter experimenter = iQuery.get(Experimenter.class,
                experimenterID);
        experimenter.setLdap(StringUtils.isNotBlank(dn));
        iUpdate.saveObject(experimenter);
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

    public void synchronizeLdapUser(String username) {
        if (!config.isSyncOnLogin()) {
            if (getBeanHelper().getLogger().isTraceEnabled()) {
                getBeanHelper().getLogger().trace("sync_on_login=false");
            }
            return;
        }

        Experimenter omeExp = iQuery.findByString(Experimenter.class,
                "omeName", username);
        Experimenter ldapExp = findExperimenter(username);
        String ldapDN = getPersonContextMapper().getDn(ldapExp);
        DistinguishedName dn = new DistinguishedName(ldapDN);
        GroupLoader loader = new GroupLoader(username, dn);
        List<Long> ldapGroups = loader.getGroups();
        List<Long> ownedGroups = loader.getOwnedGroups();
        List<Object[]> currentGroups = iQuery
                .projection(
                        "select g.id, g.ldap from ExperimenterGroup g "
                                + "join g.groupExperimenterMap m join m.child e where e.id = :id",
                        new Parameters().addId(omeExp.getId()));

        final Set<Long> currentLdapGroups = new HashSet<Long>();
        for (Object[] objs : currentGroups) {
            Long id = (Long) objs[0];
            Boolean isLdap = (Boolean) objs[1];
            if (isLdap) {
                currentLdapGroups.add(id);
            }
        }

        // All the currentLdapGroups not in ldapGroups should be removed.
        modifyGroups(omeExp, currentLdapGroups, ldapGroups, false);
        // All the ldapGroups not in currentLdapGroups should be added.
        modifyGroups(omeExp, ldapGroups, currentLdapGroups, true);
        // Then for all remaining groups, set the ownership flag based
        // on ownedGroups
        for (Long ldapGroupId : ldapGroups) {
            provider.setGroupOwner(omeExp,
                    new ExperimenterGroup(ldapGroupId, false),
                    ownedGroups.contains(ldapGroupId));
        }

        List<String> fields = Arrays.asList(Experimenter.FIRSTNAME,
                Experimenter.MIDDLENAME, Experimenter.LASTNAME,
                Experimenter.EMAIL, Experimenter.INSTITUTION);

        for (String field : fields) {
            String fieldname = field.substring(field.indexOf("_") + 1);
            String ome = (String) omeExp.retrieve(field);
            String ldap = (String) ldapExp.retrieve(field);

            if (ome == null) {
                if (ldap != null) {
                    getBeanHelper().getLogger().info(
                            String.format("Nulling %s for %s, was:", fieldname,
                                    username, ome));
                    omeExp.putAt(field, ldap);
                }
            } else if (!ome.equals(ldap)) {
                getBeanHelper().getLogger().info(
                        String.format("Changing %s for %s: %s -> %s",
                                fieldname, username, ome, ldap));
                omeExp.putAt(field, ldap);
            }
        }
        iUpdate.flush();
    }

    /**
     * The IDs in "minus" will be removed from the IDs in "base" and then
     * the operation chosen by "add" will be run on them. This method
     * ignores all methods known by Roles.
     *
     * @param e
     * @param base
     * @param minus
     * @param add
     */
    private void modifyGroups(Experimenter e, Collection<Long> base,
            Collection<Long> minus, boolean add) {
        final Logger log = getBeanHelper().getLogger();
        Set<Long> ids = new HashSet<Long>(base);

        ids.removeAll(minus);
        // Take no actions on system/user group.
        ids.remove(roles.getSystemGroupId());
        ids.remove(roles.getUserGroupId());

        if (ids.size() > 0) {
            log.info(String.format("%s groups for %s: %s", add ? "Adding"
                    : "Removing", e.getOmeName(), ids));
            Set<ExperimenterGroup> grps = new HashSet<ExperimenterGroup>();
            for (Long id : ids) {
                grps.add(new ExperimenterGroup(id, false));
            }
            if (add) {
                provider.addGroups(e, grps.toArray(new ExperimenterGroup[0]));
            } else {
                provider.removeGroups(e, grps.toArray(new ExperimenterGroup[0]));
            }

            if (add) {
                // If we have just added groups, then it's possible that
                // the "user" group is at the front of the list, in which
                // case we should assign another specific group.
                e = iQuery.get(Experimenter.class, e.getId());
                log.debug("sizeOfGroupExperimenterMap="
                        + e.sizeOfGroupExperimenterMap());
                if (e.sizeOfGroupExperimenterMap() > 1) {
                    GroupExperimenterMap primary = e.getGroupExperimenterMap(0);
                    GroupExperimenterMap next = e.getGroupExperimenterMap(1);
                    log.debug("primary=" + primary.parent().getId());
                    log.debug("next=" + next.parent().getId());
                    if (primary.parent().getId().equals(roles.getUserGroupId())) {
                        log.debug("calling setDefaultGroup");
                        provider.setDefaultGroup(e, next.parent());
                    }
                }
            }
        }
    }

    /**
     * Creates an {@link Experimenter} based on the supplied LDAP username.
     * Doesn't validate the user's password and can be only executed by admin
     * users.
     *
     * @param username
     *            The user's LDAP username.
     * @param password
     *            The user's LDAP password, not null.
     * @return true if a user is created
     */
    @Deprecated
    @RolesAllowed("system")
    @Transactional(readOnly = false)
    public boolean createUserFromLdap(String username, String password) {
        return null != createUser(username, password, true);
    }

    /**
     * Creates an {@link Experimenter} based on the supplied LDAP username.
     * Doesn't validate the user's password and can be only executed by admin
     * users.
     *
     * @param username
     *            The user's LDAP username.
     * @return The newly created {@link Experimenter} object.
     */
    @RolesAllowed("system")
    @Transactional(readOnly = false)
    public Experimenter createUser(String username) {
        return createUser(username, null, false);
    }

    /**
     * Creates an {@link Experimenter} based on the supplied LDAP username.
     * Enforces user password validation.
     *
     * @param username
     *            The user's LDAP username.
     * @param password
     *            The user's LDAP password, not null.
     * @return The newly created {@link Experimenter} object.
     */
    public Experimenter createUser(String username, String password) {
        return createUser(username, password, true);
    }

    /**
     * Creates an {@link Experimenter} based on the supplied LDAP username.
     * A boolean flag controls if password checks should be performed.
     *
     * @param username
     *            The user's LDAP username.
     * @param password
     *            The user's password.
     * @param checkPassword
     *            Flag indicating if password check should be performed.
     * @return The newly created {@link Experimenter} object.
     */
    public Experimenter createUser(String username, String password,
            boolean checkPassword) {
        if (provider.isIgnoreCaseLookup()) {
            username = username.toLowerCase();
        }
        if (iQuery.findByString(Experimenter.class, "omeName", username) != null) {
            throw new ValidationException("User already exists: " + username);
        }

        Experimenter exp = findExperimenter(username);
        String ldapDn = getPersonContextMapper().getDn(exp);
        DistinguishedName dn = new DistinguishedName(ldapDn);

        boolean access = true;
        if (checkPassword) {
            access = validatePassword(dn.toString(), password);
        }

        if (access) {
            GroupLoader loader = new GroupLoader(username, dn);
            List<Long> groups = loader.getGroups();
            List<Long> ownerOfGroups = loader.getOwnedGroups();

            if (groups.size() == 0) {
                throw new ValidationException("No group found for: " + dn);
            }

            // Create the unloaded groups for creation
            Long gid = groups.remove(0);
            ExperimenterGroup grp1 = new ExperimenterGroup(gid, false);
            Set<Long> otherGroupIds = new HashSet<Long>(groups);
            ExperimenterGroup grpOther[] = new ExperimenterGroup[otherGroupIds
                    .size() + 1];

            int count = 0;
            for (Long id : otherGroupIds) {
                grpOther[count++] = new ExperimenterGroup(id, false);
            }
            grpOther[count] = new ExperimenterGroup(roles.getUserGroupId(),
                    false);
            long uid = provider.createExperimenter(exp, grp1, grpOther);
            for (Long toBeOwned : ownerOfGroups) {
                provider.setGroupOwner(exp,
                        new ExperimenterGroup(toBeOwned, false), true);
            }
            return iQuery.get(Experimenter.class, uid);
        } else {
            return null;
        }
    }

    static private final Pattern p = Pattern.compile(
        "^:(ou|" +
            "attribute|filtered_attribute|" +
            "dn_attribute|filtered_dn_attribute|" +
            "query|bean):(.*)$");

    @Deprecated // Use GroupLoader to handle ownership
    public List<Long> loadLdapGroups(String username, DistinguishedName dn) {
        return new GroupLoader(username, dn).getGroups();
    }

    /**
     * Data class which stores the state of the {@link NewUserGroupBean} and
     * {@link NewUserGroupOwnerBean} operations.
     */
    public class GroupLoader {

        final String username;
        final DistinguishedName dn;
        final String grpSpec;
        final List<Long> groups;
        final List<Long> ownedGroups;
        final NewUserGroupBean bean;
        final AttributeSet attrSet;

        /**
         * Return the found groups for the given username.
         * @return Never null.
         */
        List<Long> getGroups() {
            return groups;
        }

        /**
         * Return the found owned groups for the given username.
         * @return Never null.
         */
        public List<Long> getOwnedGroups() {
            return ownedGroups;
        }

        GroupLoader(String username, DistinguishedName dn) {
            this.username = username;
            this.dn = dn;

            grpSpec = config.getNewUserGroup();
            groups = new ArrayList<Long>();
            ownedGroups = new ArrayList<Long>();

            if (!grpSpec.startsWith(":")) {
                // The default case is the original logic: use the spec as name
                // No support for group ownership.
                groups.add(provider.createGroup(grpSpec, null, false, true));
                bean = null;
                attrSet = null;
                return; // EARLY EXIT!
            }

            final Matcher m = p.matcher(grpSpec);
            if (!m.matches()) {
                throw new ValidationException(grpSpec
                        + " spec currently not supported.");
            }

            final String type = m.group(1);
            final String data = m.group(2);

            if ("ou".equals(type)) {
                bean = new OrgUnitNewUserGroupBean(dn);
                attrSet = getAttributeSet(username, getPersonContextMapper());
            } else if ("filtered_attribute".equals(type)) {
                bean = new AttributeNewUserGroupBean(data, true, false);
                attrSet = getAttributeSet(username, getPersonContextMapper(data));
            } else if ("attribute".equals(type)) {
                bean = new AttributeNewUserGroupBean(data, false, false);
                attrSet = getAttributeSet(username, getPersonContextMapper(data));
            } else if ("filtered_dn_attribute".equals(type)) {
                bean = new AttributeNewUserGroupBean(data, true, true);
                attrSet = getAttributeSet(username, getPersonContextMapper(data));
            } else if ("dn_attribute".equals(type)) {
                bean = new AttributeNewUserGroupBean(data, false, true);
                attrSet = getAttributeSet(username, getPersonContextMapper(data));
            } else if ("query".equals(type)) {
                bean = new QueryNewUserGroupBean(data);
                attrSet = getAttributeSet(username, getPersonContextMapper());
            } else if ("bean".equals(type)) {
                bean = appContext.getBean(data, NewUserGroupBean.class);
                // Likely, this should be added to the API in order to allow bean
                // implementations to provide an attribute set.
                attrSet = getAttributeSet(username, getPersonContextMapper());
            } else {
                throw new RuntimeException("Unknown spec: " + grpSpec);
            }

            groups.addAll(bean.groups(username, config, ldap, provider, attrSet));
            if (bean instanceof NewUserGroupOwnerBean) {
                ownedGroups.addAll(((NewUserGroupOwnerBean) bean).ownerOfGroups(
                        username, config, ldap, provider, attrSet));
            }
        }
    }

    private AttributeSet getAttributeSet(String username,
            PersonContextMapper mapper) {
        Experimenter exp = mapUserName(username, mapper);
        String dn = mapper.getDn(exp);
        AttributeSet attrSet = mapper.getAttributeSet(exp);
        attrSet.put("dn", dn); // For queries
        return attrSet;
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
    public boolean validatePassword(String dn, String password) {
        try {
            isAuthContext(dn, password);
            return true;
        } catch (SecurityViolation sv) {
            return false;
        }
    }

    /**
     * Queries the LDAP server and returns the DN for all OMERO users that have
     * the <code>ldap</code> flag enabled.
     *
     * @return a list of DN to user ID maps.
     */
    public List<Map<String, Object>> lookupLdapAuthExperimenters() {
        List<Long> ldapExperimenters = sql.getLdapExperimenters();
        List<Map<String, Object>> rv = Lists
                .newArrayListWithExpectedSize(ldapExperimenters.size());
        for (Long id : ldapExperimenters) {
            Map<String, Object> values = Maps.newHashMap();
            // This will break whenever the mapping in AdminI changes
            values.put("dn", lookupLdapAuthExperimenter(id));
            values.put("experimenter_id", id);
            rv.add(values);
        }
        return rv;
    }

    /**
     * Queries the LDAP server and returns the DN for the specified OMERO user
     * ID. The LDAP server is queried and the DN returned only for IDs that have
     * the <code>ldap</code> flag enabled.
     *
     * @param id
     *            The user ID.
     * @return The DN as a String. Null if user isn't from LDAP.
     */
    public String lookupLdapAuthExperimenter(Long id) {
        // First, check that the supplied user ID is an LDAP user
        String dn = null;
        Experimenter experimenter = iQuery.get(Experimenter.class, id);
        if (experimenter.getLdap()) {
            dn = findDN(experimenter.getOmeName());
        }
        return dn;
    }

    @RolesAllowed("system")
    public List<Experimenter> discover() {
        List<Experimenter> discoveredExperimenters = Lists.newArrayList();
        Roles r = getSecuritySystem().getSecurityRoles();

        List<Experimenter> localExperimenters = iQuery.findAllByQuery(
                "select distinct e from Experimenter e "
                        + "where id not in (:ids) and ldap = :ldap",
                new Parameters()
                        .addIds(Lists.newArrayList(r.getRootId(), r.getGuestId()))
                        .addBoolean("ldap", false));

        for (Experimenter e : localExperimenters) {
            try {
                findExperimenter(e.getOmeName());
            } catch (ApiUsageException aue) {
                // This user doesn't have an LDAP account
                continue;
            }
            discoveredExperimenters.add(e);
        }
        return discoveredExperimenters;
    }

    @RolesAllowed("system")
    public List<ExperimenterGroup> discoverGroups() {
        List<ExperimenterGroup> discoveredGroups = Lists.newArrayList();
        Roles r = getSecuritySystem().getSecurityRoles();

        List<ExperimenterGroup> localGroups = iQuery.findAllByQuery(
                "select distinct g from ExperimenterGroup g "
                        + "where id not in (:ids) and ldap = :ldap",
                new Parameters().addIds(
                        Lists.newArrayList(r.getGuestGroupId(),
                                r.getSystemGroupId(), r.getUserGroupId()))
                        .addBoolean("ldap", false));

        for (ExperimenterGroup g : localGroups) {
            try {
                findGroup(g.getName());
            } catch (ApiUsageException aue) {
                // This group doesn't exist in the LDAP server
                continue;
            }
            discoveredGroups.add(g);
        }
        return discoveredGroups;
    }

    // Helpers
    // =========================================================================

    private PersonContextMapper getPersonContextMapper() {
        return new PersonContextMapper(config, getBase());
    }

    private PersonContextMapper getPersonContextMapper(String attr) {
        return new PersonContextMapper(config, getBase(), attr);
    }

    private GroupContextMapper getGroupContextMapper() {
        return new GroupContextMapper(config, getBase());
    }

    private GroupContextMapper getGroupContextMapper(String attr) {
        return new GroupContextMapper(config, getBase(), attr);
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
            // See discussion on anonymous bind in LdapPasswordProvider
            if (username == null || username.equals("") || password == null
                    || password.equals("")) {
                throw new SecurityViolation(
                        "Refused to authenticate without username and password!");
            }

            env = (Hashtable<String, String>) ctx.getReadOnlyContext()
                    .getEnvironment();
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);
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

}
