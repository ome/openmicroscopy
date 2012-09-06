/*
 * Copyright 2010 Glencoe Software, Inc. All rights reserved. Use is subject to
 * license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;

import ome.system.OmeroContext;

import org.testng.annotations.Test;

/**
 * Extends {@link LdapUnitTest} to use a real db. All these tests which are
 * registered in LdapUnitTest will also be run here, but real changes will be
 * made to the database.
 */
@Test(groups = { "ldap", "integration" })
public class LdapIntegrationTest
    extends LdapUnitTest
{

    @Override
    protected Fixture createFixture(File ctxFile)
        throws Exception
    {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        return new IntegrationFixture(ctxFile, ctx);
    }

}
