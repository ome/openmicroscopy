/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldapsync;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import ome.model.meta.Experimenter;
import ome.services.ldap.LdapTest.Fixture;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;


/**
 * Modification which will be performed on the contents
 * of the current LDAP store during processing of the
 * testChangeName files. Like {@link TestChangeNameChange}
 * but this should <em>not</em> change since the configuration
 * value sync_on_login is set to false! 
 */
public class TestNoSync implements Modification {

    public void modify(Fixture fixture) {

        final String NEWNAME = "BetterTest";
        final String NEWEMAIL = "new@example.com";

        fixture.login("test1", "grp", "password");

        ModificationItem[] mods = new ModificationItem[2];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("mail", NEWEMAIL));
        mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("givenName", NEWNAME));
        fixture.template.modifyAttributes("cn=test1", mods);

        final EventContext ec = fixture.login("test1", "grp", "password");
        final Experimenter e = (Experimenter)
        fixture.execute(new Executor.SimpleWork(this, "testNoSync*") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return session.get(Experimenter.class, ec.getCurrentUserId());
            }
        });

        if (e.getEmail().equals(NEWEMAIL)) {
            throw new RuntimeException("Wrong email: " + e.getEmail());
        } else if (e.getFirstName().equals(NEWNAME)) {
            throw new RuntimeException("Wrong name: " + e.getFirstName());
        }

    }
}
