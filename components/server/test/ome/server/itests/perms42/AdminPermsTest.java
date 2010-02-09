/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import java.util.HashSet;
import java.util.Set;

import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;

import org.testng.annotations.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Tests the new functionality added to IAdmin as a part of #1434
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1434")
public class AdminPermsTest extends PermissionsTest {

    @Test
    public void testAddRemoveGroupOwners() throws Exception {
        setup(Permissions.PRIVATE);
        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group());
        loginRoot();
        iAdmin.addGroupOwners(fixture.group(), fixture.user);
        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group(), fixture.user.getId());
        
        // Now add another user
        Experimenter e2 = loginNewUser();
        loginRoot();
        iAdmin.addGroupOwners(fixture.group(), e2);
        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        assertLeaders(fixture.group(), fixture.user.getId(), e2.getId());
        
        // Now remove that new user
        iAdmin.removeGroupOwners(fixture.group(), e2);
        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        assertLeaders(fixture.group(), fixture.user.getId());
    }

    protected void assertMembers(ExperimenterGroup group, Long...members) {
        Set<Long> toCheck = new HashSet<Long>(Arrays.asList(members));
        Set<Long> thatHas = new HashSet<Long>();
        for (GroupExperimenterMap map : group.unmodifiableGroupExperimenterMap()) {
            thatHas.add(map.child().getId());
        }
        assertEqualSets(thatHas, toCheck);
    }
    
    protected void assertLeaders(ExperimenterGroup group, Long...members) {
        Set<Long> toCheck = new HashSet<Long>(Arrays.asList(members));
        Set<Long> thatHas = new HashSet<Long>();
        for (GroupExperimenterMap map : group.unmodifiableGroupExperimenterMap()) {
            if (map.getOwner()) {
                thatHas.add(map.child().getId());
            }
        }
        assertEqualSets(thatHas, toCheck);
    }

    protected void assertEqualSets(Set<Long> thatHas, Set<Long> toCheck) {
        Set<Long> missing = new HashSet<Long>();
        missing.addAll(toCheck);
        missing.removeAll(thatHas);
        
        Set<Long> extra = new HashSet<Long>();
        extra.addAll(thatHas);
        extra.removeAll(toCheck);
        
        assertTrue(String.format("Missing:%s Extra: %s", missing, extra),
                missing.size() ==  0 && extra.size() == 0);
    }

}
