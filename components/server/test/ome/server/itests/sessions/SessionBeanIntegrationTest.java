/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sessions;

import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessionBeanIntegrationTest extends AbstractManagedContextTest {

    @Test
    public void testUserCreatesOwnSession() throws Exception {
        
        Experimenter e = loginNewUser();
        Session s = factory.getSessionService().createUserSession(0, 0, null, null);
        
    }
}
