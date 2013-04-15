/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.repo.test;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.UUID;

import ome.model.core.OriginalFile;
import ome.model.internal.Permissions;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.LegacyRepositoryI;
import ome.services.blitz.repo.PublicRepositoryI;
import ome.services.blitz.repo.RepositoryDao;
import ome.services.blitz.repo.RepositoryDaoImpl;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.testing.MockServiceFactory;
import ome.util.checksum.ChecksumProviderFactory;

import org.jmock.Mock;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = { "repo" })
public class LegacyRepositoryUnitTest extends AbstractRepoUnitTest {

    Mock exMock;
    Executor ex;
    Mock oaMock;
    Ice.ObjectAdapter oa;
    Mock regMock;
    Registry reg;
    Mock mockCpf;
    ChecksumProviderFactory cpf;

    MockServiceFactory sf;

    @BeforeMethod
    protected void beforeMethod() throws Exception {
        newTmpDir();
        exMock = mock(Executor.class);
        ex = (Executor) exMock.proxy();
        oaMock = mock(Ice.ObjectAdapter.class);
        oa = (Ice.ObjectAdapter) oaMock.proxy();
        regMock = mock(Registry.class);
        reg = (Registry) regMock.proxy();
        mockCpf = mock(ChecksumProviderFactory.class);
        cpf = (ChecksumProviderFactory) mockCpf.proxy();

        sf = new MockServiceFactory();
        sf.mockConfig.expects(atLeastOnce()).method("getDatabaseUuid").will(
                returnValue("mockuuid"));
        sf.mockQuery.expects(atLeastOnce()).method("findByString").will(
                returnValue(file()));

        exMock.expects(atLeastOnce()).method("execute").will(
                new ExecutorStub(sf));

    }

    private LegacyRepositoryI mk() throws Exception {
        Principal p = new Principal("sessionUuid", "system", "Internal");
        return new LegacyRepositoryI(oa, reg, ex, p, tmpRepo.getAbsolutePath(),
                new PublicRepositoryI(new RepositoryDaoImpl(p, ex), cpf, null));
    }

    private OriginalFile file() {
        OriginalFile f = new OriginalFile(1L, true);
        f.getDetails().setPermissions(Permissions.WORLD_IMMUTABLE);
        return f;
    }

    private void newRepoObject() {
        sf.mockUpdate.expects(once()).method("saveAndReturnObject").will(
                returnValue(file()));
    }

    private void addsRepoServices() {
        oaMock.expects(atLeastOnce()).method("add").will(returnValue(null));
        regMock.expects(atLeastOnce()).method("addObject");
        oaMock.expects(once()).method("activate");
    }

    public void testRepoAlredyInitialized() throws Exception {

        newRepoObject();
        addsRepoServices();
        final LegacyRepositoryI r1 = mk();
        assertTrue(r1.takeover());
        r1.close();

        addsRepoServices();
        final LegacyRepositoryI r2 = mk();
        assertTrue(r2.takeover());
        r2.close();
    }

    public void testNoTakeoverAfterClose() throws Exception {

        newRepoObject();
        addsRepoServices();
        final LegacyRepositoryI r1 = mk();
        assertTrue(r1.takeover());
        r1.close();
        assertFalse(r1.takeover());
        assertFalse(r1.takeover());
        assertFalse(r1.takeover());

    }

    public void testTakeoverOnlyOnce() throws Exception {
        newRepoObject();
        addsRepoServices();
        LegacyRepositoryI r1 = mk();
        assertTrue(r1.takeover());
        assertFalse(r1.takeover());
    }

    public void testLocking() throws Exception {
        newRepoObject();
        addsRepoServices();

        final LegacyRepositoryI r1 = mk();
        final LegacyRepositoryI r2 = mk();

        assertTrue(r1.takeover());

        class ShouldHang extends Thread {
            boolean tookOver = false;

            @Override
            public void run() {
                r2.takeover();
                tookOver = true;
            }
        }

        ShouldHang t = new ShouldHang();
        t.join(2000);
        assertFalse(t.tookOver);

    }

    public void testRaf() throws Exception {

        RandomAccessFile raf;
        String test;
        String uuid = UUID.randomUUID().toString();
        File blank = new File(tmpRepo, "blank.raf");
        File existing = new File(tmpRepo, "existing.raf");

        // Test blank RAF
        raf = new RandomAccessFile(blank, "rw");
        try {
            test = raf.readUTF();
            fail("EOF");
        } catch (java.io.EOFException eof) {
            // ok
        }

        // Test existing RAF
        raf = new RandomAccessFile(existing, "rw");
        raf.writeUTF(uuid);
        raf.close();
        raf = new RandomAccessFile(existing, "rw");
        test = raf.readUTF();
        assertEquals(uuid, test);
    }

    class ExecutorStub implements Stub {

        ServiceFactory sf;

        public ExecutorStub(ServiceFactory sf) {
            this.sf = sf;
        }

        public Object invoke(Invocation arg0) throws Throwable {
            Principal p = (Principal) arg0.parameterValues.get(0);
            Executor.SimpleWork work = (Executor.SimpleWork) arg0.parameterValues
                    .get(1);
            return work.doWork(null, this.sf);
        }

        public StringBuffer describeTo(StringBuffer arg0) {
            arg0.append("calls execute");
            return arg0;
        }
    }
}
