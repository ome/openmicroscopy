/*
 *   $Id$
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth.mappers;

import ome.model.IObject;
import ome.model.meta.Experimenter;
import ome.security.auth.LdapConfig;

import org.springframework.ldap.core.DirContextAdapter;

/**
 * Specialized OME Experimenter context mapper.
 */
public class PersonContextMapper extends AbstractContextMapper {

    public PersonContextMapper(LdapConfig cfg, String base) {
        super(cfg, base);
    }

    public PersonContextMapper(LdapConfig cfg, String base, String attribute) {
        super(cfg, base, attribute);
    }

    public String get(String attribute, DirContextAdapter context) {
        String attributeName = cfg.getUserAttribute(attribute);
        if (attributeName != null) {
            return context.getStringAttribute(attributeName);
        }
        return null;
    }

    public IObject onMap(DirContextAdapter ctx) {
        Experimenter person = new Experimenter();
        person.setOmeName(get("omeName", ctx));
        person.setFirstName(get("firstName", ctx));
        person.setLastName(get("lastName", ctx));
        person.setInstitution(get("institution", ctx));
        person.setEmail(get("email", ctx));
        return person;
    }

}
