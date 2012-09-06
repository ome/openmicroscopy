/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldapsync;

import java.io.File;
import java.util.List;
import java.util.Map;

import ome.services.ldap.AbstractLdapTest;
import ome.services.ldap.Fixture;
import ome.services.ldap.IntegrationFixture;
import ome.services.ldap.LdapIntegrationTest;
import ome.system.OmeroContext;

import org.testng.annotations.Test;

/**
 * Extends {@link LdapIntegrationTest} to use test how modifications of LDAP
 * users/groups are propagated to OMERO.
 */
@Test(groups = {"ldap", "integration"})
public class LdapSyncTest extends AbstractLdapTest {

    @Override
    protected Fixture createFixture(File ctxFile)
        throws Exception
    {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        return new SyncFixture(ctxFile, ctx);
    }

    public void testChangeAttribute() throws Exception {
        File file = getLdifContextFile("testChangeAttribute");
        assertLdifFile(file);
    }

    public void testChangeDnAttribute() throws Exception {
        File file = getLdifContextFile("testChangeDnAttribute");
        assertLdifFile(file);
    }

    public void testChangeName() throws Exception {
        File file = getLdifContextFile("testChangeName");
        assertLdifFile(file);
    }

    public void testDefaultGroup() throws Exception {
        File file = getLdifContextFile("testDefaultGroup");
        assertLdifFile(file);
    }

    public void testNoSyncDefault() throws Exception {
        File file = getLdifContextFile("testNoSyncDefault");
        assertLdifFile(file);
    }

    public void testNoSyncExplicit() throws Exception {
        File file = getLdifContextFile("testNoSyncExplicit");
        assertLdifFile(file);
    }

    public void testQueryGroup() throws Exception {
        File file = getLdifContextFile("testQueryGroup");
        assertLdifFile(file);
    }

    public void testUserFilter() throws Exception {
        File file = getLdifContextFile("testUserFilter");
        assertLdifFile(file);
    }

}
