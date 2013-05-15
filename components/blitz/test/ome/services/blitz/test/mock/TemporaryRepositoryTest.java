/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import java.io.File;

import ome.model.core.OriginalFile;
import ome.model.internal.Permissions;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.PublicRepositoryI;
import ome.services.blitz.repo.RepositoryDaoImpl;
import ome.services.blitz.repo.TemporaryRepositoryI;
import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.testing.MockServiceFactory;
import ome.util.checksum.ChecksumProviderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import Ice.ObjectAdapter;

/**
 *
 */
public class TemporaryRepositoryTest extends MockObjectTestCase {

    MockFixture fixture;
    ObjectAdapter oa;
    Registry reg;
    ChecksumProviderFactory cpf;

    @BeforeClass(groups = "integration")
    public void setup() throws Exception {
        fixture = new MockFixture(this, "OMERO.mock");
        oa = fixture.blitz.getBlitzAdapter();
        // reg = fixture.blitz.getRegistry();
        Mock mockReg = mock(Registry.class);
        reg = (Registry) mockReg.proxy();
        mockReg.expects(atLeastOnce()).method("addObject");
        Mock mockCpf = mock(ChecksumProviderFactory.class);
        cpf = (ChecksumProviderFactory) mockCpf.proxy();
        mockCpf.expects(atLeastOnce()).method("getProvider()");
    }

    @Test(groups = "integration")
    public void testSimple() throws Exception {
        final OriginalFile repo = new OriginalFile();
        repo.setId(1L);
        repo.getDetails().setPermissions(Permissions.WORLD_IMMUTABLE);
        final MockServiceFactory sf = new MockServiceFactory();
        sf.mockConfig = fixture.mock("mock-ome.api.IConfig");
        sf.mockConfig.expects(once()).method("getDatabaseUuid").will(
                returnValue("mock-db-uuid"));
        sf.mockUpdate = fixture.mock("mock-ome.api.IUpdate");
        sf.mockUpdate.expects(once()).method("saveAndReturnObject").will(
                returnValue(repo));
        sf.mockQuery = fixture.mock("mock-ome.api.IQuery");
        sf.mockQuery.expects(once()).method("findByString").will(
                returnValue(repo));
        sf.mockQuery.expects(once()).method("findByQuery").will(
                returnValue(repo));

        Principal p = new Principal("session");
        TemporaryRepositoryI tr = new TemporaryRepositoryI(oa, reg, fixture.ex,
                p, new PublicRepositoryI(new RepositoryDaoImpl(p, fixture.ex),
                        cpf, null, FilePathRestrictionInstance.UNIX_REQUIRED.name));
        fixture.mock("executorMock").expects(atLeastOnce()).method("execute")
                .will(new Stub() {

                    public Object invoke(Invocation arg0) throws Throwable {
                        return ((Executor.Work) arg0.parameterValues.get(1))
                                .doWork(null, sf);
                    }

                    public StringBuffer describeTo(StringBuffer arg0) {
                        arg0.append(" calls doWork");
                        return arg0;
                    }
                });
        assertTrue(tr.takeover());
        assertNotNull(tr.getDescription());
    }

    @Test(groups = "integration")
    public void testFileUtils() throws Exception {
        String tmpPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpPath).getAbsoluteFile();
        tmpDir.list();
        System.out.println(FileUtils.listFiles(tmpDir, FileFilterUtils
                .fileFileFilter(), FileFilterUtils.falseFileFilter()));

    }
}
