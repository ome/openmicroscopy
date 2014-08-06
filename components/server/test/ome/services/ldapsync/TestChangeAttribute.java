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
package ome.services.ldapsync;

import java.util.UUID;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.auth.RoleProvider;
import ome.services.ldap.LdapTest.Fixture;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.ServiceFactory;


/**
 * Modification which will remove one of the group attributes
 * from a user. After synchronization, that group should be
 * removed from the user, but the one added outside of LDAP
 * (i.e. via IAdmin) should persist.
 */
public class TestChangeAttribute implements Modification {

    /**
     * The string value which will be removed.
     */
    protected final String toRemove;

    public TestChangeAttribute(String toRemove) {
        this.toRemove = toRemove;
    }

    public void modify(final Fixture fixture) {

        // Need simple role provider in order to create
        // a group without setting the "ldap" flag.
        final RoleProvider simpleRP = fixture.applicationContext.getBean(
            "roleProvider", RoleProvider.class);

        final EventContext ec1 = fixture.login("test1", "grp1", "password");
        final long grp1 = ec1.getCurrentGroupId();


        // Add the user to a new group. This requires starting a tx as
        // root.
        fixture.login("root", "system", null);
        final long grp3 = (Long)
            fixture.execute(new Executor.SimpleWork(this, "addUserToNewGroup"){
            @Transactional(readOnly=false)
            @Override
            public Object doWork(Session session, ServiceFactory sf) {
                String uuid = UUID.randomUUID().toString();
                long grp3 = simpleRP.createGroup(uuid, Permissions.PRIVATE, true);
                simpleRP.addGroups(new Experimenter(ec1.getCurrentUserId(), false),
                    new ExperimenterGroup(grp3, false));
                return grp3;
            }});
        EventContext ec2 = fixture.login("test1", "grp1", "password");
        assertMember(ec2, grp1, true);
        assertMember(ec2, grp3, true);


        // And remove from the old group
        final ModificationItem[] mods = new ModificationItem[2];
        mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                new BasicAttribute("roleOccupant", toRemove));
        fixture.template.modifyAttributes("cn=test1", mods);


        // Check that the user is no longer in grp1, but is still in grp3
        final EventContext ec3 = fixture.login("test1", "grp3", "password");
        assertMember(ec3, grp1, false);
        assertMember(ec3, grp3, true);

    }

    void assertMember(EventContext ec, long groupID, boolean isMember) {
        boolean member = ec.getMemberOfGroupsList().contains(groupID);
        if (member != isMember) {
            String msg = String.format(
                "Checking membership in %s. Expected: %s. Found: %s",
                groupID, isMember, member);
            throw new RuntimeException(msg);
        }
    }
}
