/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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
package ome.security.auth.mappers;

import java.util.Set;

import ome.model.IObject;
import ome.security.auth.AttributeSet;
import ome.security.auth.LdapConfig;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

/**
 * Specialized OME context mappers.
 */
public abstract class AbstractContextMapper implements ContextMapper {

    private static final String LDAP_DN = "LDAP_DN";

    private static final String LDAP_ATTR = "LDAP_ATTR";

    private static final String LDAP_PROPS = "LDAP_PROPS";

    protected final LdapConfig cfg;

    protected final String base;

    protected final String attribute;

    public AbstractContextMapper(LdapConfig cfg, String base) {
        this(cfg, base, null);
    }

    public AbstractContextMapper(LdapConfig cfg, String base, String attribute) {
        this.cfg = cfg;
        this.base = base;
        this.attribute = attribute;
    }

    /**
     * Look up the proper field to use based on the OME field name
     */
    public abstract String get(String attribute, DirContextAdapter context);

    public Object mapFromContext(Object obj) {
        DirContextAdapter ctx = (DirContextAdapter) obj;
        IObject result = onMap(ctx);
        result.putAt(LDAP_DN, ctx.getNameInNamespace());
        if (attribute != null) {
            result.putAt(LDAP_ATTR, ctx.getAttributeSortedStringSet(attribute));
        }
        result.putAt(LDAP_PROPS, new AttributeSet(ctx));
        return result;
    }

    public abstract IObject onMap(DirContextAdapter ctx);

    public String getDn(IObject object) {
        return (String) object.retrieve(LDAP_DN);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAttribute(IObject object) {
        return (Set<String>) object.retrieve(LDAP_ATTR);
    }

    @SuppressWarnings("unchecked")
    public AttributeSet getAttributeSet(IObject object) {
        return (AttributeSet) object.retrieve(LDAP_PROPS);
    }


}
