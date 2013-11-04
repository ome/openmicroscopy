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
import java.util.HashMap;

import loci.formats.FormatTools;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import Ice.Current;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.LegacyRepositoryI;
import ome.services.blitz.repo.ManagedRepositoryI;
import ome.services.blitz.repo.RepositoryDaoImpl;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import ome.system.Principal;

import omero.api.AMD_RawFileStore_write;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.api._RawFileStoreTie;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportSettings;
import omero.model.Fileset;
import omero.model.FilesetI;
import omero.util.TempFileManager;

@Test(groups = { "integration", "repo", "fs" })
public class ManagedRepositoryITest extends AbstractServantTest {
    private final FilePathRestrictions conservativeRules =
            FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.values());

    FakeAdapter adapter;
    Mock adapterMock, regMock, sfMock;
    ManagedRepositoryI repo;
    LegacyRepositoryI internal;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        adapter = new FakeAdapter();
        adapterMock = (Mock) user.ctx.getBean("adapterMock");
        adapterMock.setDefaultStub(adapter);

        sfMock = mock(ServiceFactoryPrx.class);
        sfMock.setDefaultStub(new FakeProxy(null, user.sf));

        regMock = mock(Registry.class);
        regMock.expects(atLeastOnce()).method("getInternalServiceFactory")
            .will(returnValue(sfMock.proxy()));
        Registry reg = (Registry) regMock.proxy();

        final Principal rootPrincipal = root.getPrincipal();
        final Principal userPrincipal = user.getPrincipal();
        final File targetDir = new File(omeroDataDir, "ManagedRepo-"+userPrincipal.getName());
        targetDir.mkdirs();

        repo = new ManagedRepositoryI("template",
                new RepositoryDaoImpl(rootPrincipal, user.ex));
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
        ImportContainer ic = new ImportContainer(ids, null /*target*/,
                null /*user pixels */,
                null /*reader*/, new String[]{ids.getAbsolutePath(), ics.getAbsolutePath()},
                Boolean.FALSE /*spw*/);
        return ic;
    }

    public void testBasicImportExample() throws Exception {
        final ClientFilePathTransformer clientPaths = 
                new ClientFilePathTransformer(new MakePathComponentSafe(this.conservativeRules));
        
        File tmpDir = TempFileManager.create_path("mydata.", ".dir", true);
        ImportContainer ic = makeFake(tmpDir);
        ImportSettings settings = new ImportSettings();
        Fileset fs = new FilesetI();
        ic.fillData(new ImportConfig(), settings, fs, clientPaths);
        final Current curr = curr();
        settings.checksumAlgorithm = repo.suggestChecksumAlgorithm(repo.listChecksumAlgorithms(curr), curr);

        ImportProcessPrx i = repo.importFileset(fs, settings, curr);
        assertNotNull(i);

        upload(i.getUploader(0));
        upload(i.getUploader(1));

        // FIXME: TBD

    }

    void upload(RawFileStorePrx prx) throws Exception {
        final Exception[] ex = new Exception[1];
        final _RawFileStoreTie file = (_RawFileStoreTie) adapter.findByProxy(prx);
        try {
            file.write_async(new AMD_RawFileStore_write(){
                public void ice_response() {
                    // no-op
                }

                public void ice_exception(Exception ex2) {
                    ex[0] = ex2;
                }}, new byte[]{0}, 0, 1, curr());
        } finally {
            file.close_async(new AMD_StatefulServiceInterface_close(){

                public void ice_response() {
                    // no-op
                }

                public void ice_exception(Exception ex2) {
                    if (ex[0] == null) {
                        ex[0] = ex2;
                    } else {
                        ex2.printStackTrace();
                    }
                }}, curr());
        }
        if (ex[0] != null) {
            throw ex[0];
        }

    }
}
