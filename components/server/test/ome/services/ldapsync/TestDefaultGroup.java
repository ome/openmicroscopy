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
import ome.system.Roles;


/**
 * Modification which will be performed on the contents
 * of the current LDAP store during processing of the
 * testDefaultGroup files. The intent is that after
 * a user is removed from their last non-"user" group
 * and then that group is readded, that the user should
 * again have a non-"user" default group.
 */
public class TestDefaultGroup implements Modification {

    public void modify(Fixture fixture) {

        EventContext before = fixture.login("test1", "grp", "password");
        if (2 != before.getMemberOfGroupsList().size()) {
            throw new RuntimeException("Not 2 groups!");
        }

        ModificationItem[] remove = new ModificationItem[1];
        remove[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                new BasicAttribute("member", "cn=test1,ou=testDefaultGroup,o=eg"));
        fixture.template.modifyAttributes("cn=grp", remove);

        try {
            fixture.login("test1", "grp", "password");
            throw new RuntimeException("Expecting a sec. violation!");
        } catch (SecurityViolation sv) {
            // good!
        }

        ModificationItem[] readd = new ModificationItem[1];
        readd[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                new BasicAttribute("member", "cn=test1,ou=testDefaultGroup,o=eg"));
        fixture.template.modifyAttributes("cn=grp", readd);

        EventContext after = fixture.login("test1", "grp", "password");
        if (2 != after.getMemberOfGroupsList().size()) {
            throw new RuntimeException("Not 2 groups!");
        }
        
        Long userGrp = new Long(new Roles().getUserGroupId());
        Long firstGrp = after.getMemberOfGroupsList().get(0);
        if (firstGrp.equals(userGrp)) {
            throw new RuntimeException("User group is still first!");
        }
        
    }
}
