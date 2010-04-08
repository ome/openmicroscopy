/*
 *   $Id$
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import javax.naming.InvalidNameException;

import ome.model.meta.Experimenter;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;

/**
 * Specialized OME Experimenter context mapper.
 */
public class PersonContextMapper implements ContextMapper {

    private DistinguishedName dn = new DistinguishedName();

    private final LdapConfig cfg;

    private final String base;

    public PersonContextMapper(LdapConfig cfg, String base) {
        this.cfg = cfg;
        this.base = base;
    }

    public DistinguishedName getDn() {
        return dn;
    }

    public void setDn(DistinguishedName dn) {
        this.dn = dn;
    }

    public String get(String attribute, DirContextAdapter context) {
        String attributeName = cfg.getUserAttribute(attribute);
        if (attributeName != null) {
            return context.getStringAttribute(attributeName);
        }
        return null;
    }

    public Object mapFromContext(Object obj) {
        DirContextAdapter ctx = (DirContextAdapter) obj;
        DistinguishedName dn = new DistinguishedName(ctx.getDn());
        try {
            dn.addAll(0, new DistinguishedName(base));
        } catch (InvalidNameException e) {
            return null;
        }
        setDn(dn);

        Experimenter person = new Experimenter();
        person.setOmeName(get("omeName", ctx));
        person.setFirstName(get("firstName", ctx));
        person.setLastName(get("lastName", ctx));
        person.setInstitution(get("institution", ctx));
        person.setEmail(get("email", ctx));

        person.putAt("LDAP_DN", dn.toString());
        return person;
    }

}