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

import ome.model.IObject;
import ome.model.meta.ExperimenterGroup;
import ome.security.auth.LdapConfig;

import org.springframework.ldap.core.DirContextAdapter;

/**
 * Specialized OME ExperimenterGroup context mapper.
 */
public class GroupContextMapper extends AbstractContextMapper {

    public GroupContextMapper(LdapConfig cfg, String base) {
        super(cfg, base);
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

    public IObject onMap(DirContextAdapter ctx) {
        ExperimenterGroup group = new ExperimenterGroup();
        group.setName(get("name", ctx));
        return group;
    }

}
