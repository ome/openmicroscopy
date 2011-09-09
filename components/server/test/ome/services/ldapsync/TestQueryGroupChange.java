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
 * testQueryGroup files.
 */
public class TestQueryGroupChange implements Modification {

    /**
dn: ou=testQueryGroup,o=eg
objectclass: organizationalUnit
objectClass: top
ou: testQueryGroup

dn: cn=grp,ou=testQueryGroup,o=eg
objectClass: top
objectClass: groupOfNames
cn: grp
member: cn=test1,ou=testQueryGroup,o=eg

dn: cn=test1,ou=testQueryGroup,o=eg
objectClass: person
cn: test1
givenName: Testy
sn: Tester
userPassword: password
     */
    public void modify(Fixture fixture) {

        /*
        DirContextAdapter ctx = (DirContextAdapter) fixture.template.lookup("cn=grp");
        ome.security.auth.AttributeSet as = new AttributeSet(ctx);
        Set<String> members = as.getAll("member");
        */

        EventContext before = fixture.login("test1", "grp", "password");
        if (2 != before.getMemberOfGroupsList().size()) {
            throw new RuntimeException("Not 2 groups!");
        }

        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                new BasicAttribute("member", "cn=test1,ou=testQueryGroup,o=eg"));
        fixture.template.modifyAttributes("cn=grp", mods);

        try {
            EventContext after = fixture.login("test1", "grp", "password");
            throw new RuntimeException("Expecting a sec. violation!");
        } catch (SecurityViolation sv) {
            // good!
        }
    }
}
