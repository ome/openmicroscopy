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

import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.LegacyRepositoryI;
import ome.services.util.Executor;
import omero.api.IConfigPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.OriginalFileI;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = { "repo" })
public class LegacyRepositoryUnitTest extends MockObjectTestCase {

    File tmpRepo;
    Mock regMock;
    Registry reg;
    Mock exMock;
    Executor ex;
    Mock oaMock;
    Ice.ObjectAdapter oa;
    Mock sfMock;
    ServiceFactoryPrx sf;
    Mock cfgMock;
    IConfigPrx cfg;
    Mock qMock;
    IQueryPrx q;
    Mock uMock;
    IUpdatePrx u;

    @BeforeClass
    protected void beforeClass() throws Exception {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File testOmero = new File(new File(tmpdir), "test-omero");
        testOmero.mkdirs();
        tmpRepo = File.createTempFile("tmp", "tst", testOmero);
        tmpRepo.delete();
        tmpRepo.mkdir();
    }

    @BeforeMethod
    protected void beforeMethod() throws Exception {
        exMock = mock(Executor.class);
        ex = (Executor) exMock.proxy();
        regMock = mock(Registry.class);
        reg = (Registry) regMock.proxy();
        oaMock = mock(Ice.ObjectAdapter.class);
        oa = (Ice.ObjectAdapter) oaMock.proxy();
        sfMock = mock(ServiceFactoryPrx.class);
        sf = (ServiceFactoryPrx) sfMock.proxy();
        cfgMock = mock(IConfigPrx.class);
        cfg = (IConfigPrx) cfgMock.proxy();
        qMock = mock(IQueryPrx.class);
        q = (IQueryPrx) qMock.proxy();

        regMock.expects(atLeastOnce()).method("getInternalServiceFactory")
                .will(returnValue(sf));
        sfMock.expects(atLeastOnce()).method("destroy");
        sfMock.expects(atLeastOnce()).method("getConfigService").will(
                returnValue(cfg));
        sfMock.expects(atLeastOnce()).method("getQueryService").will(
                returnValue(q));

        cfgMock.expects(atLeastOnce()).method("getDatabaseUuid").will(
                returnValue(omero.rtypes.rstring("mockuuid")));
        qMock.expects(atLeastOnce()).method("findByString").will(
                returnValue(new OriginalFileI(1, false)));

    }

    private LegacyRepositoryI mk() {
        return new LegacyRepositoryI(oa, reg, ex, "sessuuid", tmpRepo
                .getAbsolutePath());
    }

    private void newRepoObject() {
        uMock = mock(IUpdatePrx.class);
        u = (IUpdatePrx) uMock.proxy();
        sfMock.expects(atLeastOnce()).method("getUpdateService").will(
                returnValue(u));
        uMock.expects(once()).method("saveAndReturnObject").will(
                returnValue(new OriginalFileI(1, false)));
    }

    private void addsRepoServices() {
        oaMock.expects(once()).method("add").will(returnValue(null));
        oaMock.expects(once()).method("add").will(returnValue(null));
        regMock.expects(once()).method("addObject");
        regMock.expects(once()).method("addObject");
        oaMock.expects(once()).method("activate");
    }

    public void testRepoAlredyInitialized() throws Exception {
        fail();
    }

    public void testObjectAlreadyRegistered() throws Exception {
        fail();
    }

    public void testTakeoverOnlyOnce() throws Exception {
        newRepoObject();
        addsRepoServices();
        LegacyRepositoryI r1 = mk();
        assertTrue(r1.takeover());
        assertFalse(r1.takeover());
    }

    public void testLocking() throws Exception {

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

}
