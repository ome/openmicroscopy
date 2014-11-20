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

import javax.naming.directory.SearchControls;

import ome.model.meta.ExperimenterGroup;

import org.springframework.ldap.core.DirContextAdapter;

/**
 * Specialized OME ExperimenterGroup context mapper.
 */
public class GroupContextMapper extends OmeroModelContextMapper {

    public GroupContextMapper(LdapConfig cfg, String base) {
        this(cfg, base, null);
    }

    public GroupContextMapper(LdapConfig cfg, String base, String attribute) {
        super(cfg, base, attribute);
    }

    public String get(String attribute, DirContextAdapter context) {
        String attributeName = cfg.getGroupAttribute(attribute);
        if (attributeName != null) {
            return context.getStringAttribute(attributeName);
        }
        return null;
    }

    @Override
    public Object mapFromContext(Object obj) {
        DirContextAdapter ctx = (DirContextAdapter) obj;

        ExperimenterGroup group = new ExperimenterGroup();
        group.setName(get("name", ctx));
        group.setDescription(get("description", ctx));
        group.setLdap(true);

        group.putAt(LDAP_DN, ctx.getNameInNamespace());

        if (attribute != null) {
            group.putAt(LDAP_ATTR, ctx.getAttributeSortedStringSet(attribute));
        }

        group.putAt(LDAP_PROPS, new AttributeSet(ctx));
        return group;
    }

    public SearchControls getControls() {
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);
        if (attribute == null) {
            return controls;
        }

        final String name = cfg.getGroupAttribute("name");
        final String description = cfg.getUserAttribute("description");
        final String[] attrs = new String[] {
            "dn",
            attribute,
            cfg.getGroupAttribute("name"),
            cfg.getUserAttribute("description"),
        };
        controls.setReturningAttributes(attrs);
        return controls;
    }
}
