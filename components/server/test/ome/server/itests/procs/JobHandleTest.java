/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.procs;

import junit.framework.TestCase;
import ome.api.JobHandle;
import ome.security.SecuritySystem;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.spring.ManagedServiceFactory;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@Test(groups = { "jobs", "integration" })
public class JobHandleTest extends TestCase {

    protected JobHandle jh;
    protected ServiceFactory sf = new ManagedServiceFactory();
    protected SecuritySystem sec;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        super.setUp();
        sec = (SecuritySystem) sf.getContext().getBean("securitySystem");
        sec.login(new Principal());
    }

}
