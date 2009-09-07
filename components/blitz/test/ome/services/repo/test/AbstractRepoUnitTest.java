/*
 *   $Id$
 *   
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.repo.test;

import java.io.File;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = { "repo" })
public class AbstractRepoUnitTest extends MockObjectTestCase {

    protected File tmpRepo;

    @BeforeClass
    public void newTmpDir() throws Exception {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File testOmero = new File(new File(tmpdir), "test-omero");
        testOmero.mkdirs();
        tmpRepo = File.createTempFile("tmp", "tst", testOmero);
        tmpRepo.delete();
        tmpRepo.mkdir();
    }

}
