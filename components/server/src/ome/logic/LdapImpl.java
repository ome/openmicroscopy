/*
 *   $Id$
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import ome.annotations.NotNull;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.annotations.RolesAllowed;
import ome.api.ILdap;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecuritySystem;
import ome.security.auth.LdapUtil;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
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

    protected final LdapOperations ldapOperations;

    protected final String newUserGroup;

    protected final String groups;

    protected final String attributes;

    protected final String values;

    protected final LdapUtil util;

    public LdapImpl(
            LdapOperations ldapOperations,
            LdapUtil util,
            String newUserGroup, String groups, String attributes,
            String values) {
        this.ldapOperations = ldapOperations;
        this.util = util;
        this.newUserGroup = newUserGroup;
        this.groups = groups;
        this.attributes = attributes;
        this.values = values;
    }

    // ~ System-only interface methods
    // =========================================================================

    @SuppressWarnings("unchecked")
    @RolesAllowed("system")
    public List<Experimenter> searchAll() {
        EqualsFilter filter = new EqualsFilter("objectClass", "person");
        return ldapOperations.search(DistinguishedName.EMPTY_PATH, filter
                .encode(), util.getContextMapper());
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

            return ldapOperations.search(dn, filter.encode(),
                    util.getContextMapper());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @RolesAllowed("system")
    public Experimenter searchByDN(String dns) {
        DistinguishedName dn = new DistinguishedName(dns);
        return (Experimenter) ldapOperations.lookup(dn, util.getContextMapper());
    }

    @RolesAllowed("system")
    public String findDN(String username) {
        DistinguishedName dn;
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("cn", username));
        List<Experimenter> p = ldapOperations.search("", filter.encode(),
                util.getContextMapper());
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
        return util.findExperimenter(username);
    }

    @RolesAllowed("system")
    public List<String> searchDnInGroups(String attr, String value) {
        return util.searchDnInGroups(attr, value);
    }

    @RolesAllowed("system")
    public List<Experimenter> searchByAttributes(String dn,
            String[] attributes, String[] values) {
        return util.searchByAttributes(dn, attributes, values);
    }

    @RolesAllowed("system")
    public List<ExperimenterGroup> searchGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @RolesAllowed("system")
    @Transactional(readOnly = false)
    public void setDN(@NotNull Long experimenterID, String dn) {
        util.setDNById(experimenterID, dn);
    }

    // Getters and Setters for requiroments
    // =========================================================================

    @RolesAllowed("system")
    public boolean getSetting() {
        return util.getConfig();
    }

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

    public Class<? extends ServiceInterface> getServiceInterface() {
        return ILdap.class;
    }

}
