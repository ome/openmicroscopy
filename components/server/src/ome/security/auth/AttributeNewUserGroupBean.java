/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.conditions.ValidationException;
import ome.security.SecuritySystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;

/**
 * Handles the "*_attribute" specifiers from etc/omero.properties.
 *
 * The values of the attribute equal to the string following ":*_attribute:" are
 * taken to be the names and/or DNs of {@link ExperimenterGroup} instances and
 * created if necessary. If {@link #filtered} is set to true, then the names/DNs
 * found must pass the assigned group filter.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @since Beta4.2
 */
public class AttributeNewUserGroupBean implements NewUserGroupBean {

    private final static Logger log = LoggerFactory.getLogger(AttributeNewUserGroupBean.class);

    /**
     * The value following ":*attribute:" in the configuration, where "*"
     * can be one of: "", "filtered_", "dn_", and "filtered_dn_".
     */
    private final String grpAttribute;

    /**
     * Whether or not the group filter should be applied to found groups.
     */
    private final boolean filtered;

    /**
     * Whether the value of the attribute should be interpreted as a DN
     * or as the group name.
     */
    private final boolean dn;

    public AttributeNewUserGroupBean(String grpAttribute, boolean filtered, boolean dn) {
        this.grpAttribute = grpAttribute;
        this.filtered = filtered;
        this.dn = dn;
    }

    @SuppressWarnings("unchecked")
    public List<Long> groups(String username, LdapConfig config,
            LdapOperations ldap, RoleProvider provider, AttributeSet attrSet) {

        Set<String> groupNames = attrSet.getAll(grpAttribute);
        if (groupNames == null) {
            throw new ValidationException(username + " has no attributes "
                    + grpAttribute);
        }

        final GroupAttributeMapper mapper = new GroupAttributeMapper(config);

            // If filtered is activated, then load all group names as mapped
        // via the name field.
        //
        // TODO: this should likely be done via either paged queries
        // or once for each target.
        List<String> filteredNames = null;
        if (filtered) {
            String filter = config.getGroupFilter().encode();
            filteredNames = (List<String>) ldap.search("", filter, mapper);
        }

        List<Long> groups = new ArrayList<Long>();
        for (String grpName : groupNames) {
            // If DN is true, then we need to map from the attribute value
            // to the actual group name before comparing.
            if (dn) {
                DistinguishedName relative = config.relativeDN(grpName);
                String nameAttr = config.getGroupAttribute("name");
                grpName = relative.getValue(nameAttr);
            }

            // Apply filter if necessary.
            if (filtered && !filteredNames.contains(grpName)) {
                log.debug("Group not found by filter: " + grpName);
                continue;
            }

            // Finally, add the grou
            groups.add(provider.createGroup(grpName, null, false, true));

        }
        return groups;

    }

}
