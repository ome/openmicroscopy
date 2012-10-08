/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import java.io.File;

import ome.services.blitz.fire.Registry;
import ome.services.blitz.repo.InternalRepositoryI;
import ome.services.blitz.repo.PublicRepositoryI;
import ome.services.blitz.repo.RepositoryDao;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import omero.util.TempFileManager;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import Ice.ObjectAdapter;

/**
 *
 */
public class StandaloneRepositoryTest extends MockObjectTestCase {

    OmeroContext ctx;
    MockFixture fixture;
    ObjectAdapter oa;
    Registry reg;
    Executor ex;
    File dir;

    @BeforeClass
    public void setup() throws Exception {
        dir = TempFileManager.create_path("repo", "test", true);
        System.setProperty("omero.repo.dir", dir.getAbsolutePath());
        ctx = OmeroContext.getInstance("OMERO.repository");
        ex = (Executor) ctx.getBean("executor");
        fixture = new MockFixture(this, ctx);
        oa = fixture.blitz.getBlitzAdapter();
        reg = fixture.blitz.getRegistry();
    }

    @Test
    public void testSimple() throws Exception {
        Principal p = new Principal("mock-uuid");
        InternalRepositoryI repo = new InternalRepositoryI(oa, reg, ex,
                p, dir.getAbsolutePath(),
                new PublicRepositoryI(new RepositoryDao(p, ex)));
    }

}
