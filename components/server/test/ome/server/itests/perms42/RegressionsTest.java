/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import ome.model.internal.Permissions;
import ome.system.Principal;

import org.testng.annotations.Test;

/**
 * For a private group, any annotations, thumbnails, or similar from root could
 * cause inconsistent graphs.
 *
 * @since Beta-4.2.0
 *
 */
@Test
public class RegressionsTest extends PermissionsTest {

    /**
     * sudo fails with new changes to security system
     */
    @Test(groups = "ticket:1774")
    public void testSudo() throws Exception {
        setup(Permissions.USER_PRIVATE);
        loginRoot();
        Principal p = new Principal(fixture.user.getOmeName(),
                fixture.groupName, "Test");
        iSession.createSessionWithTimeout(p, 1200000);

    }
}
