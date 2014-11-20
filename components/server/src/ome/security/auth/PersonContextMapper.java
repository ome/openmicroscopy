/*
 *   Copyright 2007-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import javax.naming.directory.SearchControls;

import ome.model.meta.Experimenter;

import org.springframework.ldap.core.DirContextAdapter;

/**
 * Specialized OME Experimenter context mapper.
 */
public class PersonContextMapper extends OmeroModelContextMapper {

    public PersonContextMapper(LdapConfig cfg, String base) {
        this(cfg, base, null);
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

    @Override
    public Object mapFromContext(Object obj) {
        DirContextAdapter ctx = (DirContextAdapter) obj;

        Experimenter person = new Experimenter();
        person.setOmeName(get("omeName", ctx));
        person.setFirstName(get("firstName", ctx));
        person.setLastName(get("lastName", ctx));
        person.setInstitution(get("institution", ctx));
        person.setEmail(get("email", ctx));
        person.setLdap(true);

        person.putAt(LDAP_DN, ctx.getNameInNamespace());

        if (attribute != null) {
            person.putAt(LDAP_ATTR, ctx.getAttributeSortedStringSet(attribute));
        }

        person.putAt(LDAP_PROPS, new AttributeSet(ctx));
        return person;
    }

    public SearchControls getControls() {
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);
        if (attribute == null) {
            return controls;
        }

        final String inst = cfg.getUserAttribute("institution");
        final String email = cfg.getUserAttribute("email");
        final String[] attrs = new String[]{
            "dn",
            attribute,
            cfg.getUserAttribute("omeName"),
            cfg.getUserAttribute("firstName"),
            cfg.getUserAttribute("lastName"),
            inst == null ? "dn" : inst,
            email == null ? "dn" : email
        };
        controls.setReturningAttributes(attrs);
        return controls;
    }
}
