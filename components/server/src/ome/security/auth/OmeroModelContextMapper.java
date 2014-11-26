/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.security.auth;

import java.util.Set;

import ome.model.IObject;

import org.springframework.ldap.core.ContextMapper;

/**
 * Parent class for any OMERO model-to-LDAP mappers. Contains common logic.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since OMERO-5.1
 */
public abstract class OmeroModelContextMapper implements ContextMapper {

    static final String LDAP_DN = "LDAP_DN";

    static final String LDAP_ATTR = "LDAP_ATTR";

    static final String LDAP_PROPS = "LDAP_PROPS";

    final LdapConfig cfg;

    final String base;

    final String attribute;

    public OmeroModelContextMapper(LdapConfig cfg, String base, String attribute) {
        this.cfg = cfg;
        this.base = base;
        this.attribute = attribute;
    }

    @Override
    public abstract Object mapFromContext(Object obj);

    public <T extends IObject> String getDn(T modelObj) {
        return (String) modelObj.retrieve(LDAP_DN);
    }

    @SuppressWarnings("unchecked")
    public <T extends IObject> Set<String> getAttribute(T modelObj) {
        return (Set<String>) modelObj.retrieve(LDAP_ATTR);
    }

    public <T extends IObject> AttributeSet getAttributeSet(T modelObj) {
        return (AttributeSet) modelObj.retrieve(LDAP_PROPS);
    }
}
