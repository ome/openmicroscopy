/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldapsync;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import ome.conditions.SecurityViolation;
import ome.services.ldap.LdapTest.Fixture;
import ome.system.EventContext;


/**
 * Modification which will be performed on the contents
 * of the current LDAP store during processing of the
 * testQueryGroup files. This will remove an attribute
 * from the user so that the user_filter should no longer
 * allow them to login.
 */
public class TestUserFilter implements Modification {

    public void modify(Fixture fixture) {

        EventContext before = fixture.login("test1attr", "Group1", "password");

        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                new BasicAttribute("employeeType", "Employee"));
        fixture.template.modifyAttributes("cn=test1attr", mods);

        try {
            EventContext after = fixture.login("test1attr", "Group1", "password");
            throw new RuntimeException("Expecting a sec. violation!");
        } catch (SecurityViolation sv) {
            // good!
        }
    }
}
