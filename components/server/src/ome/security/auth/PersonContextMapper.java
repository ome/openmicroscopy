/*
 *   $Id$
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingEnumeration;

import ome.model.meta.Experimenter;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

/**
 * Specialized OME Experimenter context mapper.
 */
public class PersonContextMapper implements ContextMapper {

    private static final String LDAP_DN = "LDAP_DN";

    private static final String LDAP_ATTR = "LDAP_ATTR";

    private static final String LDAP_PROPS = "LDAP_PROPS";

    private final LdapConfig cfg;

    private final String base;

    private final String attribute;

    public PersonContextMapper(LdapConfig cfg, String base) {
        this(cfg, base, null);
    }

    public PersonContextMapper(LdapConfig cfg, String base, String attribute) {
        this.cfg = cfg;
        this.base = base;
        this.attribute = attribute;
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

        Experimenter person = new Experimenter();
        person.setOmeName(get("omeName", ctx));
        person.setFirstName(get("firstName", ctx));
        person.setLastName(get("lastName", ctx));
        person.setInstitution(get("institution", ctx));
        person.setEmail(get("email", ctx));

        person.putAt(LDAP_DN, ctx.getNameInNamespace());

        if (attribute != null) {
            person.putAt(LDAP_ATTR, ctx.getAttributeSortedStringSet(attribute));
        }

        Properties properties = new Properties();
        NamingEnumeration<String> ids = ctx.getAttributes().getIDs();
        while (ids.hasMoreElements()) {
            String id = ids.nextElement();
            Object val = ctx.getObjectAttribute(id);
            if (val != null) {
                properties.put(id, val.toString());
            }
        }
        person.putAt(LDAP_PROPS, properties);

        return person;
    }

    public String getDn(Experimenter person) {
        return (String) person.retrieve(LDAP_DN);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAttribute(Experimenter person) {
        return (Set<String>) person.retrieve(LDAP_ATTR);
    }

    @SuppressWarnings("unchecked")
    public Properties getProperties(Experimenter person) {
        return (Properties) person.retrieve(LDAP_PROPS);
    }


}