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
import java.util.List;

import loci.formats.FormatTools;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.LegacyRepositoryI;
import ome.services.blitz.repo.ManagedRepositoryI;
import ome.system.Principal;

import omero.api.RawFileStorePrx;
import omero.grid.Import;
import omero.grid.RepositoryImportContainer;
import omero.model.Pixels;
import omero.util.TempFileManager;

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
        repo.setApplicationContext(user.ctx);

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
        ic.id = new Ice.Identity();
        ic.id.category = "fake";
        ic.id.name = p.getName();
        return ic;
    }

    /**
     * Generate a multi-file fake data set by touching "test.fake" and then
     * converting that into a multi-file format (here, ics). Thanks, Melissa.
     *
     * @param dir Directory in which the fakes are created.
     * @return {@link File} object for one of the two files that can be imported.
     * @throws Exception
     */
    protected ImportContainer makeFake(File dir) throws Exception {
        File fake = new File(dir, "test.fake");
        File ids = new File(dir, "test.ids");
        File ics = new File(dir, "test.ics");
        FileUtils.touch(fake);
        FormatTools.convert(fake.getAbsolutePath(), ids.getAbsolutePath());
        ImportContainer ic = new ImportContainer(ids, null /*proj*/, null /*target*/,
                Boolean.FALSE /*archive*/, null /*user pixels */,
                null /*reader*/, new String[]{ids.getAbsolutePath(), ics.getAbsolutePath()},
                Boolean.FALSE /*spw*/);
        return ic;
    }

    public void testBasicImportExample() throws Exception {
        File tmpDir = TempFileManager.create_path("mydata.", ".dir", true);
        ImportContainer ic = makeFake(tmpDir);

        Import i = repo.prepareImport(Arrays.asList(ic.getUsedFiles()), curr());
        assertNotNull(i);

        RawFileStorePrx file = null;
        try {
            file = repo.uploadUsedFile(i, i.usedFiles.get(0), curr());
            file.write(new byte[]{0}, 0, 1);
            file.close();
            file = repo.uploadUsedFile(i, i.usedFiles.get(1), curr());
            file.write(
                    new byte[]{0}, 0, 1);
            file.close();
            file = null;
            // FIXME: This should be a much simpler method call.
            RepositoryImportContainer repoIc =
                    ImportLibrary.createRepositoryImportContainer(ic);
            List<Pixels> pixList = repo.importMetadata(i, repoIc, curr());
        } finally {
            if (file != null) {
                file.close();
            }
        }

    }

}
