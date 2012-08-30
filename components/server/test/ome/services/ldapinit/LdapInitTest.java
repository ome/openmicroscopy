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
package ome.services.ldapinit;

import java.io.File;

import ome.services.ldap.LdapTest;

import org.springframework.beans.FatalBeanException;
import org.testng.annotations.Test;

/**
 * Extends {@link LdapTest} to check that on startup
 * OMERO properly throws an exception if the LDAP
 * connection is invalid.
 */
@Test(groups = {"ldap", "ticket:1253"})
public class LdapInitTest extends LdapTest {

    @Override
    protected Fixture createFixture(File ctxFile)
        throws Exception {
        try {
            return super.createFixture(ctxFile);
        } catch (FatalBeanException fbe) {
            // pass
            return null;
        }
    }
}
