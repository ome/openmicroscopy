/*
 *   $Id$
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.security.auth.mappers;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import ome.security.auth.LdapConfig;

import org.springframework.ldap.core.AttributesMapper;

public class GroupAttributeMapper implements AttributesMapper {

    private final LdapConfig cfg;

    public GroupAttributeMapper(LdapConfig config) {
        this.cfg = config;
    }

    public Object mapFromAttributes(Attributes attributes)
            throws NamingException {

        String attributeKey = cfg.getGroupAttribute("name");
        return attributes.get(attributeKey).get();

    }

}
