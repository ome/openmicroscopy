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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import org.jmock.Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.FileMaker;
import ome.services.blitz.repo.LegacyRepositoryI;
import ome.services.blitz.repo.ManagedRepositoryI;
import ome.services.blitz.repo.PublicRepositoryI;
import ome.services.util.Executor;
import ome.system.Principal;

import omero.grid.Import;

/**
 */
@Test(groups = { "integration", "repo", "fslite" })
public class ManagedRepositoryITest extends AbstractServantTest {

    Mock adapterMock;
    ManagedRepositoryI repo;
    LegacyRepositoryI internal;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        adapterMock = (Mock) user.ctx.getBean("adapterMock");
        adapterMock.setDefaultStub(new FakeAdapter());
        Registry reg = null;

        final Principal rootPrincipal = root.getPrincipal();
        final Principal userPrincipal = user.getPrincipal();
        final File targetDir = new File(omeroDataDir, "ManagedRepo-"+userPrincipal.getName());
        targetDir.mkdirs();

        repo = new ManagedRepositoryI("template", user.ex, userPrincipal, reg);
        internal = new LegacyRepositoryI(user.adapter, reg, user.ex, rootPrincipal,
                targetDir.getAbsolutePath(), repo);
        internal.takeover();
    }

    @AfterClass
    protected void tearDown() throws Exception {
        if (internal != null) {
            internal.close();
        }
        super.tearDown();
    }

    protected Ice.Current curr() {
        Principal p = user.getPrincipal();
        Ice.Current ic = new Ice.Current();
        ic.ctx = new HashMap<String, String>();
        ic.ctx.put(omero.constants.SESSIONUUID.value, p.getName());
        return ic;
    }

    /**
     */
    public void testBasicImportExample() throws Exception {
        Import i = repo.prepareImport(Arrays.asList("my-dir/test.txt"), curr());
        assertNotNull(i);
    }

}
