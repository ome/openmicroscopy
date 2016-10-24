/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sec;

import ome.security.SystemTypes;
import ome.security.basic.BasicSecuritySystem;
import ome.security.basic.LightAdminPrivileges;
import ome.security.basic.OmeroInterceptor;
import ome.security.basic.TokenHolder;
import ome.server.utests.TestSessionCache;
import ome.services.sessions.SessionManager;
import ome.services.sessions.stats.NullSessionStats;
import ome.system.Roles;
import ome.testing.MockServiceFactory;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class OmeroInterceptorTest extends MockObjectTestCase {

    OmeroInterceptor oi;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        MockServiceFactory sf = new MockServiceFactory();
        SessionManager sm = (SessionManager) mock(SessionManager.class).proxy();
        BasicSecuritySystem sec = BasicSecuritySystem.selfConfigure(sm, sf, new TestSessionCache(this));
        TokenHolder tokenHolder = new TokenHolder();
        final Roles roles = new Roles();
        oi = new OmeroInterceptor(roles, new SystemTypes(), null, null, tokenHolder, new NullSessionStats(),
                new LightAdminPrivileges(roles));
    }

    // ~ TESTS
    // =========================================================================

    @Test
    public void testSQLDoesntNeedFrom() throws Exception {
        String t;
        t = oi.onPrepareStatement("select p");
        t = oi.onPrepareStatement("select p from Project p");
        t = oi.onPrepareStatement("select p from Project p where\nx");
        t = oi.onPrepareStatement("select p from Project p where\n(x");
        t = oi.onPrepareStatement("select p from Project p where(x");
        String s = "select dataset0_.id as id142_, dataset0_.owner_id as owner2_142_, dataset0_.group_id as group3_1"
                + "42_, dataset0_.creation_id as creation4_142_, dataset0_.update_id as update5_142_, dataset0_.permissions as permissi6_142_, dataset0_.vers"
                + "ion as version142_, dataset0_.name as name142_, dataset0_.description as descript9_142_ from dataset dataset0_ where "
                + "( "
                + "? OR "
                + "(dataset0_.group_id in (?, ?)) OR "
                + "(dataset0_.owner_id = ? AND (cast(dataset0_.permissions as bit(64)) & cast(1024 as bit(64))) = cast(1024 as bit(64))) OR "
                + "(dataset0_.group_id in (?, ?) AND (cast(dataset0_.permissions as bit(64)) & cast(64 as bit(64))) = cast(64 as bit(64))) OR "
                + "((cast(dataset0_.permissions as bit(64)) & cast(4 as bit(64))) = cast(4 as bit(64))) "
                + ") and (dataset0_.id in (select projectdat1_.child from projectdatasetlink projectdat1_ where projectdat1_.parent=?)) limit ?";
        t = oi.onPrepareStatement(s);
    }

}
