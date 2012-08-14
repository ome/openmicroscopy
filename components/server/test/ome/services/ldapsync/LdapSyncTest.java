/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldapsync;

import java.io.File;
import java.util.List;
import java.util.Map;

import ome.services.ldap.LdapIntegrationTest;

import org.testng.annotations.Test;

/**
 * Extends {@link LdapIntegrationTest} to use test how modifications of LDAP
 * users/groups are propagated to OMERO.
 */
@Test(groups = {"ldap", "integration"})
public class LdapSyncTest extends LdapIntegrationTest {

    /**
     * In order to insert our logic into the {@link #testLdiffFile(File)}
     * method, we're going to override assertPasses.
     */
    @Override
    protected void assertPasses(Fixture fixture, Map<String, List<String>> users)
            throws Exception {
        super.assertPasses(fixture, users);

        for (Modification mod : fixture.ctx.getBeansOfType(Modification.class)
                .values()) {
            // After the modification, an exception will be thrown
            // if the proper response from OMERO is not encountered.
            mod.modify(fixture);
        }

    }

    /**
     * All testing takes place in the {@link Modification}.
     */
    @Override
    protected void assertFails(Fixture fixture, Map<String, List<String>> users) {
        // no-op
    }
}
