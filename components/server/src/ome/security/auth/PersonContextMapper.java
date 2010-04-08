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

    private final String base;

    public PersonContextMapper(String base) {
        this.base = base;
    }

    public DistinguishedName getDn() {
        return dn;
    }

    public void setDn(DistinguishedName dn) {
        this.dn = dn;
    }

    public Object mapFromContext(Object ctx) {
        DirContextAdapter context = (DirContextAdapter) ctx;
        DistinguishedName dn = new DistinguishedName(context.getDn());
        try {
            dn.addAll(0, new DistinguishedName(base));
        } catch (InvalidNameException e) {
            return null;
        }
        setDn(dn);

        Experimenter person = new Experimenter();
        if (context.getStringAttribute("cn") != null) {
            person.setOmeName(context.getStringAttribute("cn"));
        }
        person.setLastName("");
        if (context.getStringAttribute("sn") != null) {
            person.setLastName(context.getStringAttribute("sn"));
        }
        person.setFirstName("");
        if (context.getStringAttribute("givenName") != null) {
            person.setFirstName(context.getStringAttribute("givenName"));
        }
        if (context.getStringAttribute("mail") != null) {
            person.setEmail(context.getStringAttribute("mail"));
        }
        person.putAt("LDAP_DN", dn.toString());
        return person;
    }

}