/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sessions;

import junit.framework.TestCase;

import ome.system.OmeroContext;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessionBeanIntegrationTest extends TestCase {

    @Test
    public void testFindAllSessionsReturnsOnlyOwn() throws Exception {
    	OmeroContext.getManagedServerContext();
    	fail("NYI");
    }
    

}
